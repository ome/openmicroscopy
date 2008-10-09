/*
 *   $Id$
 *
 *   Copyright 2007, 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <iosfwd>
#include <sstream>
#include <omero/client.h>
#include <omero/Constants.h>

using namespace std;

namespace omero {

    void client::init(const Ice::InitializationData& data) {

	// Not possible for id to be null since its a struct
	Ice::InitializationData id(data);

	if (!id.properties) {
	    id.properties = Ice::createProperties();
	}

	// Strictly necessary for this class to work
	id.properties->setProperty("Ice.ImplicitContext", "Shared");

	// Setting MessageSizeMax
	std::string messageSize = id.properties->getProperty("Ice.MessageSizeMax");
	if ( messageSize.length() == 0 ) {
	    stringstream ssmsgsize;
	    ssmsgsize << omero::constants::MESSAGESIZEMAX;
	    id.properties->setProperty("Ice.MessageSizeMax", ssmsgsize.str());
	}

	// Endpoints set to tcp if not present
	std::string endpoints = id.properties->getProperty("omero.client.Endpoints");
	if ( endpoints.length() == 0 ) {
	    id.properties->setProperty("omero.client.Endpoints", "tcp");
	}

	// Por, setting to default if not present
	std::string port = id.properties->getProperty("omero.port");
	if ( port.length() == 0 ) {
	    stringstream ssport;
	    ssport << omero::constants::GLACIER2PORT;
	    id.properties->setProperty("omero.port", ssport.str());
	}

	// Default Router, set a default and then replace
	std::string router = id.properties->getProperty("Ice.Default.Router");
	if ( router.length() == 0 ) {
	    router = omero::constants::DEFAULTROUTER;
	}
	std::string host = id.properties->getPropertyWithDefault("omero.host", "<\"omero.host\" not set >");

	std::string h_("@omero.host@");
	std::string p_("@omero.port@");
	size_t found = router.rfind(h_);
	if (found != string::npos) {
	    router.replace(found, h_.length(), host);
	}

	found = router.rfind(p_);
	if (found != string::npos) {
	    router.replace(found, p_.length(), router);
	}


	// Synchronization
	IceUtil::RecMutex::Lock lock(mutex);

	if ( ic ) {
	    throw new ClientError(__FILE__, __LINE__, "Client already initialized.");
	}

	ic = Ice::initialize(id);

	if ( ! ic ) {
	    throw new ClientError(__FILE__, __LINE__, "Improper initialization");
	}

	// Register Object Factory
	ObjectFactoryPtr of = new ObjectFactory();
	of->registerObjectFactory(ic);

	// Define our unique identifier (used during close/detach)
	Ice::ImplicitContextPtr ctx = ic->getImplicitContext();
	if (!ctx) {
	    throw omero::ClientError(__FILE__,__LINE__,"Ice.ImplicitContext not set to Shared");
	}
	ctx->put(omero::constants::CLIENTUUID, IceUtil::generateUUID());

	// Register the default client callback.
	CallbackIPtr cb = new CallbackI();
	Ice::ObjectAdapterPtr oa = ic->createObjectAdapter("omero.client");
	oa->add(cb, ic->stringToIdentity("ClientCallback"));
	oa->activate();

    }

    // --------------------------------------------------------------------

    client::client(int& argc, char* argv[],
	   const Ice::InitializationData& data) {

	Ice::InitializationData id(data);

	if ( ! id.properties ) {
	    id.properties = Ice::createProperties();
	}

	Ice::StringSeq args = Ice::argsToStringSeq(argc, argv);
	id.properties->parseIceCommandLineOptions(args);
	id.properties->parseCommandLineOptions("omero", args);
	init(id);
    }


    // --------------------------------------------------------------------


    client::client(const Ice::InitializationData& id) {
	init(id);
    }


    // --------------------------------------------------------------------

    client::client(const std::string& host, int port) {

	stringstream ss;
	ss << port;
	Ice::InitializationData id;
	id.properties = Ice::createProperties();
	id.properties->setProperty("omero.host", host);
	id.properties->setProperty("omero.port", ss.str());
	init(id);
    }


    // --------------------------------------------------------------------


    client::~client(){
	try {
	    closeSession();
	} catch (...) {
	    std::cout << "Ignoring error in ~client"<< std::endl;
	}
  }


    // Acessors
    // ===================================================================


    Ice::CommunicatorPtr client::getCommunicator() const {
	IceUtil::RecMutex::Lock lock(mutex);
	if ( ! ic ) {
	    throw new ClientError(__FILE__, __LINE__, "No Ice.Communicator active; call createSession()");
	}
	return ic;
    }


    // --------------------------------------------------------------------


    omero::api::ServiceFactoryPrx client::getSession() const {
	IceUtil::RecMutex::Lock lock(mutex);
	if ( ! sf ) {
	    throw new ClientError(__FILE__, __LINE__, "Call createSession() to login");
	}
	return sf;
    }


    // --------------------------------------------------------------------


    Ice::ImplicitContextPtr client::getImplicitContext() const {
	return getCommunicator()->getImplicitContext();
    }


    // --------------------------------------------------------------------


    Ice::PropertiesPtr client::getProperties() const {
	return getCommunicator()->getProperties();
    }


    // --------------------------------------------------------------------


    std::string client::getProperty(const std::string& key) const {
	return getCommunicator()->getProperties()->getProperty(key);
    }


    // Session management
    // ====================================================================


    omero::api::ServiceFactoryPrx client::joinSession(const std::string& _sessionUuid) {
	createSession(_sessionUuid, _sessionUuid);
    }


    // --------------------------------------------------------------------


    omero::api::ServiceFactoryPrx client::createSession(const std::string& _username, const std::string& _password) {

	IceUtil::RecMutex::Lock lock(mutex);

	// Checking state

	if ( ! sf ) {
	    throw new ClientError(__FILE__, __LINE__,
				  "Session already active. Create a new omero.client or closeSession()");
	}

	if ( ! ic ) {
	    if ( ! previous ) {
		throw new ClientError(__FILE__, __LINE__,
				      "No previous data to recreate communicator");
	    }
	    init(*previous);
	    previous = 0;
	}

	// Check the required properties

	std::string username, password;
	if ( _username.empty() ) {
	    username = getProperty(omero::constants::USERNAME);
	    if ( username.empty() ) {
		throw omero::ClientError(__FILE__,__LINE__,"No username provided");
	    }
	} else {
	    username = _username;
	}

	if (_password.empty()) {
	    password = getProperty(omero::constants::PASSWORD);
	} else {
	    password = _password;
	}

	// Acquire router and get the proxy
	Glacier2::SessionPrx prx = getRouter()->createSession(username, password);
	if ( ! prx ) {
	    throw omero::ClientError(__FILE__,__LINE__,"Obtained null object proxy");
	}

	// Check type
	sf = omero::api::ServiceFactoryPrx::uncheckedCast(prx);
	if ( ! sf ) {
	    throw omero::ClientError(__FILE__,__LINE__,"Obtained object proxy is not a ServiceFactory.");
	}
	return sf;
    }


    // --------------------------------------------------------------------


    Glacier2::RouterPrx const client::getRouter() const {

	IceUtil::RecMutex::Lock lock(mutex);

	Ice::RouterPrx prx = ic->getDefaultRouter();
	if ( ! prx ) {
	    throw omero::ClientError(__FILE__,__LINE__,"No default router found.");
	}

	Glacier2::RouterPrx router = Glacier2::RouterPrx::checkedCast(prx);
	if ( ! router ) {
	    throw new ClientError(__FILE__, __LINE__, "Error obtaining Glacier2 router");
	}

	// For whatever reason, we have to se the context
	// on the router context here as well.
	router = Glacier2::RouterPrx::uncheckedCast(router->ice_context(ic->getImplicitContext()->getContext()));

	return router;
    }


    // --------------------------------------------------------------------


    void client::exit() {
	Ice::CommunicatorPtr copy(ic);
	ic = Ice::CommunicatorPtr();
	copy->destroy();
    }

    void client::closeSession() {

	omero::api::ServiceFactoryPrx old = sf;
	sf = omero::api::ServiceFactoryPrx();

	// This is unlikely, but a last ditch effort
	if ( ! ic && old ) {
	    ic = old->ice_getCommunicator();
	}

	if ( ! ic) {
	    return; // EARLY EXIT
	}

	try {
	    getRouter()->destroySession();
	} catch (const Glacier2::SessionNotExistException& snee) {
	    // ok. We don't want it to exist
	    exit();
	} catch (const Ice::ConnectionLostException& cle) {
	    // ok. Exception will always be thrown.
	    exit();
	} catch (...) {
	    exit();
	    throw;
	}

    }


    // File handling
    // ====================================================================


    std::string client::sha1(const std::string& file) {
	throw new ClientError(__FILE__, __LINE__, "Not yet implemented");
    }


    // --------------------------------------------------------------------


    void client::upload(const std::string& file,
			       const omero::model::OriginalFilePtr& ofile,
			       int blockSize) {
	throw new ClientError(__FILE__, __LINE__, "Not yet implemented");
    }


    // Environment methods
    // ====================================================================


    omero::RTypePtr client::getInput(const string& key) {
	return env()->getInput(sess(), key);
    }
    omero::RTypePtr client::getOutput(const string& key) {
	return env()->getOutput(sess(), key);
    }
    void client::setInput(const string& key, const omero::RTypePtr& value) {
	env()->setInput(sess(), key, value);
    }
    void client::setOutput(const string& key, const omero::RTypePtr& value) {
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

    // Misc
    // ====================================================================

    CallbackI::CallbackI() {
	// no-op at the moment.
    }

    bool CallbackI::ping(const Ice::Current& current) {
	return true;
    }

    void CallbackI::shutdownIn(Ice::Long milliSeconds, const Ice::Current& current) {
	// no-op
    }

}

ostream& operator<<(ostream& os, const omero::model::IObjectPtr ptr) {
  if (!ptr) {
    os << "null";
  } else {
    os << ptr->ice_staticId() << ":";
    if (!ptr->getId()) {
      os << "null_id";
    } else {
      os << ptr->getId();
    }
  }
  return os;
}
