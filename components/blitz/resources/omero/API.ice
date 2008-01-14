/*
 *   $Id$
 * 
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef omero_API
#define omero_API

#include <omero/fwd.ice>
#include <omero/ROMIO.ice>
#include <omero/RTypes.ice>
#include <omero/System.ice>
#include <Glacier2/Session.ice>
#include <Ice/BuiltinSequences.ice>

/*
 * The omero::api module defines all the central verbs for working
 * with OMERO.blitz. Arguments and return values consist of those
 * types defined in the other ice files available here. With no
 * further custom code, it is possible to interoperate with
 * OMERO.blitz simply via the definitions here. Start with the
 * ServiceFactory definition at the end of this file.
 *
 * Note: Using these types is significantly easier in combination with
 * the JavaDocs of the OMERO.server, specifically the ome.api
 * package. Where not further noted below, the follow mappings between
 * ome.api argument types and omero::api argument types hold:
 *
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
 */
module omero { 
   
  module api {     
   
    ["java:type:java.util.ArrayList"] 
      sequence<omero::model::Experimenter> ExperimenterList;
   
    ["java:type:java.util.ArrayList"] 
      sequence<omero::model::ExperimenterGroup> ExperimenterGroupList;
   
    ["java:type:java.util.ArrayList<omero.model.IObject>:java.util.List<omero.model.IObject>"] 
      sequence<omero::model::IObject> IObjectList;
   
    ["java:type:java.util.ArrayList"] 
      sequence<omero::model::Image> ImageList;

    ["java:type:java.util.ArrayList<String>:java.util.List<String>"] 
      sequence<string> StringSet;
      
    /*
     * Service marker similar to ome.api.ServiceInterface
     */
    interface ServiceInterface
      {
      };

	sequence<ServiceInterface*> ServiceList;

    /*
     * Service marker for stateful services which permits the closing
     * of a particular service before the destruction of the session.
     */
    interface StatefulServiceInterface extends ServiceInterface
      {
        void close();
        idempotent omero::sys::EventContext getCurrentEventContext() throws ServerError;
      };

    interface IAdmin extends ServiceInterface
      {
    
	// Getters
	idempotent omero::model::Experimenter getExperimenter(long id) throws ServerError;
	idempotent omero::model::Experimenter lookupExperimenter(string name) throws ServerError;
	idempotent ExperimenterList lookupExperimenters() throws ServerError;
	idempotent omero::model::ExperimenterGroup getGroup(long id) throws ServerError;
	idempotent omero::model::ExperimenterGroup lookupGroup(string name) throws ServerError ; 
	idempotent ExperimenterGroupList lookupGroups() throws ServerError;
	idempotent ExperimenterList containedExperimenters(long groupId) throws ServerError;
	idempotent ExperimenterGroupList containedGroups(long experimenterId) throws ServerError;
	idempotent omero::model::ExperimenterGroup getDefaultGroup(long experimenterId) throws ServerError;
    
	// Mutators
    
	void updateExperimenter(omero::model::Experimenter experimenter) throws ServerError;
	void updateGroup(omero::model::ExperimenterGroup group) throws ServerError;
	long createUser(omero::model::Experimenter experimenter, string group) throws ServerError;
	long createSystemUser(omero::model::Experimenter experimenter) throws ServerError;
	long createExperimenter(omero::model::Experimenter user, 
				omero::model::ExperimenterGroup defaultGroup, ExperimenterGroupList groups) throws ServerError; 
	long createGroup(omero::model::ExperimenterGroup group) throws ServerError;
	idempotent void addGroups(omero::model::Experimenter user, ExperimenterList groups) throws ServerError;
	idempotent void removeGroups(omero::model::Experimenter user, ExperimenterGroupList groups) throws ServerError;
	idempotent void setDefaultGroup(omero::model::Experimenter user, omero::model::ExperimenterGroup group) throws ServerError;
	idempotent void setGroupOwner(omero::model::ExperimenterGroup group, omero::model::Experimenter owner) throws ServerError;
	idempotent void deleteExperimenter(omero::model::Experimenter user) throws ServerError;
	idempotent void changeOwner(omero::model::IObject obj, string omeName) throws ServerError;
	idempotent void changeGroup(omero::model::IObject obj, string omeName) throws ServerError;
	idempotent void changePermissions(omero::model::IObject obj, omero::model::Permissions perms) throws ServerError;
	/* Leaving this non-idempotent, because of the overhead, though technically it is. */
	Ice::BoolSeq unlock(IObjectList objects) throws ServerError;

	// UAuth
	idempotent void changePassword(omero::RString newPassword) throws ServerError;
	idempotent void changeUserPassword(string omeName, omero::RString newPassword) throws ServerError;
	idempotent void synchronizeLoginCache() throws ServerError;

	// Security Context
	idempotent omero::sys::Roles getSecurityRoles() throws ServerError;
	idempotent omero::sys::EventContext getEventContext() throws ServerError;
      };

    interface IConfig extends ServiceInterface
      {
	idempotent string getVersion() throws ServerError;
	idempotent string getConfigValue(string key) throws ServerError;
	idempotent void setConfigValue(string key, string value) throws ServerError;
	idempotent omero::RTime getDatabaseTime() throws ServerError;
	idempotent omero::RTime getServerTime() throws ServerError;
      };

    interface IDelete extends omero::api::ServiceInterface
      {
        omero::api::IObjectList checkImageDelete(long id, bool force) throws ServerError;
        omero::api::IObjectList previewImageDelete(long id, bool force) throws ServerError;
        void deleteImage(long id, bool force) throws ApiUsageException, ValidationException, SecurityViolation, ServerError;
      };

     interface ILdap extends ServiceInterface
      {
	idempotent ExperimenterList searchAll() throws ServerError;
	idempotent StringSet searchDnInGroups(string attr, string value) throws ServerError;
	idempotent ExperimenterList searchByAttribute(string attribute, string value) throws ServerError;
	idempotent omero::model::Experimenter searchByDN(string userdn) throws ServerError;
	idempotent string findDN(string username) throws ServerError;
	idempotent void setDN(long experimenterID, string dn) throws ServerError;
	idempotent ExperimenterGroupList searchGroups() throws ServerError;
	idempotent StringSet searchAttributes() throws ServerError;
	idempotent bool checkAttributes(string dn, StringSet attrs) throws ServerError;
      };


    interface IPixels extends ServiceInterface
      {
	idempotent omero::model::Pixels retrievePixDescription(long pixId) throws ServerError;
	idempotent omero::model::RenderingDef retrieveRndSettings(long pixId) throws ServerError;
	void saveRndSettings(omero::model::RenderingDef rndSettings) throws ServerError;
	idempotent int getBitDeptch(omero::model::PixelsType type) throws ServerError;
	idempotent omero::RObject getEnumeration(string enumClass, string value) throws ServerError;
	idempotent IObjectList getAllEnumerations(string enumClass) throws ServerError;
      };

    dictionary<long, IObjectList> AnnotationMap;
    dictionary<string, omero::model::Experimenter> UserMap;
    dictionary<int, int> CountMap;

    interface IPojos extends ServiceInterface
      {
	idempotent IObjectList loadContainerHierarchy(string rootType, omero::sys::LongList rootIds, omero::sys::ParamMap options) throws ServerError;
	idempotent IObjectList findContainerHierarchies(string rootType, omero::sys::LongList imageIds, omero::sys::ParamMap options) throws ServerError;
	idempotent AnnotationMap findAnnotations(string rootType, omero::sys::LongList rootIds, omero::sys::LongList annotatorIds, omero::sys::ParamMap options) throws ServerError;
	idempotent IObjectList findCGCPaths(omero::sys::LongList imageIds, string algo, omero::sys::ParamMap options) throws ServerError;
	idempotent ImageList getImages(string rootType, omero::sys::LongList rootIds, omero::sys::ParamMap options) throws ServerError;
	idempotent ImageList getUserImages(omero::sys::ParamMap options) throws ServerError;
	idempotent UserMap getUserDetails(StringSet names, omero::sys::ParamMap options) throws ServerError;
	idempotent CountMap getCollectionCount(string type, string property, omero::sys::LongList ids, omero::sys::ParamMap options) throws ServerError;
	idempotent IObjectList retrieveCollection(omero::model::IObject obj, string collectionName, omero::sys::ParamMap options) throws ServerError;
	omero::model::IObject createDataObject(omero::model::IObject obj, omero::sys::ParamMap options) throws ServerError;
	IObjectList createDataObjects(IObjectList dataObjects, omero::sys::ParamMap options) throws ServerError;
	void unlink(IObjectList links, omero::sys::ParamMap options) throws ServerError;
	IObjectList link(IObjectList links, omero::sys::ParamMap options) throws ServerError;
	omero::model::IObject updateDataObject(omero::model::IObject obj, omero::sys::ParamMap options) throws ServerError;
	IObjectList updateDataObjects(IObjectList objs, omero::sys::ParamMap options) throws ServerError;
	void deleteDataObject(omero::model::IObject obj, omero::sys::ParamMap options) throws ServerError;
	void deleteDataObjects(IObjectList objs, omero::sys::ParamMap options) throws ServerError;
      };

    interface IQuery extends ServiceInterface
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
	idempotent IObjectList           findAllByFullTet(string klass, string query, omero::sys::Parameters params) throws ServerError;
	idempotent omero::model::IObject refresh(omero::model::IObject iObject) throws ServerError;
      };

    interface ITypes extends ServiceInterface
      {
	omero::model::IObject createEnumeration(omero::model::IObject newEnum) throws ServerError;
	idempotent omero::model::IObject getEnumeration(string type, string value) throws ServerError;
	idempotent IObjectList allEnumerations(string type) throws ServerError;
      };

    interface IUpdate extends ServiceInterface
      { 
	void saveObject(omero::model::IObject obj) throws ServerError;
	void saveCollection(IObjectList objs) throws ServerError;     
	omero::model::IObject saveAndReturnObject(omero::model::IObject obj) throws ServerError;
	IObjectList saveAndReturnArray(IObjectList graph) throws ServerError;
	void deleteObject(omero::model::IObject row) throws ServerError;
      };
	
    interface IRepositoryInfo extends ServiceInterface
      {
	idempotent long getUsedSpaceInKilobytes() throws ServerError;
	idempotent long getFreeSpaceInKilobytes() throws ServerError;
	idempotent double getUsageFraction() throws ServerError;
	void sanityCheckRepository() throws ServerError;
	void removeUnusedFiles() throws ServerError;
      };
	
    interface RawFileStore extends StatefulServiceInterface
      {
	void setFileId(long fileId) throws ServerError;
	idempotent Ice::ByteSeq read(long position, int length) throws ServerError;
	idempotent void write(Ice::ByteSeq buf, long position, int length) throws ServerError;
      };

    interface RawPixelsStore extends StatefulServiceInterface
      {
	void setPixelsId(long pixelsId) throws ServerError;
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

    interface RenderingEngine extends StatefulServiceInterface
      {
	omero::romio::RGBBuffer render(omero::romio::PlaneDef def) throws ServerError;
	Ice::IntSeq renderAsPackedInt(omero::romio::PlaneDef def) throws ServerError;
	Ice::ByteSeq renderCompressed(omero::romio::PlaneDef def) throws ServerError;
	void lookupPixels(long pixelsId) throws ServerError;
	bool lookupRenderingDef(long pixelsId) throws ServerError;
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
	bool getchannelNoiseReduction(int w) throws ServerError;
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
      };

    //interface Search extends StatefulServiceInterface
    //  {
    //  
    //  };

    interface ThumbnailStore extends StatefulServiceInterface
      {
	bool setPixelsId(long pixelsId) throws ServerError;
	void setRenderingDefId(long renderingDefId) throws ServerError;
	Ice::ByteSeq getThumbnail(int sizeX, int sizeY) throws ServerError;
	omero::sys::ParamMap getThumbnailSet(int sizeX, int sizeY, omero::sys::LongList pixelsIds) throws ServerError;
	Ice::ByteSeq getThumbnailByLongestSide(int size) throws ServerError;
	Ice::ByteSeq getThumbnailDirect(int sizeX, int sizeY) throws ServerError;
	Ice::ByteSeq getThumbnailForSectionDirect(int theZ, int theT, int sizeX, int sizeY) throws ServerError;
	Ice::ByteSeq getThumbnailForSectionByLongestSideDirect(int theZ, int theT, int size) throws ServerError;
	void createThumbnails() throws ServerError;
	bool thumbnailExist(int sizeX, int sizeY) throws ServerError;
	void resetDefaults() throws ServerError;
      };

    /*
     * Unused, and unsupported. Similar callbacks can be added
     * as specific needs arise.
     */
    interface SimpleCallback 
      {
	void call();
      };

    /*
     * Starting point for all OMERO.blitz interaction. Similar to the
     * ome.system.ServiceFactory class in the OMERO.server and its
     * RMI/Java clients, this ServiceFactory once properly created
     * creates functioning proxies to the server. 
     *
     * The difference between the two types is that the blitz instance
     * sits server-side. 
     */
    interface ServiceFactory extends Glacier2::Session
      {
	// Central OMERO.blitz stateless services.
	IAdmin*    getAdminService() throws ServerError;
	IConfig*   getConfigService() throws ServerError;
	IPixels*   getPixelsService() throws ServerError;
	IPojos*    getPojosService() throws ServerError;
	IQuery*    getQueryService() throws ServerError;
	IRepositoryInfo* getRepositoryInfoService() throws ServerError;
	ITypes*    getTypesService() throws ServerError;
	IUpdate*   getUpdateService() throws ServerError;

	// Central OMERO.blitz stateful services.
	RawFileStore* createRawFileStore() throws ServerError;
	RawPixelsStore* createRawPixelsStore() throws ServerError;
	RenderingEngine* createRenderingEngine() throws ServerError;
	// Search* createSearchService() throws ServerError;
	ThumbnailStore* createThumbnailStore() throws ServerError;

	/*
	 * Allows looking up any service by name. See Constants.ice
	 * for examples of services. If a service has been added 
	 * by third-parties, getByName can be used even though 
	 * no concrete method is available.
	 */
	   
	ServiceInterface* getByName(string name) throws ServerError;
	
	StatefulServiceInterface* createByName(string name) throws ServerError;
	
	/*
	 * Example for what a server callback would look like.
	 * Unsupported.
	 */
	void setCallback(SimpleCallback* callback);

	/*
	 * Closes the service factory and all related services.
	 */
	void close();

	// Session management
	
	/*
	 * Returns a list of string ids for currently active services. This will
	 * _not_ keep services alive, and in fact checks for all expired services
	 * and removes them.
	 */
	StringSet activeServices();

	/*
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
	 */
	long keepAllAlive(ServiceList proxies);

	/*
	 * Returns true if the given service is alive.
	 * 
	 * Except for fatal server or session errors, this method should never
	 * throw an exception. 
	 */
	bool keepAlive(ServiceInterface* proxy);

      };

  };
};

#endif 
