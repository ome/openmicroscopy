#!/usr/bin/env python

"""
   Test of the omero.scripts comparison functionality

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from path import path

import sys
import unittest

import omero
from omero.scripts import *
from omero.rtypes import *

class TestPrototypes(unittest.TestCase):

    # Nested lists

    def testRListRInt(self):
        params = omero.grid.JobParams()
        param = omero.grid.Param()
        param.prototype = rlist(rint(0))
        params.inputs = {"a":param}

        input = rlist(rint(1))
        inputs = {"a":input}
        self.assertEquals("", validate_inputs(params, inputs))

    def testRListRList(self):
        params = omero.grid.JobParams()
        param = omero.grid.Param()
        param.prototype = rlist(rlist())
        params.inputs = {"a":param}

        input = rlist(rlist(rint(1), rstring("a")))
        inputs = {"a":input}
        self.assertEquals("", validate_inputs(params, inputs))

    def testRListRListRString(self):
        params = omero.grid.JobParams()
        param = omero.grid.Param()
        param.prototype = rlist(rlist(rstring("")))
        params.inputs = {"a":param}

        input = rlist(rlist(rstring("a")))
        inputs = {"a":input}
        self.assertEquals("", validate_inputs(params, inputs))

        input.val[0].val.insert(0, rint(1))
        self.assertFalse( "" == validate_inputs(params, inputs) )

    # Nested maps

    def testRMapRInt(self):
        params = omero.grid.JobParams()
        param = omero.grid.Param()
        param.prototype = rmap({"b":rint(0)})
        params.inputs = {"a":param}

        input = rmap({"b":rint(1)})
        inputs = {"a":input}
        self.assertEquals("", validate_inputs(params, inputs))

    def testRMapRMap(self):
        params = omero.grid.JobParams()
        param = omero.grid.Param()
        param.prototype = rmap({"b":rmap({})})
        params.inputs = {"a":param}

        input = rmap({"b":rmap({"l":rlong(0)})})
        inputs = {"a":input}
        self.assertEquals("", validate_inputs(params, inputs))

    def testRMapRMapRInt(self):
        params = omero.grid.JobParams()
        param = omero.grid.Param()
        param.prototype = rmap({"b":rmap({"c":rint(0)})})
        params.inputs = {"a":param}

        input = rmap({"b":rmap({"c":rint(1)})})
        inputs = {"a":input}
        self.assertEquals("", validate_inputs(params, inputs))

    # Other

    def testAllParametersChecked(self):
        params = omero.grid.JobParams()
        param = omero.grid.Param()
        param.prototype = rlist(rstring(""))
        params.inputs = {"a":param}

        input = rlist(rstring("foo"), rint(1))
        inputs = {"a":input}
        self.assertFalse("" == validate_inputs(params, inputs))

    # Bugs

    def testTicket2323Min(self):
        params = omero.grid.JobParams()
        # Copied from integration/scripts.py:testUploadOfficialScripts
        param = Long('longParam', True, description='theDesc', min=long(1), max=long(10), values=[rlong(5)])
        self.assertEquals(1, param.min.getValue(), "Min value not correct:" + str(param.min))
        self.assertEquals(10, param.max.getValue(), "Max value not correct:" + str(param.max))
        self.assertEquals(5, param.values.getValue()[0].getValue(), "First option value not correct:" + str(param.values))

        params.inputs = {"a": param}
        inputs = {"a": rlong(5)}
        errors = validate_inputs(params, inputs)
        self.assertTrue("" == errors, errors)

    def testTicket2323List(self):
        param = List('listParam', True, description='theDesc', values=[rlong(5)])
        self.assertEquals([5], unwrap(param.values), str(param.values))

if __name__ == '__main__':
    unittest.main()
