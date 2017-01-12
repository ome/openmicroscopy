#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
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

"""Tests querying Images with web json api."""

from omeroweb.testlib import IWebTest, _get_response_json
from django.core.urlresolvers import reverse
from django.conf import settings
import pytest
from omero.gateway import BlitzGateway
from omero_marshal import get_encoder
from omero.model import DatasetI, ImageI
from omero.rtypes import rstring, unwrap
import json


def get_update_service(user):
    """Get the update_service for the given user's client."""
    return user[0].getSession().getUpdateService()


def get_query_service(user):
    """Get the query_service for the given user's client."""
    return user[0].getSession().getQueryService()


def get_connection(user, group_id=None):
    """Get a BlitzGateway connection for the given user's client."""
    connection = BlitzGateway(client_obj=user[0])
    # Refresh the session context
    connection.getEventContext()
    if group_id is not None:
        connection.SERVICE_OPTS.setOmeroGroup(group_id)
    return connection


def cmp_name_insensitive(x, y):
    """Case-insensitive name comparator."""
    return cmp(unwrap(x.name).lower(), unwrap(y.name).lower())


def marshal_objects(objects):
    """Marshal objects using omero_marshal."""
    expected = []
    for obj in objects:
        encoder = get_encoder(obj.__class__)
        expected.append(encoder.encode(obj))
    return expected


def assert_objects(conn, json_objects, omero_ids_objects, dtype="Project",
                   group='-1', extra=None, opts=None):
    """
    Load objects from OMERO, via conn.getObjects().

    marshal with omero_marshal and compare with json_objects.
    omero_ids_objects can be IDs or list of omero.model objects.

    @param: extra       List of dicts containing expected extra json data
                        e.g. {'omero:childCount': 1}
    """
    pids = []
    for p in omero_ids_objects:
        try:
            pids.append(long(p))
        except TypeError:
            pids.append(p.id.val)
    conn.SERVICE_OPTS.setOmeroGroup(group)
    objs = conn.getObjects(dtype, pids, respect_order=True, opts=opts)
    objs = [p._obj for p in objs]
    expected = marshal_objects(objs)
    assert len(json_objects) == len(expected)
    for i, o1, o2 in zip(range(len(expected)), json_objects, expected):
        if extra is not None and i < len(extra):
            o2.update(extra[i])
        # dumping to json and loading (same as test data) means that
        # unicode has been handled in same way, e.g. Pixel size symbols.
        o2 = json.loads(json.dumps(o2))
        assert o1 == o2


class TestImages(IWebTest):
    """Tests querying & editing Images."""

    @pytest.fixture()
    def user1(self):
        """Return a new user in a read-annotate group."""
        group = self.new_group(perms='rwra--')
        user = self.new_client_and_user(group=group)
        return user

    @pytest.fixture()
    def dataset_images(self, user1):
        """Return Dataset with Images and an orphaned Image."""
        query = get_query_service(user1)
        dataset = DatasetI()
        dataset.name = rstring('Dataset')

        # Create 5 Images in Dataset
        for i in range(5):
            img = self.create_test_image(size_x=125, size_y=125,
                                         session=user1[0].getSession(),
                                         name="Image%s" % i)
            img = ImageI(img.id.val, False)
            dataset.linkImage(img)

        # Import a single orphaned Image "tinyTest.d3d.dv" and get ImageID
        pids = self.import_image(client=user1[0], skip=None)
        pixels = query.get("Pixels", long(pids[0]))
        image = query.get("Image", pixels.image.id.val)

        dataset = get_update_service(user1).saveAndReturnObject(dataset)
        return dataset, image

    def test_dataset_images(self, user1, dataset_images):
        """Test listing of Images in a Dataset."""
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)
        version = settings.API_VERSIONS[-1]

        dataset = dataset_images[0]
        images = dataset.linkedImageList()
        orphaned = dataset_images[1]

        images_url = reverse('api_images', kwargs={'api_version': version})
        datasets_url = reverse('api_datasets', kwargs={'api_version': version})

        # List ALL Images
        rsp = _get_response_json(django_client, images_url, {})
        assert len(rsp['data']) == 6

        # Filter Images by Orphaned
        payload = {'orphaned': 'true'}
        rsp = _get_response_json(django_client, images_url, payload)
        assert_objects(conn, rsp['data'], [orphaned], dtype='Image',
                       opts={'load_pixels': True})

        # Filter Images by Dataset
        images.sort(cmp_name_insensitive)
        payload = {'dataset': dataset.id.val}
        rsp = _get_response_json(django_client, images_url, payload)
        # Manual check that Pixels is loaded but Channels are not
        assert 'Channels' not in rsp['data'][0]['Pixels']
        assert_objects(conn, rsp['data'], images, dtype='Image',
                       opts={'load_pixels': True})

        # Pagination, listing images via /datasets/:id/images/
        limit = 3
        dataset_images_url = datasets_url + "%s/images/" % dataset.id.val
        payload = {'dataset': dataset.id.val, 'limit': limit}
        rsp = _get_response_json(django_client, dataset_images_url, payload)
        assert_objects(conn, rsp['data'], images[0:limit], dtype='Image',
                       opts={'load_pixels': True})
        payload['page'] = 2
        rsp = _get_response_json(django_client, images_url, payload)
        assert_objects(conn, rsp['data'], images[limit:limit * 2],
                       dtype='Image', opts={'load_pixels': True})

        # Show ONLY the orphaned image (channels are loaded by default)
        img_url = images_url + '%s/' % orphaned.id.val
        rsp = _get_response_json(django_client, img_url, {})
        # Manual check that Channels is loaded
        assert len(rsp['Pixels']['Channels']) == 1
        assert_objects(conn, [rsp], [orphaned], dtype='Image',
                       opts={'load_channels': True})
