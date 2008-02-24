/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
#include <boost_fixture.h>

using namespace omero::api;
using namespace omero::model;

BOOST_AUTO_TEST_CASE( SavingPixels )
{
    Fixture f;

    const omero::client* client = f.login();
    ServiceFactoryPrx sf = (*client).getSession();

    PixelsIPtr pix = new PixelsI();
    PixelsTypeIPtr pt = new PixelsTypeI();
    PixelsDimensionsIPtr pd = new PixelsDimensionsI();
    PhotometricInterpretationIPtr pi = new PhotometricInterpretationI();
    ImageIPtr i = new ImageI();
    AcquisitionModeIPtr mode = new AcquisitionModeI();
    DimensionOrderIPtr d0 = new DimensionOrderI();
    ChannelIPtr c = new ChannelI();
    LogicalChannelIPtr lc = new LogicalChannelI();
    StatsInfoIPtr si = new StatsInfoI();
    PlaneInfoIPtr pl = new PlaneInfoI();

    mode->value = new omero::RString("Wide-field");
    pi->value = new omero::RString("RGB");
    pt->value = new omero::RString("int8");
    d0->value = new omero::RString("XYZTC");

    pd->sizeX = new omero::CFloat(1.0);
    pd->sizeY = new omero::CFloat(1.0);
    pd->sizeZ = new omero::CFloat(1.0);
    lc->photometricInterpretation = pi;

    pix->sizeX = new omero::CInt(1);
    pix->sizeY = new omero::CInt(1);
    pix->sizeZ = new omero::CInt(1);
    pix->sizeT = new omero::CInt(1);
    pix->sizeC = new omero::CInt(1);
    pix->sha1 = new omero::RString("09bc7b2dcc9a510f4ab3a40c47f7a4cb77954356"); // for "pixels"
    pix->pixelsType = pt;
    pix->dimensionOrder = d0;
    pix->pixelsDimensions = pd;

    PixelsChannelsSeq channels;
    channels.push_back(c);
    pix->channels = channels;

    // At this point trying to save throws a ValidationException
    try {
        sf->getUpdateService()->saveObject(pix);
        BOOST_ERROR("Should fail");
    } catch (const omero::ValidationException& ve) {
        // ok
    }

    ImagePixelsSeq pixels;
    pixels.push_back(pix);
    i->pixels = pixels;
    i->name = new omero::RString("test1");

    sf->getUpdateService()->saveObject(i);

}

