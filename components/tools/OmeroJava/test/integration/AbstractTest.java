/*
 * $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.io.nio.AbstractFileSystemService;
import omero.api.IAdminPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.*;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


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

    /** Helper class creating mock object. */
    protected ModelMockFactory mmFactory;
    
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
        if (client != null) {
            client.__del__();
        }

        if (root != null) {
            root.__del__();
        }
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
        mmFactory = new ModelMockFactory(factory.getPixelsService());
        return iAdmin.getEventContext();
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
	 * Helper method to the wells the wells.
	 * 
	 * @param plateID The identifier of the plate.
	 * @param pixels  Pass <code>true</code> to load the pixels, 
	 * 					<code>false</code> otherwise.
	 * @return See above.
	 * @throws Exception  Thrown if an error occurred.
	 */
    @SuppressWarnings("unchecked")
	List<Well> loadWells(long plateID, boolean pixels)
		throws Exception 
	{
		StringBuilder sb = new StringBuilder();
		ParametersI param = new ParametersI();
		param.addLong("plateID", plateID);
		sb.append("select well from Well as well ");
		sb.append("left outer join fetch well.plate as pt ");
		sb.append("left outer join fetch well.wellSamples as ws ");
		sb.append("left outer join fetch ws.image as img ");
		if (pixels) {
			sb.append("left outer join fetch img.pixels as pix ");
	        sb.append("left outer join fetch pix.pixelsType as pixType ");
		}
        sb.append("where pt.id = :plateID");
        return (List<Well>) (List<?>) 
        	iQuery.findAllByQuery(sb.toString(), param);
	}
	
    /**
     * Makes sure that the passed object exists.
     * 
     * @param obj The object to handle.
     *  @throws Exception  Thrown if an error occurred.
     */
    void assertExists(IObject obj) 
    	throws Exception
    {
    	IObject copy = iQuery.find(
    			obj.getClass().getSimpleName(), obj.getId().getValue());
    	assertNotNull(String.format("%s:%s",
    			obj.getClass().getName(), obj.getId().getValue())
    			+ " is missing!", copy);
    }

    /**
     * Makes sure that the passed object does not exist.
     * 
     * @param obj The object to handle.
     *  @throws Exception  Thrown if an error occurred.
     */
    void assertDoesNotExist(IObject obj) 
    	throws Exception
    {
    	IObject copy = iQuery.find(
    			obj.getClass().getSimpleName(), obj.getId().getValue());
    	assertNull(String.format("%s:%s",
    			obj.getClass().getName(), obj.getId().getValue())
    			+ " still exists!", copy);
    }

    /**
     * Makes sure that the omero file exists of the given type and id
     * 
     * @param id The object id corresponding to the filename.
     * @param klass The class (table name) of the object.
     *  @throws Exception  Thrown if an error occurred.
     */
    void assertFileExists(Long id, String klass) throws Exception {
        AbstractFileSystemService afs = new AbstractFileSystemService(
        		root.getProperty("omero.data.dir"));
        File file;
        if (klass.equals("OriginalFile")) {
            file = new File(afs.getFilesPath(id));
        } else if (klass.equals("Pixels")) {
            file = new File(afs.getPixelsPath(id));
        } else { // Thumbnail
            file = new File(afs.getThumbnailPath(id));
        }
        assertTrue(String.format("File %s:%s does not exist!", 
        		klass, id.toString()), file.exists());
    }
    
    /**
     * Makes sure that the omero file does not exist of the given type and id
     * 
     * @param id The object id corresponding to the filename.
     * @param klass The class (table name) of the object.
     * @throws Exception  Thrown if an error occurred.
     */
    void assertFileDoesNotExist(Long id, String klass) 
    	throws Exception {
        AbstractFileSystemService afs = new AbstractFileSystemService(
        		root.getProperty("omero.data.dir"));
        File file;
        if (klass.equals("OriginalFile")) {
            file = new File(afs.getFilesPath(id));
        } else if (klass.equals("Pixels")) {
            file = new File(afs.getPixelsPath(id));
        } else { // Thumbnail
            file = new File(afs.getThumbnailPath(id));
        }
        assertFalse(String.format("File %s:%s still exists!", klass, 
        		id.toString()), file.exists());
    }

	/**
	 * Imports the specified OME-XML file and returns the pixels set
	 * if successfully imported.
	 * 
	 * @param importer The metadataStore to use.
	 * @param file The file to import.
	 * @param format The format of the file to import.
	 * @return The collection of imported pixels set.
	 * @throws Exception Thrown if an error occurred while encoding the image.
	 */
	List<Pixels> importFile(OMEROMetadataStoreClient importer, 
			File file, String format)
		throws Throwable
	{
		return importFile(importer, file, format, false);
	}
	
	/**
	 * Imports the specified OME-XML file and returns the pixels set
	 * if successfully imported.
	 * 
	 * @param importer The metadataStore to use.
	 * @param file The file to import.
	 * @param format The format of the file to import.
	 * @param metadata Pass <code>true</code> to only import the metadata,
	 *                 <code>false</code> otherwise.
	 * @return The collection of imported pixels set.
	 * @throws Exception Thrown if an error occurred while encoding the image.
	 */
	List<Pixels> importFile(OMEROMetadataStoreClient importer,
			File file, String format, boolean metadata)
		throws Throwable
	{
		ImportLibrary library = new ImportLibrary(importer, 
				new OMEROWrapper(new ImportConfig()));
		library.setMetadataOnly(metadata);
		List<Pixels> pixels = library.importImage(file, 0, 0, 1, format, null, 
				false, true, null, null);
		assertNotNull(pixels);
		assertTrue(pixels.size() > 0);
		return pixels;
	}
	
}
