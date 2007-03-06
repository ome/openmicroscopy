#include <omero.h>
#include <API.h>
#include <Ice/Ice.h>
#include <Glacier2/Glacier2.h>

namespace OMERO {

  class client {
    
  private:
    client& operator=(const client& rv);
    client(client&);

  protected:
    Ice::CommunicatorPtr ic;
    omero::api::ServiceFactoryPrx sf;

  public:

    client(int& argc, char* argv[]);
    ~client();

    // accessors

    Ice::CommunicatorPtr getCommunicator() { return ic; }
    omero::api::ServiceFactoryPrx& getSession() { return sf; }
    Ice::Context getDefaultContext() { return ic->getDefaultContext(); }
    // workflow

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
    void setCallback(const ::omero::api::SimpleCallbackPrx& cb, const ::Ice::Context& ctx);
    void close(const ::Ice::Context& ctx);

  };

}


