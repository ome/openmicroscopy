/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
#include <boost_fixture.h>
#include <omero/model/AcquisitionModeI.h>
#include <omero/model/ChannelI.h>
#include <omero/model/DimensionOrderI.h>
#include <omero/model/ImageI.h>
#include <omero/model/LogicalChannelI.h>
#include <omero/model/PhotometricInterpretationI.h>
#include <omero/model/PixelsI.h>
#include <omero/model/PixelsTypeI.h>
#include <omero/model/PlaneInfoI.h>
#include <omero/model/StatsInfoI.h>

using namespace omero::api;
using namespace omero::model;
using namespace omero::rtypes;

BOOST_AUTO_TEST_CASE( SavingPixels )
{
    Fixture f;

    const omero::client_ptr client = f.login();
    ServiceFactoryPrx sf = client->getSession();

    PixelsIPtr pix = new PixelsI();
    PixelsTypePtr pt = new PixelsTypeI();
    PhotometricInterpretationIPtr pi = new PhotometricInterpretationI();
    ImageIPtr i = new ImageI();
    AcquisitionModeIPtr mode = new AcquisitionModeI();
    DimensionOrderIPtr d0 = new DimensionOrderI();
    ChannelIPtr c = new ChannelI();
    LogicalChannelIPtr lc = new LogicalChannelI();
    StatsInfoIPtr si = new StatsInfoI();
    PlaneInfoIPtr pl = new PlaneInfoI();

    mode->setValue( rstring("Wide-field") );
    pi->setValue( rstring("RGB") );
    pt->setValue( rstring("int8") );
    d0->setValue( rstring("XYZTC") );

    lc->setPhotometricInterpretation( pi );

    pix->setSizeX( rint(1) );
    pix->setSizeY( rint(1) );
    pix->setSizeZ( rint(1) );
    pix->setSizeT( rint(1) );
    pix->setSizeC( rint(1) );
    pix->setSha1 (rstring("09bc7b2dcc9a510f4ab3a40c47f7a4cb77954356") ); // for "pixels"
    pix->setPixelsType( pt );
    pix->setDimensionOrder( d0 );
    pix->setPhysicalSizeX( rfloat(1.0) );
    pix->setPhysicalSizeY( rfloat(1.0) );
    pix->setPhysicalSizeZ( rfloat(1.0) );

    pix->addChannel( c );

    // At this point trying to save throws a ValidationException
    try {
        sf->getUpdateService()->saveObject(pix);
        BOOST_ERROR("Should fail");
    } catch (const omero::ValidationException& ve) {
        // ok
    }

    i->addPixels( pix );
    i->setName( rstring("test1") );

    sf->getUpdateService()->saveObject(i);

}
