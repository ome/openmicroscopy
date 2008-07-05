/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <omero/client.h>

using namespace std;

namespace omero {

  void init(Ice::CommunicatorPtr ic) {
    if (!ic) {
      throw omero::ClientError(__FILE__,__LINE__,"Improper initialization.");
    }
    // Register Object Factory
    ObjectFactoryPtr of = new ObjectFactory();
    of->registerObjectFactory(ic);
    // Define our unique identifier (used during close/detach)
    ic->getImplicitContext()->put(omero::constants::CLIENTUUID, IceUtil::generateUUID());
  }

  client::client(const Ice::InitializationData& id) {
    ic = Ice::initialize(id);
    init(ic);
  }

  client::client(int& argc, char* argv[],
		 const Ice::InitializationData& id) {
    ic = Ice::initialize(argc, argv, id);
    init(ic);
  }

  client::~client(){
      try {
          closeSession();
      } catch (...) {
	  std::cout << "Ignoring error in ~client"<< std::endl;
      }
  }

  omero::api::ServiceFactoryPrx client::createSession(const std::string& _username, const std::string& _password) {

      // Check the required properties
    std::string username, password;
    if (_username.empty()) {
      username = getProperty(omero::constants::USERNAME);
      if (username.empty()) {
        throw omero::ClientError(__FILE__,__LINE__,"No username provided");
      }
    } else {
      username = _username;
    }

    if (_password.empty()) {
      password = getProperty(omero::constants::PASSWORD);
      if (password.empty()) {
        throw omero::ClientError(__FILE__,__LINE__,"No password provided");
      }
    } else {
      password = _password;
    }

    // Acquire router and get the proxy
    // For whatever reason, we have to se the context
    // on the router context here as well.
    Ice::RouterPrx prx = ic->getDefaultRouter();
    if (!prx) {
	throw omero::ClientError(__FILE__,__LINE__,"No default router found.");
    }
    prx = Ice::RouterPrx::uncheckedCast(prx->ice_context(ic->getImplicitContext()->getContext()));
    Glacier2::RouterPrx router = Glacier2::RouterPrx::checkedCast(prx);
    if (!router) {
	throw omero::ClientError(__FILE__,__LINE__,"Error obtaining Glacier2 router.");
    }

    Glacier2::SessionPrx session;
    session = router->createSession(username, password);
    if (!session) {
	throw omero::ClientError(__FILE__,__LINE__,"Obtained null object proxy");
    }

    // Check type
    sf = omero::api::ServiceFactoryPrx::checkedCast(session);
    if (!sf) {
      throw omero::ClientError(__FILE__,__LINE__,"Obtained object proxy is not a ServiceFactory.");
    }
    return sf;
  }

  void client::closeSession() {

      omero::api::ServiceFactoryPrx old = sf;
      sf = omero::api::ServiceFactoryPrx();

      if (!ic && old) {
	  ic = old->ice_getCommunicator();
      }

      if (ic) {

	  Ice::RouterPrx prx = ic->getDefaultRouter();
	  Glacier2::RouterPrx router = Glacier2::RouterPrx::checkedCast(prx);
	  if (router) {
	      try {
		  router->destroySession();
	      } catch (const Ice::ConnectionLostException& cle) {
		  // ok. Always thrown.
	      } // TODO what about SNEE
	  }

          try {
              ic->destroy();
          } catch (const Ice::Exception& ex) {
              cerr << "Caught Ice exception while destroying communicator." << endl;
              cerr << ex << endl;
          }
          ic = Ice::CommunicatorPtr();
      }

  }

    // Environment methods
    // ======================================================================
    omero::RTypePtr client::getInput(const string& key) {
	return env()->getInput(sess(), key);
    }
    omero::RTypePtr client::getOutput(const string& key) {
	return env()->getOutput(sess(), key);
    }
    void client::setInput(const string& key, omero::RTypePtr value) {
	env()->setInput(sess(), key, value);
    }
    void client::setOutput(const string& key, omero::RTypePtr value) {
	env()->setOutput(sess(), key, value);
    }
    vector<string> client::getInputKeys() {
	env()->getInputKeys(sess());
    }
    vector<string> client::getOutputKeys() {
	env()->getOutputKeys(sess());
    }
    omero::api::ISessionPrx client::env() {
	return sf->getSessionService();
    }
    const std::string client::sess() {
	return sf->getAdminService()->getEventContext()->sessionUuid;
    }
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
