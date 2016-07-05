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
Simple unit tests for the "webclient_utils" module.
"""

import pytest
import json

from django.core.urlresolvers import reverse

from omeroweb.utils import reverse_with_params
from omeroweb.webclient.webclient_utils import formatPercentFraction
from omeroweb.webclient.webclient_utils import getDateTime


class TestUtil(object):
    """
    Tests various util methods
    """

    def test_format_percent_fraction(self):

        assert formatPercentFraction(1) == "100"
        assert formatPercentFraction(0.805) == "81"
        assert formatPercentFraction(0.2) == "20"
        assert formatPercentFraction(0.01) == "1"
        assert formatPercentFraction(0.005) == "0.5"
        assert formatPercentFraction(0.005) == "0.5"
        assert formatPercentFraction(0.0025) == "0.3"
        assert formatPercentFraction(0.00) == "0.0"

    def test_get_date_time(self):
        """ Tests that only a full date-time string is valid """
        assert getDateTime("2015-12-01 00:00:00") is not None
        assert getDateTime("2015-12-01 23:59:59") is not None
        with pytest.raises(ValueError):
            getDateTime("12345")
        with pytest.raises(ValueError):
            getDateTime("invalid")
        with pytest.raises(ValueError):
            getDateTime("2015-12-01")

    @pytest.mark.parametrize('top_links', [(
        ('{"viewname": "load_template", "args": ["userdata"],'
         '"query_string": {"experimenter": -1}}'),
        "/webclient/userdata/?experimenter=-1"),
        ('{"viewname": "webindex", "query_string": {"foo": "bar"}}',
         "/webclient/?foo=bar"),
        ('{"viewname": "foo", "args": ["bar"]}', ""),
        ('{"viewname": "foo", "query_string": {"foo": "bar"}}', ""),
        ])
    def test_reverse_with_params_dict(self, top_links):
        top_link = json.loads(top_links[0])
        assert reverse_with_params(**top_link) == top_links[1]

    @pytest.mark.parametrize('top_links', [
        ("history", "/webclient/history/"),
        ("webindex", "/webclient/"),
        ])
    def test_reverse_with_params_string(self, top_links):
        top_link = top_links[0]
        assert reverse_with_params(top_link) == reverse(top_link) \
            == top_links[1]

    @pytest.mark.xfail(raises=TypeError)
    @pytest.mark.parametrize('top_link', ["foo", '', None])
    def test_bad_reverse_with_params_string(self, top_link):
        reverse_with_params(**top_link)
