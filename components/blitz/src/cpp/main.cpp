#include <OMERO/common.h>
#include <OMERO/client.h>
#include <Ice/Ice.h>
#include <Glacier2/Glacier2.h>
#include <Glacier2/Router.h>
#include <Glacier2/Session.h>
#include <iostream>

using namespace std;

int
main(int argc, char* argv[])
{
    
  int status = 0;
  try {



    OMERO::client omero(argc, argv);
    omero.createSession();
    Ice::Context ctx = omero.getDefaultContext();

    omero::api::IUpdatePrx u = omero.getUpdateService(ctx);
    omero::model::ImageIPtr img = new omero::model::ImageI();
    img->name = new omero::RString(false,"foo");
    u->saveObject(img);
    cout << "Saved image foo" << endl;

    omero::api::IQueryPrx q = omero.getQueryService(ctx);
    //    omero::model::ImageIPtr test = q->find...
    omero::model::IObjectPtr root = q->get("Experimenter", 0);
    cout << "Got root user" << endl;

    omero::api::IAdminPrx admin = omero.getAdminService(ctx);
    omero::sys::EventContextPtr ec = admin->getEventContext();
    cout << "Got event context: group name = " << ec->groupName << endl;

  } catch (const Ice::Exception& ex) {
    cerr << "Exception:" << endl << ex << endl;
    status = 1;
  } catch (const char* msg) {
    cerr << "Msg:" << endl << msg << endl;
    status = 1;
  }

  return status;
  
}
