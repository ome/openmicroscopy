/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
#include <boost_fixture.h>
#include <omero/callbacks.h>
#include <omero/all.h>
#include <omero/cmd/Graphs.h>
#include <string>
#include <map>

using namespace std;
using namespace omero;
using namespace omero::api;
using namespace omero::cmd;
using namespace omero::callbacks;
using namespace omero::model;
using namespace omero::rtypes;
using namespace omero::sys;


BOOST_AUTO_TEST_CASE( testSimpleChgrp ) {
    Fixture f;
    client_ptr c = f.login();
    ServiceFactoryPrx sf = c->getSession();

    IQueryPrx iquery = sf->getQueryService();
    IUpdatePrx iupdate = sf->getUpdateService();

    ImagePtr image = new ImageI();
    image->setName( rstring("testSimpleDelete") );
    image->setAcquisitionDate( rtime(0) );
    image = ImagePtr::dynamicCast( iupdate->saveAndReturnObject( image ) );

    std::map<string, string> options;
    ChgrpPtr chgrp;
    chgrp->type = "/Image";
    chgrp->id = image->getId()->getValue();
    chgrp->grp = -1L;
    chgrp->options = options;

    HandlePrx handle = sf->submit( chgrp );
    CmdCallbackIPtr cb = new CmdCallbackI(c->getObjectAdapter(), handle);
    ResponsePtr rsp = cb->loop(10, 500);
    ERRPtr err = ERRPtr::dynamicCast(rsp);
    if (err) {
        BOOST_ERROR("ERR returned");
    }

}
