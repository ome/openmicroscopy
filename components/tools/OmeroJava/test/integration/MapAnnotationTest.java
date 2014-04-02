/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package integration;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.MapAnnotation;

import org.testng.annotations.Test;

/**
 * Testing of the new {@link MapAnnotation} feature including
 * the underlying support for {@link Map} fields.
 *
 * @since 5.1.0-m1
 */
public class MapAnnotationTest extends AbstractServerTest {

    @Test
    public void testStringMapField() throws Exception {
        String uuid = UUID.randomUUID().toString();
        IQueryPrx queryService = root.getSession().getQueryService();
        IUpdatePrx updateService = root.getSession().getUpdateService();
        ExperimenterGroup group = new ExperimenterGroupI();
        group.setName(omero.rtypes.rstring(uuid));
        group.setConfig(new HashMap<String, String>());
        group.getConfig().put("foo", "bar");
        group = (ExperimenterGroup) updateService.saveAndReturnObject(group);
        group = (ExperimenterGroup) queryService.findByQuery(
                "select g from ExperimenterGroup g join fetch g.config " +
                "where g.id = " + group.getId().getValue(), null);
        assertEquals("bar", group.getConfig().get("foo"));
    }

}
