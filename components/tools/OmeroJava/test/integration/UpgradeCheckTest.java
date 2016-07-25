/*
 *------------------------------------------------------------------------------
 * Copyright (C) 2014 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package integration;

import ome.system.OmeroContext;
import ome.system.UpgradeCheck;

import org.testng.Assert;
import org.testng.annotations.Test;

public class UpgradeCheckTest {

    OmeroContext ctx = new OmeroContext(new String[]{"classpath:ome/config.xml"});
    String url = ctx.getProperty("omero.upgrades.url");
    String version = ctx.getProperty("omero.version");
    ome.system.UpgradeCheck check;

    @Test
    public void testNoResponse() throws Exception {
	    check = new UpgradeCheck(url, "test", "test");
	    check.run();
	    Assert.assertTrue(check.isUpgradeNeeded());
	    Assert.assertFalse(check.isExceptionThrown());

	}

}
