/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
#include <omero/fixture.h>
#include <omero/util/tiles.h>

using namespace std;
using namespace omero::api;
using namespace omero::model;
using namespace omero::rtypes;
using namespace omero::sys;
using namespace omero::util;


class MyIteration : virtual public TileLoopIteration {
private:
    // Preventing copy-construction and assigning by value.
    MyIteration& operator=(const MyIteration& rv);
    MyIteration(MyIteration&);
public:
    MyIteration() : TileLoopIteration(){}
    ~MyIteration(){}
    void run(const TileDataPtr& data, int z, int c, int t, int x, int y, int tileWidth, int tileHeight, int /*tileCount*/) const {
        Ice::ByteSeq buf(tileWidth*tileHeight*8);
        data->setTile(buf, z, c, t, x, y, tileWidth, tileHeight);
    }
};


class RndFixture {
    Fixture f;
    ServiceFactoryPrx sf;
    string _name;
public:
    RndFixture() {
    }
    RndFixture(string name) {
        _name = name;
    }
    void init() {
        if (!sf) {
            omero::client_ptr client;
            if (_name.empty()) {
                f.login();
                client = f.client;
            } else if (_name == "root") {
                client = f.root;
            } else {
                f.login(_name);
                client = f.client;
            }
            sf = client->getSession();
        }
    }

    IUpdatePrx update() {
        init();
        return sf->getUpdateService();
    }
    IRenderingSettingsPrx rndService() {
        init();
        return sf->getRenderingSettingsService();
    }

    /**
     * Create a single image with binary.
     *
     * After recent changes on the server to check for existing
     * binary data for pixels, many resetDefaults methods tested
     * below began returning null since {@link omero.LockTimeout}
     * exceptions were being thrown server-side. By using
     * omero.client.forEachTile, we can set the necessary data easily.
     *
     * @see ticket:5755
     */
    ImagePtr createBinaryImage() {
        ImagePtr image = new ImageI();
        image->setName(rstring("createBinaryImage"));
        image->addPixels(f.pixels());
        image = ImagePtr::dynamicCast(update()->saveAndReturnObject(image));
        return createBinaryImage(image);
    }


    /**
     * Create the binary data for the given image.
     */
    ImagePtr createBinaryImage(ImagePtr _image) {

        PixelsPtr pixels = _image->getPixels(0);
        RPSTileLoopPtr loop = new RPSTileLoop(f.client->getSession(), pixels);
        loop->forEachTile(256, 256, new MyIteration());
        // This block will change the updateEvent on the pixels
        // therefore we're going to reload the pixels.

        _image->setPixels(0, loop->getPixels());
        return _image;

    }

};

TEST(RenderingSettingsTest, testResetDefaultsInImage )
{

    RndFixture f;
    ImagePtr img = f.createBinaryImage();
    ASSERT_TRUE( img->getId() );
    f.rndService()->resetDefaultsInImage(img->getId()->getValue());
}
