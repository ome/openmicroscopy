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

import static omero.rtypes.rstring;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import omero.api.IAdminPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.AcquisitionMode;
import omero.model.Annotation;
import omero.model.AnnotationAnnotationLinkI;
import omero.model.Arc;
import omero.model.BooleanAnnotation;
import omero.model.BooleanAnnotationI;
import omero.model.Channel;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.ContrastMethod;
import omero.model.Dataset;
import omero.model.DatasetAnnotationLink;
import omero.model.DatasetAnnotationLinkI;
import omero.model.DatasetI;
import omero.model.Detector;
import omero.model.Dichroic;
import omero.model.DoubleAnnotation;
import omero.model.DoubleAnnotationI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.Filament;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Filter;
import omero.model.FilterSet;
import omero.model.IObject;
import omero.model.Illumination;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.Instrument;
import omero.model.Laser;
import omero.model.LightSource;
import omero.model.LogicalChannel;
import omero.model.LongAnnotation;
import omero.model.LongAnnotationI;
import omero.model.OTF;
import omero.model.Objective;
import omero.model.OriginalFile;
import omero.model.PermissionsI;
import omero.model.Pixels;
import omero.model.PixelsAnnotationLink;
import omero.model.PixelsAnnotationLinkI;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.PlateAcquisitionAnnotationLink;
import omero.model.PlateAcquisitionAnnotationLinkI;
import omero.model.PlateAcquisitionI;
import omero.model.PlateAnnotationLink;
import omero.model.PlateAnnotationLinkI;
import omero.model.PlateI;
import omero.model.Project;
import omero.model.ProjectAnnotationLink;
import omero.model.ProjectAnnotationLinkI;
import omero.model.ProjectI;
import omero.model.Screen;
import omero.model.ScreenAnnotationLink;
import omero.model.ScreenAnnotationLinkI;
import omero.model.ScreenI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.TermAnnotation;
import omero.model.TermAnnotationI;
import omero.model.Well;
import omero.model.WellAnnotationLink;
import omero.model.WellAnnotationLinkI;
import omero.model.XmlAnnotation;
import omero.model.XmlAnnotationI;
import omero.sys.Parameters;
import omero.sys.ParametersI;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import pojos.BooleanAnnotationData;
import pojos.ChannelAcquisitionData;
import pojos.DoubleAnnotationData;
import pojos.FileAnnotationData;
import pojos.InstrumentData;
import pojos.LightSourceData;
import pojos.LongAnnotationData;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;
import pojos.XMLAnnotationData;

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
