#include <omero/RTypesI.h>
using namespace omero::rtypes;
int main() {
    // Sets and Lists may be interpreted differently on the server
    omero::RListPtr l = rlist(); // rstring("a"), rstring("b"));
    omero::RSetPtr s = rset();   // rint(1), rint(2));
                                 // No-varargs (#1242)
}
