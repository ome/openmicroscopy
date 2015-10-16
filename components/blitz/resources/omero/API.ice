/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_ICE
#define OMERO_API_ICE

#include <omero/cmd/API.ice>
#include <omero/ServerErrors.ice>
#include <omero/ModelF.ice>
#include <omero/ServicesF.ice>
#include <omero/System.ice>
#include <omero/Collections.ice>
#include <Glacier2/Session.ice>
#include <Ice/BuiltinSequences.ice>
#include <Ice/Identity.ice>

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
         * Starting point for all OMERO.blitz interaction.
         *
         * <p> A ServiceFactory once acquired can be used to create any number
         * of service proxies to the server. Most services implement [ServiceInterface]
         * or its subinterface [StatefulServiceInterface]. </p>
         **/
        interface ServiceFactory extends omero::cmd::Session
        {

            // Security context

            /**
             * Provides a list of all valid security contexts for this session.
             * Each of the returned [omero::model::IObject] instances can be
             * passed to setSecurityContext.
             **/
            IObjectList getSecurityContexts() throws ServerError;

            /**
             * Changes the security context for the current session.
             *
             * <p> A security context limits the set of objects which will
             * be returned by all queries and restricts what updates
             * can be made. </p>
             *
             * <p> Current valid values for security context:
             * <ul>
             *  <li>[omero::model::ExperimenterGroup] - logs into a specific group</li>
             *  <li>[omero::model::Share] - uses IShare to activate a share</li>
             * </ul> </p>
             *
             * <p> Passing an unloaded version of either object type will change
             * the way the current session operates. Note: only objects which
             * are returned by the [getSecurityContexts] method are considered
             * valid. Any other instance will cause an exception to be thrown. </p>
             *
             * <h4>Example usage in Python:<h4>
             * <pre>
             * sf = client.createSession()
             * objs = sf.getSecurityContexts()
             * old = sf.setSecurityContext(objs[-1])
             * </pre>
             *
             **/
            omero::model::IObject setSecurityContext(omero::model::IObject obj) throws ServerError;

            /**
             * Re-validates the password for the current session. This prevents
             *
             * See methods that mention "HasPassword".
             **/
            void setSecurityPassword(string password) throws ServerError;

            // Central OMERO.blitz stateless services.

            IAdmin*          getAdminService() throws ServerError;
            IConfig*         getConfigService() throws ServerError;
            IContainer*      getContainerService() throws ServerError;
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
             *   <li><a href="http://www.openmicroscopy.org/site/support/omero5.2/developers/scripts/">OMERO.scripts</a>
             *   <li><a href="http://www.openmicroscopy.org/site/support/omero5.2/developers/Tables.html">OMERO.tables</a>
             * </ul>
             * These facilities may or may not be available on first request.
             *
             * @see omero::grid::SharedResources
             **/
            omero::grid::SharedResources* sharedResources() throws ServerError;

            // General methods ------------------------------------------------

            /**
             * Allows looking up any stateless service by name.
             *
             * See Constants.ice for examples of services.
             * If a service has been added by third-parties,
             * getByName can be used even though no concrete
             * method is available.
             **/
            ServiceInterface* getByName(string name) throws ServerError;

            /**
             * Allows looking up any stateful service by name.
             *
             * See Constants.ice for examples of services.
             * If a service has been added by third-parties,
             * createByName can be used even though no concrete
             * method is available.
             **/
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
            void setCallback(ClientCallback* callback) throws ServerError;

            /**
             * Marks the session for closure rather than detachment, which will
             * be triggered by the destruction of the Glacier2 connection via
             * router.destroySession()
             *
             * Closing the session rather the detaching is more secure, since all
             * resources are removed from the server and can safely be set once
             * it is clear that a client is finished with those resources.
             **/
            void closeOnDestroy() throws ServerError;

            /**
             * Marks the session for detachment rather than closure, which will
             * be triggered by the destruction of the Glacier2 connection via
             * router.destroySession()
             *
             * This is the default and allows a lost session to be reconnected,
             * at a slight security cost since the session will persist longer
             * and can be used by others if the UUID is intercepted.
             **/
            void detachOnDestroy() throws ServerError;

            // Session management

            /**
             * Returns a list of string ids for currently active services. This will
             * _not_ keep services alive, and in fact checks for all expired services
             * and removes them.
             **/
            StringSet activeServices() throws ServerError;

            /**
             * Requests that the given services be marked as alive. It is
             * possible that one of the services has already timed out, in which
             * case the returned long value will be non-zero.
             *
             * Specifically, the bit representing the 0-based index will be 1:
             *
             *        if (retval & 1&lt;&lt;idx == 1&lt;&lt;idx) { // not alive }
             *
             * Except for fatal server or session errors, this method should never
             * throw an exception.
             **/
            long keepAllAlive(ServiceList proxies) throws ServerError;

            /**
             * Returns true if the given service is alive.
             *
             * Except for fatal server or session errors, this method should never
             * throw an exception.
             **/
            bool keepAlive(ServiceInterface* proxy) throws ServerError;

        };

    };
};

#endif
