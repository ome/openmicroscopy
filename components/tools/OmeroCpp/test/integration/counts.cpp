/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
#include <omero/fixture.h>
#include <omero/model/ImageI.h>
#include <omero/model/TagAnnotationI.h>

using namespace std;
using namespace omero::api;
using namespace omero::model;
using namespace omero::rtypes;
using namespace omero::sys;

TEST(CountsTest, Counts )
{
    try {
        Fixture f;

	const omero::client_ptr client = f.login();
	ServiceFactoryPrx sf = client->getSession();
	IAdminPrx admin = sf->getAdminService();
	IQueryPrx query = sf->getQueryService();
	IUpdatePrx update = sf->getUpdateService();

	long usr = admin->getEventContext()->userId;

	ImagePtr img = new_ImageI();
        img->setName( rstring("name") );
	TagAnnotationIPtr tag = new TagAnnotationI();
	img->linkAnnotation( tag );
	img = ImageIPtr::dynamicCast( update->saveAndReturnObject( img ) );

        stringstream q;
        q << "select img from Image img ";
        q << "join fetch img.annotationLinksCountPerOwner ";
        q << "where img.id = ";
        q << img->getId()->getValue();
	img = ImageIPtr::dynamicCast( query->findByQuery(q.str(), 0) );

	EXPECT_TRUE( img->getAnnotationLinksCountPerOwner()[usr] > 0 );

    } catch (const omero::ApiUsageException& aue) {
        cout << aue.message <<endl;
	FAIL() << "api usage exception thrown";
    } catch (const Ice::UnknownException& ue) {
        cout << ue << endl;
	FAIL() << "unknown exception thrown";
    }
}
