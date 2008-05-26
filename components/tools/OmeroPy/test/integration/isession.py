#!/usr/bin/env python

"""
   Integration test focused on the omero.api.IPojos interface
   a running server.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import test.integration.library as lib
import omero
import omero_RTypes_ice
from omero_model_PixelsI import PixelsI
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_GroupExperimenterMapI import GroupExperimenterMapI
from omero_model_DatasetImageLinkI import DatasetImageLinkI

class TestISession(lib.ITest):

    def testBasicUsage(self):
        isess = self.client.sf.getSessionService()
        admin = self.client.sf.getAdminService()
        sid   = admin.getEventContext().sessionUuid

    def testGettingACopyOfSessionForUpdate(self):
        isess = self.client.sf.getSessionService()
        admin = self.client.sf.getAdminService()
        sid   = admin.getEventContext().sessionUuid

        p = omero.sys.Principal() # dummy
        session = isess.createSession(p, sid)
        return session

    def testManuallyClosingOwnSession(self):
        session = self.testGettingACopyOfSessionForUpdate()

        isess = self.client.sf.getSessionService()
        s = isess.updateSession(session)
        isess.closeSession(s)

    def testCreateSessionForUser(self):
        user = self.root.sf.getQueryService().findAllByQuery("""
            select e from Experimenter e where e.id > 0 and e.omeName != 'guest'
            """, None)[0]
        p = omero.sys.Principal()
        p.name  = user.omeName.val
        p.group = "user"
        p.eventType = "Test"
        sess  = self.root.sf.getSessionService().createSessionWithTimeout(p, 10000) # 10 secs

        client = omero.client()
        user_sess = client.createSession(sess.uuid,sess.uuid)
        new_uuid   = user_sess.getAdminService().getEventContext().sessionUuid
        self.assert_( sess.uuid.val == new_uuid )
        client.closeSession()

    def testCreateSessionForGuest(self):
        p = omero.sys.Principal()
        p.name  = "guest"
        p.group = "guest"
        p.eventType = "guest"
        sess  = self.root.sf.getSessionService().createSessionWithTimeout(p, 10000) # 10 secs

        guest_client = omero.client()
        guest_sess = guest_client.createSession("guest",sess.uuid)
        guest_client.closeSession()

if __name__ == '__main__':
    unittest.main()
