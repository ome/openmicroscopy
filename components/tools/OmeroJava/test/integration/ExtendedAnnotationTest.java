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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import omero.api.IMetadataPrx;
import omero.model.CommentAnnotationI;
import omero.model.Detector;
import omero.model.DetectorAnnotationLink;
import omero.model.DetectorAnnotationLinkI;
import omero.model.CommentAnnotation;
import omero.model.IObject;
import omero.model.Instrument;
import omero.sys.Parameters;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Testing of the range of {@link AnnotationRef} locations
 * added to the model.
 * @since 5.1.0-m1
 */
public class ExtendedAnnotationTest extends AbstractServerTest {

    /**
     * Test annotations on detector in instrument.
     * Retrieve via simplest methods
     * @throws Exception
     */
    @Test
    public void testAnnotationOnDetector() throws Exception {
        String uuid = UUID.randomUUID().toString();

        Instrument instrument = (Instrument) iUpdate
                .saveAndReturnObject(mmFactory.createInstrument(uuid));
        Assert.assertNotNull(instrument);

        // creation
        Detector detector = mmFactory.createDetector();
        detector.setInstrument((Instrument) instrument.proxy());
        detector = (Detector) iUpdate.saveAndReturnObject(detector);
        Assert.assertNotNull(detector);

        // updating
        detector.setManufacturer(omero.rtypes.rstring("OME-Sample Inc"));
        detector = (Detector) iUpdate.saveAndReturnObject(detector);
        Assert.assertNotNull(detector);

        // Create annotation
        CommentAnnotation annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("commentOnDetector"));
        DetectorAnnotationLink dal = new DetectorAnnotationLinkI();
        dal.setParent((Detector)detector.proxy());
        dal.setChild(annotation);
        dal = (DetectorAnnotationLink) iUpdate.saveAndReturnObject(dal);
        Assert.assertNotNull(dal);

        // retrieval
        String sql = "select d from Detector as d left outer join fetch d.annotationLinks as link left outer join fetch link.child as child where d.id = " + detector.getId().getValue();
        detector = (Detector) iQuery.findByQuery(sql, null);
        Assert.assertNotNull(detector);
        Assert.assertTrue(detector.isAnnotated());
        List<DetectorAnnotationLink> listOfLinks = detector.copyAnnotationLinks();
        Assert.assertNotNull(listOfLinks);
        Assert.assertEquals(1, listOfLinks.size());
        Assert.assertNotNull(listOfLinks.get(0));
        DetectorAnnotationLink newDal = listOfLinks.get(0);
        Assert.assertNotNull(newDal);
        Assert.assertNotNull(newDal.getChild());

        annotation = (CommentAnnotation) newDal.getChild();

        // comparison
        Assert.assertEquals("OME-Sample Inc", detector.getManufacturer().getValue());
        Assert.assertEquals("commentOnDetector", annotation.getTextValue().getValue());
    }

    /**
     * Test annotations on detector in instrument.
     * Retrieve via two methods
     * @throws Exception
     */
    @Test
    public void testAnnotationOnDetectorFull() throws Exception {
        String uuid = UUID.randomUUID().toString();

        Instrument instrument = (Instrument) iUpdate
                .saveAndReturnObject(mmFactory.createInstrument(uuid));
        Assert.assertNotNull(instrument);

        // creation
        Detector detector = mmFactory.createDetector();
        detector.setInstrument((Instrument) instrument.proxy());
        detector = (Detector) iUpdate.saveAndReturnObject(detector);
        Assert.assertNotNull(detector);

        // updating
        detector.setManufacturer(omero.rtypes.rstring("OME-Sample Inc"));
        detector = (Detector) iUpdate.saveAndReturnObject(detector);
        Assert.assertNotNull(detector);

        // Create annotation
        CommentAnnotation annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("commentOnDetector"));
        DetectorAnnotationLink dal = new DetectorAnnotationLinkI();
        dal.setParent((Detector)detector.proxy());
        dal.setChild(annotation);
        dal = (DetectorAnnotationLink) iUpdate.saveAndReturnObject(dal);
        Assert.assertNotNull(dal);

        // retrieval
        String sql = "select d from Detector as d left outer join fetch d.annotationLinks as link left outer join fetch link.child as child where d.id = " + detector.getId().getValue();
        detector = (Detector) iQuery.findByQuery(sql, null);
        Assert.assertNotNull(detector);
        Assert.assertTrue(detector.isAnnotated());
        List<DetectorAnnotationLink> listOfLinks = detector.copyAnnotationLinks();
        Assert.assertNotNull(listOfLinks);
        Assert.assertEquals(1, listOfLinks.size());
        Assert.assertNotNull(listOfLinks.get(0));
        DetectorAnnotationLink newDal = listOfLinks.get(0);
        Assert.assertNotNull(newDal);
        Assert.assertNotNull(newDal.getChild());

        annotation = (CommentAnnotation) newDal.getChild();
        Assert.assertEquals("commentOnDetector", annotation.getTextValue().getValue());

        Assert.assertEquals(1, detector.sizeOfAnnotationLinks());
        Assert.assertNotNull(detector.linkedAnnotationList());
        Assert.assertNotNull(detector.linkedAnnotationList().get(0));
        Assert.assertNotNull(detector.linkedAnnotationList().get(0).getId());

        sql = "select a from Annotation as a where a.id = " + detector.linkedAnnotationList().get(0).getId().getValue();
        annotation = (CommentAnnotation) iQuery.findByQuery(sql, null);
        Assert.assertNotNull(annotation);
        Assert.assertNotNull(annotation.getTextValue());

        // comparison
        Assert.assertEquals("OME-Sample Inc", detector.getManufacturer().getValue());
        Assert.assertEquals("commentOnDetector", annotation.getTextValue().getValue());
    }

    /**
     * Test annotations on detector in instrument.
     * Retrieve via iMetadata.
     * @throws Exception
     */
    @Test
    public void testAnnotationOnDetectorViaMetadata() throws Exception {
        String uuid = UUID.randomUUID().toString();

        Instrument instrument = (Instrument) iUpdate
                .saveAndReturnObject(mmFactory.createInstrument(uuid));
        Assert.assertNotNull(instrument);

        // creation
        Detector detector = mmFactory.createDetector();
        detector.setInstrument((Instrument) instrument.proxy());
        detector = (Detector) iUpdate.saveAndReturnObject(detector);
        Assert.assertNotNull(detector);
        
        // updating
        detector.setManufacturer(omero.rtypes.rstring("OME-Sample Inc"));
        detector = (Detector) iUpdate.saveAndReturnObject(detector);
        Assert.assertNotNull(detector);
        
        // Create annotation
        CommentAnnotation annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("commentOnDetectorViaiMetadata"));
        DetectorAnnotationLink dal = new DetectorAnnotationLinkI();
        dal.setParent((Detector)detector.proxy());
        dal.setChild(annotation);
        dal = (DetectorAnnotationLink) iUpdate.saveAndReturnObject(dal);
        Assert.assertNotNull(dal);
        
        // retrieval
        String sql = "select d from Detector as d left outer join fetch d.annotationLinks where d.id = " + detector.getId().getValue();
        detector = (Detector) iQuery.findByQuery(sql, null);
        Assert.assertTrue(detector.isAnnotated());
        Assert.assertEquals(1, detector.sizeOfAnnotationLinks());
        
        // load the annotations via iMetadata
        List<Long> ids = new ArrayList<Long>();
        Parameters param = new Parameters();
        List<Long> nodes = new ArrayList<Long>();
        nodes.add(detector.getId().getValue());
        IMetadataPrx iMetadata = factory.getMetadataService();
        String COMMENT_ANNOTATION = "ome.model.annotations.CommentAnnotation";
        Map<Long, List<IObject>> result = iMetadata.loadAnnotations(
                Detector.class.getName(), nodes, Arrays.asList(COMMENT_ANNOTATION),
                ids, param);
        Assert.assertNotNull(result);
        List<IObject> l = result.get(detector.getId().getValue());
        Assert.assertNotNull(l);
        Assert.assertEquals(1,l.size());


        // comparison
        Assert.assertEquals("OME-Sample Inc", detector.getManufacturer().getValue());

        Iterator<IObject> i = l.iterator();
        IObject o;
        while (i.hasNext()) {
            o = i.next();
            if (o instanceof CommentAnnotation) {
                CommentAnnotation theComAnn = (CommentAnnotation) o;
                Assert.assertNotNull(theComAnn);
                Assert.assertEquals("commentOnDetectorViaiMetadata", theComAnn.getTextValue().getValue());
            }
        }
    }
}


/*       Filter f = mmFactory.createFilter(500, 560);
       f.setInstrument((Instrument) instrument.proxy());
       f = (Filter) iUpdate.saveAndReturnObject(f);
       Assert.assertNotNull(f);

       Dichroic di = mmFactory.createDichroic();
       di.setInstrument((Instrument) instrument.proxy());
       di = (Dichroic) iUpdate.saveAndReturnObject(di);
       Assert.assertNotNull(di);

       Objective o = mmFactory.createObjective();
       o.setInstrument((Instrument) instrument.proxy());
       o = (Objective) iUpdate.saveAndReturnObject(o);
       Assert.assertNotNull(o);

       Laser l = mmFactory.createLaser();
       l.setInstrument((Instrument) instrument.proxy());
       l = (Laser) iUpdate.saveAndReturnObject(l);
       Assert.assertNotNull(l);
*/

