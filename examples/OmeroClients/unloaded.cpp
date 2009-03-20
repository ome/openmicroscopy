#include <omero/model/ImageI.h>
#include <omero/model/DatasetI.h>
#include <omero/ClientErrors.h>
using namespace omero::model;
int main() {
    ImagePtr image = new ImageI();         // A loaded object by default
    assert(image->isLoaded());
    image->unload();                       // can then be unloaded
    assert(! image->isLoaded());

    image = new ImageI( 1L, false );       // Creates an unloaded "proxy"
    assert(! image->isLoaded());

    image->getId();                        // Ok
    try {
        image->getName();                  // No data access is allowed other than id.
    } catch (const omero::ClientError& ce) {
        // Ok.
    }
}
