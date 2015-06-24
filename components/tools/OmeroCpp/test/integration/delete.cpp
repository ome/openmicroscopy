/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
#include <omero/fixture.h>
#include <omero/callbacks.h>
#include <omero/all.h>
#include <omero/cmd/Graphs.h>
#include <string>
#include <map>

using namespace std;
using namespace omero;
using namespace omero::api;
using namespace omero::cmd;
using namespace omero::cmd::graphs;
using namespace omero::callbacks;
using namespace omero::model;
using namespace omero::rtypes;
using namespace omero::sys;


TEST(DeleteTest, testSimpleDelete ) {
    Fixture f;
    f.login();
    ServiceFactoryPrx sf = f.client->getSession();

    IQueryPrx iquery = sf->getQueryService();
    IUpdatePrx iupdate = sf->getUpdateService();

    ImagePtr image = new ImageI();
    image->setName( rstring("testSimpleDelete") );
    image = ImagePtr::dynamicCast( iupdate->saveAndReturnObject( image ) );

    omero::api::LongList imageIds;
    omero::api::StringLongListMap objects;
    ChildOptions options;
    Delete2Ptr deleteCmd = new Delete2();
    imageIds.push_back( image->getId()->getValue() );
    objects["Image"] = imageIds;
    deleteCmd->targetObjects = objects;
    deleteCmd->childOptions = options;

    // Submit and wait for completion
    HandlePrx handle = sf->submit( deleteCmd );
    CmdCallbackIPtr cb = new CmdCallbackI(f.client, handle);
    ResponsePtr resp = cb->loop(10, 500);

    ERRPtr err = ERRPtr::dynamicCast(resp);
    if (err) {
        FAIL() << "Failed to delete image: " << err->category << ", " << err->name << endl;
    }
}
