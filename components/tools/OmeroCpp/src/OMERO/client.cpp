/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <OMERO/client.h>

using namespace std;

namespace OMERO {
  
  client::client(int& argc, char* argv[],
		 const Ice::InitializationData& id) {
    ic = Ice::initialize(argc, argv, id);
    if (!ic) {
      throw omero::ClientError(__FILE__,__LINE__,"Improper initialization.");
    }
    ObjectFactoryPtr of = new ObjectFactory();
    of->registerObjectFactory(ic);
  }
  
  client::~client(){
    if (ic) {
      try {
	ic->destroy();
      } catch (const Ice::Exception& ex) {
	cerr << "Caught Ice exception while destroying communicator." << endl;
	cerr << ex << endl;
      }	
    }
  }
  
  void client::createSession() {
    
    string username = getProperty("OMERO.username");
    string password = getProperty("OMERO.password");

    Ice::RouterPrx prx = ic->getDefaultRouter();
    Glacier2::RouterPrx router = Glacier2::RouterPrx::checkedCast(prx);
    Glacier2::SessionPrx session;
    session = router->createSession(username, password);
    sf = omero::api::ServiceFactoryPrx::checkedCast(session);
    if (!sf) {
      throw omero::ClientError(__FILE__,__LINE__,"No session obtained.");
    }
  }
  
  omero::api::IAdminPrx client::getAdminService() {return sf->getAdminService();}
  omero::api::IAdminPrx client::getAdminService(const ::Ice::Context& ctx) {return sf->getAdminService(ctx);}
  
  omero::api::IConfigPrx client::getConfigService() {return sf->getConfigService();}
  omero::api::IConfigPrx client::getConfigService(const ::Ice::Context& ctx) {return sf->getConfigService(ctx);}
  
  omero::api::IPixelsPrx client::getPixelsService() {return sf->getPixelsService();}
  omero::api::IPixelsPrx client::getPixelsService(const ::Ice::Context& ctx) {return sf->getPixelsService(ctx);}
  
  omero::api::IPojosPrx client::getPojosService() {return sf->getPojosService();}
  omero::api::IPojosPrx client::getPojosService(const ::Ice::Context& ctx) {return sf->getPojosService(ctx);}
  
  omero::api::IQueryPrx client::getQueryService() {return sf->getQueryService();}
  omero::api::IQueryPrx client::getQueryService(const ::Ice::Context& ctx) {return sf->getQueryService(ctx);}

  omero::api::IRepositoryInfoPrx client::getRepositoryInfoService() {return sf->getRepositoryInfoService();}
  omero::api::IRepositoryInfoPrx client::getRepositoryInfoService(const ::Ice::Context& ctx) {return sf->getRepositoryInfoService(ctx);}
  
  omero::api::ITypesPrx client::getTypesService() {return sf->getTypesService();}
  omero::api::ITypesPrx client::getTypesService(const ::Ice::Context& ctx) {return sf->getTypesService(ctx);}
  
  omero::api::IUpdatePrx client::getUpdateService() {return sf->getUpdateService();}
  omero::api::IUpdatePrx client::getUpdateService(const ::Ice::Context& ctx) {return sf->getUpdateService(ctx);}
  
  omero::api::RawFileStorePrx client::createRawFileStore() {return sf->createRawFileStore();}
  omero::api::RawFileStorePrx client::createRawFileStore(const ::Ice::Context& ctx) {return sf->createRawFileStore(ctx);}
  
  omero::api::RawPixelsStorePrx client::createRawPixelsStore() {return sf->createRawPixelsStore();}
  omero::api::RawPixelsStorePrx client::createRawPixelsStore(const ::Ice::Context& ctx) {return sf->createRawPixelsStore(ctx);}
  
  omero::api::RenderingEnginePrx client::createRenderingEngine() {return sf->createRenderingEngine();}
  omero::api::RenderingEnginePrx client::createRenderingEngine(const ::Ice::Context& ctx) {return sf->createRenderingEngine(ctx);}
  
  omero::api::ThumbnailStorePrx client::createThumbnailStore() {return sf->createThumbnailStore();}
  omero::api::ThumbnailStorePrx client::createThumbnailStore(const ::Ice::Context& ctx) {return sf->createThumbnailStore(ctx);}

  Ice::ObjectPrx client::getByName(const string& name) {return sf->getByName(name);}
  Ice::ObjectPrx client::getByName(const string& name, const ::Ice::Context& ctx) {return sf->getByName(name, ctx);}

  void client::setCallback(const ::omero::api::SimpleCallbackPrx& cb) {sf->setCallback(cb);}
  void client::setCallback(const ::omero::api::SimpleCallbackPrx& cb, const ::Ice::Context& ctx) {sf->setCallback(cb, ctx);}
 
  void client::close() {sf->close();}
  void client::close(const ::Ice::Context& ctx) {sf->close(ctx);}

}

ostream& operator<<(ostream& os, const omero::model::IObjectPtr ptr) {
  if (!ptr) {
    os << "null";
  } else {
    os << ptr->ice_staticId() << ":";
    if (!ptr->id) {
      os << "null_id";
    } else {
      os << ptr->id; 
    } 
  }
  return os;
}
