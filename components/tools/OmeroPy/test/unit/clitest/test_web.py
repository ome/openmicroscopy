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

    def add_prefix(self, prefix, monkeypatch):
        if prefix:
            monkeypatch.setattr(settings, 'FORCE_SCRIPT_NAME', prefix,
                                raising=False)
            static_prefix = prefix + '-static/'
            monkeypatch.setattr(settings, 'STATIC_URL', static_prefix,
                                raising=False)
        else:
            static_prefix = settings.STATIC_URL
        return static_prefix

    def clean_generated_file(self, txt):
        assert "%(" not in txt   # Make sure all markers have been replaced
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
    @pytest.mark.parametrize('prefix', [None, '/test'])
    def testNginxConfig(self, system, http, prefix, capsys, monkeypatch):

        static_prefix = self.add_prefix(prefix, monkeypatch)
        self.args += ["config", "nginx"]
        if system:
            self.args += ["--system"]
        if http:
            self.args += ["--http", str(http)]
        self.add_templates_dir()
        self.cli.invoke(self.args, strict=True)
        o, e = capsys.readouterr()
        lines = self.clean_generated_file(o)

        if system:
            assert lines[0] == "server {"
            assert lines[1] == "listen       %s;" % (http or 8080)
            assert lines[3] == "location %s {" % static_prefix[:-1]
            assert lines[6] == "location %s {" % (prefix or "/")
            assert lines[8] == "error_page 503 %s/maintenance.html;" % (
                prefix or "")
            assert lines[-4] == "location %s/maintenance.html {" % (
                prefix or "")
        else:
            assert lines[16] == "server {"
            assert lines[17] == "listen       %s;" % (http or 8080)
            assert lines[21] == "location %s {" % static_prefix[:-1]
            assert lines[24] == "location %s {" % (prefix or "/")
            assert lines[26] == "error_page 503 %s/maintenance.html;" % (
                prefix or "")
            assert lines[-5] == "location %s/maintenance.html {" % (
                prefix or "")

    @pytest.mark.parametrize('prefix', [None, '/test'])
    def testApacheConfig(self, prefix, capsys, monkeypatch):

        static_prefix = self.add_prefix(prefix, monkeypatch)
        self.args += ["config", "apache"]
        self.add_templates_dir()
        self.cli.invoke(self.args, strict=True)
        o, e = capsys.readouterr()

        lines = self.clean_generated_file(o)

        if prefix:
            assert lines[0] == 'RewriteEngine on'
            assert lines[1] == 'RewriteRule ^/?$ %s/ [R]' % prefix
            assert lines[2].startswith('FastCGIExternalServer ')
            assert lines[2].endswith(
                'var/omero.fcgi" -host 0.0.0.0:4080 -idle-timeout 60')
            assert lines[-2].startswith('Alias %s ' % static_prefix[:-1])
            assert lines[-2].endswith('lib/python/omeroweb/static')
            assert lines[-1].startswith('Alias %s "' % prefix)
            assert lines[-1].endswith('var/omero.fcgi/"')
        else:
            assert lines[0].startswith('FastCGIExternalServer ')
            assert lines[0].endswith(
                'var/omero.fcgi" -host 0.0.0.0:4080 -idle-timeout 60')
            assert lines[-2].startswith('Alias /static ')
            assert lines[-2].endswith('lib/python/omeroweb/static')
            assert lines[-1].startswith('Alias / "')
            assert lines[-1].endswith('var/omero.fcgi/"')

    @pytest.mark.parametrize('prefix', [None, '/test'])
    def testApacheFcgiConfig(self, prefix, capsys, monkeypatch):

        static_prefix = self.add_prefix(prefix, monkeypatch)
        self.args += ["config", "apache-fcgi"]
        self.add_templates_dir()
        self.cli.invoke(self.args, strict=True)
        o, e = capsys.readouterr()

        lines = self.clean_generated_file(o)

        if prefix:
            assert lines[-5].startswith('Alias %s' % static_prefix[:-1])
            assert lines[-5].endswith('lib/python/omeroweb/static')
            assert lines[-4] == 'RewriteCond %{REQUEST_URI} ' + \
                '!^(%s|/\\.fcgi)' % static_prefix[:-1]
            assert lines[-3] == \
                'RewriteRule ^%s(/|$)(.*) %s.fcgi/$2 [PT]' % (prefix, prefix)
            assert lines[-2] == 'SetEnvIf Request_URI . proxy-fcgi-pathinfo=1'
            assert lines[-1] == 'ProxyPass %s.fcgi/ fcgi://0.0.0.0:4080/' \
                % prefix
        else:
            assert lines[-5].startswith('Alias /static ')
            assert lines[-5].endswith('lib/python/omeroweb/static')
            assert lines[-4] == \
                'RewriteCond %{REQUEST_URI} !^(/static|/\\.fcgi)'
            assert lines[-3] == 'RewriteRule ^(/|$)(.*) /.fcgi/$2 [PT]'
            assert lines[-2] == 'SetEnvIf Request_URI . proxy-fcgi-pathinfo=1'
            assert lines[-1] == 'ProxyPass /.fcgi/ fcgi://0.0.0.0:4080/'
