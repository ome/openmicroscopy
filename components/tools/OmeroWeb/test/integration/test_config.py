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

from omero.testlib import ITest

from django.test import RequestFactory
from django.contrib.sessions.middleware import SessionMiddleware

from omeroweb.webclient import webclient_gateway  # NOQA
from omero.gateway import BlitzGateway
from omeroweb.decorators import login_required

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


class TestConfig(ITest):

    def setup_method(self, method):
        # prepare session
        self.r = RequestFactory().get('/rand')
        middleware = SessionMiddleware()
        middleware.process_request(self.r)
        self.r.session.save()
        self.rs = self.root.sf.getConfigService()
        self.conn = BlitzGateway(client_obj=self.new_client())

    def teardown_method(self, method):
        self.conn.close()
        self.r.session.flush()

    def testDefaultConfig(self):
        """ Test loading default config """
        default = self.rs.getClientConfigDefaults()
        login_required(default_view).load_server_settings(self.conn, self.r)
        s = {"omero": {"client": self.r.session.get('server_settings', {})}}
        # compare keys in default and config loaded by decorator
        a = filter(lambda x: x not in (
            set(default.keys())),
            set(flattenProperties(s).keys())
            )
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
