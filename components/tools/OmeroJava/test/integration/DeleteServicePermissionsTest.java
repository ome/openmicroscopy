/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;


//Java imports

//Third-party libraries
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import omero.api.IAdminPrx;
import omero.api.IDeletePrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.api.delete.DeleteCommand;
import omero.model.Dataset;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.PermissionsI;
import omero.model.Plate;
import omero.model.Project;
import omero.model.Screen;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.sys.ParametersI;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

//Application-internal dependencies

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
public class DeleteServicePermissionsTest 
	extends AbstractTest
{

    /**
     * Since so many tests rely on counting the number of objects present
     * globally, we're going to start each method with a new user in 
     * <code>private</code> group.
     */
    @BeforeMethod
    public void createNewUser() 
    	throws Exception
    {
        newUserAndGroup("rw----");
    }

    /**
     * Since we are creating a new client on each invocation, we should also
     * clean it up. Note: {@link #newUserAndGroup(String)} also closes, but
     * not the very last invocation.
     */
    @AfterMethod
    public void close() 
    	throws Exception
    {
        if (client != null) {
            client.__del__();
            client = null;
        }
    }
    
    /**
     * Test to try to delete an (top) object owned by another user in a 
     * private group i.e. RW----.
     * @throws Exception Thrown if an error occurred.     
     */
    @Test
    public void testDeleteObjectOwnedByOtherRW()
    	throws Exception
    {
    	IAdminPrx svc = root.getSession().getAdminService();
    	String uuid = UUID.randomUUID().toString();
    	String name = factory.getAdminService().getEventContext().groupName;
    	Experimenter user = new ExperimenterI();
    	user.setOmeName(omero.rtypes.rstring(uuid));
    	user.setFirstName(omero.rtypes.rstring("user"));
    	user.setLastName(omero.rtypes.rstring("user")); 
		ExperimenterGroup group = svc.lookupGroup(name);
		List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
		ExperimenterGroup userGroup = svc.lookupGroup(USER_GROUP);
		groups.add(group);
		groups.add(userGroup);
		
		svc.createExperimenter(user, group, groups);
		
		//Image
		Image img = (Image) iUpdate.saveAndReturnObject(
				mmFactory.createImage());
    	//Dataset
		Dataset d = (Dataset) iUpdate.saveAndReturnObject(
				mmFactory.simpleDatasetData().asDataset());
		
		//Project
		Project p = (Project) iUpdate.saveAndReturnObject(
				mmFactory.simpleProjectData().asProject());
		
		//Dataset
		Screen s = (Screen) iUpdate.saveAndReturnObject(
				mmFactory.simpleScreenData().asScreen());
		
		//Dataset
		Plate plate = (Plate) iUpdate.saveAndReturnObject(
				mmFactory.simplePlateData().asPlate());
    	
    	omero.client newClient = new omero.client();
    	// owner creates the image
        ServiceFactoryPrx f = newClient.createSession(uuid, uuid); 
        IDeletePrx delete = f.getDeleteService();
        try {
        	DeleteCommand[] dcs = new DeleteCommand[5];
        	dcs[0] = new DeleteCommand(DeleteServiceTest.REF_IMAGE, 
					img.getId().getValue(), null);
        	dcs[1] = new DeleteCommand(DeleteServiceTest.REF_DATASET, 
					d.getId().getValue(), null);
        	dcs[2] = new DeleteCommand(DeleteServiceTest.REF_PROJECT, 
					p.getId().getValue(), null);
        	dcs[3] = new DeleteCommand(DeleteServiceTest.REF_SCREEN, 
					s.getId().getValue(), null);
        	dcs[4] = new DeleteCommand(DeleteServiceTest.REF_PLATE, 
					plate.getId().getValue(), null);
        	delete(delete, newClient, dcs);
        	newClient.__del__();
        	
        	ParametersI param = new ParametersI();
        	param.addId(img.getId().getValue());
        	assertNotNull(iQuery.findByQuery(
        			"select i from Image i where i.id = :id", param));
        	param = new ParametersI();
        	param.addId(d.getId().getValue());
        	assertNotNull(iQuery.findByQuery(
        			"select i from Dataset i where i.id = :id", param));
        	param = new ParametersI();
        	param.addId(p.getId().getValue());
        	assertNotNull(iQuery.findByQuery(
        			"select i from Project i where i.id = :id", param));
        	param = new ParametersI();
        	param.addId(s.getId().getValue());
        	assertNotNull(iQuery.findByQuery(
        			"select i from Screen i where i.id = :id", param));
        	param = new ParametersI();
        	param.addId(plate.getId().getValue());
        	assertNotNull(iQuery.findByQuery(
        			"select i from Plate i where i.id = :id", param));
        	
		} catch (Exception e) {
			throw e;
		}
    }
    
    /**
     * Test to try to delete an image owned by another user in a collaborative
     * group i.e. RWR---
     * @throws Exception Thrown if an error occurred.     
     */
    @Test(enabled = false)
    public void testDeleteImageOwnedByOtherRWR()
    	throws Exception
    {
    	IAdminPrx svc = root.getSession().getAdminService();
		// set up collaborative group with 2 users
        String uuid = UUID.randomUUID().toString();
		ExperimenterGroup group = new ExperimenterGroupI();
		group.setName(omero.rtypes.rstring(uuid));
		group.getDetails().setPermissions(new PermissionsI("rwr---"));
		svc.createGroup(group);
		group = svc.lookupGroup(uuid);
		 
		Experimenter owner = new ExperimenterI();
		owner.setOmeName(omero.rtypes.rstring(uuid));
		owner.setFirstName(omero.rtypes.rstring("owner"));
		owner.setLastName(omero.rtypes.rstring("owner")); 
		 
		Experimenter other = new ExperimenterI();
		other.setOmeName(omero.rtypes.rstring(uuid+"other"));
		other.setFirstName(omero.rtypes.rstring("other"));
		other.setLastName(omero.rtypes.rstring("other"));
		
		List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
		ExperimenterGroup userGroup = svc.lookupGroup(USER_GROUP);
		groups.add(group);
		groups.add(userGroup);
		
		svc.createExperimenter(owner, group, groups);
		svc.createExperimenter(other, group, groups);

		omero.client newClient = new omero.client();
    	// owner creates the image
        ServiceFactoryPrx f = newClient.createSession(uuid, uuid); 
        IUpdatePrx update = f.getUpdateService();
        ModelMockFactory mmFactory = new ModelMockFactory(f.getPixelsService());
        Image img = (Image) update.saveAndReturnObject(
        		mmFactory.simpleImage(0));
        long imageID = img.getId().getValue();
        
        
        omero.client otherClient = new omero.client();
        ServiceFactoryPrx of = otherClient.createSession(uuid+"other", uuid); 
        IDeletePrx delete = of.getDeleteService();
        try {
        	delete(delete, otherClient, new DeleteCommand(
        			DeleteServiceTest.REF_IMAGE, imageID, null));

        	ParametersI p = new ParametersI();
        	p.addId(imageID);
        	assertNotNull(f.getQueryService().findByQuery(
        			"select i from Image i where i.id = :id", p));
        	newClient.__del__();
        	otherClient.__del__();
		} catch (Exception e) {
			throw e;
		}
    }

    /**
     * Test to try to delete an object by the administrator in a private group
     * i.e. RW----
     * @throws Exception Thrown if an error occurred.     
     */
    @Test(enabled = false)
    public void testDeleteObjectByAdmin()
    	throws Exception
    {
    	//Image
		Image img = (Image) iUpdate.saveAndReturnObject(
				mmFactory.createImage());
		IDeletePrx delete = root.getSession().getDeleteService();
		delete(delete, root, new DeleteCommand(
    			DeleteServiceTest.REF_IMAGE, img.getId().getValue(), null));
		ParametersI p = new ParametersI();
    	p.addId(img.getId().getValue());
    	assertNull(iQuery.findByQuery(
    			"select i from Image i where i.id = :id", p));
    }

    /**
     * Test to try to delete an object by the owner of a private group
     * i.e. RW----
     * @throws Exception Thrown if an error occurred.     
     */
    @Test(enabled = false)
    public void testDeleteObjectByGroupOwner()
    	throws Exception
    {
    	IAdminPrx svc = root.getSession().getAdminService();
    	String uuid = UUID.randomUUID().toString();
    	
    	String name = factory.getAdminService().getEventContext().groupName;
    	//add a new user to the group
    	Experimenter user = new ExperimenterI();
    	user.setOmeName(omero.rtypes.rstring(uuid));
    	user.setFirstName(omero.rtypes.rstring("user"));
    	user.setLastName(omero.rtypes.rstring("user")); 
		ExperimenterGroup group = svc.lookupGroup(name);
		List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
		ExperimenterGroup userGroup = svc.lookupGroup(USER_GROUP);
		groups.add(group);
		groups.add(userGroup);
		svc.createExperimenter(user, group, groups);
    	user = svc.lookupExperimenter(uuid);
    	svc.setGroupOwner(userGroup, user);

    	//owner creates the image
		Image img = (Image) iUpdate.saveAndReturnObject(
				mmFactory.createImage());
		omero.client newClient = new omero.client();
		
    	//group owner deletes it
        ServiceFactoryPrx f = newClient.createSession(uuid, name); 
		IDeletePrx delete = f.getDeleteService();
		delete(delete, newClient, new DeleteCommand(
    			DeleteServiceTest.REF_IMAGE, img.getId().getValue(), null));
		newClient.__del__();
		//Image should be deleted.
		ParametersI p = new ParametersI();
    	p.addId(img.getId().getValue());
    	assertNull(iQuery.findByQuery(
    			"select i from Image i where i.id = :id", p));
    }
    
    /**
     * Test to try to delete an object by the administrator in a read-only
     * collaborative group i.e. RWR---
     * @throws Exception Thrown if an error occurred.     
     */
    @Test(enabled = false)
    public void testDeleteObjectByAdminRWR()
    	throws Exception
    {
    	IAdminPrx svc = root.getSession().getAdminService();
		// set up collaborative group with 2 users
        String uuid = UUID.randomUUID().toString();
		ExperimenterGroup group = new ExperimenterGroupI();
		group.setName(omero.rtypes.rstring(uuid));
		group.getDetails().setPermissions(new PermissionsI("rwr---"));
		svc.createGroup(group);

		group = svc.lookupGroup(uuid);
		Experimenter user = new ExperimenterI();
    	user.setOmeName(omero.rtypes.rstring(uuid));
    	user.setFirstName(omero.rtypes.rstring("user"));
    	user.setLastName(omero.rtypes.rstring("user")); 
		group = svc.lookupGroup(uuid);
		List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
		ExperimenterGroup userGroup = svc.lookupGroup(USER_GROUP);
		groups.add(group);
		groups.add(userGroup);
		svc.createExperimenter(user, group, groups);
		
		omero.client newClient = new omero.client();
    	// owner creates the image
        ServiceFactoryPrx f = newClient.createSession(uuid, uuid); 
        ModelMockFactory mmFactory = new ModelMockFactory(f.getPixelsService());
    	//Image
		Image img = (Image) f.getUpdateService().saveAndReturnObject(
				mmFactory.createImage());
		
		//admin delete the object.
		IDeletePrx delete = root.getSession().getDeleteService();
		delete(delete, root, new DeleteCommand(
    			DeleteServiceTest.REF_IMAGE, img.getId().getValue(), null));
		ParametersI p = new ParametersI();
    	p.addId(img.getId().getValue());
    	//it should be deleted
    	assertNull(f.getQueryService().findByQuery(
    			"select i from Image i where i.id = :id", p));
    	newClient.__del__();
    }
    
	/**
     * Test to delete an image tagged collaboratively by another user.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = false, groups = {"ticket:2881"})
    public void testDeleteTaggedImageTagOwnedByOther() 
    	throws Exception
    {
        // set up collaborative group with 2 users
        String uuid = UUID.randomUUID().toString();
		ExperimenterGroup g1 = new ExperimenterGroupI();
		g1.setName(omero.rtypes.rstring(uuid));
		g1.getDetails().setPermissions(new PermissionsI("rwrw--"));
		IAdminPrx svc = root.getSession().getAdminService();
		svc.createGroup(g1);
		ExperimenterGroup eg1 = svc.lookupGroup(uuid);
		 
		Experimenter owner = new ExperimenterI();
		owner.setOmeName(omero.rtypes.rstring(uuid + "owner"));
		owner.setFirstName(omero.rtypes.rstring("owner"));
		owner.setLastName(omero.rtypes.rstring("owner")); 
		 
		Experimenter tagger = new ExperimenterI();
		tagger.setOmeName(omero.rtypes.rstring(uuid + "tagger"));
		tagger.setFirstName(omero.rtypes.rstring("tagger"));
		tagger.setLastName(omero.rtypes.rstring("tagger"));
		 
		List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
		ExperimenterGroup userGroup = svc.lookupGroup(USER_GROUP);
		groups.add(eg1);
		groups.add(userGroup);
		
		svc.createExperimenter(owner, eg1, groups);
		svc.createExperimenter(tagger, eg1, groups);
    		
    	omero.client newClient = new omero.client();
    	// owner creates the image
        ServiceFactoryPrx f = newClient.createSession(uuid + "owner", uuid); 
        IUpdatePrx update = f.getUpdateService();
        
        ModelMockFactory mmFactory = new ModelMockFactory(f.getPixelsService());
        Image img = (Image) update.saveAndReturnObject(
        		mmFactory.simpleImage(0));
        
        // tagger creates tag and tags the image
        omero.client clientTag = new omero.client();
        ServiceFactoryPrx sf = clientTag.createSession(uuid + "tagger", uuid);
        IUpdatePrx tagUpdate = sf.getUpdateService();
        
        TagAnnotation c = new TagAnnotationI();
    	c.setTextValue(omero.rtypes.rstring("tag"));
    	c = (TagAnnotation) tagUpdate.saveAndReturnObject(c);
    	ImageAnnotationLink link = new ImageAnnotationLinkI();
    	link.setParent(img);
    	link.setChild(new TagAnnotationI(c.getId().getValue(), false));		
    	link = (ImageAnnotationLink) tagUpdate.saveAndReturnObject(link);
    	
    	
    	
    	// owner tries to delete image. 
    	long id = img.getId().getValue();
    	delete(f.getDeleteService(), newClient, 
    			new DeleteCommand(DeleteServiceTest.REF_IMAGE, id, null));
        // check it has been deleted 
        ParametersI param = new ParametersI();
    	param.addId(id);
    	img = (Image) f.getQueryService().findByQuery(
    			"select i from Image i where i.id = :id", param);
    	assertNull(img);
    	
    	// check that the tag hasn't been deleted
    	param = new ParametersI();
    	param.addId(c.getId().getValue());
    	c = (TagAnnotation) f.getQueryService().findByQuery(
    			"select c from Annotation c where c.id = :id", param);
    	assertNotNull(c);
    	
    	clientTag.__del__(); //tagger 
    	newClient.__del__();
    	
    }
    
}
