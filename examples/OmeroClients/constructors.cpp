#include <omero/model/ImageI.h>
#include <omero/model/DatasetI.h>
using namespace omero::model;
int main() {
    ImagePtr image = new ImageI();
    DatasetPtr dataset = new DatasetI(1L, false);
    image->linkDataset(dataset);
}
