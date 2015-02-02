#!/usr/bin/env python
# -*- coding: utf-8 -*-

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
Tests chgrp functionality of views.py
"""

# import omero
# import omero.clients
from omero.model import ProjectI
from omero.rtypes import rstring

import pytest
import library as lib

from urllib import urlencode
from django.test import Client
from django.core.urlresolvers import reverse
import json

PRIVATE = 'rw----'
READONLY = 'rwr---'
READANNOTATE = 'rwra--'
COLLAB = 'rwrw--'

@pytest.fixture(scope='function')
def itest(request):
    """
    Returns a new L{test.integration.library.ITest} instance. With attached
    finalizer so that pytest will clean it up.
    """
    o = lib.ITest()
    o.setup_class()

    def finalizer():
        o.teardown_class()
    request.addfinalizer(finalizer)
    return o


@pytest.fixture(scope='function')
def client_2_groups(request, itest):
    """Returns a new user client in 2 read-annotate groups."""
    # Use group read-only permissions (not private) by default
    # return itest.new_client(perms='rwr---')
    # One user in two groups
    client, exp = itest.new_client_and_user(perms=READANNOTATE)
    grp = itest.new_group(experimenters=[exp], perms=READANNOTATE)
    client.sf.getAdminService().getEventContext()  # Reset session
    return client, grp


@pytest.fixture(scope='function')
def update_service(request, client_2_groups):
    """Returns a new OMERO update service."""
    client = client_2_groups[0]
    return client.getSession().getUpdateService()


@pytest.fixture(scope='function')
def project(request, itest, update_service):
    """Returns a new OMERO Project with required fields set."""
    project = ProjectI()
    project.name = rstring(itest.uuid())
    return update_service.saveAndReturnObject(project)


@pytest.fixture(scope='function')
def django_client(request, client_2_groups):
    """Returns a logged in Django test client."""
    client = client_2_groups[0]
    username = client.getProperty('omero.user')
    password = client.getProperty('omero.pass')
    django_client = _login_django_client(request, client, username, password)
    return django_client


@pytest.fixture(scope='function')
def admin_django_client(request, client_2_groups):
    """Returns Admin logged in Django test client."""
    client = client_2_groups[0]
    username = 'root'
    password = client.getProperty('omero.rootpass')
    django_client = _login_django_client(request, client, username, password)
    return django_client


class TestChgrp(object):
    """
    Tests chgrp 
    """

    def test_load_chgrp_groups(self, client_2_groups, project, django_client):
        """
        A user in 2 groups should have options to move object
        from one group to another.
        """
        group2 = client_2_groups[1]

        request_url = reverse('load_chgrp_groups')
        data = {
            "Project": project.id.val
        }
        data = _get_response_json(django_client, request_url, data)

        assert 'groups' in data
        assert len(data['groups']) == 1
        assert data['groups'][0]['id'] == group2.id.val

    def test_load_chgrp_groups_admin(self, client_2_groups,
                                     project, admin_django_client):
        """
        Admin should be able to move a user's Project
        from one group to another.
        """
        group2 = client_2_groups[1]

        request_url = reverse('load_chgrp_groups')
        data = {
            "Project": project.id.val
        }
        data = _get_response_json(admin_django_client, request_url, data)

        assert 'groups' in data
        assert len(data['groups']) == 1
        assert data['groups'][0]['id'] == group2.id.val


# Helpers
def _post_reponse(django_client, request_url, data, status_code=403):
    response = django_client.post(request_url, data=data)
    assert response.status_code == status_code
    return response

def _get_reponse(django_client, request_url, query_string, status_code=405):
    query_string = urlencode(query_string.items())
    response = django_client.get('%s?%s' % (request_url, query_string))
    assert response.status_code == status_code
    return response

def _get_response_json(django_client, request_url,
                       query_string, status_code=200):
    rsp = _get_reponse(django_client, request_url, query_string, status_code)
    assert rsp.get('Content-Type') == 'application/json'
    return json.loads(rsp.content)

def _login_django_client(request, client, username, password):

    django_client = Client(enforce_csrf_checks=True)
    login_url = reverse('weblogin')

    response = django_client.get(login_url)
    assert response.status_code == 200
    csrf_token = django_client.cookies['csrftoken'].value

    data = {
        'server': 1,
        'username': username,
        'password': password,
        'csrfmiddlewaretoken': csrf_token
    }
    response = django_client.post(login_url, data)
    assert response.status_code == 302

    def finalizer():
        logout_url = reverse('weblogout')
        data = {'csrfmiddlewaretoken': csrf_token}
        response = django_client.post(logout_url, data=data)
        assert response.status_code == 302
    request.addfinalizer(finalizer)
    return django_client
