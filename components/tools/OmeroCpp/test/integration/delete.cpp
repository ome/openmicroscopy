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


BOOST_AUTO_TEST_CASE( testSimpleDelete ) {
    Fixture f;
    client_ptr c = f.login();
    ServiceFactoryPrx sf = c->getSession();

    IQueryPrx iquery = sf->getQueryService();
    IUpdatePrx iupdate = sf->getUpdateService();
    IDeletePrx idelete = sf->getDeleteService();

    ImagePtr image = new ImageI();
    image->setName( rstring("testSimpleDelete") );
    image->setAcquisitionDate( rtime(0) );
    image = ImagePtr::dynamicCast( iupdate->saveAndReturnObject( image ) );

    std::map<string, string> options;
    DeleteCommands dcs;
    DeleteCommand dc;
    dc.type = "/Image";
    dc.id = image->getId()->getValue();
    dc.options = options;
    dcs.push_back(dc);

    DeleteHandlePrx handle = idelete->queueDelete( dcs );
    DeleteCallbackIPtr cb = new DeleteCallbackI(c->getObjectAdapter(), handle);
    cb->loop(10, 500);

}
