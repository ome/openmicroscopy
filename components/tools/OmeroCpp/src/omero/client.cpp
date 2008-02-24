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
    ObjectFactoryPtr of = new ObjectFactory();
    of->registerObjectFactory(ic);
  }

  client::client(const Ice::InitializationData& id) {
    ic = Ice::initialize(id);
    close_on_destroy = false;
    init(ic);
  }

  client::client(int& argc, char* argv[],
		 const Ice::InitializationData& id) {
    ic = Ice::initialize(argc, argv, id);
    close_on_destroy = false;
    init(ic);
  }

  client::~client(){
      if (close_on_destroy && sf) {
          closeSession();
      }
      if (ic) {
          try {
              ic->destroy();
          } catch (const Ice::Exception& ex) {
              cerr << "Caught Ice exception while destroying communicator." << endl;
              cerr << ex << endl;
          }
          ic = Ice::CommunicatorPtr();
      }
  }

  omero::api::ServiceFactoryPrx client::createSession(const std::string& _username, const std::string& _password) {

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

    Ice::RouterPrx prx = ic->getDefaultRouter();
    if (!prx) {
	throw omero::ClientError(__FILE__,__LINE__,"No default router found.");
    }
    Glacier2::RouterPrx router = Glacier2::RouterPrx::checkedCast(prx);
    if (!router) {
	throw omero::ClientError(__FILE__,__LINE__,"Error obtaining Glacier2 router.");
    }

    Glacier2::SessionPrx session;
    session = router->createSession(username, password);
    sf = omero::api::ServiceFactoryPrx::checkedCast(session);
    if (!sf) {
      throw omero::ClientError(__FILE__,__LINE__,"No session obtained.");
    }
    return sf;
  }

  void client::closeSession() {
    if (sf) {
      try {
        sf->close();
      } catch (const Ice::Exception& ex) {
        // ok
      }
      sf = omero::api::ServiceFactoryPrx();
    }
    Ice::RouterPrx prx = ic->getDefaultRouter();
    Glacier2::RouterPrx router = Glacier2::RouterPrx::checkedCast(prx);
    try {
        router->destroySession();
    } catch (const Ice::ConnectionLostException& cle) {
        // ok. Always thrown.
    }
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
