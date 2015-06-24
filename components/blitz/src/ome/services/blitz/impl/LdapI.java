/*
 *   $Id$
 *
 *   Copyright 2008-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

// Java imports
import java.util.List;

import ome.api.ILdap;
import ome.services.blitz.util.BlitzExecutor;
import omero.ServerError;
import omero.api.AMD_ILdap_createUser;
import omero.api.AMD_ILdap_discover;
import omero.api.AMD_ILdap_discoverGroups;
import omero.api.AMD_ILdap_findDN;
import omero.api.AMD_ILdap_findExperimenter;
import omero.api.AMD_ILdap_findGroup;
import omero.api.AMD_ILdap_findGroupDN;
import omero.api.AMD_ILdap_getSetting;
import omero.api.AMD_ILdap_searchAll;
import omero.api.AMD_ILdap_searchByAttribute;
import omero.api.AMD_ILdap_searchByAttributes;
import omero.api.AMD_ILdap_searchByDN;
import omero.api.AMD_ILdap_searchDnInGroups;
import omero.api.AMD_ILdap_setDN;
import omero.api._ILdapOperations;
import Ice.Current;

/**
 * Implementation of the ILdap service.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see ome.api.ILdap
 */
public class LdapI extends AbstractAmdServant implements _ILdapOperations {

    public LdapI(ILdap service, BlitzExecutor be) {
        super(service, be);
    }

    // Interface methods
    // =========================================================================

    /*
     * public void checkAttributes_async(AMD_ILdap_checkAttributes __cb, String
     * dn, List<String> attrs, Current __current) throws ServerError {
     * 
     * callInvokerOnRawArgs(__cb, __current, dn, attrs); }
     */

    public void searchAll_async(AMD_ILdap_searchAll __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
    }

    public void searchDnInGroups_async(AMD_ILdap_searchDnInGroups __cb,
            String attribute, String value, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, attribute, value);
    }

    public void searchByAttribute_async(AMD_ILdap_searchByAttribute __cb,
            String dn, String attribute, String value, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, dn, attribute, value);
    }

    public void searchByAttributes_async(AMD_ILdap_searchByAttributes __cb,
            String dn, List<String> attributes, List<String> values,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, dn, attributes, values);
    }

    public void searchByDN_async(AMD_ILdap_searchByDN __cb, String userdn,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, userdn);
    }

    public void findDN_async(AMD_ILdap_findDN __cb, String username,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, username);
    }

    public void findGroupDN_async(AMD_ILdap_findGroupDN __cb, String groupname,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, groupname);
    }

    public void findExperimenter_async(AMD_ILdap_findExperimenter __cb,
            String username, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, username);
    }

    public void findGroup_async(AMD_ILdap_findGroup __cb, String groupname,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, groupname);
    }

    public void setDN_async(AMD_ILdap_setDN __cb, omero.RLong experimenterID,
            String dn, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, experimenterID, dn);
    }

    public void getSetting_async(AMD_ILdap_getSetting __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
    }

    public void createUser_async(AMD_ILdap_createUser __cb,
            String username, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, username);
    }

    public void discover_async(AMD_ILdap_discover __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
    }

    public void discoverGroups_async(AMD_ILdap_discoverGroups __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
    }

}
