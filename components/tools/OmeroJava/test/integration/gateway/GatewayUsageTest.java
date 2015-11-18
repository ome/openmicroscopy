/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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

import integration.AbstractServerTest;
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.exception.DSAccessException;
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
            throws DSAccessException {
        omero.client client =  new omero.client();
        String port = client.getProperty("omero.port");
        LoginCredentials c = new LoginCredentials();
        c.getServer().setHostname(client.getProperty("omero.host"));
        c.getServer().setPort(Integer.parseInt(port));
        c.getUser().setUsername("root");
        c.getUser().setPassword(client.getProperty("omero.rootpass"));
        Gateway gw = null;
        try {
            gw = new Gateway(new SimpleLogger());
            ExperimenterData root = gw.connect(c);
            Assert.assertNotNull(root);
        } catch (Exception e) {
            throw new DSAccessException("Not able to connect using credentials",
                    e);
        } finally {
            if (gw != null) {
                gw.disconnect();
            }
            client.__del__();
        }
    }

    @Test
    public void testLoginWithArgs()
            throws DSAccessException {
        omero.client client =  new omero.client();
        String[] args = new String[4];
        args[0] = "--omero.host="+client.getProperty("omero.host");
        args[1] = "--omero.port="+client.getProperty("omero.port");
        args[2] = "--omero.user=root";
        args[3] = "--omero.pass="+client.getProperty("omero.rootpass");
        LoginCredentials c = new LoginCredentials(args);
        Gateway gw = null;
        try {
            gw = new Gateway(new SimpleLogger());
            ExperimenterData root = gw.connect(c);
            Assert.assertNotNull(root);
        } catch (Exception e) {
            throw new DSAccessException("Not able to connect using args",
                    e);
        } finally {
            if (gw != null) {
                gw.disconnect();
            }
            client.__del__();
        }
    }
}
