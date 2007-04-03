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
    omero::api::IQueryPrx q = omero.getQueryService(ctx);

    omero::model::IObjectPtr root;
    root = q->get("Experimenter", 0);
    cout << root << endl;
    
  } catch (const Ice::Exception& ex) {
    cerr << "Exception:" << endl << ex << endl;
    status = 1;
  } catch (const char* msg) {
    cerr << "Msg:" << endl << msg << endl;
    status = 1;
  }

  return status;
  
}
