package ome.client.itests;

import org.testng.annotations.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import ome.api.IAdmin;
import ome.api.IPojos;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.model.IObject;
import ome.model.annotations.DatasetAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.core.PlaneInfo;
import ome.model.internal.Permissions;
import ome.parameters.Parameters;
import ome.system.EventContext;
import ome.system.ServiceFactory;
import ome.testing.ObjectFactory;
import ome.util.builders.PojoOptions;
import pojos.ImageData;

@Test( 
	groups = {"client","integration"} 
)
public class TicketsUpTo500Test extends TestCase
{

    ServiceFactory sf = new ServiceFactory();
    IUpdate iUpdate   = sf.getUpdateService();
    IQuery iQuery     = sf.getQueryService();
    IAdmin iAdmin     = sf.getAdminService();

    // ~ Ticket 168
    // =========================================================================
    
    @Test( groups = "ticket:168")
    public void test_planeInfoSetPixelsSavePixels() throws Exception
    {
        Pixels pixels = ObjectFactory.createPixelGraph(null);
        pixels.clearPlaneInfo();
        PlaneInfo planeInfo = createPlaneInfo();
        planeInfo.setPixels(pixels);
        pixels = (Pixels) iUpdate.saveAndReturnObject(pixels);
        PlaneInfo test = (PlaneInfo)
        iQuery.findByQuery( "select pi from PlaneInfo pi " +
                "where pi.pixels.id = :id",new Parameters().addId(pixels.getId()));
        // Null because saving the pixels rather than planeinfo does not work.
        assertNull( test );
    }
    
    @Test( groups = "ticket:168")
    public void test_planeInfoSetPixelsSavePlaneInfo() throws Exception
    {
        Pixels pixels = ObjectFactory.createPixelGraph(null);
        pixels.clearPlaneInfo();
        PlaneInfo planeInfo = createPlaneInfo();
        planeInfo.setPixels(pixels);
        planeInfo = (PlaneInfo) iUpdate.saveAndReturnObject(planeInfo);
        Pixels test = (Pixels)
        iQuery.findByQuery( "select p from Pixels p " +
                "where p.planeInfo.id = :id",new Parameters().addId(planeInfo.getId()));
        assertNotNull( test );
    }

    @Test( groups = "ticket:168")
    public void test_pixelsAddToPlaneInfoSavePixels() throws Exception
    {
        IUpdate iUpdate = sf.getUpdateService();
        Pixels pixels = ObjectFactory.createPixelGraph(null);
        pixels.clearPlaneInfo();
        PlaneInfo planeInfo = createPlaneInfo();
        pixels.addPlaneInfo(planeInfo);
        pixels = (Pixels) iUpdate.saveAndReturnObject(pixels);
        PlaneInfo test = (PlaneInfo)
        iQuery.findByQuery( "select pi from PlaneInfo pi " +
                "where pi.pixels.id = :id",new Parameters().addId(pixels.getId()));
        assertNotNull( test );
    }
    
    @Test( groups = {"ticket:221"} )
    public void testGetImagesReturnsNoNulls() throws Exception {
  	  	Dataset d = new Dataset();
  	  	d.setName("ticket:221");
  	  	Image i = new Image();
  	  	i.setName("ticket:221");
  	  	Pixels p = ObjectFactory.createPixelGraph(null);
  	  	p.setDefaultPixels( Boolean.TRUE );
  	  	i.addPixels(p);
  	  	d.linkImage(i);
  	  	d = iUpdate.saveAndReturnObject(d);
  	  	
  	  	Set<Image> set = 
  	  	sf.getPojosService().getImages(
  	  			Dataset.class, 
  	  			Collections.singleton(
  	  					d.getId()), 
  	  					null);
  	  	Image img = set.iterator().next();
  	  	ImageData test = new ImageData( img );
  	  	assertNotNull(test);
  		assertNotNull(test.getDefaultPixels());
  		assertNotNull(test.getDefaultPixels().getPixelSizeX());
  		assertNotNull(test.getDefaultPixels().getPixelSizeY());
  		assertNotNull(test.getDefaultPixels().getPixelSizeZ());
  		assertNotNull(test.getDefaultPixels().getPixelType());
  		assertNotNull(test.getDefaultPixels().getImage());
  		assertNotNull(test.getDefaultPixels().getOwner());
  		assertNotNull(test.getDefaultPixels().getSizeC());
  		assertNotNull(test.getDefaultPixels().getSizeT());
  		assertNotNull(test.getDefaultPixels().getSizeZ());
  		assertNotNull(test.getDefaultPixels().getSizeY());
  		assertNotNull(test.getDefaultPixels().getSizeX());
  	}
    
    @Test( groups = {"ticket:293"})
    public void testChangingUnreadablePermissions() throws Exception {
		Project p = new Project();
		p.setName("ticket:293");
		p = iUpdate.saveAndReturnObject(p);
		iAdmin.changePermissions(p, Permissions.EMPTY);
		iAdmin.changePermissions(p, Permissions.DEFAULT);
	}
    
    @Test( groups = {"ticket:376"} )
    public void testLeavesFunctional() throws Exception {
    	
    	Long id = iAdmin.getEventContext().getCurrentUserId();

  	  	PojoOptions leaves = new PojoOptions().exp(id).leaves();
  	  	PojoOptions noleaves = new PojoOptions().exp(id).noLeaves();
    	
  	  	Project p = new Project();
  	  	p.setName("ticket:376");
    	Dataset d = new Dataset();
  	  	d.setName("ticket:376");
  	  	Image i = new Image();
  	  	i.setName("ticket:376");
  	  	p.linkDataset(d);
  	  	d.linkImage(i);
  	  	p = iUpdate.saveAndReturnObject(p);
  	  	d = (Dataset) p.linkedDatasetList().get(0);
  	  	
  	  	boolean found = false;
  	  	
  	  	// Project --------------------------------------
		Set<Long> ids = new HashSet<Long>( Arrays.asList(p.getId()) ); 

		// with leaves & ids
  	  	Set<Project> set = 
  	  	sf.getPojosService().loadContainerHierarchy(
  	  			Project.class, ids, leaves.map());
  	  	
  	  	Project tP = set.iterator().next();
  	  	Dataset tD = (Dataset) tP.linkedDatasetList().get(0);
  	  	assertTrue( tD.sizeOfImageLinks() > 0 );

  	  	// with ids & no leaves
  	  	set = 
  	  	sf.getPojosService().loadContainerHierarchy(
  	  	  		Project.class, ids, noleaves.map());
  	  	  	
  	  	tP = set.iterator().next();
  	  	tD = (Dataset) tP.linkedDatasetList().get(0);
  	  	assertTrue( tD.sizeOfImageLinks() < 0 );

  	  	// with no ids & no leaves
  	  	set = 
  	  	sf.getPojosService().loadContainerHierarchy(
  	  	  		Project.class, null, noleaves.map());
  	  	  	
  	  	found = false;
  	  	for (Project project : set) {
  	  		if (!project.getId().equals(p.getId())) continue;
  	  	  	tP = project;
  	  	  	tD = (Dataset) tP.linkedDatasetList().get(0);
  	  	  	assertTrue( tD.sizeOfImageLinks() < 0 );
  	  	  	found = true;
		}
  	  	if (!found) fail(" prj not found (no ids/no leaves)");

  	  	// with no ids but leaves
  	  	set = 
  	  	sf.getPojosService().loadContainerHierarchy(
  	  	  		Project.class, null, leaves.map());
  	  	  	
  	  	found = false;
  	  	for (Project project : set) {
  	  		if (!project.getId().equals(p.getId())) continue;
  	  	  	tP = project;
  	  	  	tD = (Dataset) tP.linkedDatasetList().get(0);
  	  	  	assertTrue( tD.sizeOfImageLinks() > 0 );
  	  	  	found = true;
		}
  	  	if (!found) fail(" prj not found (no ids/leaves)");
  	  	
  	  	// Dataset --------------------------------------
		ids = new HashSet<Long>( Arrays.asList(d.getId()) ); 

		// with leaves & ids
  	  	Set<Dataset> set2 = 
  	  	sf.getPojosService().loadContainerHierarchy(
  	  			Dataset.class, ids, leaves.map());
  	  	
  	  	tD = set2.iterator().next();
  	  	assertTrue( tD.sizeOfImageLinks() > 0 );

  	  	// with ids & no leaves
  	  	set2 = 
  	  	sf.getPojosService().loadContainerHierarchy(
  	  	  		Dataset.class, ids, noleaves.map());
  	  	  	
  	  	tD = set2.iterator().next();
  	  	assertTrue( tD.sizeOfImageLinks() < 0 );

  	  	// with no ids & no leaves
  	  	set2 = 
  	  	sf.getPojosService().loadContainerHierarchy(
  	  	  		Dataset.class, null, noleaves.map());
  	  	  	
  	  	found = false;
  	  	for (Dataset dataset : set2) {
  	  		if (!dataset.getId().equals(d.getId())) continue;
  	  	  	tD = dataset;
  	  	  	assertTrue( tD.sizeOfImageLinks() < 0 );
  	  	  	found = true;
		}
  	  	if (!found) fail(" ds not found (no ids/no leaves)");

  	  	// with no ids & no leaves
  	  	set2 = 
  	  	sf.getPojosService().loadContainerHierarchy(
  	  	  		Dataset.class, null, leaves.map());
  	  	  	
  	  	found = false;
  	  	for (Dataset dataset : set2) {
  	  		if (!dataset.getId().equals(d.getId())) continue;
  	  	  	tD = dataset;
  	  	  	assertTrue( tD.sizeOfImageLinks() > 0 );
  	  	  	found = true;
  	  	}
  	  	if (!found) fail(" ds not found (no ids/leaves)");
  	  	
    }

    /** trying to find a null constraint violation of Event.experimenter 
     */
    @SuppressWarnings("unchecked")
    @Test( groups = "ticket:396" )
    public void testAnnotationIsUpdatable() throws Exception {

    	final IPojos pojosService = sf.getPojosService();
    	
    	// create dataset
    	Dataset dataset = new Dataset();
    	dataset.setName( "ticket:396" );
		dataset = pojosService.updateDataObject( dataset, null );

		// load dataset via loadContainerHierarchy
		dataset = pojosService.loadContainerHierarchy(Dataset.class, 
    			new HashSet<Long>( Arrays.asList(dataset.getId())), null).iterator().next();
    	
		// create annotation
    	DatasetAnnotation annotation = new DatasetAnnotation();
    	annotation.setContent( "ticket:396" );
    	annotation.setDataset( dataset );	
    	pojosService.updateDataObject( annotation, null );
    	
    	// load annotation via findAnnotations
    	annotation = ((Set<DatasetAnnotation>) pojosService.findAnnotations(Dataset.class, 
    			new HashSet<Long>( Arrays.asList( dataset.getId() )), null,null).
    			get(dataset.getId())).iterator().next();
    	
    	// update annotation
    	annotation.setContent( annotation.getContent()+":updated");
    	pojosService.updateDataObject( annotation, null );
    
	}
    
    @Test( groups = "ticket:401" )
    public void testLoadContainerReturnsDefaultPixels() throws Exception {
    	PojoOptions 
    	withLeaves = new PojoOptions().leaves();
    	
    	Dataset d = new Dataset();
    	d.setName("ticket:401");
    	Image i = new Image();
    	i.setName("ticket:401");
    	Pixels p = ObjectFactory.createPixelGraph(null);
    	p.setDefaultPixels(Boolean.TRUE);
    	d.linkImage(i);
    	i.addPixels(p);
    	
    	final IPojos pj = sf.getPojosService();
    	
    	d = (Dataset) pj.updateDataObject( d, null );
    	
    	Set<Dataset> ds = (Set<Dataset>)
    	pj.loadContainerHierarchy(Dataset.class, Collections.singleton(d.getId()), 
    			withLeaves.map());
    	    	
		for (Dataset dataset : ds) {
			Image image = (Image) dataset.linkedImageList().get(0);
			assertNotNull(image.getDefaultPixels());
		}
    }
    
    // ~ Helpers
    // =========================================================================
    // TODO refactor to ObjectFactory
    private PlaneInfo createPlaneInfo()
    {
        PlaneInfo planeInfo = new PlaneInfo();
        planeInfo.setTheZ( 1 );
        planeInfo.setTheC( 1 );
        planeInfo.setTheT( 1 );
        planeInfo.setTimestamp( 0F );
        return planeInfo;
    }
        
    
    
  
    
}
