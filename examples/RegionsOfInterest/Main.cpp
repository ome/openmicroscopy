#include <omero/RTypesI.h>
#include <omero/model/RoiI.h>
#include <omero/model/EllipseI.h>

using namespace omero::model;
using namespace omero::rtypes;

int main() {
        RoiPtr roi = new RoiI();
        EllipsePtr ellipse = new EllipseI();
        ellipse->setX(rdouble(1));
}
