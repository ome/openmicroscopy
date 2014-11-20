/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <omero/IceNoWarnPush.h>
#include <Ice/Initialize.h>
#include <omero/IceNoWarnPop.h>
#include <omero/client.h>
#include <omero/model/ExperimenterI.h>
#include <omero/model/GroupExperimenterMapI.h>
#include <omero/model/ExperimenterGroupI.h>
#include <omero/model/SessionI.h>
#include <omero/fixture.h>

using namespace omero::rtypes;

TEST(SessionsTest, RootCanCreateSessionForUser )
{
  Fixture f;
  omero::api::ServiceFactoryPrx sf = f.root->getSession();
  omero::api::ISessionPrx sess = sf->getSessionService();

  omero::model::ExperimenterGroupPtr group = f.newGroup("rwr---");
  omero::model::ExperimenterPtr e = f.newUser(group);

  omero::sys::PrincipalPtr p = new omero::sys::Principal();
  p->name = e->getOmeName()->getValue();
  p->group = e->getPrimaryGroupExperimenterMap()->getParent()->getName()->getValue();
  p->eventType = "Test";
  omero::model::SessionPtr session = sess->createSessionWithTimeout(p, 10000L);

  omero::client_ptr user = new omero::client(f.root->getPropertyMap());
  user->joinSession(session->getUuid()->getValue());
  omero::api::ServiceFactoryPrx sf2 = f.root->getSession();
  sf2->closeOnDestroy();
  sf2->getQueryService()->get("Experimenter",0L);
}

