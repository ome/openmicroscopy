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
from test_api_projects import cmp_name_insensitive, get_update_service, \
    get_connection, marshal_objects
from omero.gateway import BlitzGateway
from omero.model import DatasetI, \
    ImageI, \
    PlateI, \
    ProjectI, \
    ScreenI, \
    WellI, \
    WellSampleI
from omero.rtypes import rstring, rint
from omero_marshal import OME_SCHEMA_URL


def build_url(client, url_name, url_kwargs):
    """Build an absolute url using client response url."""
    response = client.request()
    # http://testserver/webclient/
    webclient_url = response.url
    url = reverse(url_name, kwargs=url_kwargs)
    url = webclient_url.replace('/webclient/', url)
    return url


def add_image_urls(expected, client):
    """Add urls to expected Images within Well dict."""
    version = settings.API_VERSIONS[-1]
    if 'WellSamples' in expected:
        for ws in expected['WellSamples']:
            image_id = ws['Image']['@id']
            url = build_url(client, 'api_image', {'api_version': version,
                                                  'object_id': image_id})
            ws['Image']['url:image'] = url
    return expected


def assert_objects(conn, json_objects, omero_ids_objects, dtype="Project",
                   group='-1', extra=None, opts=None, client=None):
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
        # remove any urls from json, if not in both objects
        for key in o1.keys():
            if key.startswith('url:') and key not in o2:
                del(o1[key])
        # add urls to any 'Image' in expected 'Wells' dict
        add_image_urls(o2, client)
        assert o1 == o2


class TestContainers(IWebTest):
    """Tests querying & editing Datasets, Screens etc."""

    @pytest.fixture()
    def user1(self):
        """Return a new user in a read-annotate group."""
        group = self.new_group(perms='rwra--')
        user = self.new_client_and_user(group=group)
        return user

    @pytest.fixture()
    def project_datasets(self, user1):
        """Return Project with Datasets and an orphaned Dataset."""
        # Create and name all the objects
        project = ProjectI()
        project.name = rstring('Project')

        # Create 5 Datasets, each with 0-4 images.
        for d in range(5):
            dataset1 = DatasetI()
            dataset1.name = rstring('Dataset%s' % d)
            for i in range(d):
                image = self.create_test_image(size_x=5, size_y=5,
                                               session=user1[0].getSession(),
                                               name="Image%s" % i)
                image = ImageI(image.id.val, False)
                dataset1.linkImage(image)
            project.linkDataset(dataset1)

        # Create single orphaned Dataset
        dataset = DatasetI()
        dataset.name = rstring('Dataset')

        project = get_update_service(user1).saveAndReturnObject(project)
        dataset = get_update_service(user1).saveAndReturnObject(dataset)
        return project, dataset

    @pytest.fixture()
    def screen_plates(self, user1):
        """Return Screen with Plates and an orphaned Plate."""
        # Create and name all the objects
        screen = ScreenI()
        screen.name = rstring('screen')

        for i in range(5):
            plate1 = PlateI()
            plate1.name = rstring('Plate%s' % i)
            screen.linkPlate(plate1)

        # Create single orphaned Plate
        plate = PlateI()
        plate.name = rstring('plate')

        screen = get_update_service(user1).saveAndReturnObject(screen)
        plate = get_update_service(user1).saveAndReturnObject(plate)

        # Add well to first plate
        plates = screen.linkedPlateList()
        plates.sort(cmp_name_insensitive)
        plate_id = plates[0].id.val
        well = WellI()
        well.column = rint(0)
        well.row = rint(0)
        well.plate = PlateI(plate_id, False)
        image = self.create_test_image(
            size_x=5, size_y=5, session=user1[0].getSession())
        ws = WellSampleI()
        ws.image = ImageI(image.id, False)
        ws.well = well
        well.addWellSample(ws)
        well = get_update_service(user1).saveAndReturnObject(well)
        return screen, plate

    @pytest.fixture()
    def user_screens(self, user1):
        """Create screens belonging to user1."""
        screens = []
        for i in range(5):
            screen = ScreenI()
            screen.name = rstring('Screen%s' % i)
            screens.append(screen)
        screens = get_update_service(user1).saveAndReturnArray(screens)
        screens.sort(cmp_name_insensitive)
        return screens

    @pytest.mark.parametrize("dtype", ['Plate', 'Image', 'Well',
                                       'Channel', 'foo'])
    def test_crud_unsupported(self, user1, dtype):
        """Test create, update & delete are rejected for unsupported types."""
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)
        version = settings.API_VERSIONS[-1]
        save_url = reverse('api_save', kwargs={'api_version': version})
        payload = {'Name': 'test',
                   '@type': OME_SCHEMA_URL + '#%s' % dtype}
        # Test POST creation
        rsp = _csrf_post_json(django_client, save_url, payload,
                              status_code=405)
        assert rsp['message'] == 'Creation of %s not supported' % dtype
        # Test PUT update
        rsp = _csrf_put_json(django_client, save_url, payload,
                             status_code=405)
        assert rsp['message'] == 'Update of %s not supported' % dtype
        # Delete (fake url - image doesn't need to exist for test)
        if dtype in ('Plate', 'Image', 'Well'):
            url_name = 'api_%s' % dtype.lower()
            delete_url = reverse(url_name, kwargs={'api_version': version,
                                                   'object_id': 1})
            rsp = _csrf_delete_response_json(django_client, delete_url, {},
                                             status_code=405)
            assert rsp['message'] == 'Delete of %s not supported' % dtype

    @pytest.mark.parametrize("dtype", ['Project', 'Dataset',
                                       'Screen'])
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
        schema_url = rsp['url:schema']
        # specify group via query params
        save_url = "%s?group=%s" % (rsp['url:save'], group)
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

    @pytest.mark.parametrize("dtype", ['Dataset', 'Plate'])
    def test_datasets_plates(self, user1, dtype, project_datasets,
                             screen_plates):
        """Test listing of Datasets in a Project and Plates in Screen."""
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)
        version = settings.API_VERSIONS[-1]

        # Handle parametrized dtype, setting up other variables
        if dtype == 'Dataset':
            parent = project_datasets[0]
            children = parent.linkedDatasetList()
            orphaned = project_datasets[1]
            url_name = 'api_datasets'
            ptype = 'project'
            child_counts = [{'omero:childCount': c} for c in range(5)]
        else:
            parent = screen_plates[0]
            children = parent.linkedPlateList()
            orphaned = screen_plates[1]
            url_name = 'api_plates'
            ptype = 'screen'
            child_counts = None

        request_url = reverse(url_name, kwargs={'api_version': version})

        # List ALL Datasets or Plates
        rsp = _get_response_json(django_client, request_url, {})
        assert len(rsp['data']) == 6

        # Filter Datasets or Plates by Orphaned
        payload = {'orphaned': 'true'}
        rsp = _get_response_json(django_client, request_url, payload)
        assert_objects(conn, rsp['data'], [orphaned], dtype=dtype)

        # Filter Datasets by Project or Plates by Screen
        children.sort(cmp_name_insensitive)
        # Also testing childCount
        payload = {ptype: parent.id.val, 'childCount': 'true'}
        rsp = _get_response_json(django_client, request_url, payload)
        assert len(rsp['data']) == 5
        assert_objects(conn, rsp['data'], children, dtype=dtype,
                       extra=child_counts)

        # Pagination
        limit = 3
        payload = {ptype: parent.id.val, 'limit': limit}
        rsp = _get_response_json(django_client, request_url, payload)
        assert_objects(conn, rsp['data'], children[0:limit], dtype=dtype)
        payload['page'] = 2
        rsp = _get_response_json(django_client, request_url, payload)
        assert_objects(conn, rsp['data'], children[limit:limit * 2],
                       dtype=dtype)

    def test_screens(self, user1, user_screens):
        """Test listing of Screens."""
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        client = self.new_django_client(user_name, user_name)
        version = settings.API_VERSIONS[-1]
        request_url = reverse('api_screens', kwargs={'api_version': version})

        # List ALL Screens
        rsp = _get_response_json(client, request_url, {})
        extra = []
        for screen in user_screens:
            s_url = build_url(client, 'api_screen',
                              {'api_version': version,
                               'object_id': screen.id.val})
            p_url = build_url(client, 'api_screen_plates',
                              {'api_version': version,
                               'screen_id': screen.id.val})
            extra.append({
                'url:screen': s_url,
                'url:plates': p_url
            })
        assert_objects(conn, rsp['data'], user_screens,
                       dtype="Screen", extra=extra)

    def test_spw_urls(self, user1, screen_plates):
        """Test browsing via urls in json /api/->SPW."""
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        client = self.new_django_client(user_name, user_name)
        version = settings.API_VERSIONS[-1]
        base_url = reverse('api_base', kwargs={'api_version': version})
        base_rsp = _get_response_json(client, base_url, {})

        # List screens
        screen, plate = screen_plates
        screens_url = base_rsp['url:screens']
        rsp = _get_response_json(client, screens_url, {})
        screens_json = rsp['data']
        extra = [{
            'url:screen': build_url(client, 'api_screen',
                                    {'api_version': version,
                                     'object_id': screen.id.val}),
            'url:plates': build_url(client, 'api_screen_plates',
                                    {'api_version': version,
                                     'screen_id': screen.id.val})
        }]
        assert_objects(conn, screens_json, [screen], dtype='Screen',
                       extra=extra)
        # View single screen
        rsp = _get_response_json(client, screens_json[0]['url:screen'], {})
        assert_objects(conn, [rsp], [screen], dtype='Screen',
                       extra=[{'url:plates': extra[0]['url:plates']}])

        # List plates
        plates_url = screens_json[0]['url:plates']
        plates = screen.linkedPlateList()
        plates.sort(cmp_name_insensitive)
        rsp = _get_response_json(client, plates_url, {})
        plates_json = rsp['data']
        extra = []
        for p in plates:
            extra.append({
                'url:plate': build_url(client, 'api_plate',
                                       {'api_version': version,
                                        'object_id': p.id.val}),
                'url:wells': build_url(client, 'api_plate_wells',
                                       {'api_version': version,
                                        'plate_id': p.id.val})
            })
        assert_objects(conn, plates_json, plates, dtype='Plate', extra=extra)
        # View single plate
        rsp = _get_response_json(client, plates_json[0]['url:plate'], {})
        assert_objects(conn, [rsp], plates[0:1], dtype='Plate')

        # List wells of first plate
        wells_url = plates_json[0]['url:wells']
        rsp = _get_response_json(client, wells_url, {})
        wells_json = rsp['data']
        well_id = wells_json[0]['@id']
        extra = [{'url:well': build_url(client, 'api_well',
                  {'api_version': version, 'object_id': well_id})}
                 ]
        assert_objects(conn, wells_json, [well_id], dtype='Well',
                       extra=extra, opts={'load_images': True}, client=client)

    def test_pdi_urls(self, user1, project_datasets):
        """Test browsing via urls in json /api/->PDI."""
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        client = self.new_django_client(user_name, user_name)
        version = settings.API_VERSIONS[-1]
        base_url = reverse('api_base', kwargs={'api_version': version})
        base_rsp = _get_response_json(client, base_url, {})

        # List projects
        project, dataset = project_datasets
        projects_url = base_rsp['url:projects']
        rsp = _get_response_json(client, projects_url, {})
        projects_json = rsp['data']
        extra = [{
            'url:project': build_url(client, 'api_project',
                                     {'api_version': version,
                                      'object_id': project.id.val}),
            'url:datasets': build_url(client, 'api_project_datasets',
                                      {'api_version': version,
                                       'project_id': project.id.val})
        }]
        assert_objects(conn, projects_json, [project], extra=extra)
        # View single Project
        rsp = _get_response_json(client, projects_json[0]['url:project'], {})
        assert_objects(conn, [rsp], [project],
                       extra=[{'url:datasets': extra[0]['url:datasets']}])

        # List datasets
        datasets_url = projects_json[0]['url:datasets']
        datasets = project.linkedDatasetList()
        datasets.sort(cmp_name_insensitive)
        rsp = _get_response_json(client, datasets_url, {})
        datasets_json = rsp['data']
        extra = []
        for d in datasets:
            extra.append({
                'url:dataset': build_url(client, 'api_dataset',
                                         {'api_version': version,
                                          'object_id': d.id.val}),
                'url:images': build_url(client, 'api_dataset_images',
                                        {'api_version': version,
                                         'dataset_id': d.id.val})
            })
        assert_objects(conn, datasets_json, datasets,
                       dtype='Dataset', extra=extra)
        # View single Dataset
        rsp = _get_response_json(client, datasets_json[0]['url:dataset'], {})
        assert_objects(conn, [rsp], datasets[0:1], dtype='Dataset',
                       extra=[{'url:images': extra[0]['url:images']}])

        # List images (from last Dataset)
        images_url = datasets_json[-1]['url:images']
        images = datasets[-1].linkedImageList()
        images.sort(cmp_name_insensitive)
        rsp = _get_response_json(client, images_url, {})
        images_json = rsp['data']
        extra = []
        for i in images:
            extra.append({
                'url:image': build_url(client, 'api_image',
                                       {'api_version': version,
                                        'object_id': i.id.val}),
            })
        assert_objects(conn, images_json, images,
                       dtype='Image', extra=extra, opts={'load_pixels': True})
        # View single Image
        rsp = _get_response_json(client, images_json[0]['url:image'], {})
        assert_objects(conn, [rsp], images[0:1], dtype='Image',
                       opts={'load_channels': True})
