#!/usr/bin/env python

"""
   Integration test demonstrating various script creation methods

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import integration.library as lib
import unittest, os, sys, uuid

import omero
import omero.clients
import omero.model
import omero.api
import omero_api_IScript_ice
from omero.util.temp_files import create_path, remove_path

import omero.rtypes as OR

IMPORTS = """#!/usr/bin/env python

import omero
import omero.grid as OG
import omero.rtypes as OR
import omero.scripts OS

"""

FILE1 = IMPORTS + """# Old-style script definition

in1 = OS.Long("a")
in2 = OS.Long("b")
out1 = OS.String("result").out()

c = OS.client(name="my script", description="Takes two longs, and returns a string", in1, in2, out1)

try:
    c.setOutput(OR.rstring("my simple result"))
finally:
    c.closeSession()
"""

FILE2 = IMPORTS + """# Using params

in1 =  OG.Param(name="a",      prototype=OS.rlong(0))
in2 =  OG.Param(name="b",      prototype=OS.rlong(0))
out1 = OG.Param(name="result", prototype=OS.rstring(""))

c = OS.client(name="my script", description="Takes two longs, and returns a string", inputs={in1, in2], outputs=[out1])

try:
    c.setOutput(OR.rstring("my simple result"))
finally:
    c.closeSession()
"""

FILE3 = IMPORTS + """#Using JobParams

in1 =  OG.Param(name="a",      prototype=OS.rlong(0))
in2 =  OG.Param(name="b",      prototype=OS.rlong(0))
out1 = OG.Param(name="result", prototype=OS.rstring(""))

params = OG.JobParams()
params.name = "my script"
params.description = "Takes two longs, and returns a string"
params.inputs = [in1, in2]
params.output = [out1]

params.authors = ["Me", "Myself", "I"]
params.institutions = ["Here", "Now"]
params.authorsInstitutionsIndex = [ [0,1], [0], [1] ]

params.namespaces = ["algorithm", "simple"]

params.stderrFormat = "" # Ignore

c = OS.client(params)

try:
    c.setOutput(OR.rstring("my simple result"))
finally:
    c.closeSession()
"""

class TestParams(lib.ITest):

    def run(self, text):
        svc = self.client.sf.getScriptService()
        id = svc.uploadScript(text)
        processor = svc.runScript(id, OR.wrap({"a":0, "b":0}), None)
        processor.poll()

    def test1(self):
        self.run(FILE1)

    def test2(self):
        self.run(FILE2)

    def test3(self):
        self.run(FILE3)

    def test4(self):
        self.run(FILE4)

if __name__ == '__main__':
    unittest.main()
