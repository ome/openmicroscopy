#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the scripts plugin

   Copyright 2010-2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import pytest
from omero.cli import CLI, NonZeroReturnCode
from omero.config import ConfigXml
from omero.plugins.prefs import PrefsControl, HELP
from omero.util.temp_files import create_path


@pytest.fixture
def configxml(monkeypatch):
    class MockConfigXml(object):
        def __init__(self, path, **kwargs):
            pass

        def as_map(self):
            return {}

        def close(self):
            pass
    monkeypatch.setattr("omero.config.ConfigXml", MockConfigXml)


class TestPrefs(object):

    def setup_method(self, method):
        self.cli = CLI()
        self.cli.register("config", PrefsControl, HELP)
        self.p = create_path()
        self.args = ["config", "--source", "%s" % self.p]

    def config(self):
        return ConfigXml(filename=str(self.p))

    def assertStdoutStderr(self, capsys, out='', err=''):
        o, e = capsys.readouterr()
        assert (o.strip() == out and
                e.strip() == err)

    def invoke(self, s):
        self.cli.invoke(self.args + s.split(), strict=True)

    def testHelp(self):
        self.invoke("-h")
        assert 0 == self.cli.rv

    @pytest.mark.parametrize('subcommand', PrefsControl().get_subcommands())
    def testSubcommandHelp(self, subcommand):
        self.invoke("%s -h" % subcommand)
        assert 0 == self.cli.rv

    def testAll(self, capsys):
        config = self.config()
        config.default("test")
        config.close()
        self.invoke("all")
        self.assertStdoutStderr(capsys, out="test\ndefault")

    def testDefaultInitial(self, capsys):
        self.invoke("def")
        self.assertStdoutStderr(capsys, out="default")

    def testDefaultEnvironment(self, capsys, monkeypatch):
        monkeypatch.setenv("OMERO_CONFIG", "testDefaultEnvironment")
        self.invoke("def")
        self.assertStdoutStderr(capsys, out="testDefaultEnvironment")

    def testDefaultSet(self, capsys):
        self.invoke("def x")
        self.assertStdoutStderr(capsys, out="x")
        self.invoke("def")
        self.assertStdoutStderr(capsys, out="x")

    def testGetSet(self, capsys):
        self.invoke("get X")
        self.assertStdoutStderr(capsys)
        self.invoke("set A B")
        self.assertStdoutStderr(capsys)
        self.invoke("get A")
        self.assertStdoutStderr(capsys, out='B')
        self.invoke("get")
        self.assertStdoutStderr(capsys, out='A=B')
        self.invoke("set A")
        self.assertStdoutStderr(capsys)
        self.invoke("keys")
        self.assertStdoutStderr(capsys)

    def testGetHidePassword(self, capsys):
        self.invoke("set omero.X.pass shortpass")
        self.cli.invoke(self.args + ["set", "omero.Y.pass", ""], strict=True)
        self.invoke("set omero.Y.password long_password")
        self.invoke("set omero.Z val")
        self.invoke("get")
        self.assertStdoutStderr(capsys, out=(
            'omero.X.pass=shortpass\n'
            'omero.Y.pass=\n'
            'omero.Y.password=long_password\n'
            'omero.Z=val'))
        self.invoke("get --hide-password")
        self.assertStdoutStderr(capsys, out=(
            'omero.X.pass=********\n'
            'omero.Y.pass=\n'
            'omero.Y.password=********\n'
            'omero.Z=val'))

    @pytest.mark.parametrize('argument', ['A=B', 'A= B'])
    def testSetFails(self, capsys, argument):
        self.invoke("set %s" % argument)
        self.assertStdoutStderr(
            capsys, err="\"=\" in key name. Did you mean \"...set A B\"?")

    def testKeys(self, capsys):
        self.invoke("keys")
        self.assertStdoutStderr(capsys)
        self.invoke("set A B")
        self.assertStdoutStderr(capsys)
        self.invoke("keys")
        self.assertStdoutStderr(capsys, out="A")

    def testVersion(self, capsys):
        self.invoke("version")
        self.assertStdoutStderr(capsys, out=ConfigXml.VERSION)

    def testPath(self, capsys):
        self.invoke("path")
        self.assertStdoutStderr(capsys, out=self.p)

    def testLoad(self, capsys):
        to_load = create_path()
        to_load.write_text("A=B")
        self.invoke("load %s" % to_load)
        self.assertStdoutStderr(capsys)
        self.invoke("get")
        self.assertStdoutStderr(capsys, out="A=B")

        # Same property/value pairs should pass
        self.invoke("load %s" % to_load)

        to_load.write_text("A=C")
        with pytest.raises(NonZeroReturnCode):
            # Different property/value pair should fail
            self.invoke("load %s" % to_load)
        self.assertStdoutStderr(
            capsys, err="Duplicate property: A ('B' => 'C')")

        # Quiet load
        self.invoke("load -q %s" % to_load)
        self.assertStdoutStderr(capsys)
        self.invoke("get")
        self.assertStdoutStderr(capsys, out="A=C")

    def testLoadDoesNotExist(self):
        # ticket:7273
        pytest.raises(NonZeroReturnCode, self.invoke,
                      "load THIS_FILE_SHOULD_NOT_EXIST")

    def testLoadMultiLine(self, capsys):
        to_load = create_path()
        to_load.write_text("A=B\\\nC")
        self.invoke("load %s" % to_load)
        self.invoke("get")
        self.assertStdoutStderr(capsys, out="A=BC")

    def testSetFromFile(self, capsys):
        to_load = create_path()
        to_load.write_text("Test")
        self.invoke("set -f %s A" % to_load)
        self.invoke("get")
        self.assertStdoutStderr(capsys, out="A=Test")

    def testDrop(self, capsys):
        self.invoke("def x")
        self.assertStdoutStderr(capsys, out="x")
        self.invoke("def")
        self.assertStdoutStderr(capsys, out="x")
        self.invoke("all")
        self.assertStdoutStderr(capsys, out="x\ndefault")
        self.invoke("def y")
        self.assertStdoutStderr(capsys, out="y")
        self.invoke("all")
        self.assertStdoutStderr(capsys, out="y\nx\ndefault")
        self.invoke("drop x")
        self.assertStdoutStderr(capsys)
        self.invoke("all")
        self.assertStdoutStderr(capsys, 'y\ndefault')

    def testDropFails(self, capsys):
        self.invoke("drop x")
        self.assertStdoutStderr(capsys, err="Unknown configuration: x")

    def testEdit(self):
        """
        Testing edit is a bit more complex since it wants to
        start another process. Rather than using invoke, we
        manage things ourselves here.
        """
        def fake_edit_path(tmp_file, tmp_text):
            pass
        args = self.cli.parser.parse_args("config edit".split())
        control = self.cli.controls["config"]
        config = self.config()
        try:
            control.edit(args, config, fake_edit_path)
        finally:
            config.close()

    def testNewEnvironment(self, capsys, monkeypatch):
        config = self.config()
        config.default("default")
        config.close()
        monkeypatch.setenv("OMERO_CONFIG", "testNewEnvironment")
        self.invoke("set A B")
        self.assertStdoutStderr(capsys)
        self.invoke("get")
        self.assertStdoutStderr(capsys, out="A=B")

    @pytest.mark.parametrize(
        ('initval', 'newval'),
        [('1', '2'), ('\"1\"', '\"2\"'), ('test', 'test')])
    def testAppendFails(self, initval, newval):
        self.invoke("set A %s" % initval)
        with pytest.raises(NonZeroReturnCode):
            self.invoke("append A %s" % newval)

    def testRemoveUnsetPropertyFails(self):
        with pytest.raises(NonZeroReturnCode):
            self.invoke("remove A x")

    @pytest.mark.parametrize(
        ('initval', 'newval'),
        [('1', '1'), ('[\"1\"]', '1'), ('[1]', '\"1\"')])
    def testRemoveFails(self, initval, newval):
        self.invoke("set A %s" % initval)
        with pytest.raises(NonZeroReturnCode):
            self.invoke("remove A %s" % newval)

    def testAppendRemove(self, capsys):
        self.invoke("append A 1")
        self.invoke("get A")
        self.assertStdoutStderr(capsys, out='[1]')
        self.invoke("append A \"y\"")
        self.invoke("get A")
        self.assertStdoutStderr(capsys, out='[1, "y"]')
        self.invoke("remove A \"y\"")
        self.invoke("get A")
        self.assertStdoutStderr(capsys, out='[1]')
        self.invoke("remove A 1")
        self.invoke("get A")
        self.assertStdoutStderr(capsys, out='[]')

    def testRemoveIdenticalValues(self, capsys):
        self.invoke("set A [1,1]")
        self.invoke("remove A 1")
        self.invoke("get A")
        self.assertStdoutStderr(capsys, out='[1]')
        self.invoke("remove A 1")
        self.invoke("get A")
        self.assertStdoutStderr(capsys, out='[]')

    @pytest.mark.usefixtures('configxml')
    def testAppendWithDefault(self, monkeypatch, capsys):
        import json
        monkeypatch.setattr("omeroweb.settings.CUSTOM_SETTINGS_MAPPINGS", {
            "omero.web.test": ["TEST", "[1,2,3]", json.loads],
            "omero.web.notalist": ["NOTALIST", "abc", str],
        })
        self.invoke("append omero.web.test 4")
        self.invoke("get omero.web.test")
        self.assertStdoutStderr(capsys, out='[1, 2, 3, 4]')
        self.invoke("append omero.web.unknown 1")
        self.invoke("get omero.web.unknown")
        self.assertStdoutStderr(capsys, out='[1]')
        with pytest.raises(NonZeroReturnCode):
            self.invoke("append omero.web.notalist 1")

    @pytest.mark.usefixtures('configxml')
    def testRemoveWithDefault(self, monkeypatch, capsys):
        import json
        monkeypatch.setattr("omeroweb.settings.CUSTOM_SETTINGS_MAPPINGS", {
            "omero.web.test": ["TEST", "[1,2,3]", json.loads],
        })
        self.invoke("remove omero.web.test 2")
        self.invoke("get omero.web.test")
        self.assertStdoutStderr(capsys, out='[1, 3]')
        self.invoke("remove omero.web.test 1")
        self.invoke("remove omero.web.test 3")
        self.invoke("get omero.web.test")
        self.assertStdoutStderr(capsys, out='[]')

    @pytest.mark.parametrize("data", (
        ({}, ""),
        ({"a": "b"}, "a=b"),
        ({"a": "b", "a": "d"}, "a=d"),
        ({"a": "b", "c": "d"}, "a=b\nc=d"),
        ({"c": "d", "a": "b"}, "a=b\nc=d"),
    ))
    @pytest.mark.usefixtures('configxml')
    def testList(self, data, monkeypatch, capsys):
        for k, v in data[0].items():
            self.invoke("set %s %s" % (k, v))
        self.invoke("list")
        self.assertStdoutStderr(capsys, out=data[1])

    @pytest.mark.parametrize("data", (
        ("omero.a=b\nomero.c=d\n##ignore=me\n",
         "omero.a=b\nomero.c=d",
         "a (1)\n\t\nc (1)"),
        ("omero.whitelist=\\\nome.foo,\\\nome.bar\n### END",
         "omero.whitelist=ome.foo,ome.bar",
         "whitelist (1)"),
        ("omero.whitelist=\\\nome.foo,\\\nome.bar\n",
         "omero.whitelist=ome.foo,ome.bar",
         "whitelist (1)"),
        ("omero.whitelist=\\\nome.foo,\\\nome.bar",
         "omero.whitelist=ome.foo,ome.bar",
         "whitelist (1)"),
        ("omero.user_mapping=\\\na=b,c=d",
         "omero.user_mapping=a=b,c=d",
         "user_mapping (1)"),
        ("omero.whitelist=ome.foo\nIce.c=d\n",
         "Ice.c=d\nomero.whitelist=ome.foo",
         "whitelist (1)"),
        ("omero.a=b\nomero.c=d\nomero.e=f\n##ignore=me\n",
         "omero.a=b\nomero.c=d\nomero.e=f",
         "a (1)\n\t\nc (1)\n\t\ne (1)"),
        ("omero.a=b\nomero.c=d\nomero.e=f\n##ignore=me\n",
         "omero.a=b\nomero.c=d\nomero.e=f",
         "a (1)\n\t\nc (1)\n\t\ne (1)"),
    ))
    def testFileParsing(self, tmpdir, capsys, data):
        input, defaults, keys = data
        cfg = tmpdir.join("test.cfg")
        cfg.write(input)
        self.invoke("parse --file=%s --no-web" % cfg)
        self.assertStdoutStderr(capsys, out=defaults)
        self.invoke("parse --file=%s --defaults --no-web" % cfg)
        self.assertStdoutStderr(capsys, out=defaults)
        self.invoke("parse --file=%s --keys --no-web" % cfg)
        self.assertStdoutStderr(capsys, out=keys)
