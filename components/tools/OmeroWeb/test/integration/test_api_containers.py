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

"""Tests querying & editing Containers with webgateway json api."""

from omeroweb.testlib import IWebTest, _get_response_json, \
    _csrf_post_json, _csrf_put_json, _csrf_delete_response_json
from django.core.urlresolvers import reverse
from django.conf import settings
import pytest
from omero.gateway import BlitzGateway
from omero_marshal import get_encoder


def marshal_objects(objects):
    """Marshal objects using omero_marshal."""
    expected = []
    for obj in objects:
        encoder = get_encoder(obj.__class__)
        expected.append(encoder.encode(obj))
    return expected


def assert_objects(conn, json_objects, omero_ids_objects, dtype="Project",
                   group='-1'):
    """
    Load objects from OMERO, via conn.getObjects().

    marshal with omero_marshal and compare with json_objects.
    omero_ids_objects can be IDs or list of omero.model objects.
    """
    pids = []
    for p in omero_ids_objects:
        try:
            pids.append(long(p))
        except TypeError:
            pids.append(p.id.val)
    conn.SERVICE_OPTS.setOmeroGroup(group)
    projects = conn.getObjects(dtype, pids, respect_order=True)
    projects = [p._obj for p in projects]
    expected = marshal_objects(projects)
    assert len(json_objects) == len(expected)
    for o1, o2 in zip(json_objects, expected):
        assert o1 == o2


class TestContainers(IWebTest):
    """Tests querying & editing Datasets, Screens etc."""

    @pytest.mark.parametrize("dtype", ['Project', 'Dataset', 'Screen'])
    def test_container_crud(self, dtype):
        """
        Test create, read, update and delete of Containers.

        Create with POST to /save
        Read with GET of /m/dtype/:id/
        Update with PUT to /m/dtype/:id/
        Delete with DELETE to /m/dtype/:id/
        """
        django_client = self.django_root_client
        group = self.ctx.groupId
        version = settings.API_VERSIONS[-1]
        # Need to get the Schema url to create @type
        base_url = reverse('api_base', kwargs={'api_version': version})
        rsp = _get_response_json(django_client, base_url, {})
        schema_url = rsp['schema_url']
        # specify group via query params
        save_url = "%s?group=%s" % (rsp['save_url'], group)
        project_name = 'test_container_create_read'
        payload = {'Name': project_name,
                   '@type': '%s#%s' % (schema_url, dtype)}
        rsp = _csrf_post_json(django_client, save_url, payload,
                              status_code=201)
        # We get the complete new Object returned
        assert rsp['Name'] == project_name
        object_id = rsp['@id']

        # Read Object
        object_url = "%sm/%ss/%s/" % (base_url, dtype.lower(), object_id)
        object_json = _get_response_json(django_client, object_url, {})
        assert object_json['@id'] == object_id
        conn = BlitzGateway(client_obj=self.root)
        assert_objects(conn, [object_json], [object_id], dtype=dtype)

        # Update Object...
        object_json['Name'] = 'new name'
        rsp = _csrf_put_json(django_client, save_url, object_json)
        # ...and read again to check
        updated_json = _get_response_json(django_client, object_url, {})
        assert updated_json['Name'] == 'new name'

        # Delete
        _csrf_delete_response_json(django_client, object_url, {})
        # Get should now return 404
        rsp = _get_response_json(django_client, object_url, {},
                                 status_code=404)
