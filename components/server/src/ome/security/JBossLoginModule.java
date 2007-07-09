/*
 * ome.security.JBossLoginModule
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

// Java imports
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.naming.AuthenticationException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;

//Third-party libraries
import org.jboss.security.auth.spi.DatabaseServerLoginModule;

// Application-internal dependencies

/**
 * configured in jboss-login.xml to add logic to the JBoss authentication
 * procedure.
 * 
 * Specifically, we override {@link #validatePassword(String, String)} here in
 * order to interpret empty string passwords as "open", i.e. any password will
 * be accepted. This eases entry into the system in that passwords can be
 * initially ignored.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date:
 *          2007-04-03 20:11:19 +0100 (Tue, 03 Apr 2007) $) </small>
 * @since 1.0
 */
public class JBossLoginModule extends DatabaseServerLoginModule {

	protected String dnQuery = "select Dn from Principals where PrincipalID=?";

	/** overrides password creation for testing purposes */
	@Override
	protected String createPasswordHash(String arg0, String arg1, String arg2)
			throws LoginException {
		String retVal = super.createPasswordHash(arg0, arg1, arg2);
		return retVal;
	}

	/**
	 * Overrides the standard behavior of returning false (bad match) for all
	 * differing passwords. Here, we allow stored passwords to be empty which
	 * signifies that anyone can use the account, regardless of password.
	 */
	@Override
	protected boolean validatePassword(String inputPassword,
			String expectedPassword) {

		//validate password by LDAP if DN exists.
		try {
			String base = getUsersDn();
			if (base != null) {
				if (LdapUtil.validatePassword(base, super
						.getUsernameAndPassword()[1]))
					return true;
				else
					return false;
			}
		} catch (LoginException e) {
			log.error("Authentication failure! Login exception.");
			return false;
		} catch (AuthenticationException ae) {
			log.error("Login or Password Incorrect/Password Required.");
			return false;
		} catch (NamingException e) {
			log.error("Naming exception. Check existing configuration files.");
			return false;
		}

		if (null != expectedPassword && expectedPassword.trim().length() <= 0) {
			return true;
		}
		return super.validatePassword(inputPassword == null ? null
				: inputPassword.trim(), expectedPassword == null ? null
				: expectedPassword.trim());
	}

	/**
	 * Get the expected password for the current username available via the
	 * getUsername() method. This is called from within the login() method after
	 * the CallbackHandler has returned the username and candidate password.
	 * If password is not set on DB will be checked on LDAP. It means that 
	 * if there is possibility to create context for user's DN, password is correct.
	 * There is no getting password form LDAP.
	 * 
	 * @return the valid password String
	 */
	@Override
	protected String getUsersPassword() throws LoginException {
		String username = getUsername();
		String password = null;
		String dn = null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			InitialContext ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup(dsJndiName);
			conn = ds.getConnection();
			// Get the password
			ps = conn.prepareStatement(principalsQuery);
			ps.setString(1, username);
			rs = ps.executeQuery();
			if (rs.next() == false) {
				// If user is not in DB import his details from LDAP (without
				// password) and set his DN
				boolean login = LdapUtil.getUserFromLdap(username,
						getUsernameAndPassword()[1]);
				if (login) {
					password = createPasswordHash(username,
							getUsernameAndPassword()[1], "digestCallback");
				} else
					throw new FailedLoginException(
							"LDAP Error: No matching username found in Principals");
			} else {
				dn = rs.getString(2);
				if (dn != null) {
					if (LdapUtil.validatePassword(dn,
							getUsernameAndPassword()[1]))
						password = createPasswordHash(username,
								getUsernameAndPassword()[1], "digestCallback");
					else
						throw new FailedLoginException(
								"LDAP Error: No matching username found in Principals");

				} else {
					password = rs.getString(1);
					password = convertRawPassword(password);
				}
			}
		} catch (AuthenticationException ae) {
			log.error("Login or Password Incorrect/Password Required.");
			throw new LoginException(ae.toString(true));
		} catch (NamingException ex) {
			throw new LoginException(ex.toString(true));
		} catch (SQLException ex) {
			log.error("Query failed", ex);
			throw new LoginException(ex.toString());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {
				}
			}
		}
		return password;
	}

	/**
	 * Initialize this LoginModule.
	 */
	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map sharedState, Map options) {
		super.initialize(subject, callbackHandler, sharedState, options);
		dsJndiName = (String) options.get("dsJndiName");
		if (dsJndiName == null)
			dsJndiName = "java:/DefaultDS";
		Object tmp = options.get("dnQuery");
		if (tmp != null)
			dnQuery = tmp.toString();
		log.trace("dnQuery=" + rolesQuery);
	}

	/**
	 * Execute the UsersDnQuery against the dsJndiName to obtain the DN for the
	 * authenticated user.
	 * 
	 * @return String containing the sets of DN
	 */
	protected String getUsersDn() throws LoginException {
		String username = getUsername();
		String dn = null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			InitialContext ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup(dsJndiName);
			conn = ds.getConnection();
			// Get the DN
			ps = conn.prepareStatement(dnQuery);
			ps.setString(1, username);
			rs = ps.executeQuery();
			if (rs.next() == false)
				throw new FailedLoginException(
						"No matching username found in Principals");

			dn = rs.getString(1);
		} catch (NamingException ex) {
			throw new LoginException(ex.toString(true));
		} catch (SQLException ex) {
			log.error("Query failed", ex);
			throw new LoginException(ex.toString());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {
				}
			}
		}
		return dn;
	}

}
