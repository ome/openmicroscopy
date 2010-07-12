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
#include <omero/model/GroupExperimenterMapI.h>
#include <omero/model/ExperimenterGroupI.h>
#include <omero/model/SessionI.h>
#include <boost_fixture.h>

using namespace omero::rtypes;

BOOST_AUTO_TEST_CASE( RootCanCreateSessionForUser )
{
  Fixture f;

  const omero::client_ptr root = f.root_login();
  omero::api::ServiceFactoryPrx sf = root->getSession();
  omero::api::ISessionPrx sess = sf->getSessionService();


  omero::model::ExperimenterPtr e = f.newUser(sf->getAdminService());

  omero::sys::PrincipalPtr p = new omero::sys::Principal();
  p->name = e->getOmeName()->getValue();
  p->group = e->getPrimaryGroupExperimenterMap()->getParent()->getName()->getValue();
  p->eventType = "Test";
  omero::model::SessionPtr session = sess->createSessionWithTimeout(p, 10000L);

  omero::client user(root->getPropertyMap());
  user.joinSession(session->getUuid()->getValue());
  omero::api::ServiceFactoryPrx sf2 = root->getSession();
  sf2->closeOnDestroy();
  sf2->getQueryService()->get("Experimenter",0L);
}

