/*
 * $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.services.blitz.repo.path.FsFile;
import omero.ApiUsageException;
import omero.ServerError;
import omero.api.IAdminPrx;
import omero.api.IDeletePrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.cmd.Chmod;
import omero.cmd.CmdCallbackI;
import omero.cmd.Delete;
import omero.cmd.DeleteRsp;
import omero.cmd.DoAll;
import omero.cmd.DoAllRsp;
import omero.cmd.ERR;
import omero.cmd.HandlePrx;
import omero.cmd.OK;
import omero.cmd.Request;
import omero.cmd.Response;
import omero.cmd.State;
import omero.cmd.Status;
import omero.model.BooleanAnnotation;
import omero.model.BooleanAnnotationI;
import omero.model.ChannelBinding;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetAnnotationLink;
import omero.model.DatasetAnnotationLinkI;
import omero.model.Experiment;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Fileset;
import omero.model.FilesetI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.LongAnnotation;
import omero.model.LongAnnotationI;
import omero.model.OriginalFile;
import omero.model.Permissions;
import omero.model.PermissionsI;
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
import omero.model.QuantumDef;
import omero.model.RenderingDef;
import omero.model.Screen;
import omero.model.ScreenAnnotationLink;
import omero.model.ScreenAnnotationLinkI;
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
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

import spec.AbstractTest;

//Application-internal dependencies

/**
 * Base test for integration tests.
 *
 * @since Beta4.2
 */
@Test(groups = { "client", "integration", "blitz", "fs" })
public class AbstractServerTest
	extends AbstractTest
{

	/** Performs the move as data owner.*/
	public static final int DATA_OWNER = 100;
	
	/** Performs the move as group owner.*/
	public static final int GROUP_OWNER = 101;
	
	/** Performs the move as group owner.*/
	public static final int ADMIN = 102;
	
    /** Identifies the <code>system</code> group. */
	public String SYSTEM_GROUP = "system";
	
	/** Identifies the <code>user</code> group. */
	public String USER_GROUP = "user";
	
	/** Identifies the <code>guest</code> group. */
	public String GUEST_GROUP = "guest";

	/** The client object, this is the entry point to the Server. */
    protected omero.client client;

    /** A root-client object. */
    protected omero.client root;

    /** Helper reference to the <code>Service factory</code>. */
    protected ServiceFactoryPrx factory;

    /** Helper reference to the <code>Service factory</code>. */
    protected ServiceFactoryPrx factoryEncrypted;

    /** Helper reference to the <code>IQuery</code> service. */
    protected IQueryPrx iQuery;

    /** Helper reference to the <code>IUpdate</code> service. */
    protected IUpdatePrx iUpdate;

    /** Helper reference to the <code>IAdmin</code> service. */
    protected IAdminPrx iAdmin;
    
    /** Helper reference to the <code>IDelete</code> service. */
    protected IDeletePrx iDelete;

    /** Reference to the importer store. */
    protected OMEROMetadataStoreClient importer;

    /** Helper class creating mock object. */
    protected ModelMockFactory mmFactory;

    /* the managed repository directory for the user from test class setup */
    private String userFsDir = null;

    /**
     * {@link omero.client} instances which are created via the newUser* methods.
     * These will be forcefully closed at the end of the test. "new omero.client(...)"
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

        final EventContext ctx = newUserAndGroup("rw----");
        this.userFsDir = ctx.userName + "_" + ctx.userId + FsFile.separatorChar;
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
     * @param owner Pass <code>true</code> to indicate that the new user
     * 				is an owner of the group, <code>false</code> otherwise.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected EventContext newUserAndGroup(String perms, boolean owner)
	throws Exception
    {
        return newUserAndGroup(new PermissionsI(perms), owner);
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
        return newUserAndGroup(new PermissionsI(perms), false);
    }

    /**
     * Creates a new group and experimenter and returns the event context.
     * 
     * @param perms The permissions level.
     * @param owner Pass <code>true</code> to indicate that the new user
     * 				is an owner of the group, <code>false</code> otherwise.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected EventContext newUserAndGroup(Permissions perms, boolean owner)
	throws Exception
    {
        IAdminPrx rootAdmin = root.getSession().getAdminService();
        String uuid = UUID.randomUUID().toString();
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.getDetails().setPermissions(perms);
        g = new ExperimenterGroupI(rootAdmin.createGroup(g), false);
        return newUserInGroup(g, owner);
    }

    /**
     * Changes the permissions of the group.
     * 
     * @param perms The permissions level.
     * @param groupId The identifier of the group to handle.
     * @throws Exception Thrown if an error occurred.
     */
    protected void resetGroupPerms(String perms, long groupId)
    	throws Exception
    {
    	 IAdminPrx rootAdmin = root.getSession().getAdminService();
    	 ExperimenterGroup g = rootAdmin.getGroup(groupId);
    	 g.getDetails().setPermissions(new PermissionsI(perms));
    	 rootAdmin.updateGroup(g);
    }
    
    /**
     * Creates a new group and experimenter and returns the event context.
     *
     * @param perms The permissions level.
     * @param experimenterId The identifier of the experimenter.
     * @
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected ExperimenterGroup newGroupAddUser(Permissions perms,
		long experimenterId)
	throws Exception
    {
	return newGroupAddUser(perms, Arrays.asList(experimenterId), false);
    }

    /**
     * Creates a new group and experimenter and returns the event context.
     *
     * @param perms The permissions level.
     * @param experimenterId The identifier of the experimenter.
     * @param owner Pass <code>true</code> to indicate that the new user
     * 				is an owner of the group, <code>false</code> otherwise.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected ExperimenterGroup newGroupAddUser(Permissions perms,
		long experimenterId, boolean owner)
	throws Exception
    {
	return newGroupAddUser(perms, Arrays.asList(experimenterId), owner);
    }

    /**
     * Creates a new group and experimenter and returns the event context.
     *
     * @param perms The permissions level.
     * @param experimenterIds The identifier of the experimenters.
     * @param owner Pass <code>true</code> to indicate that the new user
     * 				is an owner of the group, <code>false</code> otherwise.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected ExperimenterGroup newGroupAddUser(Permissions perms,
		List<Long> experimenterIds, boolean owner)
	throws Exception
    {
        IAdminPrx rootAdmin = root.getSession().getAdminService();
        String uuid = UUID.randomUUID().toString();
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.getDetails().setPermissions(perms);
        g = new ExperimenterGroupI(rootAdmin.createGroup(g), false);
        return addUsers(g, experimenterIds, owner);
    }

    protected ExperimenterGroup addUsers(ExperimenterGroup g,
            List<Long> experimenterIds, boolean owner)
        throws Exception
    {
        IAdminPrx rootAdmin = root.getSession().getAdminService();
        g = rootAdmin.getGroup(g.getId().getValue());
        Iterator<Long> i = experimenterIds.iterator();
        List<Experimenter> l = new ArrayList<Experimenter>();
        while (i.hasNext()) {
			Experimenter e = rootAdmin.getExperimenter(i.next());
	        rootAdmin.addGroups(e, Arrays.asList(g));
	        l.add(e);
		}
        if (owner && l.size() > 0) {
		rootAdmin.addGroupOwners(g, l);
        }
        return g;
    }

    /**
     * Creates a new group and experimenter and returns the event context.
     *
     * @param perms The permissions level.
     * @param experimenterId The identifier of the experimenters.
     * @param owner Pass <code>true</code> to indicate that the new user
     * 				is an owner of the group, <code>false</code> otherwise.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected ExperimenterGroup newGroupAddUser(String perms,
		List<Long> experimenterIds, boolean owner)
	throws Exception
    {
	return newGroupAddUser(new PermissionsI(perms), experimenterIds, owner);
    }

    /**
     * Creates a new group and experimenter and returns the event context.
     *
     * @param perms The permissions level.
     * @param experimenterId The identifier of the experimenters.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected ExperimenterGroup newGroupAddUser(String perms,
		List<Long> experimenterIds)
	throws Exception
    {
	return newGroupAddUser(new PermissionsI(perms), experimenterIds, false);
    }

    /**
     * Creates a new group and experimenter and returns the event context.
     *
     * @param perms The permissions level.
     * @param experimenterId The identifier of the experimenter.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected ExperimenterGroup newGroupAddUser(String perms,
		long experimenterId)
	throws Exception
    {
	return newGroupAddUser(new PermissionsI(perms), experimenterId);
    }

    /**
     * Creates a new group and experimenter and returns the event context.
     *
     * @param perms The permissions level.
     * @param experimenterId The identifier of the experimenter.
     * @param owner Pass <code>true</code> to indicate that the new user
     * 				is an owner of the group, <code>false</code> otherwise.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected ExperimenterGroup newGroupAddUser(String perms,
		long experimenterId, boolean owner)
	throws Exception
    {
	return newGroupAddUser(new PermissionsI(perms), experimenterId, owner);
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
        return newUserInGroup(previousUser, false);
    }

    /**
     * Takes the {@link EventContext} from another user and creates a new user
     * in the same group as that user is currently logged in to.
     *
     * @param previousUser The context of the previous user.
     * @param owner Pass <code>true</code> to indicate that the new user
     * 				is an owner of the group, <code>false</code> otherwise.
     * @throws Exception Thrown if an error occurred.
     */
    protected EventContext newUserInGroup(EventContext previousUser,
		boolean owner)
    throws Exception
    {
        ExperimenterGroup eg = new ExperimenterGroupI(previousUser.groupId, 
        		false);
        return newUserInGroup(eg, owner);
    }
    
    /**
     * Creates a new user in the specified group.
     * 
     * @param group The group to add the user to.
     * @param owner Pass <code>true</code> to indicate that the new user
     * 				is an owner of the group, <code>false</code> otherwise.
     * @return The context.
     * @throws Exception Thrown if an error occurred.
     */
    protected EventContext newUserInGroup(ExperimenterGroup group,
		boolean owner)
    	throws Exception
    {
        IAdminPrx rootAdmin = root.getSession().getAdminService();
        group = rootAdmin.getGroup(group.getId().getValue());

        String uuid = UUID.randomUUID().toString();
        Experimenter e = new ExperimenterI();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring("integration"));
        e.setLastName(omero.rtypes.rstring("tester"));
        long id = rootAdmin.createUser(e, group.getName().getValue());
        e = rootAdmin.getExperimenter(id);
        rootAdmin.addGroups(e, Arrays.asList(group));
        if (owner) {
		rootAdmin.addGroupOwners(group, Arrays.asList(e));
        }
        omero.client client = newOmeroClient();
        client.createSession(uuid, uuid);
        return init(client);
    }

    /**
     * Create a fileset with a template prefix appropriate for the user created by {@link #setUp()}.
     * Does not access the OMERO API or persist the new fileset.
     * @return a new fileset
     */
    protected Fileset newFileset() {
        final Fileset fileset = new FilesetI();
        fileset.setTemplatePrefix(omero.rtypes.rstring(this.userFsDir + System.currentTimeMillis() + FsFile.separatorChar));
        return fileset;
    }

    /**
     * Logs in the user.
     *
     * @param ownerEc The context of the user.
     * @param g The group to log into.
     * @throws Exception Thrown if an error occurred.
     */
    protected EventContext loginUser(ExperimenterGroup g)
	throws Exception
    {
	EventContext ec = iAdmin.getEventContext();
        omero.client client = newOmeroClient();
        client.createSession(ec.userName, "dummy");
        client.getSession().setSecurityContext(new ExperimenterGroupI(
			g.getId(), false));
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
     * Creates a new {@link omero.client} for root based on the current group.
     */
    protected void logRootIntoGroup() throws Exception {
        EventContext ec = iAdmin.getEventContext();
        logRootIntoGroup(ec);
    }

    /**
     * Creates a new {@link omero.client} for root based on the {@link EventContext}
     */
    protected void logRootIntoGroup(EventContext ec) throws Exception {
	logRootIntoGroup(ec.groupId);
    }

    /**
     * Creates a new {@link omero.client} for root based on the group identifier.
     */
    protected void logRootIntoGroup(long groupId) throws Exception {
        omero.client rootClient = newRootOmeroClient();
        rootClient.getSession().setSecurityContext(new ExperimenterGroupI(
			groupId, false));
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

        disconnect();
        init(ec); // Create new session with the added privileges
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
        if (importer != null) {
            importer.closeServices();
            importer = null;
        }

        if (client != null) {
            client.__del__();
        }
        client = null;
    }

    /**
     */
    protected EventContext init(EventContext ec) throws Exception {
        omero.client c = newOmeroClient();
        factoryEncrypted = c.createSession(ec.userName, "");
        return init(c);
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

        importer = new OMEROMetadataStoreClient();
        importer.initialize(factory);

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
	 * Helper method to load the wells.
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
		sb.append("left outer join fetch ws.plateAcquisition as pa ");
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
     * Helper method to load a well sample with its well and plate intact
     * (and possibly a screen if one exists) for the given pixels.
     * @param p
     * @return
     * @throws ServerError
     */
    protected WellSample getWellSample(Pixels p) throws ServerError {
        long id = p.getImage().getId().getValue();
        String sql = "select ws from WellSample as ws ";
        sql += "join fetch ws.well as w ";
        sql += "left outer join fetch ws.plateAcquisition as pa ";
        sql += "join fetch w.plate as p ";
        sql += "left outer join fetch p.screenLinks sl ";
        sql += "left outer join fetch sl.parent s ";
        sql += "where ws.image.id = :id";
        ParametersI param = new ParametersI();
        param.addId(id);
        List<IObject> results = iQuery.findAllByQuery(sql, param);
        assertTrue(results.size() == 1);
        WellSample ws = (WellSample) results.get(0);
        assertNotNull(ws);
        return ws;
    }

    /**
     * Helper method to load the Experiment which is is associated
     * with the pixels argument via Image.
     * @param p
     * @return
     * @throws ServerError
     */
    protected Experiment getExperiment(Pixels p) throws ServerError {
        long id = p.getImage().getId().getValue();
        String sql = "select e from Image i ";
        sql += "join i.experiment e ";
        sql += "where i.id = :id";
        ParametersI param = new ParametersI();
        param.addId(id);
        List<IObject> results = iQuery.findAllByQuery(sql, param);
        assertTrue(results.size() == 1);
        Experiment e = (Experiment) results.get(0);
        assertNotNull(e);
        return e;
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

    protected void assertAllExist(IObject...obj) throws Exception
    {
        for (IObject iObject : obj) {
            assertExists(iObject);
        }
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

    protected void assertNoneExist(IObject... obj) throws Exception
    {
        for (IObject iObject : obj) {
            assertDoesNotExist(iObject);
        }
    }


    /**
     * Imports the specified OME-XML file and returns the pixels set
     * if successfully imported.
     *
     * @param file The file to import.
     * @param format The format of the file to import.
     * @return The collection of imported pixels set.
     * @throws Exception Thrown if an error occurred while encoding the image.
     */
    protected List<Pixels> importFile(File file, String format)
        throws Throwable
    {
        return importFile(importer, file, format, false, null);
    }

    /**
     * Imports the specified OME-XML file and returns the pixels set
     * if successfully imported.
     *
     * @param file The file to import.
     * @param format The format of the file to import.
     * @return The collection of imported pixels set.
     * @throws Throwable Thrown if an error occurred while encoding the image.
     */
    protected List<Pixels> importFile(File file, String format, boolean metadata)
        throws Throwable
    {
        return importFile(importer, file, format, metadata, null);
    }

    /**
	 * Imports the specified OME-XML file and returns the pixels set
	 * if successfully imported.
	 * 
	 * @param importer The metadataStore to use.
	 * @param file The file to import.
	 * @param format The format of the file to import.
	 * @return The collection of imported pixels set.
	 * @throws Throwable Thrown if an error occurred while encoding the image.
	 */
	protected List<Pixels> importFile(OMEROMetadataStoreClient importer,
			File file, String format)
		throws Throwable
	{
		return importFile(importer, file, format, false, null);
	}
	
	/**
	 * Imports the specified OME-XML file and returns the pixels set
	 * if successfully imported.
	 * 
	 * @param importer The metadataStore to use.
	 * @param file The file to import.
	 * @param target The container where to import the image.
	 * @return The collection of imported pixels set.
	 * @throws Throwable Thrown if an error occurred while encoding the image.
	 */
	protected List<Pixels> importFile(File file, String format, IObject target)
		throws Throwable
	{
		return importFile(importer, file, format, false, target);
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
	 * @throws Throwable Thrown if an error occurred while encoding the image.
	 */
	protected List<Pixels> importFile(OMEROMetadataStoreClient importer,
			File file, String format, boolean metadata)
		throws Throwable
	{
		return importFile(importer, file, format, metadata, null);
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
	 * @throws Throwable Thrown if an error occurred while encoding the image.
	 */
	protected List<Pixels> importFile(OMEROMetadataStoreClient importer,
			File file, String format, boolean metadata, IObject target)
		throws Throwable
	{
		String[] paths = new String[1];
		paths[0] = file.getAbsolutePath();
		ImportConfig config = new ImportConfig();
		OMEROWrapper reader = new OMEROWrapper(config);
		IObserver o = new IObserver() {
	        public void update(IObservable importLibrary, ImportEvent event) {
	            
	        }
	    };
		ImportCandidates candidates = new ImportCandidates(reader, paths, o);
		
		ImportLibrary library = new ImportLibrary(importer, reader);
		ImportContainer ic = candidates.getContainers().get(0);
		//new ImportContainer(
        //        file, null, target, false, null, null, null, null);
		ic.setUserSpecifiedName(format);
		ic.setTarget(target);
		//ic = library.uploadFilesToRepository(ic);
		List<Pixels> pixels = library.importImage(ic, 0, 0, 1);
		assertNotNull(pixels);
		assertTrue(pixels.size() > 0);
		return pixels;
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
    protected String delete(omero.client c, Delete...dc)
    throws ApiUsageException, ServerError,
    InterruptedException
    {
        return delete(true, c, dc);
    }

    /**
     * Basic asynchronous delete command. Used in order to reduce the number
     * of places that we do the same thing in case the API changes.
     * 
     * @param passes Pass <code>true</code> to indicate that no error
     *               found in report, <code>false</code> otherwise.
     * @param dc The command to handle.
     * @param strict whether or not the method should succeed.
     * @throws ApiUsageException
     * @throws ServerError
     * @throws InterruptedException
     */
    protected String delete(boolean passes, omero.client c, 
            Delete...dc)
                throws ApiUsageException, ServerError,
                InterruptedException
    {

        callback(passes, c, dc);
        return "ok";
    }
    
    /**
     * Creates the command to change permissions.
     * 
     * @param session
     * @param type
     * @param id
     * @param perms
     * @return
     */
    Chmod createChmodCommand(String type, long id, String perms)
    {
	return new Chmod(type, id, null, perms);
    }

    /**
     * Asynchronous command for a single delete, this means a single
     * report is returned for testing. 
     * 
     * @param dc The SINGLE command to handle.
     * @throws ApiUsageException
     * @throws ServerError
     * @throws InterruptedException
     */
    protected DeleteRsp singleDeleteWithReport(omero.client c, Delete dc)
    throws ApiUsageException, ServerError,
    InterruptedException
    {
        return deleteWithReports(c, dc)[0];
    }
    /**
     * Asynchronous command for delete, report array is returned.
     * 
     * @param dc The command to handle.
     * @throws ApiUsageException
     * @throws ServerError
     * @throws InterruptedException
     */
    private DeleteRsp[] deleteWithReports(omero.client c, 
    		Delete...dc)
    throws ApiUsageException, ServerError,
    InterruptedException
    {
        CmdCallbackI cb = callback(true, c, dc);
        // If the above passes, then we know it's not an ERR
        DoAllRsp all = (DoAllRsp) cb.getResponse();
        DeleteRsp[] reports = new DeleteRsp[all.responses.size()];
        for (int i = 0; i < reports.length; i++)
        {
            reports[i] = (DeleteRsp) all.responses.get(i);
        }
        return reports;
    }
   
    /**
     * Create a single image with binary.
     *
     * After recent changes on the server to check for existing
     * binary data for pixels, many resetDefaults methods tested
     * below began returning null since {@link omero.LockTimeout}
     * exceptions were being thrown server-side. By using
     * omero.client.forEachTile, we can set the necessary data easily.
     *
     * @see ticket:5755
     */
    protected Image createBinaryImage() throws Exception {
        Image image = mmFactory.createImage();
        image = (Image) iUpdate.saveAndReturnObject(image);
        return createBinaryImage(image);
    }

    /**
     * Create the binary data for the given image.
     */
    protected Image createBinaryImage(Image image) throws Exception {
        Pixels pixels = image.getPrimaryPixels();
        //Image
        List<Long> ids = new ArrayList<Long>();
        ids.add(image.getId().getValue());
        //method already tested

        // first write to the image
        omero.util.RPSTileLoop loop =
            new omero.util.RPSTileLoop(client.getSession(), pixels);
        loop.forEachTile(256, 256, new omero.util.TileLoopIteration(){
            public void run(omero.util.TileData data, int z, int c, int t, 
            		int x, int y, int tileWidth,
                    int tileHeight, int tileCount) {
                data.setTile(new byte[tileWidth*tileHeight*8], z, c, t, x, y, 
                		tileWidth, tileHeight);
            }
        });
        // This block will change the updateEvent on the pixels
        // therefore we're going to reload the pixels.

        image.setPixels(0, loop.getPixels());
        return image;
    }

    /**
     * Creates various sharable annotations i.e. TagAnnotation, TermAnnotation,
     * FileAnnotation
     *
     * @param parent1 The object to link the annotation to.
     * @param parent2 The object to link the annotation to if not null.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected List<Long> createSharableAnnotation(IObject parent1,
		IObject parent2)
	throws Exception
    {
        // Copying to a proxy to prevent issues with parent.annotationLinks
        // becoming stale on multiple copies.
        parent1 = parent1.proxy();
        if (parent2 != null) {
            parent2 = parent2.proxy();
        }

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
	if (parent1 instanceof Image) {
		ImageAnnotationLink link = new ImageAnnotationLinkI();
		link.setChild(new TagAnnotationI(c.getId().getValue(), false));
		link.setParent((Image) parent1);
		links.add(link);
		link = new ImageAnnotationLinkI();
		link.setChild(new TermAnnotationI(t.getId().getValue(), false));
		link.setParent((Image) parent1);
		links.add(link);
		link = new ImageAnnotationLinkI();
		link.setChild(new FileAnnotationI(f.getId().getValue(), false));
		link.setParent((Image) parent1);
		links.add(link);
		if (parent2 != null) {
			link.setChild(new TagAnnotationI(c.getId().getValue(), false));
			link.setParent((Image) parent2);
			links.add(link);
			link = new ImageAnnotationLinkI();
			link.setChild(new TermAnnotationI(t.getId().getValue(), false));
			link.setParent((Image) parent2);
			links.add(link);
			link = new ImageAnnotationLinkI();
			link.setChild(new FileAnnotationI(f.getId().getValue(), false));
			link.setParent((Image) parent2);
			links.add(link);
		}
	} else if (parent1 instanceof Project) {
		ProjectAnnotationLink link = new ProjectAnnotationLinkI();
		link.setChild(new TagAnnotationI(c.getId().getValue(), false));
		link.setParent((Project) parent1);
		links.add(link);
		link = new ProjectAnnotationLinkI();
		link.setChild(new TermAnnotationI(t.getId().getValue(), false));
		link.setParent((Project) parent1);
		links.add(link);
		link = new ProjectAnnotationLinkI();
		link.setChild(new FileAnnotationI(f.getId().getValue(), false));
		link.setParent((Project) parent1);
		links.add(link);
		if (parent2 != null) {
			link.setChild(new TagAnnotationI(c.getId().getValue(), false));
			link.setParent((Project) parent2);
			links.add(link);
			link = new ProjectAnnotationLinkI();
			link.setChild(new TermAnnotationI(t.getId().getValue(), false));
			link.setParent((Project) parent2);
			links.add(link);
			link = new ProjectAnnotationLinkI();
			link.setChild(new FileAnnotationI(f.getId().getValue(), false));
			link.setParent((Project) parent2);
			links.add(link);
		}
	} else if (parent1 instanceof Dataset) {
		DatasetAnnotationLink link = new DatasetAnnotationLinkI();
		link.setChild(new TagAnnotationI(c.getId().getValue(), false));
		link.setParent((Dataset) parent1);
		links.add(link);
		link = new DatasetAnnotationLinkI();
		link.setChild(new TermAnnotationI(t.getId().getValue(), false));
		link.setParent((Dataset) parent1);
		links.add(link);
		link = new DatasetAnnotationLinkI();
		link.setChild(new FileAnnotationI(f.getId().getValue(), false));
		link.setParent((Dataset) parent1);
		links.add(link);
		if (parent2 != null) {
			link.setChild(new TagAnnotationI(c.getId().getValue(), false));
			link.setParent((Dataset) parent2);
			links.add(link);
			link = new DatasetAnnotationLinkI();
			link.setChild(new TermAnnotationI(t.getId().getValue(), false));
			link.setParent((Dataset) parent2);
			links.add(link);
			link = new DatasetAnnotationLinkI();
			link.setChild(new FileAnnotationI(f.getId().getValue(), false));
			link.setParent((Dataset) parent2);
			links.add(link);
		}
	} else if (parent1 instanceof Plate) {
		PlateAnnotationLink link = new PlateAnnotationLinkI();
		link.setChild(new TagAnnotationI(c.getId().getValue(), false));
		link.setParent((Plate) parent1);
		links.add(link);
		link = new PlateAnnotationLinkI();
		link.setChild(new TermAnnotationI(t.getId().getValue(), false));
		link.setParent((Plate) parent1);
		links.add(link);
		link = new PlateAnnotationLinkI();
		link.setChild(new FileAnnotationI(f.getId().getValue(), false));
		link.setParent((Plate) parent1);
		links.add(link);
		if (parent2 != null) {
			link.setChild(new TagAnnotationI(c.getId().getValue(), false));
			link.setParent((Plate) parent2);
			links.add(link);
			link = new PlateAnnotationLinkI();
			link.setChild(new TermAnnotationI(t.getId().getValue(), false));
			link.setParent((Plate) parent2);
			links.add(link);
			link = new PlateAnnotationLinkI();
			link.setChild(new FileAnnotationI(f.getId().getValue(), false));
			link.setParent((Plate) parent2);
			links.add(link);
		}
	} else if (parent1 instanceof Screen) {
		ScreenAnnotationLink link = new ScreenAnnotationLinkI();
		link.setChild(new TagAnnotationI(c.getId().getValue(), false));
		link.setParent((Screen) parent1);
		links.add(link);
		link = new ScreenAnnotationLinkI();
		link.setChild(new TermAnnotationI(t.getId().getValue(), false));
		link.setParent((Screen) parent1);
		links.add(link);
		link = new ScreenAnnotationLinkI();
		link.setChild(new FileAnnotationI(f.getId().getValue(), false));
		link.setParent((Screen) parent1);
		links.add(link);
		if (parent2 != null) {
			link.setChild(new TagAnnotationI(c.getId().getValue(), false));
			link.setParent((Screen) parent2);
			links.add(link);
			link = new ScreenAnnotationLinkI();
			link.setChild(new TermAnnotationI(t.getId().getValue(), false));
			link.setParent((Screen) parent2);
			links.add(link);
			link = new ScreenAnnotationLinkI();
			link.setChild(new FileAnnotationI(f.getId().getValue(), false));
			link.setParent((Screen) parent2);
			links.add(link);
		}
	} else if (parent1 instanceof Well) {
		WellAnnotationLink link = new WellAnnotationLinkI();
		link.setChild(new TagAnnotationI(c.getId().getValue(), false));
		link.setParent((Well) parent1);
		links.add(link);
		link = new WellAnnotationLinkI();
		link.setChild(new TermAnnotationI(t.getId().getValue(), false));
		link.setParent((Well) parent1);
		links.add(link);
		link = new WellAnnotationLinkI();
		link.setChild(new FileAnnotationI(f.getId().getValue(), false));
		link.setParent((Well) parent1);
		links.add(link);
		if (parent2 != null) {
			link.setChild(new TagAnnotationI(c.getId().getValue(), false));
			link.setParent((Well) parent2);
			links.add(link);
			link = new WellAnnotationLinkI();
			link.setChild(new TermAnnotationI(t.getId().getValue(), false));
			link.setParent((Well) parent2);
			links.add(link);
			link = new WellAnnotationLinkI();
			link.setChild(new FileAnnotationI(f.getId().getValue(), false));
			link.setParent((Well) parent2);
			links.add(link);
		}
	} else if (parent1 instanceof WellSample) {
		WellSampleAnnotationLink link = new WellSampleAnnotationLinkI();
		link.setChild(new TagAnnotationI(c.getId().getValue(), false));
		link.setParent((WellSample) parent1);
		links.add(link);
		link = new WellSampleAnnotationLinkI();
		link.setChild(new TermAnnotationI(t.getId().getValue(), false));
		link.setParent((WellSample) parent1);
		links.add(link);
		link = new WellSampleAnnotationLinkI();
		link.setChild(new FileAnnotationI(f.getId().getValue(), false));
		link.setParent((WellSample) parent1);
		links.add(link);
		if (parent2 != null) {
			link.setChild(new TagAnnotationI(c.getId().getValue(), false));
			link.setParent((WellSample) parent2);
			links.add(link);
			link = new WellSampleAnnotationLinkI();
			link.setChild(new TermAnnotationI(t.getId().getValue(), false));
			link.setParent((WellSample) parent2);
			links.add(link);
			link = new WellSampleAnnotationLinkI();
			link.setChild(new FileAnnotationI(f.getId().getValue(), false));
			link.setParent((WellSample) parent2);
			links.add(link);
		}
	} else if (parent1 instanceof PlateAcquisition) {
		PlateAcquisitionAnnotationLink link =
			new PlateAcquisitionAnnotationLinkI();
		link.setChild(new TagAnnotationI(c.getId().getValue(), false));
		link.setParent((PlateAcquisition) parent1);
		links.add(link);
		link = new PlateAcquisitionAnnotationLinkI();
		link.setChild(new TermAnnotationI(t.getId().getValue(), false));
		link.setParent((PlateAcquisition) parent1);
		links.add(link);
		link = new PlateAcquisitionAnnotationLinkI();
		link.setChild(new FileAnnotationI(f.getId().getValue(), false));
		link.setParent((PlateAcquisition) parent1);
		links.add(link);
		if (parent2 != null) {
			link.setChild(new TagAnnotationI(c.getId().getValue(), false));
			link.setParent((PlateAcquisition) parent2);
			links.add(link);
			link = new PlateAcquisitionAnnotationLinkI();
			link.setChild(new TermAnnotationI(t.getId().getValue(), false));
			link.setParent((PlateAcquisition) parent2);
			links.add(link);
			link = new PlateAcquisitionAnnotationLinkI();
			link.setChild(new FileAnnotationI(f.getId().getValue(), false));
			link.setParent((PlateAcquisition) parent2);
			links.add(link);
		}
	}
	if (links.size() > 0) iUpdate.saveAndReturnArray(links);
	return ids;
    }

    /**
     * Creates various non sharable annotations.
     *
     * @param parent The object to link the annotation to.
     * @param ns     The name space or <code>null</code>.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected List<Long> createNonSharableAnnotation(IObject parent, String ns)
	throws Exception
    {
        // Copying to a proxy to prevent issues with parent.annotationLinks
        // becoming stale on multiple copies.
        parent = parent.proxy();

	//creation already tested in UpdateServiceTest
	List<Long> ids = new ArrayList<Long>();
	CommentAnnotation c = new CommentAnnotationI();
	c.setTextValue(omero.rtypes.rstring("comment"));
	if (ns != null) c.setNs(omero.rtypes.rstring(ns));

	c = (CommentAnnotation) iUpdate.saveAndReturnObject(c);

	LongAnnotation l = new LongAnnotationI();
	l.setLongValue(omero.rtypes.rlong(1L));
	if (ns != null) l.setNs(omero.rtypes.rstring(ns));

	l = (LongAnnotation) iUpdate.saveAndReturnObject(l);

	BooleanAnnotation b = new BooleanAnnotationI();
	b.setBoolValue(omero.rtypes.rbool(true));
	if (ns != null) b.setNs(omero.rtypes.rstring(ns));

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
	 * Modifies the graph.
	 *
	 * @param change The object hosting information about data to modify.
	 * @return See above.
	 * @throws Exception
	 */
	protected Response doChange(Request change)
	    throws Exception {
	    return doChange(client, factory, change, true, null);
	}

	/**
	 * Modifies the graph.
	 *
	 * @param change The object hosting information about data to modify.
	 * @return See above.
	 * @throws Exception
	 */
	protected Response doChange(Request change, long groupID)
	    throws Exception {
	    return doChange(client, factory, change, true, groupID);
	}

	protected Response doChange(omero.client c, ServiceFactoryPrx f,
			Request change, boolean pass)
		throws Exception
	{
		return doChange(c, f, change, pass, null);
	}

	protected Response doAllChanges(omero.client c, ServiceFactoryPrx f,
	        boolean pass, Request...changes) throws Exception {
	    DoAll all = new DoAll();
	    all.requests = new ArrayList<Request>();
	    all.requests.addAll(Arrays.asList(changes));
	    return doChange(c, f, all, pass);
	}

	/**
	 * 
	 * @param c
	 * @param f
	 * @param change
	 * @param pass
	 * @return
	 * @throws Exception
	 */
	protected Response doChange(omero.client c, ServiceFactoryPrx f,
			Request change, boolean pass, Long groupID)
		throws Exception
	{
		final Map<String, String> callContext = new HashMap<String, String>();
		if (groupID != null) {
			callContext.put("omero.group", ""+groupID);
		}
		final HandlePrx prx = f.submit(change, callContext);
		//assertFalse(prx.getStatus().flags.contains(State.FAILURE));
		CmdCallbackI cb = new CmdCallbackI(c, prx);
		cb.loop(20, 500);
		return assertCmd(cb, pass);
	}

        protected CmdCallbackI callback(boolean passes, omero.client c, omero.cmd.Request...reqs)
            throws ApiUsageException, ServerError,
                   InterruptedException
        {
            DoAll all = new DoAll();
            all.requests = new ArrayList<omero.cmd.Request>();
            for (omero.cmd.Request req : reqs) {
                all.requests.add(req);
            }
            HandlePrx handle = c.getSession().submit(all);
            CmdCallbackI cb = new CmdCallbackI(c, handle);
            cb.loop(10 * reqs.length, 500); // throws on timeout
            assertCmd(cb, passes);
            return cb;
        }

        protected Response assertCmd(CmdCallbackI cb, boolean pass) {
		assertNotNull(cb.getResponse());

		Status status = cb.getStatus();
		Response rsp = cb.getResponse();
		assertNotNull(rsp);
		if (pass) {
		    if (rsp instanceof ERR) {
		        ERR err = (ERR) rsp;
		        String name = err.getClass().getSimpleName();
		        fail(String.format("Found %s when pass==true: %s (%s) params=%s",
		                name, err.category, err.name, err.parameters));
		    }
		    assertFalse(status.flags.contains(State.FAILURE));
		} else {
		    if (rsp instanceof OK) {
		        OK ok = (OK) rsp;
		        fail(String.format("Found OK when pass==false: %s", ok));
		    }
		    assertTrue(status.flags.contains(State.FAILURE));
		}
		return rsp;
	}

    /**
     * Creates a new group with the specified permissions and sets the role
     * of the user.
     * 
     * @param permissions The permissions of the group.
     * @param userRole The role of the user e.g. group owner.
     * @throws Exception Thrown if an error occurred.
     */
    protected void login(String permissions, int userRole)
    throws Exception
    {
    	newUserAndGroup(permissions);
    	switch (userRole) {
			case GROUP_OWNER:
				makeGroupOwner();
				break;
			case ADMIN:
				logRootIntoGroup();
		}
    }
}
