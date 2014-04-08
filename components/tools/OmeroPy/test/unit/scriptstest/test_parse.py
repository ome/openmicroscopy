#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the omero.scripts.parse functionality

   Copyright 2009-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import pytest

from path import path

import omero

from omero.grid import JobParams
from omero.scripts import String, List, Bool, Long, MissingInputs, ParseExit
from omero.scripts import client, parse_inputs, validate_inputs, parse_text
from omero.scripts import parse_file, group_params, rlong, rint, wrap, unwrap

SCRIPTS = path(".") / "scripts" / "omero"


class TestParse(object):

    def testParse(self):
        try:
            class mock(object):
                def setAgent(self, *args):
                    pass

                def createSession(self, *args):
                    return self

                def detachOnDestroy(self, *args):
                    pass

                def getProperty(self, *args):
                    return "true"

                def setOutput(self, *args):
                    pass
            script_client = client(
                "testParse", "simple ping script", Long("a").inout(),
                String("b").inout(), client=mock())
            print "IN CLIENT: " + \
                script_client.getProperty("omero.scripts.parse")
            assert False, "Should have raised ParseExit"
        except ParseExit:
            pass

    def testMinMaxTicket2318(self):
        # Duplicating from some of the scripts to reproduce problem
        def makeParam(paramClass, name, description=None, optional=True,
                      min=None, max=None, values=None):
            param = paramClass(name, optional, description=description,
                               min=min, max=max, values=values)
            return param
        p = makeParam(Long, "thumbSize", "The dimension of each thumbnail."
                      " Default is 100", True, 10, 250)
        assert 10 == p.min.val
        assert 250 == p.max.val

    def testTicket2323(self):
        SCRIPT = """
if True:
    import omero
    from omero.rtypes import rstring, rlong
    import omero.scripts as scripts
    client = scripts.client(
        'HelloWorld.py', 'Hello World example script',
        scripts.Long('longParam', True, description='theDesc', min=long(1),
        max=long(10), values=[rlong(5)]) )
    client.setOutput('returnMessage', rstring('Script ran OK!'))"""
        params = parse_text(SCRIPT)
        longParam = params.inputs["longParam"]
        assert 1 == unwrap(longParam.min), str(longParam.min)
        assert 10 == unwrap(longParam.max), str(longParam.max)
        assert [5] == unwrap(longParam.values), str(longParam.values)

    def testObjectType(self):
        SCRIPT = """
if True:
    import omero
    import omero.all
    import omero.scripts as scripts
    from omero.rtypes import robject

    client = scripts.client(
        'RObjectExample.py', 'Example script passing an robject',
        scripts.Object('objParam', True, description='theDesc'))"""
        params = parse_text(SCRIPT)
        objParam = params.inputs["objParam"]
        assert isinstance(objParam.prototype, omero.RObject)
        assert objParam.prototype.val is None

        rv = parse_inputs(["objParam=OriginalFile:1"], params)
        assert rv["objParam"].val.__class__ == omero.model.OriginalFileI
        assert rv["objParam"].val.id.val == 1

    def testObjectTypeWithDefault(self):
        SCRIPT = """
if True:
    import omero
    import omero.all
    import omero.scripts as scripts
    from omero.rtypes import robject

    client = scripts.client(
        'RObjectExampleWithDefault.py', 'Example script passing an robject',
        scripts.Object('objParam', True, description='theDesc',
                       default=omero.model.ImageI()))"""
        params = parse_text(SCRIPT)
        objParam = params.inputs["objParam"]
        assert isinstance(objParam.prototype, omero.RObject)
        assert isinstance(objParam.prototype.val, omero.model.ImageI)

    def testListOfType(self):
        SCRIPT = """
if True:
    import omero
    import omero.all
    from omero.rtypes import rstring, rlong
    import omero.scripts as scripts
    client = scripts.client(
        'HelloWorld.py', 'Hello World example script',
        scripts.List('Image_List').ofType(omero.model.ImageI))
    client.setOutput('returnMessage', rstring('Script ran OK!'))"""
        params = parse_text(SCRIPT)
        listParam = params.inputs["Image_List"]
        assert isinstance(listParam.prototype.val[0].val, omero.model.Image)

    def testGrouping(self):
        SCRIPT = """if True:
            from omero.scripts import *
            c = client('testGrouping',
                Long('these', grouping="A.1"),
                Long('belong', grouping="A.2"),
                Long('together', grouping="A.3"))"""
        params = parse_text(SCRIPT)

        groupings = group_params(params)
        assert "these" == groupings["A"]["1"], str(groupings)
        assert "belong" == groupings["A"]["2"], str(groupings)
        assert "together" == groupings["A"]["3"], str(groupings)

    def testGroupingWithMain(self):
        SCRIPT = """if True:
            from omero.scripts import *
            c = client('testGrouping',
                Bool('checkbox', grouping="A"),
                Long('these', grouping="A.1"),
                Long('belong', grouping="A.2"),
                Long('together', grouping="A.3"))"""
        params = parse_text(SCRIPT)

        groupings = group_params(params)
        try:
            assert "checkbox" == groupings["A"][""], str(groupings)
            assert "these" == groupings["A"]["1"], str(groupings)
            assert "belong" == groupings["A"]["2"], str(groupings)
            assert "together" == groupings["A"]["3"], str(groupings)
        except KeyError:
            assert False, str(groupings)

    def testGroupingWithMainExtraDot(self):
        SCRIPT = """if True:
            from omero.scripts import *
            c = client('testGrouping',
                Bool('checkbox', grouping="A."),
                Long('these', grouping="A.1"),
                Long('belong', grouping="A.2"),
                Long('together', grouping="A.3"))"""
        params = parse_text(SCRIPT)

        groupings = group_params(params)
        assert "checkbox" == groupings["A"][""], str(groupings)
        assert "these" == groupings["A"]["1"], str(groupings)
        assert "belong" == groupings["A"]["2"], str(groupings)
        assert "together" == groupings["A"]["3"], str(groupings)

    def testParseAllOfficialScripts(self):
        for script in SCRIPTS.walk("*.py"):
            try:
                parse_file(str(script))
            except Exception, e:
                assert False, "%s\n%s" % (script, e)

    def testValidateRoiMovieCall(self):
        script = SCRIPTS / "figure_scripts" / "Movie_ROI_Figure.py"
        params = parse_file(str(script))
        inputs = {
            "Merged_Colours": wrap(['Red', 'Green']),
            "Image_Labels": wrap("Datasets"),
            "Data_Type": wrap("Image"),
            "IDs": wrap([long(1)])
        }
        errors = validate_inputs(params, inputs)
        assert "" == errors, errors

    def test2340(self):
        SCRIPT = """if True:
            from omero.scripts import *
            c = client('2340',
                Long('l', default=10))"""
        params = parse_text(SCRIPT)
        l = params.inputs["l"]
        assert None != l.prototype, str(l)

        # Copied from testUploadOfficialScript
        scriptLines = [
            "import omero",
            "from omero.rtypes import rstring, rlong",
            "import omero.scripts as scripts",
            "if __name__ == '__main__':",
            "    client = scripts.client('HelloWorld.py',"
            " 'Hello World example script',",
            "    scripts.Long('longParam', True, description='theDesc',"
            " min=rlong(1), max=rlong(10), values=[rlong(5)]) )",
            "    client.setOutput('returnMessage', rstring('Script ran"
            " OK!'))"]
        params = parse_text("\n".join(scriptLines))
        l = params.inputs["longParam"]
        assert None != l.prototype, str(l)

    def parse_list(self, SCRIPT):
        params = parse_text(SCRIPT)
        l = params.inputs["l"]
        assert l.useDefault, str(l)
        assert ["a"] == unwrap(l.prototype)

    def test2405_String(self):
        SCRIPT = """
if True:
    from omero.scripts import *
    from omero.rtypes import *
    cOptions = wrap(["a","b","c"])

    c = client('2405', List("l", default=rstring("a"),
               values=cOptions).ofType(rstring("")))"""
        self.parse_list(SCRIPT)

    def test2405_List(self):
        SCRIPT = """
if True:
    from omero.scripts import *
    from omero.rtypes import *
    cOptions = wrap(["a","b","c"])

    c = client('2405', List("l", default=["a"],
               values=cOptions).ofType(rstring("")))"""
        self.parse_list(SCRIPT)

    def test2405_RList(self):
        SCRIPT = """
if True:
    from omero.scripts import *
    from omero.rtypes import *
    cOptions = wrap(["a","b","c"])

    c = client('2405', List("l", default=wrap(["a"]),
               values=cOptions).ofType(rstring("")))"""
        self.parse_list(SCRIPT)

    def test2405BadMixOfOfType(self):
        SCRIPT = """
if True:
    from omero.scripts import *
    from omero.rtypes import *

    c = client('2405', List("Channel_Colours", grouping="7",
               description="List of Colours for channels.",
               default="White").ofType(rint(0))) """

        pytest.raises(ValueError, parse_text, SCRIPT)

    def test2405BadMixOfValues(self):
        SCRIPT = """
if True:
    from omero.scripts import *
    from omero.rtypes import *
    cOptions = wrap([1,2,3])

    c = client('2405', List("Channel_Colours", grouping="7",
               description="List of Colours for channels.", default="White",
               values=cOptions))"""

        pytest.raises(ValueError, parse_text, SCRIPT)

    def testParseInputsSimple(self):
        params = JobParams()
        params.inputs = {"a": Long("a", optional=False)}
        rv = parse_inputs(["a=1"], params)
        assert isinstance(rv["a"], omero.RLong)
        assert 1 == rv["a"].val
        try:
            parse_inputs(["b=1"], params)
        except MissingInputs, mi:
            assert ["a"] == mi.keys

    def testParseInputsLongList(self):
        params = JobParams()
        params.inputs = {"a": List("a", optional=False).ofType(rlong(0))}
        # List of one
        rv = parse_inputs(["a=1"], params)
        assert isinstance(rv["a"], omero.RList)
        assert 1 == rv["a"].val[0].val
        # List of two
        rv = parse_inputs(["a=1,2"], params)
        assert isinstance(rv["a"], omero.RList)
        assert 1 == rv["a"].val[0].val
        assert 2 == rv["a"].val[1].val

    def testParseInputsStringListIsDefault(self):
        params = JobParams()
        params.inputs = {"a": List("a", optional=False)}
        rv = parse_inputs(["a=1"], params)
        assert isinstance(rv["a"], omero.RList)
        assert "1" == rv["a"].val[0].val

    def testParseBool(self):
        """ see ticket:7003 """
        params = JobParams()
        params.inputs = {"a": Bool("a", default=True)}

        rv = parse_inputs(["a=False"], params)
        assert False == rv["a"].val

        rv = parse_inputs(["a=false"], params)
        assert False == rv["a"].val

        rv = parse_inputs(["a=0"], params)
        assert False == rv["a"].val

        rv = parse_inputs(["a="], params)
        assert False == rv["a"].val

        rv = parse_inputs(["a=True"], params)
        assert True == rv["a"].val

        rv = parse_inputs(["a=true"], params)
        assert True == rv["a"].val

        rv = parse_inputs(["a=1"], params)
        assert True == rv["a"].val

        rv = parse_inputs(["a=xxxanytextxxx"], params)
        assert True == rv["a"].val

    def testParseIntList(self):
        """
        see ticket:7003
        """
        params = JobParams()
        a = List("Channels").ofType(rint(0))
        params.inputs = {"a": a}

        rv = parse_inputs(["a=1,2"], params)["a"].val
        for x in rv:
            assert isinstance(x, omero.RInt)
        assert 1 == rv[0].val
        assert 2 == rv[1].val
