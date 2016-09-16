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
Tests webclient login
"""

from weblibrary import IWebTest, _csrf_post_response
from django.core.urlresolvers import reverse
from random import random
import pytest


class TestLogin(IWebTest):
    """
    Tests login
    """

    @pytest.mark.parametrize("credentials", [
        [{'username': 'guest', 'password': 'fake', 'server': 1},
            "Guest account is not supported."],
        [{'username': 'nobody', 'password': '', 'server': 1},
            "This field is required."],
        [{'password': 'fake'},
            "This field is required."],
        [{'username': 'g', 'password': str(random()), 'server': 1},
            "please check your user name and password."]
        ])
    def test_login_errors(self, credentials):
        django_client = self.django_root_client
        request_url = reverse('weblogin')
        data = credentials[0]
        data['server'] = 1
        message = credentials[1]
        rsp = _csrf_post_response(django_client, request_url, data,
                                  status_code=200)
        assert message in rsp.content
