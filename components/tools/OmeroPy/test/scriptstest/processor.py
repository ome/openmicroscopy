#!/usr/bin/env python

"""
   Test of the Process facility independent of Ice.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, sys, logging, subprocess

logging.basicConfig(level=logging.DEBUG)

import Ice
import omero.processor
import omero.util
import omero.util.concurrency
from omero_ext.functional import wraps

def pass_through(arg):
    return arg

def make_client(self):
    self.client = None
    self.uuid = "mock_uuid"

def _term(self, *args):
    self.rcode = -9

omero.processor.ProcessI._term = _term
omero.processor.ProcessI.make_client = make_client

class Callback(object):
    def __init__(self):
        self._finished = None
        self._cancelled = None
        self._killed = None
    def ice_getIdentity(self):
        return Ice.Identity("a","b")
    def ice_oneway(self):
        return self
    def processFinished(self, rc):
        self._finished = rc
    def processCancelled(self, success):
        self._cancelled = success
    def processKilled(self, success):
        self._killed = success

class MockPopen(object):
    def __init__(self, *args, **kwargs):
        self.args = args
        self.kwargs = kwargs
        self.rcode = None
        self.pid = 1
    def poll(self):
        return self.rcode
    def wait(self):
        return self.rcode
    def kill(self, *args):
        self.rcode = -9
        return self.rcode

def with_process(func, Popen = MockPopen):
    """ Decorator for running a test with a Process """
    def handler(*args, **kwargs):
        self = args[0]
        self.process = omero.processor.ProcessI(self.ctx, sys.executable, self.props(), self.params(), Popen = Popen, callback_cast = pass_through)
        try:
            rv = func(*args, **kwargs)
        finally:
            self.process.cleanup()
    return wraps(func)(handler)

class TestProcess(unittest.TestCase):

    def setUp(self):
        self.log = logging.getLogger("TestProcess")
        self.ctx = omero.util.ServerContext(server_id='mock', communicator=None, stop_event=omero.util.concurrency.get_event())

    def tearDown(self):
        self.log.info("stop_event")
        self.ctx.stop_event.set()

    def props(self):
        p = {"omero.user":"sessionId","omero.pass":"sessionId", "Ice.Default.Router":"foo"}
        return p

    def params(self):
        params = omero.grid.JobParams()
        params.name = "name"
        params.description = "description"
        params.inputs = {}
        params.outputs = {}
        params.stdoutFormat = "text/plain"
        params.stderrFormat = "text/plain"
        return params

    #
    # Env
    #

    def testEnvironment(self):
        env = omero.util.Environment("PATH")
        env.append("PATH", os.pathsep.join(["bob","cat"]))
        env.append("PATH", os.path.join(os.getcwd(), "lib"))

    def testEnvironemnt2(self):
        process = omero.processor.ProcessI(self.ctx, sys.executable, self.props(), self.params())

    #
    # MockPopen
    #

    @with_process
    def testMockPopenPoll(self):
        self.process.activate()
        self.assertEquals(None, self.process.poll())
        self.process.popen.rcode = 1
        self.assertEquals(1, self.process.poll().val)
        # Now wait should return too
        self.assertEquals(1, self.process.wait())

    @with_process
    def testMockPopenWait(self):
        self.process.activate()
        self.assertEquals(True, self.process.isActive())
        self.process.popen.rcode = 1
        self.assertEquals(1, self.process.wait())
        self.assertEquals(1, self.process.poll().val)

    @with_process
    def testMockPopenAlreadyDone(self):
        self.assertFalse(self.process.isActive())
        self.process.activate()
        self.assertTrue(self.process.isActive())
        self.assertFalse(self.process.isFinished())
        self.assertFalse(self.process.alreadyDone())
        self.process.deactivate()
        self.assertFalse(self.process.isActive())
        self.assertTrue(self.process.isFinished())
        self.assertTrue(self.process.alreadyDone())

    @with_process
    def testCallback(self):
        callback = Callback()
        self.process.activate()
        self.process.registerCallback(callback)
        self.process.allcallbacks("processCancelled", True)
        self.assert_( callback._cancelled )

    #
    # Real calls
    #

    def testPopen(self):
        f = open(str(self.process.script_path), "w")
        f.write("""
print "Hello"
        """)
        f.close()
        self.process.activate()
        self.assert_( None != self.process.wait() )
        self.assert_( None != self.process.poll() )
    testPopen = with_process(testPopen, subprocess.Popen)

    def testParameters(self):
        p = self.props()
        p["omero.scripts.parse"] = "1"
        f = open(str(self.process.script_path), "w")
        f.write("""
import omero, omero.scripts s
client = s.client("name","description",s.Long("l"))
        """)
        f.close()
        self.process.activate()
        self.process.wait()
        self.assert_( self.process.poll() )
    testParameters = with_process(testParameters, subprocess.Popen)

    def testKillProcess(self):
        f = open(str(self.process.script_path), "w")
        f.write("import time\n")
        f.write("time.sleep(100)\n")
        f.close()
        self.process.activate()
        self.assertFalse(self.process.poll())
        self.process.cleanup()
    testKillProcess = with_process(testKillProcess, subprocess.Popen)

if __name__ == '__main__':
    unittest.main()
