#!/usr/bin/env python
# -*- coding: utf-8 -*-
import time
import omero
import threading

IDLETIME = 5

c = omero.client()
s = c.createSession()
re = s.createRenderingEngine()


class KeepAlive(threading.Thread):
    def run(self):
        self.stop = False
        while not self.stop:
            time.sleep(IDLETIME)
            print "calling keep alive"
            # Currently, passing a null or empty array to keepAllAlive
            # would suffice. For future-proofing, however, it makes sense
            # to pass stateful services.
            try:
                s.keepAllAlive([re])
            except:
                c.closeSession()
                raise

keepAlive = KeepAlive()
keepAlive.start()

time.sleep(IDLETIME * 2)
keepAlive.stop = True
