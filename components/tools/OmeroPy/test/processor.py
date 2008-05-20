#!/usr/bin/env python

"""
   Test of the Process facility independent of Ice.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os
import omero.processor

class log:
    def warning(self, string):
        print "Warning:",string

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
            """
            TODO: This might be useful for testing a situation
            in which an active process gets pass directly to
            ProcessI.__init__(self, popen). At the moment, there's
            no use case for this, so skipping.
            """
            def __init__(self):
                self._poll = None
                self._wait = None
                self.pid = 1
            def poll(self):
                return self._poll
            def wait(self):
                return self._wait

        print """
        SKIPPED! direct opening of popen
        """
        return
        process = omero.processor.ProcessI("python",Popen())
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

        p = {"omero.user":"sessionId","omero.pass":"sessionId", "Ice.Default.Router":"conn"}
        process = omero.processor.ProcessI("python",p,log())
        f = open(process.script_name, "w")
        f.write("""
print "Hello"
        """)
        f.close()
        process.activate()
        print process.dir
        self.assert_( None != process.wait() )
        self.assert_( None != process.poll() )

    def testParameters(self):

        p = {"omero.user":"sessionId","omero.pass":"sessionId", "Ice.Default.Router":"conn"}
        p["omero.scripts.parse"] = "1"
        process = omero.processor.ProcessI("python",p,log())
        f = open(process.script_name, "w")
        f.write("""
import omero, omero.scripts s
client = s.client("name","description",s.Long("l"))
        """)
        f.close()
        process.activate()
        process.wait()
        process.poll()

    def testEnvironment(self):
        env = omero.processor.Environment("PATH")
        env.append("PATH", os.pathsep.join(["bob","cat"]))
        env.append("PATH", os.path.join(os.getcwd(), "lib"))

    def testEnvironemnt2(self):
        p = {"omero.user":"sessionId","omero.pass":"sessionId", "Ice.Default.Router":"foo"}
        process = omero.processor.ProcessI("python",p,log())
        print process.env()

if __name__ == '__main__':
    unittest.main()
