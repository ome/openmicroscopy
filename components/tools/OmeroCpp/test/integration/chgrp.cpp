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


TEST(ChgrpTest, testSimpleChgrp ) {
    Fixture f;
    ExperimenterGroupPtr g1 = f.newGroup();
    ExperimenterGroupPtr g2 = f.newGroup();
    ExperimenterPtr user = f.newUser(g1);
    f.addExperimenter(g2, user);

    f.login(user->getOmeName()->getValue(), user->getOmeName()->getValue());
    ServiceFactoryPrx sf = f.client->getSession();
    IAdminPrx admin = sf->getAdminService();
    ASSERT_EQ(g1->getId()->getValue(), admin->getEventContext()->groupId);

    IQueryPrx iquery = sf->getQueryService();
    IUpdatePrx iupdate = sf->getUpdateService();

    ImagePtr image = new ImageI();
    image->setName( rstring("testSimpleChgrp") );
    image = ImagePtr::dynamicCast( iupdate->saveAndReturnObject( image ) );

    LongList imageIds;
    StringLongListMap objects;
    ChildOptions options;
    Chgrp2Ptr chgrpCmd = new Chgrp2();
    imageIds.push_back( image->getId()->getValue() );
    objects["Image"] = imageIds;
    chgrpCmd->targetObjects = objects;
    chgrpCmd->groupId = g2->getId()->getValue();
    chgrpCmd->childOptions = options;

    HandlePrx handle = sf->submit( chgrpCmd );
    CmdCallbackIPtr cb = new CmdCallbackI(f.client, handle);
    ResponsePtr rsp = cb->loop(10, 500);
    ERRPtr err = ERRPtr::dynamicCast(rsp);
    if (err) {
        FAIL() << "ERR returned";
    }

}
