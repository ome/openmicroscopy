#!/usr/bin/env python

"""
   Test of the Process facility independent of Ice.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import omero.processor

class log:
    def warning(self, string):
        print string

class TestProcess(unittest.TestCase):

    def testMockPopen(self):

        class Callback(object):
            def __init__(self):
                self._finished = None
                self._cancelled = None
                self._killed = None
            def processFinished(self, rc):
                self._finished = rc
            def processCancelled(self, success):
                self._cancelled = success
            def processKilled(self, success):
                self._killed = success

        class Popen(object):
            def __init__(self):
                self._poll = None
                self._wait = None
                self.pid = 1
            def poll(self):
                return self._poll
            def wait(self):
                return self._wait

        popen = Popen()
        process = omero.processor.ProcessI(None, popen)
        print process.poll()
        self.assert_( not process.poll() )
        popen._poll = 1
        self.assert_( 1 == process.poll().val )
        self.assert_( None == process.wait() )
        popen._wait = 1
        self.assert_( 1 == process.wait() )

        callback = Callback()
        process.registerCallback(callback)
        process.allcallbacks("cancel", True)
        self.asseert_( callback._cancelled )

    def testPopen(self):

        p = {"name":"name","pasw":"pass", "conn":"conn"}
        process = omero.processor.ProcessI("python",p,log())
        f = open(process.script_name, "w")
        f.write("""
print "Hello"
        """)
        process.activate()
        process.wait()
        process.poll()

    def testParameters(self):

        p = {"name":"name","pasw":"pass", "conn":"conn"}
        p["omero.scripts.parse"] = "1"
        process = omero.processor.ProcessI("python",p,log())
        f = open(process.script_name, "w")
        f.write("""
import omero, omero.scripts s
client = s.client("name","description",s.Long("l"))
        """)
        process.activate()
        process.wait()
        process.poll()

if __name__ == '__main__':
    unittest.main()
