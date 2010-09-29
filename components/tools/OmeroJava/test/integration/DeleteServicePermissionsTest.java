/*
 * integration.DeleteServicePermissionsTest 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package integration;


//Java imports

//Third-party libraries
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import omero.ApiUsageException;
import omero.ServerError;
import omero.api.IAdminPrx;
import omero.api.IDeletePrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.api.delete.DeleteCommand;
import omero.api.delete.DeleteHandlePrx;
import omero.grid.DeleteCallbackI;
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

    /** Helper reference to the <code>IDelete</code> service. */
    private IDeletePrx iDelete;

    /**
     * Basic asynchronous delete command. Used in order to reduce the number
     * of places that we do the same thing in case the API changes.
     * 
     * @param dc The command to handle.
     * @throws ApiUsageException
     * @throws ServerError
     * @throws InterruptedException
     */
    private String delete(IDeletePrx proxy, omero.client client, 
    		DeleteCommand...dc)
    	throws ApiUsageException, ServerError,
        InterruptedException
    {
        DeleteHandlePrx handle = proxy.queueDelete(dc);
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
        return report;
    }
   
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
        iDelete = factory.getDeleteService();
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
     * Test to try to delete a image owned by another user in a private group
     * i.e. RW----
     * @throws Exception Thrown if an error occurred.     
     */
    @Test
    public void testDeleteObjectOwnedByOtherRW()
    	throws Exception
    {
    	IAdminPrx svc = root.getSession().getAdminService();
		IQueryPrx query = root.getSession().getQueryService();
    	String uuid1 = UUID.randomUUID().toString();
    	long id = factory.getAdminService().getEventContext().groupId;
    	Experimenter user = new ExperimenterI();
    	user.setOmeName(omero.rtypes.rstring(uuid1));
    	user.setFirstName(omero.rtypes.rstring("user"));
    	user.setLastName(omero.rtypes.rstring("user")); 
		ParametersI param = new ParametersI();
		param.addId(id);
		ExperimenterGroup group = (ExperimenterGroup) query.findByQuery(
				"select distinct g from ExperimenterGroup g where g.id = :id", 
				param);
		List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
		ExperimenterGroup userGroup = svc.lookupGroup(USER_GROUP);
		groups.add(group);
		groups.add(userGroup);
		long userID = svc.createExperimenter(user, group, groups);
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
        ServiceFactoryPrx f = newClient.createSession(uuid1, uuid1); 
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
        	param = new ParametersI();
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
        	newClient.__del__();
		} catch (Exception e) {
			throw e;
		}
    }
    
    /**
     * Test to try to delete a image owned by another user in a private group
     * i.e. RWR---
     * @throws Exception Thrown if an error occurred.     
     */
    @Test
    public void testDeleteObjectOwnedByOtherRWR()
    	throws Exception
    {
    	IAdminPrx svc = root.getSession().getAdminService();
		IQueryPrx query = root.getSession().getQueryService();
		// set up collaborative group with 2 users
        String uuid1 = UUID.randomUUID().toString();
		ExperimenterGroup group = new ExperimenterGroupI();
		group.setName(omero.rtypes.rstring(uuid1));
		group.getDetails().setPermissions(new PermissionsI("rwr---"));
		long id1 = svc.createGroup(group);
		ParametersI p = new ParametersI();
		p.addId(id1);
		group = (ExperimenterGroup) query.findByQuery(
				"select distinct g from ExperimenterGroup g where g.id = :id", 
				p);
		 
		Experimenter owner = new ExperimenterI();
		owner.setOmeName(omero.rtypes.rstring(uuid1));
		owner.setFirstName(omero.rtypes.rstring("owner"));
		owner.setLastName(omero.rtypes.rstring("owner")); 
		 
		Experimenter other = new ExperimenterI();
		other.setOmeName(omero.rtypes.rstring(uuid1+"other"));
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
        ServiceFactoryPrx f = newClient.createSession(uuid1, uuid1); 
        IUpdatePrx update = f.getUpdateService();
        Image img = (Image) update.saveAndReturnObject(
        		mmFactory.simpleImage(0));
        long imageID = img.getId().getValue();
        
        newClient.__del__();
        newClient = new omero.client();
        f = newClient.createSession(uuid1+"other", uuid1); 
        IDeletePrx delete = f.getDeleteService();
        try {
        	
        	delete(delete, newClient, new DeleteCommand(
        			DeleteServiceTest.REF_IMAGE, imageID, null));
        	p = new ParametersI();
        	p.addId(imageID);
        	assertNotNull(iQuery.findByQuery(
        			"select i from Image i where i.id = :id", p));
		} catch (Exception e) {
			throw e;
		}
    }
    
    
	/**
     * Test to delete an image tagged collaboratively by another user.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = false, groups = "ticket:2881")
    public void testDeleteTaggedImageTagOwnedByOther() 
    	throws Exception
    {
        // set up collaborative group with 2 users
        String uuid1 = UUID.randomUUID().toString();
		ExperimenterGroup g1 = new ExperimenterGroupI();
		g1.setName(omero.rtypes.rstring(uuid1));
		g1.getDetails().setPermissions(new PermissionsI("rwrw--"));
		IAdminPrx svc = root.getSession().getAdminService();
		IQueryPrx query = root.getSession().getQueryService();
		long id1 = svc.createGroup(g1);
		ParametersI p = new ParametersI();
		p.addId(id1);
		ExperimenterGroup eg1 = (ExperimenterGroup) query.findByQuery(
				"select distinct g from ExperimenterGroup g where g.id = :id", 
				p);
		 
		Experimenter owner = new ExperimenterI();
		owner.setOmeName(omero.rtypes.rstring(uuid1 + "owner"));
		owner.setFirstName(omero.rtypes.rstring("owner"));
		owner.setLastName(omero.rtypes.rstring("owner")); 
		 
		Experimenter tagger = new ExperimenterI();
		tagger.setOmeName(omero.rtypes.rstring(uuid1 + "tagger"));
		tagger.setFirstName(omero.rtypes.rstring("tagger"));
		tagger.setLastName(omero.rtypes.rstring("tagger"));
		 
		List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
		ExperimenterGroup userGroup = svc.lookupGroup(USER_GROUP);
		groups.add(eg1);
		groups.add(userGroup);
		
		svc.createExperimenter(owner, eg1, groups);
		svc.createExperimenter(tagger, eg1, groups);
    		
    	omero.client client = new omero.client();
    	// owner creates the image
        ServiceFactoryPrx f = client.createSession(uuid1 + "owner", uuid1); 
        IUpdatePrx update = f.getUpdateService();
        Image img = (Image) update.saveAndReturnObject(
        		mmFactory.simpleImage(0));
        client.__del__();
        // tagger creates tag and tags the image
        omero.client clientTag = new omero.client();
        ServiceFactoryPrx sf = clientTag.createSession(uuid1 + "tagger", uuid1);
        IUpdatePrx tagUpdate = sf.getUpdateService();
        
        TagAnnotation c = new TagAnnotationI();
    	c.setTextValue(omero.rtypes.rstring("tag"));
    	c = (TagAnnotation) tagUpdate.saveAndReturnObject(c);
    	ImageAnnotationLink link = new ImageAnnotationLinkI();
    	link.setParent(img);
    	link.setChild(new TagAnnotationI(c.getId().getValue(), false));		
    	link = (ImageAnnotationLink) tagUpdate.saveAndReturnObject(link);
    	
    	// try to delete image. 
    	long id = img.getId().getValue();
    	delete(iDelete, client, 
    			new DeleteCommand(DeleteServiceTest.REF_IMAGE, id, null));
        // check it has been deleted 
        ParametersI param = new ParametersI();
    	param.addId(id);
    	img = (Image) iQuery.findByQuery(
    			"select i from Image i where i.id = :id", param);
    	assertNull(img);
    	
    	// check that the tag hasn't been deleted
    	param = new ParametersI();
    	param.addId(c.getId().getValue());
    	c = (TagAnnotation) iQuery.findByQuery(
    			"select c from Annotation c where c.id = :id", param);
    	assertNotNull(c);
    	clientTag.__del__();
    }
    
}
