/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import static omero.rtypes.rdouble;
import static omero.rtypes.rint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import omero.ApiUsageException;
import omero.ServerError;
import omero.api.IDeletePrx;
import omero.api.IRenderingSettingsPrx;
import omero.api.delete.DeleteCommand;
import omero.api.delete.DeleteHandlePrx;
import omero.grid.DeleteCallbackI;
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
import omero.model.Detector;
import omero.model.Dichroic;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.FilterSet;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImagingEnvironment;
import omero.model.Instrument;
import omero.model.Laser;
import omero.model.LogicalChannel;
import omero.model.LongAnnotation;
import omero.model.LongAnnotationI;
import omero.model.OTF;
import omero.model.Objective;
import omero.model.ObjectiveSettings;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.PlateAcquisitionAnnotationLink;
import omero.model.PlateAcquisitionAnnotationLinkI;
import omero.model.PlateAnnotationLink;
import omero.model.PlateAnnotationLinkI;
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
import omero.model.ScreenPlateLink;
import omero.model.ScreenPlateLinkI;
import omero.model.Shape;
import omero.model.StageLabel;
import omero.model.StatsInfo;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.TermAnnotation;
import omero.model.TermAnnotationI;
import omero.model.Well;
import omero.model.WellAnnotationLink;
import omero.model.WellAnnotationLinkI;
import omero.model.WellSample;
import omero.model.WellSampleAnnotationLink;
import omero.model.WellSampleAnnotationLinkI;
import omero.sys.ParametersI;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** 
 * Collections of tests for the <code>Delete</code> service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
@Test(groups = {"delete", "integration"})
public class DeleteServiceTest 
	extends AbstractTest
{

    /** Helper reference to the <code>IDelete</code> service. */
    private IDeletePrx iDelete;

    /**
     * Since so many tests rely on counting the number of objects present
     * globally, we're going to start each method with a new user in a new
     * group.
     */
    @BeforeMethod
    public void createNewUser() throws Exception {
        newUserAndGroup("rw----");
        iDelete = factory.getDeleteService();
    }

    /**
     * Basic asychronous delete command. Used in order to reduce the number
     * of places that we do the same thing in case the API changes.
     * @param dc
     * @throws ApiUsageException
     * @throws ServerError
     * @throws InterruptedException
     */
    private void delete(DeleteCommand...dc) throws ApiUsageException, ServerError,
        InterruptedException {
        DeleteHandlePrx handle = iDelete.queueDelete(dc);
        DeleteCallbackI cb = new DeleteCallbackI(client, handle);
        int count = 10;
        while (null == cb.block(500)) {
            count--;
            if (count == 0) {
                throw new RuntimeException("Waiting on delete timed out");
            }
        }
        String report = handle.report().toString();
        assertEquals(report, 0, handle.errors());
    }

    /**
     * Returns a map from IObject instance saved to the DB to delete
     * command need to remove it. E.g.
     * <pre>
     * {
     *   new ProjectI() : "/Project"
     * }
     * </pre>
     */
    private Map<IObject, String> createIObjects() throws Exception {
        Map<IObject, String> objects = new HashMap<IObject, String>();
        objects.put(iUpdate.saveAndReturnObject(mmFactory.createImage()), "/Image");
        objects.put(iUpdate.saveAndReturnObject(mmFactory.simpleDatasetData().asIObject()), "/Dataset");
        objects.put(iUpdate.saveAndReturnObject(mmFactory.simpleProjectData().asIObject()), "/Project");
        objects.put(iUpdate.saveAndReturnObject(mmFactory.simplePlateData().asIObject()), "/Plate");
        objects.put(iUpdate.saveAndReturnObject(mmFactory.simpleScreenData().asIObject()), "/Screen");
        return objects;
    }

    /**
     * Creates a basic query to check if the object has been deleted.
     * 
     * @param i The string identifying the class.
     * @return See above.
     */
    private String createBasicContainerQuery(Class<? extends IObject> k)
    {
		if (Image.class.isAssignableFrom(k)) {
			return "select i from Image as i where i.id = :id";
		} else if (Dataset.class.isAssignableFrom(k)) {
			return "select d from Dataset as d where d.id = :id";
		} else if (Project.class.isAssignableFrom(k)) {
			return "select p from Project as p where p.id = :id";
		} else if (Plate.class.isAssignableFrom(k)) {
			return "select p from Plate as p where p.id = :id";
		} else if (Screen.class.isAssignableFrom(k)) {
			return "select s from Screen as s where s.id = :id";
		}
		throw new UnsupportedOperationException("Unknown type: " + k);
    }
    
    /**
     * Creates various non sharable annotations.
     * 
     * @param parent The object to link the annotation to.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    private List<Long> createNonSharableAnnotation(IObject parent)
    	throws Exception 
    {
    	//creation already tested in UpdateServiceTest
    	List<Long> ids = new ArrayList<Long>();
    	CommentAnnotation c = new CommentAnnotationI();
    	c.setTextValue(omero.rtypes.rstring("comment"));
    	c = (CommentAnnotation) iUpdate.saveAndReturnObject(c);
    	
    	LongAnnotation l = new LongAnnotationI();
    	l.setLongValue(omero.rtypes.rlong(1L));
    	l = (LongAnnotation) iUpdate.saveAndReturnObject(l);
    	
    	BooleanAnnotation b = new BooleanAnnotationI();
    	b.setBoolValue(omero.rtypes.rbool(true));
    	b = (BooleanAnnotation) iUpdate.saveAndReturnObject(b);
    	
    	ids.add(c.getId().getValue());
    	ids.add(l.getId().getValue());
    	ids.add(b.getId().getValue());
    	
    	List<IObject> links = new ArrayList<IObject>();
    	if (parent instanceof Image) {
    		ImageAnnotationLink link = new ImageAnnotationLinkI();
    		link.setChild(c);
    		link.setParent((Image) parent);
    		links.add(link);
    		link = new ImageAnnotationLinkI();
    		link.setChild(l);
    		link.setParent((Image) parent);
    		links.add(link);
    		link = new ImageAnnotationLinkI();
    		link.setChild(b);
    		link.setParent((Image) parent);
    		links.add(link);
    	} else if (parent instanceof Project) {
    		ProjectAnnotationLink link = new ProjectAnnotationLinkI();
    		link.setChild(c);
    		link.setParent((Project) parent);
    		links.add(link);
    		link = new ProjectAnnotationLinkI();
    		link.setChild(l);
    		link.setParent((Project) parent);
    		links.add(link);
    		link = new ProjectAnnotationLinkI();
    		link.setChild(b);
    		link.setParent((Project) parent);
    		links.add(link);
    	} else if (parent instanceof Dataset) {
    		DatasetAnnotationLink link = new DatasetAnnotationLinkI();
    		link.setChild(c);
    		link.setParent((Dataset) parent);
    		links.add(link);
    		link = new DatasetAnnotationLinkI();
    		link.setChild(l);
    		link.setParent((Dataset) parent);
    		links.add(link);
    		link = new DatasetAnnotationLinkI();
    		link.setChild(b);
    		link.setParent((Dataset) parent);
    		links.add(link);
    	} else if (parent instanceof Plate) {
    		PlateAnnotationLink link = new PlateAnnotationLinkI();
    		link.setChild(c);
    		link.setParent((Plate) parent);
    		links.add(link);
    		link = new PlateAnnotationLinkI();
    		link.setChild(l);
    		link.setParent((Plate) parent);
    		links.add(link);
    		link = new PlateAnnotationLinkI();
    		link.setChild(b);
    		link.setParent((Plate) parent);
    		links.add(link);
    	} else if (parent instanceof Screen) {
    		ScreenAnnotationLink link = new ScreenAnnotationLinkI();
    		link.setChild(c);
    		link.setParent((Screen) parent);
    		links.add(link);
    		link = new ScreenAnnotationLinkI();
    		link.setChild(l);
    		link.setParent((Screen) parent);
    		links.add(link);
    		link = new ScreenAnnotationLinkI();
    		link.setChild(b);
    		link.setParent((Screen) parent);
    		links.add(link);
    	} else if (parent instanceof Well) {
    		WellAnnotationLink link = new WellAnnotationLinkI();
    		link.setChild(c);
    		link.setParent((Well) parent);
    		links.add(link);
    		link = new WellAnnotationLinkI();
    		link.setChild(l);
    		link.setParent((Well) parent);
    		links.add(link);
    		link = new WellAnnotationLinkI();
    		link.setChild(b);
    		link.setParent((Well) parent);
    		links.add(link);
    	} else if (parent instanceof WellSample) {
    		WellSampleAnnotationLink link = new WellSampleAnnotationLinkI();
    		link.setChild(c);
    		link.setParent((WellSample) parent);
    		links.add(link);
    		link = new WellSampleAnnotationLinkI();
    		link.setChild(l);
    		link.setParent((WellSample) parent);
    		links.add(link);
    		link = new WellSampleAnnotationLinkI();
    		link.setChild(b);
    		link.setParent((WellSample) parent);
    		links.add(link);
    	} else if (parent instanceof PlateAcquisition) {
    		PlateAcquisitionAnnotationLink link = 
    			new PlateAcquisitionAnnotationLinkI();
    		link.setChild(c);
    		link.setParent((PlateAcquisition) parent);
    		links.add(link);
    		link = new PlateAcquisitionAnnotationLinkI();
    		link.setChild(l);
    		link.setParent((PlateAcquisition) parent);
    		links.add(link);
    		link = new PlateAcquisitionAnnotationLinkI();
    		link.setChild(b);
    		link.setParent((PlateAcquisition) parent);
    		links.add(link);
    	} 
    	if (links.size() > 0) iUpdate.saveAndReturnArray(links);
    	return ids;
    }
    
    /**
     * Creates various non sharable annotations.
     * 
     * @param parent The object to link the annotation to.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    private List<Long> createSharableAnnotation(IObject parent)
    	throws Exception 
    {
    	//creation already tested in UpdateServiceTest
    	List<Long> ids = new ArrayList<Long>();
    	TagAnnotation c = new TagAnnotationI();
    	c.setTextValue(omero.rtypes.rstring("tag"));
    	c = (TagAnnotation) iUpdate.saveAndReturnObject(c);
    	ids.add(c.getId().getValue());
    	
    	TermAnnotation t = new TermAnnotationI();
    	t.setTermValue(omero.rtypes.rstring("term"));
    	t = (TermAnnotation) iUpdate.saveAndReturnObject(t);
    	ids.add(t.getId().getValue());
    	
    	OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(
				mmFactory.createOriginalFile());
		assertNotNull(of);
		FileAnnotation f = new FileAnnotationI();
		f.setFile(of);
		f = (FileAnnotation) iUpdate.saveAndReturnObject(f);
		ids.add(f.getId().getValue());
		
    	List<IObject> links = new ArrayList<IObject>();
    	if (parent instanceof Image) {
    		ImageAnnotationLink link = new ImageAnnotationLinkI();
    		link.setChild(c);
    		link.setParent((Image) parent);
    		links.add(link);
    		link = new ImageAnnotationLinkI();
    		link.setChild(t);
    		link.setParent((Image) parent);
    		links.add(link);
    		link = new ImageAnnotationLinkI();
    		link.setChild(f);
    		link.setParent((Image) parent);
    		links.add(link);
    	} else if (parent instanceof Project) {
    		ProjectAnnotationLink link = new ProjectAnnotationLinkI();
    		link.setChild(c);
    		link.setParent((Project) parent);
    		links.add(link);
    		link = new ProjectAnnotationLinkI();
    		link.setChild(t);
    		link.setParent((Project) parent);
    		links.add(link);
    		link = new ProjectAnnotationLinkI();
    		link.setChild(f);
    		link.setParent((Project) parent);
    		links.add(link);
    	} else if (parent instanceof Dataset) {
    		DatasetAnnotationLink link = new DatasetAnnotationLinkI();
    		link.setChild(c);
    		link.setParent((Dataset) parent);
    		links.add(link);
    		link = new DatasetAnnotationLinkI();
    		link.setChild(t);
    		link.setParent((Dataset) parent);
    		links.add(link);
    		link = new DatasetAnnotationLinkI();
    		link.setChild(f);
    		link.setParent((Dataset) parent);
    		links.add(link);
    	} else if (parent instanceof Plate) {
    		PlateAnnotationLink link = new PlateAnnotationLinkI();
    		link.setChild(c);
    		link.setParent((Plate) parent);
    		links.add(link);
    		link = new PlateAnnotationLinkI();
    		link.setChild(t);
    		link.setParent((Plate) parent);
    		links.add(link);
    		link = new PlateAnnotationLinkI();
    		link.setChild(f);
    		link.setParent((Plate) parent);
    		links.add(link);
    	} else if (parent instanceof Screen) {
    		ScreenAnnotationLink link = new ScreenAnnotationLinkI();
    		link.setChild(c);
    		link.setParent((Screen) parent);
    		links.add(link);
    		link = new ScreenAnnotationLinkI();
    		link.setChild(t);
    		link.setParent((Screen) parent);
    		links.add(link);
    		link = new ScreenAnnotationLinkI();
    		link.setChild(f);
    		link.setParent((Screen) parent);
    		links.add(link);
    	} else if (parent instanceof Well) {
    		WellAnnotationLink link = new WellAnnotationLinkI();
    		link.setChild(c);
    		link.setParent((Well) parent);
    		links.add(link);
    		link = new WellAnnotationLinkI();
    		link.setChild(t);
    		link.setParent((Well) parent);
    		links.add(link);
    		link = new WellAnnotationLinkI();
    		link.setChild(f);
    		link.setParent((Well) parent);
    		links.add(link);
    	} else if (parent instanceof WellSample) {
    		WellSampleAnnotationLink link = new WellSampleAnnotationLinkI();
    		link.setChild(c);
    		link.setParent((WellSample) parent);
    		links.add(link);
    		link = new WellSampleAnnotationLinkI();
    		link.setChild(t);
    		link.setParent((WellSample) parent);
    		links.add(link);
    		link = new WellSampleAnnotationLinkI();
    		link.setChild(f);
    		link.setParent((WellSample) parent);
    		links.add(link);
    	} else if (parent instanceof PlateAcquisition) {
    		PlateAcquisitionAnnotationLink link = 
    			new PlateAcquisitionAnnotationLinkI();
    		link.setChild(c);
    		link.setParent((PlateAcquisition) parent);
    		links.add(link);
    		link = new PlateAcquisitionAnnotationLinkI();
    		link.setChild(t);
    		link.setParent((PlateAcquisition) parent);
    		links.add(link);
    		link = new PlateAcquisitionAnnotationLinkI();
    		link.setChild(f);
    		link.setParent((PlateAcquisition) parent);
    		links.add(link);
    	} 
    	if (links.size() > 0) iUpdate.saveAndReturnArray(links);
    	return ids;
    }
    
    /**
     * Test to delete an image w/o pixels.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteBasicImage() 
    	throws Exception
    {
    	Image img = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.simpleImage(0));
    	assertNotNull(img);
    	long id = img.getId().getValue();
    	iDelete.deleteImage(id, false); //do not force.
    	ParametersI param = new ParametersI();
    	param.addId(id);

    	StringBuilder sb = new StringBuilder();
    	sb.append("select i from Image i ");
    	sb.append("where i.id = :id");
    	img = (Image) iQuery.findByQuery(sb.toString(), param);
    	assertNull(img);
    }
    
    /**
     * Test to delete a simple plate i.e. w/o wells or acquisition.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteEmptyPlate() 
    	throws Exception
    {
    	Plate p = (Plate) iUpdate.saveAndReturnObject(
    			mmFactory.simplePlateData().asIObject());
    	assertNotNull(p);
    	long id = p.getId().getValue();
    	iDelete.deletePlate(id);
    	ParametersI param = new ParametersI();
    	param.addId(id);

    	StringBuilder sb = new StringBuilder();
    	sb.append("select i from Plate i ");
    	sb.append("where i.id = :id");
    	p = (Plate) iQuery.findByQuery(sb.toString(), param);
    	assertNull(p);
    }
    
    /**
     * Test to delete a populated plate.
     * The boolean flag indicates to create or no plate acquisition.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = false)
    public void testDeletePlate() 
    	throws Exception
    {
    	Boolean[] values = {Boolean.valueOf(false)};//, Boolean.valueOf(true)};
    	Boolean b;
    	Plate p;
    	List results;
    	PlateAcquisition pa = null;
    	StringBuilder sb;
    	Well well;
    	WellSample field;
    	Iterator j;
		ParametersI param;
		List<Long> wellSampleIds;
		List<Long> imageIds;
    	for (int i = 0; i < values.length; i++) {
			b = values[i];
			p = (Plate) iUpdate.saveAndReturnObject(
					mmFactory.createPlate(1, 1, 1, b, false));
			param = new ParametersI();
			param.addLong("plateID", p.getId().getValue());
			sb = new StringBuilder();
			sb.append("select well from Well as well ");
			sb.append("left outer join fetch well.plate as pt ");
			sb.append("left outer join fetch well.wellSamples as ws ");
			sb.append("left outer join fetch ws.image as img ");
	        sb.append("where pt.id = :plateID");
	        results = iQuery.findAllByQuery(sb.toString(), param);
	        
	        sb = new StringBuilder();
	        sb.append("select pa from PlateAcquisition as pa " +
	        		"where pa.plate.id = :plateID"); 
	        pa = (PlateAcquisition) iQuery.findByQuery(sb.toString(), param);
	        
	        j = results.iterator();
	        wellSampleIds = new ArrayList<Long>();
	        imageIds = new ArrayList<Long>();
	        while (j.hasNext()) {
				well = (Well) j.next();
				for (int k = 0; k < well.sizeOfWellSamples(); k++) {
					field = well.getWellSample(k);
					wellSampleIds.add(field.getId().getValue());
					assertNotNull(field.getImage());
					imageIds.add(field.getImage().getId().getValue());
				}
			}
	        //Now delete the plate
	        iDelete.deletePlate(p.getId().getValue());
	        
	        param = new ParametersI();
	        param.addId(p.getId().getValue());
	        sb = new StringBuilder();
	        //check the plate
	        sb.append("select p from Plate as p where p.id = :id");
	        assertNull(iQuery.findByQuery(sb.toString(), param));
	        
	        //check the well
	        param = new ParametersI();
	        param.addLong("plateID", p.getId().getValue());
	        sb = new StringBuilder();
			sb.append("select well from Well as well ");
			sb.append("left outer join fetch well.plate as pt ");
			sb.append("where pt.id = :plateID");
			results = iQuery.findAllByQuery(sb.toString(), param);
	        assertTrue(results.size() == 0);
	        
	        //check the well samples.
	        sb = new StringBuilder();
	        param = new ParametersI();
	        param.addIds(wellSampleIds);
	        sb.append("select p from WellSample as p where p.id in (:ids)");
	        results = iQuery.findAllByQuery(sb.toString(), param);
	        assertTrue(results.size() == 0);
	        
	        //check the image.
	        sb = new StringBuilder();
	        param = new ParametersI();
	        param.addIds(imageIds);
	        sb.append("select p from Image as p where p.id in (:ids)");
	        results = iQuery.findAllByQuery(sb.toString(), param);
	        assertTrue(results.size() == 0);
	        if (pa != null && b) {
	        	param = new ParametersI();
		        param.addId(pa.getId().getValue());
		        sb = new StringBuilder();
		        //check the plate
		        sb.append("select p from PlateAcquisition as p " +
		        		"where p.id = :id");
		        assertNull(iQuery.findByQuery(sb.toString(), param));
	        }
		}
    }

    /**
     * Tests to delete a dataset with images.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteDataset() 
    	throws Exception
    {
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	Image image1 = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.simpleImage(0));
    	Image image2 = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.simpleImage(0));
    	List<IObject> links = new ArrayList<IObject>();
    	DatasetImageLink link = new DatasetImageLinkI();
    	link.setChild(image1);
    	link.setParent(d);
    	links.add(link);
    	
    	link = new DatasetImageLinkI();
    	link.setChild(image2);
    	link.setParent(d);
    	links.add(link);
    	
    	iUpdate.saveAndReturnArray(links);
    	
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(image1.getId().getValue());
    	ids.add(image2.getId().getValue());


        DeleteCommand dc = new DeleteCommand("/Dataset", d.getId().getValue(), null);
        delete(dc);

    	
    	//Check if objects have been deleted
    	ParametersI param = new ParametersI();
    	param.addIds(ids);
    	String sql = "select i from Image as i where i.id in (:ids)";
    	List results = iQuery.findAllByQuery(sql, param);
    	assertTrue(results.size() == 0);
    	
    	param = new ParametersI();
    	param.addId(d.getId().getValue());
    	sql = "select i from Dataset as i where i.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    }

    /**
     * Tests to delete a project containing a dataset with images.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteProject() 
    	throws Exception
    {
    	Project p = (Project) iUpdate.saveAndReturnObject(
    			mmFactory.simpleProjectData().asIObject());
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	Image image1 = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.simpleImage(0));
    	Image image2 = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.simpleImage(0));
    	List<IObject> links = new ArrayList<IObject>();
    	DatasetImageLink link = new DatasetImageLinkI();
    	link.setChild(image1);
    	link.setParent(d);
    	links.add(link);
    	
    	link = new DatasetImageLinkI();
    	link.setChild(image2);
    	link.setParent(d);
    	links.add(link);
    	
    	ProjectDatasetLink l = new ProjectDatasetLinkI();
    	l.setChild(d);
    	l.setParent(p);
    	links.add(l);
    	iUpdate.saveAndReturnArray(links);
    	
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(image1.getId().getValue());
    	ids.add(image2.getId().getValue());


        DeleteCommand dc = new DeleteCommand("/Project", p.getId().getValue(), null);
        delete(dc);

    	
    	//Check if objects have been deleted
    	ParametersI param = new ParametersI();
    	param.addIds(ids);
    	String sql = "select i from Image as i where i.id in (:ids)";
    	List results = iQuery.findAllByQuery(sql, param);
    	assertTrue(results.size() == 0);
    	
    	param = new ParametersI();
    	param.addId(d.getId().getValue());
    	sql = "select i from Dataset as i where i.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	
    	param = new ParametersI();
    	param.addId(p.getId().getValue());
    	sql = "select i from Project as i where i.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    }
    
    /**
     * Tests to delete a screen containing 2 plates, one w/o plate acquisition
     * and one with plate acquisition.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = false) // SPW not implemented
    public void testDeleteScreen() 
    	throws Exception
    {
    	Screen screen = (Screen) iUpdate.saveAndReturnObject(
    			mmFactory.simpleScreenData().asIObject());
    	//Plate w/o plate acquisition
    	Plate p1 = (Plate) iUpdate.saveAndReturnObject(
    			mmFactory.createPlate(1, 1, 1, false, false)); 
    	//Plate with plate acquisition
    	Plate p2 = (Plate) iUpdate.saveAndReturnObject(
    			mmFactory.createPlate(1, 1, 1, true, false));
    	List<IObject> links = new ArrayList<IObject>();
    	ScreenPlateLink link = new ScreenPlateLinkI();
    	link.setChild(p1);
    	link.setParent(screen);
    	links.add(link);
    	link = new ScreenPlateLinkI();
    	link.setChild(p2);
    	link.setParent(screen);
    	links.add(link);
    	iUpdate.saveAndReturnArray(links);

    	
        DeleteCommand dc = new DeleteCommand("/Screen", screen.getId().getValue(), null);
        delete(dc);

    	
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(p1.getId().getValue());
    	ids.add(p2.getId().getValue());
    	
    	//Check if the plates exist.
    	ParametersI param = new ParametersI();
    	param.addIds(ids);
    	String sql = "select i from Plate as i where i.id in (:ids)";
    	List results = iQuery.findAllByQuery(sql, param);
    	assertTrue(results.size() == 0);
    	
    	param = new ParametersI();
    	param.addId(screen.getId().getValue());
    	sql = "select i from Screen as i where i.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    }
    
    /**
     * Test to delete an image with pixels, channels, logical channels 
     * and statistics.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteImage() 
    	throws Exception
    {
    	Image img = mmFactory.createImage();
    	img = (Image) iUpdate.saveAndReturnObject(img);
    	Pixels pixels = img.getPrimaryPixels();
    	long pixId = pixels.getId().getValue();
    	//method already tested in PixelsServiceTest
    	//make sure objects are loaded.
    	pixels = factory.getPixelsService().retrievePixDescription(pixId);
    	//channels.
    	long id = img.getId().getValue();
    	
    	List<Long> channels = new ArrayList<Long>();
    	List<Long> logicalChannels = new ArrayList<Long>();
    	List<Long> infos = new ArrayList<Long>();
    	Channel channel;
    	LogicalChannel lc;
    	StatsInfo info;
    	for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
			channel = pixels.getChannel(i);
			assertNotNull(channel);
			channels.add(channel.getId().getValue());
			lc = channel.getLogicalChannel();
			assertNotNull(lc);
			logicalChannels.add(lc.getId().getValue());
			info = channel.getStatsInfo();
			assertNotNull(info);
			infos.add(info.getId().getValue());
		}
    	
    	iDelete.deleteImage(id, false); //do not force.
    	ParametersI param = new ParametersI();
    	param.addId(id);

    	StringBuilder sb = new StringBuilder();
    	sb.append("select i from Image i ");
    	sb.append("where i.id = :id");
    	img = (Image) iQuery.findByQuery(sb.toString(), param);
    	assertNull(img);
    	sb = new StringBuilder();
    	param = new ParametersI();
    	param.addId(pixId);
    	sb.append("select i from Pixels i ");
    	sb.append("where i.id = :id");
    	pixels = (Pixels) iQuery.findByQuery(sb.toString(), param);
    	assertNull(img);
    	Iterator<Long> i = channels.iterator();
    	while (i.hasNext()) {
			id =  i.next();
			param = new ParametersI();
	    	param.addId(id);
	    	sb = new StringBuilder();
	    	sb.append("select i from Channel i ");
	    	sb.append("where i.id = :id");
	    	channel = (Channel) iQuery.findByQuery(sb.toString(), param);
	    	assertNull(channel);
		}
    	i = infos.iterator();
    	while (i.hasNext()) {
			id =  i.next();
			param = new ParametersI();
	    	param.addId(id);
	    	sb = new StringBuilder();
	    	sb.append("select i from StatsInfo i ");
	    	sb.append("where i.id = :id");
	    	info = (StatsInfo) iQuery.findByQuery(sb.toString(), param);
	    	assertNull(info);
		}
    	i = logicalChannels.iterator();
    	while (i.hasNext()) {
			id =  i.next();
			param = new ParametersI();
	    	param.addId(id);
	    	sb = new StringBuilder();
	    	sb.append("select i from LogicalChannel i ");
	    	sb.append("where i.id = :id");
	    	lc = (LogicalChannel) iQuery.findByQuery(sb.toString(), param);
	    	assertNull(lc);
		}
    }

    /**
     * Test to delete an image with rendering settings.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteImageWithRenderingSettings() 
    	throws Exception
    {
    	Image img = mmFactory.createImage();
    	img = (Image) iUpdate.saveAndReturnObject(img);
    	Pixels pixels = img.getPrimaryPixels();
    	//method already tested in RenderingSettingsServiceTest
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(pixels.getId().getValue());
    	prx.resetDefaultsInSet(Pixels.class.getName(), ids);
    	//check if we have settings now.
    	ParametersI param = new ParametersI();
    	param.addLong("pid", pixels.getId().getValue());
    	String sql = "select rdef from RenderingDef as rdef " +
    			"where rdef.pixels.id = :pid";
    	List<IObject> settings = iQuery.findAllByQuery(sql, param);
    	//now delete the image
    	assertTrue(settings.size() > 0);
    	iDelete.deleteImage(img.getId().getValue(), false); //do not force.
    	//check if the settings have been deleted.
    	Iterator<IObject> i = settings.iterator();
    	IObject o;
    	while (i.hasNext()) {
			o = i.next();
			param = new ParametersI();
			param.addId(o.getId().getValue());
			sql = "select rdef from RenderingDef as rdef " +
			"where rdef.id = :id";
			o = iQuery.findByQuery(sql, param);
			assertNull(o);
		}
    }
    
    /**
     * Test to delete an image with acquisition data.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteImageWithAcquisitionData() 
    	throws Exception
    {
    	Image img = mmFactory.createImage();
    	img = (Image) iUpdate.saveAndReturnObject(img);
    	Pixels pixels = img.getPrimaryPixels();
    	long pixId = pixels.getId().getValue();
    	//method already tested in PixelsServiceTest
    	//make sure objects are loaded.
    	pixels = factory.getPixelsService().retrievePixDescription(pixId);
    	//create an instrument.
    	Instrument instrument = mmFactory.createInstrument(
    			ModelMockFactory.LASER);
    	instrument = (Instrument) iUpdate.saveAndReturnObject(instrument);
    	assertNotNull(instrument);

    	//retrieve the elements we need for the settings.
    	//retrieve the detector.
    	ParametersI param = new ParametersI();
    	param.addLong("iid", instrument.getId().getValue());
    	String sql = "select d from Detector as d where d.instrument.id = :iid";
    	Detector detector = (Detector) iQuery.findByQuery(sql, param);
    	sql = "select d from FilterSet as d where d.instrument.id = :iid";
    	FilterSet filterSet = (FilterSet) iQuery.findByQuery(sql, param);
    	sql = "select d from Laser as d where d.instrument.id = :iid";
    	Laser laser = (Laser) iQuery.findByQuery(sql, param);
    	sql = "select d from Dichroic as d where d.instrument.id = :iid";
    	Dichroic dichroic = (Dichroic) iQuery.findByQuery(sql, param);
    	sql = "select d from OTF as d where d.instrument.id = :iid";
    	OTF otf = (OTF) iQuery.findByQuery(sql, param);
    	sql = "select d from Objective as d where d.instrument.id = :iid";
    	Objective objective = (Objective) iQuery.findByQuery(sql, param);
    	
    	img.setImagingEnvironment(mmFactory.createImageEnvironment());
    	img.setObjectiveSettings(mmFactory.createObjectiveSettings(objective));
    	img.setStageLabel(mmFactory.createStageLabel());
    	iUpdate.saveAndReturnObject(img);
    	param = new ParametersI();
    	param.acquisitionData();
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(img.getId().getValue());
    	//method already tested in PojosService test
    	List results = factory.getContainerService().getImages(
    			Image.class.getName(), ids, param);
    	img = (Image) results.get(0);
    	ObjectiveSettings settings = img.getObjectiveSettings();
    	StageLabel label = img.getStageLabel();
    	ImagingEnvironment env = img.getImagingEnvironment();
    	
    	LogicalChannel lc;
    	Channel channel;
    	ids = new ArrayList<Long>();
    	long detectorSettingsID = 0;
    	long lightSourceSettingsID = 0;
    	long ligthPathID = 0;
    	for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
			channel = pixels.getChannel(i);
			lc = channel.getLogicalChannel();
			lc.setOtf(otf);
	    	lc.setDetectorSettings(mmFactory.createDetectorSettings(detector));
	    	lc.setFilterSet(filterSet);
	    	lc.setLightSourceSettings(mmFactory.createLightSettings(laser));
	    	lc.setLightPath(mmFactory.createLightPath(null, dichroic, null));
	    	lc = (LogicalChannel) iUpdate.saveAndReturnObject(lc);
	    	assertNotNull(lc);
	    	ids.add(lc.getId().getValue());
	    	detectorSettingsID = lc.getDetectorSettings().getId().getValue();
	    	lightSourceSettingsID = 
	    		lc.getLightSourceSettings().getId().getValue();
	    	ligthPathID = lc.getLightPath().getId().getValue();
		}
    	
    	//Now we try to delete the image.
    	iDelete.deleteImage(img.getId().getValue(), true);
    	//Follow the section with acquisition data.
    	//Now check if the settings are still there.

    	param = new ParametersI();
    	param.addId(detectorSettingsID);
    	sql = "select d from DetectorSettings as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	param.addId(lightSourceSettingsID);
    	sql = "select d from LightSourceSettings as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	param.addId(ligthPathID);
    	sql = "select d from LightPath as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	
    	//instrument
    	param.addId(instrument.getId().getValue());
    	sql = "select d from Instrument as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	param.addId(detector.getId().getValue());
    	sql = "select d from Detector as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	
    	param.addId(otf.getId().getValue());
    	sql = "select d from OTF as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	
    	param.addId(objective.getId().getValue());
    	sql = "select d from Objective as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	
    	param.addId(dichroic.getId().getValue());
    	sql = "select d from Dichroic as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	
    	param.addId(filterSet.getId().getValue());
    	sql = "select d from FilterSet as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	
    	param.addId(laser.getId().getValue());
    	sql = "select d from Laser as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	
    	param.addId(settings.getId().getValue());
    	sql = "select d from ObjectiveSettings as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	
    	param.addId(env.getId().getValue());
    	sql = "select d from ImagingEnvironment as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	
    	param.addId(env.getId().getValue());
    	sql = "select d from StageLabel as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    }
    
    /**
     * Test to delete an image with ROis.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteImageWithROIs() 
    	throws Exception
    {
    	Image image = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.simpleImage(0));
    	Roi roi = new RoiI();
    	roi.setImage(image);
    	Rect rect;
    	Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
    	for (int i = 0; i < 3; i++) {
    		rect = new RectI();
    		rect.setX(rdouble(10));
    		rect.setY(rdouble(10));
    		rect.setWidth(rdouble(10));
    		rect.setHeight(rdouble(10));
    		rect.setTheZ(rint(i));
    		rect.setTheT(rint(0));
    		serverROI.addShape(rect);
    	}
    	serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);
    	List<Long> shapeIds = new ArrayList<Long>();
    	Shape shape;
    	for (int i = 0; i < serverROI.sizeOfShapes(); i++) {
    		shape = serverROI.getShape(i);
    		shapeIds.add(shape.getId().getValue());
    	}
    	//Delete the image.
    	iDelete.deleteImage(image.getId().getValue(), true);
    	//check if the objects have been delete.

    	ParametersI param = new ParametersI();
    	param.addId(serverROI.getId().getValue());
    	String sql = "select d from Roi as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));  
    	
    	//shapes
    	param = new ParametersI();
    	param.addIds(shapeIds);
    	sql = "select d from Shape as d where d.id in (:ids)";
    	List results = iQuery.findAllByQuery(sql, param);
    	assertTrue(results.size() == 0);

    }
    
    /**
     * Test to deletes rois.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteROIs() 
    	throws Exception
    {
    	Image image = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.simpleImage(0));
    	Roi roi = new RoiI();
    	roi.setImage(image);
    	Rect rect;
    	Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
    	for (int i = 0; i < 3; i++) {
    		rect = new RectI();
    		rect.setX(rdouble(10));
    		rect.setY(rdouble(10));
    		rect.setWidth(rdouble(10));
    		rect.setHeight(rdouble(10));
    		rect.setTheZ(rint(i));
    		rect.setTheT(rint(0));
    		serverROI.addShape(rect);
    	}
    	serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);
    	List<Long> shapeIds = new ArrayList<Long>();
    	Shape shape;
    	for (int i = 0; i < serverROI.sizeOfShapes(); i++) {
    		shape = serverROI.getShape(i);
    		shapeIds.add(shape.getId().getValue());
    	}

    	
	DeleteCommand dc = new DeleteCommand("/Roi", serverROI.getId().getValue(), null);
	delete(dc);

    	
    	//make sure we still have the image
    	ParametersI param = new ParametersI();
    	param.addId(image.getId().getValue());
    	String sql = "select d from Image as d where d.id = :id";
    	assertNotNull(iQuery.findByQuery(sql, param));  
    	
    	//check if the objects have been delete.
	param = new ParametersI();
    	param.addId(serverROI.getId().getValue());
    	sql = "select d from Roi as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));  
    	
    	//shapes
    	param = new ParametersI();
    	param.addIds(shapeIds);
    	sql = "select d from Shape as d where d.id in (:ids)";
    	List results = iQuery.findAllByQuery(sql, param);
    	assertTrue(results.size() == 0);
    }

    /**
     * Test to delete object with annotations that cannot be shared
     * e.g. tags terms. The test will 
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = false)
    public void testDeleteObjectWithNonSharableAnnotations() 
    	throws Exception
    {
	Map<IObject, String> objects = createIObjects();
    	IObject obj = null;
	Long id = null;
	String type = null;
    	List<Long> annotationIds;
    	ParametersI param;
    	String sql;
	for (Map.Entry<IObject, String> entry : objects.entrySet()) {
		obj = entry.getKey();
	    type = entry.getValue();
	    id = obj.getId().getValue();
			annotationIds = createNonSharableAnnotation(obj);	

			DeleteCommand dc =
			    new DeleteCommand(type, id, null);
			delete(dc);

			param = new ParametersI();
	    	param.addId(obj.getId().getValue());
		sql = createBasicContainerQuery(obj.getClass());
			assertNull(iQuery.findByQuery(sql, param));
			param = new ParametersI();
	    	param.addIds(annotationIds);
	    	assertTrue(annotationIds.size() > 0);
	    	sql = "select i from Annotation as i where i.id in (:ids)";
	    	assertTrue(iQuery.findAllByQuery(sql, param).size() == 0);	
		}
    }
    
    /**
     * Test to delete object with annotations that cannot be shared
     * e.g. tags, terms. One run will delete the annotations, a second 
     * will be them.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = false)
    public void testDeleteObjectWithSharableAnnotations() 
    	throws Exception
    {
	Map<IObject, String> objects = createIObjects();
    	Boolean[] values = {Boolean.valueOf(false), Boolean.valueOf(true)};
    	IObject obj = null;
	String type = null;
	Long id = null;
    	List<Long> annotationIds;
    	ParametersI param;
    	String sql;
    	List l;
	for (Map.Entry<IObject, String> entry : objects.entrySet()) {
    		for (int j = 0; j < values.length; j++) {
			obj = entry.getKey();
			type = entry.getValue();
			id = obj.getId().getValue();
    			annotationIds = createSharableAnnotation(obj);	

			DeleteCommand dc = new DeleteCommand(type, id, null);
			delete(dc);

    			param = new ParametersI();
    	    	param.addId(obj.getId().getValue());
		sql = createBasicContainerQuery(obj.getClass());
    			assertNull(iQuery.findByQuery(sql, param));
    			//annotations should be deleted to
    			param = new ParametersI();
    	    	param.addIds(annotationIds);
    	    	assertTrue(annotationIds.size() > 0);
    	    	sql = "select i from Annotation as i where i.id in (:ids)";
    	    	l = iQuery.findAllByQuery(sql, param);
    	    	if (values[j]) {
    	    		assertTrue(l.size() == annotationIds.size());	
    	    	} else {
    	    		assertTrue(l.size() == 0);	
    	    	}
			}
		}
    }
    
    /**
     * Test to delete a plate with non sharable annotations linked to the well 
     * and well samples and plate with Plate acquisition and annotation.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = false) // SPW not implemented
    public void testPlateWithNonSharableAnnotations() 
    	throws Exception
    {
    	Boolean[] values = {Boolean.valueOf(false)};//, Boolean.valueOf(true)};

    	Boolean b;
    	Plate p;
    	List results;
    	PlateAcquisition pa = null;
    	StringBuilder sb;
    	Well well;
    	WellSample field;
    	Iterator j;
		ParametersI param;
		List<Long> wellSampleIds;
		List<Long> imageIds;
		List<Long> annotationIds = new ArrayList<Long>();
		List<Long> r;
		List l;
    	for (int i = 0; i < values.length; i++) {
    		b = values[i];
			p = (Plate) iUpdate.saveAndReturnObject(
					mmFactory.createPlate(1, 1, 1, b, false));
			param = new ParametersI();
			param.addLong("plateID", p.getId().getValue());
			sb = new StringBuilder();
			sb.append("select well from Well as well ");
			sb.append("left outer join fetch well.plate as pt ");
			sb.append("left outer join fetch well.wellSamples as ws ");
			sb.append("left outer join fetch ws.image as img ");
	        sb.append("where pt.id = :plateID");
	        results = iQuery.findAllByQuery(sb.toString(), param);
	        
	        sb = new StringBuilder();
	        sb.append("select pa from PlateAcquisition as pa " +
	        		"where pa.plate.id = :plateID"); 
	        pa = (PlateAcquisition) iQuery.findByQuery(sb.toString(), param);
	        
	        j = results.iterator();
	        wellSampleIds = new ArrayList<Long>();
	        imageIds = new ArrayList<Long>();
	        while (j.hasNext()) {
				well = (Well) j.next();
				r = createNonSharableAnnotation(well);
				if (r.size() > 0) annotationIds.addAll(r);
				for (int f = 0; f < well.sizeOfWellSamples(); f++) {
					field = well.getWellSample(f);
					r = createSharableAnnotation(field);
					if (r.size() > 0) annotationIds.addAll(r);
					wellSampleIds.add(field.getId().getValue());
					assertNotNull(field.getImage());
					imageIds.add(field.getImage().getId().getValue());
				}
			}
	        if (pa != null && b) {
	        	r = createNonSharableAnnotation(pa);
				if (r.size() > 0) annotationIds.addAll(r);
	        }
	        //Now delete the plate
	        iDelete.deletePlate(p.getId().getValue());
	        
	        param = new ParametersI();
	        param.addId(p.getId().getValue());
	        sb = new StringBuilder();
	        //check the plate
	        sb.append("select p from Plate as p where p.id = :id");
	        assertNull(iQuery.findByQuery(sb.toString(), param));
	        
	        //check the well
	        param = new ParametersI();
	        param.addLong("plateID", p.getId().getValue());
	        sb = new StringBuilder();
			sb.append("select well from Well as well ");
			sb.append("left outer join fetch well.plate as pt ");
			sb.append("where pt.id = :plateID");
			results = iQuery.findAllByQuery(sb.toString(), param);
	        assertTrue(results.size() == 0);
	        
	        //check the well samples.
	        sb = new StringBuilder();
	        param = new ParametersI();
	        param.addIds(wellSampleIds);
	        sb.append("select p from WellSample as p where p.id in (:ids)");
	        results = iQuery.findAllByQuery(sb.toString(), param);
	        assertTrue(results.size() == 0);
	        
	        //check the image.
	        sb = new StringBuilder();
	        param = new ParametersI();
	        param.addIds(imageIds);
	        sb.append("select p from Image as p where p.id in (:ids)");
	        results = iQuery.findAllByQuery(sb.toString(), param);
	        assertTrue(results.size() == 0);
	        if (pa != null && b) {
	        	param = new ParametersI();
		        param.addId(pa.getId().getValue());
		        sb = new StringBuilder();
		        //check the plate
		        sb.append("select p from PlateAcquisition as p " +
		        		"where p.id = :id");
		        assertNull(iQuery.findByQuery(sb.toString(), param));
	        }
	        //Check if annotations have been deleted.
	        param = new ParametersI();
	    	param.addIds(annotationIds);
	    	assertTrue(annotationIds.size() > 0);
	    	sb = new StringBuilder();
	    	sb.append("select i from Annotation as i where i.id in (:ids)");
	    	l = iQuery.findAllByQuery(sb.toString(), param);
	    	assertTrue(l.size() == 0);
		}
    }
    
    /**
     * Test to delete a plate with sharable annotations linked to the well and 
     * well samples and plate with Plate acquisition and annotation.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = false)
    public void testPlateWithSharableAnnotations() 
    	throws Exception
    {
    	Boolean[] values = {Boolean.valueOf(false)};//, Boolean.valueOf(true)};
    	Boolean[] annotations = {Boolean.valueOf(false), Boolean.valueOf(true)};
    	Boolean b;
    	Plate p;
    	List results;
    	PlateAcquisition pa = null;
    	StringBuilder sb;
    	Well well;
    	WellSample field;
    	Iterator j;
		ParametersI param;
		List<Long> wellSampleIds;
		List<Long> imageIds;
		List<Long> annotationIds = new ArrayList<Long>();
		List<Long> r;
		List l;
    	for (int i = 0; i < values.length; i++) {
    		for (int k = 0; k < annotations.length; k++) {
    			b = values[i];
    			p = (Plate) iUpdate.saveAndReturnObject(
    					mmFactory.createPlate(1, 1, 1, b, false));
    			param = new ParametersI();
    			param.addLong("plateID", p.getId().getValue());
    			sb = new StringBuilder();
    			sb.append("select well from Well as well ");
    			sb.append("left outer join fetch well.plate as pt ");
    			sb.append("left outer join fetch well.wellSamples as ws ");
    			sb.append("left outer join fetch ws.image as img ");
    	        sb.append("where pt.id = :plateID");
    	        results = iQuery.findAllByQuery(sb.toString(), param);
    	        
    	        sb = new StringBuilder();
    	        sb.append("select pa from PlateAcquisition as pa " +
    	        		"where pa.plate.id = :plateID"); 
    	        pa = (PlateAcquisition) iQuery.findByQuery(sb.toString(), param);
    	        
    	        j = results.iterator();
    	        wellSampleIds = new ArrayList<Long>();
    	        imageIds = new ArrayList<Long>();
    	        while (j.hasNext()) {
    				well = (Well) j.next();
    				r = createSharableAnnotation(well);
    				if (r.size() > 0) annotationIds.addAll(r);
    				for (int f = 0; f < well.sizeOfWellSamples(); f++) {
    					field = well.getWellSample(f);
    					r = createSharableAnnotation(field);
    					if (r.size() > 0) annotationIds.addAll(r);
    					wellSampleIds.add(field.getId().getValue());
    					assertNotNull(field.getImage());
    					imageIds.add(field.getImage().getId().getValue());
    				}
    			}
    	        if (pa != null && b) {
    	        	r = createNonSharableAnnotation(pa);
    				if (r.size() > 0) annotationIds.addAll(r);
    	        }
    	        //Now delete the plate
    	        iDelete.deletePlate(p.getId().getValue());
    	        
    	        param = new ParametersI();
    	        param.addId(p.getId().getValue());
    	        sb = new StringBuilder();
    	        //check the plate
    	        sb.append("select p from Plate as p where p.id = :id");
    	        assertNull(iQuery.findByQuery(sb.toString(), param));
    	        
    	        //check the well
    	        param = new ParametersI();
    	        param.addLong("plateID", p.getId().getValue());
    	        sb = new StringBuilder();
    			sb.append("select well from Well as well ");
    			sb.append("left outer join fetch well.plate as pt ");
    			sb.append("where pt.id = :plateID");
    			results = iQuery.findAllByQuery(sb.toString(), param);
    	        assertTrue(results.size() == 0);
    	        
    	        //check the well samples.
    	        sb = new StringBuilder();
    	        param = new ParametersI();
    	        param.addIds(wellSampleIds);
    	        sb.append("select p from WellSample as p where p.id in (:ids)");
    	        results = iQuery.findAllByQuery(sb.toString(), param);
    	        assertTrue(results.size() == 0);
    	        
    	        //check the image.
    	        sb = new StringBuilder();
    	        param = new ParametersI();
    	        param.addIds(imageIds);
    	        sb.append("select p from Image as p where p.id in (:ids)");
    	        results = iQuery.findAllByQuery(sb.toString(), param);
    	        assertTrue(results.size() == 0);
    	        if (pa != null && b) {
    	        	param = new ParametersI();
    		        param.addId(pa.getId().getValue());
    		        sb = new StringBuilder();
    		        //check the plate
    		        sb.append("select p from PlateAcquisition as p " +
    		        		"where p.id = :id");
    		        assertNull(iQuery.findByQuery(sb.toString(), param));
    	        }

    	        //Check if annotations have been deleted.
    	        param = new ParametersI();
    	    	param.addIds(annotationIds);
    	    	assertTrue(annotationIds.size() > 0);
    	    	sb = new StringBuilder();
    	    	sb.append("select i from Annotation as i where i.id in (:ids)");
    	    	l = iQuery.findAllByQuery(sb.toString(), param);
    	    	if (annotations[k]) {
    	    		assertTrue(l.size() == annotationIds.size());
    	    	} else {
    	    		assertTrue(l.size() == 0);
    	    	}
		}
	}
    }

    /**
     * Tests to delete a dataset with images.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCascadingDeleteDatasetAsRoot() 
    	throws Exception
    {
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
			mmFactory.simpleDatasetData().asIObject());
	Image img1 = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
	Image img2 = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
    	DatasetImageLink l = new DatasetImageLinkI();
    	l.link(new DatasetI(d.getId().getValue(), false), img1);
    	iUpdate.saveAndReturnObject(l);
    	l = new DatasetImageLinkI();
    	l.link(new DatasetI(d.getId().getValue(), false), img2);
    	iUpdate.saveAndReturnObject(l);
    	

	DeleteCommand dc = new DeleteCommand("/Dataset", d.getId().getValue(), null);
	delete(dc);


    	ParametersI param = new ParametersI();
    	param.addId(d.getId().getValue());
	String sql = "select d from Dataset d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(img1.getId().getValue());
    	ids.add(img2.getId().getValue());
    	param = new ParametersI();
    	param.addIds(ids);
	sql = "select i from Image i where i.id in (:ids)";
	assertEquals(0, iQuery.findAllByQuery(sql, param).size());
    }
    
    /**
     * Tests to delete a project with dataset and images.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCascadingDeleteProjectAsRoot() 
    	throws Exception
    {
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
	        mmFactory.simpleDatasetData().asIObject());
    	Dataset d2 = (Dataset) iUpdate.saveAndReturnObject(
	        mmFactory.simpleDatasetData().asIObject());
	Image img1 = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
	Image img2 = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
    	DatasetImageLink l = new DatasetImageLinkI();
    	l.link(new DatasetI(d.getId().getValue(), false), img1);
    	iUpdate.saveAndReturnObject(l);
    	l = new DatasetImageLinkI();
    	l.link(new DatasetI(d.getId().getValue(), false), img2);
    	iUpdate.saveAndReturnObject(l);
    	
    	Project p = (Project) iUpdate.saveAndReturnObject(
	        mmFactory.simpleProjectData().asIObject());
    	ProjectDatasetLink pl = new ProjectDatasetLinkI();
    	pl.link(new ProjectI(p.getId().getValue(), false), 
    			new DatasetI(d.getId().getValue(), false));
    	
    	iUpdate.saveAndReturnObject(pl);
    	pl = new ProjectDatasetLinkI();
    	pl.link(new ProjectI(p.getId().getValue(), false), d2);
    	iUpdate.saveAndReturnObject(pl);
    	
	DeleteCommand dc = new DeleteCommand("/Project", p.getId().getValue(), null);
	delete(dc);

    	ParametersI param = new ParametersI();
    	param.addId(p.getId().getValue());
	String sql = "select p from Project p where p.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(d.getId().getValue());
    	ids.add(d2.getId().getValue());
    	param = new ParametersI();
    	param.addIds(ids);
	sql = "select d from Dataset d where d.id in (:ids)";
    	assertTrue(iQuery.findAllByQuery(sql, param).size() == 0);
    	
    	
    	ids.clear();
    	ids.add(img1.getId().getValue());
    	ids.add(img2.getId().getValue());
    	param = new ParametersI();
    	param.addIds(ids);
	sql = "select i from Image i where i.id in (:ids)";
    	assertTrue(iQuery.findAllByQuery(sql, param).size() == 0);
    }
    
    /**
     * Tests to delete a dataset with images.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = false)
    public void testCascadingDeleteScreenAsRoot() 
    	throws Exception
    {
    	Boolean[] values = {Boolean.valueOf(false), Boolean.valueOf(true)};
    	Plate plate;
    	String sql;
    	PlateAcquisition pa = null;
    	ParametersI param;
    	Screen screen;
    	ScreenPlateLink link;
    	for (int i = 0; i < values.length; i++) {
    		param = new ParametersI();
			plate = mmFactory.createPlate(1, 1, 1, values[i], false);
			plate = (Plate) iUpdate.saveAndReturnObject(plate);
			sql = "select pa from PlateAcquisition as pa " +
    		 "where pa.plate.id = :plateID"; 
			param.addLong("plateID", plate.getId().getValue());
			pa = (PlateAcquisition) iQuery.findByQuery(sql, param);
			screen = (Screen) iUpdate.saveAndReturnObject(
			        mmFactory.simpleScreenData().asIObject());
			link = new ScreenPlateLinkI();
			link.link(screen, plate);
			iUpdate.saveAndReturnObject(link);


			DeleteCommand dc = new DeleteCommand("/Screen", screen.getId().getValue(), null);
			delete(dc);


			param = new ParametersI();
			sql = "select s from Screen as s where s.id = :id";
			param.addId(screen.getId().getValue());
			assertNull(iQuery.findByQuery(sql, param));
			param = new ParametersI();
			sql = "select s from Plate as as where s.id = :id";
			param.addId(plate.getId().getValue());
			assertNull(iQuery.findByQuery(sql, param));
			if (values[i] && pa != null) {
				param = new ParametersI();
				sql = "select pa from PlateAcquisition as pa " +
	    		 "where pa.plate.id = :plateID"; 
				param.addId(plate.getId().getValue());
				assertNull(iQuery.findByQuery(sql, param));
			}
		}
    }

}
