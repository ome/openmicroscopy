/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.old;

import java.util.UUID;

import static omero.rtypes.*;
import omero.SecurityViolation;
import omero.ServerError;

import org.testng.annotations.Test;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

public class SecurityTest extends IceTest {

    @Test(groups = "ticket:645", expectedExceptions = SecurityViolation.class)
    public void testSynchronizeLoginCacheShouldBeDisallowed() throws Exception {
        omero.client user = createUser();
        user.getSession().getAdminService().synchronizeLoginCache();
    }

    @Test(groups = "ticket:645")
    public void testGetEventContextShouldBeAllowed() throws Exception {
        omero.client user = createUser();
        user.getSession().getAdminService().getEventContext();
    }

    private omero.client createUser() throws ServerError,
            CannotCreateSessionException, PermissionDeniedException {
        omero.model.Experimenter e = new omero.model.ExperimenterI();
        e.setOmeName(rstring(UUID.randomUUID().toString()));
        e.setFirstName(rstring("ticket"));
        e.setLastName(rstring("645"));
        root.getSession().getAdminService().createUser(e, "default");

        omero.client user = new omero.client();
        user.createSession(e.getOmeName().getValue(), "");
        return user;
    }

}
