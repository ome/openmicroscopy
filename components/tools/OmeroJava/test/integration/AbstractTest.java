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
import omero.RLong;
import omero.api.IAdminPrx;
import omero.api.IPixelsPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Annotation;
import omero.model.AnnotationAnnotationLinkI;
import omero.model.Channel;
import omero.model.ChannelBinding;
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
import omero.model.PixelsType;
import omero.model.PlaneInfo;
import omero.model.PlaneInfoI;
import omero.model.PlateI;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;
import omero.model.QuantumDef;
import omero.model.RenderingDef;
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

	/** The default number of channels. */
	public int DEFAULT_CHANNELS_NUMBER = 3;
	
	/** The default size along the X-axis. */
	public int SIZE_X = 10;
	
	/** The default size along the Y-axis. */
	public int SIXE_Y = 10;
	
	/** The number of z-sections. */
	public int SIXE_Z = 10;
	
	/** The number of time points. */
	public int SIXE_T = 10;
	
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
        p = ObjectFactory.createPixelGraphWithChannels(p, 
        		DEFAULT_CHANNELS_NUMBER);
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
    
    /**
     * Compares the passed rendering definitions.
     * 
     * @param def1 The first rendering definition to handle.
     * @param def2 The second rendering definition to handle.
     * @throws Exception Thrown if an error occurred.
     */
    protected void compareRenderingDef(RenderingDef def1, RenderingDef def2)
		throws Exception 
	{
		assertNotNull(def1);
		assertNotNull(def2);
		assertTrue(def1.getDefaultZ().getValue() == 
				def2.getDefaultZ().getValue());
		assertTrue(def1.getDefaultT().getValue() == 
			def2.getDefaultT().getValue());
		assertTrue(def1.getModel().getValue().getValue().equals( 
			def2.getModel().getValue().getValue()));
		QuantumDef q1 = def1.getQuantization();
		QuantumDef q2 = def2.getQuantization();
		assertNotNull(q1);
		assertNotNull(q2);
		assertTrue(q1.getBitResolution().getValue() == 
			q2.getBitResolution().getValue());
		assertTrue(q1.getCdStart().getValue() == 
			q2.getCdStart().getValue());
		assertTrue(q1.getCdEnd().getValue() == 
			q2.getCdEnd().getValue());
		List<ChannelBinding> channels1 = def1.copyWaveRendering();
		List<ChannelBinding> channels2 = def2.copyWaveRendering();
		assertNotNull(channels1);
		assertNotNull(channels2);
		assertTrue(channels1.size() == channels2.size());
		Iterator<ChannelBinding> i = channels1.iterator();
		ChannelBinding c1, c2;
		int index = 0;
		while (i.hasNext()) {
			c1 = i.next();
			c2 = channels2.get(index);
			assertTrue(c1.getAlpha().getValue() == c2.getAlpha().getValue());
			assertTrue(c1.getRed().getValue() == c2.getRed().getValue());
			assertTrue(c1.getGreen().getValue() == c2.getGreen().getValue());
			assertTrue(c1.getBlue().getValue() == c2.getBlue().getValue());
			assertTrue(c1.getCoefficient().getValue() 
					== c2.getCoefficient().getValue());
			assertTrue(c1.getFamily().getValue().getValue().equals(
					c2.getFamily().getValue().getValue()) );
			assertTrue(c1.getInputStart().getValue() == 
				c2.getInputStart().getValue());
			assertTrue(c1.getInputEnd().getValue() == 
				c2.getInputEnd().getValue());
			Boolean b1 = Boolean.valueOf(c1.getActive().getValue());
			Boolean b2 = Boolean.valueOf(c2.getActive().getValue());
			assertTrue(b1.equals(b2));
			b1 = Boolean.valueOf(c1.getNoiseReduction().getValue());
			b2 = Boolean.valueOf(c2.getNoiseReduction().getValue());
			assertTrue(b1.equals(b2));
		}
	}
 
	/**
	 * Creates an image. This method has been tested in 
	 * <code>PixelsServiceTest</code>.
	 * 
	 * @return See above.
	 * @throws Exception Thrown if an error occurred.
	 */
	protected Image createImage()
		throws Exception
	{
		return createImage(1, 1, 1, 1, 1);
	}
	
	/**
	 * Creates an image. This method has been tested in 
	 * <code>PixelsServiceTest</code>.
	 * 
	 * @return See above.
	 * @throws Exception Thrown if an error occurred.
	 */
	protected Image createImage(int sizeC, int sizeX, int sizeY, int sizeZ, 
			int sizeT)
		throws Exception
	{
		IPixelsPrx svc = factory.getPixelsService();

    	List<IObject> types = 
    		svc.getAllEnumerations(PixelsType.class.getName());
    	List<Integer> channels = new ArrayList<Integer>();
    	for (int i = 0; i < sizeC; i++) {
			channels.add(i);
		}
    	
    	RLong id = svc.createImage(sizeX, sizeY, sizeZ, sizeT, channels, 
    			(PixelsType) types.get(1), "test", "");
    	//Retrieve the image.
    	ParametersI param = new ParametersI();
    	param.addId(id.getValue());
    	Image img = (Image) iQuery.findByQuery(
    			"select i from Image i where i.id = :id", param);
    	return (Image) iUpdate.saveAndReturnObject(img);
	}
	
}
