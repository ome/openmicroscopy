#include <iostream>
#include <omero/api/IAdmin.h>
#include <omero/api/ISession.h>
#include <omero/client.h>
#include <omero/model/Session.h>

int main(int argc, char* argv[]) {

    Ice::StringSeq args1 = Ice::argsToStringSeq(argc, argv);
    Ice::StringSeq args2(args1); // Copies

    // ticket:1246
    Ice::InitializationData id1;
    id1.properties = Ice::createProperties(args1);

    Ice::InitializationData id2;
    id2.properties = Ice::createProperties(args2);

    omero::client_ptr client = new omero::client(id1);
    omero::client_ptr sudoClient = new omero::client(id2);

    omero::api::ServiceFactoryPrx sf = client->createSession();
    omero::api::ISessionPrx sessionSvc = sf->getSessionService();

    omero::sys::PrincipalPtr p = new omero::sys::Principal();
    p->name = "root"; // Can change to any user
    p->group = "user";
    p->eventType = "User";

    omero::model::SessionPtr sudoSession = sessionSvc->createSessionWithTimeout( p, 3*60*1000L ); // 3 minutes to live

    omero::api::ServiceFactoryPrx sudoSf = sudoClient->joinSession( sudoSession->getUuid()->getValue() );
    omero::api::IAdminPrx sudoAdminSvc = sudoSf->getAdminService();
    std::cout << sudoAdminSvc->getEventContext()->userName;

}
