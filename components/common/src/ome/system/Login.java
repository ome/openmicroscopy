/*
 * ome.system.Login
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.system;

import java.util.Properties;

import ome.conditions.ApiUsageException;

/**
 * Provides simplified handling of login properties when creating a
 * {@link ome.system.ServiceFactory}. For more complicated uses,
 * {@link java.util.Properties} can also be used. In which case, the constant
 * {@link java.lang.String strings} provided in this class can be used as the
 * keys to the {@link java.util.Properties properties instance} passed to
 * {@link ome.system.ServiceFactory#ServiceFactory(Properties)}.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * @see ome.system.ServiceFactory
 * @since 1.0
 */
public class Login {

    /**
     * Java property name for use in configuration of client login.
     */
    public final static String OMERO_USER = "omero.user";

    /**
     * Java property name for use in configuration of client login.
     */
    public final static String OMERO_GROUP = "omero.group";

    /**
     * Java property name for use in configuration of client login.
     */
    public final static String OMERO_PASS = "omero.pass";

    /**
     * Java property name for use in configuration of client login.
     */
    public final static String OMERO_EVENT = "omero.event";

    /**
     * {@link Login} constant which has username and password values set
     * to null and other values set to their default. This will permit logging
     * in as an anonymous user.
     */
    public final static Login GUEST = new Login() {
        @Override
        public Properties asProperties() {
            Properties p = super.asProperties();
            p.setProperty(OMERO_USER, null);
            p.setProperty(OMERO_PASS, null);
            p.setProperty(OMERO_GROUP, null);
            p.setProperty(OMERO_EVENT, null);
            return p;
        }
    };

    private String _user, _group, _pass, _event;

    // Need at least user and password
    private Login() {
    }

    /**
     * standard constructor which leaves OMERO_GROUP and OMERO_EVENT null.
     * 
     * @param user
     *            {@link ome.model.meta.Experimenter#getOmeName()}. Not null.
     * @param password
     *            Cleartext password. Not null.
     */
    public Login(String user, String password) {
        if (user == null || password == null) {
            throw new ApiUsageException("User and password arguments "
                    + "to Login constructor cannot be null");
        }
        _user = user;
        _pass = password;
    }

    /**
     * extended constructor. As with {@link #Login(String, String)}, user and
     * password may not be null.
     * 
     * @param user
     *            {@link ome.model.meta.Experimenter#getOmeName()}. Not null.
     * @param password
     *            Cleartext password. Not null.
     * @param group
     *            Group name. May be null.
     * @param event
     *            Enumeration value of the EventType. May be null.
     */
    public Login(String user, String password, String group, String event) {
        this(user, password);
        _group = group;
        _event = event;
    }

    // ~ Views
    // =========================================================================

    /**
     * produces a copy of the internal fields as a {@link java.util.Properties}
     * instance. Only those keys are present for which a field is non-null.
     * 
     * @return Properties. Not null.
     */
    public Properties asProperties() {
        Properties p = new Properties();
        p.setProperty(OMERO_USER, _user);
        p.setProperty(OMERO_PASS, _pass);
        if (_group != null) {
            p.setProperty(OMERO_GROUP, _group);
        }
        if (_event != null) {
            p.setProperty(OMERO_EVENT, _event);
        }
        return p;
    }

    /**
     * simple getter for the user name passed into the constructor
     * 
     * @return {@link ome.model.meta.Experimenter#getOmeName() user name}. Not
     *         null unless Login == {@link Login#GUEST}.
     */
    public String getName() {
        return _user;
    }

    /**
     * simple getter for the password passed into the constructor
     * 
     * @return password. Not null unless Login == {@link Login#GUEST}
     */
    public String getPassword() {
        return _pass;
    }

    /**
     * simple getter for the group name passed into the constructor
     * 
     * @return {@link ome.model.meta.ExperimenterGroup#getName() group name}.
     *         May be null.
     */
    public String getGroup() {
        return _group;
    }

    /**
     * simple getter for the event type passed into the constructor
     * 
     * @return {@link ome.model.enums.EventType event type}. May be null.
     */
    public String getEvent() {
        return _event;
    }

}
