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
    PixelsTypePtr pt = new PixelsTypeI();
    PixelsDimensionsIPtr pd = new PixelsDimensionsI();
    PhotometricInterpretationIPtr pi = new PhotometricInterpretationI();
    ImageIPtr i = new ImageI();
    AcquisitionModeIPtr mode = new AcquisitionModeI();
    DimensionOrderIPtr d0 = new DimensionOrderI();
    ChannelIPtr c = new ChannelI();
    LogicalChannelIPtr lc = new LogicalChannelI();
    StatsInfoIPtr si = new StatsInfoI();
    PlaneInfoIPtr pl = new PlaneInfoI();

    mode->setValue( new omero::RString("Wide-field") );
    pi->setValue( new omero::RString("RGB") );
    pt->setValue( new omero::RString("int8") );
    d0->setValue( new omero::RString("XYZTC") );

    pd->setSizeX( new omero::CFloat(1.0) );
    pd->setSizeY( new omero::CFloat(1.0) );
    pd->setSizeZ( new omero::CFloat(1.0) );
    lc->setPhotometricInterpretation( pi );

    pix->setSizeX( new omero::CInt(1) );
    pix->setSizeY( new omero::CInt(1) );
    pix->setSizeZ( new omero::CInt(1) );
    pix->setSizeT( new omero::CInt(1) );
    pix->setSizeC( new omero::CInt(1) );
    pix->setSha1 (new omero::RString("09bc7b2dcc9a510f4ab3a40c47f7a4cb77954356") ); // for "pixels"
    pix->setPixelsType( pt );
    pix->setDimensionOrder( d0 );
    pix->setPixelsDimensions( pd );

    pix->addChannel( c );

    // At this point trying to save throws a ValidationException
    try {
        sf->getUpdateService()->saveObject(pix);
        BOOST_ERROR("Should fail");
    } catch (const omero::ValidationException& ve) {
        // ok
    }

    ImagePixelsSeq pixels;
    pixels.push_back(pix);
    i->setPixels( pixels );
    i->setName( new omero::RString("test1") );

    sf->getUpdateService()->saveObject(i);

}
