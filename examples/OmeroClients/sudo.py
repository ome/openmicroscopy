#!/usr/bin/env python
# -*- coding: utf-8 -*-
import sys
import omero

args = list(sys.argv)
client = omero.client(args)
sudoClient = omero.client(args)
try:
    sf = client.createSession("root", "ome")
    sessionSvc = sf.getSessionService()

    p = omero.sys.Principal()
    p.name = "root"  # Can change to any user
    p.group = "user"
    p.eventType = "User"

    # 3 minutes to live
    sudoSession = sessionSvc.createSessionWithTimeout(p, 3*60*1000L)

    sudoSf = sudoClient.joinSession(sudoSession.getUuid().getValue())
    sudoAdminSvc = sudoSf.getAdminService()
    print sudoAdminSvc.getEventContext().userName

finally:
    sudoClient.closeSession()
    client.closeSession()
