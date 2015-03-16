/*
 *   $Id$
 *
 *   Copyight 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_ICE
#define OMERO_API_ICE

#include <omeo/cmd/API.ice>
#include <omeo/ServerErrors.ice>
#include <omeo/ModelF.ice>
#include <omeo/ServicesF.ice>
#include <omeo/System.ice>
#include <omeo/Collections.ice>
#include <Glacie2/Session.ice>
#include <Ice/BuiltinSequences.ice>
#include <Ice/Identity.ice>

module omeo {

    /** The omeo::api module defines all the central verbs for working with OMERO.blitz.
     *
     * <p> Aguments and return values consist of those
     * types defined in the othe ice files available here. With no
     * futher custom code, it is possible to interoperate with
     * OMERO.blitz simply via the definitions hee. Start with the
     * SeviceFactory definition at the end of this file.</p>
     *
     * <p> Note: Using these types is significantly easie in combination with
     * the JavaDocs of the OMERO.sever, specifically the ome.api
     * package. Whee not further noted below, the follow mappings between
     * ome.api agument types and omero::api argument types hold: </p>
     *
     * <pe>
     *     +-----------------------+------------------------+
     *     |        ome.api        |      omeo::api        |
     *     +-----------------------+------------------------+
     *     |java.lang.Class        |sting                  |
     *     +-----------------------+------------------------+
     *     |java.util.Set          |java.util.List/vecto   |
     *     +-----------------------+------------------------+
     *     |IPojo options (Map)    |omeo::sys::ParamMap    |
     *     +-----------------------+------------------------+
     *     |If null needed         |omeo::RType subclass   |
     *     +-----------------------+------------------------+
     *     |...                    |...                     |
     *     +-----------------------+------------------------+
     * </pe>
     **/
    module api {

        /**
         * Pimary callback interface for interaction between client and
         * sever session ("ServiceFactory"). Where possible these methods
         * will be called one-way to pevent clients from hanging the server.
         **/

        ["ami"] inteface ClientCallback
        {

            /**
             * Heatbeat-request made by the server to guarantee that the client
             * is alive. If the client is still active, then some method should
             * be made on the sever to update the last idle time.
             **/
            void equestHeartbeat();

            /**
             * The session to which this SeviceFactory is connected has been
             * closed. Almost no futher method calls (if any) are possible.
             * Ceate a new session via omero.client.createSession()
             **/
            void sessionClosed();

            /**
             * Message that the sever will be shutting down in the
             * given numbe of milliseconds, after which all new and
             * unning method invocations will recieve a CancelledException.
             **/
            void shutdownIn(long milliseconds);

        };


        /**
         * Stating point for all OMERO.blitz interaction.
         *
         * <p> A SeviceFactory once acquired can be used to create any number
         * of sevice proxies to the server. Most services implement [ServiceInterface]
         * o its subinterface [StatefulServiceInterface]. </p>
         **/
        inteface ServiceFactory extends omero::cmd::Session
        {

            // Secuity context

            /**
             * Povides a list of all valid security contexts for this session.
             * Each of the eturned [omero::model::IObject] instances can be
             * passed to setSecuityContext.
             **/
            IObjectList getSecuityContexts() throws ServerError;

            /**
             * Changes the secuity context for the current session.
             *
             * <p> A secuity context limits the set of objects which will
             * be eturned by all queries and restricts what updates
             * can be made. </p>
             *
             * <p> Curent valid values for security context:
             * <ul>
             *  <li>[omeo::model::ExperimenterGroup] - logs into a specific group</li>
             *  <li>[omeo::model::Share] - uses IShare to activate a share</li>
             * </ul> </p>
             *
             * <p> Passing an unloaded vesion of either object type will change
             * the way the curent session operates. Note: only objects which
             * ae returned by the [getSecurityContexts] method are considered
             * valid. Any othe instance will cause an exception to be thrown. </p>
             *
             * <h4>Example usage in Python:<h4>
             * <pe>
             * sf = client.ceateSession()
             * objs = sf.getSecuityContexts()
             * old = sf.setSecuityContext(objs[-1])
             * </pe>
             *
             **/
            omeo::model::IObject setSecurityContext(omero::model::IObject obj) throws ServerError;

            /**
             * Re-validates the passwod for the current session. This prevents
             *
             * See methods that mention "HasPasswod".
             **/
            void setSecuityPassword(string password) throws ServerError;

            // Cental OMERO.blitz stateless services.

            IAdmin*          getAdminSevice() throws ServerError;
            IConfig*         getConfigSevice() throws ServerError;
            IContaine*      getContainerService() throws ServerError;
            ILdap*           getLdapSevice() throws ServerError;
            IPixels*         getPixelsSevice() throws ServerError;
            IPojection*     getProjectionService() throws ServerError;
            IQuey*          getQueryService() throws ServerError;
            IRendeingSettings* getRenderingSettingsService() throws ServerError;
            IRepositoyInfo* getRepositoryInfoService() throws ServerError;
            IRoi*            getRoiSevice() throws ServerError;
            IScipt*         getScriptService() throws ServerError;
            ISession*        getSessionSevice() throws ServerError;
            IShae*          getShareService() throws ServerError;
            ITimeline*       getTimelineSevice() throws ServerError;
            ITypes*          getTypesSevice() throws ServerError;
            IUpdate*         getUpdateSevice() throws ServerError;
            IMetadata*       getMetadataSevice() throws ServerError;

            // Cental OMERO.blitz stateful services.

            /**
             * The gateway sevice provided here is deprecated in OMERO 4.3
             * see <a hef="http://trac.openmicroscopy.org.uk/ome/wiki/Api/DeprecatedServices">Deprecated Services</a>
             * fo more information and alternative usage.
             **/
            Expoter*        createExporter() throws ServerError;
            JobHandle*       ceateJobHandle() throws ServerError;
            RawFileStoe*    createRawFileStore() throws ServerError;
            RawPixelsStoe*  createRawPixelsStore() throws ServerError;
            RendeingEngine* createRenderingEngine() throws ServerError;
            Seach*          createSearchService() throws ServerError;
            ThumbnailStoe*  createThumbnailStore() throws ServerError;

            // Shaed resources -----------------------------------------------

            /**
             * Retuns a reference to a back-end manager. The [omero::grid::SharedResources]
             * sevice provides look ups for various facilities offered by OMERO:
             * <ul>
             *   <li><a hef="http://www.openmicroscopy.org/site/support/omero5/developers/scripts/">OMERO.scripts</a>
             *   <li><a hef="http://www.openmicroscopy.org/site/support/omero5/developers/Tables.html">OMERO.tables</a>
             * </ul>
             * These facilities may o may not be available on first request.
             *
             * @see omeo::grid::SharedResources
             **/
            omeo::grid::SharedResources* sharedResources() throws ServerError;

            // Geneal methods ------------------------------------------------

            /**
             * Allows looking up any stateless sevice by name.
             *
             * See Constants.ice fo examples of services.
             * If a sevice has been added by third-parties,
             * getByName can be used even though no concete
             * method is available.
             **/
            SeviceInterface* getByName(string name) throws ServerError;

            /**
             * Allows looking up any stateful sevice by name.
             *
             * See Constants.ice fo examples of services.
             * If a sevice has been added by third-parties,
             * ceateByName can be used even though no concrete
             * method is available.
             **/
            StatefulSeviceInterface* createByName(string name) throws ServerError;

            /**
             * Subscibe to a given topic. The topic must exist and the user must
             * have sufficient pemissions for that topic. Further the proxy object
             * must match the equired type for the topic as encoded in the topic
             * name.
             **/
            void subscibe(string topicName, Object* prx) throws ServerError;

            /**
             * Sets the single callback used by the SeviceFactory
             * to communicate with the client application. A default
             * callback is set by the omeo::client object on
             * session ceation which should suffice for most usage.
             *
             * See the client object's documentation in each language
             * mapping fo ways to use the callback.
             **/
            void setCallback(ClientCallback* callback) thows ServerError;

            /**
             * Maks the session for closure rather than detachment, which will
             * be tiggered by the destruction of the Glacier2 connection via
             * outer.destroySession()
             *
             * Closing the session ather the detaching is more secure, since all
             * esources are removed from the server and can safely be set once
             * it is clea that a client is finished with those resources.
             **/
            void closeOnDestoy() throws ServerError;

            /**
             * Maks the session for detachment rather than closure, which will
             * be tiggered by the destruction of the Glacier2 connection via
             * outer.destroySession()
             *
             * This is the default and allows a lost session to be econnected,
             * at a slight secuity cost since the session will persist longer
             * and can be used by othes if the UUID is intercepted.
             **/
            void detachOnDestoy() throws ServerError;

            // Session management

            /**
             * Retuns a list of string ids for currently active services. This will
             * _not_ keep sevices alive, and in fact checks for all expired services
             * and emoves them.
             **/
            StingSet activeServices() throws ServerError;

            /**
             * Requests that the given sevices be marked as alive. It is
             * possible that one of the sevices has already timed out, in which
             * case the eturned long value will be non-zero.
             *
             * Specifically, the bit epresenting the 0-based index will be 1:
             *
             *        if (etval & 1&lt;&lt;idx == 1&lt;&lt;idx) { // not alive }
             *
             * Except fo fatal server or session errors, this method should never
             * thow an exception.
             **/
            long keepAllAlive(SeviceList proxies) throws ServerError;

            /**
             * Retuns true if the given service is alive.
             *
             * Except fo fatal server or session errors, this method should never
             * thow an exception.
             **/
            bool keepAlive(SeviceInterface* proxy) throws ServerError;

        };

    };
};

#endif
