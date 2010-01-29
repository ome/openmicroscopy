/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_ICE
#define OMERO_API_ICE

#include <omero/ModelF.ice>
#include <omero/ServicesF.ice>
#include <omero/Collections.ice>
#include <omero/Constants.ice>
#include <omero/ROMIO.ice>
#include <omero/RTypes.ice>
#include <omero/Scripts.ice>
#include <omero/Tables.ice>
#include <omero/System.ice>
#include <Glacier2/Session.ice>
#include <Ice/BuiltinSequences.ice>


module omero {

    /** The omero::api module defines all the central verbs for working with OMERO.blitz.
     *
     * <p> Arguments and return values consist of those
     * types defined in the other ice files available here. With no
     * further custom code, it is possible to interoperate with
     * OMERO.blitz simply via the definitions here. Start with the
     * ServiceFactory definition at the end of this file.</p>
     *
     * <p> Note: Using these types is significantly easier in combination with
     * the JavaDocs of the OMERO.server, specifically the ome.api
     * package. Where not further noted below, the follow mappings between
     * ome.api argument types and omero::api argument types hold: </p>
     *
     * <pre>
     *     +-----------------------+------------------------+
     *     |        ome.api        |      omero::api        |
     *     +-----------------------+------------------------+
     *     |java.lang.Class        |string                  |
     *     +-----------------------+------------------------+
     *     |java.util.Set          |java.util.List/vector   |
     *     +-----------------------+------------------------+
     *     |IPojo options (Map)    |omero::sys::ParamMap    |
     *     +-----------------------+------------------------+
     *     |If null needed         |omero::RType subclass   |
     *     +-----------------------+------------------------+
     *     |...                    |...                     |
     *     +-----------------------+------------------------+
     * </pre>
     **/
    module api {

	/**
	 * Service marker similar to ome.api.ServiceInterface. Any object which
	 * IS-A ServiceInterface but IS-NOT-A StatefulServiceInterface (below)
	 * is be definition a "stateless service"
	 **/
	interface ServiceInterface
	{
	};

	sequence<ServiceInterface*> ServiceList;

	/**
	 * Service marker for stateful services which permits the closing
	 * of a particular service before the destruction of the session.
	 **/
	["ami", "amd"] interface StatefulServiceInterface extends ServiceInterface
	{
	    /**
	     * Causes the blitz server to store the service implementation to disk
	     * to free memory. This is typically done automatically by the server
	     * when a pre-defined memory limit is reached, but can be used by the
	     * client if it clear that a stateful service will not be used for some
	     * time.
	     *
	     * Activation will happen automatically whether passivation was done
	     * manually or automatically.
	     **/
	    void passivate() throws ServerError;

	    /**
	     * Load a service implementation from disk if it was previously
	     * passivated. It is unnecessary to call this method since activation
	     * happens automatically, but calling this may prevent a short
	     * lapse when the service is first accessed after passivation.
	     *
	     * It is safe to call this method at any time, even when the service
	     * is not passivated.
	     **/
	    void activate() throws ServerError;

	    /**
	     * Frees all resources -- passivated or active -- for the given
	     * stateful service and removes its name from the object adapter.
	     * Any further method calls will fail with a Ice::NoSuchObjectException.
	     *
	     * Note: with JavaEE, the close method was called publically,
	     * and internally this called destroy(). As of the OmeroBlitz
	     * migration, this functionality has been combined.
	     **/
            void close() throws ServerError;

	    /**
	     * To free clients from tracking the mapping from session to stateful
	     * service, each stateful service can returns its own context information.
	     **/
	    idempotent omero::sys::EventContext getCurrentEventContext() throws ServerError;
	};

	// Stateless service
	// ===================================================================================

	/**
	 * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IAdmin.html">IAdmin.html</a>
	 **/
	["ami", "amd"] interface IAdmin extends ServiceInterface
	{

	    // Getters
	    idempotent bool canUpdate(omero::model::IObject obj) throws ServerError;
	    idempotent omero::model::Experimenter getExperimenter(long id) throws ServerError;
	    idempotent omero::model::Experimenter lookupExperimenter(string name) throws ServerError;
	    idempotent ExperimenterList lookupExperimenters() throws ServerError;
	    idempotent omero::model::ExperimenterGroup getGroup(long id) throws ServerError;
	    idempotent omero::model::ExperimenterGroup lookupGroup(string name) throws ServerError ;
	    idempotent ExperimenterGroupList lookupGroups() throws ServerError;
	    idempotent ExperimenterList containedExperimenters(long groupId) throws ServerError;
	    idempotent ExperimenterGroupList containedGroups(long experimenterId) throws ServerError;
	    idempotent omero::model::ExperimenterGroup getDefaultGroup(long experimenterId) throws ServerError;
	    idempotent string lookupLdapAuthExperimenter(long id) throws ServerError;
	    idempotent RList lookupLdapAuthExperimenters() throws ServerError;

	    // Mutators

	    void updateSelf(omero::model::Experimenter experimenter) throws ServerError;
	    void updateExperimenter(omero::model::Experimenter experimenter) throws ServerError;
	    void updateExperimenterWithPassword(omero::model::Experimenter experimenter,
                                                omero::RString password) throws ServerError;
	    void updateGroup(omero::model::ExperimenterGroup group) throws ServerError;
	    long createUser(omero::model::Experimenter experimenter, string group) throws ServerError;
	    long createSystemUser(omero::model::Experimenter experimenter) throws ServerError;
	    long createExperimenter(omero::model::Experimenter user,
				    omero::model::ExperimenterGroup defaultGroup, ExperimenterGroupList groups) throws ServerError;
            long createExperimenterWithPassword(omero::model::Experimenter user, omero::RString password,
				    omero::model::ExperimenterGroup defaultGroup, ExperimenterGroupList groups) throws ServerError;
	    long createGroup(omero::model::ExperimenterGroup group) throws ServerError;
	    idempotent void addGroups(omero::model::Experimenter user, ExperimenterGroupList groups) throws ServerError;
	    idempotent void removeGroups(omero::model::Experimenter user, ExperimenterGroupList groups) throws ServerError;
	    idempotent void setDefaultGroup(omero::model::Experimenter user, omero::model::ExperimenterGroup group) throws ServerError;
	    idempotent void setGroupOwner(omero::model::ExperimenterGroup group, omero::model::Experimenter owner) throws ServerError;
	    idempotent void deleteExperimenter(omero::model::Experimenter user) throws ServerError;
	    idempotent void deleteGroup(omero::model::ExperimenterGroup group) throws ServerError;
	    idempotent void changeOwner(omero::model::IObject obj, string omeName) throws ServerError;
	    idempotent void changeGroup(omero::model::IObject obj, string omeName) throws ServerError;
	    idempotent void changePermissions(omero::model::IObject obj, omero::model::Permissions perms) throws ServerError;
	    /* Leaving this non-idempotent, because of the overhead, though technically it is. */
	    Ice::BoolSeq unlock(IObjectList objects) throws ServerError;

	    // UAuth
	    idempotent void changePassword(omero::RString newPassword) throws ServerError;
	    idempotent void changeUserPassword(string omeName, omero::RString newPassword) throws ServerError;
	    idempotent void synchronizeLoginCache() throws ServerError;
	    void changeExpiredCredentials(string name, string oldCred, string newCred) throws ServerError;
	    void reportForgottenPassword(string name, string email) throws ServerError;

	    // Security Context
	    idempotent omero::sys::Roles getSecurityRoles() throws ServerError;
	    idempotent omero::sys::EventContext getEventContext() throws ServerError;
	};

	/**
	 * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IConfig.html">IConfig.html</a>
	 **/

	["ami", "amd"] interface IConfig extends ServiceInterface
	{
	    idempotent string getVersion() throws ServerError;
	    idempotent string getConfigValue(string key) throws ServerError;
	    idempotent void setConfigValue(string key, string value) throws ServerError;
	    idempotent bool setConfigValueIfEquals(string key, string value, string test) throws ServerError;
	    idempotent string getDatabaseUuid() throws ServerError;
	    idempotent omero::RTime getDatabaseTime() throws ServerError;
	    idempotent omero::RTime getServerTime() throws ServerError;
	};

	/**
	 * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IDelete.html">IDelete.html</a>
	 **/
	["ami", "amd"] interface IDelete extends omero::api::ServiceInterface
	{
	    omero::api::IObjectList checkImageDelete(long id, bool force) throws ServerError;
	    omero::api::IObjectList previewImageDelete(long id, bool force) throws ServerError;
	    void deleteImage(long id, bool force) throws ApiUsageException, ValidationException, SecurityViolation, ServerError;
	    void deleteImages(LongList ids, bool force) throws ApiUsageException, ValidationException, SecurityViolation, ServerError;
	    void deleteImagesByDataset(long datasetId, bool force) throws ApiUsageException, ValidationException, SecurityViolation, ServerError;
            void deleteSettings(long imageId) throws ServerError;
            void deletePlate(long plateId) throws ServerError;
	};

	/**
	 * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/ILdap.html">ILdap.html</a>
	 **/
	["ami", "amd"] interface ILdap extends ServiceInterface
	{
	    idempotent ExperimenterList searchAll() throws ServerError;
	    idempotent StringSet searchDnInGroups(string attr, string value) throws ServerError;
	    idempotent ExperimenterList searchByAttribute(string dn, string attribute, string value) throws ServerError;
	    idempotent ExperimenterList searchByAttributes(string dn, StringSet attributes, StringSet values) throws ServerError;
	    idempotent omero::model::Experimenter searchByDN(string userdn) throws ServerError;
	    idempotent string findDN(string username) throws ServerError;
	    idempotent omero::model::Experimenter findExperimenter(string username) throws ServerError;
	    idempotent void setDN(omero::RLong experimenterID, string dn) throws ServerError;
	    idempotent ExperimenterGroupList searchGroups() throws ServerError;
	    idempotent StringSet getReqGroups() throws ServerError;
	    idempotent StringSet getReqAttributes() throws ServerError;
	    idempotent StringSet getReqValues() throws ServerError;
	    idempotent void setReqGroups(StringSet groups) throws ServerError;
	    idempotent void setReqAttributes(StringSet attrs) throws ServerError;
	    idempotent void setReqValues(StringSet vals) throws ServerError;
	    idempotent bool getSetting() throws ServerError;
	};


	/**
	 * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IPixels.html">IPixels.html</a>
	 **/
	["ami", "amd"] interface IPixels extends ServiceInterface
	{
	    idempotent omero::model::Pixels retrievePixDescription(long pixId) throws ServerError;
	    idempotent omero::model::RenderingDef retrieveRndSettings(long pixId) throws ServerError;
	    idempotent omero::model::RenderingDef retrieveRndSettingsFor(long pixId, long userId) throws ServerError;
	    idempotent IObjectList retrieveAllRndSettings(long pixId, long userId) throws ServerError;
	    idempotent omero::model::RenderingDef loadRndSettings(long renderingSettingsId) throws ServerError;
	    void saveRndSettings(omero::model::RenderingDef rndSettings) throws ServerError;
	    idempotent int getBitDepth(omero::model::PixelsType type) throws ServerError;
	    idempotent omero::model::IObject getEnumeration(string enumClass, string value) throws ServerError;
	    idempotent IObjectList getAllEnumerations(string enumClass) throws ServerError;
	    omero::RLong copyAndResizePixels(long pixelsId,
					     omero::RInt sizeX,
					     omero::RInt sizeY,
					     omero::RInt sizeZ,
					     omero::RInt sizeT,
					     omero::sys::IntList channelList,
					     string methodology,
					     bool copyStats) throws ServerError;
	    omero::RLong copyAndResizeImage(long imageId,
					    omero::RInt sizeX,
					    omero::RInt sizeY,
					    omero::RInt sizeZ,
					    omero::RInt sizeT,
					    omero::sys::IntList channelList,
					    string methodology,
					    bool copyStats) throws ServerError;
	    omero::RLong createImage(int sizeX, int sizeY, int sizeZ, int sizeT,
				     omero::sys::IntList channelList,
				     omero::model::PixelsType pixelsType,
				     string name, string description) throws ServerError;
	    void setChannelGlobalMinMax(long pixelsId, int channelIndex, double min, double max) throws ServerError;
	};

	/**
	 * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IContainer.html">IContainer.html</a>
	 **/
	["ami", "amd"] interface IContainer extends ServiceInterface
	{
	    idempotent IObjectList loadContainerHierarchy(string rootType, omero::sys::LongList rootIds, omero::sys::Parameters options) throws ServerError;
	    idempotent IObjectList findContainerHierarchies(string rootType, omero::sys::LongList imageIds, omero::sys::Parameters options) throws ServerError;
	    //idempotent AnnotationMap findAnnotations(string rootType, omero::sys::LongList rootIds, omero::sys::LongList annotatorIds, omero::sys::Parameters options) throws ServerError;
	    idempotent ImageList getImages(string rootType, omero::sys::LongList rootIds, omero::sys::Parameters options) throws ServerError;
	    idempotent ImageList getUserImages(omero::sys::Parameters options) throws ServerError;
	    idempotent ImageList getImagesByOptions(omero::sys::Parameters options) throws ServerError;
	    idempotent omero::sys::CountMap getCollectionCount(string type, string property, omero::sys::LongList ids, omero::sys::Parameters options) throws ServerError;
	    idempotent IObjectList retrieveCollection(omero::model::IObject obj, string collectionName, omero::sys::Parameters options) throws ServerError;
	    omero::model::IObject createDataObject(omero::model::IObject obj, omero::sys::Parameters options) throws ServerError;
	    IObjectList createDataObjects(IObjectList dataObjects, omero::sys::Parameters options) throws ServerError;
	    void unlink(IObjectList links, omero::sys::Parameters options) throws ServerError;
	    IObjectList link(IObjectList links, omero::sys::Parameters options) throws ServerError;
	    omero::model::IObject updateDataObject(omero::model::IObject obj, omero::sys::Parameters options) throws ServerError;
	    IObjectList updateDataObjects(IObjectList objs, omero::sys::Parameters options) throws ServerError;
	    void deleteDataObject(omero::model::IObject obj, omero::sys::Parameters options) throws ServerError;
	    void deleteDataObjects(IObjectList objs, omero::sys::Parameters options) throws ServerError;
	};

	/**
	 * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IMetadata.html">IMetadata.html</a>
	 **/
	["ami", "amd"] interface IMetadata extends ServiceInterface
	{
	    idempotent LogicalChannelList loadChannelAcquisitionData(omero::sys::LongList ids) throws ServerError;
	    idempotent AnnotationMap loadAnnotations(string rootType, omero::sys::LongList rootIds, omero::api::StringSet annotationTypes, omero::sys::LongList annotatorIds, omero::sys::Parameters options) throws ServerError;
	    idempotent AnnotationList loadSpecifiedAnnotations(string annotationType, omero::api::StringSet include, omero::api::StringSet exclude, omero::sys::Parameters options) throws ServerError;
	    //idempotent omero::metadata::TagSetContainerList loadTagSets(long id, bool withObjects, omero::sys::Parameters options) throws ServerError;
	    //idempotent omero::metadata::TagContainerList loadTags(long id, bool withObjects, omero::sys::Parameters options) throws ServerError;
	    idempotent AnnotationMap loadTagContent(omero::sys::LongList ids, omero::sys::Parameters options) throws ServerError;
	    idempotent IObjectList loadTagSets(omero::sys::Parameters options) throws ServerError;
	    idempotent omero::sys::CountMap getTaggedObjectsCount(omero::sys::LongList ids, omero::sys::Parameters options) throws ServerError;
	    omero::RLong countSpecifiedAnnotations(string annotationType, omero::api::StringSet include, omero::api::StringSet exclude, omero::sys::Parameters options) throws ServerError;
	    idempotent AnnotationList loadAnnotation(omero::sys::LongList annotationIds) throws ServerError;
	    idempotent IObjectList loadInstrument(long id) throws ServerError;
	};

	//interface IMetadata; /* Forward definition. See omero/api/Metadata.ice */

	/**
	 * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IProjection.html">IProjection.html</a>
	 **/
	["ami", "amd"] interface IProjection extends ServiceInterface
	{
	    Ice::ByteSeq projectStack(long pixelsId,
	                              omero::model::PixelsType pixelsType,
                                  omero::constants::projection::ProjectionType algorithm,
                                  int timepoint, int channelIndex, int stepping,
                                  int start, int end) throws ServerError;
        long projectPixels(long pixelsId, omero::model::PixelsType pixelsType,
                           omero::constants::projection::ProjectionType algorithm,
                           int tStart, int tEnd,
                           omero::sys::IntList channelList, int stepping,
                           int zStart, int zEnd, string name)
                           throws ServerError;
	};


	/**
	 * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IQuery.html">IQuery.html</a>
	 **/
	["ami", "amd"] interface IQuery extends ServiceInterface
	{
	    idempotent omero::model::IObject get(string klass, long id) throws ServerError;
	    idempotent omero::model::IObject find(string klass, long id) throws ServerError;
	    idempotent IObjectList           findAll(string klass, omero::sys::Filter filter) throws ServerError;
	    idempotent omero::model::IObject findByExample(omero::model::IObject example) throws ServerError;
	    idempotent IObjectList           findAllByExample(omero::model::IObject example, omero::sys::Filter filter) throws ServerError;
	    idempotent omero::model::IObject findByString(string klass, string field, string value) throws ServerError;
	    idempotent IObjectList           findAllByString(string klass, string field, string value, bool caseSensitive, omero::sys::Filter filter) throws ServerError;
	    idempotent omero::model::IObject findByQuery(string query, omero::sys::Parameters params) throws ServerError;
	    idempotent IObjectList           findAllByQuery(string query, omero::sys::Parameters params) throws ServerError;
	    idempotent IObjectList           findAllByFullText(string klass, string query, omero::sys::Parameters params) throws ServerError;
	    idempotent omero::model::IObject refresh(omero::model::IObject iObject) throws ServerError;
	};

	/**
	 * Forward declaration; see omero/api/IRoi.ice
	 *
	 * If you receive a segfault or a bus error in Python, be sure to
	 * also import the definition "import omero_api_IRoi_ice". For
	 * more information see:
	 *
	 * <a href="http://www.zeroc.com/forums/bug-reports/3883-bus-error-under-mac-ox-10-4-icepy-3-3-0-a.html#post17120">this thread</a>
	 **/
        interface IRoi; //

	/**
	 * Forward declaration; see omero/api/IScript.ice
	 *
	 * If you receive a segfault or a bus error in Python, be sure to
	 * also import the definition "import omero_api_IScript_ice". For
	 * more information see:
	 *
	 * <a href="http://www.zeroc.com/forums/bug-reports/3883-bus-error-under-mac-ox-10-4-icepy-3-3-0-a.html#post17120">this thread</a>
	 **/
        interface IScript; //

	/**
	 * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/ISession.html">ISession.html</a>
	 **/
	["ami", "amd"] interface ISession extends ServiceInterface
	{
	    omero::model::Session createSession(omero::sys::Principal p, string credentials) throws ServerError;
	    omero::model::Session createUserSession(long timeToLiveMilliseconds, long timeToIdleMilliseconds,
						    string defaultGroup, omero::model::Permissions umask) throws ServerError;
	    omero::model::Session getSession(string sessionUuid) throws ServerError;
	    int getReferenceCount(string sessionUuid) throws ServerError;
	    omero::model::Session updateSession(omero::model::Session sess) throws ServerError;
	    int closeSession(omero::model::Session sess) throws ServerError;
	    // System users
	    omero::model::Session createSessionWithTimeout(omero::sys::Principal p,
                                                           long timeToLiveMilliseconds) throws ServerError;
	    omero::model::Session createSessionWithTimeouts(omero::sys::Principal p,
	                                                    long timeToLiveMilliseconds,
	                                                    long timeToIdleMilliseconds) throws ServerError;

	    // Environment
	    omero::RType getInput(string sess, string key) throws ServerError;
	    omero::RType getOutput(string sess, string key) throws ServerError;
	    void setInput(string sess, string key, omero::RType value) throws ServerError;
	    void setOutput(string sess, string key, omero::RType value) throws ServerError;
	    StringSet getInputKeys(string sess) throws ServerError;
	    StringSet getOutputKeys(string sess) throws ServerError;
	};

	/**
	 * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IShare.html">IShare.html<a/>
	 **/
	["ami", "amd"] interface IShare extends ServiceInterface
	{
	    void activate(long shareId) throws ServerError;
	    void deactivate() throws ServerError;
	    omero::model::Share getShare(long shareId) throws ServerError;
	    omero::sys::CountMap getMemberCount(omero::sys::LongList shareIds) throws ServerError;
	    SessionList getOwnShares(bool active) throws ServerError;
	    SessionList getMemberShares(bool active) throws ServerError;
	    SessionList getSharesOwnedBy(omero::model::Experimenter user, bool active) throws ServerError;
	    SessionList getMemberSharesFor(omero::model::Experimenter user, bool active) throws ServerError;
	    IObjectList getContents(long shareId) throws ServerError;
	    IObjectList getContentSubList(long shareId, int start, int finish) throws ServerError;
	    int getContentSize(long shareId) throws ServerError;
	    IdListMap getContentMap(long shareId) throws ServerError;

	    long createShare(string description,
			     omero::RTime expiration,
			     IObjectList items,
			     ExperimenterList exps,
			     StringSet guests,
			     bool enabled) throws ServerError;
	    void setDescription(long shareId, string description) throws ServerError;
	    void setExpiration(long shareId, omero::RTime expiration) throws ServerError;
	    void setActive(long shareId, bool active) throws ServerError;
	    void closeShare(long shareId) throws ServerError;

	    void addObjects(long shareId, IObjectList iobjects) throws ServerError;
	    void addObject(long shareId, omero::model::IObject iobject) throws ServerError;
	    void removeObjects(long shareId, IObjectList iobjects) throws ServerError;
	    void removeObject(long shareId, omero::model::IObject iobject) throws ServerError;

	    omero::sys::CountMap getCommentCount(omero::sys::LongList shareIds) throws ServerError;
	    AnnotationList getComments(long shareId) throws ServerError;
	    omero::model::TextAnnotation addComment(long shareId, string comment) throws ServerError;
	    omero::model::TextAnnotation addReply(long shareId,
						  string comment,
						  omero::model::TextAnnotation replyTo) throws ServerError;
	    void deleteComment(omero::model::Annotation comment) throws ServerError;

	    ExperimenterList getAllMembers(long shareId) throws ServerError;
	    StringSet getAllGuests(long shareId) throws ServerError;
	    StringSet getAllUsers(long shareId) throws ValidationException, ServerError;
	    void addUsers(long shareId, ExperimenterList exps) throws ServerError;
	    void addGuests(long shareId, StringSet emailAddresses) throws ServerError;
	    void removeUsers(long shareId, ExperimenterList exps) throws ServerError;
	    void removeGuests(long shareId, StringSet emailAddresses) throws ServerError;
	    void addUser(long shareId, omero::model::Experimenter exp) throws ServerError;
	    void addGuest(long shareId, string emailAddress) throws ServerError;
	    void removeUser(long shareId, omero::model::Experimenter exp) throws ServerError;
	    void removeGuest(long shareId, string emailAddress) throws ServerError;

         // Under construction
        UserMap getActiveConnections(long shareId) throws ServerError;
        UserMap getPastConnections(long shareId) throws ServerError;
        void invalidateConnection(long shareId, omero::model::Experimenter exp) throws ServerError;
        IObjectList getEvents(long shareId, omero::model::Experimenter exp, omero::RTime from, omero::RTime to) throws ServerError;
	};

	/**
	 * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/ITypes.html">ITypes.html</a>
	 **/
	["ami", "amd"] interface ITypes extends ServiceInterface
	{
	    omero::model::IObject createEnumeration(omero::model::IObject newEnum) throws ServerError;
	    idempotent omero::model::IObject getEnumeration(string type, string value) throws ServerError;
	    idempotent IObjectList allEnumerations(string type) throws ServerError;
	    omero::model::IObject updateEnumeration(omero::model::IObject oldEnum) throws ServerError;
	    void updateEnumerations(IObjectList oldEnums) throws ServerError;
	    void deleteEnumeration(omero::model::IObject oldEnum) throws ServerError;
	    idempotent StringSet getEnumerationTypes() throws ServerError;
	    idempotent StringSet getAnnotationTypes() throws ServerError;
	    idempotent IObjectListMap getEnumerationsWithEntries() throws ServerError;
	    IObjectList getOriginalEnumerations() throws ServerError;
	    void resetEnumerations(string enumClass) throws ServerError;
	};

	/**
	 * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IUpdate.html">IUpdate.html</a>
	 **/
	["ami", "amd"] interface IUpdate extends ServiceInterface
	{
	    void saveObject(omero::model::IObject obj) throws ServerError;
	    void saveCollection(IObjectList objs) throws ServerError;
	    omero::model::IObject saveAndReturnObject(omero::model::IObject obj) throws ServerError;
	    void saveArray(IObjectList graph) throws ServerError;
	    IObjectList saveAndReturnArray(IObjectList graph) throws ServerError;
	    omero::sys::LongList saveAndReturnIds(IObjectList graph) throws ServerError;
	    void deleteObject(omero::model::IObject row) throws ServerError;
	    void indexObject(omero::model::IObject row) throws ServerError;
	};

	/**
	 * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IRenderingSettings.html">IRenderingSettings.html</a>
	 **/
	["ami", "amd"] interface IRenderingSettings extends ServiceInterface
	{
	    bool sanityCheckPixels(omero::model::Pixels pFrom, omero::model::Pixels pTo) throws ServerError;
	    omero::model::RenderingDef getRenderingSettings(long pixelsId) throws ServerError;
	    omero::model::RenderingDef createNewRenderingDef(omero::model::Pixels pixels) throws ServerError;
	    void resetDefaults(omero::model::RenderingDef def, omero::model::Pixels pixels) throws ServerError;
	    omero::model::RenderingDef resetDefaultsNoSave(omero::model::RenderingDef def, omero::model::Pixels pixels) throws ServerError;
	    void resetDefaultsInImage(long imageId) throws ServerError;
	    void resetDefaultsForPixels(long pixelsId) throws ServerError;
	    omero::sys::LongList resetDefaultsInDataset(long dataSetId) throws ServerError;
	    omero::sys::LongList resetDefaultsInSet(string type, omero::sys::LongList noteIds) throws ServerError;
	    BooleanIdListMap applySettingsToSet(long from, string toType, omero::sys::LongList to) throws ServerError;
	    BooleanIdListMap applySettingsToProject(long from, long to) throws ServerError;
	    BooleanIdListMap applySettingsToDataset(long from, long to) throws ServerError;
	    BooleanIdListMap applySettingsToImages(long from, omero::sys::LongList to) throws ServerError;
	    bool applySettingsToImage(long from, long to) throws ServerError;
	    bool applySettingsToPixels(long from, long to) throws ServerError;
	    void setOriginalSettingsInImage(long imageId) throws ServerError;
	    void setOriginalSettingsForPixels(long pixelsId) throws ServerError;
	    omero::sys::LongList setOriginalSettingsInDataset(long dataSetId) throws ServerError;
	    omero::sys::LongList setOriginalSettingsInSet(string type, omero::sys::LongList noteIds) throws ServerError;
	};

	/**
	 * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IRepositoryInfo.html">IRepositoryInfo.html</a>
	 **/
	["ami", "amd"] interface IRepositoryInfo extends ServiceInterface
	{
	    idempotent long getUsedSpaceInKilobytes() throws ServerError;
	    idempotent long getFreeSpaceInKilobytes() throws ServerError;
	    idempotent double getUsageFraction() throws ServerError;
	    void sanityCheckRepository() throws ServerError;
	    void removeUnusedFiles() throws ServerError;
	};


	/**
	 * Service for the querying of OMERO metadata based on creation and modification
	 * time. Currently supported types for querying include:
	 *
	 *    - "Annotation"
	 *    - "Dataset"
	 *    - "Image"
	 *    - "Project"
	 *    - "RenderingDef"
	 *
	 * Return maps:
	 * -----------
	 * The map return values will be indexed by the short type name above:
	 * "Project", "Image", ... All keys which are passed in the StringSet
	 * argument will be included in the returned map, even if they have no
	 * values. A default value of 0 or the empty list [] will be used.
	 * The only exception to this rule is that the null or empty StringSet
	 * implies all valid keys.
	 *
	 * Parameters:
	 * ----------
	 * All methods take a omero::sys::Parameters object and will apply the filter
	 * object for paging through the data in order to prevent loading too
	 * many objects. If the parameters argument is null or no paging is activated,
	 * then the default will be: OFFSET=0, LIMIT=50. Filter.ownerId and
	 * Filter.groupId will also be AND'ed to the query if either value is present.
	 * If both are null, then the current user id will be used. To retrieve for
	 * all users, use ownerId == rlong(-1) and groupId == null.
	 *
	 * Merging:
	 * -------
	 * The methods which take a StringSet and a Parameters object, also have
	 * a "bool merge" argument. This argument defines whether or not the LIMIT
	 * applies to each object independently (["a","b"] @ 100 == 200) or merges
	 * the lists together chronologically (["a","b"] @ 100 merged == 100).
	 *
	 * Time used:
	 * =========
	 * Except for Image, IObject.details.updateEvent is used in all cases for
	 * determining most recent events. Images are compared via
	 * Image.acquisitionDate which is unlike the other properties is modifiable
	 * by the user.
	 *
	 *
	 *
	 * A typical invocation might look like (in Python):
	 *
	 *     timeline = sf.getTimelineService()
	 *     params = ParametersI().page(0,100)
	 *     types = ["Project","Dataset"])
	 *     map = timeline.getByPeriod(types, params, False)
	 *
	 * At this point, map will not contain more than 200 objects.
	 *
	 * This service is defined only in Blitz and so no javadoc is available
	 * in the ome.api package.
	 *
	 * TODOS: binning, stateful caching, ...
	 **/
	["ami", "amd"] interface ITimeline extends ServiceInterface {

        /**
	     * Return the last LIMIT annotation __Links__ whose parent (IAnnotated)
	     * matches one of the parentTypes, whose child (Annotation) matches one
	     * of the childTypes (limit of one for the moment), and who namespace
	     * matches via ILIKE.
	     *
	     * The parents and children will be unloaded. The link will have
	     * its creation/update events loaded.
	     *
	     * If Parameters.theFilter.ownerId or groupId are set, they will be
	     * AND'ed to the query to filter results.
	     *
	     * Merges by default based on parentType.
	     **/
	    IObjectList
		getMostRecentAnnotationLinks(StringSet parentTypes, StringSet childTypes,
					     StringSet namespaces, omero::sys::Parameters p)
		throws ServerError;

	    /**
	     * Return the last LIMIT comment annotation links attached to a share by
	     * __others__.
	     *
	     * Note: Currently the storage of these objects is not optimal
	     * and so this method may change.
	     **/
	    IObjectList
		getMostRecentShareCommentLinks(omero::sys::Parameters p)
		throws ServerError;

	    /**
	     * Returns the last LIMIT objects of TYPES as ordered by
	     * creation/modification times in the Event table.
	     **/
	    IObjectListMap
		getMostRecentObjects(StringSet types, omero::sys::Parameters p, bool merge)
		throws ServerError;

	    /**
	     * Returns the given LIMIT objects of TYPES as ordered by
	     * creation/modification times in the Event table, but
	     * within the given time window.
	     **/
	    IObjectListMap
		getByPeriod(StringSet types, omero::RTime start, omero::RTime end, omero::sys::Parameters p,  bool merge)
		throws ServerError;

	    /**
	     * Queries the same information as getByPeriod, but only returns the counts
	     * for the given objects.
	     **/
	    StringLongMap
		countByPeriod(StringSet types, omero::RTime start, omero::RTime end, omero::sys::Parameters p)
		throws ServerError;

	    /**
	     * Returns the EventLog table objects which are queried to produce the counts above.
	     * Note the concept of "period inclusion" mentioned above.
	     *
	     * WORKAROUND WARNING: this method returns non-managed EventLogs (i.e.
	     * eventLog.getId() == null) for "Image acquisitions".
	     **/
	    EventLogList
		getEventLogsByPeriod(omero::RTime start, omero::RTime end, omero::sys::Parameters p)
		throws ServerError;

	};

	// Stateful services
	// ===================================================================================

        // Forward definition. See omero/api/Gateway.ice
    interface Gateway;

        /**
         * Stateful service for generating OME-XML or OME-TIFF from data stored
         * in OMERO. Intended usage:
         * <pre>
         *
         *   ExporterPrx e = sf.createExporter();
         *
         *   // Exporter is currently in the "configuration" state
         *   // Objects can be added by id which should be present
         *   // in the output.
         *
         *   e.addImage(1);
         *
         *
         *   // As soon as a generate method is called, the objects
         *   // added to the Exporter are converted to the specified
         *   // format. The length of the file produced is returned.
         *   // No more objects can be added to the Exporter, nor can
         *   // another generate method be called.
         *
         *   long length = e.generateTiff();
         *
         *   // As soon as the server-side file is generated, read()
         *   // can be called to get file segments. To create another
         *   // file, create a second Exporter. Be sure to close all
         *   // Exporter instances.
         *
         *   long read = 0
         *   byte[] buf;
         *   while (true) {
         *      buf = e.read(read, 1000000);
         *      // Store to file locally here
         *      if (buf.length < 1000000) {
         *          break;
         *       }
         *       read += buf.length;
         *   }
         *   e.close();
         *
         * </pre>
         **/
        ["ami", "amd"] interface Exporter extends StatefulServiceInterface {

            // Config ================================================

            /**
             * Adds a single image with basic metadata to the Exporter for inclusion
             * on the next call to getBytes().
             **/
            void addImage(long id) throws ServerError;

            // Output ================================================

            /**
             * Generates an OME-XML file. The return value is the length
             * of the file produced.
             **/
            long generateXml() throws ServerError;

            /**
             * Generates an OME-TIFF file. The return value is the length
             * of the file produced. This method ends configuration.
             **/
            long generateTiff() throws ServerError;

            /**
             * Returns "length" bytes from the output file. The file can
             * be safely read until reset() is called.
             **/
	    idempotent Ice::ByteSeq read(long position, int length) throws ServerError;

            // StatefulService: be sure to call close()!

        };

	/**
	 * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/JobHandle.html">JobHandle.html</a>
	 **/
	["ami", "amd"] interface JobHandle extends StatefulServiceInterface
	{
	    long submit(omero::model::Job j) throws ServerError;
	    omero::model::JobStatus attach(long jobId) throws ServerError;
	    omero::model::Job getJob()  throws ServerError;
	    omero::model::JobStatus jobStatus()  throws ServerError;
	    omero::RTime jobFinished()  throws ServerError;
	    string jobMessage()  throws ServerError;
	    bool jobRunning()  throws ServerError;
	    bool jobError()  throws ServerError;
	    void cancelJob()  throws ServerError;
	};

	/**
	 * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/RawFileStore.html">RawFileStore.html</a>
	 **/
	["ami", "amd"] interface RawFileStore extends StatefulServiceInterface
	{
	    void setFileId(long fileId) throws ServerError;
	    idempotent Ice::ByteSeq read(long position, int length) throws ServerError;
	    idempotent void write(Ice::ByteSeq buf, long position, int length) throws ServerError;
	    idempotent bool exists() throws ServerError;
	};

	/**
	 * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/RawPixelsStore.html">RawPixelsStore.html</a>
	 **/
	["ami", "amd"] interface RawPixelsStore extends StatefulServiceInterface
	{
	    void setPixelsId(long pixelsId, bool bypassOriginalFile) throws ServerError;
            idempotent void prepare(omero::sys::LongList pixelsIds) throws ServerError;
	    idempotent int getPlaneSize() throws ServerError;
	    idempotent int getRowSize() throws ServerError;
	    idempotent int getStackSize() throws ServerError;
	    idempotent int getTimepointSize() throws ServerError;
	    idempotent int getTotalSize() throws ServerError;
	    idempotent long getRowOffset(int y, int z, int c, int t) throws ServerError;
	    idempotent long getPlaneOffset(int z, int c, int t) throws ServerError;
	    idempotent long getStackOffset(int c, int t) throws ServerError;
	    idempotent long getTimepointOffset(int t) throws ServerError;
	    idempotent Ice::ByteSeq getRegion(int size, long offset) throws ServerError;
	    idempotent Ice::ByteSeq getRow(int y, int z, int c, int t) throws ServerError;
	    idempotent Ice::ByteSeq getCol(int x, int z, int c, int t) throws ServerError;
	    idempotent Ice::ByteSeq getPlane(int z, int c, int t) throws ServerError;
	    idempotent Ice::ByteSeq getPlaneRegion(int z, int c, int t, int size, int offset) throws ServerError;
	    idempotent Ice::ByteSeq getStack(int c, int t) throws ServerError;
	    idempotent Ice::ByteSeq getTimepoint(int t) throws ServerError;
	    idempotent void setRegion(int size, long offset, Ice::ByteSeq buffer) throws ServerError;
	    idempotent void setRow(Ice::ByteSeq buf, int y, int z, int c, int t) throws ServerError;
	    idempotent void setPlane(Ice::ByteSeq buf, int z, int c, int t) throws ServerError;
	    idempotent void setStack(Ice::ByteSeq buf, int z, int c, int t) throws ServerError;
	    idempotent void setTimepoint(Ice::ByteSeq buf, int t) throws ServerError;
	    idempotent int getByteWidth() throws ServerError;
	    idempotent bool isSigned() throws ServerError;
	    idempotent bool isFloat() throws ServerError;
	    idempotent Ice::ByteSeq calculateMessageDigest() throws ServerError;
	};

	/**
	 * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/omeis/re/providers/RenderingEngine.html">RenderingEngine.html</a>
	 **/
	["ami", "amd"] interface RenderingEngine extends StatefulServiceInterface
	{
	    omero::romio::RGBBuffer render(omero::romio::PlaneDef def) throws ServerError;
	    Ice::IntSeq renderAsPackedInt(omero::romio::PlaneDef def) throws ServerError;
	    Ice::IntSeq renderAsPackedIntAsRGBA(omero::romio::PlaneDef def) throws ServerError;
	    Ice::IntSeq renderProjectedAsPackedInt(omero::constants::projection::ProjectionType algorithm, int timepoint, int stepping, int start, int end) throws ServerError;
	    Ice::ByteSeq renderCompressed(omero::romio::PlaneDef def) throws ServerError;
	    Ice::ByteSeq renderProjectedCompressed(omero::constants::projection::ProjectionType algorithm, int timepoint, int stepping, int start, int end) throws ServerError;
	    void lookupPixels(long pixelsId) throws ServerError;
	    bool lookupRenderingDef(long pixelsId) throws ServerError;
	    void loadRenderingDef(long renderingDefId) throws ServerError;
	    void setOverlays(omero::RLong tablesId, omero::RLong imageId, LongIntMap rowColorMap) throws ServerError;
	    void load() throws ServerError;
	    void setModel(omero::model::RenderingModel model) throws ServerError;
	    omero::model::RenderingModel getModel() throws ServerError;
	    int getDefaultZ() throws ServerError;
	    int getDefaultT() throws ServerError;
	    void setDefaultZ(int z) throws ServerError;
	    void setDefaultT(int t) throws ServerError;
	    omero::model::Pixels getPixels() throws ServerError;
	    IObjectList getAvailableModels() throws ServerError;
	    IObjectList getAvailableFamilies() throws ServerError;
	    void setQuantumStrategy(int bitResolution) throws ServerError;
	    void setCodomainInterval(int start, int end) throws ServerError;
	    omero::model::QuantumDef getQuantumDef() throws ServerError;
	    void setQuantizationMap(int w, omero::model::Family fam, double coefficient, bool noiseReduction) throws ServerError;
	    omero::model::Family getChannelFamily(int w) throws ServerError;
	    bool getChannelNoiseReduction(int w) throws ServerError;
	    Ice::DoubleSeq getChannelStats(int w) throws ServerError;
	    double getChannelCurveCoefficient(int w) throws ServerError;
	    void setChannelWindow(int w, double start, double end) throws ServerError;
	    double getChannelWindowStart(int w) throws ServerError;
	    double getChannelWindowEnd(int w) throws ServerError;
	    void setRGBA(int w, int red, int green, int blue, int alpha) throws ServerError;
	    Ice::IntSeq getRGBA(int w) throws ServerError;
	    void setActive(int w, bool active) throws ServerError;
	    bool isActive(int w) throws ServerError;
	    void addCodomainMap(omero::romio::CodomainMapContext mapCtx) throws ServerError;
	    void updateCodomainMap(omero::romio::CodomainMapContext mapCtx) throws ServerError;
	    void removeCodomainMap(omero::romio::CodomainMapContext mapCtx) throws ServerError;
	    void saveCurrentSettings() throws ServerError;
	    void resetDefaults() throws ServerError;
	    void resetDefaultsNoSave() throws ServerError;
	    void setCompressionLevel(float percentage) throws ServerError;
	    float getCompressionLevel() throws ServerError;
	    bool isPixelsTypeSigned() throws ServerError;
	    double getPixelsTypeUpperBound(int w) throws ServerError;
	    double getPixelsTypeLowerBound(int w) throws ServerError;
	};

	/**
	 * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/Search.html">Search.html</a>
	 **/
	["ami", "amd"] interface Search extends StatefulServiceInterface
	{

	    // Non-query state ~~~~~~~~~~~~~~~~~~~~~~

	    int activeQueries() throws ServerError;
	    void setBatchSize(int size) throws ServerError;
	    int getBatchSize() throws ServerError;
	    void setMergedBatches(bool merge) throws ServerError;
	    bool isMergedBatches() throws ServerError;
	    void setCaseSentivice(bool caseSensitive) throws ServerError;
	    bool isCaseSensitive() throws ServerError;
	    void setUseProjections(bool useProjections) throws ServerError;
	    bool isUseProjections() throws ServerError;
	    void setReturnUnloaded(bool returnUnloaded) throws ServerError;
	    bool isReturnUnloaded() throws ServerError;
	    void setAllowLeadingWildcard(bool allowLeadingWildcard) throws ServerError;
	    bool isAllowLeadingWildcard() throws ServerError;


	    // Filters ~~~~~~~~~~~~~~~~~~~~~~

	    void onlyType(string klass) throws ServerError;
	    void onlyTypes(StringSet classes) throws ServerError;
	    void allTypes() throws ServerError;
	    void onlyIds(omero::sys::LongList ids) throws ServerError;
	    void onlyOwnedBy(omero::model::Details d) throws ServerError;
	    void notOwnedBy(omero::model::Details d) throws ServerError;
	    void onlyCreatedBetween(omero::RTime start, omero::RTime  stop) throws ServerError;
	    void onlyModifiedBetween(omero::RTime start, omero::RTime stop) throws ServerError;
	    void onlyAnnotatedBetween(omero::RTime start, omero::RTime stop) throws ServerError;
	    void onlyAnnotatedBy(omero::model::Details d) throws ServerError;
	    void notAnnotatedBy(omero::model::Details d) throws ServerError;
	    void onlyAnnotatedWith(StringSet classes) throws ServerError;


	    // Fetches, order, counts, etc ~~~~~~~~~~~~~~~~~~~~~~

	    void addOrderByAsc(string path) throws ServerError;
	    void addOrderByDesc(string path) throws ServerError;
	    void unordered() throws ServerError;
	    void fetchAnnotations(StringSet classes) throws ServerError;
	    void fetchAlso(StringSet fetches) throws ServerError;


	    // Reset ~~~~~~~~~~~~~~~~~~~~~~~~~

	    void resetDefaults() throws ServerError;


	    // Query state  ~~~~~~~~~~~~~~~~~~~~~~~~~

	    void byGroupForTags(string group) throws ServerError;
	    void byTagForGroups(string tag) throws ServerError;
	    void byFullText(string query) throws ServerError;
            void bySimilarTerms(StringSet terms) throws ServerError;
	    void byHqlQuery(string query, omero::sys::Parameters params) throws ServerError;
	    void bySomeMustNone(StringSet some, StringSet must, StringSet none) throws ServerError;
	    void byAnnotatedWith(AnnotationList examples) throws ServerError;
	    void clearQueries() throws ServerError;

	    void and() throws ServerError;
	    void or() throws ServerError;
	    void not() throws ServerError;


	    // Retrieval  ~~~~~~~~~~~~~~~~~~~~~~~~~

	    bool hasNext() throws ServerError;
	    omero::model::IObject next() throws ServerError;
	    IObjectList results() throws ServerError;

	    // Currently unused
	    SearchMetadata currentMetadata() throws ServerError;
	    SearchMetadataList currentMetadataList() throws ServerError;

	    // Unused; Part of Java Iterator interface
	    void remove() throws ServerError;
	};

	/**
	 * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/ThumbnailStore.html">ThumbnailStore.html</a>
	 **/
	["ami", "amd"] interface ThumbnailStore extends StatefulServiceInterface
	{
	    bool setPixelsId(long pixelsId) throws ServerError;
	    void setRenderingDefId(long renderingDefId) throws ServerError;
	    Ice::ByteSeq getThumbnail(omero::RInt sizeX, omero::RInt sizeY) throws ServerError;
	    omero::sys::IdByteMap getThumbnailSet(omero::RInt sizeX, omero::RInt sizeY, omero::sys::LongList pixelsIds) throws ServerError;
	    omero::sys::IdByteMap getThumbnailByLongestSideSet(omero::RInt size, omero::sys::LongList pixelsIds) throws ServerError;
	    Ice::ByteSeq getThumbnailByLongestSide(omero::RInt size) throws ServerError;
	    Ice::ByteSeq getThumbnailByLongestSideDirect(omero::RInt size) throws ServerError;
	    Ice::ByteSeq getThumbnailDirect(omero::RInt sizeX, omero::RInt sizeY) throws ServerError;
	    Ice::ByteSeq getThumbnailForSectionDirect(int theZ, int theT, omero::RInt sizeX, omero::RInt sizeY) throws ServerError;
	    Ice::ByteSeq getThumbnailForSectionByLongestSideDirect(int theZ, int theT, omero::RInt size) throws ServerError;
	    void createThumbnails() throws ServerError;
	    void createThumbnail(omero::RInt sizeX, omero::RInt sizeY) throws ServerError;
	    void createThumbnailsByLongestSideSet(omero::RInt size, omero::sys::LongList pixelsIds) throws ServerError;
	    bool thumbnailExists(omero::RInt sizeX, omero::RInt sizeY) throws ServerError;
	    void resetDefaults() throws ServerError;
	};


	/**
	 * Primary callback interface for interaction between client and
	 * server session ("ServiceFactory"). Where possible these methods
	 * will be called one-way to prevent clients from hanging the server.
	 **/

	["ami"] interface ClientCallback
	{

	    /**
	     * Heartbeat-request made by the server to guarantee that the client
	     * is alive. If the client is still active, then some method should
	     * be made on the server to update the last idle time.
	     **/
	    void requestHeartbeat();

	    /**
	     * The session to which this ServiceFactory is connected has been
	     * closed. Almost no further method calls (if any) are possible.
	     * Create a new session via omero.client.createSession()
	     **/
	    void sessionClosed();

	    /**
	     * Message that the server will be shutting down in the
	     * given number of milliseconds, after which all new and
	     * running method invocations will recieve a CancelledException.
	     **/
	    void shutdownIn(long milliseconds);
	};


	/**
	 * Starting point for all OMERO.blitz interaction. This ServiceFactory once properly created
	 * creates functioning proxies to the server.
	 **/
	interface ServiceFactory extends Glacier2::Session
	{
	    // Central OMERO.blitz stateless services.

	    IAdmin*          getAdminService() throws ServerError;
	    IConfig*         getConfigService() throws ServerError;
	    IContainer*      getContainerService() throws ServerError;
	    IDelete*         getDeleteService() throws ServerError;
	    ILdap*           getLdapService() throws ServerError;
	    IPixels*         getPixelsService() throws ServerError;
	    IProjection*     getProjectionService() throws ServerError;
	    IQuery*          getQueryService() throws ServerError;
	    IRenderingSettings* getRenderingSettingsService() throws ServerError;
	    IRepositoryInfo* getRepositoryInfoService() throws ServerError;
	    IRoi*            getRoiService() throws ServerError;
	    IScript*         getScriptService() throws ServerError;
	    ISession*        getSessionService() throws ServerError;
	    IShare*          getShareService() throws ServerError;
	    ITimeline*       getTimelineService() throws ServerError;
	    ITypes*          getTypesService() throws ServerError;
	    IUpdate*         getUpdateService() throws ServerError;
	    IMetadata*       getMetadataService() throws ServerError;

	    // Central OMERO.blitz stateful services.

	    Gateway*         createGateway() throws ServerError;
	    Exporter*        createExporter() throws ServerError;
	    JobHandle*       createJobHandle() throws ServerError;
	    RawFileStore*    createRawFileStore() throws ServerError;
	    RawPixelsStore*  createRawPixelsStore() throws ServerError;
	    RenderingEngine* createRenderingEngine() throws ServerError;
	    Search*          createSearchService() throws ServerError;
	    ThumbnailStore*  createThumbnailStore() throws ServerError;

            // Shared resources -----------------------------------------------

        /**
         * Returns a reference to a back-end manager. The [omero::grid::SharedResources]
         * service provides look ups for various facilities offered by OMERO:
         * <ul>
         *   <li><a href="http://trac.openmicroscopy.org.uk/omero/wiki/OmeroScripts">OMERO.scripts</a>
         *   <li><a href="http://trac.openmicroscopy.org.uk/omero/wiki/OmeroTables">OMERO.tables</a>
         * </ul>
         * These facilities may or may not be available on first request.
         *
         * @see omero::grid::SharedResources
         */
            omero::grid::SharedResources* sharedResources() throws ServerError;

            // General methods ------------------------------------------------

	    /**
	     * Allows looking up any service by name. See Constants.ice
	     * for examples of services. If a service has been added
	     * by third-parties, getByName can be used even though
	     * no concrete method is available.
	     **/

	    ServiceInterface* getByName(string name) throws ServerError;

	    StatefulServiceInterface* createByName(string name) throws ServerError;

	    /**
	     * Subscribe to a given topic. The topic must exist and the user must
	     * have sufficient permissions for that topic. Further the proxy object
	     * must match the required type for the topic as encoded in the topic
	     * name.
	     **/
	    void subscribe(string topicName, Object* prx) throws ServerError;

	    /**
	     * Sets the single callback used by the ServiceFactory
	     * to communicate with the client application. A default
	     * callback is set by the omero::client object on
	     * session creation which should suffice for most usage.
	     *
	     * See the client object's documentation in each language
	     * mapping for ways to use the callback.
	     **/
	    void setCallback(ClientCallback* callback);

	    /**
	     * Deprecated misnomer.
	     **/
	    ["deprecated:close() is deprecated. use closeOnDestroy() instead."] void close();

	    /**
	     * Marks the session for closure rather than detachment, which will
	     * be triggered by the destruction of the Glacier2 connection via
	     * router.destroySession()
	     *
	     * Closing the session rather the detaching is more secure, since all
	     * resources are removed from the server and can safely be set once
	     * it is clear that a client is finished with those resources.
	     **/
	    void closeOnDestroy();

	    /**
	     * Marks the session for detachment rather than closure, which will
	     * be triggered by the destruction of the Glacier2 connection via
	     * router.destroySession()
	     *
	     * This is the default and allows a lost session to be reconnected,
	     * at a slight security cost since the session will persist longer
	     * and can be used by others if the UUID is intercepted.
	     **/
	    void detachOnDestroy();

	    // Session management

	    /**
	     * Returns a list of string ids for currently active services. This will
	     * _not_ keep services alive, and in fact checks for all expired services
	     * and removes them.
	     **/
	    StringSet activeServices();

	    /**
	     * Requests that the given services be marked as alive. It is
	     * possible that one of the services has already timed out, in which
	     * case the returned long value will be non-zero.
	     *
	     * Specifically, the bit representing the 0-based index will be 1:
	     *
	     *        if (retval & 1<<idx == 1<<idx) { // not alive }
	     *
	     * Except for fatal server or session errors, this method should never
	     * throw an exception.
	     **/
	    long keepAllAlive(ServiceList proxies);

	    /**
	     * Returns true if the given service is alive.
	     *
	     * Except for fatal server or session errors, this method should never
	     * throw an exception.
	     **/
	    bool keepAlive(ServiceInterface* proxy);

	};

    };
};

#endif
