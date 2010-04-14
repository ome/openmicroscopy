/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

#include <Ice/Initialize.h>
#include <omero/client.h>
#include <boost_fixture.h>
#include <algorithm>

using namespace omero::rtypes;

BOOST_AUTO_TEST_CASE( testClientClosedAutomatically)
{
    omero::client_ptr client = new omero::client();
    client->createSession();
    client->getSession()->closeOnDestroy();
}

BOOST_AUTO_TEST_CASE( testClientClosedManually )
{
    omero::client_ptr client = new omero::client();
    client->createSession();
    client->getSession()->closeOnDestroy();
    client->closeSession();
}

BOOST_AUTO_TEST_CASE( testUseSharedMemory )
{
    omero::client_ptr client = new omero::client();
    client->createSession();

    BOOST_CHECK_EQUAL(0, (int)client->getInputKeys().size());
    client->setInput("a", rstring("b"));
    BOOST_CHECK_EQUAL(1, (int)client->getInputKeys().size());
    std::vector<std::string> keys = client->getInputKeys();
    std::vector<std::string>::iterator it = find(keys.begin(), keys.end(), "a");
    BOOST_CHECK( it != keys.end() );
    BOOST_CHECK_EQUAL("b", omero::RStringPtr::dynamicCast(client->getInput("a"))->getValue());

    client->closeSession();
}

BOOST_AUTO_TEST_CASE( testCreateInsecureClientTicket2099 )
{
    omero::client_ptr secure = new omero::client();
    BOOST_CHECK(secure->isSecure());
    secure->createSession()->getAdminService()->getEventContext();
    omero::client_ptr insecure = secure->createClient(false);
    insecure->getSession()->getAdminService()->getEventContext();
    BOOST_CHECK( ! insecure->isSecure());
}
