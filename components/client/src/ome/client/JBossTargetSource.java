/*
 * ome.client.JBossTargetSource
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package ome.client;

//Java imports
import java.security.Principal;
import javax.naming.NamingException;

//Third-party libraries
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.springframework.aop.TargetSource;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.jndi.JndiObjectTargetSource;
import org.springframework.util.Assert;

//Application-internal dependencies

/** 
 * mostly a copy of {@link SingletonTargetSource} responsible for performing
 * login properly on all calls to JBoss.
 *  
 *  @author  <br>Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:josh.more@gmx.de">
 *                  josh.moore@gmx.de</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME3.0
 * @see ConfigurableJndiObjectFactoryBean#getObject()
 */
public class JBossTargetSource implements TargetSource
{
	/**
	 * sets authentication information on a per-Thread basis.
	 */
	static {
		SecurityAssociation.setServer();
	}
	
	protected JndiObjectTargetSource target;
	
	protected Principal principal;
	
	protected String credentials;
	
	/** creates a {@link JBossTargetSource} which performs proper login 
	 * on every access. 
	 */
    public JBossTargetSource( 
    		JndiObjectTargetSource target,
    		Principal securityPrincipal,
    		String securityCredentials )
    {
    	Assert.notNull(target,"Target is required");
    	Assert.notNull(securityPrincipal, "Principal is required.");
    	Assert.notNull(securityCredentials, "Credentials required.");
    	this.target = target;
    	this.principal = securityPrincipal;
    	this.credentials = securityCredentials;
    }

    /** delegates to {@link JndiObjectTargetSource#getTargetClass()} */
	public Class getTargetClass() {
		return this.target.getTargetClass();
	}
	
	/** delegates to {@link JndiObjectTargetSource#getTarget()} and then
	 * sets security creditials for this thread
	 */
	public Object getTarget() throws NamingException {
		Object retVal = this.target.getTarget();
		
	      // Associate this security context
	      SecurityAssociation.setPrincipal(principal);
	      SecurityAssociation.setCredential(credentials);
	      return retVal;
	}
	
	/** delegates to {@link JndiObjectTargetSource#releaseTarget(Object)} */
	public void releaseTarget(Object target) {
		this.target.releaseTarget(target);
	}

	/** delegates to {@link JndiObjectTargetSource#isStatic()} */
	public boolean isStatic() {
		return this.target.isStatic();
	}


	/** delegates to {@link JndiObjectTargetSource#hashCode()}  
	 */	
	public int hashCode() {
		return this.target.hashCode();
	}

	/**
	 * Two invoker interceptors are equal if they have the same target or if the
	 * targets or the targets are equal.
	 */
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof JBossTargetSource)) {
			return false;
		}
		JBossTargetSource otherTargetSource = (JBossTargetSource) other;
		return this.target.equals(otherTargetSource.target);
	}

	public String toString() {
		return "JBossTargetSource for target: " + this.target;
	}
    
}
