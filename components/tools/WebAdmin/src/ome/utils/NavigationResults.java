/*
 * ome.admin.controller
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.utils;

/**
 * It's the navigation results interface.
 * 
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public interface NavigationResults {

    /**
     * Sucsess
     */
    public static final String SUCCESS = "success";

    /**
     * False
     */
    public static final String FALSE = "false";

    /**
     * Account
     */
    public static final String ACCOUNT = "account";

    /**
     * Logout
     */
    public static final String LOGOUT = "logout";

    /**
     * Expired
     */
    public static final String EXPIRED = "expired";

}
