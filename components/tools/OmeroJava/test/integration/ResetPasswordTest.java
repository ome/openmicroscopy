/*
 * Copyright (C) 2020 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package integration;

import org.testng.Assert;
import org.testng.annotations.Test;

import omero.cmd.ResetPasswordRequest;
import omero.cmd.ResetPasswordResponse;
import omero.cmd.Response;
import omero.model.Experimenter;
import omero.sys.EventContext;

import Glacier2.PermissionDeniedException;

/**
 * Test the reset password function.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.6.2
 */
public class ResetPasswordTest extends AbstractServerTest {

    /**
     * Test that resetting a password with a correct e-mail reports success and does affect further logins.
     * @throws Exception unexpected
     */
    @Test
    public void testPasswordResetEmailGood() throws Exception {
        final EventContext ec = iAdmin.getEventContext();

        /* Set up a user with e-mail. */
        final String email = "nobody@example.org";
        final Experimenter user = iAdmin.getExperimenter(ec.userId);
        user.setEmail(omero.rtypes.rstring(email));
        iAdmin.updateSelf(user);

        /* Login should succeed. */
        loginUser(ec);

        /* As guest reset the user's password. */
        loginUser(roles.guestName);
        final ResetPasswordRequest req = new ResetPasswordRequest();
        req.omename = ec.userName;
        req.email = email;
        final Response rsp = doChange(req);
        Assert.assertTrue(rsp instanceof ResetPasswordResponse);

        try {
            /* Login should fail. */
            loginUser(ec);
            Assert.fail("password should have been changed");
        } catch (PermissionDeniedException pde) {
            /* expected */
        }
    }

    /**
     * Test that resetting a password with an incorrect e-mail reports failure and does not affect further logins.
     * @param hasEmail if the target user should have an e-mail address
     * @param useEmail if the reset request should use an e-mail address
     * @throws Exception unexpected
     */
    @Test(dataProvider = "test cases using two Boolean arguments")
    public void testPasswordResetEmailBad(boolean hasEmail, boolean useEmail) throws Exception {
        final EventContext ec = iAdmin.getEventContext();

        /* Set up a user with e-mail or not. */
        final String email = "correct@example.org";
        final Experimenter user = iAdmin.getExperimenter(ec.userId);
        user.setEmail(hasEmail ? omero.rtypes.rstring(email) : null);
        iAdmin.updateSelf(user);

        /* Login should succeed. */
        loginUser(ec);

        /* As guest reset the user's password. */
        loginUser(roles.guestName);
        final ResetPasswordRequest req = new ResetPasswordRequest();
        req.omename = ec.userName;
        req.email = useEmail ? "wrong@example.org" : null;
        Assert.assertNotEquals(req.email, email);
        doChange(client, factory, req, false);  // fails

        /* Login should succeed. */
        loginUser(ec);
    }
}
