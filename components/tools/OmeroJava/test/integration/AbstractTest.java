/*
 * $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import static omero.rtypes.rstring;
import static omero.rtypes.rtime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import junit.framework.TestCase;
import ome.testing.ObjectFactory;
import omero.OptimisticLockException;
import omero.RInt;
import omero.api.IAdminPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Annotation;
import omero.model.AnnotationAnnotationLinkI;
import omero.model.Channel;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
import omero.model.OriginalFileI;
import omero.model.Permissions;
import omero.model.PermissionsI;
import omero.model.Pixels;
import omero.model.PlaneInfo;
import omero.model.PlaneInfoI;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;
import omero.model.Screen;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.sys.EventContext;
import omero.sys.ParametersI;
import omero.util.IceMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import pojos.DatasetData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;

/**
 * Base test for integration tests.
 *
 * @since Beta4.2
 */
@Test(groups = { "client", "integration", "blitz" })
public class AbstractTest
	extends TestCase
{

	/** Identifies the <code>system</code> group. */
	public String SYSTEM_GROUP = "system";
	
	/** Identifies the <code>user</code> group. */
	public String USER_GROUP = "user";
	
	/** Identifies the <code>guest</code> group. */
	public String GUEST_GROUP = "guest";
	
	/** Holds the error, info, warning. */
    protected Log log = LogFactory.getLog(getClass());

	/** The client object, this is the entry point to the Server. */
    protected omero.client client;

    /** A root-client object. */
    protected omero.client root;

    /** Helper reference to the <code>Service factory</code>. */
    protected ServiceFactoryPrx factory;

    /** Helper reference to the <code>IQuery</code> service. */
    protected IQueryPrx iQuery;

    /** Helper reference to the <code>IUpdate</code> service. */
    protected IUpdatePrx iUpdate;

    /** Helper reference to the <code>IAdmin</code> service. */
    protected IAdminPrx iAdmin;
    
    
    // ~ Helpers
    // =========================================================================
    /**
     * Creates a default image and returns it.
     *
     * @param time The acquisition time.
     * @return See above.
     */
    protected Image simpleImage(long time)
    {
        // prepare data
        Image img = new ImageI();
        img.setName(rstring("image1"));
        img.setDescription(rstring("descriptionImage1"));
        img.setAcquisitionDate(rtime(time));
        return img;
    }
    
    /**
     * Creates a default dataset and returns it.
     * 
     * @return See above.
     */
    protected DatasetData simpleDatasetData()
    {
        DatasetData dd = new DatasetData();
        dd.setName("t1");
        dd.setDescription("t1");
        return dd;
    }
    
    /**
     * Creates a default project and returns it.
     * 
     * @return See above.
     */
    protected ProjectData simpleProjectData()
    {
        ProjectData data = new ProjectData();
        data.setName("project1");
        data.setDescription("project1");
        return data;
    }

    /**
     * Creates a default screen and returns it.
     * 
     * @return See above.
     */
    protected ScreenData simpleScreenData()
    {
    	ScreenData data = new ScreenData();
        data.setName("screen1");
        data.setDescription("screen1");
        return data;
    }
    
    /**
     * Creates a default project and returns it.
     * 
     * @return See above.
     */
    protected PlateData simplePlateData()
    {
    	PlateData data = new PlateData();
        data.setName("plate1");
        data.setDescription("plate1");
        return data;
    }

    /**
     * Creates and returns an original file object.
     * 
     * @return See above.
     */
    protected OriginalFileI createOriginalFile()
    {
    	OriginalFileI oFile = new OriginalFileI();
		oFile.setName(omero.rtypes.rstring("of1"));
		oFile.setPath(omero.rtypes.rstring("/omero"));
		oFile.setSize(omero.rtypes.rlong(0));
		//Need to be modified
		oFile.setSha1(omero.rtypes.rstring("pending"));
		oFile.setMimetype(omero.rtypes.rstring("application/octet-stream"));
		return oFile;
    }
    
	/**
     * Initializes the various services.
     * @throws Exception Thrown if an error occurred.
     */
    @Override
    @BeforeClass
    protected void setUp() 
    	throws Exception
    {
        // administrator client
        omero.client tmp = new omero.client();
        String rootpass = tmp.getProperty("omero.rootpass");
        root = new omero.client();
        root.createSession("root", rootpass);
        tmp.__del__();

        newUserAndGroup("rw----");
    }

    /**
     * Closes the session.
     * @throws Exception Thrown if an error occurred.
     */
    @Override
    @AfterClass
    public void tearDown() 
    	throws Exception
    {
        client.__del__();
        root.__del__();
    }

    /**
     * Creates a new group and experimenter and returns the event context.
     * 
     * @param perms The permissions level.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected EventContext newUserAndGroup(String perms) 
    	throws Exception
    {
        return newUserAndGroup(new PermissionsI(perms));
    }

    /**
     * Creates a new group and experimenter and returns the event context.
     * 
     * @param perms The permissions level.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected EventContext newUserAndGroup(Permissions perms) 
    	throws Exception
    {
        IAdminPrx rootAdmin = root.getSession().getAdminService();
        String uuid = UUID.randomUUID().toString();
        Experimenter e = new ExperimenterI();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring("integeration"));
        e.setLastName(omero.rtypes.rstring("tester"));
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.getDetails().setPermissions(perms);
        rootAdmin.createGroup(g);
        rootAdmin.createUser(e, uuid);

        if (client != null) {
            client.__del__();
        }

        client = new omero.client();
        factory = client.createSession(uuid, uuid);
        iQuery = factory.getQueryService();
        iUpdate = factory.getUpdateService();
        iAdmin = factory.getAdminService();
        return iAdmin.getEventContext();
    }

    /**
     * Creates a pixels set.
     * 
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected Pixels createPixels() 
    	throws Exception
    {
        return createPixels(null);
    }

    /**
     * Creates a pixels set.
     * 
     * @param example The pixels set of reference.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected Pixels createPixels(Pixels example) 
    	throws Exception {
        IceMapper mapper = new IceMapper();
        ome.model.core.Pixels p = 
        	(ome.model.core.Pixels) mapper.reverse(example);
        p = ObjectFactory.createPixelGraphWithChannels(p, 3);
        return (Pixels) mapper.map(p);
    }

    /**
     * Creates a channel.
     * 
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected Channel createChannel() 
    	throws Exception
    {
        IceMapper mapper = new IceMapper();
        return (Channel) mapper.map(ObjectFactory.createChannel(null));
    }
    
    /**
     * Creates a plane info object.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected PlaneInfo createPlaneInfo()
   		throws Exception
    {
        PlaneInfo planeInfo = new PlaneInfoI();
        planeInfo.setTheZ(omero.rtypes.rint(0));
        planeInfo.setTheC(omero.rtypes.rint(0));
        planeInfo.setTheT(omero.rtypes.rint(0));
        planeInfo.setDeltaT(omero.rtypes.rdouble(0.5));
        return planeInfo;
    }
}
