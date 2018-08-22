/*
 *   Copyright 2010-2015 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import ome.formats.importer.util.ErrorHandler;
import ome.io.nio.SimpleBackOff;
import ome.services.blitz.repo.path.FsFile;
import ome.system.Login;
import omero.ApiUsageException;
import omero.RLong;
import omero.RType;
import omero.SecurityViolation;
import omero.ServerError;
import omero.rtypes;
import omero.api.IAdminPrx;
import omero.api.IPixelsPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.cmd.CmdCallbackI;
import omero.cmd.Delete2;
import omero.cmd.Delete2Response;
import omero.cmd.DoAll;
import omero.cmd.DoAllRsp;
import omero.cmd.ERR;
import omero.cmd.HandlePrx;
import omero.cmd.OK;
import omero.cmd.Request;
import omero.cmd.Response;
import omero.cmd.State;
import omero.cmd.Status;
import omero.grid.RepositoryMap;
import omero.grid.RepositoryPrx;
import omero.model.Annotation;
import omero.model.BooleanAnnotation;
import omero.model.BooleanAnnotationI;
import omero.model.ChannelBinding;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetAnnotationLink;
import omero.model.DatasetAnnotationLinkI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Detector;
import omero.model.DetectorAnnotationLink;
import omero.model.DetectorAnnotationLinkI;
import omero.model.Experiment;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Fileset;
import omero.model.FilesetI;
import omero.model.Folder;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.Instrument;
import omero.model.InstrumentAnnotationLink;
import omero.model.InstrumentAnnotationLinkI;
import omero.model.LightSource;
import omero.model.LightSourceAnnotationLink;
import omero.model.LightSourceAnnotationLinkI;
import omero.model.LongAnnotation;
import omero.model.LongAnnotationI;
import omero.model.MapAnnotation;
import omero.model.MapAnnotationI;
import omero.model.NamedValue;
import omero.model.OriginalFile;
import omero.model.OriginalFileAnnotationLink;
import omero.model.OriginalFileAnnotationLinkI;
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
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
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
import omero.sys.EventContext;
import omero.sys.Parameters;
import omero.sys.ParametersI;
import omero.sys.Roles;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.ResourceUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * Base test for integration tests.
 *
 * @since Beta4.2
 */
public class AbstractServerTest extends AbstractTest {

    /** Performs the move as data owner. */
    public static final int MEMBER = 100;

    /** Performs the move as group owner. */
    public static final int GROUP_OWNER = 101;

    /** Performs the move as group owner. */
    public static final int ADMIN = 102;

    /** Scaling factor used for CmdCallbackI loop timings. */
    protected long scalingFactor;

    /** All groups context to use in cases where errors due to group restriction are to be avoided. */
    protected static final ImmutableMap<String, String> ALL_GROUPS_CONTEXT = ImmutableMap.of(Login.OMERO_GROUP, "-1");

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

    /** Helper reference to the <code>IPixels</code> service. */
    protected IPixelsPrx iPix;

    /** Helper reference to the server's critical roles. */
    protected Roles roles;

    /** Reference to the importer store. */
    private OMEROMetadataStoreClient importer;

    /** Helper class creating mock object. */
    protected ModelMockFactory mmFactory;

    /** the managed repository directory for the user from test class setup **/
    private String userFsDir = null;

    /**
     * {@link omero.client} instances which are created via the newUser*
     * methods. These will be forcefully closed at the end of the test.
     * "new omero.client(...)" should be strictly avoided except for in the
     * method {@link #newOmeroClient()}.
     *
     * @see #newUserAndGroup(Permissions)
     * @see #newUserAndGroup(String)
     * @see #newUserInGroup()
     * @see #newUserInGroup(EventContext)
     * @see #newUserInGroup(ExperimenterGroup)
     */
    private final Set<omero.client> clients = new HashSet<omero.client>();

    /* a simple valid Python script */
    private String pythonScript = null;

    protected AbstractServerTest() {
        final ome.system.Roles defaultRoles = new ome.system.Roles();
        roles = new Roles(
                defaultRoles.getRootId(), defaultRoles.getRootName(),
                defaultRoles.getSystemGroupId(), defaultRoles.getSystemGroupName(),
                defaultRoles.getUserGroupId(), defaultRoles.getUserGroupName(),
                defaultRoles.getGuestId(), defaultRoles.getGuestName(),
                defaultRoles.getGuestGroupId(), defaultRoles.getGuestGroupName());
    }

    /**
     * Sole location where {@link omero.client#client()} or any other
     * {@link omero.client} constructor should be called.
     */
    protected omero.client newOmeroClient() {
        omero.client client = new omero.client(); // OK
        clients.add(client);
        return client;
    }

    /**
     * Creates a client for the root user.
     *
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected omero.client newRootOmeroClient() throws Exception {
        omero.client client = newOmeroClient();
        client.createSession("root", rootpass);
        return client;
    }

    /**
     * Initializes the various services.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Override
    @BeforeClass(alwaysRun = true)
    protected void setUp() throws Exception {
        // administrator client
        omero.client tmp = newOmeroClient();
        rootpass = tmp.getProperty("omero.rootpass");
        root = newRootOmeroClient();
        tmp.__del__();

        scalingFactor = 500;
        final String timeoutString = System.getProperty("omero.test.timeout");
        if (StringUtils.isNotBlank(timeoutString)) {
            try {
                scalingFactor = Long.valueOf(timeoutString);
            } catch (NumberFormatException e) {
                log.warn("Problem setting 'omero.test.timeout' to: {}. " +
                         "Defaulting to {}.", timeoutString, scalingFactor);
            }
        }
        final EventContext ctx = newUserAndGroup("rw----");
        this.userFsDir = ctx.userName + "_" + ctx.userId + FsFile.separatorChar;
        SimpleBackOff backOff = new SimpleBackOff();
        long newScalingFactor = (long) backOff.getScalingFactor()
                * backOff.getCount();
        if (newScalingFactor > scalingFactor) {
            scalingFactor = newScalingFactor;
        }
    }

    /**
     * Closes the session.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Override
    @AfterClass
    public void tearDown() throws Exception {
        clean();
        for (omero.client c : clients) {
            if (c != null) {
                c.__del__();
            }
        }
    }

    /**
     * Creates the import if not already initialized and returns it.
     */
    protected OMEROMetadataStoreClient createImporter() throws Exception
    {

        if (importer == null) {
            try {
                importer = new OMEROMetadataStoreClient();
                importer.initialize(factory);
            } catch (Exception e) {
                if (importer != null) {
                    try {
                        importer.closeServices();
                    } catch (Exception ex) {
                        //the initial error will be thrown
                    }
                    importer = null;
                }
                throw e;
            }
        }
        return importer;
    }

    /**
     * An enumeration of properties for which IObjects can be examined.
     * Used in {@link AbstractServerTest.verifyObjectProperty}.
     * @author pwalczysko@dundee.ac.uk
     */
    private enum DetailsProperty {
        GROUP("group"),
        OWNER("owner");

        private final String name;

        DetailsProperty(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    /**
     * Add the given annotation to the given image.
     * @param image an image
     * @param annotation an annotation
     * @return the new loaded link from the image to the annotation
     * @throws ServerError an error possibly occurring during saving of the link
     */
    protected ImageAnnotationLink linkParentToChild(Image image, Annotation annotation) throws ServerError {
        if (image.isLoaded()) {
            image = (Image) image.proxy();
        }
        if (annotation.isLoaded() && annotation.getId() != null) {
            annotation = (Annotation) annotation.proxy();
        }

        final ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.setParent(image);
        link.setChild(annotation);
        return (ImageAnnotationLink) iUpdate.saveAndReturnObject(link);
    }

    /**
     * Create a link between a Project and a Dataset.
     * @param project an OMERO Project
     * @param dataset an OMERO Dataset
     * @return the created link
     * @throws ServerError an error possibly occurring during saving of the link
     */
    protected ProjectDatasetLink linkParentToChild(Project project, Dataset dataset) throws ServerError {
        if (project.isLoaded() && project.getId() != null) {
            project = (Project) project.proxy();
        }
        if (dataset.isLoaded() && dataset.getId() != null) {
            dataset = (Dataset) dataset.proxy();
        }

        final ProjectDatasetLink link = new ProjectDatasetLinkI();
        link.setParent(project);
        link.setChild(dataset);
        return (ProjectDatasetLink) iUpdate.saveAndReturnObject(link);
    }

    /**
     * Create a link between a Dataset and an Image.
     * @param dataset an OMERO Dataset
     * @param image an OMERO Image
     * @return the created link
     * @throws ServerError an error possibly occurring during saving of the link
     */
    protected DatasetImageLink linkParentToChild(Dataset dataset, Image image) throws ServerError {
        if (dataset.isLoaded() && dataset.getId() != null) {
            dataset = (Dataset) dataset.proxy();
        }
        if (image.isLoaded() && image.getId() != null) {
            image = (Image) image.proxy();
        }

        final DatasetImageLink link = new DatasetImageLinkI();
        link.setParent(dataset);
        link.setChild(image);
        return (DatasetImageLink) iUpdate.saveAndReturnObject(link);
    }

    /**
     * Assert that the given object is in the given group.
     * @param object a model object
     * @param expectedGroup an experimenter group
     * @throws ServerError unexpected
     */
    protected void assertInGroup(IObject object, ExperimenterGroup expectedGroup) throws ServerError {
        assertInGroup(Collections.singleton(object), expectedGroup);
    }

    /**
     * Assert that the given objects are in the given group.
     * @param objects some model objects
     * @param expectedGroup an experimenter group
     * @throws ServerError unexpected
     */
    protected void assertInGroup(Collection<? extends IObject> objects, ExperimenterGroup expectedGroup) throws ServerError {
        assertInGroup(objects, expectedGroup.getId().getValue());
    }

    /**
     * Assert that the given object is in the given group.
     * @param object a model object
     * @param expectedGroupId a group Id
     * @throws ServerError unexpected
     */
    protected void assertInGroup(IObject object, long expectedGroupId) throws ServerError {
        assertInGroup(Collections.singleton(object), expectedGroupId);
    }

    /**
     * Assert that the given objects are in the given group.
     * @param objects some model objects
     * @param expectedGroupId a group Id
     * @throws ServerError unexpected
     */
    protected void assertInGroup(Collection<? extends IObject> objects, long expectedGroupId) throws ServerError {
        verifyObjectProperty(objects, expectedGroupId, DetailsProperty.GROUP);
    }

    /**
     * Assert that certain objects either belong to a certain group
     * or have a certain owner.
     * @param testedObjects some model objects to test for properties
     * @param id expected id of the property object (of GROUP or OWNER)
     * @param property property to examine the testedObjects for (can be GROUP or OWNER)
     * @throws ServerError if query fails
     */
    protected void verifyObjectProperty(Collection<? extends IObject> testedObjects, long id, DetailsProperty property)
            throws ServerError {
        if (testedObjects.isEmpty()) {
            throw new IllegalArgumentException("must assert about some objects");
        }
        for (final IObject testedObject : testedObjects) {
            final String testedObjectName = testedObject.getClass().getName() + '[' + testedObject.getId().getValue() + ']';
            final String query = "SELECT details." + property + ".id FROM " +
                    testedObject.getClass().getSuperclass().getSimpleName() + " WHERE id = :id";
            final Parameters params = new ParametersI().addId(testedObject.getId());
            final List<List<RType>> results = root.getSession().getQueryService().projection(query, params, ALL_GROUPS_CONTEXT);
            final long actualId = ((RLong) results.get(0).get(0)).getValue();
            Assert.assertEquals(actualId, id, testedObjectName);
        }
    }

    /**
     * Assert that the given object is owned by the given owner.
     * @param object a model object
     * @param expectedOwner a user's event context
     * @throws ServerError unexpected
     */
    protected void assertOwnedBy(IObject object, EventContext expectedOwner) throws ServerError {
        assertOwnedBy(Collections.singleton(object), expectedOwner);
    }

    /**
     * Assert that the given objects are owned by the given owner.
     * @param objects some model objects
     * @param expectedOwner a user's event context
     * @throws ServerError unexpected
     */
    protected void assertOwnedBy(Collection<? extends IObject> objects, EventContext expectedOwner) throws ServerError {
        verifyObjectProperty(objects, expectedOwner.userId, DetailsProperty.OWNER);
    }

    /**
     * Creates a new group and experimenter and returns the event context.
     *
     * @param perms
     *            The permissions level.
     * @param owner
     *            Pass <code>true</code> to indicate that the new user is an
     *            owner of the group, <code>false</code> otherwise.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected EventContext newUserAndGroup(String perms, boolean owner)
            throws Exception {
        return newUserAndGroup(new PermissionsI(perms), owner);
    }

    /**
     * Creates a new group and experimenter and returns the event context.
     *
     * @param perms
     *            The permissions level.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected EventContext newUserAndGroup(String perms) throws Exception {
        return newUserAndGroup(new PermissionsI(perms), false);
    }

    /**
     * Creates a new group and experimenter and returns the event context.
     *
     * @param perms
     *            The permissions level.
     * @param owner
     *            Pass <code>true</code> to indicate that the new user is an
     *            owner of the group, <code>false</code> otherwise.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected EventContext newUserAndGroup(Permissions perms, boolean owner)
            throws Exception {
        IAdminPrx rootAdmin = root.getSession().getAdminService();
        String uuid = UUID.randomUUID().toString();
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(perms);
        g = new ExperimenterGroupI(rootAdmin.createGroup(g), false);
        return newUserInGroup(g, owner);
    }

    /**
     * Creates a new group and experimenter and returns the event context.
     *
     * @param perms
     *            The permissions level.
     * @param experimenterId
     *            The identifier of the experimenter. @ * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected ExperimenterGroup newGroupAddUser(Permissions perms,
            long experimenterId) throws Exception {
        return newGroupAddUser(perms, Arrays.asList(experimenterId), false);
    }

    /**
     * Creates a new group and experimenter and returns the event context.
     *
     * @param perms
     *            The permissions level.
     * @param experimenterId
     *            The identifier of the experimenter.
     * @param owner
     *            Pass <code>true</code> to indicate that the new user is an
     *            owner of the group, <code>false</code> otherwise.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected ExperimenterGroup newGroupAddUser(Permissions perms,
            long experimenterId, boolean owner) throws Exception {
        return newGroupAddUser(perms, Arrays.asList(experimenterId), owner);
    }

    /**
     * Creates a new group and experimenter and returns the event context.
     *
     * @param perms
     *            The permissions level.
     * @param experimenterIds
     *            The identifier of the experimenters.
     * @param owner
     *            Pass <code>true</code> to indicate that the new user is an
     *            owner of the group, <code>false</code> otherwise.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected ExperimenterGroup newGroupAddUser(Permissions perms,
            List<Long> experimenterIds, boolean owner) throws Exception {
        IAdminPrx rootAdmin = root.getSession().getAdminService();
        String uuid = UUID.randomUUID().toString();
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(perms);
        g = new ExperimenterGroupI(rootAdmin.createGroup(g), false);
        return addUsers(g, experimenterIds, owner);
    }

    protected ExperimenterGroup addUsers(ExperimenterGroup g,
            List<Long> experimenterIds, boolean owner) throws Exception {
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
     * @param perms
     *            The permissions level.
     * @param experimenterId
     *            The identifier of the experimenters.
     * @param owner
     *            Pass <code>true</code> to indicate that the new user is an
     *            owner of the group, <code>false</code> otherwise.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected ExperimenterGroup newGroupAddUser(String perms,
            List<Long> experimenterIds, boolean owner) throws Exception {
        return newGroupAddUser(new PermissionsI(perms), experimenterIds, owner);
    }

    /**
     * Creates a new group and experimenter and returns the event context.
     *
     * @param perms
     *            The permissions level.
     * @param experimenterId
     *            The identifier of the experimenters.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected ExperimenterGroup newGroupAddUser(String perms,
            List<Long> experimenterIds) throws Exception {
        return newGroupAddUser(new PermissionsI(perms), experimenterIds, false);
    }

    /**
     * Creates a new group and experimenter and returns the event context.
     *
     * @param perms
     *            The permissions level.
     * @param experimenterId
     *            The identifier of the experimenter.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected ExperimenterGroup newGroupAddUser(String perms,
            long experimenterId) throws Exception {
        return newGroupAddUser(new PermissionsI(perms), experimenterId);
    }

    /**
     * Creates a new group and experimenter and returns the event context.
     *
     * @param perms
     *            The permissions level.
     * @param experimenterId
     *            The identifier of the experimenter.
     * @param owner
     *            Pass <code>true</code> to indicate that the new user is an
     *            owner of the group, <code>false</code> otherwise.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected ExperimenterGroup newGroupAddUser(String perms,
            long experimenterId, boolean owner) throws Exception {
        return newGroupAddUser(new PermissionsI(perms), experimenterId, owner);
    }

    /**
     * Creates a new user in the current group.
     *
     * @return
     */
    protected EventContext newUserInGroup() throws Exception {
        EventContext ec = client.getSession().getAdminService()
                .getEventContext();
        return newUserInGroup(ec);
    }

    /**
     * Takes the {@link EventContext} from another user and creates a new user
     * in the same group as that user is currently logged in to.
     *
     * @param previousUser
     *            The context of the previous user.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected EventContext newUserInGroup(EventContext previousUser)
            throws Exception {
        return newUserInGroup(previousUser, false);
    }

    /**
     * Takes the {@link EventContext} from another user and creates a new user
     * in the same group as that user is currently logged in to.
     *
     * @param previousUser
     *            The context of the previous user.
     * @param owner
     *            Pass <code>true</code> to indicate that the new user is an
     *            owner of the group, <code>false</code> otherwise.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected EventContext newUserInGroup(EventContext previousUser,
            boolean owner) throws Exception {
        ExperimenterGroup eg = new ExperimenterGroupI(previousUser.groupId,
                false);
        return newUserInGroup(eg, owner);
    }

    /**
     * Creates a new user in the specified group.
     *
     * @param group
     *            The group to add the user to.
     * @param owner
     *            Pass <code>true</code> to indicate that the new user is an
     *            owner of the group, <code>false</code> otherwise.
     * @return The context.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected EventContext newUserInGroup(ExperimenterGroup group, boolean owner)
            throws Exception {
        IAdminPrx rootAdmin = root.getSession().getAdminService();
        group = rootAdmin.getGroup(group.getId().getValue());

        String uuid = UUID.randomUUID().toString();
        Experimenter e = new ExperimenterI();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring("integration"));
        e.setLastName(omero.rtypes.rstring("tester"));
        e.setLdap(omero.rtypes.rbool(false));
        long id = newUserInGroupWithPassword(e, group, uuid);
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
     * Creates the specified user in the specified groups. Also adds the user
     * to the default user group. Requires a password.
     *
     * @param experimenter The pre-populated Experimenter object.
     * @param groups The target groups.
     * @param password The user password.
     * @return long The created user ID.
     */
    protected long newUserInGroupWithPassword(Experimenter experimenter,
            List<ExperimenterGroup> groups, String password) throws Exception {
        IAdminPrx rootAdmin = root.getSession().getAdminService();
        ExperimenterGroup userGroup = rootAdmin.lookupGroup(roles.userGroupName);
        return rootAdmin.createExperimenterWithPassword(experimenter,
                omero.rtypes.rstring(password), userGroup, groups);
    }

    /**
     * Creates the specified user in the specified group. Also adds the user
     * to the default user group. Requires a password.
     *
     * @param experimenter The pre-populated Experimenter object.
     * @param group The target group.
     * @param password The user password.
     * @return long The created user ID.
     */
    protected long newUserInGroupWithPassword(Experimenter experimenter,
            ExperimenterGroup group, String password) throws Exception {
        return newUserInGroupWithPassword(experimenter,
                Lists.newArrayList(group), password);
    }

    /**
     * Create a fileset with a template prefix appropriate for the user created
     * by {@link #setUp()}. Does not access the OMERO API or persist the new
     * fileset.
     *
     * @return a new fileset
     */
    protected Fileset newFileset() {
        final Fileset fileset = new FilesetI();
        fileset.setTemplatePrefix(omero.rtypes.rstring(this.userFsDir
                + System.currentTimeMillis() + FsFile.separatorChar));
        return fileset;
    }

    /**
     * Logs in the user.
     *
     * @param g
     *            The group to log into.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected EventContext loginUser(ExperimenterGroup g) throws Exception {
        EventContext ec = iAdmin.getEventContext();
        omero.client client = newOmeroClient();
        client.createSession(ec.userName, ec.userName);
        client.getSession().setSecurityContext(
                new ExperimenterGroupI(g.getId(), false));
        return init(client);
    }

    /**
     * Logs in the user.
     *
     * @param ownerEc
     *            The context of the user.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected void loginUser(EventContext ownerEc) throws Exception {
        loginUser(ownerEc.userName);
    }

    /**
     * Logs in the user.
     *
     * @param ownerName
     *            The OME name of the user.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected void loginUser(String ownerName) throws Exception {
        omero.client client = newOmeroClient();
        client.createSession(ownerName, ownerName);
        init(client);
    }

    /**
     * Logs in the user.
     *
     * @param ownerEc
     *            The context of the user.
     * @param g
     *            The group to log into.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected void loginUser(EventContext ownerEc, ExperimenterGroup g) throws Exception {
        final omero.client client = newOmeroClient();
        client.createSession(ownerEc.userName, ownerEc.userName);
        client.getSession().setSecurityContext(
                new ExperimenterGroupI(g.getId(), false));
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
     * Creates a new {@link omero.client} for root based on the
     * {@link EventContext}
     */
    protected void logRootIntoGroup(EventContext ec) throws Exception {
        logRootIntoGroup(ec.groupId);
    }

    /**
     * Creates a new {@link omero.client} for root based on the group
     * identifier.
     */
    protected void logRootIntoGroup(long groupId) throws Exception {
        omero.client rootClient = newRootOmeroClient();
        rootClient.getSession().setSecurityContext(
                new ExperimenterGroupI(groupId, false));
        init(rootClient);
    }

    /**
     * Makes the current user an owner of the current group.
     */
    protected void makeGroupOwner() throws Exception {
        EventContext ec = client.getSession().getAdminService()
                .getEventContext();
        IAdminPrx rootAdmin = root.getSession().getAdminService();
        rootAdmin.setGroupOwner(new ExperimenterGroupI(ec.groupId, false),
                new ExperimenterI(ec.userId, false));

        disconnect();
        init(ec); // Create new session with the added privileges
    }

    /**
     * Saves the current client before calling {@link #clean()} and returns it
     * to the user.
     */
    protected omero.client disconnect() throws Exception {
        omero.client oldClient = client;
        clean();
        client = null;
        return oldClient;
    }

    /**
     * If {@link #client} is non-null, destroys the client and nulls all fields
     * which were set on creation.
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
        factoryEncrypted = c.createSession(ec.userName, ec.userName);
        return init(c);
    }

    /**
     * Resets the client and return the event context.
     *
     * @param client
     *            The client to handle.
     * @return The event context to handle.
     * @throws Exception
     */
    protected EventContext init(omero.client client) throws Exception {

        clean();

        this.client = client;
        factory = client.getSession();
        EventContext ctx = null;
        try {
            iQuery = factory.getQueryService();
            iUpdate = factory.getUpdateService();
            iAdmin = factory.getAdminService();
            iPix = factory.getPixelsService();
            roles = iAdmin.getSecurityRoles();
            mmFactory = new ModelMockFactory(root.getSession().getTypesService());
            ctx = iAdmin.getEventContext();
        } catch (SecurityViolation sv) {
            mmFactory = null;
            iAdmin = null;
            iQuery = null;
            iUpdate = null;
            iPix = null;
        }

        return ctx;
    }

    /**
     * Compares the passed rendering definitions.
     *
     * @param def1
     *            The first rendering definition to handle.
     * @param def2
     *            The second rendering definition to handle.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected void compareRenderingDef(RenderingDef def1, RenderingDef def2)
            throws Exception {
        Assert.assertNotNull(def1);
        Assert.assertNotNull(def2);
        Assert.assertEquals(def1.getDefaultZ().getValue(),
                def2.getDefaultZ().getValue());
        Assert.assertEquals(def1.getDefaultT().getValue(),
                def2.getDefaultT().getValue());
        Assert.assertEquals(def1.getModel().getValue().getValue(),
                def2.getModel().getValue().getValue());
        QuantumDef q1 = def1.getQuantization();
        QuantumDef q2 = def2.getQuantization();
        Assert.assertNotNull(q1);
        Assert.assertNotNull(q2);
        Assert.assertEquals(q1.getBitResolution().getValue(),
                q2.getBitResolution().getValue());
        Assert.assertEquals(q1.getCdStart().getValue(), q2.getCdStart().getValue());
        Assert.assertEquals(q1.getCdEnd().getValue(), q2.getCdEnd().getValue());
        List<ChannelBinding> channels1 = def1.copyWaveRendering();
        List<ChannelBinding> channels2 = def2.copyWaveRendering();
        Assert.assertNotNull(channels1);
        Assert.assertNotNull(channels2);
        Assert.assertEquals(channels1.size(), channels2.size());
        Iterator<ChannelBinding> i = channels1.iterator();
        ChannelBinding c1, c2;
        int index = 0;
        while (i.hasNext()) {
            c1 = i.next();
            c2 = channels2.get(index);
            Assert.assertEquals(c1.getAlpha().getValue(), c2.getAlpha().getValue());
            Assert.assertEquals(c1.getRed().getValue(), c2.getRed().getValue());
            Assert.assertEquals(c1.getGreen().getValue(), c2.getGreen().getValue());
            Assert.assertEquals(c1.getBlue().getValue(), c2.getBlue().getValue());
            Assert.assertEquals(c1.getCoefficient().getValue(), c2.getCoefficient()
                    .getValue());
            Assert.assertEquals(c1.getFamily().getValue().getValue(),
                    c2.getFamily().getValue().getValue());
            Assert.assertEquals(c1.getInputStart().getValue(), c2.getInputStart()
                    .getValue());
            Assert.assertEquals(c1.getInputEnd().getValue(), c2.getInputEnd()
                    .getValue());
            Boolean b1 = Boolean.valueOf(c1.getActive().getValue());
            Boolean b2 = Boolean.valueOf(c2.getActive().getValue());
            Assert.assertEquals(b1, b2);
            b1 = Boolean.valueOf(c1.getNoiseReduction().getValue());
            b2 = Boolean.valueOf(c2.getNoiseReduction().getValue());
            Assert.assertEquals(b1, b2);
            //Check lut
            if (c1.getLookupTable() != null && c2.getLookupTable() != null) {
                Assert.assertEquals(c1.getLookupTable().getValue(),
                        c2.getLookupTable().getValue());
            }
        }
    }

    /**
     * Helper method to load the wells.
     *
     * @param plateID
     *            The identifier of the plate.
     * @param pixels
     *            Pass <code>true</code> to load the pixels, <code>false</code>
     *            otherwise.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @SuppressWarnings("unchecked")
    protected List<Well> loadWells(long plateID, boolean pixels)
            throws Exception {
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
        return (List<Well>) (List<?>) iQuery.findAllByQuery(sb.toString(),
                param);
    }

    /**
     * Helper method to load a well sample with its well and plate intact (and
     * possibly a screen if one exists) for the given pixels.
     *
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
        Assert.assertEquals(1, results.size());
        WellSample ws = (WellSample) results.get(0);
        Assert.assertNotNull(ws);
        return ws;
    }

    /**
     * Helper method to load the Experiment which is is associated with the
     * pixels argument via Image.
     *
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
        Assert.assertEquals(1, results.size());
        Experiment e = (Experiment) results.get(0);
        Assert.assertNotNull(e);
        return e;
    }

    /**
     * @return a repository rooted at a directory named <q>ManagedRepository</q>
     * @throws ServerError if the repository map could not be retrieved
     */
    protected RepositoryPrx getManagedRepository() throws ServerError {
        final RepositoryMap repos = factory.sharedResources().repositories();
        int index = repos.descriptions.size();
        while (--index >= 0) {
            if ("ManagedRepository".equals(repos.descriptions.get(index).getName().getValue())) {
                return repos.proxies.get(index);
            }
        }
        throw new RuntimeException("no managed repository");
    }

    /**
     * Provides a simple Python script with valid syntax.
     * @return the content of an uploadable Python script
     * @throws IOException if the simple script cannot be read
     */
    protected String getPythonScript() throws IOException {
        if (pythonScript == null) {
            final File scriptFile = ResourceUtils.getFile("classpath:minimal-script.py");
            pythonScript = Files.toString(scriptFile, StandardCharsets.UTF_8);
        }
        return pythonScript;
    }

    /**
     * Makes sure that the passed object exists.
     *
     * @param obj
     *            The object to handle.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected void assertExists(IObject obj) throws Exception {
        IObject copy = iQuery.find(obj.getClass().getSimpleName(), obj.getId()
                .getValue());
        Assert.assertNotNull(copy,
                String.format("%s:%s", obj.getClass().getName(), obj.getId()
                        .getValue()) + " is missing!");
    }

    protected void assertAllExist(IObject... obj) throws Exception {
        for (IObject iObject : obj) {
            assertExists(iObject);
        }
    }

    protected void assertAllExist(Iterable<? extends IObject> obj) throws Exception {
        for (IObject iObject : obj) {
            assertExists(iObject);
        }
    }

    protected void assertExists(String className, Long id) throws ServerError {
        assertAllExist(className, Collections.singletonList(id));
    }

    protected void assertAllExist(String className, Collection<Long> ids) throws ServerError {
        final String hql = "SELECT COUNT(*) FROM " + className + " WHERE id IN (:ids)";
        final List<List<RType>> results = iQuery.projection(hql, new ParametersI().addIds(ids));
        final long count = ((RLong) results.get(0).get(0)).getValue();
        Assert.assertEquals(count, ids.size());
    }

    /**
     * Makes sure that the passed object does not exist.
     *
     * @param obj
     *            The object to handle.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected void assertDoesNotExist(IObject obj) throws Exception {
        IObject copy = iQuery.find(obj.getClass().getSimpleName(), obj.getId()
                .getValue());
        Assert.assertNull(copy,
                String.format("%s:%s", obj.getClass().getName(), obj.getId()
                        .getValue()) + " still exists!");
    }

    protected void assertNoneExist(IObject... obj) throws Exception {
        for (IObject iObject : obj) {
            assertDoesNotExist(iObject);
        }
    }

    protected void assertNoneExist(Iterable<? extends IObject> obj) throws Exception {
        for (IObject iObject : obj) {
            assertDoesNotExist(iObject);
        }
    }

    protected void assertDoesNotExist(String className, Long id) throws ServerError {
        assertNoneExist(className, Collections.singletonList(id));
    }

    protected void assertNoneExist(String className, Collection<Long> ids) throws ServerError {
        final String hql = "SELECT COUNT(*) FROM " + className + " WHERE id IN (:ids)";
        final List<List<RType>> results = iQuery.projection(hql, new ParametersI().addIds(ids));
        final long count = ((RLong) results.get(0).get(0)).getValue();
        Assert.assertEquals(count, 0);
    }

    /**
     * Imports the specified OME-XML file and returns the pixels set if
     * successfully imported.
     *
     * @param file
     *            The file to import.
     * @param format
     *            The format of the file to import.
     * @return The collection of imported pixels set.
     * @throws Exception
     *             Thrown if an error occurred while encoding the image.
     */
    protected List<Pixels> importFile(File file, String format)
            throws Throwable {
        return importFile(importer, file, format, false, null);
    }

    /**
     * Imports the specified OME-XML file and returns the pixels set if
     * successfully imported.
     *
     * @param file
     *            The file to import.
     * @param format
     *            The format of the file to import.
     * @return The collection of imported pixels set.
     * @throws Throwable
     *             Thrown if an error occurred while encoding the image.
     */
    protected List<Pixels> importFile(File file, String format, boolean metadata)
            throws Throwable {
        return importFile(importer, file, format, metadata, null);
    }

    /**
     * Imports the specified OME-XML file and returns the pixels set if
     * successfully imported.
     *
     * @param importer
     *            The metadataStore to use.
     * @param file
     *            The file to import.
     * @param format
     *            The format of the file to import.
     * @return The collection of imported pixels set.
     * @throws Throwable
     *             Thrown if an error occurred while encoding the image.
     */
    protected List<Pixels> importFile(OMEROMetadataStoreClient importer,
            File file, String format) throws Throwable {
        return importFile(importer, file, format, false, null);
    }

    /**
     * Imports the specified OME-XML file and returns the pixels set if
     * successfully imported.
     *
     * @param importer
     *            The metadataStore to use.
     * @param file
     *            The file to import.
     * @param target
     *            The container where to import the image.
     * @return The collection of imported pixels set.
     * @throws Throwable
     *             Thrown if an error occurred while encoding the image.
     */
    protected List<Pixels> importFile(File file, String format, IObject target)
            throws Throwable {
        return importFile(importer, file, format, false, target);
    }

    /**
     * Imports the specified OME-XML file and returns the pixels set if
     * successfully imported.
     *
     * @param importer
     *            The metadataStore to use.
     * @param file
     *            The file to import.
     * @param format
     *            The format of the file to import.
     * @param metadata
     *            Pass <code>true</code> to only import the metadata,
     *            <code>false</code> otherwise.
     * @return The collection of imported pixels set.
     * @throws Throwable
     *             Thrown if an error occurred while encoding the image.
     */
    protected List<Pixels> importFile(OMEROMetadataStoreClient importer,
            File file, String format, boolean metadata) throws Throwable {
        return importFile(importer, file, format, metadata, null);
    }

    /**
     * Imports the specified OME-XML file and returns the pixels set if
     * successfully imported.
     *
     * @param importer
     *            The metadataStore to use.
     * @param file
     *            The file to import.
     * @param format
     *            The format of the file to import.
     * @param metadata
     *            Pass <code>true</code> to only import the metadata,
     *            <code>false</code> otherwise.
     * @return The collection of imported pixels set.
     * @throws Throwable
     *             Thrown if an error occurred while encoding the image.
     */
    protected List<Pixels> importFile(OMEROMetadataStoreClient importer,
            File file, String format, boolean metadata, IObject target)
            throws Throwable {
        if (importer == null) {
            importer = createImporter();
        }        
        String[] paths = new String[1];
        paths[0] = file.getAbsolutePath();
        ImportConfig config = new ImportConfig();
        OMEROWrapper reader = new OMEROWrapper(config);
        IObserver o = new IObserver() {
            public void update(IObservable importLibrary, ImportEvent event) {
                if (event instanceof ErrorHandler.EXCEPTION_EVENT) {
                    Exception ex = ((ErrorHandler.EXCEPTION_EVENT) event).exception;
                    if (ex instanceof RuntimeException) {
                        throw (RuntimeException) ex;
                    } else {
                        throw new RuntimeException(ex);
                    }
                }
            }
        };
        ImportCandidates candidates = new ImportCandidates(reader, paths, o);

        ImportLibrary library = new ImportLibrary(importer, reader);
        library.addObserver(o);

        ImportContainer ic = candidates.getContainers().get(0);
        // new ImportContainer(
        // file, null, target, false, null, null, null, null);
        ic.setUserSpecifiedName(format);
        ic.setTarget(target);
        // ic = library.uploadFilesToRepository(ic);
        List<Pixels> pixels = library.importImage(ic, 0, 0, 1);
        Assert.assertNotNull(pixels);
        Assert.assertTrue(CollectionUtils.isNotEmpty(pixels));
        return pixels;
    }

    /**
     * Basic asynchronous delete command. Used in order to reduce the number of
     * places that we do the same thing in case the API changes.
     *
     * @param dc
     *            The command to handle.
     * @throws ApiUsageException
     * @throws ServerError
     * @throws InterruptedException
     */
    protected String delete(omero.client c, Delete2... dc)
            throws ApiUsageException, ServerError, InterruptedException {
        return delete(true, c, dc);
    }

    /**
     * Basic asynchronous delete command. Used in order to reduce the number of
     * places that we do the same thing in case the API changes.
     *
     * @param passes
     *            Pass <code>true</code> to indicate that no error found in
     *            report, <code>false</code> otherwise.
     * @param dc
     *            The command to handle.
     * @param strict
     *            whether or not the method should succeed.
     * @throws ApiUsageException
     * @throws ServerError
     * @throws InterruptedException
     */
    protected String delete(boolean passes, omero.client c, Delete2... dc)
            throws ApiUsageException, ServerError, InterruptedException {

        callback(passes, c, dc);
        return "ok";
    }

    /**
     * Asynchronous command for a single delete, this means a single report is
     * returned for testing.
     *
     * @param dc
     *            The SINGLE command to handle.
     * @throws ApiUsageException
     * @throws ServerError
     * @throws InterruptedException
     */
    protected Delete2Response singleDeleteWithReport(omero.client c, Delete2 dc)
            throws ApiUsageException, ServerError, InterruptedException {
        return deleteWithReports(c, dc)[0];
    }

    /**
     * Asynchronous command for delete, report array is returned.
     *
     * @param dc
     *            The command to handle.
     * @throws ApiUsageException
     * @throws ServerError
     * @throws InterruptedException
     */
    private Delete2Response[] deleteWithReports(omero.client c, Delete2... dc)
            throws ApiUsageException, ServerError, InterruptedException {
        CmdCallbackI cb = callback(true, c, dc);
        // If the above passes, then we know it's not an ERR
        DoAllRsp all = (DoAllRsp) cb.getResponse();
        Delete2Response[] reports = new Delete2Response[all.responses.size()];
        for (int i = 0; i < reports.length; i++) {
            reports[i] = (Delete2Response) all.responses.get(i);
        }
        return reports;
    }

    /**
     * Create a single image with binary.
     *
     * After recent changes on the server to check for existing binary data for
     * pixels, many resetDefaults methods tested below began returning null
     * since {@link omero.LockTimeout} exceptions were being thrown server-side.
     * By using omero.client.forEachTile, we can set the necessary data easily.
     *
     * @see ticket:5755
     */
    protected Image createBinaryImage() throws Exception {
        Image image = mmFactory.createImage();
        image = (Image) iUpdate.saveAndReturnObject(image);
        return createBinaryImage(image);
    }

    /**
     * Create a single image with binary.
     *
     * After recent changes on the server to check for existing binary data for
     * pixels, many resetDefaults methods tested below began returning null
     * since {@link omero.LockTimeout} exceptions were being thrown server-side.
     * By using omero.client.forEachTile, we can set the necessary data easily.
     *
     * @param sizeX The number of pixels along the X-axis.
     * @param sizeY The number of pixels along the Y-axis.
     * @param sizeZ The number of z-sections.
     * @param sizeT The number of timepoints.
     * @param sizeC The number of channels.
     * @see ticket:5755
     */
    protected Image createBinaryImage(int sizeX, int sizeY, int sizeZ,
            int sizeT, int sizeC) throws Exception {
        Image image = mmFactory.createImage(sizeX, sizeY, sizeZ, sizeT,
                sizeC, ModelMockFactory.UINT16);
        image = (Image) iUpdate.saveAndReturnObject(image);
        return createBinaryImage(image);
    }

    /**
     * Create the binary data for the given image.
     */
    protected Image createBinaryImage(Image image) throws Exception {
        Pixels pixels = image.getPrimaryPixels();
        // Image
        List<Long> ids = new ArrayList<Long>();
        ids.add(image.getId().getValue());
        // method already tested

        // first write to the image
        omero.util.RPSTileLoop loop = new omero.util.RPSTileLoop(
                client.getSession(), pixels);
        loop.forEachTile(256, 256, new omero.util.TileLoopIteration() {
            public void run(omero.util.TileData data, int z, int c, int t,
                    int x, int y, int tileWidth, int tileHeight, int tileCount) {
                data.setTile(new byte[tileWidth * tileHeight * 8], z, c, t, x,
                        y, tileWidth, tileHeight);
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
     * @param parent1
     *            The object to link the annotation to.
     * @param parent2
     *            The object to link the annotation to if not null.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected List<Long> createSharableAnnotation(IObject parent1,
            IObject parent2) throws Exception {
        // Copying to a proxy to prevent issues with parent.annotationLinks
        // becoming stale on multiple copies.
        parent1 = parent1.proxy();
        if (parent2 != null) {
            parent2 = parent2.proxy();
        }

        // creation already tested in UpdateServiceTest
        List<Long> ids = new ArrayList<Long>();
        TagAnnotation c = new TagAnnotationI();
        c.setTextValue(omero.rtypes.rstring("tag"));
        c = (TagAnnotation) iUpdate.saveAndReturnObject(c);
        ids.add(c.getId().getValue());

        TermAnnotation t = new TermAnnotationI();
        t.setTermValue(omero.rtypes.rstring("term"));
        t = (TermAnnotation) iUpdate.saveAndReturnObject(t);
        ids.add(t.getId().getValue());

        OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory
                .createOriginalFile());
        Assert.assertNotNull(of);
        FileAnnotation f = new FileAnnotationI();
        f.setFile(of);
        f = (FileAnnotation) iUpdate.saveAndReturnObject(f);
        ids.add(f.getId().getValue());

		MapAnnotation ma = new MapAnnotationI();
		List<NamedValue> values = new ArrayList<NamedValue>();
		for (int i = 0; i < 3; i++)
			values.add(new NamedValue("name " + i, "value " + i));
		ma.setMapValue(values);
		ma = (MapAnnotation) iUpdate.saveAndReturnObject(ma);
		ids.add(ma.getId().getValue());
        
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
            link = new ImageAnnotationLinkI();
            link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
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
                link = new ImageAnnotationLinkI();
                link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
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
            link = new ProjectAnnotationLinkI();
            link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
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
                link = new ProjectAnnotationLinkI();
                link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
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
            link = new DatasetAnnotationLinkI();
            link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
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
                link = new DatasetAnnotationLinkI();
                link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
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
            link = new PlateAnnotationLinkI();
            link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
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
                link = new PlateAnnotationLinkI();
                link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
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
            link = new ScreenAnnotationLinkI();
            link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
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
                link = new ScreenAnnotationLinkI();
                link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
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
            link = new WellAnnotationLinkI();
            link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
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
                link = new WellAnnotationLinkI();
                link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
                link.setParent((Well) parent2);
                links.add(link);
            }
        } else if (parent1 instanceof PlateAcquisition) {
            PlateAcquisitionAnnotationLink link = new PlateAcquisitionAnnotationLinkI();
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
            link = new PlateAcquisitionAnnotationLinkI();
            link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
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
                link = new PlateAcquisitionAnnotationLinkI();
                link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
                link.setParent((PlateAcquisition) parent2);
                links.add(link);
            }
        } else if (parent1 instanceof Detector) {
            DetectorAnnotationLink link = new DetectorAnnotationLinkI();
            link.setChild(new TagAnnotationI(c.getId().getValue(), false));
            link.setParent((Detector) parent1);
            links.add(link);
            link = new DetectorAnnotationLinkI();
            link.setChild(new TermAnnotationI(t.getId().getValue(), false));
            link.setParent((Detector) parent1);
            links.add(link);
            link = new DetectorAnnotationLinkI();
            link.setChild(new FileAnnotationI(f.getId().getValue(), false));
            link.setParent((Detector) parent1);
            links.add(link);
            link = new DetectorAnnotationLinkI();
            link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
            link.setParent((Detector) parent1);
            links.add(link);
            if (parent2 != null) {
                link.setChild(new TagAnnotationI(c.getId().getValue(), false));
                link.setParent((Detector) parent2);
                links.add(link);
                link = new DetectorAnnotationLinkI();
                link.setChild(new TermAnnotationI(t.getId().getValue(), false));
                link.setParent((Detector) parent2);
                links.add(link);
                link = new DetectorAnnotationLinkI();
                link.setChild(new FileAnnotationI(f.getId().getValue(), false));
                link.setParent((Detector) parent2);
                links.add(link);
                link = new DetectorAnnotationLinkI();
                link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
                link.setParent((Detector) parent2);
                links.add(link);
            }
        } else if (parent1 instanceof Detector) {
            DetectorAnnotationLink link = new DetectorAnnotationLinkI();
            link.setChild(new TagAnnotationI(c.getId().getValue(), false));
            link.setParent((Detector) parent1);
            links.add(link);
            link = new DetectorAnnotationLinkI();
            link.setChild(new TermAnnotationI(t.getId().getValue(), false));
            link.setParent((Detector) parent1);
            links.add(link);
            link = new DetectorAnnotationLinkI();
            link.setChild(new FileAnnotationI(f.getId().getValue(), false));
            link.setParent((Detector) parent1);
            links.add(link);
            link = new DetectorAnnotationLinkI();
            link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
            link.setParent((Detector) parent1);
            links.add(link);
            if (parent2 != null) {
                link.setChild(new TagAnnotationI(c.getId().getValue(), false));
                link.setParent((Detector) parent2);
                links.add(link);
                link = new DetectorAnnotationLinkI();
                link.setChild(new TermAnnotationI(t.getId().getValue(), false));
                link.setParent((Detector) parent2);
                links.add(link);
                link = new DetectorAnnotationLinkI();
                link.setChild(new FileAnnotationI(f.getId().getValue(), false));
                link.setParent((Detector) parent2);
                links.add(link);
                link = new DetectorAnnotationLinkI();
                link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
                link.setParent((Detector) parent2);
                links.add(link);
            }
        } else if (parent1 instanceof LightSource) {
            LightSourceAnnotationLink link = new LightSourceAnnotationLinkI();
            link.setChild(new TagAnnotationI(c.getId().getValue(), false));
            link.setParent((LightSource) parent1);
            links.add(link);
            link = new LightSourceAnnotationLinkI();
            link.setChild(new TermAnnotationI(t.getId().getValue(), false));
            link.setParent((LightSource) parent1);
            links.add(link);
            link = new LightSourceAnnotationLinkI();
            link.setChild(new FileAnnotationI(f.getId().getValue(), false));
            link.setParent((LightSource) parent1);
            links.add(link);
            link = new LightSourceAnnotationLinkI();
            link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
            link.setParent((LightSource) parent1);
            links.add(link);
            if (parent2 != null) {
                link.setChild(new TagAnnotationI(c.getId().getValue(), false));
                link.setParent((LightSource) parent2);
                links.add(link);
                link = new LightSourceAnnotationLinkI();
                link.setChild(new TermAnnotationI(t.getId().getValue(), false));
                link.setParent((LightSource) parent2);
                links.add(link);
                link = new LightSourceAnnotationLinkI();
                link.setChild(new FileAnnotationI(f.getId().getValue(), false));
                link.setParent((LightSource) parent2);
                links.add(link);
                link = new LightSourceAnnotationLinkI();
                link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
                link.setParent((LightSource) parent2);
                links.add(link);
            }
        } else if (parent1 instanceof Instrument) {
            InstrumentAnnotationLink link = new InstrumentAnnotationLinkI();
            link.setChild(new TagAnnotationI(c.getId().getValue(), false));
            link.setParent((Instrument) parent1);
            links.add(link);
            link = new InstrumentAnnotationLinkI();
            link.setChild(new TermAnnotationI(t.getId().getValue(), false));
            link.setParent((Instrument) parent1);
            links.add(link);
            link = new InstrumentAnnotationLinkI();
            link.setChild(new FileAnnotationI(f.getId().getValue(), false));
            link.setParent((Instrument) parent1);
            links.add(link);
            link = new InstrumentAnnotationLinkI();
            link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
            link.setParent((Instrument) parent1);
            links.add(link);
            if (parent2 != null) {
                link.setChild(new TagAnnotationI(c.getId().getValue(), false));
                link.setParent((Instrument) parent2);
                links.add(link);
                link = new InstrumentAnnotationLinkI();
                link.setChild(new TermAnnotationI(t.getId().getValue(), false));
                link.setParent((Instrument) parent2);
                links.add(link);
                link = new InstrumentAnnotationLinkI();
                link.setChild(new FileAnnotationI(f.getId().getValue(), false));
                link.setParent((Instrument) parent2);
                links.add(link);
                link = new InstrumentAnnotationLinkI();
                link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
                link.setParent((Instrument) parent2);
                links.add(link);
            }
        } else if (parent1 instanceof OriginalFile) {
            OriginalFileAnnotationLink link = new OriginalFileAnnotationLinkI();
            link.setChild(new TagAnnotationI(c.getId().getValue(), false));
            link.setParent((OriginalFile) parent1);
            links.add(link);
            link = new OriginalFileAnnotationLinkI();
            link.setChild(new TermAnnotationI(t.getId().getValue(), false));
            link.setParent((OriginalFile) parent1);
            links.add(link);
            link = new OriginalFileAnnotationLinkI();
            link.setChild(new FileAnnotationI(f.getId().getValue(), false));
            link.setParent((OriginalFile) parent1);
            links.add(link);
            link = new OriginalFileAnnotationLinkI();
            link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
            link.setParent((OriginalFile) parent1);
            links.add(link);
            if (parent2 != null) {
                link.setChild(new TagAnnotationI(c.getId().getValue(), false));
                link.setParent((OriginalFile) parent2);
                links.add(link);
                link = new OriginalFileAnnotationLinkI();
                link.setChild(new TermAnnotationI(t.getId().getValue(), false));
                link.setParent((OriginalFile) parent2);
                links.add(link);
                link = new OriginalFileAnnotationLinkI();
                link.setChild(new FileAnnotationI(f.getId().getValue(), false));
                link.setParent((OriginalFile) parent2);
                links.add(link);
                link = new OriginalFileAnnotationLinkI();
                link.setChild(new MapAnnotationI(ma.getId().getValue(), false));
                link.setParent((OriginalFile) parent2);
                links.add(link);
            }
        } else {
            throw new UnsupportedOperationException("Unknown parent type: " + parent1);
        }
        if (links.size() > 0)
            iUpdate.saveAndReturnArray(links);
        return ids;
    }

    /**
     * Creates various non sharable annotations.
     *
     * @param parent
     *            The object to link the annotation to.
     * @param ns
     *            The name space or <code>null</code>.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected List<Long> createNonSharableAnnotation(IObject parent, String ns)
            throws Exception {
        // Copying to a proxy to prevent issues with parent.annotationLinks
        // becoming stale on multiple copies.
        parent = parent.proxy();

        // creation already tested in UpdateServiceTest
        List<Long> ids = new ArrayList<Long>();
        CommentAnnotation c = new CommentAnnotationI();
        c.setTextValue(omero.rtypes.rstring("comment"));
        if (ns != null)
            c.setNs(omero.rtypes.rstring(ns));

        c = (CommentAnnotation) iUpdate.saveAndReturnObject(c);

        LongAnnotation l = new LongAnnotationI();
        l.setLongValue(omero.rtypes.rlong(1L));
        if (ns != null)
            l.setNs(omero.rtypes.rstring(ns));

        l = (LongAnnotation) iUpdate.saveAndReturnObject(l);

        BooleanAnnotation b = new BooleanAnnotationI();
        b.setBoolValue(omero.rtypes.rbool(true));
        if (ns != null)
            b.setNs(omero.rtypes.rstring(ns));

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
        } else if (parent instanceof PlateAcquisition) {
            PlateAcquisitionAnnotationLink link = new PlateAcquisitionAnnotationLinkI();
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
        } else if (parent instanceof Detector) {
            DetectorAnnotationLink link = new DetectorAnnotationLinkI();
            link.setChild(c);
            link.setParent((Detector) parent);
            links.add(link);
            link = new DetectorAnnotationLinkI();
            link.setChild(l);
            link.setParent((Detector) parent);
            links.add(link);
            link = new DetectorAnnotationLinkI();
            link.setChild(b);
            link.setParent((Detector) parent);
            links.add(link);
        } else if (parent instanceof Instrument) {
            InstrumentAnnotationLink link = new InstrumentAnnotationLinkI();
            link.setChild(c);
            link.setParent((Instrument) parent);
            links.add(link);
            link = new InstrumentAnnotationLinkI();
            link.setChild(l);
            link.setParent((Instrument) parent);
            links.add(link);
            link = new InstrumentAnnotationLinkI();
            link.setChild(b);
            link.setParent((Instrument) parent);
            links.add(link);
        } else if (parent instanceof LightSource) {
            LightSourceAnnotationLink link = new LightSourceAnnotationLinkI();
            link.setChild(c);
            link.setParent((LightSource) parent);
            links.add(link);
            link = new LightSourceAnnotationLinkI();
            link.setChild(l);
            link.setParent((LightSource) parent);
            links.add(link);
            link = new LightSourceAnnotationLinkI();
            link.setChild(b);
            link.setParent((LightSource) parent);
            links.add(link);
        } else {
            throw new UnsupportedOperationException("Unknown parent type: " + parent);
        }
        if (links.size() > 0)
            iUpdate.saveAndReturnArray(links);
        return ids;
    }

    /**
     * Create a new unpersisted experimenter with the given field values.
     * @param omeName an OME name
     * @param firstName a first name
     * @param lastName a last time
     * @return the new experimenter
     */
    protected Experimenter createExperimenterI(String omeName, String firstName, String lastName) {
        final Experimenter experimenter = new ExperimenterI();
        experimenter.setOmeName(rtypes.rstring(omeName));
        experimenter.setFirstName(rtypes.rstring(firstName));
        experimenter.setLastName(rtypes.rstring(lastName));
        experimenter.setLdap(rtypes.rbool(false));
        return experimenter;
    }

    /**
     * Refresh a folder.
     * @param folder the folder to refresh
     * @return the same folder refreshed with its child folder and image link collections loaded
     * @throws ServerError unexpected
     */
    protected Folder returnFolder(Folder folder) throws ServerError {
        final String query =
                "FROM Folder AS f LEFT OUTER JOIN FETCH f.childFolders LEFT OUTER JOIN FETCH f.imageLinks WHERE f.id = :id";
        final Parameters params = new ParametersI().addId(folder.getId().getValue());
        return (Folder) iQuery.findByQuery(query, params);
    }

    /**
     * Save and refresh a folder.
     * @param folder the folder to save and refresh
     * @return the same folder refreshed with its child folder and image link collections loaded
     * @throws ServerError unexpected
     */
    protected Folder saveAndReturnFolder(Folder folder) throws ServerError {
        folder = (Folder) iUpdate.saveAndReturnObject(folder);
        return returnFolder(folder);
    }

    /**
     * Modifies the graph.
     *
     * @param change
     *            The object hosting information about data to modify.
     * @return See above.
     * @throws Exception
     */
    protected Response doChange(Request change) throws Exception {
        return doChange(client, factory, change, true);
    }

    /**
     * Modifies the graph.
     *
     * @param change
     *            The object hosting information about data to modify.
     * @return See above.
     * @throws Exception
     */
    protected Response doChange(Request change, long groupID) throws Exception {
        return doChange(client, factory, change, true, groupID, null);
    }

    protected Response doChange(omero.client c, ServiceFactoryPrx f,
            Request change, boolean pass) throws Exception {
        return doChange(c, f, change, pass, null, null);
    }

    protected Response doAllChanges(omero.client c, ServiceFactoryPrx f,
            boolean pass, Request... changes) throws Exception {
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
            Request change, boolean pass, Long groupID, Integer scalingFactorAdjustment) throws Exception {
        final Map<String, String> callContext = new HashMap<String, String>();
        if (groupID != null) {
            callContext.put("omero.group", "" + groupID);
        }
        final HandlePrx prx = f.submit(change, callContext);
        // assertFalse(prx.getStatus().flags.contains(State.FAILURE));
        CmdCallbackI cb = new CmdCallbackI(c, prx);
        long useScalingFactor = scalingFactor;
        if (scalingFactorAdjustment != null) {
            useScalingFactor *= scalingFactorAdjustment;
        }
        cb.loop(20, useScalingFactor);
        return assertCmd(cb, pass);
    }

    protected CmdCallbackI callback(boolean passes, omero.client c,
            omero.cmd.Request... reqs) throws ApiUsageException, ServerError,
            InterruptedException {
        DoAll all = new DoAll();
        all.requests = new ArrayList<omero.cmd.Request>();
        for (omero.cmd.Request req : reqs) {
            all.requests.add(req);
        }
        HandlePrx handle = c.getSession().submit(all);
        CmdCallbackI cb = new CmdCallbackI(c, handle);
        cb.loop(10 * reqs.length, scalingFactor); // throws on timeout
        assertCmd(cb, passes);
        return cb;
    }

    protected Response assertCmd(CmdCallbackI cb, boolean pass) {
        Status status = cb.getStatus();
        Response rsp = cb.getResponse();
        Assert.assertNotNull(rsp);
        if (pass) {
            if (rsp instanceof ERR) {
                ERR err = (ERR) rsp;
                String name = err.getClass().getSimpleName();
                Assert.fail(String.format(
                        "Found %s when pass==true: %s (%s) params=%s", name,
                        err.category, err.name, err.parameters));
            }
            Assert.assertFalse(status.flags.contains(State.FAILURE));
        } else {
            if (rsp instanceof OK) {
                OK ok = (OK) rsp;
                Assert.fail(String.format("Found OK when pass==false: %s", ok));
            }
            Assert.assertTrue(status.flags.contains(State.FAILURE));
        }
        return rsp;
    }

    /**
     * Creates a new group with the specified permissions and sets the role of
     * the user.
     *
     * @param permissions
     *            The permissions of the group.
     * @param userRole
     *            The role of the user e.g. group owner.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected void login(String permissions, int userRole) throws Exception {
        newUserAndGroup(permissions);
        switch (userRole) {
            case GROUP_OWNER:
                makeGroupOwner();
                break;
            case ADMIN:
                logRootIntoGroup();
        }
    }

    /**
     * Convenient helper function for providing Boolean arguments to TestNG tests.
     * @param argCount how many arguments the test takes
     * @return every combination of argument values
     * @see ome.testing.DataProviderBuilder#addBoolean(boolean)
     */
    private static Boolean[][] provideEveryBooleanCombination(int argCount) {
        // TODO: Once we use Guava 19 we can use Collections.nCopies and Lists.cartesianProduct instead of this manual approach.
        if (argCount < 1) {
            throw new IllegalArgumentException("argument count must be strictly positive");
        }
        final Boolean[][] testArguments = new Boolean[1 << argCount][];
        int testNum = 0;
        testArguments[testNum] = new Boolean[argCount];
        Arrays.fill(testArguments[testNum], false);
        while (++testNum < testArguments.length) {
            testArguments[testNum] = Arrays.copyOf(testArguments[testNum - 1], argCount);
            int argNum = argCount - 1;
            while (true) {
                if (testArguments[testNum][argNum]) {
                    testArguments[testNum][argNum--] = false;
                } else {
                    testArguments[testNum][argNum] = true;
                    break;
                }
            }
        }
        return testArguments;
    }

    /**
     * @return all four combinations of Boolean argument values
     */
    @DataProvider(name = "test cases using two Boolean arguments")
    public Object[][] provideTwoBooleanArguments() {
        return provideEveryBooleanCombination(2);
    }

    /**
     * @return all eight combinations of Boolean argument values
     */
    @DataProvider(name = "test cases using three Boolean arguments")
    public Object[][] provideThreeBooleanArguments() {
        return provideEveryBooleanCombination(3);
    }

    /**
     * @return all sixteen combinations of Boolean argument values
     */
    @DataProvider(name = "test cases using four Boolean arguments")
    public Object[][] provideFourBooleanArguments() {
        return provideEveryBooleanCombination(4);
    }

    /**
     * Override the Ice implicit call context with a group ID.
     * Removes the override upon closing.
     * @author m.t.b.carroll@ixod.org
     * @since 5.4.0
     */
    protected class ImplicitGroupContext implements AutoCloseable {
        /**
         * Set the implicit group context to the given group.
         * @param groupId a group ID
         */
        ImplicitGroupContext(long groupId) {
            if (client.getImplicitContext().containsKey(Login.OMERO_GROUP)) {
                throw new IllegalStateException("group context already set");
            }
            client.getImplicitContext().put(Login.OMERO_GROUP, Long.toString(groupId));
        }

        /**
         * Set the implicit group context.
         * @param groupId a group ID
         */
        ImplicitGroupContext(RLong groupId) {
            this(groupId.getValue());
        }

        @Override
        public void close() {
            if (!client.getImplicitContext().containsKey(Login.OMERO_GROUP)) {
                throw new IllegalStateException("group context no longer set");
            }
            client.getImplicitContext().remove(Login.OMERO_GROUP);
        }
    }

    /**
     * Override the Ice implicit call context with all-groups.
     * Removes the override upon closing.
     * @author m.t.b.carroll@ixod.org
     * @since 5.4.0
     */
    protected class ImplicitAllGroupsContext extends ImplicitGroupContext {
        /**
         * Set the implicit group context to all-groups.
         * @param groupId a group ID
         */
        ImplicitAllGroupsContext() {
            super(-1);
        }
    }
}
