#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2008-2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

"""
   Integration test testing distributed processing via
   ServiceFactoryI.acquireProcessor().

"""

import library as lib
import pytest
import os

import omero
import omero.clients
import omero.model
import omero.api
import uuid

from omero.util.temp_files import create_path, remove_path
from omero.rtypes import rlong, rstring, rmap
from omero.scripts import wait

PINGFILE = """
#!/usr/bin/env python

import os
import omero, omero.scripts as s
import uuid

#
# Unique name so that IScript does not reject us
# based on duplicate file names.
#
uuid = str(uuid.uuid4())
print "I am the script named %s" % uuid

#
# Creation
#
client = s.client(uuid, "simple ping script",
    s.Long("a", optional=True).inout(), s.String("b", optional=True).inout())
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

    def processFinished(self, rv, current=True):
        self.finish.append(rv)

    def processCancelled(self, rv, current=True):
        self.cancel.append(rv)

    def processKilled(self, rv, current=True):
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
        id = scripts.uploadOfficialScript(
            "/tests/ping_py/%s.py" % self.uuid(), PINGFILE)
        j = omero.model.ScriptJobI()
        j.linkOriginalFile(omero.model.OriginalFileI(rlong(id), False))
        p = self.client.sf.sharedResources().acquireProcessor(j, 100)
        return p

    def _checkstd(self, output, which):
        rfile = output.val[which]
        ofile = rfile.val
        assert ofile

        tmppath = create_path("pingtest")
        try:
            self.client.download(ofile, str(tmppath))
            assert os.path.getsize(str(tmppath))
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
            assert False, "STDOUT:\n%s\nSTDERR:\n%s\n" % (stdout, stderr)
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
        assert output.val["a"].val == 2

    def testPingParametersViaISCript(self):
        p = self._getProcessor()
        params = p.params()
        assert params
        assert params.inputs["a"]
        assert params.inputs["b"]
        assert params.outputs["a"]
        assert params.outputs["b"]

    def testPingStdout(self):
        p = self._getProcessor()
        params = p.params()
        assert params.stdoutFormat

        process = p.execute(rmap({}))
        self.assertSuccess(p, process)

    @pytest.mark.broken(ticket="11494")
    def testProcessCallback(self):

        callback = CallbackI()

        id = self.client.getCommunicator().stringToIdentity(str(uuid.uuid4()))
        cb = self.client.getAdapter().add(callback, id)
        cb = omero.grid.ProcessCallbackPrx.uncheckedCast(cb)
        p = self._getProcessor()
        params = p.params()
        assert params.stdoutFormat

        process = p.execute(rmap({}))
        process.registerCallback(cb)
        self.assertSuccess(p, process)

        assert len(callback.finish) > 0

    def testProcessShutdown(self):
        p = self._getProcessor()
        process = p.execute(rmap({}))
        process.shutdown()

        p.getResults(process)

        # Line above was: output = p.getResults(process)
        # Probably doesn't have IO since killed
        # self.assertIO(output)

    def testProcessShutdownOneway(self):
        p = self._getProcessor()
        process = p.execute(rmap({}))
        oneway = omero.grid.ProcessPrx.uncheckedCast(process.ice_oneway())
        oneway.shutdown()
        # Depending on what's faster this may or may not throw
        try:
            p.getResults(process)
            assert process.poll()
            p.getResults(process)
        except omero.ServerError:
            pass

        # Line above was: output = p.getResults(process)
        # Probably doesn't have IO since killed
        # self.assertIO(output)

    def testProcessorGetResultsBeforeFinished(self):
        p = self._getProcessor()
        process = p.execute(None)
        with pytest.raises(omero.ServerError):
            p.getResults(process)
        self.assertSuccess(p, process)

    #
    # Execution-less tests
    #

    def testProcessorExpires(self):
        p = self._getProcessor()
        assert p.expires() > 0

    def testProcessorGetJob(self):
        p = self._getProcessor()
        assert p.getJob()

    def testProcessorStop(self):
        p = self._getProcessor()
        p.execute(rmap({}))
        p.stop()

    def testProcessorDetach(self):
        p = self._getProcessor()
        p.execute(rmap({}))
        p.setDetach(True)
        p.stop()
