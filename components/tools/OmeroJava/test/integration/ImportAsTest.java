/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2020 University of Dundee. All rights reserved.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package integration;

import java.util.Collections;


import omero.SecurityViolation;
import omero.api.ISessionPrx;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.Session;
import omero.sys.EventContext;
import omero.sys.Principal;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests import as another user
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.6.1
 */
@Test(groups = { "import", "integration"})
public class ImportAsTest extends AbstractServerTest {


    @DataProvider(name = "role")
    public Object[][] dataProviderMethod() {
        return new Object[][] { { "owner" }, { "admin" } };
    }

    @Test(dataProvider = "role")
    public void testImportAs(String role) throws Throwable {
        EventContext sudoer;
        if (role.equals("owner")) {
           sudoer = newUserAndGroup("rwr---", true);
        } else {
           sudoer = newUserAndGroup("rwr---");
           ExperimenterGroup systemGroup = new ExperimenterGroupI(roles.systemGroupId, false);
           addUsers(systemGroup, Collections.singletonList(sudoer.userId), false);
        }
        final EventContext scientist = addUser(sudoer, false, false);

        /* The sudoer takes on the scientist's identity. */
        loginUser(sudoer);
        final Principal principal = new Principal();
        principal.name = scientist.userName;
        principal.group = iAdmin.getEventContext().groupName;
        principal.eventType = "User";
        final ISessionPrx iSession = factory.getSessionService();
        final Session session = iSession.createSessionWithTimeout(principal, 50000);
        final omero.client client = newOmeroClient();
        client.joinSession(session.getUuid().getValue());
        init(client);
        // Now we create an importer for that user
        boolean value = importImageFile("testImportAsGroupOwner");
        if (role.equals("owner")) {
            Assert.assertFalse(value); // This should be true
        } else {
            Assert.assertTrue(value);
        }
    }

}