/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
#include <boost_fixture.h>

using namespace std;
using namespace omero::api;
using namespace omero::model;
using namespace omero::sys;

BOOST_AUTO_TEST_CASE( GuestLogin )
{
    try {
        Fixture f;

	const omero::client* client = f.login();
	ServiceFactoryPrx sf = (*client).getSession();

	sf->getQueryService()->findAll("Experimenter",0);

    } catch (omero::ApiUsageException& aue) {
        cout << aue.message <<endl;
        throw;
    }

}

