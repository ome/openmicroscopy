#include <omero/RTypesI.h>
using namespace omero::rtypes;
int main() {
    omero::RStringPtr s = rstring("value");
    omero::RBoolPtr b = rbool(true);
    omero::RLongPtr l = rlong(1);
    omero::RIntPtr i = rint(1);
}
