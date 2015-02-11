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
from omero.util.temp_files import create_path

from mocks import MockCLI

omeroDir = path(os.getcwd()) / "build"


class TestAdmin(object):

    def setup_method(self, method):
        # Non-temp directories
        build_dir = path() / "build"
        top_dir = path() / ".." / ".." / ".."
        etc_dir = top_dir / "etc"

        # Necessary fiels
        prefs_file = build_dir / "prefs.class"
        internal_cfg = etc_dir / "internal.cfg"
        master_cfg = etc_dir / "master.cfg"

        # Temp directories
        tmp_dir = create_path(folder=True)
        tmp_etc_dir = tmp_dir / "etc"
        tmp_grid_dir = tmp_etc_dir / "grid"
        tmp_lib_dir = tmp_dir / "lib"
        tmp_var_dir = tmp_dir / "var"

        # Setup tmp dir
        [x.makedirs() for x in (tmp_grid_dir, tmp_lib_dir, tmp_var_dir)]
        prefs_file.copy(tmp_lib_dir)
        master_cfg.copy(tmp_etc_dir)
        internal_cfg.copy(tmp_etc_dir)

        # Other setup
        self.cli = MockCLI()
        self.cli.dir = tmp_dir
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
        # # Non-temp directories
        ctxdir = path() / ".." / ".." / ".." / "dist"
        etc_dir = ctxdir / "etc"

        # List configuration files to backup
        self.cfg_files = {}
        for f in ['internal.cfg', 'master.cfg', 'ice.config']:
            self.cfg_files[f] = etc_dir / f
        for f in ['windefault.xml', 'default.xml', 'config.xml']:
            self.cfg_files[f] = etc_dir / 'grid' / f

        # Create temp files for backup
        tmp_dir = create_path(folder=True)
        self.tmp_cfg_files = {}
        for key in self.cfg_files.keys():
            if self.cfg_files[key].exists():
                self.tmp_cfg_files[key] = tmp_dir / key
                self.cfg_files[key].copy(self.tmp_cfg_files[key])
            else:
                self.tmp_cfg_files[key] = None

        # Other setup
        self.cli = CLI()
        self.cli.dir = ctxdir
        self.cli.register("admin", AdminControl, "TEST")
        self.args = ["admin", "ports"]

    def teardown_method(self, method):
        # Restore backups
        for key in self.cfg_files.keys():
            if self.tmp_cfg_files[key] is not None:
                self.tmp_cfg_files[key].copy(self.cfg_files[key])
            else:
                self.cfg_files[key].remove()

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

    @classmethod
    def setup_class(cls):
        # Other setup
        cls.cli = CLI()
        cls.cli.register("admin", AdminControl, "TEST")
        cls.cli.register("config", PrefsControl, "TEST")
        cls.args = ["admin", "jvmcfg"]

    @pytest.fixture
    def tmp_grid_dir(self, monkeypatch):
        # # Non-temp directories
        ctxdir = path() / ".." / ".." / ".." / "dist"
        grid_dir = ctxdir / "etc" / "grid"
        templates_file = grid_dir / "templates.xml"

        # Create temp files for backup
        self.tmp_grid_dir = create_path(folder=True)
        templates_file.copy(self.tmp_grid_dir)
        config_file = self.tmp_grid_dir / "config.xml"
        open(config_file, 'a').close()

        monkeypatch.setattr(AdminControl, '_get_grid_dir',
                            lambda x: self.tmp_grid_dir)

    def testDefault(self, tmp_grid_dir):

        self.cli.invoke(self.args, strict=True)
        assert os.path.exists(self.tmp_grid_dir / "generated.xml")

    @pytest.mark.parametrize(
        'suffix', ['', '.blitz', '.indexer', '.pixeldata', '.repository'])
    def testInvalidStrategy(self, suffix, tmp_grid_dir):
        source_config = "%s" % (self.tmp_grid_dir / "config.xml")
        key = "omero.jvmcfg.strategy%s" % suffix
        self.cli.invoke(
            ["config", "--source", source_config, "set", key, "bad"],
            strict=True)
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    def testOldTemplates(self, tmp_grid_dir):

        old_templates = path(__file__).dirname() / ".." / "old_templates.xml"
        old_templates.copy(self.tmp_grid_dir / "templates.xml")
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)
