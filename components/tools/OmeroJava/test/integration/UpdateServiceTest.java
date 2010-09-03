/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

//Java imports
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import junit.framework.TestCase;
import ome.testing.ObjectFactory;
import omero.OptimisticLockException;
import omero.RInt;
import omero.RLong;
import omero.RString;
import omero.RType;
import omero.api.IAdminPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.RenderingEnginePrx;
import omero.api.ServiceFactoryPrx;
import omero.grid.Param;
import omero.model.Annotation;
import omero.model.AnnotationAnnotationLinkI;
import omero.model.Arc;
import omero.model.BooleanAnnotation;
import omero.model.BooleanAnnotationI;
import omero.model.Channel;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetAnnotationLink;
import omero.model.DatasetAnnotationLinkI;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Details;
import omero.model.Detector;
import omero.model.Dichroic;
import omero.model.DimensionOrder;
import omero.model.EllipseI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.Filament;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Filter;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
import omero.model.Instrument;
import omero.model.Laser;
import omero.model.Line;
import omero.model.LineI;
import omero.model.LongAnnotation;
import omero.model.LongAnnotationI;
import omero.model.Mask;
import omero.model.MaskI;
import omero.model.Objective;
import omero.model.OriginalFile;
import omero.model.Permissions;
import omero.model.PermissionsI;
import omero.model.Pixels;
import omero.model.PlaneInfo;
import omero.model.Plate;
import omero.model.PlateAnnotationLink;
import omero.model.PlateAnnotationLinkI;
import omero.model.PlateI;
import omero.model.Point;
import omero.model.PointI;
import omero.model.Polygon;
import omero.model.PolygonI;
import omero.model.Polyline;
import omero.model.PolylineI;
import omero.model.Project;
import omero.model.ProjectAnnotationLink;
import omero.model.ProjectAnnotationLinkI;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;
import omero.model.Rect;
import omero.model.RectI;
import omero.model.Roi;
import omero.model.RoiI;
import omero.model.Screen;
import omero.model.ScreenAnnotationLink;
import omero.model.ScreenAnnotationLinkI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.TermAnnotation;
import omero.model.TermAnnotationI;
import omero.model.Well;
import omero.model.WellI;
import omero.model.WellSample;
import omero.model.WellSampleI;
import omero.sys.EventContext;
import omero.sys.Parameters;
import omero.sys.ParametersI;
import omero.util.IceMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import Ice.Current;

import pojos.BooleanAnnotationData;
import pojos.DatasetData;
import pojos.EllipseData;
import pojos.ImageData;
import pojos.LineData;
import pojos.LongAnnotationData;
import pojos.MaskData;
import pojos.PlateData;
import pojos.PointData;
import pojos.PolygonData;
import pojos.PolylineData;
import pojos.ProjectData;
import pojos.ROIData;
import pojos.RatingAnnotationData;
import pojos.RectangleData;
import pojos.ScreenData;
import pojos.ShapeData;
import pojos.TagAnnotationData;
import pojos.TermAnnotationData;
import pojos.TextualAnnotationData;

/** 
 * Collections of tests for the <code>IUpdate</code> service.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
@Test(groups = { "client", "integration", "blitz" })
public class UpdateServiceTest 
	extends AbstractTest
{
	
    /**
     * Test to create an image and make sure the version is correct.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "versions"})
    public void testVersionHandling() 
    	throws Exception
    {
        Image img = mmFactory.simpleImage(0);
        img.setName(rstring("version handling"));
        Image sent = (Image) iUpdate.saveAndReturnObject(img);
        long version = sent.getDetails().getUpdateEvent().getId().getValue();
        
        sent.setDescription(rstring("version handling update"));
        // Update event should be created
        Image sent2 = (Image) iUpdate.saveAndReturnObject(sent);
        long version2 = sent2.getDetails().getUpdateEvent().getId().getValue();
        assertTrue(version != version2);
    }
    
    /**
     * Test to link datasets and images using the 
     * <code>saveAndReturnObject</code> method.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testNoDuplicateDatasetImageLinks() 
    	throws Exception
    {
    	/*TODO: rewrite test
    	Image img = new ImageI();
        img.setName(rstring("duplinks"));
        img.setAcquisitionDate( rtime(0) );

        Dataset ds = new DatasetI();
        ds.setName(rstring("duplinks"));

        img.linkDataset(ds);

        img = (Image) iUpdate.saveAndReturnObject(img);
        ds = img.linkedDatasetList().get(0);

        List imgLinks = iQuery.findAllByQuery("from DatasetImageLink",
                new ParametersI().addLong("child.id", img.getId()));

        List dsLinks = iQuery.findAllByQuery("from DatasetImageLink",
                new ParametersI().addLong("parent.id", ds.getId()));

        assertTrue(imgLinks.size() == 1);
        assertTrue(dsLinks.size() == 1);

        assertTrue(((DatasetImageLink) imgLinks.get(0)).getId().equals(
                ((DatasetImageLink) dsLinks.get(0)).getId()));
                */
    }
    
    /**
     * Test to link datasets and images using the 
     * <code>saveAndReturnArray</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testNoDuplicateDatasetImageLink() 
    	throws Exception 
    {
    	/*
    	Image img = new ImageI();
        img.setName(rstring("duplinks2"));
        img.setAcquisitionDate(rtime(0));

        Dataset ds = new DatasetI();
        ds.setName(rstring("duplinks2"));

        img.linkDataset(ds);

        List<IObject> l = new ArrayList<IObject>();
        l.add(img);
        List<IObject> retVal = iUpdate.saveAndReturnArray(l);
        assertTrue(retVal.size() == 1);
        
        assertTrue(retVal.get(0) instanceof Image);
        img = (Image) retVal.get(0);
        ds = img.linkedDatasetList().get(0);
        List dsLinks = iQuery.findAllByQuery("from DatasetImageLink",
                new ParametersI().addLong("parent.id", ds.getId()));

       
        List imgLinks = iQuery.findAllByQuery("from DatasetImageLink",
                new ParametersI().addLong("child.id", img.getId()));
        assertTrue(imgLinks.size() > 0);
        assertTrue(dsLinks.size() > 0);
 */
    }
    
    /**
     * Test to link datasets and images using the 
     * <code>saveAndReturnObject</code> method.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testNoDuplicateProjectDatasetLink() 
    	throws Exception 
    {
    	/*TODO: rewrite test
    	String name = "TEST:" + System.currentTimeMillis();

        // Save Project.
        Project p = new ProjectI();
        p.setName(rstring(name));
        p = (Project) iUpdate.saveAndReturnObject(p);

        // Update it.
        ProjectData pd = new ProjectData(p);
        pd.setDescription("....testnodups....");
        Project send = (Project) pd.asIObject();
        assertEquals(p.getId().getValue(), pd.getId());
        assertEquals(send.getId().getValue(), pd.getId());

        Project result = (Project) iUpdate.saveAndReturnObject(send);
        ProjectData test = new ProjectData(result);
        assertEquals(test.getId(), p.getId().getValue());
        */
    }
    
    /**
     * Test to make sure that an update event is created for an object
     * after updating an annotation linked to the image.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "versions", "ticket:118" })
    public void tesVersionNotIncreasingAfterUpdate()
            throws Exception 
    {
        CommentAnnotation ann = new CommentAnnotationI();
        Image img = mmFactory.simpleImage(0);
        img.setName(rstring("version_test"));
        img = (Image) iUpdate.saveAndReturnObject(img);
        
        ann.setTextValue(rstring("version_test"));
        img.linkAnnotation(ann);

        img = (Image) iUpdate.saveAndReturnObject(img);
        ann = (CommentAnnotation) img.linkedAnnotationList().get(0);
        assertNotNull(img.getId());
        assertNotNull(ann.getId());
        long oldId = img.getDetails().getUpdateEvent().getId().getValue();
        ann.setTextValue(rstring("updated version_test"));
        ann = (CommentAnnotation) iUpdate.saveAndReturnObject(ann);
        img = (Image) iQuery.get(Image.class.getName(), img.getId().getValue()); 

        long newId = img.getDetails().getUpdateEvent().getId().getValue();
        assertTrue(newId == oldId);
    }
    
    /**
     * Test to make sure that an update event is not created when
     * when invoking the <code>SaveAndReturnObject</code> on an unmodified 
     * Object.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "versions", "ticket:118" })
    public void testVersionNotIncreasingOnUnmodifiedObject() 
    	throws Exception 
    {
        Image img = (Image) iUpdate.saveAndReturnObject(
        		mmFactory.simpleImage(0));
        assertNotNull(img.getDetails().getUpdateEvent());
        long id = img.getDetails().getUpdateEvent().getId().getValue();
        Image test = (Image) iUpdate.saveAndReturnObject(img);
        assertNotNull(test.getDetails().getUpdateEvent());
        assertTrue(id == test.getDetails().getUpdateEvent().getId().getValue());
    }

    /**
     * Tests the creation of a project without datasets.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:1106")
    public void testEmptyProject() 
    	throws Exception
    {
        Project p = (Project) iUpdate.saveAndReturnObject(
        		mmFactory.simpleProjectData().asIObject());
        assertNotNull(p);
        ProjectData pd = new ProjectData(p);
    	assertTrue(p.getId().getValue() > 0);
    	assertTrue(p.getId().getValue() == pd.getId());
    	assertTrue(p.getName().getValue() == pd.getName());
    	assertTrue(p.getDescription().getValue() == pd.getDescription());
    }
    
    /**
     * Tests the creation of a dataset.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:1106")
    public void testEmptyDataset() 
    	throws Exception
    {
        Dataset p = (Dataset) iUpdate.saveAndReturnObject(
        		mmFactory.simpleDatasetData().asIObject());
        assertNotNull(p);
        DatasetData d = new DatasetData(p);
    	assertTrue(p.getId().getValue() > 0);
    	assertTrue(p.getId().getValue() == d.getId());
    	assertTrue(p.getName().getValue() == d.getName());
    	assertTrue(p.getDescription().getValue() == d.getDescription());
    }
    
    /**
     * Tests the creation of a dataset.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:1106")
    public void testEmptyImage() 
    	throws Exception
    {
        Image p = (Image) iUpdate.saveAndReturnObject(mmFactory.simpleImage(0));
        ImageData img = new ImageData(p);
    	assertNotNull(p);
    	assertTrue(p.getId().getValue() > 0);
    	assertTrue(p.getId().getValue() == img.getId());
    	assertTrue(p.getName().getValue() == img.getName());
    	assertTrue(p.getDescription().getValue() == img.getDescription());
    }
    
    /**
     * Tests the creation of an image with a set of pixels.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCreateImageWithPixels()
    	throws Exception 
    {
    	Image img = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.simpleImage(0));
    	assertNotNull(img);
    	Pixels pixels = mmFactory.createPixels();
    	img.addPixels(pixels);
    	img = (Image) iUpdate.saveAndReturnObject(img);
    	
    	ParametersI param = new ParametersI();
    	param.addId(img.getId().getValue());

    	StringBuilder sb = new StringBuilder();
    	sb.append("select i from Image i ");
    	sb.append("left outer join fetch i.pixels as pix ");
        sb.append("left outer join fetch pix.pixelsType as pt ");
    	sb.append("where i.id = :id");
    	img = (Image) iQuery.findByQuery(sb.toString(), param);
    	assertNotNull(img);
    	//Make sure we have a pixels set.
    	pixels = img.getPixels(0);
    	assertNotNull(pixels);
    }
    
    /**
     * Tests the creation of a screen.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:1106")
    public void testEmptyScreen() 
    	throws Exception
    {
        Screen p = (Screen) 
        	factory.getUpdateService().saveAndReturnObject(
        			mmFactory.simpleScreenData().asIObject());
        ScreenData data = new ScreenData(p);
    	assertNotNull(p);
    	assertTrue(p.getId().getValue() > 0);
    	assertTrue(p.getId().getValue() == data.getId());
    	assertTrue(p.getName().getValue() == data.getName());
    	assertTrue(p.getDescription().getValue() == data.getDescription());
    }
    
    /**
     * Tests the creation of a screen.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testEmptyPlate() 
    	throws Exception
    {
        Plate p = (Plate) 
        	factory.getUpdateService().saveAndReturnObject(
        			mmFactory.simplePlateData().asIObject());
        PlateData data = new PlateData(p);
    	assertNotNull(p);
    	assertTrue(p.getId().getValue() > 0);
    	assertTrue(p.getId().getValue() == data.getId());
    	assertTrue(p.getName().getValue() == data.getName());
    	assertTrue(p.getDescription().getValue() == data.getDescription());
    }
    
    /**
     * Tests the creation of a plate with wells, wells sample and 
     * plate acquisition.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testPopulatedPlate()
		throws Exception
    {
    	Plate p = mmFactory.createPlate(1, 1, 1, true, false);
    	p = (Plate) iUpdate.saveAndReturnObject(p);
    	assertNotNull(p);
    	p = mmFactory.createPlate(1, 1, 1, false, false);
    	p = (Plate) iUpdate.saveAndReturnObject(p);
    	assertNotNull(p);
    	p = mmFactory.createPlate(1, 1, 1, true, true);
    	p = (Plate) iUpdate.saveAndReturnObject(p);
    	assertNotNull(p);
    }
    
    /**
     * Test to create a project and link datasets to it.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCreateProjectAndLinkDatasets() 
    	throws Exception 
    {
        String name = " 2&1 " + System.currentTimeMillis();
        Project p = new ProjectI();
        p.setName(rstring(name));

        p = (Project) iUpdate.saveAndReturnObject(p);

        Dataset d1 = new DatasetI();
        d1.setName(rstring(name));
        d1 = (Dataset) iUpdate.saveAndReturnObject(d1);

        Dataset d2 = new DatasetI();
        d2.setName(rstring(name));
        d2 = (Dataset) iUpdate.saveAndReturnObject(d2);

        List<IObject> links = new ArrayList<IObject>();
        ProjectDatasetLink link = new ProjectDatasetLinkI();
        link.setParent(p);
        link.setChild(d1);
        links.add(link);
        link = new ProjectDatasetLinkI();
        link.setParent(p);
        link.setChild(d2);
        links.add(link);
        //links dataset and project.
        iUpdate.saveAndReturnArray(links);
        
        //load the project
        ParametersI param = new ParametersI();
        param.addId(p.getId());
       
        StringBuilder sb = new StringBuilder();
        sb.append("select p from Project p ");
        sb.append("left outer join fetch p.datasetLinks pdl ");
        sb.append("left outer join fetch pdl.child ds ");
        sb.append("where p.id = :id");
        p = (Project) iQuery.findByQuery(sb.toString(), param);
        
        //Check the conversion of Project to ProjectData
        ProjectData pData = new ProjectData(p);
        Set<DatasetData> datasets = pData.getDatasets();
        //We should have 2 datasets
        assertTrue(datasets.size() == 2);
        int count = 0;
        Iterator<DatasetData> i = datasets.iterator();
        DatasetData dataset;
        while (i.hasNext()) {
        	dataset = i.next();
			if (dataset.getId() == d1.getId().getValue() ||
					dataset.getId() == d2.getId().getValue()) count++;
		}
        assertTrue(count == 2);
    }
    
    /**
     * Test to create a dataset and link images to it.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCreateDatasetAndLinkImages() 
    	throws Exception 
    {
        String name = " 2&1 " + System.currentTimeMillis();
        Dataset p = new DatasetI();
        p.setName(rstring(name));

        p = (Dataset) iUpdate.saveAndReturnObject(p);

        Image d1 = new ImageI();
        d1.setName(rstring(name));
        d1.setAcquisitionDate(rtime(0));
        d1 = (Image) iUpdate.saveAndReturnObject(d1);

        Image d2 = new ImageI();
        d2.setAcquisitionDate(rtime(0));
        d2.setName(rstring(name));
        d2 = (Image) iUpdate.saveAndReturnObject(d2);

        List<IObject> links = new ArrayList<IObject>();
        DatasetImageLink link = new DatasetImageLinkI();
        link.setParent(p);
        link.setChild(d1);
        links.add(link);
        link = new DatasetImageLinkI();
        link.setParent(p);
        link.setChild(d2);
        links.add(link);
        //links dataset and project.
        iUpdate.saveAndReturnArray(links);
        
        //load the project
        ParametersI param = new ParametersI();
        param.addId(p.getId());
       
        StringBuilder sb = new StringBuilder();
        sb.append("select p from Dataset p ");
        sb.append("left outer join fetch p.imageLinks pdl ");
        sb.append("left outer join fetch pdl.child ds ");
        sb.append("where p.id = :id");
        p = (Dataset) iQuery.findByQuery(sb.toString(), param);
        
        //Check the conversion of Project to ProjectData
        DatasetData pData = new DatasetData(p);
        Set<ImageData> images = pData.getImages();
        //We should have 2 datasets
        assertTrue(images.size() == 2);
        int count = 0;
        Iterator<ImageData> i = images.iterator();
        ImageData image;
        while (i.hasNext()) {
        	image = i.next();
			if (image.getId() == d1.getId().getValue() ||
					image.getId() == d2.getId().getValue()) count++;
		}
        assertTrue(count == 2);
    }
    
    /**
     * Test to unlink projects and datasets from just one side.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "ticket:541" })
    public void testUnlinkProjectAndDatasetFromJustOneSide() 
    	throws Exception 
    {
    	/*
    	 *broken
        Image img = saveImage(true);
        DatasetImageLink link = img.copyDatasetLinks().get(0);
        img.removeDatasetImageLinkFromBoth(link, false);

        iContainer.updateDataObject(img, null);

        DatasetImageLink test = (DatasetImageLink) 
        	iQuery.find(DatasetImageLink.class.getName(), 
        			link.getId().getValue());

        assertNull(test);
        */
    }

    /**
     * Test to unlink datasets and images.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "ticket:541" })
    public void testUnlinkDatasetAndImage() 
    	throws Exception
    {

    	/*
        // Method 1:
        Image img = saveImage(true);
        List updated = unlinkImage(img);
        iUpdate.saveCollection(updated);

        // Make sure it's not linked.
        List list = iQuery.findAllByQuery(DatasetImageLink.class.getName(),
                new ParametersI().addLong("child.id", img.getId()));
        assertTrue(list.size() == 0);

        // Method 2:
        img = saveImage(true);
        updated = unlinkImage(img);
        iContainer.updateDataObjects(updated, null);

        List list2 = iQuery.findAllByQuery(DatasetImageLink.class.getName(),
                new ParametersI().addLong("child.id", img.getId()));
        assertTrue(list.size() == 0);

        // Method 3:
        img = saveImage(true);
        Dataset target = img.linkedDatasetList().get(0);
        // For querying
        DatasetImageLink dslink = img.findDatasetImageLink(target).iterator()
                .next();

        img.unlinkDataset(target);
        img = (Image) iContainer.updateDataObject(img, null);

        IObject test = iQuery.find(DatasetImageLink.class.getName(), 
        		dslink.getId().getValue());
        assertNull(test);

        // Method 4;
        Dataset d = new DatasetI();
        d.setName(rstring("unlinking"));
        Project p = new ProjectI();
        p.setName(rstring("unlinking"));
        p = (Project) iContainer.createDataObject(p, null);
        d = (Dataset) iContainer.createDataObject(d, null);

        ProjectDatasetLink link = new ProjectDatasetLinkI();
        link.setParent(p);
        link.setChild(d);
        */
    }
    
    /**
     * Test to unlink datasets and images from just one side.
     * @throws Exception
     */
    @Test(groups = { "ticket:541" })
    public void testUnlinkDatasetAndImageFromJustOneSide() 
    	throws Exception 
    {
    	/*
        Image img = saveImage(true);
        DatasetImageLink link = img.copyDatasetLinks().get(0);
        img.removeDatasetImageLinkFromBoth(link, false);

        iContainer.updateDataObject(img, null);

        DatasetImageLink test = (DatasetImageLink) 
        	iQuery.find(DatasetImageLink.class.getName(), 
        			link.getId().getValue());

        assertNull(test);
        */
    }
    
    /**
     * Test to handle duplicate links
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDuplicateProjectDatasetLink() 
    	throws Exception 
    {

    	/*TODO: rewrite test
        String string = "duplinksagain" + System.currentTimeMillis();

        Dataset d = new DatasetI();
        d.setName(rstring(string));

        Project p = new ProjectI();
        p.setName(rstring(string));

        d.linkProject(p);
        d = (Dataset) iContainer.createDataObject(d, null);
        List<Project> orig = d.linkedProjectList();
        Set orig_ids = new HashSet();
        for (Project pr : orig) {
            orig_ids.add(pr.getId().getValue());
        }

        DatasetData dd = new DatasetData(d);
        Dataset toSend = dd.asDataset();

        Dataset updated = (Dataset) iContainer.updateDataObject(toSend, null);

        List<Project> updt = updated.linkedProjectList();
        Set updt_ids = new HashSet();
        for (Project pr : updt) {
            updt_ids.add(pr.getId().getValue());
        }

        if (log.isDebugEnabled()) {
            log.debug(orig_ids);
            log.debug(updt_ids);
        }

        assertTrue(updt_ids.containsAll(orig_ids));
        assertTrue(orig_ids.containsAll(updt_ids));
        */
    }

    //Annotation section
    
    /**
     * Links the passed annotation and test if correctly linked.
     * @throws Exception Thrown if an error occurred.
     */
    private void linkAnnotationAndObjects(Annotation data)
    	throws Exception 
    {
    	//Image
        Image i = (Image) iUpdate.saveAndReturnObject(
        		mmFactory.simpleImage(0));
        ImageAnnotationLink l = new ImageAnnotationLinkI();
        l.setParent((Image) i.proxy());
        l.setChild((Annotation) data.proxy());
        IObject o1 = iUpdate.saveAndReturnObject(l);
        assertNotNull(o1);
        l  = (ImageAnnotationLink) o1;
        assertTrue(l.getChild().getId().getValue() == data.getId().getValue());
        assertTrue(l.getParent().getId().getValue() == i.getId().getValue());
        
        //Project
        Project p = (Project) iUpdate.saveAndReturnObject(
        		mmFactory.simpleProjectData().asIObject());
        ProjectAnnotationLink pl = new ProjectAnnotationLinkI();
        pl.setParent((Project) p.proxy());
        pl.setChild((Annotation) data.proxy());
        o1 = iUpdate.saveAndReturnObject(pl);
        assertNotNull(o1);
        pl  = (ProjectAnnotationLink) o1;
        assertTrue(pl.getChild().getId().getValue() == data.getId().getValue());
        assertTrue(pl.getParent().getId().getValue() == p.getId().getValue());
        
        //Dataset
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(
        		mmFactory.simpleDatasetData().asIObject());
        DatasetAnnotationLink dl = new DatasetAnnotationLinkI();
        dl.setParent((Dataset) d.proxy());
        dl.setChild((Annotation) data.proxy());
        o1 = iUpdate.saveAndReturnObject(dl);
        assertNotNull(o1);
        dl  = (DatasetAnnotationLink) o1;
        assertTrue(dl.getChild().getId().getValue() == data.getId().getValue());
        assertTrue(dl.getParent().getId().getValue() == d.getId().getValue());
        
        //Screen
        Screen s = (Screen) iUpdate.saveAndReturnObject(
        		mmFactory.simpleScreenData().asIObject());
        ScreenAnnotationLink sl = new ScreenAnnotationLinkI();
        sl.setParent((Screen) s.proxy());
        sl.setChild((Annotation) data.proxy());
        o1 = iUpdate.saveAndReturnObject(sl);
        assertNotNull(o1);
        sl  = (ScreenAnnotationLink) o1;
        assertTrue(sl.getChild().getId().getValue() == data.getId().getValue());
        assertTrue(sl.getParent().getId().getValue() == s.getId().getValue());
        
        //Plate
        Plate pp = (Plate) iUpdate.saveAndReturnObject(
        		mmFactory.simplePlateData().asIObject());
        PlateAnnotationLink ppl = new PlateAnnotationLinkI();
        ppl.setParent((Plate) pp.proxy());
        ppl.setChild((Annotation) data.proxy());
        o1 = iUpdate.saveAndReturnObject(ppl);
        assertNotNull(o1);
        ppl  = (PlateAnnotationLink) o1;
        assertTrue(ppl.getChild().getId().getValue() == data.getId().getValue());
        assertTrue(ppl.getParent().getId().getValue() == pp.getId().getValue());
    }
    
    /**
     * Tests to create a comment annotation and link it to various objects.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCreateCommentAnnotation()
    	throws Exception 
    {
    	CommentAnnotation annotation = new CommentAnnotationI();
    	annotation.setTextValue(omero.rtypes.rstring("comment"));
    	annotation = (CommentAnnotation) 
    	iUpdate.saveAndReturnObject(annotation);
    	assertNotNull(annotation);
    	linkAnnotationAndObjects(annotation);
    	TextualAnnotationData data = new TextualAnnotationData(annotation);
    	assertNotNull(data);
    	assertTrue(data.getText().equals(annotation.getTextValue().getValue()));
    }
    
    /**
     * Tests to create a tag annotation and link it to various objects.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCreateTagAnnotation()
    	throws Exception 
    {
    	TagAnnotation annotation = new TagAnnotationI();
    	annotation.setTextValue(omero.rtypes.rstring("tag"));
    	annotation = (TagAnnotation) 
    		iUpdate.saveAndReturnObject(annotation);
    	assertNotNull(annotation);
    	linkAnnotationAndObjects(annotation);
    	TagAnnotationData data = new TagAnnotationData(annotation);
    	assertNotNull(data);
    	assertTrue(data.getTagValue().equals(
    			annotation.getTextValue().getValue()));
    }
    
    /**
     * Tests to create a boolean annotation and link it to various objects.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCreateBooleanAnnotation()
    	throws Exception 
    {
    	BooleanAnnotation annotation = new BooleanAnnotationI();
    	annotation.setBoolValue(omero.rtypes.rbool(true));
    	annotation = (BooleanAnnotation) 
    		iUpdate.saveAndReturnObject(annotation);
    	assertNotNull(annotation);
    	linkAnnotationAndObjects(annotation);
    	BooleanAnnotationData data = new BooleanAnnotationData(annotation);
    	assertNotNull(data);
    }
    
    /**
     * Tests to create a long annotation and link it to various objects.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCreateLongAnnotation()
    	throws Exception 
    {
    	LongAnnotation annotation = new LongAnnotationI();
    	annotation.setLongValue(omero.rtypes.rlong(1L));
    	annotation = (LongAnnotation) 
    		iUpdate.saveAndReturnObject(annotation);
    	assertNotNull(annotation);
    	linkAnnotationAndObjects(annotation);
    	LongAnnotationData data = new LongAnnotationData(annotation);
    	assertNotNull(data);
    	assertTrue(data.getDataValue() == annotation.getLongValue().getValue());
    }
    
    /**
     * Tests to create a file annotation and link it to various objects.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCreateFileAnnotation()
    	throws Exception 
    {
    	OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(
    			mmFactory.createOriginalFile());
		assertNotNull(of);
		FileAnnotation fa = new FileAnnotationI();
		fa.setFile(of);
		FileAnnotation data = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
		assertNotNull(data);
    	linkAnnotationAndObjects(data);
    }
    
    /**
     * Tests to create a term and link it to various objects.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCreateTermAnnotation()
    	throws Exception 
    {
		TermAnnotation term = new TermAnnotationI();
		term.setTermValue(omero.rtypes.rstring("term"));
		term = (TermAnnotation) iUpdate.saveAndReturnObject(term);
		assertNotNull(term);
    	linkAnnotationAndObjects(term);
    	TermAnnotationData data = new TermAnnotationData(term);
    	assertNotNull(data);
    	assertTrue(data.getTerm().equals(term.getTermValue().getValue()));
    }
    
    /**
     * Tests to unlink of an annotation. Creates only one type of annotation.
     * This method uses the <code>deleteObject</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testRemoveAnnotation()
    	throws Exception 
    {
    	LongAnnotationI annotation = new LongAnnotationI();
    	annotation.setLongValue(omero.rtypes.rlong(1L));
    	LongAnnotation data = (LongAnnotation) 
    		iUpdate.saveAndReturnObject(annotation);
    	assertNotNull(data);
    	//Image
        Image i = (Image) iUpdate.saveAndReturnObject(mmFactory.simpleImage(0));
        ImageAnnotationLink l = new ImageAnnotationLinkI();
        l.setParent((Image) i.proxy());
        l.setChild((Annotation) data.proxy());
        l = (ImageAnnotationLink) iUpdate.saveAndReturnObject(l);
        assertNotNull(l);
        long id = l.getId().getValue();
        //annotation and image are linked. Remove the link.
        iUpdate.deleteObject(l);
        //now check that the image is no longer linked to the annotation
        String sql = "select link from ImageAnnotationLink as link";
		sql += " where link.id = :id";
		ParametersI p = new ParametersI();
		p.addId(id);
		IObject object = iQuery.findByQuery(sql, p);
		assertNull(object);
    }
    
    /**
     * Tests to update an annotation.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testUpdateAnnotation() 
    	throws Exception 
    {
    	CommentAnnotationI annotation = new CommentAnnotationI();
    	annotation.setTextValue(omero.rtypes.rstring("comment"));
    	CommentAnnotation data = (CommentAnnotation) 
    		iUpdate.saveAndReturnObject(annotation);
    	assertNotNull(data);
    	//modified the text
    	String newText = "commentModified";
    	data.setTextValue(omero.rtypes.rstring(newText));
    	CommentAnnotation update = (CommentAnnotation) 
			iUpdate.saveAndReturnObject(data);
    	assertNotNull(update);
    	
    	assertTrue(data.getId().getValue() == update.getId().getValue());
    	
    	assertTrue(newText.equals(update.getTextValue().getValue()));
    }
    
    /**
     * Tests the creation of tag annotation, linked it to an image by a
     * user and link it to the same image by a different user.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testUpdateSameTagAnnotationUsedByTwoUsers() 
    	throws Exception
    {
        String groupName = newUserAndGroup("rwrw--").groupName;

        //create an image.
        Image image = (Image) iUpdate.saveAndReturnObject(
        		mmFactory.simpleImage(0));

        //create the tag.
        TagAnnotationI tag = new TagAnnotationI();
        tag.setTextValue(omero.rtypes.rstring("tag1"));

        Annotation data = (Annotation) iUpdate.saveAndReturnObject(tag);
        //link the image and the tag
        ImageAnnotationLink l = new ImageAnnotationLinkI();
        l.setParent((Image) image.proxy());
        l.setChild((Annotation) data.proxy());

        IObject o1 = iUpdate.saveAndReturnObject(l);
        assertNotNull(o1);
        CreatePojosFixture2 fixture = CreatePojosFixture2.withNewUser(root, 
        		groupName);

        l = new ImageAnnotationLinkI();
        l.setParent((Image) image.proxy());
        l.setChild((Annotation) data.proxy());
        // l.getDetails().setOwner(fixture.e);
        IObject o2 = fixture.iUpdate.saveAndReturnObject(l);
        assertNotNull(o2);

        long self = factory.getAdminService().getEventContext().userId;

        assertTrue(o1.getId().getValue() != o2.getId().getValue());
        assertTrue(o1.getDetails().getOwner().getId().getValue() == self);
        assertTrue(o2.getDetails().getOwner().getId().getValue() ==
            fixture.e.getId().getValue());
    }
    
    /**
     * Tests the creation of tag annotation, linked it to an image by a
     * user and link it to the same image by a different user.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testTagSetTagCreation() 
    	throws Exception
    {
    	//Create a tag set.
    	TagAnnotationI tagSet = new TagAnnotationI();
    	tagSet.setTextValue(omero.rtypes.rstring("tagSet"));
    	tagSet.setNs(omero.rtypes.rstring(TagAnnotationData.INSIGHT_TAGSET_NS));
    	TagAnnotation tagSetReturned = 
    		(TagAnnotation) iUpdate.saveAndReturnObject(tagSet);
    	//create a tag and link it to the tag set
    	assertNotNull(tagSetReturned);
    	TagAnnotationI tag = new TagAnnotationI();
    	tag.setTextValue(omero.rtypes.rstring("tag"));
    	TagAnnotation tagReturned = 
    		(TagAnnotation) iUpdate.saveAndReturnObject(tag);
    	assertNotNull(tagReturned);
    	AnnotationAnnotationLinkI link = new AnnotationAnnotationLinkI();
    	link.setChild(tagReturned);
    	link.setParent(tagSetReturned);
    	IObject l = iUpdate.saveAndReturnObject(link); //save the link.
    	assertNotNull(l);

    	ParametersI param = new ParametersI();
    	param.addId(l.getId());

    	StringBuilder sb = new StringBuilder();
    	sb.append("select l from AnnotationAnnotationLink l ");
    	sb.append("left outer join fetch l.child c ");
    	sb.append("left outer join fetch l.parent p ");
    	sb.append("where l.id = :id");
    	AnnotationAnnotationLinkI lReturned = (AnnotationAnnotationLinkI) 
    	iQuery.findByQuery(sb.toString(), param);
    	assertNotNull(lReturned.getChild());
    	assertNotNull(lReturned.getParent());
    	assertTrue(lReturned.getChild().getId().getValue() 
    			== tagReturned.getId().getValue());
    	assertTrue(lReturned.getParent().getId().getValue() 
    			== tagSetReturned.getId().getValue());
    }

    //
    // The following are duplicated in ome.server.itests.update.UpdateTest
    //
    
    /**
     * Test the creation and handling of channels.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:2547")
    public void testChannelMoveWithFullArrayGoesToEnd() 
    	throws Exception
    {
    	Image i = mmFactory.createImage(ModelMockFactory.SIZE_X, 
    			ModelMockFactory.SIZE_Y, ModelMockFactory.SIZE_Z, 
    			ModelMockFactory.SIZE_T, 
    			ModelMockFactory.DEFAULT_CHANNELS_NUMBER);
        i = (Image) iUpdate.saveAndReturnObject(i);
        Pixels p = i.getPrimaryPixels();

        Set<Long> ids = new HashSet<Long>();
        assertEquals(ModelMockFactory.DEFAULT_CHANNELS_NUMBER, 
        		p.sizeOfChannels());
        for (Channel ch : p.copyChannels()) {
            assertNotNull(ch);
            ids.add(ch.getId().getValue());
        }

        // Now add another channel
        Channel extra = mmFactory.createChannel(0); // Copies dimension orders, etc.
        p.addChannel(extra);

        i = (Image) iUpdate.saveAndReturnObject(i);
        p = i.getPrimaryPixels();

        assertEquals(ModelMockFactory.DEFAULT_CHANNELS_NUMBER+1, 
        		p.sizeOfChannels());
        assertFalse(ids.contains(p.getChannel(
        		ModelMockFactory.DEFAULT_CHANNELS_NUMBER).getId().getValue()));
    }

    /**
     * Test the creation and handling of channels.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:2547")
    public void testChannelMoveWithSpaceFillsSpace() 
    	throws Exception
    {
    	Image i = mmFactory.createImage(ModelMockFactory.SIZE_X, 
    			ModelMockFactory.SIZE_Y, ModelMockFactory.SIZE_Z, 
    			ModelMockFactory.SIZE_T, 
    			ModelMockFactory.DEFAULT_CHANNELS_NUMBER);
        i = (Image) iUpdate.saveAndReturnObject(i);
        
        Pixels p = i.getPrimaryPixels();
        p.setChannel(1, null);
        p = (Pixels) iUpdate.saveAndReturnObject(p);
        

        Set<Long> ids = new HashSet<Long>();
        Channel old = p.getChannel(0);
        assertEquals(ModelMockFactory.DEFAULT_CHANNELS_NUMBER, 
        		p.sizeOfChannels());
        assertNotNull(old);
        ids.add(p.getChannel(0).getId().getValue());

        // Middle should be empty
        assertNull(p.getChannel(1));

        assertNotNull(p.getChannel(2));
        ids.add(p.getChannel(2).getId().getValue());

        // Now add a channel to the front
        
        //extra = (Channel) iUpdate.saveAndReturnObject(extra);
        //p.setChannel(0, extra);
        p.setChannel(1, old);
        
        p = (Pixels) iUpdate.saveAndReturnObject(p);
        Channel extra =  mmFactory.createChannel(0);
        p.setChannel(0, extra);
        
        p = (Pixels) iUpdate.saveAndReturnObject(p);

        assertEquals(ModelMockFactory.DEFAULT_CHANNELS_NUMBER, 
        		p.sizeOfChannels());
        assertFalse(ids.contains(p.getChannel(0).getId().getValue()));
    }

    /**
     * Test the creation and handling of channels.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:2547")
    public void testChannelToSpaceChangesNothing() 
    	throws Exception
    {
    	Image i = mmFactory.createImage(ModelMockFactory.SIZE_X, 
    			ModelMockFactory.SIZE_Y, ModelMockFactory.SIZE_Z, 
    			ModelMockFactory.SIZE_T, 
    			ModelMockFactory.DEFAULT_CHANNELS_NUMBER);
        i = (Image) iUpdate.saveAndReturnObject(i);
        
        Pixels p = i.getPrimaryPixels();
        p.setChannel(1, null);
        p = (Pixels) iUpdate.saveAndReturnObject(p);

        Set<Long> ids = new HashSet<Long>();
        assertEquals(ModelMockFactory.DEFAULT_CHANNELS_NUMBER, 
        		p.sizeOfChannels());
        assertNotNull(p.getChannel(0));
        ids.add(p.getChannel(0).getId().getValue());

        // Middle should be empty
        assertNull(p.getChannel(1));

        assertNotNull(p.getChannel(2));
        ids.add(p.getChannel(2).getId().getValue());

        // Now add a channel to the space
        Channel extra =  mmFactory.createChannel(0);
        p.setChannel(1, extra);

        p = (Pixels) iUpdate.saveAndReturnObject(p);

        assertEquals(ModelMockFactory.DEFAULT_CHANNELS_NUMBER, 
        		p.sizeOfChannels());
        assertFalse(ids.contains(p.getChannel(1).getId().getValue()));
    }
    
    /**
     * Tests the creation of plane information objects.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "ticket:168", "ticket:767" })
    public void testPlaneInfoSetPixelsSavePlaneInfo() 
    	throws Exception
    {
    	Image image = mmFactory.createImage();
		image = (Image) iUpdate.saveAndReturnObject(image);
        Pixels pixels = image.getPrimaryPixels();
        pixels.clearPlaneInfo();
        PlaneInfo planeInfo = mmFactory.createPlaneInfo();
        planeInfo.setPixels(pixels);
        planeInfo = (PlaneInfo) iUpdate.saveAndReturnObject(planeInfo);
        ParametersI param = new ParametersI();
    	param.addId(planeInfo.getId());
        Pixels test = (Pixels) iQuery.findByQuery(
                "select pi.pixels from PlaneInfo pi where pi.id = :id",
                param);
        assertNotNull(test);
    }

    /**
     * Tests the creation of plane information objects. This time the plane info
     * object is directly added to the pixels set. The plane info should be
     * saved.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:168")
    public void testPixelsAddToPlaneInfoSavePixels() 
    	throws Exception
    {
    	Image image = mmFactory.createImage();
    	image = (Image) iUpdate.saveAndReturnObject(image);
        Pixels pixels = image.getPrimaryPixels();
    	pixels.clearPlaneInfo();
    	PlaneInfo planeInfo = mmFactory.createPlaneInfo();
    	pixels.addPlaneInfo(planeInfo);
    	pixels = (Pixels) iUpdate.saveAndReturnObject(pixels);
    	ParametersI param = new ParametersI();
    	param.addId(pixels.getId());
    	List<IObject> test = (List<IObject>) iQuery.findAllByQuery(
    			"select pi from PlaneInfo pi where pi.pixels.id = :id",
    			param);
    	assertNotNull(test);
    }
    
    /**
	 * Tests the creation of ROIs whose shapes are Ellipses and converts them 
	 * into the corresponding <code>POJO</code> objects.
	 * @throws Exception  Thrown if an error occurred.
	 */
    @Test
    public void testCreateROIWithEllipse() 
    	throws Exception
    {
    	ImageI image = (ImageI) iUpdate.saveAndReturnObject(
    			mmFactory.simpleImage(0));
        RoiI roi = new RoiI();
        roi.setImage(image);
        RoiI serverROI = (RoiI) iUpdate.saveAndReturnObject(roi);
        assertNotNull(serverROI);
        double v = 10;
        int z = 0;
        int t = 0;
        int c = 0;
        EllipseI rect = new EllipseI();
        rect.setCx(omero.rtypes.rdouble(v));
        rect.setCy(omero.rtypes.rdouble(v));
        rect.setRx(omero.rtypes.rdouble(v));
        rect.setRy(omero.rtypes.rdouble(v));
        rect.setTheZ(omero.rtypes.rint(z));
        rect.setTheT(omero.rtypes.rint(t));
        rect.setTheC(omero.rtypes.rint(c));
        serverROI.addShape(rect);
        
        serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);
        
        ROIData data = new ROIData(serverROI);
        assertTrue(data.getId() == serverROI.getId().getValue());
        assertTrue(data.getShapeCount() == 1);
        
        List<ShapeData> shapes = data.getShapes(z, t);
        assertNotNull(shapes);
        assertTrue(shapes.size() == 1);
        EllipseData shape;
        Iterator<ShapeData> i = shapes.iterator();
        while (i.hasNext()) {
        	shape = (EllipseData) i.next();
        	assertTrue(shape.getT() == t);
        	assertTrue(shape.getZ() == z);
        	assertTrue(shape.getC() == c);
        	assertTrue(shape.getX() == v);
        	assertTrue(shape.getY() == v);
        	assertTrue(shape.getRadiusX() == v);
        	assertTrue(shape.getRadiusY() == v);
		}
    }
    
    /**
	 * Tests the creation of ROIs whose shapes are Points and converts them 
	 * into the corresponding <code>POJO</code> objects.
	 * @throws Exception  Thrown if an error occurred.
	 */
    @Test
    public void testCreateROIWithPoint() 
    	throws Exception
    {
        Image image = (Image) iUpdate.saveAndReturnObject(
        		mmFactory.simpleImage(0));
        Roi roi = new RoiI();
        roi.setImage(image);
        Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
        assertNotNull(serverROI);
        double v = 10;
        int z = 0;
        int t = 0;
        int c = 0;
        Point rect = new PointI();
        rect.setCx(omero.rtypes.rdouble(v));
        rect.setCy(omero.rtypes.rdouble(v));
        rect.setTheZ(omero.rtypes.rint(z));
        rect.setTheT(omero.rtypes.rint(t));
        rect.setTheC(omero.rtypes.rint(c));
        serverROI.addShape(rect);
        
        serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);
        
        ROIData data = new ROIData(serverROI);
        assertTrue(data.getId() == serverROI.getId().getValue());
        assertTrue(data.getShapeCount() == 1);
        
        List<ShapeData> shapes = data.getShapes(z, t);
        assertNotNull(shapes);
        assertTrue(shapes.size() == 1);
        PointData shape;
        Iterator<ShapeData> i = shapes.iterator();
        while (i.hasNext()) {
        	shape = (PointData) i.next();
        	assertTrue(shape.getT() == t);
        	assertTrue(shape.getZ() == z);
        	assertTrue(shape.getC() == c);
        	assertTrue(shape.getX() == v);
        	assertTrue(shape.getY() == v);
		}
    }
    
    /**
	 * Tests the creation of ROIs whose shapes are Points and converts them 
	 * into the corresponding <code>POJO</code> objects.
	 * @throws Exception  Thrown if an error occurred.
	 */
    @Test
    public void testCreateROIWithRectangle() 
    	throws Exception
    {
        Image image = (Image) iUpdate.saveAndReturnObject(
        		mmFactory.simpleImage(0));
        Roi roi = new RoiI();
        roi.setImage(image);
        Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
        assertNotNull(serverROI);
        double v = 10;
        int z = 0;
        int t = 0;
        int c = 0;
        Rect rect = new RectI();
        rect.setX(omero.rtypes.rdouble(v));
        rect.setY(omero.rtypes.rdouble(v));
        rect.setWidth(omero.rtypes.rdouble(v));
        rect.setHeight(omero.rtypes.rdouble(v));
        rect.setTheZ(omero.rtypes.rint(z));
        rect.setTheT(omero.rtypes.rint(t));
        rect.setTheC(omero.rtypes.rint(c));
        serverROI.addShape(rect);
        
        serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);
        
        ROIData data = new ROIData(serverROI);
        assertTrue(data.getId() == serverROI.getId().getValue());
        assertTrue(data.getShapeCount() == 1);
        
        List<ShapeData> shapes = data.getShapes(z, t);
        assertNotNull(shapes);
        assertTrue(shapes.size() == 1);
        RectangleData shape;
        Iterator<ShapeData> i = shapes.iterator();
        while (i.hasNext()) {
        	shape = (RectangleData) i.next();
        	assertTrue(shape.getT() == t);
        	assertTrue(shape.getZ() == z);
        	assertTrue(shape.getC() == c);
        	assertTrue(shape.getX() == v);
        	assertTrue(shape.getY() == v);
        	assertTrue(shape.getWidth() == v);
        	assertTrue(shape.getHeight() == v);
		}
    }
    
    /**
     * Tests the creation of an ROI not linked to an image.
     * @throws Exception  Thrown if an error occurred.
     */
    @Test
    public void testCreateROIWithoutImage()
    	throws Exception
    {
    	/*
    	 Roi roi = new RoiI();
         roi.setDescription(omero.rtypes.rstring("roi w/o image"));
         Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
    	 */
    }
    
    /**
	 * Tests the creation of ROIs whose shapes are Polygons and converts them 
	 * into the corresponding <code>POJO</code> objects.
	 * @throws Exception  Thrown if an error occurred.
	 */
    @Test
    public void testCreateROIWithPolygon() 
    	throws Exception
    {
       	Image image = (Image) iUpdate.saveAndReturnObject(
       			mmFactory.simpleImage(0));
        Roi roi = new RoiI();
        roi.setImage(image);
        Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
        assertNotNull(serverROI);
        double v = 10;
        double w = 11;
        int z = 0;
        int t = 0;
        int c = 0;
        String points = "points[10, 10] points1[10, 10] points2[10, 10]";
        Polygon rect = new PolygonI();
        rect.setPoints(omero.rtypes.rstring(points));
        rect.setTheZ(omero.rtypes.rint(z));
        rect.setTheT(omero.rtypes.rint(t));
        rect.setTheC(omero.rtypes.rint(c));
        serverROI.addShape(rect);
        
        serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);
        
        ROIData data = new ROIData(serverROI);
        assertTrue(data.getId() == serverROI.getId().getValue());
        assertTrue(data.getShapeCount() == 1);
        
        List<ShapeData> shapes = data.getShapes(z, t);
        assertNotNull(shapes);
        assertTrue(shapes.size() == 1);
        PolygonData shape;
        Iterator<ShapeData> i = shapes.iterator();
        while (i.hasNext()) {
        	shape = (PolygonData) i.next();
        	assertTrue(shape.getT() == t);
        	assertTrue(shape.getZ() == z);
        	assertTrue(shape.getC() == c);
        	assertTrue(shape.getPoints().size() == 1);
        	assertTrue(shape.getPoints1().size() == 1);
        	assertTrue(shape.getPoints2().size() == 1);
		}
    }
    
    /**
	 * Tests the creation of ROIs whose shapes are Polylines and converts them 
	 * into the corresponding <code>POJO</code> objects.
	 * @throws Exception  Thrown if an error occurred.
	 */
    @Test
    public void testCreateROIWithPolyline() 
    	throws Exception
    {
    	Image image = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.simpleImage(0));
        Roi roi = new RoiI();
        roi.setImage(image);
        Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
        assertNotNull(serverROI);
        double v = 10;
        String points = "points[10, 10] points1[10, 10] points2[10, 10]";
        int z = 0;
        int t = 0;
        int c = 0;
        Polyline rect = new PolylineI();
        rect.setPoints(omero.rtypes.rstring(points));
        rect.setTheZ(omero.rtypes.rint(z));
        rect.setTheT(omero.rtypes.rint(t));
        rect.setTheC(omero.rtypes.rint(c));
        serverROI.addShape(rect);
        
        serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);
        
        ROIData data = new ROIData(serverROI);
        assertTrue(data.getId() == serverROI.getId().getValue());
        assertTrue(data.getShapeCount() == 1);
        
        List<ShapeData> shapes = data.getShapes(z, t);
        assertNotNull(shapes);
        assertTrue(shapes.size() == 1);
        PolylineData shape;
        Iterator<ShapeData> i = shapes.iterator();
        while (i.hasNext()) {
        	shape = (PolylineData) i.next();
        	assertTrue(shape.getT() == t);
        	assertTrue(shape.getZ() == z);
        	assertTrue(shape.getC() == c);
        	assertTrue(shape.getPoints().size() == 1);
        	assertTrue(shape.getPoints1().size() == 1);
        	assertTrue(shape.getPoints2().size() == 1);
		}
    }
    
    /**
	 * Tests the creation of ROIs whose shapes are Lines and converts them 
	 * into the corresponding <code>POJO</code> objects.
	 * @throws Exception  Thrown if an error occurred.
	 */
    @Test
    public void testCreateROIWithLine() 
    	throws Exception
    {
        Image image = (Image) iUpdate.saveAndReturnObject(
        		mmFactory.simpleImage(0));
        Roi roi = new RoiI();
        roi.setImage(image);
        Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
        assertNotNull(serverROI);
        double v = 10;
        double w = 11;
        int z = 0;
        int t = 0;
        int c = 0;
        Line rect = new LineI();
        rect.setX1(omero.rtypes.rdouble(v));
        rect.setY1(omero.rtypes.rdouble(v));
        rect.setX2(omero.rtypes.rdouble(w));
        rect.setY2(omero.rtypes.rdouble(w));
        rect.setTheZ(omero.rtypes.rint(z));
        rect.setTheT(omero.rtypes.rint(t));
        rect.setTheC(omero.rtypes.rint(c));
        serverROI.addShape(rect);
        
        serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);
        
        ROIData data = new ROIData(serverROI);
        assertTrue(data.getId() == serverROI.getId().getValue());
        assertTrue(data.getShapeCount() == 1);
        
        List<ShapeData> shapes = data.getShapes(z, t);
        assertNotNull(shapes);
        assertTrue(shapes.size() == 1);
        LineData shape;
        Iterator<ShapeData> i = shapes.iterator();
        while (i.hasNext()) {
        	shape = (LineData) i.next();
        	assertTrue(shape.getT() == t);
        	assertTrue(shape.getZ() == z);
        	assertTrue(shape.getC() == c);
        	assertTrue(shape.getX1() == v);
        	assertTrue(shape.getY1() == v);
        	assertTrue(shape.getX2() == w);
        	assertTrue(shape.getY2() == w);
		}
    }
    
    /**
	 * Tests the creation of ROIs whose shapes are Masks and converts them 
	 * into the corresponding <code>POJO</code> objects.
	 * @throws Exception  Thrown if an error occurred.
	 */
    @Test
    public void testCreateROIWithMask() 
    	throws Exception
    {
        Image image = (Image) iUpdate.saveAndReturnObject(
        		mmFactory.simpleImage(0));
        Roi roi = new RoiI();
        roi.setImage(image);
        Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
        assertNotNull(serverROI);
        double v = 10;
        int z = 0;
        int t = 0;
        int c = 0;
        Mask rect = new MaskI();
        rect.setX(omero.rtypes.rdouble(v));
        rect.setY(omero.rtypes.rdouble(v));
        rect.setWidth(omero.rtypes.rdouble(v));
        rect.setHeight(omero.rtypes.rdouble(v));
        rect.setTheZ(omero.rtypes.rint(z));
        rect.setTheT(omero.rtypes.rint(t));
        rect.setTheC(omero.rtypes.rint(c));
        serverROI.addShape(rect);
        
        serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);
        
        ROIData data = new ROIData(serverROI);
        assertTrue(data.getId() == serverROI.getId().getValue());
        assertTrue(data.getShapeCount() == 1);
        
        List<ShapeData> shapes = data.getShapes(z, t);
        assertNotNull(shapes);
        assertTrue(shapes.size() == 1);
        MaskData shape;
        Iterator<ShapeData> i = shapes.iterator();
        while (i.hasNext()) {
        	shape = (MaskData) i.next();
        	assertTrue(shape.getT() == t);
        	assertTrue(shape.getZ() == z);
        	assertTrue(shape.getC() == c);
        	assertTrue(shape.getX() == v);
        	assertTrue(shape.getY() == v);
        	assertTrue(shape.getWidth() == v);
        	assertTrue(shape.getHeight() == v);
		}
    }
    
    /**
	 * Tests the creation of an instrument using the <code>Add</code> methods
	 * associated to an instrument.
	 * @throws Exception  Thrown if an error occurred.
	 */
    @Test
    public void testCreateInstrumentUsingAdd() 
    	throws Exception
    {
    	Instrument instrument;
    	ParametersI param;
    	String sql;
    	IObject test;
    	String value;
    	for (int i = 0; i < ModelMockFactory.LIGHT_SOURCES.length; i++) {
    		value = ModelMockFactory.LIGHT_SOURCES[i];
    		instrument = mmFactory.createInstrument(value);
        	instrument = (Instrument) iUpdate.saveAndReturnObject(instrument);
        	assertNotNull(instrument);
    		param = new ParametersI();
        	param.addLong("iid", instrument.getId().getValue());
        	sql = "select d from Detector as d where d.instrument.id = :iid";
            test = iQuery.findByQuery(sql, param);
            assertNotNull(test);
            sql = "select d from Dichroic as d where d.instrument.id = :iid";
            test = iQuery.findByQuery(sql, param);
            assertNotNull(test);
            sql = "select d from Filter as d where d.instrument.id = :iid";
            test = iQuery.findByQuery(sql, param);
            assertNotNull(test);
            sql = "select d from Objective as d where d.instrument.id = :iid";
            test = iQuery.findByQuery(sql, param);
            assertNotNull(test);
            sql = "select d from LightSource as d where d.instrument.id = :iid";
            test = iQuery.findByQuery(sql, param);
            assertNotNull(test);
            param = new ParametersI();
        	param.addLong("iid", test.getId().getValue());
            if (ModelMockFactory.LASER.equals(value)) {
            	sql = "select d from Laser as d where d.id = :iid";
            	test = iQuery.findByQuery(sql, param);
            	assertNotNull(test);
            } else if (ModelMockFactory.FILAMENT.equals(value)) {
            	sql = "select d from Filament as d where d.id = :iid";
            	test = iQuery.findByQuery(sql, param);
            	assertNotNull(test);
            } else if (ModelMockFactory.ARC.equals(value)) {
            	sql = "select d from Arc as d where d.id = :iid";
            	test = iQuery.findByQuery(sql, param);
            	assertNotNull(test);
            } else if (ModelMockFactory.LIGHT_EMITTING_DIODE.equals(value)) {
            	sql = "select d from LightEmittingDiode as d where d.id = :iid";
            	test = iQuery.findByQuery(sql, param);
            	assertNotNull(test);
            }
		}
    }
    
    /**
	 * Tests the creation of an instrument using the <code>setInstrument</code> 
	 * method on the entities composing the instrument.
	 * @throws Exception  Thrown if an error occurred.
	 */
    @Test
    public void testCreateInstrumentUsingSet() 
    	throws Exception
    {
    	Instrument instrument = (Instrument) iUpdate.saveAndReturnObject(
    			mmFactory.createInstrument());
    	assertNotNull(instrument);
    	
    	Detector d = mmFactory.createDetector();
    	d.setInstrument((Instrument) instrument.proxy());
    	d = (Detector) iUpdate.saveAndReturnObject(d);
    	assertNotNull(d);
    	
    	Filter f = mmFactory.createFilter(500, 560);
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
    	
    	ParametersI param = new ParametersI();
    	param.addLong("iid", instrument.getId().getValue());
    	//Now check that we have a detector.
    	String sql = "select d from Detector as d where d.instrument.id = :iid";
        IObject test = iQuery.findByQuery(sql, param);
        assertNotNull(test);
        assertNotNull(test.getId().getValue() == d.getId().getValue());
        sql = "select d from Dichroic as d where d.instrument.id = :iid";
        test = iQuery.findByQuery(sql, param);
        assertNotNull(test);
        assertNotNull(test.getId().getValue() == di.getId().getValue());
        sql = "select d from Filter as d where d.instrument.id = :iid";
        test = iQuery.findByQuery(sql, param);
        assertNotNull(test);
        assertNotNull(test.getId().getValue() == f.getId().getValue());
        sql = "select d from Objective as d where d.instrument.id = :iid";
        test = iQuery.findByQuery(sql, param);
        assertNotNull(test);
        assertNotNull(test.getId().getValue() == o.getId().getValue());
        sql = "select d from LightSource as d where d.instrument.id = :iid";
        test = iQuery.findByQuery(sql, param);
        assertNotNull(test);
    }
    
    /**
	 * Tests to delete various types of annotations i.e. 
	 * Boolean, comment, long, tag, file annotation. 
	 * This method the <code>deleteObject</code> method. 
	 * 
	 * @throws Exception  Thrown if an error occurred.
	 */
    @Test(groups = {"ticket:2705"})
    public void testDeleteAnnotation() 
    	throws Exception
    {
    	//creation and linkage have already been tested
    	//boolean
    	BooleanAnnotation b = new BooleanAnnotationI();
    	b.setBoolValue(omero.rtypes.rbool(true));
    	Annotation data = (Annotation) iUpdate.saveAndReturnObject(b);
    	//delete and check
    	long id = data.getId().getValue();
    	iUpdate.deleteObject(data);
    	ParametersI param = new ParametersI();
    	param.addId(id);
    	String sql = "select a from Annotation as a where a.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	//long
    	LongAnnotation l = new LongAnnotationI();
    	l.setLongValue(omero.rtypes.rlong(1L));
    	data = (Annotation) iUpdate.saveAndReturnObject(l);
    	id = data.getId().getValue();
    	iUpdate.deleteObject(data);
    	param = new ParametersI();
    	param.addId(id);
    	sql = "select a from Annotation as a where a.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	//comment
    	CommentAnnotation c = new CommentAnnotationI();
    	c.setTextValue(omero.rtypes.rstring("comment"));
    	data = (Annotation) iUpdate.saveAndReturnObject(c);
    	id = data.getId().getValue();
    	iUpdate.deleteObject(data);
    	param = new ParametersI();
    	param.addId(id);
    	sql = "select a from Annotation as a where a.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	//tag
    	TagAnnotation t = new TagAnnotationI();
    	t.setTextValue(omero.rtypes.rstring("tag"));
    	data = (Annotation) iUpdate.saveAndReturnObject(t);
    	id = data.getId().getValue();
    	iUpdate.deleteObject(data);
    	param = new ParametersI();
    	param.addId(id);
    	sql = "select a from Annotation as a where a.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	//File 
    	OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(
    			mmFactory.createOriginalFile());
		FileAnnotation fa = new FileAnnotationI();
		fa.setFile(of);
		long ofId = of.getId().getValue();
		data = (Annotation) iUpdate.saveAndReturnObject(fa);
		id = data.getId().getValue();
    	iUpdate.deleteObject(data);
    	param = new ParametersI();
    	param.addId(id);
    	sql = "select a from Annotation as a where a.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	param = new ParametersI();
    	param.addId(id);
    	//See ticket #2705
    	//sql = "select a from OriginalFile as a where a.id = :id";
    	//assertNull(iQuery.findByQuery(sql, param));
    	
    	//Term
    	TermAnnotation term = new TermAnnotationI();
    	term.setTermValue(omero.rtypes.rstring("term"));
    	data = (Annotation) iUpdate.saveAndReturnObject(term);
    	id = data.getId().getValue();
    	iUpdate.deleteObject(data);
    	param = new ParametersI();
    	param.addId(id);
    	sql = "select a from Annotation as a where a.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    }

}
