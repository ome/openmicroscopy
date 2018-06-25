#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the omero.scripts comparison functionality

   Copyright 2010-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import omero
from omero.scripts import Long, List, Set, validate_inputs
from omero.rtypes import rmap, rint, rlong, rstring, rlist, rset, unwrap


class TestPrototypes(object):

    def testRSetRInt(self):
        params = omero.grid.JobParams()
        param = omero.grid.Param()
        param.prototype = rset(rint(0))
        params.inputs = {"a": param}

        input = rset(rint(1))
        inputs = {"a": input}
        assert "" == validate_inputs(params, inputs)

    # Nested lists
    def testRListRInt(self):
        params = omero.grid.JobParams()
        param = omero.grid.Param()
        param.prototype = rlist(rint(0))
        params.inputs = {"a": param}

        input = rlist(rint(1))
        inputs = {"a": input}
        assert "" == validate_inputs(params, inputs)

    def testRListRList(self):
        params = omero.grid.JobParams()
        param = omero.grid.Param()
        param.prototype = rlist(rlist())
        params.inputs = {"a": param}

        input = rlist(rlist(rint(1), rstring("a")))
        inputs = {"a": input}
        assert "" == validate_inputs(params, inputs)

    def testRListRListRString(self):
        params = omero.grid.JobParams()
        param = omero.grid.Param()
        param.prototype = rlist(rlist(rstring("")))
        params.inputs = {"a": param}

        input = rlist(rlist(rstring("a")))
        inputs = {"a": input}
        assert "" == validate_inputs(params, inputs)

        input.val[0].val.insert(0, rint(1))
        assert not "" == validate_inputs(params, inputs)

    # Nested maps
    def testRMapRInt(self):
        params = omero.grid.JobParams()
        param = omero.grid.Param()
        param.prototype = rmap({"b": rint(0)})
        params.inputs = {"a": param}

        input = rmap({"b": rint(1)})
        inputs = {"a": input}
        assert "" == validate_inputs(params, inputs)

    def testRMapRMap(self):
        params = omero.grid.JobParams()
        param = omero.grid.Param()
        param.prototype = rmap({"b": rmap({})})
        params.inputs = {"a": param}

        input = rmap({"b": rmap({"l": rlong(0)})})
        inputs = {"a": input}
        assert "" == validate_inputs(params, inputs)

    def testRMapRMapRInt(self):
        params = omero.grid.JobParams()
        param = omero.grid.Param()
        param.prototype = rmap({"b": rmap({"c": rint(0)})})
        params.inputs = {"a": param}

        input = rmap({"b": rmap({"c": rint(1)})})
        inputs = {"a": input}
        assert "" == validate_inputs(params, inputs)

    # Other
    def testAllParametersChecked(self):
        params = omero.grid.JobParams()
        param = omero.grid.Param()
        param.prototype = rlist(rstring(""))
        params.inputs = {"a": param}

        input = rlist(rstring("foo"), rint(1))
        inputs = {"a": input}
        assert not "" == validate_inputs(params, inputs)

    # Bugs
    def testTicket2323Min(self):
        params = omero.grid.JobParams()
        # Copied from integration/scripts.py:testUploadOfficialScripts
        param = Long('longParam', True, description='theDesc', min=long(1),
                     max=long(10), values=[rlong(5)])
        assert 1 == param.min.getValue(), \
            "Min value not correct:" + str(param.min)
        assert 10 == param.max.getValue(), \
            "Max value not correct:" + str(param.max)
        assert 5 == param.values.getValue()[0].getValue(), \
            "First option value not correct:" + str(param.values)

        params.inputs = {"a": param}
        inputs = {"a": rlong(5)}
        errors = validate_inputs(params, inputs)
        assert "" == errors, errors

    def testTicket2323List(self):
        param = List('listParam', True, description='theDesc',
                     values=[rlong(5)])
        assert [5] == unwrap(param.values), str(param.values)
