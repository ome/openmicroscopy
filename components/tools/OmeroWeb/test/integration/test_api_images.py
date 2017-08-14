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

from omeroweb.testlib import IWebTest, get_json
from django.core.urlresolvers import reverse
from omeroweb.api import api_settings
import pytest
from test_api_projects import cmp_name_insensitive, get_update_service, \
    get_connection, marshal_objects
from omero.model import DatasetI, ImageI
from omero.rtypes import rstring
import json


def get_query_service(user):
    """Get the query_service for the given user's client."""
    return user[0].getSession().getQueryService()


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
        # remove any urls from json (tested elsewhere)
        for key in o1.keys():
            if key.startswith('url:'):
                del(o1[key])
        assert o1 == o2


class TestImages(IWebTest):
    """Tests querying & editing Images."""

    @pytest.fixture()
    def user1(self):
        """Return a new user in a read-annotate group."""
        group = self.new_group(perms='rwra--')
        return self.new_client_and_user(group=group)

    @pytest.fixture()
    def dataset_images(self, user1):
        """Return Dataset with Images and an orphaned Image."""
        dataset = DatasetI()
        dataset.name = rstring('Dataset')

        # Create 5 Images in Dataset
        for i in range(5):
            img = self.create_test_image(size_x=125, size_y=125,
                                         session=user1[0].getSession(),
                                         name="Image%s" % i)
            img = ImageI(img.id.val, False)
            dataset.linkImage(img)

        # Create a single orphaned Image
        image = self.create_test_image(size_x=125, size_y=125,
                                       session=user1[0].getSession())

        dataset = get_update_service(user1).saveAndReturnObject(dataset)
        return dataset, image

    def test_dataset_images(self, user1, dataset_images):
        """Test listing of Images in a Dataset."""
        conn = get_connection(user1)
        groupId = conn.getEventContext().groupId
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]

        dataset = dataset_images[0]
        images = dataset.linkedImageList()
        orphaned = dataset_images[1]

        images_url = reverse('api_images', kwargs={'api_version': version})
        datasets_url = reverse('api_datasets', kwargs={'api_version': version})

        # List ALL Images
        rsp = get_json(django_client, images_url, {'group': groupId})
        assert len(rsp['data']) == 6
        assert rsp['meta'] == {'totalCount': 6,
                               'limit': api_settings.API_LIMIT,
                               'maxLimit': api_settings.API_MAX_LIMIT,
                               'offset': 0}

        # Filter Images by Orphaned
        payload = {'orphaned': 'true', 'group': groupId}
        rsp = get_json(django_client, images_url, payload)
        assert_objects(conn, rsp['data'], [orphaned], dtype='Image',
                       group=groupId, opts={'load_pixels': True})
        assert rsp['meta'] == {'totalCount': 1,
                               'limit': api_settings.API_LIMIT,
                               'maxLimit': api_settings.API_MAX_LIMIT,
                               'offset': 0}

        # Filter Images by Dataset
        images.sort(cmp_name_insensitive)
        payload = {'dataset': dataset.id.val}
        rsp = get_json(django_client, images_url, payload)
        # Manual check that Pixels & Type are loaded but Channels are not
        assert 'Type' in rsp['data'][0]['Pixels']
        assert 'Channels' not in rsp['data'][0]['Pixels']
        assert_objects(conn, rsp['data'], images, dtype='Image',
                       opts={'load_pixels': True})
        assert rsp['meta'] == {'totalCount': 5,
                               'limit': api_settings.API_LIMIT,
                               'maxLimit': api_settings.API_MAX_LIMIT,
                               'offset': 0}

        # Pagination, listing images via /datasets/:id/images/
        limit = 3
        dataset_images_url = datasets_url + "%s/images/" % dataset.id.val
        payload = {'dataset': dataset.id.val, 'limit': limit}
        rsp = get_json(django_client, dataset_images_url, payload)
        assert_objects(conn, rsp['data'], images[0:limit], dtype='Image',
                       opts={'load_pixels': True})
        assert rsp['meta'] == {'totalCount': 5,
                               'limit': limit,
                               'maxLimit': api_settings.API_MAX_LIMIT,
                               'offset': 0}
        payload['offset'] = limit   # page 2
        rsp = get_json(django_client, images_url, payload)
        assert_objects(conn, rsp['data'], images[limit:limit * 2],
                       dtype='Image', opts={'load_pixels': True})
        assert rsp['meta'] == {'totalCount': 5,
                               'limit': limit,
                               'maxLimit': api_settings.API_MAX_LIMIT,
                               'offset': limit}

        # Show ONLY the orphaned image (channels are loaded by default)
        img_url = images_url + '%s/' % orphaned.id.val
        rsp = get_json(django_client, img_url)
        # Manual check that Channels are loaded
        img_json = rsp['data']
        assert len(img_json['Pixels']['Channels']) == 1
        assert_objects(conn, [img_json], [orphaned], dtype='Image',
                       opts={'load_channels': True})
