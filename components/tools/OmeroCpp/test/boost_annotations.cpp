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

BOOST_AUTO_TEST_CASE( Annotations )
{
    try {

        Fixture f;
        const omero::client* client = f.login();
        ServiceFactoryPrx sf = (*client).getSession();
        IQueryPrx q = sf->getQueryService();
        IUpdatePrx u = sf->getUpdateService();

        TagAnnotationIPtr tag = new TagAnnotationI();
        tag->setTextValue(new omero::RString("my-first-tag"));

        string uuid = IceUtil::generateUUID();
        ImageIPtr i = new ImageI();
        i->setName(new omero::RString(uuid));
        i->linkAnnotation(tag);
        u->saveObject(i);

        i = ImageIPtr::dynamicCast(
                q->findByQuery(
                    "select i from Image i "
                    "join fetch i.annotationLinks l "
                    "join fetch l.child where i.name = '" + uuid +"'", 0));
        ImageAnnotationLinkIPtr link = ImageAnnotationLinkIPtr::dynamicCast(i->beginAnnotationLinks()[0]);
        AnnotationPtr a = link->getChild();
        tag = TagAnnotationIPtr::dynamicCast(a);
        BOOST_CHECK_EQUAL( "my-first-tag", tag->textValue->val );

    } catch (omero::ApiUsageException& aue) {
        cout << aue.message <<endl;
        throw;
    }
}

