#include <omero/RTypesI.h>
using namespace omero::rtypes;
int main() {
    // Sets and Lists may be interpreted differently on the server
    omero::RListPtr s = rlist(rstring("a"), rstring("b"));
    omero::RSetPtr s = rlist(rint(1), rint(2));
}
