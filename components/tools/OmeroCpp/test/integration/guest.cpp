/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
#include <omero/fixture.h>

using namespace std;
using namespace omero::api;
using namespace omero::model;
using namespace omero::sys;

TEST(GuestTest, GuestLogin )
{
    try {
        Fixture f;

	const omero::client_ptr client = f.login();
	ServiceFactoryPrx sf = client->getSession();

	sf->getQueryService()->findAll("Experimenter",0);

    } catch (omero::ApiUsageException& aue) {
        cout << aue.message <<endl;
        throw;
    }

}

