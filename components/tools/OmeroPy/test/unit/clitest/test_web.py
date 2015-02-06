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


class TestWeb(object):

    def setup_method(self, method):
        self.cli = CLI()
        self.cli.register("web", WebControl, "TEST")
        self.args = ["web"]

    def set_templates_dir(self, monkeypatch):

        dist_dir = path(__file__) / ".." / ".." / ".." / ".." / ".." / ".." /\
            ".." / "dist"  # FIXME: should not be hard-coded
        dist_dir = dist_dir.abspath()
        monkeypatch.setattr(WebControl, '_get_templates_dir',
                            lambda x: dist_dir / "etc" / "templates")

    def add_prefix(self, prefix, monkeypatch):

        def _get_default_value(x):
            return settings.CUSTOM_SETTINGS_MAPPINGS[x][1]
        if prefix:
            static_prefix = prefix + '-static/'
        else:
            prefix = _get_default_value('omero.web.prefix')
            static_prefix = _get_default_value('omero.web.static_url')
        monkeypatch.setattr(settings, 'STATIC_URL', static_prefix,
                            raising=False)
        monkeypatch.setattr(settings, 'FORCE_SCRIPT_NAME', prefix,
                            raising=False)
        return static_prefix

    def clean_generated_file(self, txt):
        assert "%(" not in txt   # Make sure all markers have been replaced
        lines = [line.strip() for line in txt.split('\n')]
        lines = [line for line in lines if line and not line.startswith('#')]
        return lines

    def testHelp(self):
        self.args += ["-h"]
        self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize('subcommand', WebControl().get_subcommands())
    def testSubcommandHelp(self, subcommand):
        self.args += [subcommand, "-h"]
        self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize('max_body_size', [None, '0', '1m'])
    @pytest.mark.parametrize('server_type', [
        "nginx", "nginx-development", "nginx --system"])
    @pytest.mark.parametrize('http', [False, 8081])
    @pytest.mark.parametrize('prefix', [None, '/test'])
    def testNginxConfig(self, server_type, http, prefix, capsys, monkeypatch,
                        max_body_size):

        static_prefix = self.add_prefix(prefix, monkeypatch)
        self.args += ["config"]
        self.args += server_type.split()
        if http:
            self.args += ["--http", str(http)]
        if max_body_size:
            self.args += ["--max-body-size", max_body_size]
        self.set_templates_dir(monkeypatch)
        self.cli.invoke(self.args, strict=True)
        o, e = capsys.readouterr()
        lines = self.clean_generated_file(o)

        if server_type.split()[0] == "nginx":
            assert lines[0] == "server {"
            assert lines[1] == "listen       %s;" % (http or 80)
            assert lines[4] == "client_max_body_size %s;" % (
                max_body_size or '0')
            assert lines[9] == "location %s {" % static_prefix[:-1]
            assert lines[12] == "location %s {" % (prefix or "/")
        else:
            assert lines[13] == "server {"
            assert lines[14] == "listen       %s;" % (http or 8080)
            assert lines[19] == "client_max_body_size %s;" % (
                max_body_size or '0')
            assert lines[24] == "location %s {" % static_prefix[:-1]
            assert lines[27] == "location %s {" % (prefix or "/")

    @pytest.mark.parametrize('prefix', [None, '/test'])
    def testApacheConfig(self, prefix, capsys, monkeypatch):

        static_prefix = self.add_prefix(prefix, monkeypatch)
        self.args += ["config", "apache"]
        self.set_templates_dir(monkeypatch)
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
        self.set_templates_dir(monkeypatch)
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
