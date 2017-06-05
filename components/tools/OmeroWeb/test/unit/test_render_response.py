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
#
"""
Simple unit tests for the "omeroweb.decorators".
"""

import pytest
import string

from django.test import RequestFactory
from django.test import override_settings
from django.utils.http import urlencode

from omeroweb.webclient.decorators import render_response

QUERY_STRING = " %s" % string.printable


def call_load_settings(request, conn):
    context = {'ome': {}}
    render_response().load_settings(request, context, conn)
    return context


class TestRenderResponse(object):

    def setup_method(self, method):
        # prepare session
        self.r = RequestFactory().get('/rand')

    @override_settings()
    def test_load_settings_defaults(self):
        context = call_load_settings(self.r, None)
        defaults = [
            {
                'link': u'/webclient/',
                'attrs': {u'title': u'Browse Data via Projects, Tags etc'},
                'label': u'Data'
            }, {
                'link': u'/webclient/history/',
                'attrs': {u'title': u'History'},
                'label': u'History'
            }, {
                'link': u'http://help.openmicroscopy.org/',
                'attrs': {
                    u'target': u'new',
                    u'title': u'Open OMERO user guide in a new tab'
                },
                'label': u'Help'
            }]
        assert context['ome']['top_links'] == defaults

    @pytest.mark.parametrize('top_links', [
        [['Data1', 'webindex', {"title": "Some text"}], ["/webclient/"]],
        [['Data2', {"viewname": 'webindex'}, {"title": "Some text"}],
            ["/webclient/"]],
        [['Data3', {"viewname": "load_template", "args": ["userdata"]},
            {}], ["/webclient/userdata/"]],
        [['Data4', {"viewname": "load_template", "args": ["userdata"],
                    "query_string": {"experimenter": -1}}, {}],
            ["/webclient/userdata/?experimenter=-1"]],
        [['Data5', {"viewname": "load_template", "args": ["userdata"],
                    "query_string": {"test": QUERY_STRING}}, {}],
            ["/webclient/userdata/?%s" % urlencode({'test': QUERY_STRING})]],
        [['History', 'history', {"title": "History"}],
            ["/webclient/history/"]],
        [['HELP', 'http://help.openmicroscopy.org', {"title": "Help"}],
            ["http://help.openmicroscopy.org"]],
        [["", "", {}], [""]],
        [["", None, {}], [None]],
        [["Foo", "bar", {}], ["bar"]],
        [['Foo', {"viewname": "foo"}, {}], [""]],
        [["Foo", {"viewname": "load_template", "args": ["bar"]}, {}], [""]],
        ])
    def test_load_settings(self, top_links):
        @override_settings(TOP_LINKS=[top_links[0]])
        def _test_load_settings():
            return call_load_settings(self.r, None)

        context = _test_load_settings()
        assert context['ome']['top_links'][0]['label'] == top_links[0][0]
        assert context['ome']['top_links'][0]['link'] == top_links[1][0]
        assert context['ome']['top_links'][0]['attrs'] == top_links[0][2]
