/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <OMERO/common.h>
#include <OMERO/types.h>
#include <Ice/Ice.h>
#include <Glacier2/Glacier2.h>
#include <string>
namespace OMERO {

  /*
   * The OMERO::client class is the main entry point for consumers of
   * the OMERO.blitz server. An instance provides access to an
   * Ice::Communicator which is the central Ice interface for all
   * communication. An instance also provides access to a single
   * omero::api::ServiceFactoryPrx which is the blitz session facade,
   * from which all other proxies can be obtained. Once the
   * ServiceFactoryPrx is destroyed, times out, or close() is called,
   * all proxies obtained from this instance are also destroyed.
   */
  class client {

    // Preventing copy-construction and assigning by value.
  private:
    client& operator=(const client& rv);
    client(client&);

    // These are the central instances provided by this class.
  protected:
    Ice::CommunicatorPtr ic;
    omero::api::ServiceFactoryPrx sf;

  public:

    /*
     * The constructor takes the arguments passed into int main()
     * since Ice's main configuration methods center on parsing the
     * command-line arguments for several reserved keywords
     * ("--Ice.*", "--Glacier2.*", etc.) --Ice.Config is the most
     * important of these and specifies a comma-separated list of
     * configuration file (which can alternatively be specified via
     * the ICE_CONFIG environment property). Command-line arguments
     * with the reserved prefixes, however, override all other values.
     */
    client(int& argc, char* argv[]);

    /*
     * Destroys the session and the communicator instances.
     */
    ~client();

    // Accessors:

    Ice::CommunicatorPtr getCommunicator() { return ic; }
    omero::api::ServiceFactoryPrx& getSession() { return sf; }
    Ice::Context getDefaultContext() { return ic->getDefaultContext(); }
    Ice::PropertiesPtr getProperties() { return ic->getProperties(); }
    std::string getProperty(const std::string& key) { return getProperties()->getProperty(key); }

    // Session management

    /* Creates a session. Calling this method while a session is
     * active will throw an exception. It should only be used again,
     * after a session timeout exception, or similar.
     */
    void createSession();
    omero::api::IAdminPrx getAdminService(const ::Ice::Context& ctx);
    omero::api::IConfigPrx getConfigService(const ::Ice::Context& ctx);
    omero::api::IPixelsPrx getPixelsService(const ::Ice::Context& ctx);
    omero::api::IPojosPrx getPojosService(const ::Ice::Context& ctx);
    omero::api::IQueryPrx getQueryService(const ::Ice::Context& ctx);
    omero::api::ITypesPrx getTypesService(const ::Ice::Context& ctx);
    omero::api::IUpdatePrx getUpdateService(const ::Ice::Context& ctx);
    omero::api::RawFileStorePrx createRawFileStore(const ::Ice::Context& ctx);
    omero::api::RawPixelsStorePrx createRawPixelsStore(const ::Ice::Context& ctx);
    omero::api::RenderingEnginePrx createRenderingEngine(const ::Ice::Context& ctx);
    omero::api::ThumbnailStorePrx createThumbnailStore(const ::Ice::Context& ctx);
    Ice::ObjectPrx getByName(const std::string& name, const ::Ice::Context& ctx);

    /*
     * Closes the session AND all proxies created by it.
     */
    void close(const ::Ice::Context& ctx);

    /* 
     * The callback is currently unused. Rather, this is an example of
     * what a callback would look like.
     */
    void setCallback(const ::omero::api::SimpleCallbackPrx& cb, const ::Ice::Context& ctx);

  };

}
