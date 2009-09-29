#!/usr/bin/env python

"""
   Test of the Process facility independent of Ice.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, logging

logging.basicConfig(level=0)

import omero.processor
import omero.util
import omero.util.concurrency

class log:
    def warning(self, string):
        print "Warning:",string

def make_client(self):
    self.client = None
    self.uuid = "mock_uuid"

omero.processor.ProcessI.make_client = make_client

class TestProcess(unittest.TestCase):

    def setUp(self):
        self.log = logging.getLogger("TestProcess")
        self.ctx = omero.util.ServerContext(server_id='mock', communicator=None, stop_event=omero.util.concurrency.get_event())

    def tearDown(self):
        self.log.info("stop_event")
        self.ctx.stop_event.set()

    def param(self):
        p = {"omero.user":"sessionId","omero.pass":"sessionId", "Ice.Default.Router":"foo"}
        return p

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
        process = omero.processor.ProcessI(self.ctx, "python", self.param(), log())
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
        p = self.param()
        p["omero.scripts.parse"] = "1"
        process = omero.processor.ProcessI(self.ctx, "python", p, log())
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
        env = omero.util.Environment("PATH")
        env.append("PATH", os.pathsep.join(["bob","cat"]))
        env.append("PATH", os.path.join(os.getcwd(), "lib"))

    def testEnvironemnt2(self):
        process = omero.processor.ProcessI(self.ctx, "python", self.param(), log())
        print process.env()

    def testKillProcess(self):
        process = omero.processor.ProcessI(self.ctx, "python", self.param(), log())
        f = open(process.script_name, "w")
        f.write("import time\n")
        f.write("time.sleep(100)\n")
        f.close()
        process.activate()
        self.assertFalse(process.poll())
        process.cleanup()

if __name__ == '__main__':
    unittest.main()
