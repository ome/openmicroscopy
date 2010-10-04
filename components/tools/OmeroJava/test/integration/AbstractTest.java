/*
 * $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import junit.framework.TestCase;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
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
import omero.model.ChannelBinding;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.IObject;
import omero.model.Permissions;
import omero.model.PermissionsI;
import omero.model.Pixels;
import omero.model.QuantumDef;
import omero.model.RenderingDef;
import omero.model.Well;
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
    
    /** Helper reference to the <code>IDelete</code> service. */
    protected IDeletePrx iDelete;

    /** Helper class creating mock object. */
    protected ModelMockFactory mmFactory;
    
    protected String rootpass;

    /**
     * {@link omero.client} instances which are created via the newUser* methods.
     * These will be forcifully closed at the end of the test. "new omero.client(...)"
     * should be strictly avoided except for in the method {@link #newOmeroClient()}.
     *
     * @see #newUserAndGroup(Permissions)
     * @see #newUserAndGroup(String)
     * @see #newUserInGroup()
     * @see #newUserInGroup(EventContext)
     * @see #newUserInGroup(ExperimenterGroup)
     */
    private final Set<omero.client> clients = new HashSet<omero.client>();

    /**
     * Sole location where {@link omero.client#client()} 
     * or any other {@link omero.client}
     * constructor should be called.
     */
    protected omero.client newOmeroClient()
    {
        omero.client client = new omero.client(); // OK
        clients.add(client);
        return client;
    }

    /**
     * Creates a client for the root user.
     * 
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected omero.client newRootOmeroClient() 
    	throws Exception
    {
        omero.client client = newOmeroClient();
        client.createSession("root", rootpass);
        return client;
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
        omero.client tmp = newOmeroClient();
        rootpass = tmp.getProperty("omero.rootpass");
        root = newRootOmeroClient();
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
        for (omero.client c : clients) {
            if (c != null) {
                c.__del__();
            }
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
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.getDetails().setPermissions(perms);
        g = new ExperimenterGroupI(rootAdmin.createGroup(g), false);
        return newUserInGroup(g);
    }

    /**
     * Creates a new user in the current group.
     * @return
     */
    protected EventContext newUserInGroup() throws Exception {
        EventContext ec = 
        	client.getSession().getAdminService().getEventContext();
        return newUserInGroup(ec);
    }

    /**
     * Takes the {@link EventContext} from another user and creates a new user
     * in the same group as that user is currently logged in to.
     * 
     * @param previousUser The context of the previous user.
     * @throws Exception Thrown if an error occurred.
     */
    protected EventContext newUserInGroup(EventContext previousUser)
    throws Exception
    {
        ExperimenterGroup eg = new ExperimenterGroupI(previousUser.groupId, 
        		false);
        return newUserInGroup(eg);
    }
    
    /**
     * Creates a new user in the specified group.
     * 
     * @param group The group to add the user to.
     * @return The context.
     * @throws Exception Thrown if an error occurred.
     */
    protected EventContext newUserInGroup(ExperimenterGroup group)
    	throws Exception
    {
        
        IAdminPrx rootAdmin = root.getSession().getAdminService();
        group = rootAdmin.getGroup(group.getId().getValue());

        String uuid = UUID.randomUUID().toString();
        Experimenter e = new ExperimenterI();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring("integeration"));
        e.setLastName(omero.rtypes.rstring("tester"));
        rootAdmin.createUser(e, group.getName().getValue());

        omero.client client = newOmeroClient();
        client.createSession(uuid, uuid);
        return init(client);
    }

    /**
     * Logs in the user.
     * 
     * @param ownerEc The context of the user.
     * @throws Exception Thrown if an error occurred.
     */
    protected void loginUser(EventContext ownerEc) 
    	throws Exception
    {
        omero.client client = newOmeroClient();
        client.createSession(ownerEc.userName, "dummy");
        init(client);
    }

    /**
     * Changes the {@link ServiceFactoryPrx#setSecurityContext(IObject) security context}
     * for the root user to the current group.
     */
    protected void logRootIntoGroup() throws Exception {
        EventContext ec = iAdmin.getEventContext();
        omero.client rootClient = newRootOmeroClient();
        rootClient.getSession().setSecurityContext(new ExperimenterGroupI(
        		ec.groupId, false));
        init(rootClient);
    }

    /**
     * Makes the current user an owner of the current group.
     */
    protected void makeGroupOwner() throws Exception {
        EventContext ec = client.getSession().getAdminService().getEventContext();
        IAdminPrx rootAdmin = root.getSession().getAdminService();
        rootAdmin.setGroupOwner(new ExperimenterGroupI(ec.groupId, false),
                new ExperimenterI(ec.userId, false));
    }

    /**
     * Saves the current client before calling {@link #clean()} and returns
     * it to the user.
     */
    protected omero.client disconnect() throws Exception {
        omero.client oldClient = client;
        client = null;
        clean();
        return oldClient;
    }

    /**
     * If {@link #client} is non-null, destroys the client and nulls all
     * fields which were set on creation.
     */
    protected void clean() throws Exception {
        if (client != null) {
            client.__del__();
        }
        client = null;
        factory = null;
        iQuery = null;
        iUpdate = null;
        iAdmin = null;
        iDelete = null;
        mmFactory = null;
    }

    /**
     * Resets the client and return the event context.
     * 
     * @param client The client to handle.
     * @return The event context to handle.
     * @throws Exception
     */
    protected EventContext init(omero.client client) throws Exception {

        clean();
        
        this.client = client;
        factory = client.getSession();
        iQuery = factory.getQueryService();
        iUpdate = factory.getUpdateService();
        iAdmin = factory.getAdminService();
        iDelete = factory.getDeleteService();
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
	protected List<Well> loadWells(long plateID, boolean pixels)
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
    protected void assertExists(IObject obj)
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
    protected void assertDoesNotExist(IObject obj)
    	throws Exception
    {
    	IObject copy = iQuery.find(
    			obj.getClass().getSimpleName(), obj.getId().getValue());
    	assertNull(String.format("%s:%s",
    			obj.getClass().getName(), obj.getId().getValue())
    			+ " still exists!", copy);
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
	protected List<Pixels> importFile(OMEROMetadataStoreClient importer,
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
	protected List<Pixels> importFile(OMEROMetadataStoreClient importer,
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
	
    protected String delete(omero.client c, DeleteCommand...dc)
    throws ApiUsageException, ServerError,
    InterruptedException
    {
        return delete(true, c.getSession().getDeleteService(), c, dc);
    }

    /**
     * Basic asynchronous delete command. Used in order to reduce the number
     * of places that we do the same thing in case the API changes.
     *
     * @param dc The command to handle.
     * @throws ApiUsageException
     * @throws ServerError
     * @throws InterruptedException
     */
    protected String delete(IDeletePrx proxy, omero.client c, DeleteCommand...dc)
    throws ApiUsageException, ServerError,
    InterruptedException
    {
        return delete(true, proxy, c, dc);
    }

    /**
     * Basic asynchronous delete command. Used in order to reduce the number
     * of places that we do the same thing in case the API changes.
     * 
     * @param passes Pass <code>true</code> to indicate that no error
     * 				 found in report, <code>false</code> otherwise.
     * @param dc The command to handle.
     * @param strict whether or not the method should succeed.
     * @throws ApiUsageException
     * @throws ServerError
     * @throws InterruptedException
     */
    protected String delete(boolean passes, IDeletePrx proxy, omero.client c, 
    		DeleteCommand...dc)
    throws ApiUsageException, ServerError,
    InterruptedException
    {

		DeleteHandlePrx handle = proxy.queueDelete(dc);
		DeleteCallbackI cb = new DeleteCallbackI(c, handle);
		int count = 10;
		while (null == cb.block(500)) {
			count--;
			if (count == 0) {
				throw new RuntimeException("Waiting on delete timed out");
			}
		}
		String report = handle.report().toString();
		if (passes) {
		    assertEquals(report, 0, handle.errors());
		} else {
		    assertTrue(report, 0 < handle.errors());
		}
		return report;
	}
	   
}
