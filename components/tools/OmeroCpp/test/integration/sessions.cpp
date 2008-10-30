/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
#include <Ice/Initialize.h>
#include <omero/client.h>
#include <omero/model/ExperimenterI.h>
#include <omero/model/SessionI.h>
#include <boost_fixture.h>

using namespace omero::rtypes;

BOOST_AUTO_TEST_CASE( RootCanCreateSessionForUser )
{
  Fixture f;

  const omero::client_ptr root = f.root_login();
  omero::api::ServiceFactoryPrx sf = root->getSession();
  omero::api::ISessionPrx sess = sf->getSessionService();

  omero::model::ExperimenterIPtr e = new omero::model::ExperimenterI();
  e->setOmeName(rstring(f.uuid()));
  e->setFirstName(rstring("session"));
  e->setLastName(rstring("test"));
  sf->getAdminService()->createUser(e, "default");

  omero::sys::PrincipalPtr p = new omero::sys::Principal();
  p->name = e->getOmeName()->getValue();
  p->group = "default";
  p->eventType = "Test";
  omero::model::SessionPtr session = sess->createSessionWithTimeout(p, 10000L);

  omero::client user;
  user.createSession(e->getOmeName()->getValue(),session->getUuid()->getValue());
  omero::api::ServiceFactoryPrx sf2 = root->getSession();
  sf2->closeOnDestroy();
  sf2->getQueryService()->get("Experimenter",0L);
}

