#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

"""
Test server config in decorator.
"""

import json

import library as lib

import omero
import omero.clients

from django.test import RequestFactory
from django.contrib.sessions.middleware import SessionMiddleware


from omeroweb.webclient import webclient_gateway  # NOQA
from omero.gateway import BlitzGateway
from omeroweb.decorators import login_required

import pytest
from omero.gateway.utils import propertiesToDict


def default_view(request):
    pass


# helper from http://codereview.stackexchange.com/questions/
# 21033/flatten-dictionary-in-python-functional-style
def flattenProperties(d):
    """
    Convert nested dictionary to flat map,
    """

    def items():
        for key, value in d.items():
            if isinstance(value, dict):
                for subkey, subvalue in flattenProperties(value).items():
                    yield key + "." + subkey, subvalue
            else:
                yield key, value
    return dict(items())


def test_flattenProperties():
    d = {
        'omero.prefix.str.1': 'mystring',
        'omero.prefix.str.2': '1',
        'omero.prefix.int.1': 1
    }
    dictprop = propertiesToDict(d, prefix='omero.prefix.')
    flatprop = flattenProperties({'omero': {'prefix': dictprop}})
    assert set(d.keys()) - set(flatprop.keys()) == set()


class TestConfig(lib.ITest):

    def setup_method(self, method):
        # prepare session
        self.r = RequestFactory().get('/rand')
        middleware = SessionMiddleware()
        middleware.process_request(self.r)
        self.r.session.save()
        self.rs = self.root.sf.getConfigService()
        self.conn = BlitzGateway(client_obj=self.new_client())

    def teardown_method(self, method):
        self.conn.seppuku()
        self.r.session.flush()

    def testDefaultConfig(self):
        """ Test loading default config """
        deprecated = [
            'omero.client.ui.menu.dropdown.everyone',
            'omero.client.ui.menu.dropdown.leaders',
            'omero.client.ui.menu.dropdown.colleagues'
        ]
        default = self.rs.getClientConfigDefaults()
        login_required(default_view).load_server_settings(self.conn, self.r)
        s = {"omero": {"client": self.r.session.get('server_settings', {})}}
        ss = self.r.session['server_settings']
        # assert if alias gives the same value as deprecated
        # rather then ${property}
        for d in deprecated:
            ds = d.split(".")
            assert ss['ui']['menu']['dropdown'][ds[-1]]['label'] == default[d]
            # workaround for alias as getClientConfigDefaults returns
            # ${property} rather then value
            assert ss['ui']['menu']['dropdown'][ds[-1]]['label'] == \
                self.conn.getConfigService().getConfigValue(
                    default['%s.label' % d][2:-1])

        # compare keys in default and config loaded by decorator
        a = filter(lambda x: x not in (
            set(default.keys()) - set(deprecated)),
            set(flattenProperties(s).keys()))
        assert a == ['omero.client.email']

    def testDefaultConfigConversion(self):
        default = self.rs.getClientConfigDefaults()

        # bool
        key1 = 'omero.client.ui.tree.orphans.enabled'
        self.rs.setConfigValue(key1, default[key1])

        key11 = 'omero.client.ui.tree.orphans.name'
        self.rs.setConfigValue(key11, default[key11])

        # digit
        key2 = 'omero.client.viewer.roi_limit'
        self.rs.setConfigValue(key2, default[key2])

        login_required(default_view).load_server_settings(self.conn, self.r)
        ss = self.r.session['server_settings']

        assert isinstance(ss['ui']['tree']['orphans']['enabled'], bool)
        assert ss['ui']['tree']['orphans']['enabled'] == bool(default[key1])

        assert isinstance(ss['ui']['tree']['orphans']['name'], str)
        assert ss['ui']['tree']['orphans']['name'] == default[key11]

        assert isinstance(ss['viewer']['roi_limit'], int)
        assert ss['viewer']['roi_limit'] == json.loads(default[key2])

    @pytest.mark.parametrize("prop", ["colleagues", "leaders", "everyone",
                                      "colleagues.label", "leaders.label",
                                      "everyone.label"])
    @pytest.mark.parametrize("label", ["foo"])
    def testUpgradeDropdownMenuConfig(self, prop, label):
        """ Test if alias loads deprecated property value """
        d = self.rs.getClientConfigDefaults()
        key = "omero.client.ui.menu.dropdown.%s" % prop
        try:
            self.rs.setConfigValue(key, label)
            # test load_server_settings directly
            login_required(default_view).load_server_settings(
                self.conn, self.r)
            s = self.r.session.get('server_settings', {})
            prop = prop.replace(".label", "")
            assert s['ui']['menu']['dropdown'][prop]['label'] == label
        finally:
            self.rs.setConfigValue(key, d[key])

    def mock_getOmeroClientSettings(self, monkeypatch, default):
        def get_omeroClientSettings(*args, **kwargs):
            not_exist = [
                'omero.client.ui.menu.dropdown.everyone.label',
                'omero.client.ui.menu.dropdown.leaders.label',
                'omero.client.ui.menu.dropdown.colleagues.label',
                'omero.client.ui.tree.orphans.enabled',
                'omero.client.viewer.initial_zoom_level'
            ]
            for n in not_exist:
                if n in default:
                    del default[n]
            return default
        monkeypatch.setattr(omero.gateway.BlitzGateway,
                            'getOmeroClientSettings',
                            get_omeroClientSettings)

    @pytest.mark.parametrize("prop", ["colleagues", "leaders", "everyone"])
    @pytest.mark.parametrize("label", ["foo"])
    def testOldDropdownMenuConfig(self, monkeypatch, prop, label):
        """ Test against older server with monkeypatch """
        d = self.rs.getClientConfigDefaults()
        key = "omero.client.ui.menu.dropdown.%s" % prop
        try:
            if label is not None:
                self.rs.setConfigValue(key, label)
            self.mock_getOmeroClientSettings(monkeypatch,
                                             self.rs.getClientConfigValues())
            # validate old config
            ocs = self.conn.getOmeroClientSettings()
            not_exist = [
                'omero.client.ui.menu.dropdown.everyone.label',
                'omero.client.ui.menu.dropdown.leaders.label',
                'omero.client.ui.menu.dropdown.colleagues.label',
                'omero.client.ui.tree.orphans.enabled',
                'omero.client.viewer.initial_zoom_level'
            ]
            for n in not_exist:
                assert n not in ocs
            # test load_server_settings directly
            login_required(default_view).load_server_settings(
                self.conn, self.r)
            s = self.r.session.get('server_settings', {})
            if label is not None:
                assert s['ui']['menu']['dropdown'][prop]['label'] == label
            else:
                assert s['ui']['menu']['dropdown'][prop]['label'] == d[key]
        finally:
            self.rs.setConfigValue(key, d[key])
