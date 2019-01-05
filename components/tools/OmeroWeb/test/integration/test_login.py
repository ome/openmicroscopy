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
from django.conf import settings
from django.conf.urls import url
from django.utils.importlib import import_module
from django.test.utils import override_settings

from omeroweb.webclient.views import WebclientLoginView

from omeroweb.testlib import IWebTest, post, get
from django.core.urlresolvers import reverse
from django.test import Client
from random import random
import pytest

tag_url = reverse('load_template', kwargs={'menu': 'usertags'})


class CustomWebclientLoginView(WebclientLoginView):
    pass


urlpatterns = import_module(settings.ROOT_URLCONF).urlpatterns
urlpatterns += [
    url(r'^test_login/$',
        CustomWebclientLoginView.as_view(), name="test_weblogin"),
]


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
        """
        Tests handling of various login errors.
        E.g. missing fields, invalid credentials and guest login
        """
        django_client = self.django_root_client
        request_url = reverse('weblogin')
        data = credentials[0]
        data['server'] = 1
        message = credentials[1]
        rsp = post(django_client, request_url, data, status_code=200)
        assert message in rsp.content

    def test_get_login_page(self):
        """
        Simply test if a GET of the login url returns login page
        """
        django_client = Client()
        request_url = reverse('weblogin')
        rsp = get(django_client, request_url, {}, status_code=200)
        assert 'OMERO.web - Login' in rsp.content

    @pytest.mark.parametrize("redirect", ['', tag_url])
    def test_login_redirect(self, redirect):
        """
        Test that a successful login redirects to /webclient/
        or to specified url
        """
        django_client = self.django_root_client
        # redirect = reverse('load_template', kwargs={'menu': 'usertags'})
        request_url = "%s?url=%s" % (reverse('weblogin'), redirect)
        data = {'username': self.ctx.userName,
                'password': self.ctx.userName,
                'server': 1}
        rsp = post(django_client, request_url, data, status_code=302)
        if len(redirect) == 0:
            redirect = reverse('webindex')
        assert rsp['Location'].endswith(redirect)

    @override_settings(ROOT_URLCONF=__name__, LOGIN_VIEW='test_weblogin')
    def test_login_view(self):
        """
        Test that a successful logout redirects to custom login view
        """
        django_client = self.django_root_client
        request_url = reverse('test_weblogin')
        data = {
            'server': 1,
            'username': self.ctx.userName,
            'password': self.ctx.userName,
        }
        rsp = post(django_client, request_url, data, status_code=302)
        request_url = reverse('weblogout')
        rsp = post(django_client, request_url, {}, status_code=302)
        assert rsp['Location'].endswith(reverse('test_weblogin'))
        assert not rsp['Location'].endswith(reverse('weblogin'))
