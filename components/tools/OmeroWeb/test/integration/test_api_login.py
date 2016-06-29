#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
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
Tests logging in with webgateway json api
"""

from weblibrary import IWebTest, _get_response, _csrf_post_response
from django.core.urlresolvers import reverse
import json
from django.conf import settings


class TestLogin(IWebTest):
    """
    Tests login workflow: getting url, csfv tokens etc.
    """

    def test_versions(self):
        """
        Start at the base url, get versions
        """
        django_client = self.django_root_client
        request_url = reverse('api_versions')
        data = _get_response_json(django_client, request_url, {})
        versions = data['versions']
        assert len(versions) == len(settings.WEBGATEWAY_API_VERSIONS)
        for v in versions:
            assert v['version'] in settings.WEBGATEWAY_API_VERSIONS


# Helpers
def _get_response_json(django_client, request_url,
                       query_string, status_code=200):
    rsp = _get_response(django_client, request_url, query_string, status_code)
    assert rsp.get('Content-Type') == 'application/json'
    return json.loads(rsp.content)


def _csrf_post_response_json(django_client, request_url,
                             query_string, status_code=200):
    rsp = _csrf_post_response(django_client, request_url,
                              query_string, status_code)
    assert rsp.get('Content-Type') == 'application/json'
    return json.loads(rsp.content)
