#!/usr/bin/env python

"""
   Integration test testing distributed processing via
   ServiceFactoryI.acquireProcessor().

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import test.integration.library as lib
import tempfile, unittest, os, sys, uuid

import omero
import omero.clients
import omero.model
import omero.api
import omero_api_IScript_ice
from omero.rtypes import *

PINGFILE = """
#!/usr/bin/env python

import sys
from pprint import pprint
print "PATH:"
pprint(sys.path)

print "CONFIG"
f = open("config","r")
print "".join(f.readlines())
f.close()

import os, uuid
import omero, omero.scripts as s
from omero.rtypes import *

import Ice
ic = Ice.initialize()
print ic.getProperties().getPropertiesForPrefix("Ice")
print ic.getProperties().getPropertiesForPrefix("omero")

#
# Unique name so that IScript does not reject us
# based on duplicate file names.
#
uuid = str(uuid.uuid4())
print "I am the script named %s" % uuid

#
# Creation
#
client = s.client(uuid, "simple ping script", s.Long("a").inout(), s.String("b").inout())
print "Session", client.getSession()

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

PUBLIC = omero.model.PermissionsI()
PUBLIC.setGroupRead(True)
PUBLIC.setWorldRead(True)

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

    def testUploadAndPing(self):
        pingfile = tempfile.NamedTemporaryFile(mode='w+t')
        pingfile.close();
        try:
            name = pingfile.name
            pingfile = open(name, "w")
            pingfile.write(PINGFILE)
            pingfile.flush()
            pingfile.close()
            file = self.root.upload(name, type="text/x-python", permissions = PUBLIC)
            j = omero.model.ScriptJobI()
            j.linkOriginalFile(file)

            p = self.client.sf.sharedResources().acquireProcessor(j, 100)
            jp = p.params()
            self.assert_(jp, "Non-zero params")

            input = rmap({})
            input.val["a"] = rint(1)
            input.val["b"] = rstring("c")
            process = p.execute(input)
            rc = process.wait()
            if rc:
                self.assert_(rc == 0, "Non-zero return code")
            output = p.getResults(process)
            self.assert_( 1 == output.val["a"].val )
        finally:
            if os.path.exists(name):
                os.remove(name)

    def _getProcessor(self):
        scripts = self.root.getSession().getScriptService()
        id = scripts.uploadScript(PINGFILE)
        j = omero.model.ScriptJobI()
        j.linkOriginalFile(omero.model.OriginalFileI(rlong(id),False))
        p = self.client.sf.sharedResources().acquireProcessor(j, 100)
        return p

    def testPingViaISCript(self):
        p = self._getProcessor()
        input = rmap({})
        input.val["a"] = rint(2)
        input.val["b"] = rstring("d")
        process = p.execute(input)
        process.wait()
        output = p.getResults(process)
        self.assert_( 2 == output.val["a"].val )

    def testPingParametersViaISCript(self):
        p = self._getProcessor()
        params = p.params()
        self.assert_( params )
        self.assert_( params.inputs["a"] )
        self.assert_( params.inputs["b"] )
        self.assert_( params.outputs["a"] )
        self.assert_( params.outputs["b"] )

    def _checkstd(self, output, which):
        rfile = output.val[which]
        ofile = rfile.val
        self.assert_( ofile )

        tmpfile = tempfile.NamedTemporaryFile(mode='w+t')
        try:
            self.client.download(ofile, tmpfile.name)
            self.assert_( os.path.getsize(tmpfile.name) )
        finally:
            tmpfile.close()

    def assertIO(self, output):
        self._checkstd(output, "stdout")
        self._checkstd(output, "stderr")

    def testPingStdout(self):
        p = self._getProcessor()
        params = p.params()
        self.assert_( params.stdoutFormat )

        process = p.execute(rmap({}))
        process.wait()

        output = p.getResults(process)
        self.assertIO(output)

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
        process.wait()
        output = p.getResults(process)
        self.assertIO(output)

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
        process.wait()

        output = p.getResults(process)
        self.assertIO(output)

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
