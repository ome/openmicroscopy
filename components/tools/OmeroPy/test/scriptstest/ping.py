#!/usr/bin/env python

"""
   Integration test testing distributed processing via
   ServiceFactoryI.acquireProcessor().

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import integration.library as lib
import unittest, os, sys

import omero
import omero.clients
import omero.model
import omero.api
import omero_ext.uuid as uuid # see ticket:3774

from omero.util.temp_files import create_path, remove_path
from omero.rtypes import *
from omero.scripts import wait

PINGFILE = """
#!/usr/bin/env python

import os
import omero, omero.scripts as s
import omero_ext.uuid as uuid # see ticket:3774

#
# Unique name so that IScript does not reject us
# based on duplicate file names.
#
uuid = str(uuid.uuid4())
print "I am the script named %s" % uuid

#
# Creation
#
client = s.client(uuid, "simple ping script", s.Long("a", optional=True).inout(), s.String("b", optional=True).inout())
print "Session", client.getSession()

#
# Various diagnostics
#
import sys
from pprint import pprint
print "PATH:"
pprint(sys.path)

print "CONFIG"
f = open("config","r")
print "".join(f.readlines())
f.close()

from omero.rtypes import *

import Ice
ic = Ice.initialize(["--Ice.Plugin.IceSSL=IceSSL:createIceSSL"])
print ic.getProperties().getPropertiesForPrefix("Ice")
print ic.getProperties().getPropertiesForPrefix("omero")

#
# Echo'ing input to output
#
keys = client.getInputKeys()
print "Keys found:"
print keys
for key in keys:
    client.setOutput(key, client.getInput(key))

#
# Env
#
print "This was my environment:"
for k,v in os.environ.items():
    print "%s => %s" %(k,v)

sys.stderr.write("Oh, and this is stderr.");

"""

class CallbackI(omero.grid.ProcessCallback):

    def __init__(self):
        self.finish = []
        self.cancel = []
        self.kill = []

    def processFinished(self, rv, current = True):
        self.finish.append(rv)

    def processCancelled(self, rv, current = True):
        self.cancel.append(rv)

    def processKilled(self, rv, current = True):
        self.kill.append(rv)

class TestPing(lib.ITest):
    """
    Tests which use the trivial script defined by PINGFILE to
    test the scripts API.
    """

    #
    # Helper methods
    #

    def _getProcessor(self):
        scripts = self.root.getSession().getScriptService()
        id = scripts.uploadOfficialScript("/tests/ping_py/%s.py" % self.uuid(), PINGFILE)
        j = omero.model.ScriptJobI()
        j.linkOriginalFile(omero.model.OriginalFileI(rlong(id),False))
        p = self.client.sf.sharedResources().acquireProcessor(j, 100)
        return p

    def _checkstd(self, output, which):
        rfile = output.val[which]
        ofile = rfile.val
        self.assert_( ofile )

        tmppath = create_path("pingtest")
        try:
            self.client.download(ofile, str(tmppath))
            self.assert_( os.path.getsize(str(tmppath)))
            return tmppath.text()
        finally:
            remove_path(tmppath)

    def assertIO(self, output):
        stdout = self._checkstd(output, "stdout")
        stderr = self._checkstd(output, "stderr")
        return stdout, stderr

    def assertSuccess(self, processor, process):
        wait(self.client, process)
        rc = process.poll()
        output = processor.getResults(process)
        stdout, stderr = self.assertIO(output)
        if rc is None or rc.val != 0:
            self.fail("STDOUT:\n%s\nSTDERR:\n%s\n" % (stdout, stderr))
        return output

    #
    # Test methods
    #

    def testPingViaISCript(self):
        p = self._getProcessor()
        input = rmap({})
        input.val["a"] = rlong(2)
        input.val["b"] = rstring("d")
        process = p.execute(input)
        output = self.assertSuccess(p, process)
        self.assert_( 2 == output.val["a"].val )

    def testPingParametersViaISCript(self):
        p = self._getProcessor()
        params = p.params()
        self.assert_( params )
        self.assert_( params.inputs["a"] )
        self.assert_( params.inputs["b"] )
        self.assert_( params.outputs["a"] )
        self.assert_( params.outputs["b"] )

    def testPingStdout(self):
        p = self._getProcessor()
        params = p.params()
        self.assert_( params.stdoutFormat )

        process = p.execute(rmap({}))
        output = self.assertSuccess(p, process)

    def testProcessCallback(self):

        callback = CallbackI()

        id = self.client.getCommunicator().stringToIdentity(str(uuid.uuid4()))
        cb = self.client.getAdapter().add(callback, id)
        cb = omero.grid.ProcessCallbackPrx.uncheckedCast(cb)
        p = self._getProcessor()
        params = p.params()
        self.assert_( params.stdoutFormat )

        process = p.execute(rmap({}))
        process.registerCallback(cb)
        output = self.assertSuccess(p, process)

        self.assertTrue( len(callback.finish) > 0 )

    def testProcessShutdown(self):
        p = self._getProcessor()
        process = p.execute(rmap({}))
        process.shutdown()

        output = p.getResults(process)
        # Probably doesn't have IO since killed
        # self.assertIO(output)

    def testProcessShutdownOneway(self):
        p = self._getProcessor()
        process = p.execute(rmap({}))
        oneway = omero.grid.ProcessPrx.uncheckedCast( process.ice_oneway() )
        oneway.shutdown()
        # Depending on what's faster this may or may not throw
        try:
            p.getResults(process)
            self.assert_(process.poll())
            output = p.getResults(process)
        except omero.ServerError:
            pass

        # Probably doesn't have IO since killed
        # self.assertIO(output)

    def testProcessorGetResultsBeforeFinished(self):
        p = self._getProcessor()
        process = p.execute(None)
        self.assertRaises(omero.ServerError, p.getResults, process)
        output = self.assertSuccess(p, process)

    #
    # Execution-less tests
    #

    def testProcessorExpires(self):
        p = self._getProcessor()
        self.assertTrue( p.expires() > 0 )

    def testProcessorGetJob(self):
        p = self._getProcessor()
        self.assert_( p.getJob() )

    def testProcessorStop(self):
        p = self._getProcessor()
        process = p.execute(rmap({}))
        p.stop()

    def testProcessorDetach(self):
        p = self._getProcessor()
        process = p.execute(rmap({}))
        p.setDetach(True)
        p.stop()

if __name__ == '__main__':
    unittest.main()
