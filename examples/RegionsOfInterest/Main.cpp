#include <omero/RTypesI.h>
#include <omero/model/ROII.h>
#include <omero/model/EllipseI.h>

using namespace omero::model;
using namespace omero::rtypes;

int main() {
        ROIPtr roi = new ROII();
        EllipsePtr ellipse = new EllipseI();
        ellipse->setCx(rdouble(1));
}
