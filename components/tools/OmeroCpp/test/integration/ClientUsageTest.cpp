/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

#include <Ice/Initialize.h>
#include <omero/fixture.h>
#include <omero/client.h>
#include <algorithm>

using namespace omero::rtypes;

TEST(ClientUsageTest, testClientClosedAutomatically)
{
    omero::client_ptr client = new omero::client();
    client->createSession();
    client->getSession()->closeOnDestroy();
}

TEST(ClientUsageTest, testClientClosedManually )
{
    omero::client_ptr client = new omero::client();
    client->createSession();
    client->getSession()->closeOnDestroy();
    client->closeSession();
}

TEST(ClientUsageTest, testUseSharedMemory )
{
    omero::client_ptr client = new omero::client();
    client->createSession();

    ASSERT_EQ(0U, client->getInputKeys().size());
    client->setInput("a", rstring("b"));
    ASSERT_EQ(1U, client->getInputKeys().size());
    std::vector<std::string> keys = client->getInputKeys();
    std::vector<std::string>::iterator it = find(keys.begin(), keys.end(), "a");
    ASSERT_TRUE( it != keys.end() );
    ASSERT_EQ("b", omero::RStringPtr::dynamicCast(client->getInput("a"))->getValue());

    client->closeSession();
}

TEST(ClientUsageTest, testCreateInsecureClientTicket2099 )
{
    omero::client_ptr secure = new omero::client();
    ASSERT_TRUE(secure->isSecure());
    secure->createSession()->getAdminService()->getEventContext();
    omero::client_ptr insecure = secure->createClient(false);
    insecure->getSession()->getAdminService()->getEventContext();
    ASSERT_FALSE( insecure->isSecure());
}

TEST(ClientUsageTest, testGetStatefulServices )
{
    Fixture f;
    omero::api::ServiceFactoryPrx sf = f.root->getSession();
    sf->setSecurityContext(new omero::model::ExperimenterGroupI(0L, false));
    sf->createRenderingEngine();
    std::vector<omero::api::StatefulServiceInterfacePrx> srvs = f.root->getStatefulServices();
    ASSERT_EQ(1U, srvs.size());
    try {
        sf->setSecurityContext(new omero::model::ExperimenterGroupI(1L, false));
        FAIL() << "Should not be allowed";
    } catch (const omero::SecurityViolation& sv) {
        // good
    }
    srvs.at(0)->close();
    srvs = f.root->getStatefulServices();
    ASSERT_EQ(0U, srvs.size());
    sf->setSecurityContext(new omero::model::ExperimenterGroupI(1L, false));
}

TEST(ClientUsageTest, testKillSession)
{
    Fixture f;
    f.login();
    int count = f.client->killSession();
    ASSERT_EQ(count, 1);
}
