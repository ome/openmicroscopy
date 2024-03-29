/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015-2022 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package integration.gateway;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.UUID;

import integration.AbstractServerTest;
import omero.SecurityViolation;
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.AdminFacility;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.DataManagerFacility;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;
import omero.log.LogMessage;
import omero.log.SimpleLogger;
import omero.model.LongAnnotation;
import omero.model.LongAnnotationI;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;


/**
 * Tests the login options supported by gateway
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.1
 */
public class GatewayUsageTest extends AbstractServerTest
{

    @Test
    public void testLoginWithCredentials()
            throws DSOutOfServiceException {
        omero.client client =  new omero.client();
        String port = client.getProperty("omero.port");
        LoginCredentials c = new LoginCredentials();
        c.getServer().setHost(client.getProperty("omero.host"));
        c.getServer().setPort(Integer.parseInt(port));
        c.getUser().setUsername("root");
        c.getUser().setPassword(client.getProperty("omero.rootpass"));
        try (Gateway gw = new Gateway(new SimpleLogger())) {
            ExperimenterData root = gw.connect(c);
            Assert.assertNotNull(root);
        } catch (Exception e) {
            Assert.fail("Gateway credentials login failed.", e);
        }
    }

    @Test
    public void testLoginWithArgs()
            throws DSOutOfServiceException {
        omero.client client =  new omero.client();
        String[] args = new String[4];
        args[0] = "--omero.host="+client.getProperty("omero.host");
        args[1] = "--omero.port="+client.getProperty("omero.port");
        args[2] = "--omero.user=root";
        args[3] = "--omero.pass="+client.getProperty("omero.rootpass");
        LoginCredentials c = new LoginCredentials(args);
        try (Gateway gw = new Gateway(new SimpleLogger())) {
            ExperimenterData root = gw.connect(c);
            Assert.assertNotNull(root);
        } catch (Exception e) {
            Assert.fail("Gateway args login failed.", e);
        }
    }

    @Test
    public void testLoginFallback()
            throws Exception {
        omero.client client =  new omero.client();
        String port = client.getProperty("omero.port");
        LoginCredentials c = new LoginCredentials();
        c.getServer().setHost(client.getProperty("omero.host"));
        c.getServer().setPort(Integer.parseInt(port));
        c.getUser().setUsername("root");
        c.getUser().setPassword(client.getProperty("omero.rootpass"));
        Gateway gw = new Gateway(new SimpleLogger());
        ExperimenterData root = gw.connect(c);

        // Create a user with weird password
        SecurityContext ctx = new SecurityContext(root.getDefaultGroup().getGroupId());
        GroupData group = new GroupData();
        group.setName(UUID.randomUUID().toString());
        group = gw.getFacility(AdminFacility.class).createGroup(ctx, group, null, GroupData.PERMISSIONS_GROUP_READ_WRITE);
        ExperimenterData exp = new ExperimenterData();
        exp.setFirstName("Just");
        exp.setMiddleName("a");
        exp.setLastName(("Test"));
        String user = UUID.randomUUID().toString();
        String pw = " #\\pass=word\uD83D\uDE1C\t";
        exp = gw.getFacility(AdminFacility.class).createExperimenter(ctx, exp, user,
                pw, Collections.singletonList(group), false, false);
        gw.disconnect();

        class MyLog extends SimpleLogger {
            public boolean warning = false;
            @Override
            public void warn(Object originator, LogMessage msg) {
                System.out.println(msg.toString());
                warning = true;
            }

            @Override
            public void warn(Object originator, String logMsg) {
                System.out.println(logMsg);
                warning = true;
            }
        };
        MyLog log = new MyLog();

        // Try to login with this user via ice args.
        // args[] login has priority and will fail because the password is not escaped properly.
        // Fallback using the explicitly set credentials will work.
        try (Gateway g = new Gateway(log)) {
            String[] args = new String[4];
            args[0] = "--omero.user="+user;
            args[1] = "--omero.pass="+pw;
            args[2] = "--omero.host="+client.getProperty("omero.host");
            args[3] = "--omero.port="+Integer.parseInt(port);
            c = new LoginCredentials(args);
            c.getServer().setHost(client.getProperty("omero.host"));
            c.getServer().setPort(Integer.parseInt(port));
            c.getUser().setUsername(user);
            c.getUser().setPassword(pw);
            g.connect(c);
            Assert.assertTrue(log.warning, "Didn't get a warning about failed first login attempt.");
        }
        catch (Exception e1) {
            Assert.fail("Fallback Login failed.", e1);
        }
    }
    
    @Test
    public void testLoginWithSessionID() throws DSOutOfServiceException {
        omero.client client = new omero.client();
        String[] args = new String[4];
        args[0] = "--omero.host=" + client.getProperty("omero.host");
        args[1] = "--omero.port=" + client.getProperty("omero.port");
        args[2] = "--omero.user=root";
        args[3] = "--omero.pass=" + client.getProperty("omero.rootpass");
        LoginCredentials c = new LoginCredentials(args);

        try (Gateway gw = new Gateway(new SimpleLogger())) {
            ExperimenterData root = gw.connect(c);
            String sessionId = gw.getSessionId(root);
            try (Gateway gw2 = new Gateway(new SimpleLogger())) {
                LoginCredentials c2 = new LoginCredentials(sessionId, sessionId,
                        client.getProperty("omero.host"),
                        Integer.parseInt(client.getProperty("omero.port")));
                ExperimenterData root2 = gw2.connect(c2);
                Assert.assertNotNull(root2);
                Assert.assertEquals(gw2.getSessionId(root2), sessionId);
            } catch (Exception e) {
                Assert.fail("Gateway sessionId login failed.", e);
            }
        } catch (Exception e1) {
            Assert.fail("Gateway credentials login failed.", e1);
        }
    }

    @Test
    public void testPreserveSession() throws DSOutOfServiceException {
        omero.client client = new omero.client();
        String[] args = new String[4];
        args[0] = "--omero.host=" + client.getProperty("omero.host");
        args[1] = "--omero.port=" + client.getProperty("omero.port");
        args[2] = "--omero.user=root";
        args[3] = "--omero.pass=" + client.getProperty("omero.rootpass");
        LoginCredentials c = new LoginCredentials(args);

        String sessionId = "";
        // create a session
        try (Gateway gw = new Gateway(new SimpleLogger())) {
            ExperimenterData user = gw.connect(c);
            SecurityContext ctx = new SecurityContext(user.getGroupId());
            gw.closeSessionOnExit(ctx, false); // do not close the session when disconnecting
            sessionId = gw.getSessionId(user);
            Assert.assertNotNull(sessionId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // join session
        try (Gateway gw = new Gateway(new SimpleLogger())) {
            LoginCredentials c2 = new LoginCredentials(sessionId, sessionId,
                    client.getProperty("omero.host"),
                    Integer.parseInt(client.getProperty("omero.port")));
            gw.connect(c2);
            Assert.assertTrue(gw.isConnected());
            // JoinSessionCredentials by default should not close the server session
        } catch (Exception e) {
            e.printStackTrace();
        }

        // make sure the session is still active
        try (Gateway gw = new Gateway(new SimpleLogger())) {
            LoginCredentials c2 = new LoginCredentials(sessionId, sessionId,
                    client.getProperty("omero.host"),
                    Integer.parseInt(client.getProperty("omero.port")));
            ExperimenterData user = gw.connect(c2);
            Assert.assertTrue(gw.isConnected());
            SecurityContext ctx = new SecurityContext(user.getGroupId());
            gw.closeSessionOnExit(ctx, true); // force the session to be closed
        } catch (Exception e) {
            e.printStackTrace();
        }

        // make sure the session was closed
        try (Gateway gw = new Gateway(new SimpleLogger())) {
            LoginCredentials c2 = new LoginCredentials(sessionId, sessionId,
                    client.getProperty("omero.host"),
                    Integer.parseInt(client.getProperty("omero.port")));
            gw.connect(c2);
            Assert.fail("The session "+sessionId+" should have been closed.");
        } catch (Exception e) {
            // expected to fail
        }
    }

    Boolean sessionActive = null;

    @Test
    public void testAutoClose() throws DSOutOfServiceException {
        omero.client client = new omero.client();
        String[] args = new String[4];
        args[0] = "--omero.host=" + client.getProperty("omero.host");
        args[1] = "--omero.port=" + client.getProperty("omero.port");
        args[2] = "--omero.user=root";
        args[3] = "--omero.pass=" + client.getProperty("omero.rootpass");
        LoginCredentials c = new LoginCredentials(args);

        try (Gateway gw = new Gateway(new SimpleLogger())) {
            // do nothing; checks that auto close of non-connected Gateway
            // doesn't throw any exceptions.
        } catch (Exception e1) {
            Assert.fail("Gateway autoclose threw exception.", e1);
        }

        try (Gateway gw = new Gateway(new SimpleLogger())) {
            gw.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (Gateway.PROP_SESSION_CREATED.matches(evt.getPropertyName()))
                        sessionActive = Boolean.TRUE;
                    if (Gateway.PROP_SESSION_CLOSED.matches(evt.getPropertyName()))
                        sessionActive = Boolean.FALSE;
                }
            });
            Assert.assertNull(sessionActive);
            gw.connect(c);
            Assert.assertTrue(sessionActive);
        } catch (Exception e1) {
            Assert.fail("Gateway login failed.", e1);
        }
        Assert.assertFalse(sessionActive);
    }

    @Test
    public void testFailedLogin() throws DSOutOfServiceException {
        omero.client client = new omero.client();
        String port = client.getProperty("omero.port");

        LoginCredentials c = new LoginCredentials();
        c.getServer().setHost(client.getProperty("omero.host"));
        c.getServer().setPort(Integer.parseInt(port));
        c.getUser().setUsername("root");
        c.getUser().setPassword("wrongPassword");

        try (Gateway gw = new Gateway(new SimpleLogger())) {
            gw.connect(c);
            Assert.fail("Connection should have failed");
        } catch (Exception e) {
            // Something about false "credentials"
            Assert.assertTrue(e.getMessage().contains("credentials"));
        }

        c.getServer().setHost("UnknownHost");

        try (Gateway gw = new Gateway(new SimpleLogger())) {
            gw.connect(c);
            Assert.fail("Connection should have failed");
        } catch (Exception e) {
            // Something about "resolve" hostname failed
            Assert.assertTrue(e.getMessage().contains("resolve"));
        }
    }

    @Test
    public void testSwitchGroup() throws DSOutOfServiceException {
        omero.client client = new omero.client();
        String[] args = new String[4];
        args[0] = "--omero.host=" + client.getProperty("omero.host");
        args[1] = "--omero.port=" + client.getProperty("omero.port");
        args[2] = "--omero.user=root";
        args[3] = "--omero.pass=" + client.getProperty("omero.rootpass");
        LoginCredentials c = new LoginCredentials(args);

        long groupId = -1;

        // Create a new group
        try (Gateway gw = new Gateway(new SimpleLogger())) {
            ExperimenterData root = gw.connect(c);
            SecurityContext rootCtx = new SecurityContext(root.getGroupId());
            AdminFacility af = gw.getFacility(AdminFacility.class);
            GroupData g = new GroupData();
            g.setName(UUID.randomUUID().toString().substring(0, 8));
            g = af.createGroup(rootCtx, g, root,
                    GroupData.PERMISSIONS_GROUP_READ);
            groupId = g.getId();
            Assert.assertTrue(groupId > 0, "Create group failed");
        } catch (Exception e1) {
            Assert.fail("Create group failed.", e1);
        }

        // do something within the group context
        try (Gateway gw = new Gateway(new SimpleLogger())) {
            ExperimenterData root = gw.connect(c);

            // do something with root context...
            SecurityContext rootCtx = new SecurityContext(root.getGroupId());
            gw.getFacility(BrowseFacility.class).getDatasets(rootCtx);

            // then switch group
            SecurityContext groupCtx = new SecurityContext(groupId);
            DataManagerFacility df = gw.getFacility(DataManagerFacility.class);
            DatasetData ds = new DatasetData();
            ds.setName(UUID.randomUUID().toString().substring(0, 8));
            ds = df.createDataset(groupCtx, ds, null);
            Assert.assertTrue(ds.getId() >= 0,
                    "Dataset in new group was not created");
            Assert.assertEquals(ds.getGroupId(), groupId,
                    "Dataset does not belong to new group");
        } catch (Exception e1) {
            Assert.fail("Create dataset in new group failed.", e1);
        }
    }

    /**
     * Test that queries respond as expected to group context.
     * @throws Exception unexpected
     */
    @Test
    public void testGroupContextForService() throws Exception {
        /* as a normal user create an annotation */
        LongAnnotation annotation = new LongAnnotationI();
        annotation.setLongValue(omero.rtypes.rlong(1));
        annotation.setNs(omero.rtypes.rstring("test/" + getClass().getSimpleName() + "/" + UUID.randomUUID()));
        final long annotationId = iUpdate.saveAndReturnObject(annotation).getId().getValue();
        final long annotationGroupId = iAdmin.getEventContext().groupId;

        /* now do the rest of the test as the root user via the gateway */
        final LoginCredentials credentials = new LoginCredentials(
                roles.rootName, client.getProperty("omero.rootpass"),
                client.getProperty("omero.host"), Integer.valueOf(client.getProperty("omero.port")));
        try (final Gateway gateway = new Gateway(new SimpleLogger())) {
            gateway.connect(credentials);

            /* set query service to system group's context */
            iQuery = gateway.getQueryService(new SecurityContext(roles.systemGroupId));
            try {
                annotation = (LongAnnotation) iQuery.find(LongAnnotation.class.getSimpleName(), annotationId);
                Assert.fail("should not see annotation from wrong group");
            } catch (SecurityViolation sv) {
                /* expected */
            }

            /* do query in annotation group's context */
            iQuery = gateway.getQueryService(new SecurityContext(roles.systemGroupId));
            annotation = (LongAnnotation) iQuery.find(LongAnnotation.class.getSimpleName(), annotationId,
                    ImmutableMap.of("omero.group", Long.toString(annotationGroupId)));
            Assert.assertNotNull(annotation, "should see annotation from query in its group");

            /* set query service to annotation group's context */
            iQuery = gateway.getQueryService(new SecurityContext(annotationGroupId));
            annotation = (LongAnnotation) iQuery.find(LongAnnotation.class.getSimpleName(), annotationId);
            Assert.assertNotNull(annotation, "should see annotation from query service in its group");

            /* do query in all-groups context */
            iQuery = gateway.getQueryService(new SecurityContext(roles.systemGroupId));
            annotation = (LongAnnotation) iQuery.find(LongAnnotation.class.getSimpleName(), annotationId,
                    ALL_GROUPS_CONTEXT);
            Assert.assertNotNull(annotation, "should see annotation from query in all-groups context");

            try {
                /* set query service to all-groups context */
                iQuery = gateway.getQueryService(new SecurityContext(-1));
                Assert.fail("should not be possible without an equivalent of SERVICE_OPTS");
            } catch (DSOutOfServiceException dsoose) {
                Assert.assertEquals(dsoose.getCause().getClass(), IllegalArgumentException.class);
            }
        }
    }
}
