/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
#include <IceUtil/UUID.h>
#include <Ice/Initialize.h>
#include <omero/API.h>
#include <omero/client.h>
#include <omero/model/ExperimenterI.h>
#include <boost_fixture.h>

using namespace std;
using namespace omero::api;
using namespace omero::model;
using namespace omero::sys;

BOOST_AUTO_TEST_CASE( SimpleSearch )
{
    try {
        Fixture f;

        int argc = 0;
        char** argv = new char*[0];
        omero::client root(argc, argv);
        ServiceFactoryPrx sf = root.createSession();
        sf = root.getSession();
        SearchPrx search = sf->createSearchService();
        search->onlyType("Experimenter");
        search->byFullText("root");
        if (search->hasNext()) {
            ExperimenterIPtr e = ExperimenterIPtr::dynamicCast(search->next());
        }

        string uuid(IceUtil::generateUUID());
        ImageIPtr i = new ImageI();
        i->setName(new omero::RString(uuid));
        sf->getUpdateService()->saveObject(i);

        IObjectList list;
        list = sf->getQueryService()->findAllByFullText("Image",uuid,0);
        cout << list.size() << endl;

    } catch (omero::ApiUsageException& aue) {
        cout << aue.message <<endl;
        throw;
    }

}

