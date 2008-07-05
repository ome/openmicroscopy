/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_CLIENT_H
#define OMERO_CLIENT_H

#include <omero/API.h>
#include <omero/ClientErrors.h>
#include <omero/Constants.h>
#include <omero/CTypes.h>
#include <omero/ModelI.h>
#include <omero/ObjectFactoryRegistrar.h>
#include <omero/ServerErrors.h>
#include <omero/System.h>
#include <Ice/Ice.h>
#include <Glacier2/Glacier2.h>
#include <string>
#include <iosfwd>

namespace omero {

  /*
   * The OMERO::client class is the main entry point for consumers of
   * the OMERO.blitz server in C++. An instance provides access to an
   * Ice::Communicator which is the central Ice interface for all
   * communication. An instance also provides access to a single
   * omero::api::ServiceFactoryPrx which is the blitz session facade,
   * from which all other proxies can be obtained. Once the
   * ServiceFactoryPrx is destroyed or the session times out,
   * all proxies obtained from this instance are also destroyed.
   *
   * Methods which take an Ice::Context should only be used when
   * so instructed. The Ice 3.2+ rules for contexts say that all
   * other contexts (the communicator ImplicitContext and per-proxy
   * contexts) will not be sent if an explicit context parameter is used.
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
    client(int& argc, char* argv[],
      const Ice::InitializationData& id = Ice::InitializationData());

    /*
     * Default constructor which can only parse the ICE_CONFIG environment
     * variable in a manner similar to that of the --Ice.Config file.
     */
     client(const Ice::InitializationData& id = Ice::InitializationData());

    /*
     * Destroys the communicator instance. To have the session destroyed,
     * call getSession().closeOnDestroy() on the client before destruction.
     * Otherwise, the session will be destroyed by the server on timeout.
     */
    ~client();

    // Accessors:

    Ice::CommunicatorPtr getCommunicator() const { return ic; }
    omero::api::ServiceFactoryPrx getSession() const { return sf; }
    Ice::ImplicitContextPtr getImplicitContext() const { return ic->getImplicitContext(); }
    Ice::PropertiesPtr getProperties() const { return ic->getProperties(); }
    std::string getProperty(const std::string& key) const { return getProperties()->getProperty(key); }

    // Session management

    /*
     * Creates a session. Calling this method while a session is
     * active will throw an exception. It should only be used again,
     * after a session timeout exception, or similar.
     */
    omero::api::ServiceFactoryPrx createSession(const std::string& username = std::string(), const std::string& password = std::string());

    /*
     * Closes the Router connection created by createSession(). Due to a bug in Ice,
     * only one connection is allowed per communicator, so we also destroy the
     * communicator.
     */
    void closeSession();

    /*
     * Environment methods. Allows to store and retrieve
     */
    omero::RTypePtr getInput(const std::string& key);
    omero::RTypePtr getOutput(const std::string& key);
    void setInput(const std::string& key, omero::RTypePtr value);
    void setOutput(const std::string& key, omero::RTypePtr value);
    std::vector<std::string> getInputKeys();
    std::vector<std::string> getOutputKeys();
  protected:
    const std::string sess();
    omero::api::ISessionPrx env();

  };

}

std::ostream& operator<<(std::ostream& os, const omero::model::IObjectPtr ptr);

#endif // OMERO_CLIENT_H
