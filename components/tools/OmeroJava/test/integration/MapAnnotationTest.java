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

import static omero.rtypes.rbool;
import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import omero.ServerError;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.MapAnnotation;
import omero.model.NamedValue;

import org.testng.annotations.Test;

/**
 * Testing of the new {@link MapAnnotation} feature including
 * the underlying support for {@link Map} fields.
 *
 * @since 5.1.0-m1
 */
public class MapAnnotationTest extends AbstractServerTest {

    /**
     * Test persistence of a foo &rarr; bar map.
     * @throws ServerError unexpected
     */
    @Test
    public void testStringMapField() throws ServerError {
        String uuid = UUID.randomUUID().toString();
        IQueryPrx queryService = root.getSession().getQueryService();
        IUpdatePrx updateService = root.getSession().getUpdateService();
        ExperimenterGroup group = new ExperimenterGroupI();
        group.setName(omero.rtypes.rstring(uuid));
        group.setLdap(rbool(false));
        group.setConfig(new ArrayList<NamedValue>());
        group.getConfig().add(new NamedValue("foo", "bar"));
        group = (ExperimenterGroup) updateService.saveAndReturnObject(group);
        group = (ExperimenterGroup) queryService.findByQuery(
                "select g from ExperimenterGroup g join fetch g.config " +
                "where g.id = " + group.getId().getValue(), null);
        assertEquals("foo", group.getConfig().get(0).name);
        assertEquals("bar", group.getConfig().get(0).value);
    }

    /**
     * Test persistence of a foo &rarr; empty string map.
     * @throws ServerError unexpected
     */
    @Test
    public void testStringMapEmptyField() throws Exception {
        String uuid = UUID.randomUUID().toString();
        IQueryPrx queryService = root.getSession().getQueryService();
        IUpdatePrx updateService = root.getSession().getUpdateService();
        ExperimenterGroup group = new ExperimenterGroupI();
        group.setName(omero.rtypes.rstring(uuid));
        group.setLdap(rbool(false));
        group.setConfig(new ArrayList<NamedValue>());
        group.getConfig().add(new NamedValue("foo", ""));
        group = (ExperimenterGroup) updateService.saveAndReturnObject(group);
        group = (ExperimenterGroup) queryService.findByQuery(
                "select g from ExperimenterGroup g join fetch g.config " +
                "where g.id = " + group.getId().getValue(), null);
        assertEquals("foo", group.getConfig().get(0).name);
        assertEquals("", group.getConfig().get(0).value);
    }

    /**
     * Test persistence of a bar &rarr; <code>null</code> map.
     * These null fields get converted to the empty string by Ice.
     */
    @Test
    public void testNulledMapValue() throws Exception {
        String uuid = UUID.randomUUID().toString();
        IUpdatePrx updateService = root.getSession().getUpdateService();
        ExperimenterGroup group = new ExperimenterGroupI();
        group.setName(omero.rtypes.rstring(uuid));
        group.setLdap(rbool(false));
        group.setConfig(new ArrayList<NamedValue>());
        group.getConfig().add(new NamedValue("foo", ""));
        group.getConfig().add(new NamedValue("bar", null));
        group = (ExperimenterGroup) updateService.saveAndReturnObject(group);
        assertEquals("", group.getConfig().get(0).value);
    }

    @Test
    public void testMapGetters() throws Exception {
        ExperimenterGroup group = new ExperimenterGroupI();
        group.setConfig(new ArrayList<NamedValue>());
        group.getConfig().add(new NamedValue("foo", ""));
        group.getConfig().add(new NamedValue("bar", null));
        group.getConfigAsMap();
    }
}
