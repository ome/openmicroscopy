/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

// Java imports
import java.util.List;

import ome.api.ILdap;
import ome.services.blitz.util.BlitzExecutor;
import omero.ApiUsageException;
import omero.SecurityViolation;
import omero.ServerError;
import omero.ValidationException;
import omero.api.AMD_ILdap_checkAttributes;
import omero.api.AMD_ILdap_findDN;
import omero.api.AMD_ILdap_searchAll;
import omero.api.AMD_ILdap_searchAttributes;
import omero.api.AMD_ILdap_searchByAttribute;
import omero.api.AMD_ILdap_searchByDN;
import omero.api.AMD_ILdap_searchDnInGroups;
import omero.api.AMD_ILdap_searchGroups;
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

    public void checkAttributes_async(AMD_ILdap_checkAttributes __cb,
            String dn, List<String> attrs, Current __current)
            throws ServerError {
        
        callInvokerOnRawArgs(__cb, __current, dn, attrs);
        
    }

    public void findDN_async(AMD_ILdap_findDN __cb, String username,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, username);        
    }

    public void searchAll_async(AMD_ILdap_searchAll __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
        
    }

    public void searchAttributes_async(AMD_ILdap_searchAttributes __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
        
    }

    public void searchByAttribute_async(AMD_ILdap_searchByAttribute __cb,
            String attribute, String value, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, attribute, value);
        
    }

    public void searchByDN_async(AMD_ILdap_searchByDN __cb, String userdn,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, userdn);
        
    }

    public void searchDnInGroups_async(AMD_ILdap_searchDnInGroups __cb,
            String attr, String value, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, attr, value);
        
    }

    public void searchGroups_async(AMD_ILdap_searchGroups __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
        
    }

    public void setDN_async(AMD_ILdap_setDN __cb, long experimenterID,
            String dn, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, experimenterID, dn);
        
    }

}
