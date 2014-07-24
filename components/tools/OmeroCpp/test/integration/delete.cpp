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
#include <string>
#include <map>

using namespace std;
using namespace omero;
using namespace omero::api;
using namespace omero::api::_cpp_delete;
using namespace omero::callbacks;
using namespace omero::model;
using namespace omero::rtypes;
using namespace omero::sys;
using namespace omero::cmd;



TEST(DeleteTest, testSimpleDelete ) {
    Fixture f;
    f.login();
    ServiceFactoryPrx sf = f.client->getSession();

    IQueryPrx iquery = sf->getQueryService();
    IUpdatePrx iupdate = sf->getUpdateService();
    IDeletePrx idelete = sf->getDeleteService();

    ImagePtr image = new ImageI();
    image->setName( rstring("testSimpleDelete") );
    image = ImagePtr::dynamicCast( iupdate->saveAndReturnObject( image ) );

    DeletePtr deleteCmd = new Delete("/Image", image->getId()->getValue(), StringMap());
    
    // Submit and wait for completion
    HandlePrx handle = sf->submit(deleteCmd);
    CmdCallbackIPtr cb = new CmdCallbackI(f.client, handle);
    ResponsePtr resp = cb->loop(10, 500);
    
    ERRPtr err = ERRPtr::dynamicCast(resp);
    if (err) {
        FAIL() << "Failed to delete image: " << err->category << ", " << err->name << endl;
    }
}
