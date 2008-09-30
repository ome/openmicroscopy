#include <omero/RTypes.h>

int main() {
    omero::RStringPtr nulled = (omero::RStringPtr)0;
    omero::RStringPtr empty = new omero::RString(); // Ice will send as ""
    omero::RStringPtr initialized = new omero::RString();
}
