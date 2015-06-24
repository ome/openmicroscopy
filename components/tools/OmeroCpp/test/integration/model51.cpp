/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

#include <omero/fixture.h>
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

TEST(NewModelTest, UnitsTime)
{
    Fixture f;

    f.login();
    ServiceFactoryPrx sf = f.client->getSession();

    PixelsIPtr pix = f.pixels();

    // At this point trying to save throws a ValidationException
    try {
        sf->getUpdateService()->saveObject(pix);
        FAIL() << "Should fail";
    } catch (const omero::ValidationException& ve) {
        // ok
    }

    ImagePtr i = new_ImageI();
    i->addPixels( pix );
    i->setName( rstring("test1") );

    try {
        sf->getUpdateService()->saveObject(i);
    } catch (const omero::ValidationException& ve) {
        // ok
        FAIL() << ve.serverStackTrace;
    }

}
