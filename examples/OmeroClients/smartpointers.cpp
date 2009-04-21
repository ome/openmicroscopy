#include <omero/model/ImageI.h>
using namespace omero::model;
int main()
{
    // ImageI image();                  // ERROR
    // ImageI image = new ImageI();     // ERROR

    ImageIPtr image1 = new ImageI();     // OK
    ImageIPtr image2(new ImageI());      // OK

    // image1 pointer takes value of image2
    // image1's content is garbage collected
    image1 = image2;

    //
    // Careful with boolean contexts
    //
    if (image1 && image1 == 1) {
        // Means non-null
        // This object can be dereferenced
    }

    ImageIPtr nullImage; // No assignment
    if ( !nullImage && nullImage == 0) {
        // Dereferencing nullImage here would throw an exception:
        // nullImage->getId(); // IceUtil::NullHandleException !
    }

}

