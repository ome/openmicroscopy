/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.icy.fire;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.api.local.LocalAdmin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Glacier2._PermissionsVerifierDisp;
import Ice.Current;
import Ice.StringHolder;

/**
 * @author Josh Moore, josh.moore at gmx.de
 * @since 3.0-RC1
 */
@RevisionDate("$Date: 2006-12-15 12:28:54 +0100 (Fri, 15 Dec 2006) $")
@RevisionNumber("$Revision: 1175 $")
public class PermissionsVerifierI extends _PermissionsVerifierDisp {

    private final static Log log = LogFactory
            .getLog(PermissionsVerifierI.class);

    protected LocalAdmin rawAdmin;
    
    public PermissionsVerifierI(LocalAdmin adminService) {
        this.rawAdmin = adminService;
    }
    
    public boolean checkPermissions(String userId, String password,
            StringHolder reason, Current __current) {
        return rawAdmin.checkPassword(userId, password);
    }

}
