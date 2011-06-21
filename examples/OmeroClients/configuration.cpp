#include <omero/client.h>
#include <iostream>

int main(int argc, char* argv[]) {

    // All configuration in file pointed to by
    // --Ice.Config=file.config
    // No username, password entered
    try {
        omero::client client1(argc, argv);
        client1.createSession();
        client1.closeSession();
    } catch (const Glacier2::PermissionDeniedException& pd) {
        // Bad password?
    } catch (const Ice::ConnectionRefusedException& cre) {
        // Bad address or port?
    }

    // Most basic configuration.
    // Uses default port 4064
    // createSession needs username and password
    try {
        omero::client client2("localhost");
        client2.createSession("root", "ome");
        client2.closeSession();
    } catch (const Glacier2::PermissionDeniedException& pd) {
        // Bad password?
    } catch (const Ice::ConnectionRefusedException& cre) {
        // Bad address or port?
    }

    // Configuration with port information
    try {
        omero::client client3("localhost", 24063);
        client3.createSession("root", "ome");
        client3.closeSession();
    } catch (const Glacier2::PermissionDeniedException& pd) {
        // Bad password?
    } catch (const Ice::ConnectionRefusedException& cre) {
        // Bad address or port?
    }

    // Advanced configuration in C++ takes place
    // via an InitializationData instance.
    try {
        Ice::InitializationData data;
        data.properties = Ice::createProperties();
        data.properties->setProperty("omero.host", "localhost");
        omero::client client4(data);
        client4.createSession("root", "ome");
        client4.closeSession();
    } catch (const Glacier2::PermissionDeniedException& pd) {
        // Bad password?
    } catch (const Ice::ConnectionRefusedException& cre) {
        // Bad address or port?
    }

    // std::map to be added (ticket:1278)
    try {
        Ice::InitializationData data;
        data.properties = Ice::createProperties();
        data.properties->setProperty("omero.host", "localhost");
        data.properties->setProperty("omero.user", "root");
        data.properties->setProperty("omero.pass", "ome");
        omero::client client5(data);
        // Again, no username or password needed
        // since present in the data. But they *can*
        // be overridden.
        client5.createSession();
        client5.closeSession();
    } catch (const Glacier2::PermissionDeniedException& pd) {
        // Bad password?
    } catch (const Ice::ConnectionRefusedException& cre) {
        // Bad address or port?
    }
}
