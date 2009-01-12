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
#include <omero/RTypesI.h>
#include <omero/Constants.h>

using namespace std;

namespace omero {

    void client::init(const Ice::InitializationData& data) {

	// Not possible for id to be null since its a struct
	Ice::InitializationData id(data);

	if (!id.properties) {
	    int argc = 0;
	    char* argv[] = {0};
	    id.properties = Ice::createProperties(argc, argv);
	}

	// Strictly necessary for this class to work
	id.properties->setProperty("Ice.ImplicitContext", "Shared");

	// C++ only
	std::string gcInterval = id.properties->getProperty("Ice.GC.Interval");
	if ( gcInterval.length() == 0 ) {
	    stringstream ssgcInt;
	    ssgcInt << omero::constants::GCINTERVAL;
	    id.properties->setProperty("Ice.GC.Interval", ssgcInt.str());
	}

	// Setting MessageSizeMax
	std::string messageSize = id.properties->getProperty("Ice.MessageSizeMax");
	if ( messageSize.length() == 0 ) {
	    stringstream ssmsgsize;
	    ssmsgsize << omero::constants::MESSAGESIZEMAX;
	    id.properties->setProperty("Ice.MessageSizeMax", ssmsgsize.str());
	}

	// Endpoints set to tcp if not present
	std::string endpoints = id.properties->getProperty("omero.ClientCallback.Endpoints");
	if ( endpoints.length() == 0 ) {
	    id.properties->setProperty("omero.ClientCallback.Endpoints", "tcp");
	}

	// Por, setting to default if not present
	std::string port = id.properties->getProperty("omero.port");
	if ( port.length() == 0 ) {
	    stringstream ssport;
	    ssport << omero::constants::GLACIER2PORT;
	    id.properties->setProperty("omero.port", ssport.str());
	    port = ssport.str();
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
	    router.replace(found, p_.length(), port);
	}
	id.properties->setProperty("Ice.Default.Router", router);

	// Dump properties
	std::string dump = id.properties->getProperty("omero.dump");
	if ( dump.length() != 0 ) {
	    Ice::PropertyDict omeroProperties = id.properties->getPropertiesForPrefix("omero");
	    Ice::PropertyDict::const_iterator beg = omeroProperties.begin();
	    Ice::PropertyDict::const_iterator end = omeroProperties.end();
	    while (beg != end) {
		std::cout << (*beg).first << "=" << (*beg).second << std::endl;
		beg++;
	    }
	    Ice::PropertyDict iceProperties = id.properties->getPropertiesForPrefix("Ice");
	    beg = iceProperties.begin();
	    end = iceProperties.end();
	    while (beg != end) {
		std::cout << (*beg).first << "=" << (*beg).second << std::endl;
		beg++;
	    }
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
	omero::registerObjectFactory(ic);
	map<std::string, omero::rtypes::ObjectFactoryPtr> factories = omero::rtypes::objectFactories();
	map<std::string, omero::rtypes::ObjectFactoryPtr>::iterator itr;
	for(itr = factories.begin(); itr != factories.end(); itr++) {
	    (*itr).second->register_(ic);
	}

	// Define our unique identifier (used during close/detach)
	Ice::ImplicitContextPtr ctx = ic->getImplicitContext();
	if (!ctx) {
	    throw omero::ClientError(__FILE__,__LINE__,"Ice.ImplicitContext not set to Shared");
	}
	ctx->put(omero::constants::CLIENTUUID, IceUtil::generateUUID());

	// Register the default client callback.
	CallbackIPtr cb = new CallbackI(this);
	oa = ic->createObjectAdapter("omero.ClientCallback");
	oa->add(cb, ic->stringToIdentity("ClientCallback"));
	oa->activate();

    }

    // --------------------------------------------------------------------

    client::client(int& argc, char* argv[],
	   const Ice::InitializationData& data) {

	Ice::InitializationData id(data);

	if ( ! id.properties ) {
	    // Dummy properties to be passed in.
	    int _argc = 0;
	    char* _argv[] = {0};
	    id.properties = Ice::createProperties(_argc, _argv);
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
	int argc = 0;
	char* argv[] = {0};
	stringstream ss;
	ss << port;
	Ice::InitializationData id;
	id.properties = Ice::createProperties(argc, argv);
	id.properties->setProperty("omero.host", host);
	id.properties->setProperty("omero.port", ss.str());
	init(id);
    }


    // --------------------------------------------------------------------


    client::~client(){
	try {
	    closeSession();
	} catch (const std::exception& ex) {
            std::cout << ex.what() << std::endl;
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
	return createSession(_sessionUuid, _sessionUuid);
    }


    // --------------------------------------------------------------------


    omero::api::ServiceFactoryPrx client::createSession(const std::string& _username, const std::string& _password) {

	IceUtil::RecMutex::Lock lock(mutex);

	// Checking state

	if ( sf ) {
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
	Glacier2::SessionPrx prx = getRouter(ic)->createSession(username, password);
	if ( ! prx ) {
	    throw omero::ClientError(__FILE__,__LINE__,"Obtained null object proxy");
	}

	// Check type
	sf = omero::api::ServiceFactoryPrx::uncheckedCast(prx);
	if ( ! sf ) {
	    throw omero::ClientError(__FILE__,__LINE__,"Obtained object proxy is not a ServiceFactory.");
	}

	// Set the client callback on the session
	Ice::ObjectPrx raw = oa->stringToProxy("ClientCallback");
	sf->setCallback(ClientCallbacPrx::uncheckedCast(raw));
	return sf;
    }


    // --------------------------------------------------------------------


    Glacier2::RouterPrx const client::getRouter(const Ice::CommunicatorPtr& comm) const {

	Ice::RouterPrx prx = comm->getDefaultRouter();
	if ( ! prx ) {
	    throw omero::ClientError(__FILE__,__LINE__,"No default router found.");
	}

	Glacier2::RouterPrx router = Glacier2::RouterPrx::checkedCast(prx);
	if ( ! router ) {
	    throw new ClientError(__FILE__, __LINE__, "Error obtaining Glacier2 router");
	}

	// For whatever reason, we have to set the context
	// on the router context here as well.
	router = Glacier2::RouterPrx::uncheckedCast(router->ice_context(comm->getImplicitContext()->getContext()));

	return router;
    }


    // --------------------------------------------------------------------

    void client::closeSession() {

	IceUtil::RecMutex::Lock lock(mutex);

	omero::api::ServiceFactoryPrx oldSf = sf;
	sf = omero::api::ServiceFactoryPrx();

	Ice::ObjectAdapterPtr oldOa = oa;
	oa = Ice::ObjectAdapterPtr();

	Ice::CommunicatorPtr oldIc = ic;
	ic = Ice::CommunicatorPtr();

	// Only possible if improperly configured
	if (! oldIc) {
	    return; // EARLY EXIT!
	}

	if (oldOa) {
	    try {
		oldOa.deactivate();
	    } catch (...) {
		oldIc.getLogger().warn("While deactivating adapter : TBD");
	    }
	}

        previous = new Ice::InitializationData();
        (*previous).properties = oldIc->getProperties()->clone();

	try {
	    getRouter(oldIc)->destroySession();
	} catch (const Glacier2::SessionNotExistException& snee) {
	    // ok. We don't want it to exist
	    oldIc.destroy();
	} catch (const Ice::ConnectionLostException& cle) {
	    // ok. Exception will always be thrown.
	    oldIc.destroy();
        } catch (const omero::ClientError& ce) {
            // This is called by getRouter() if a router is not configured.
            // If there isn't one, then we can't be connected. That's alright.
            // Most likely called during ~client
	    oldIc.destroy();
	} catch (...) {
	    oldIc.destroy();
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
	return env()->getInputKeys(sess());
    }
    vector<string> client::getOutputKeys() {
	return env()->getOutputKeys(sess());
    }
    omero::api::ISessionPrx client::env() {
	return sf->getSessionService();
    }
    const std::string client::sess() {
	return sf->getAdminService()->getEventContext()->sessionUuid;
    }

    // Callback methods
    // ====================================================================

    CallbackIPtr client::_getCb() {
	Ice::ObjectPtr obj = oa->find(ic->stringToIdentity("ClientCallback"));
	CallbackIPtr cb = CallbackI::dynamic_cast(obj);
	if (!cb) {
	    throw new ClientError(__FILE__,__LINE__,"Cannot find CallbackI in ObjectAdapter");
	}
	return cb;
    }

    void client::onHeartbeat(Callable callable) {
	_getCb()->onHeartbeat = callable;
    }

    void client::onSessionClosed(Callable callable) {
	_getCb()->onSessionClosed = callable;
    }

    void client::onShutdown(Callable callable) {
	_getCb()->onShutdown = callable;
    }

    CallbackI::noop = new Callable() {};

    CallbackI::closeSession = new Callable(){};

    CallbackI::onHeartbeat = CallbackI::noop;

    CallbackI::onSessionClosed = CallbackI::closeSession;

    CallbackI::onShutdown = CallbackI::closeSession;

    CallbackI::CallbackI(const omero::client& ptr) {
	client = ptr;
    }

    void CallbackI::requestHeartbeat(const Ice::Current& current) {
	execute(onHeartbeat, "heartbeat");
    }

    void CallbackI::sessionClosed(const Ice::Current& current) {
	execute(onSessionClosed, "sessionClosed");
    }

    void CallbackI::shutdownIn(const Ice::Current& current) {
	execute(onShutdown, "shutdown");
    }

    void CallbackI::execute(Callable callable, const string& action) {
	Ice::CommunicatorPtr ic = client.getCommunicator();
	try {
	    callable();
	    ic->getLogger()->trace("ClientCallback", action + " run");
	} catch (...) {
	    try {
		ic->getLogger()->error("Error performing " + action+": "+TBD);
	    } catch (...) {
		std::cerr << "Error performing " << action << ": " << TBD << std::endl;
		std::cerr << "(Stderr due to: " << TBD << std::endl;
	    }
	}
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
	omero::RLongPtr id = ptr->getId();
	if (id) {
	    os << "null";
	} else {
	    os << id->getValue();
	}
    }
  }
  return os;
}
