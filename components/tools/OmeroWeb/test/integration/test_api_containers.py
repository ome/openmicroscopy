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
from omeroweb.api import api_settings
import pytest
from test_api_projects import cmp_name_insensitive, get_update_service, \
    get_connection, marshal_objects
from omero.gateway import BlitzGateway
from omero.model import DatasetI, \
    ImageI, \
    PlateI, \
    ProjectI, \
    ScreenI, \
    TagAnnotationI, \
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
    version = api_settings.API_VERSIONS[-1]
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
    @pytest.mark.parametrize("method", [(_csrf_post_json, 'Creation'),
                                        (_csrf_put_json, 'Update')])
    def test_create_update_unsupported(self, user1, dtype, method):
        """Test create and update are rejected for unsupported types."""
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]
        save_url = reverse('api_save', kwargs={'api_version': version})
        payload = {'Name': 'test',
                   '@type': OME_SCHEMA_URL + '#%s' % dtype}
        # Test PUT/POST
        rsp = method[0](django_client, save_url, payload,
                        status_code=405)
        assert rsp['message'] == '%s of %s not supported' % (method[1], dtype)

    @pytest.mark.parametrize("dtype", ['Plate', 'Image', 'Well'])
    def test_delete_unsupported(self, user1, dtype):
        """Test delete is rejected for unsupported types."""
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]
        # Delete (fake url - image doesn't need to exist for test)
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
        version = api_settings.API_VERSIONS[-1]
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
        new_obj = rsp['data']
        # We get the complete new Object returned
        assert new_obj['Name'] == project_name
        object_id = new_obj['@id']

        # Read Object
        object_url = "%sm/%ss/%s/" % (base_url, dtype.lower(), object_id)
        rsp = _get_response_json(django_client, object_url, {})
        object_json = rsp['data']
        assert object_json['@id'] == object_id
        conn = BlitzGateway(client_obj=self.root)
        assert_objects(conn, [object_json], [object_id], dtype=dtype)

        # Update Object...
        object_json['Name'] = 'new name'
        rsp = _csrf_put_json(django_client, save_url, object_json)
        # ...and read again to check
        rsp = _get_response_json(django_client, object_url, {})
        updated_json = rsp['data']
        assert updated_json['Name'] == 'new name'

        # Delete
        _csrf_delete_response_json(django_client, object_url, {})
        # Get should now return 404
        rsp = _get_response_json(django_client, object_url, {},
                                 status_code=404)

    @pytest.mark.parametrize("child_count", [True, False])
    @pytest.mark.parametrize("dtype", ['Dataset', 'Plate'])
    def test_datasets_plates(self, user1, dtype, child_count,
                             project_datasets, screen_plates):
        """Test listing of Datasets in a Project and Plates in Screen."""
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]

        # Handle parametrized dtype, setting up other variables
        if dtype == 'Dataset':
            parent = project_datasets[0]
            children = parent.linkedDatasetList()
            orphaned = project_datasets[1]
            url_name = 'api_datasets'
            ptype = 'project'
            # Counts of Images in Dataset / orphaned Dataset
            ds_or_pl_children = [{'omero:childCount': c} for c in range(5)]
            orph_ds_pl_children = [{'omero:childCount': 0}]
            pr_or_sc_children = [{'omero:childCount': 5}]

        else:
            parent = screen_plates[0]
            children = parent.linkedPlateList()
            orphaned = screen_plates[1]
            url_name = 'api_plates'
            ptype = 'screen'
            # Plates don't support childCount.
            ds_or_pl_children = None
            orph_ds_pl_children = None
            pr_or_sc_children = [{'omero:childCount': 5}]

        if not child_count:
            ds_or_pl_children = None
            orph_ds_pl_children = None
            pr_or_sc_children = None

        # Check child_count in Projects or Screens
        base_url = reverse('api_base', kwargs={'api_version': version})
        parents_url = "%sm/%ss/" % (base_url, ptype)
        payload = {'childCount': str(child_count).lower()}
        rsp = _get_response_json(django_client, parents_url, payload)
        assert_objects(conn, rsp['data'], [parent], dtype=ptype,
                       extra=pr_or_sc_children)
        # And for single Project or Screen
        parent_url = "%sm/%ss/%s/" % (base_url, ptype, parent.id.val)
        rsp = _get_response_json(django_client, parent_url, payload)
        assert_objects(conn, [rsp['data']], [parent], dtype=ptype,
                       extra=pr_or_sc_children)

        request_url = reverse(url_name, kwargs={'api_version': version})

        # List ALL Datasets or Plates
        rsp = _get_response_json(django_client, request_url, payload)
        assert len(rsp['data']) == 6
        assert rsp['meta'] == {'totalCount': 6,
                               'limit': api_settings.API_LIMIT,
                               'maxLimit': api_settings.API_MAX_LIMIT,
                               'offset': 0}

        # Filter Datasets or Plates by Orphaned
        payload = {'orphaned': 'true', 'childCount': str(child_count).lower()}
        rsp = _get_response_json(django_client, request_url, payload)
        assert_objects(conn, rsp['data'], [orphaned], dtype=dtype,
                       extra=orph_ds_pl_children)
        assert rsp['meta'] == {'totalCount': 1,
                               'limit': api_settings.API_LIMIT,
                               'maxLimit': api_settings.API_MAX_LIMIT,
                               'offset': 0}

        # Filter Datasets by Project or Plates by Screen
        children.sort(cmp_name_insensitive)
        payload = {ptype: parent.id.val,
                   'childCount': str(child_count).lower()}
        rsp = _get_response_json(django_client, request_url, payload)
        assert len(rsp['data']) == 5
        assert_objects(conn, rsp['data'], children, dtype=dtype,
                       extra=ds_or_pl_children)
        assert rsp['meta'] == {'totalCount': 5,
                               'limit': api_settings.API_LIMIT,
                               'maxLimit': api_settings.API_MAX_LIMIT,
                               'offset': 0}

        # Single (first) Dataset or Plate
        payload = {'childCount': str(child_count).lower()}
        object_url = "%sm/%ss/%s/" % (base_url, dtype.lower(),
                                      children[0].id.val)
        rsp = _get_response_json(django_client, object_url, payload)
        if dtype == 'Plate':
            # When we get a single Plate, expect this (not when listing plates)
            ds_or_pl_children = [{'omero:wellsampleIndex': [0, 0]}]
        assert_objects(conn, [rsp['data']], [children[0]], dtype=dtype,
                       extra=ds_or_pl_children)

        # Pagination
        limit = 3
        payload = {ptype: parent.id.val,
                   'limit': limit,
                   'childCount': str(child_count).lower()}
        rsp = _get_response_json(django_client, request_url, payload)
        extra = None
        if ds_or_pl_children is not None and len(ds_or_pl_children) > 1:
            extra = ds_or_pl_children[0:limit]
        assert_objects(conn, rsp['data'], children[0:limit], dtype=dtype,
                       extra=extra)
        assert rsp['meta'] == {'totalCount': 5,
                               'limit': limit,
                               'maxLimit': api_settings.API_MAX_LIMIT,
                               'offset': 0}
        payload['offset'] = limit   # page 2
        rsp = _get_response_json(django_client, request_url, payload)
        if ds_or_pl_children is not None and len(ds_or_pl_children) > 1:
            extra = ds_or_pl_children[limit:limit * 2]
        assert_objects(conn, rsp['data'], children[limit:limit * 2],
                       dtype=dtype, extra=extra)
        assert rsp['meta'] == {'totalCount': 5,
                               'limit': limit,
                               'maxLimit': api_settings.API_MAX_LIMIT,
                               'offset': limit}

    def test_screens(self, user1, user_screens):
        """Test listing of Screens."""
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]
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

    def test_screen_plates_update(self, user1, screen_plates):
        """Test update of Screen doesn't break links to Plate."""
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]
        screen = screen_plates[0]
        plate_count = len(screen.linkedPlateList())
        screen_url = reverse('api_screen',
                             kwargs={'api_version': version,
                                     'object_id': screen.id.val})
        save_url = reverse('api_save', kwargs={'api_version': version})
        # Get Screen, update and save back
        rsp = _get_response_json(django_client, screen_url, {})
        screen_json = rsp['data']
        screen_json['Name'] = 'renamed Screen'
        _csrf_put_json(django_client, save_url, screen_json)

        # Check Screen has been updated and still has child Plates
        scr = conn.getObject('Screen', screen.id.val)
        assert scr.getName() == 'renamed Screen'
        assert len(list(scr.listChildren())) == plate_count

    @pytest.mark.parametrize("dtype", [('project', ProjectI),
                                       ('dataset', DatasetI),
                                       ('screen', ScreenI)])
    def test_container_tags_update(self, user1, dtype):
        """
        Test updating a Object without losing linked Tags.

        If we load a Object without loading Annotations, then update
        and save the Object, we don't want to lose Annotation links
        """
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)

        container = dtype[1]()
        container.name = rstring('test_container_tags_update')
        tag = TagAnnotationI()
        tag.textValue = rstring('tag')
        container.linkAnnotation(tag)
        container = get_update_service(user1).saveAndReturnObject(container)

        version = api_settings.API_VERSIONS[-1]
        object_url = reverse('api_%s' % dtype[0],
                             kwargs={'api_version': version,
                                     'object_id': container.id.val})
        save_url = reverse('api_save', kwargs={'api_version': version})
        # Get container, update and save back
        rsp = _get_response_json(django_client, object_url, {})
        object_json = rsp['data']
        object_json['Name'] = 'renamed container'
        _csrf_put_json(django_client, save_url, object_json)

        # Check container has been updated and still has annotation links
        proj = conn.getObject(dtype[0], container.id.val)
        assert proj.getName() == 'renamed container'
        assert len(list(proj.listAnnotations())) == 1

    def test_spw_urls(self, user1, screen_plates):
        """Test browsing via urls in json /api/->SPW."""
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]
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
        assert_objects(conn, [rsp['data']], [screen], dtype='Screen',
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
        plate_json = rsp['data']
        minMaxIndex = [0, 0]
        links = []
        for idx in range(minMaxIndex[0], minMaxIndex[1]+1):
            l = build_url(client, 'api_plate_wellsampleindex_wells',
                          {'api_version': version,
                           'plate_id': plate_json['@id'],
                           'index': idx})
            links.append(l)
        extra = [{'url:wellsampleindex_wells': links,
                  'omero:wellsampleIndex': minMaxIndex}]
        assert_objects(conn, [plate_json], plates[0:1], dtype='Plate',
                       extra=extra)

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

    def test_spw_parent_urls(self, user1, screen_plates):
        """Test browsing via urls in json /api/image -> well, plate, screen."""
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]
        screen, plate = screen_plates
        plates = screen.linkedPlateList()
        plates.sort(cmp_name_insensitive)

        # Listing wells - all have link to parents
        wells_url = reverse('api_wells', kwargs={'api_version': version})
        rsp = _get_response_json(client, wells_url, {})
        for w in rsp['data']:
            plates_url = build_url(client, 'api_well_plates',
                                   {'api_version': version,
                                    'well_id': w['@id']})
            assert w['url:plates'] == plates_url

        # Single Well has link to parents...
        well_id = rsp['data'][0]['@id']
        well_url = wells_url + '%s/' % well_id
        rsp = _get_response_json(client, well_url, {})
        well_json = rsp['data']
        well_plates_url = build_url(client, 'api_well_plates',
                                    {'api_version': version,
                                     'well_id': well_id})
        assert well_json['url:plates'] == well_plates_url

        # Get parent plate (Plates list, filtered by Well)
        print 'well_plates_url', well_plates_url
        rsp = _get_response_json(client, well_plates_url, {})
        plates_json = rsp['data']
        # check for link to Screen
        screens_url = build_url(client, 'api_plate_screens',
                                {'api_version': version,
                                 'plate_id': plates_json[0]['@id']})
        assert plates_json[0]['url:screens'] == screens_url
        plate_url = plates_json[0]['url:plate']
        assert_objects(conn, plates_json, [plates[0]], dtype='Plate')

        # Get the same Plate by ID
        rsp = _get_response_json(client, plate_url, {})
        assert rsp['data']['url:screens'] == screens_url

        # Get Screen
        rsp = _get_response_json(client, screens_url, {})
        assert_objects(conn, rsp['data'], [screen], dtype='Screen')

    def test_pdi_urls(self, user1, project_datasets):
        """Test browsing via urls in json /api/->PDI."""
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]
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
        assert_objects(conn, [rsp['data']], [project],
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
        assert_objects(conn, [rsp['data']], datasets[0:1], dtype='Dataset',
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
        assert_objects(conn, [rsp['data']], images[0:1], dtype='Image',
                       opts={'load_channels': True})

    def test_pdi_parent_urls(self, user1, project_datasets):
        """Test browsing via urls in json /api/image -> project."""
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]

        # Get image...
        project, dataset = project_datasets
        datasets = project.linkedDatasetList()
        datasets.sort(cmp_name_insensitive)
        # ...from last dataset
        images = datasets[-1].linkedImageList()
        dataset_id = datasets[-1].id.val

        # Listing images - all have link to parents
        imgs_url = reverse('api_images', kwargs={'api_version': version})
        rsp = _get_response_json(client, imgs_url, {'dataset': dataset_id})
        for i in rsp['data']:
            datasets_url = build_url(client, 'api_image_datasets',
                                     {'api_version': version,
                                      'image_id': i['@id']})
            assert i['url:datasets'] == datasets_url

        # Single Image has link to parents...
        img_url = imgs_url + '%s/' % images[0].id.val
        rsp = _get_response_json(client, img_url, {})
        img_json = rsp['data']
        image_datasets_url = build_url(client, 'api_image_datasets',
                                       {'api_version': version,
                                        'image_id': images[0].id.val})
        assert img_json['url:datasets'] == image_datasets_url

        # List parent datasets
        rsp = _get_response_json(client, image_datasets_url, {})
        assert_objects(conn, rsp['data'], [datasets[-1]], dtype='Dataset')

        # Listing Datasets (in Project) - all have link to parents
        datasets_url = reverse('api_datasets', kwargs={'api_version': version})
        rsp = _get_response_json(client, datasets_url,
                                 {'project': project.id.val})
        for d in rsp['data']:
            projects_url = build_url(client, 'api_dataset_projects',
                                     {'api_version': version,
                                      'dataset_id': d['@id']})
            assert d['url:projects'] == projects_url

        # Single Dataset has link to parents...
        dataset_url = datasets_url + '%s/' % dataset_id
        rsp = _get_response_json(client, dataset_url, {})
        dataset_json = rsp['data']
        dataset_projects_url = build_url(client, 'api_dataset_projects',
                                         {'api_version': version,
                                          'dataset_id': dataset_id})
        assert dataset_json['url:projects'] == dataset_projects_url

        # List parent Projects
        rsp = _get_response_json(client, dataset_projects_url, {})
        assert_objects(conn, rsp['data'], [project])
