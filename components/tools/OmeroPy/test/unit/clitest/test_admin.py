#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the omero admin control.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import re
import pytest

from path import path

import omero
import omero.clients

from omero.cli import CLI, NonZeroReturnCode
from omero.plugins.admin import AdminControl
from omero.plugins.prefs import PrefsControl

from mocks import MockCLI

omeroDir = path(os.getcwd()) / "build"


@pytest.fixture(autouse=True)
def tmpadmindir(tmpdir):
    etc_dir = tmpdir.mkdir('etc')
    etc_dir.mkdir('grid')
    tmpdir.mkdir('var')
    templates_dir = etc_dir.mkdir('templates').mkdir('grid')

    templates_file = (
        path() / ".." / ".." / ".." / "etc" / "templates" / "grid" /
        "templates.xml")
    templates_file.copy(path(templates_dir) / "templates.xml")
    return path(tmpdir)


class TestAdmin(object):

    @pytest.fixture(autouse=True)
    def setup_method(self, tmpadmindir):
        # Other setup
        self.cli = MockCLI()
        self.cli.dir = tmpadmindir
        self.cli.register("admin", AdminControl, "TEST")
        self.cli.register("config", PrefsControl, "TEST")

    def teardown_method(self, method):
        self.cli.teardown_method(method)

    def invoke(self, string, fails=False):
        try:
            self.cli.invoke(string, strict=True)
            if fails:
                assert False, "Failed to fail"
        except:
            if not fails:
                raise

    def testMain(self):
        try:
            self.invoke("")
        except NonZeroReturnCode:
            # Command-loop not implemented
            pass

    #
    # Async first because simpler
    #

    def XtestStartAsync(self):
        # DISABLED: https://trac.openmicroscopy.org.uk/ome/ticket/10584
        self.cli.addCall(0)
        self.cli.checksIceVersion()
        self.cli.checksStatus(1)  # I.e. not running

        self.invoke("admin startasync")
        self.cli.assertCalled()
        self.cli.assertStderr(
            ['No descriptor given. Using etc/grid/default.xml'])

    def testStopAsyncRunning(self):
        self.cli.checksStatus(0)  # I.e. running
        self.cli.addCall(0)
        self.invoke("admin stopasync")
        self.cli.assertStderr([])
        self.cli.assertStdout([])

    def testStopAsyncNotRunning(self):
        self.cli.checksStatus(1)  # I.e. not running
        self.invoke("admin stopasync", fails=True)
        self.cli.assertStderr(["Server not running"])
        self.cli.assertStdout([])

    def testStop(self):
        self.cli.checksStatus(0)  # I.e. running
        self.cli.addCall(0)
        self.cli.checksStatus(1)  # I.e. not running
        self.invoke("admin stop")
        self.cli.assertStderr([])
        self.cli.assertStdout(['Waiting on shutdown. Use CTRL-C to exit'])

    #
    # STATUS
    #

    def testStatusNodeFails(self):

        # Setup the call to bin/omero admin ice node
        popen = self.cli.createPopen()
        popen.wait().AndReturn(1)

        self.cli.mox.ReplayAll()
        pytest.raises(NonZeroReturnCode, self.invoke, "admin status")

    def testStatusSMFails(self):

        # Setup the call to bin/omero admin ice node
        popen = self.cli.createPopen()
        popen.wait().AndReturn(0)

        # Setup the call to session manager
        control = self.cli.controls["admin"]
        control._intcfg = lambda: ""

        def sm(*args):
            raise Exception("unknown")
        control.session_manager = sm

        self.cli.mox.ReplayAll()
        pytest.raises(NonZeroReturnCode, self.invoke, "admin status")

    def testStatusPasses(self):

        # Setup the call to bin/omero admin ice node
        popen = self.cli.createPopen()
        popen.wait().AndReturn(0)

        # Setup the call to session manager
        control = self.cli.controls["admin"]
        control._intcfg = lambda: ""

        def sm(*args):

            class A(object):
                def create(self, *args):
                    raise omero.WrappedCreateSessionException()
            return A()
        control.session_manager = sm

        self.cli.mox.ReplayAll()
        self.invoke("admin status")
        assert 0 == self.cli.rv


class TestAdminPorts(object):

    def setup_method(self, method):
        # Other setup
        self.cli = CLI()
        self.cli.dir = tmpadmindir
        self.cli.register("admin", AdminControl, "TEST")
        self.args = ["admin", "ports"]

    def create_new_ice_config(self):
        with open(self.cfg_files['ice.config'], 'w') as f:
            f.write('omero.host=localhost')

    def check_cfg(self, prefix='', registry=4061, **kwargs):
        for key in ['master.cfg', 'internal.cfg']:
            s = self.cfg_files[key].text()
            assert 'tcp -h 127.0.0.1 -p %s%s' % (prefix, registry) in s

    def check_config_xml(self, prefix='', webserver=4080, ssl=4064, **kwargs):
        config_text = self.cfg_files["config.xml"].text()
        serverport_property = (
            '<property name="omero.web.application_server.port"'
            ' value="%s%s"') % (prefix, webserver)
        serverlist_property = (
            '<property name="omero.web.server_list"'
            ' value="[[&quot;localhost&quot;, %s%s, &quot;omero&quot;]]"'
            ) % (prefix, ssl)
        assert serverport_property in config_text
        assert serverlist_property in config_text

    def check_ice_config(self, prefix='', webserver=4080, ssl=4064, **kwargs):
        config_text = self.cfg_files["ice.config"].text()
        pattern = re.compile('^omero.port=\d+$', re.MULTILINE)
        matches = pattern.findall(config_text)
        assert matches == ["omero.port=%s%s" % (prefix, ssl)]

    def check_default_xml(self, prefix='', tcp=4063, ssl=4064, **kwargs):
        routerport = (
            '<variable name="ROUTERPORT"    value="%s%s"/>' % (prefix, ssl))
        insecure_routerport = (
            '<variable name="INSECUREROUTER" value="OMERO.Glacier2'
            '/router:tcp -p %s%s -h @omero.host@"/>' % (prefix, tcp))
        client_endpoints = (
            'client-endpoints="ssl -p ${ROUTERPORT}:tcp -p %s%s"'
            % (prefix, tcp))
        for key in ['default.xml', 'windefault.xml']:
            s = self.cfg_files[key].text()
            assert routerport in s
            assert insecure_routerport in s
            assert client_endpoints in s

    @pytest.mark.parametrize('prefix', [None, 1, 2])
    @pytest.mark.parametrize('default', [True, False])
    def testRevert(self, prefix, default):

        if not default:
            self.create_new_ice_config()

        kwargs = {}
        if prefix:
            self.args += ['--prefix', '%s' % prefix]
            kwargs['prefix'] = prefix
        self.args += ['--skipcheck']
        self.cli.invoke(self.args, strict=True)

        # Check configuration file ports have been prefixed
        self.check_ice_config(**kwargs)
        self.check_cfg(**kwargs)
        self.check_config_xml(**kwargs)
        self.check_default_xml(**kwargs)

        # Check revert argument
        self.args += ['--revert']
        self.cli.invoke(self.args, strict=True)

        # Check configuration file ports have been deprefixed
        self.check_ice_config()
        self.check_cfg()
        self.check_config_xml()
        self.check_default_xml()

    @pytest.mark.parametrize('default', [True, False])
    def testFailingRevert(self, default):

        if not default:
            self.create_new_ice_config()

        kwargs = {'prefix': 1}
        self.args += ['--skipcheck']
        self.args += ['--prefix', '%s' % kwargs['prefix']]
        self.cli.invoke(self.args, strict=True)

        # Check configuration file ports
        self.check_ice_config(**kwargs)
        self.check_cfg(**kwargs)
        self.check_config_xml(**kwargs)
        self.check_default_xml(**kwargs)

        # Test revert with a mismatching prefix
        self.args[-1] = "2"
        self.args += ['--revert']
        self.cli.invoke(self.args, strict=True)

        # Check configuration file ports have not been modified
        self.check_ice_config(**kwargs)
        self.check_cfg(**kwargs)
        self.check_config_xml(**kwargs)
        self.check_default_xml(**kwargs)

    @pytest.mark.parametrize('prefix', [None, 1])
    @pytest.mark.parametrize('registry', [None, 111])
    @pytest.mark.parametrize('tcp', [None, 222])
    @pytest.mark.parametrize('ssl', [None, 333])
    @pytest.mark.parametrize('webserver', [None, 444])
    def testExplicitPorts(self, registry, ssl, tcp, webserver, prefix):
        kwargs = {}
        if prefix:
            self.args += ['--prefix', '%s' % prefix]
            kwargs['prefix'] = prefix
        if registry:
            self.args += ['--registry', '%s' % registry]
            kwargs["registry"] = registry
        if tcp:
            self.args += ['--tcp', '%s' % tcp]
            kwargs["tcp"] = tcp
        if ssl:
            self.args += ['--ssl', '%s' % ssl]
            kwargs["ssl"] = ssl
        if webserver:
            self.args += ['--webserver', '%s' % webserver]
            kwargs["webserver"] = webserver
        self.args += ['--skipcheck']
        self.cli.invoke(self.args, strict=True)

        self.check_ice_config(**kwargs)


class TestAdminJvmCfg(object):

    @pytest.fixture(autouse=True)
    def setup_method(self, tmpadmindir):
        self.cli = CLI()
        self.cli.register("admin", AdminControl, "TEST")
        self.cli.register("config", PrefsControl, "TEST")
        self.args = ["admin", "jvmcfg"]
        self.cli.dir = path(tmpadmindir)

    def testTemplatesGeneration(self):
        assert not os.path.exists(
            path(self.cli.dir) / "etc" / "grid" / "templates.xml")
        self.cli.invoke(self.args, strict=True)
        assert os.path.exists(
            path(self.cli.dir) / "etc" / "grid" / "templates.xml")

    @pytest.mark.parametrize(
        'suffix', ['', '.blitz', '.indexer', '.pixeldata', '.repository'])
    def testInvalidStrategy(self, suffix, tmpdir):

        key = "omero.jvmcfg.strategy%s" % suffix
        self.cli.invoke(["config", "set", key, "bad"], strict=True)
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    def testOldTemplates(self):
        old_templates = path(__file__).dirname() / ".." / "old_templates.xml"
        old_templates.copy(
            path(self.cli.dir) / "etc" / "templates" / "grid" /
            "templates.xml")
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)
