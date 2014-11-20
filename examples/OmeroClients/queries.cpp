#include <omero/IceNoWarnPush.h>
#include <omero/api/IQuery.h>
#include <omero/IceNoWarnPop.h>
#include <omero/client.h>
#include <omero/RTypesI.h>
#include <omero/sys/ParametersI.h>

using namespace omero::rtypes;

int main(int argc, char* argv[]) {

    omero::client_ptr client = new omero::client(argc, argv);
    omero::api::ServiceFactoryPrx sf = client->createSession();
    omero::api::IQueryPrx q = sf->getQueryService();

    std::string query_string = "select i from Image i where i.id = :id and name like :namedParameter";

    omero::sys::ParametersIPtr p = new omero::sys::ParametersI();
    p->add("id", rlong(1L));
    p->add("namedParameter", rstring("cell%mit%"));

    omero::api::IObjectList results = q->findAllByQuery(query_string, p);

}
