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

    @pytest.mark.parametrize("login", [['guest', 'guest'],
                                      ['g', str(random())]])
    def test_guest_login_not_supported(self, login):
        """
        Test that guest login is not permitted and login as 'g' is not
        confused as 'guest':
        https://trello.com/c/U47AiD1R/682-weird-guest-login
        """
        django_client = self.django_root_client
        request_url = reverse('weblogin')
        data = {'username': login[0], 'password': login[1], 'server': 1}
        rsp = _csrf_post_response(django_client, request_url, data,
                                  status_code=200)
        if login[0] == 'guest':
            assert "Guest account is not supported." in rsp.content
        else:
            assert "Guest account is not supported." not in rsp.content
            assert "please check your user name and password" in rsp.content
