/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.itests;

import ome.services.blitz.client.IceServiceFactory;
import ome.system.Login;
import omero.JString;
import omero.SecurityViolation;
import omero.ServerError;

import org.testng.annotations.Test;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

public class SecurityTest extends IceTest {


    @Test( groups = "ticket:645", expectedExceptions = SecurityViolation.class)
    public void testSynchronizeLoginCacheShouldBeDisallowed() throws Exception {
        IceServiceFactory user = createUser();
        user.getAdminService(null).synchronizeLoginCache();
    }

    @Test( groups = "ticket:645")
    public void testGetEventContextShouldBeAllowed() throws Exception {
        IceServiceFactory user = createUser();
        user.getAdminService(null).getEventContext();
    }

    private IceServiceFactory createUser() throws ServerError,
        CannotCreateSessionException, PermissionDeniedException {
        omero.model.Experimenter e = new omero.model.ExperimenterI();
        e.omeName = new JString(Ice.Util.generateUUID());
        e.firstName = new JString("ticket");
        e.lastName = new JString("645");
        root.getAdminService(null).createUser(e, "default");

        IceServiceFactory user = new IceServiceFactory(
                null, null, new Login(e.omeName.val,""));
        user.createSession();
        return user;
    }

}
