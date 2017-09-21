/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015-2017 University of Dundee. All rights reserved.
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

import integration.AbstractServerTest;
import omero.gateway.Gateway;
import omero.gateway.JoinSessionCredentials;
import omero.gateway.LoginCredentials;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.ExperimenterData;
import omero.log.SimpleLogger;

import org.testng.Assert;
import org.testng.annotations.Test;


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
        c.getServer().setHostname(client.getProperty("omero.host"));
        c.getServer().setPort(Integer.parseInt(port));
        c.getUser().setUsername("root");
        c.getUser().setPassword(client.getProperty("omero.rootpass"));
        try (Gateway gw = new Gateway(new SimpleLogger())) {
            ExperimenterData root = gw.connect(c);
            Assert.assertNotNull(root);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
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
            e.printStackTrace();
            Assert.fail();
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
                JoinSessionCredentials c2 = new JoinSessionCredentials(
                        sessionId, client.getProperty("omero.host"),
                        Integer.parseInt(client.getProperty("omero.port")));
                ExperimenterData root2 = gw2.connect(c2);
                Assert.assertNotNull(root2);
                Assert.assertEquals(gw2.getSessionId(root2), sessionId);
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail();
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
            e1.printStackTrace();
            Assert.fail();
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
            e1.printStackTrace();
            Assert.fail();
        }
        Assert.assertFalse(sessionActive);
    }
    
    @Test
    public void testFailedLogin() throws DSOutOfServiceException {
        omero.client client = new omero.client();
        String port = client.getProperty("omero.port");

        LoginCredentials c = new LoginCredentials();
        c.getServer().setHostname(client.getProperty("omero.host"));
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

        c.getServer().setHostname("UnknownHost");
        
        try (Gateway gw = new Gateway(new SimpleLogger())) {
            gw.connect(c);
            Assert.fail("Connection should have failed");
        } catch (Exception e) {
            // Something about "resolve" hostname failed
            Assert.assertTrue(e.getMessage().contains("resolve"));
        }
    }

}
