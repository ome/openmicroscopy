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
#include <boost_fixture.h>

BOOST_AUTO_TEST_CASE( RootCanCreateSessionForUser )
{
  Fixture f;

  int argc = 0;
  char** argv = new char*[0];
  const omero::client* root = f.root_login();
  omero::api::ServiceFactoryPrx sf = (*root).getSession();
  omero::api::ISessionPrx sess = sf->getSessionService();

  omero::model::ExperimenterIPtr e = new omero::model::ExperimenterI();
  e->setOmeName(new omero::RString(f.uuid()));
  e->setFirstName(new omero::RString("session"));
  e->setLastName(new omero::RString("test"));
  sf->getAdminService()->createUser(e, "default");

  omero::sys::PrincipalPtr p = new omero::sys::Principal();
  p->name = e->getOmeName()->val;
  p->group = "default";
  p->eventType = "Test";
  omero::model::SessionPtr session = sess->createSessionWithTimeout(p, 10000L);

  omero::client user(argc, argv);
  user.createSession(e->getOmeName()->val,session->getUuid()->val);
  omero::api::ServiceFactoryPrx sf2 = (*root).getSession();
  sf2->closeOnDestroy();
  sf2->getQueryService()->get("Experimenter",0L);
}

