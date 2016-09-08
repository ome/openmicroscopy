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
Tests querying & editing Projects with webgateway json api
"""

from weblibrary import IWebTest, _csrf_post_json, _csrf_put_json
from django.core.urlresolvers import reverse
from django.conf import settings
from omero.gateway import BlitzGateway
import pytest
from omero.model import ProjectI
from omero.rtypes import rstring
from omero_marshal import get_encoder, get_decoder, OME_SCHEMA_URL
from omero import ValidationException


def get_connection(user, group_id=None):
    """
    Get a BlitzGateway connection for the given user's client
    """
    connection = BlitzGateway(client_obj=user[0])
    # Refresh the session context
    connection.getEventContext()
    if group_id is not None:
        connection.SERVICE_OPTS.setOmeroGroup(group_id)
    return connection


class TestErrors(IWebTest):
    """
    Tests the response status with various error types
    """

    # Create a read-annotate group
    @pytest.fixture(scope='function')
    def groupA(self):
        """Returns a new read-only group."""
        return self.new_group(perms='rwra--')

    # Create a read-only group
    @pytest.fixture(scope='function')
    def groupB(self):
        """Returns a new read-only group."""
        return self.new_group(perms='rwr---')

    # Create users in the read-only group
    @pytest.fixture()
    def userA(self, groupA, groupB):
        """Returns a new user in the groupA group and also add to groupB"""
        user = self.new_client_and_user(group=groupA)
        self.add_groups(user[1], [groupB])
        return user

    def test_marshal_validation(self):
        django_client = self.django_root_client
        version = settings.API_VERSIONS[-1]
        save_url = reverse('api_save', kwargs={'api_version': version})
        payload = {'Name': 'test_marshal_validation',
                   '@type': OME_SCHEMA_URL + '#Project',
                   'omero:details': {'@type': 'foo'}}
        rsp = _csrf_post_json(django_client, save_url, payload,
                              status_code=400)
        assert rsp['message'] == "'NoneType' object has no attribute 'decode'"
        assert rsp['stacktrace'].startswith(
            'Traceback (most recent call last):')

    def test_security_violation(self, groupB, userA):
        conn = get_connection(userA)
        groupAid = conn.getEventContext().groupId
        userName = conn.getUser().getName()
        django_client = self.new_django_client(userName, userName)
        version = settings.API_VERSIONS[-1]
        groupBid = groupB.id.val
        save_url = reverse('api_save', kwargs={'api_version': version})
        # Create project in groupA (default group)
        payload = {'Name': 'test_security_violation',
                   '@type': OME_SCHEMA_URL + '#Project'}
        save_url_grpA = save_url + '?group=' + str(groupAid)
        pr_json = _csrf_post_json(django_client, save_url_grpA, payload)
        projectId = pr_json['@id']
        # Try to save again into group B
        save_url_grpB = save_url + '?group=' + str(groupBid)
        rsp = _csrf_put_json(django_client, save_url_grpB, pr_json,
                             status_code=403)
        assert 'message' in rsp
        msg = "Cannot read ome.model.containers.Project:Id_%s" % projectId
        assert msg in rsp['message']
        assert rsp['stacktrace'].startswith(
            'Traceback (most recent call last):')

    def test_marshal_exception(self):
        django_client = self.django_root_client
        version = settings.API_VERSIONS[-1]
        save_url = reverse('api_save', kwargs={'api_version': version})
        payload = {'Name': 'test_type_error',
                   '@type': OME_SCHEMA_URL + '#Project',
                   'omero:details': {'@type': 'foo'}}
        rsp = _csrf_post_json(django_client, save_url, payload,
                              status_code=400)
        assert rsp['message'] == "'NoneType' object has no attribute 'decode'"
        assert rsp['stacktrace'].startswith(
            'Traceback (most recent call last):')

    def test_validation_exception(self, userA):
        conn = get_connection(userA)
        group = conn.getEventContext().groupId
        userName = conn.getUser().getName()
        django_client = self.new_django_client(userName, userName)
        version = settings.API_VERSIONS[-1]
        save_url = reverse('api_save', kwargs={'api_version': version})
        save_url += '?group=' + str(group)

        # Create Tag
        tag = {'Value': 'test_tag',
               '@type': OME_SCHEMA_URL + '#TagAnnotation'}
        tag_rsp = _csrf_post_json(django_client, save_url, tag)

        # Add Tag twice to Project to get Validation Exception
        del tag_rsp['omero:details']
        payload = {'Name': 'test_validation',
                   '@type': OME_SCHEMA_URL + '#Project',
                   'Annotations': [tag_rsp, tag_rsp]}
        rsp = _csrf_post_json(django_client, save_url, payload,
                              status_code=404)
        # NB: message contains whole stack trace
        assert "ValidationException" in rsp['message']
        assert rsp['stacktrace'].startswith(
            'Traceback (most recent call last):')

    def test_project_validation(self, userA):
        """
        This test illustrates the ValidationException we see when
        Project is encoded to dict then decoded back to Project
        and saved.
        No exception is seen if the original Project is simply
        saved without encode & decode OR if the details are unloaded
        before saving
        """
        conn = get_connection(userA)
        project = ProjectI()
        project.name = rstring('test_project_validation')
        project = conn.getUpdateService().saveAndReturnObject(project)

        # Saving original Project again is OK
        conn.getUpdateService().saveObject(project)

        # encode and decode before Save raises Validation Exception
        project_json = get_encoder(project.__class__).encode(project)
        decoder = get_decoder(project_json['@type'])
        p = decoder.decode(project_json)
        with pytest.raises(ValidationException):
            conn.getUpdateService().saveObject(p)

        p = decoder.decode(project_json)
        # Unloading details allows Save without exception
        p.unloadDetails()
        conn.getUpdateService().saveObject(p)
