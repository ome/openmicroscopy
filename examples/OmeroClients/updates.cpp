#include <omero/IceNoWarnPush.h>
#include <IceUtil/Time.h>
#include <omero/api/IUpdate.h>
#include <omero/IceNoWarnPop.h>

#include <omero/client.h>
#include <omero/RTypesI.h>
#include <omero/model/ImageI.h>

using namespace omero::rtypes;

int main(int argc, char* argv[]) {

    omero::client_ptr client = new omero::client(argc, argv);

    omero::model::ImagePtr i = new omero::model::ImageI();
    i->setName( rstring("name") );

    omero::api::ServiceFactoryPrx sf = client->createSession();
    omero::api::IUpdatePrx u = sf->getUpdateService();

    i = omero::model::ImagePtr::dynamicCast( u->saveAndReturnObject( i ) );
}
