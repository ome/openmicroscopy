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
#include <omero/CTypes.h>
#include <omero/Gateway.h>
#include <omero/IScript.h>
#include <omero/ModelI.h>
#include <omero/ObjectFactoryRegistrar.h>
#include <omero/ServerErrors.h>
#include <omero/System.h>
#include <Ice/Ice.h>
#include <IceUtil/Handle.h>
#include <IceUtil/RecMutex.h>
#include <Glacier2/Glacier2.h>
#include <string>
#include <iosfwd>

namespace omero {

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
    class client : IceUtil::Shared {

	// Preventing copy-construction and assigning by value.
    private:
	client& operator=(const client& rv);
	client(client&);
	void exit(); // Localized logic for closing communicator

	// These are the central instances provided by this class.
    protected:

	/*
	 * InitializationData from the last communicator used to create
	 * ic if nulled after closeSession(). A pointer is used since
	 * ID is a simple struct.
	 */
	Ice::InitializationData* previous;

	/*
	 * Single communicator for this omero::client. Nullness is used as
	 * a test of what state the client is in, therefore all access is
	 * synchronized by locking on mutex.
	 */
	Ice::CommunicatorPtr ic;

	/*
	 * Single session for this omero::client. Nullness is used as a test
	 * of what state the client is in, like ic, therefore all access is
	 * synchronized by locking on mutex.
	 */
	omero::api::ServiceFactoryPrx sf;

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
	 * Creates an Ice::Communicator pointing at the given server using
	 * GLACIER2PORT
	 */
	client(const std::string& host);

	/*
	 * Creates an Ice::Communicator pointing at the given server with
	 * the non-standard port.
	 */
	client(const std::string& host, int port = omero::constants::GLACIER2PORT);

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
	Glacier2::RouterPrx const getRouter() const;

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

    };

    /*
     * Simple initial implementation for a client callback. More functionality
     * is planned in upcoming versions.
     */
    class CallbackI : virtual public omero::api::ClientCallback {

	// Preventing copy-construction and assigning by value.
    private:
	CallbackI& operator=(const CallbackI& rv);
	CallbackI(CallbackI&);

    public:
	CallbackI();
	virtual bool ping(const Ice::Current& current = Ice::Current());
	virtual void shutdownIn(Ice::Long milliSeconds, const Ice::Current& current = Ice::Current());
    };

    typedef IceUtil::Handle<CallbackI> CallbackIPtr;

    /*
     * Typedef for using Ice's smart pointer reference counting
     * infrastructure.
     *
     *  omero::client_ptr client1 = new omero::client("localhost");
     *  omero::client_ptr client2 = new omero::client("localhost", port);
     */
    typedef IceUtil::Handle<client> client_ptr;

};

/*
 * operator<< for printing all IObject classes
 */
std::ostream& operator<<(std::ostream& os, const omero::model::IObjectPtr ptr);

#endif // OMERO_CLIENT_H
