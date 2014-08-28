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
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.UUID;

import omero.ServerError;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.model.Annotation;
import omero.model.CommentAnnotationI;
import omero.model.Detector;
import omero.model.DetectorAnnotationLink;
import omero.model.DetectorAnnotationLinkI;
import omero.model.DetectorI;
import omero.model.CommentAnnotation;
import omero.model.Dichroic;
import omero.model.Filter;
import omero.model.Instrument;
import omero.model.InstrumentI;
import omero.model.Laser;
import omero.model.Objective;

import org.testng.annotations.Test;

/**
 * Testing of the range of {@link AnnotationRef} locations
 * added to the model.
 * @since 5.1.0-m1
 */
public class ExtendedAnnotationTest extends AbstractServerTest {

    /**
     * Test annotations on instrument.
     * @throws Exception
     */
    @Test
    public void testAnnotationOnDetector() throws Exception {
        String uuid = UUID.randomUUID().toString();

        Instrument instrument = (Instrument) iUpdate
                .saveAndReturnObject(mmFactory.createInstrument(uuid));
        assertNotNull(instrument);

 /*       Filter f = mmFactory.createFilter(500, 560);
        f.setInstrument((Instrument) instrument.proxy());
        f = (Filter) iUpdate.saveAndReturnObject(f);
        assertNotNull(f);

        Dichroic di = mmFactory.createDichroic();
        di.setInstrument((Instrument) instrument.proxy());
        di = (Dichroic) iUpdate.saveAndReturnObject(di);
        assertNotNull(di);

        Objective o = mmFactory.createObjective();
        o.setInstrument((Instrument) instrument.proxy());
        o = (Objective) iUpdate.saveAndReturnObject(o);
        assertNotNull(o);

        Laser l = mmFactory.createLaser();
        l.setInstrument((Instrument) instrument.proxy());
        l = (Laser) iUpdate.saveAndReturnObject(l);
        assertNotNull(l);
*/

        // creation
        Detector detector = mmFactory.createDetector();
        detector.setInstrument((Instrument) instrument.proxy());
        detector = (Detector) iUpdate.saveAndReturnObject(detector);
        assertNotNull(detector);
        
        // updating
        detector.setManufacturer(omero.rtypes.rstring("OME Inc"));
        detector = (Detector) iUpdate.saveAndReturnObject(detector);
        assertNotNull(detector);
        
        // Create annotation
        CommentAnnotation annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("comment"));
        Annotation ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
        DetectorAnnotationLink dal = new DetectorAnnotationLinkI();
        dal.link(detector, ann);
        dal = (DetectorAnnotationLink) iUpdate.saveAndReturnObject(dal);
        detector = (Detector) iUpdate.saveAndReturnObject(detector);
        
        // retrieval
        String sql = "select d from Detector as d where d.id = " + detector.getId().getValue();
        detector = (Detector) iQuery.findByQuery(sql, null);
        
        assertTrue(detector.isAnnotated());
        assertEquals(1, detector.sizeOfAnnotationLinks());
//        detector.reloadAnnotationLinks(arg0, arg1);
//        assertNotNull(detector.linkedAnnotationList());
//        sql = "select a from Annotation as a where a.id = " + detector.linkedAnnotationList().get(0).getId().getValue();
//        ann = (Annotation) iQuery.findByQuery(sql, null);
        
        // comparison
        assertEquals("OME Inc", detector.getManufacturer().getValue());
    }
}
