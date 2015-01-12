#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
# All rights reserved.
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

import pytest
from path import path
from omero.cli import CLI
from omero.plugins.web import WebControl
from omeroweb import settings

subcommands = [
    "start", "stop", "restart", "status", "iis", "config", "syncmedia",
    "clearsessions"]


class TestWeb(object):

    def setup_method(self, method):
        self.cli = CLI()
        self.cli.register("web", WebControl, "TEST")
        self.args = ["web"]

    def add_templates_dir(self):
        dist_dir = path(__file__) / ".." / ".." / ".." / ".." / ".." / ".." /\
            ".." / "dist"  # FIXME: should not be hard-coded
        dist_dir = dist_dir.abspath()
        templates_dir = dist_dir / "etc" / "templates"
        self.args += ["--templates_dir", templates_dir]

    def clean_generated_file(self, txt):
        lines = [line.strip() for line in txt.split('\n')]
        lines = [line for line in lines if line and not line.startswith('#')]
        return lines

    def testHelp(self):
        self.args += ["-h"]
        self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize('subcommand', subcommands)
    def testSubcommandHelp(self, subcommand):
        self.args += [subcommand, "-h"]
        self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize('system', [True, False])
    @pytest.mark.parametrize('http', [False, 8081])
    @pytest.mark.parametrize('static_prefix', [None, '/test'])
    def testNginxConfig(self, system, http, static_prefix, capsys,
                        monkeypatch):

        if static_prefix:
            monkeypatch.setattr(settings, 'FORCE_SCRIPT_NAME', static_prefix,
                                raising=False)
            monkeypatch.setattr(settings, 'STATIC_URL',
                                static_prefix + '/static/', raising=False)
        self.args += ["config", "nginx"]
        if system:
            self.args += ["--system"]
        if http:
            self.args += ["--http", str(http)]
        self.add_templates_dir()
        self.cli.invoke(self.args, strict=True)
        o, e = capsys.readouterr()
        print o

        assert "%(" not in o
        if system:
            assert "http {" not in o
        else:
            assert "http {" in o
        if static_prefix:
            assert " location %s/static {" % static_prefix in o
        else:
            assert " location / {" in o
        if http:
            assert "listen       %s;" % http in o

    def testApacheConfig(self, capsys):
        self.args += ["config", "apache"]
        self.add_templates_dir()
        self.cli.invoke(self.args, strict=True)
        o, e = capsys.readouterr()

        assert "%(" not in o

    @pytest.mark.parametrize('prefix', [None, '/test'])
    def testApacheFcgiConfig(self, prefix, capsys, monkeypatch):

        if prefix:
            monkeypatch.setattr(settings, 'FORCE_SCRIPT_NAME', prefix,
                                raising=False)
            monkeypatch.setattr(settings, 'STATIC_URL',
                                prefix + '-static/', raising=False)

        self.args += ["config", "apache-fcgi"]
        self.add_templates_dir()
        self.cli.invoke(self.args, strict=True)
        o, e = capsys.readouterr()

        assert "%(" not in o
        lines = self.clean_generated_file(o)
        print '\n'.join(lines)

        if prefix:
            assert lines[-4].startswith('Alias /test-static')
            assert lines[-4].endswith('lib/python/omeroweb/static')
            assert lines[-3] == 'RewriteRule ^/test/?(.*|$) /test.fcgi/$1 [PT]'
            assert lines[-2] == 'SetEnvIf Request_URI . proxy-fcgi-pathinfo=1'
            assert lines[-1] == 'ProxyPass /test.fcgi/ fcgi://0.0.0.0:4080/'
        else:
            assert lines[-5].startswith('Alias /static')
            assert lines[-5].endswith('lib/python/omeroweb/static')
            assert lines[-4] == \
                'RewriteCond %{REQUEST_URI} !^(/static|/\.fcgi)'
            assert lines[-3] == 'RewriteRule ^/?(.*|$) /.fcgi/$1 [PT]'
            assert lines[-2] == 'SetEnvIf Request_URI . proxy-fcgi-pathinfo=1'
            assert lines[-1] == 'ProxyPass /.fcgi/ fcgi://0.0.0.0:4080/'
