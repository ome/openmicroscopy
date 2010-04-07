/*
 *   $Id$
 *
 *   Copyright 2007, 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_CLIENT_H
#define OMERO_CLIENT_H

#include <omero/API.h>
#include <omero/ClientErrors.h>
#include <omero/Collections.h>
#include <omero/Constants.h>
#include <omero/ObjectFactoryRegistrar.h>
#include <omero/ServerErrors.h>
#include <omero/ServicesF.h>
#include <omero/System.h>
#include <omero/model/OriginalFile.h>
#include <Ice/Ice.h>
#include <IceUtil/Handle.h>
#include <IceUtil/RecMutex.h>
#include <Glacier2/Glacier2.h>
#include <string>
#include <iosfwd>

#ifndef OMERO_API
#   ifdef OMERO_API_EXPORTS
#       define OMERO_API ICE_DECLSPEC_EXPORT
#   else
#       define OMERO_API ICE_DECLSPEC_IMPORT
#   endif
#endif

namespace omero {

    /*
     * Simple type for use with the ClientCallback type below.
     */
    class OMERO_API Callable {
    public:
	Callable() {};
	void operator()() {};
    };

    /*
     * Forward definition for callbacks below.
     */
    class CallbackI;
    typedef IceUtil::Handle<CallbackI> CallbackIPtr;

    /*
     * Central client-side blitz entry point and should be in sync with
     * OmeroJavas and OmeroPy's omero.client classes.
     *
     * Typical usage:
     *   omero::client client               // Uses ICE_CONFIG
     *   omero::client client(host);        // Defines "omero.host"
     *   omero::client client(host, port);  // Defines "omero.host" and "omero.port"
     *
     *   omero::client::~client() called on scope exit.
     *
     * For more information, see:
     *
     *    https://trac.openmicroscopy.org.uk/omero/wiki/ClientDesign
     *
     */
    class OMERO_API client : public IceUtil::Shared {

	// Preventing copy-construction and assigning by value.
    private:
	client& operator=(const client& rv);
	client(client&);

	// These are the central instances provided by this class.
    protected:

	/*
         * See setAgent(string)
	 */
	std::string __agent;

	/*
	 * Identifier for this client instance. Multiple client uuids may be
	 * attached to a single session uuid.
	 */
	std::string __uuid;

	/*
	 * InitializationData from the last communicator used to create
	 * ic if nulled after closeSession(). A pointer is used since
	 * ID is a simple struct.
	 */
	Ice::InitializationData* __previous;

	/*
	 * Ice.ObjectAdapter containing the ClientCallback for
	 * this instance.
	 */
	Ice::ObjectAdapterPtr __oa;

	/*
	 * Single communicator for this omero::client. Nullness is used as
	 * a test of what state the client is in, therefore all access is
	 * synchronized by locking on mutex.
	 */
	Ice::CommunicatorPtr __ic;

	/*
	 * Single session for this omero::client. Nullness is used as a test
	 * of what state the client is in, like ic, therefore all access is
	 * synchronized by locking on mutex.
	 */
	omero::api::ServiceFactoryPrx __sf;

	/*
	 * Lock (mutex) for all access to ic and sf
	 */
	IceUtil::RecMutex mutex;

	/*
	 * Initializes the current client via an InitializationData instance.
	 * This is called by all of the constructors, but may also be called
	 * on createSession() if a previous call to closeSession() has nulled
	 * the Ice::Communicator.
	 */
	void init(const Ice::InitializationData& id);

    public:

	/*
	 * Creates an Ice::Communicator from command-line arguments. These
	 * are parsed via Ice::Properties::parseIceCommandLineOptions(args) and
	 * Ice::Properties::parseCommandLineOptions("omero", args)
	 */
	client(int& argc, char* argv[],
	       const Ice::InitializationData& id = Ice::InitializationData());

	/*
	 * Default constructor which can only parse the ICE_CONFIG environment
	 * variable in a manner similar to that of the --Ice.Config file.
	 */
	client(const Ice::InitializationData& id = Ice::InitializationData());

	/*
	 * Creates an Ice::Communicator pointing at the given server and port,
	 * which defaults to omero::constants::GLACIER2PORT if none is given.
	 */
	client(const std::string& host, int port = omero::constants::GLACIER2PORT);

        /*
         * Sets the omero.model.Session#getUserAgent() string for
         * this client. Every session creation will be passed this argument. Finding
         * open sesssions with the same agent can be done via
         * omero.api.ISessionPrx#getMyOpenAgentSessions(String).
         */
        void setAgent(const std::string& agent);

	/*
	 * Calls closeSession() and ignores all exceptions.
	 */
	~client();


	// Accessors
	// ==============================================================


	/*
	 * Returns the Ice::Communicator for this instance or throws
	 * an exception if null.
	 */
	Ice::CommunicatorPtr getCommunicator() const;

	/*
	 * Returns the current active session or throws an exception if none
	 * has been created via createSession() since the last closeSession()
	 */
	omero::api::ServiceFactoryPrx getSession() const;

	/*
	 * Returns the Ice::ImplicitContext which defiens what properties
	 * will be sent on every method invocation.
	 */
	Ice::ImplicitContextPtr getImplicitContext() const;

	/*
	 * Returns the active Ice.Properties for this instance.
	 */
	Ice::PropertiesPtr getProperties() const;

	/*
	 * Returns the property value for this key or the empty string if none.
	 */
	std::string getProperty(const std::string& key) const;


	// Session management
	// ================================================================

	/*
	 * Uses the given uuid as name and password to rejoin a running session.
	 */
	omero::api::ServiceFactoryPrx joinSession(const std::string& sessionUuid);

	/*
	 * Creates a session. Calling this method while a session is
	 * active will throw an exception. It should only be used again,
	 * after a session timeout exception, or similar.
	 */
	omero::api::ServiceFactoryPrx createSession(const std::string& username = std::string(), const std::string& password = std::string());

	/*
	 * Acquires the Ice::Communicator::getDefaultRouter() and throws an exception
	 * if it is not of type Glacier2::RouterPrx. Also sets the ImplicitContext
	 * on the router proxy.
	 */
	Glacier2::RouterPrx const getRouter(const Ice::CommunicatorPtr& comm) const;

	/*
	 * Calculates the local sha1 for a file.
	 */
	std::string sha1(const std::string& file);

	/*
	 * Utility method to upload a file. The original file can be a null pointer, and the
	 * block size can be 0 or negative to use defaults. The string must point to a valid
	 * file.
	 */
	void upload(const std::string& file,
		    const omero::model::OriginalFilePtr& ofile,
		    int blockSize);

	/*
	 * Closes the Router connection created by createSession(). Due to a bug in Ice,
	 * only one connection is allowed per communicator, so we also destroy the
	 * communicator.
	 *
	 * http://www.zeroc.com/forums/help-center/2370-ice_ping-error-right-after-createsession-succeed.html
	 */
	void closeSession();


	// Environment methods. Allows to store and retrieve
	// ==================================================================


	/**
	 * Sets an item in the "input" shared (session) memory under the given name.
	 */
	omero::RTypePtr getInput(const std::string& key);

	/**
	 * Sets an item in the "output" shared (session) memory under the given name.
	 */
	omero::RTypePtr getOutput(const std::string& key);

	/*
	 * Sets an item in the "input" shared (session) memory under the given name.
	 */
	void setInput(const std::string& key, const omero::RTypePtr& value);

	/*
	 * Sets an item in the "output" shared (session) memory under the given name.
	 */
	void setOutput(const std::string& key, const omero::RTypePtr& value);

	/*
	 * Returns a list of keys for all items in the "input" shared (session) memory
	 */
	std::vector<std::string> getInputKeys();

	/*
	 * Returns a list of keys for all items in the "output" shared (session) memory
	 */
	std::vector<std::string> getOutputKeys();
    protected:
	const std::string sess();
	omero::api::ISessionPrx env();
	std::string parseAndSetInt(const Ice::InitializationData& id,
				   const std::string& key, int port);

	// Callback methods
	// ==================================================================

	/*
	 * Implementation of ClientCallback which will be added to
	 * any Session which this instance creates
	 */
    protected:
	CallbackIPtr _getCb();
    public:
	void onHeartbeat(Callable callable);
	void onSessionClosed(Callable callable);
	void onShutdown(Callable callable);
    };

    /*
     * Typedef for using Ice's smart pointer reference counting
     * infrastructure.
     *
     *  omero::client_ptr client1 = new omero::client("localhost");
     *  omero::client_ptr client2 = new omero::client("localhost", port);
     */
    typedef IceUtil::Handle<client> client_ptr;

    /*
     * Default implementation of the omero::api::ClientCallback servant.
     * Provides basic operator() implementations for each of the methods
     * that the server may call on it.
     */
    class OMERO_API CallbackI : virtual public omero::api::ClientCallback {

	/*
	 * omero::client needs access to the Callable fields on the callback.
	 */
	friend class client;

	// Preventing copy-construction and assigning by value.
    private:
	CallbackI& operator=(const CallbackI& rv);
	CallbackI(CallbackI&);
	void execute(Callable callable, const std::string& action);
	// State
	Ice::CommunicatorPtr ic;
	Ice::ObjectAdapterPtr oa;
	Callable onHeartbeat;
	Callable onSessionClosed;
	Callable onShutdown;

    public:
	CallbackI(const Ice::CommunicatorPtr& ic, const Ice::ObjectAdapterPtr& oa);
	virtual void requestHeartbeat(const Ice::Current& current = Ice::Current());
	virtual void sessionClosed(const Ice::Current& current = Ice::Current());
	virtual void shutdownIn(Ice::Long milliSeconds, const Ice::Current& current = Ice::Current());
    };

    // Callable implementations
    // ==================================================================

    /*
     * Callable implementation which does nothing.
     */
    class OMERO_API NoOpCallable : public Callable {
    public:
    NoOpCallable() : Callable() {}
	void operator()(){}
    };
}

/*
 * operator<< for printing all IObject classes
 */
std::ostream& operator<<(std::ostream& os, const omero::model::IObjectPtr ptr);

#endif // OMERO_CLIENT_H
