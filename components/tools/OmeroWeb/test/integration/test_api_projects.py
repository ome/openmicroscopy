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

from weblibrary import IWebTest, _get_response_json, _get_response, \
    _csrf_post_json, _csrf_put_json, _csrf_delete_response_json
from django.core.urlresolvers import reverse
from django.conf import settings
from django.test import Client
import pytest
from omero.gateway import BlitzGateway
from omero.model import ProjectI, DatasetI
from omero.rtypes import unwrap, rstring
from omero_marshal import get_encoder, OME_SCHEMA_URL


def get_update_service(user):
    """
    Get the update_service for the given user's client
    """
    return user[0].getSession().getUpdateService()


def lower_or_none(x):
    """ Lower the case or `None`"""
    if x is not None:
        return x.lower()
    return None


def cmp_name_insensitive(x, y):
    """Case-insensitive name comparator."""
    return cmp(lower_or_none(unwrap(x.name)),
               lower_or_none(unwrap(y.name)))


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


# Some names
@pytest.fixture(scope='module')
def names1(request):
    return ('Apple', 'bat')


@pytest.fixture(scope='module')
def names2(request):
    return ('Axe',)


@pytest.fixture(scope='module')
def names3(request):
    return ('Bark', 'custard')


# Projects
@pytest.fixture(scope='function')
def projects_userA_groupA(request, names1, userA,
                          project_hierarchy_userA_groupA):
    """
    Returns new OMERO Projects with required fields set and with names
    that can be used to exercise sorting semantics.
    """
    to_save = []
    for name in names1:
        project = ProjectI()
        project.name = rstring(name)
        to_save.append(project)
    projects = get_update_service(userA).saveAndReturnArray(to_save)
    projects.extend(project_hierarchy_userA_groupA[:2])
    projects.sort(cmp_name_insensitive)
    return projects


@pytest.fixture(scope='function')
def projects_userB_groupA(request, names2, userB):
    """
    Returns a new OMERO Project with required fields set and with a name
    that can be used to exercise sorting semantics.
    """
    to_save = []
    for name in names2:
        project = ProjectI()
        project.name = rstring(name)
        to_save.append(project)
    projects = get_update_service(userB).saveAndReturnArray(
        to_save)
    projects.sort(cmp_name_insensitive)
    return projects


@pytest.fixture(scope='function')
def projects_userA_groupB(request, names3, userA, groupB):
    """
    Returns new OMERO Projects with required fields set and with names
    that can be used to exercise sorting semantics.
    """
    to_save = []
    for name in names3:
        project = ProjectI()
        project.name = rstring(name)
        to_save.append(project)
    conn = get_connection(userA, groupB.id.val)
    projects = conn.getUpdateService().saveAndReturnArray(to_save,
                                                          conn.SERVICE_OPTS)
    projects.sort(cmp_name_insensitive)
    return projects


@pytest.fixture(scope='function')
def projects_userA(request, projects_userA_groupA,
                   projects_userA_groupB):
    """
    Returns OMERO Projects for userA in both groupA and groupB
    """
    projects = projects_userA_groupA + projects_userA_groupB
    projects.sort(cmp_name_insensitive)
    return projects


def marshal_objects(objects):
    """ Marshal objects using omero_marshal """
    expected = []
    for obj in objects:
        encoder = get_encoder(obj.__class__)
        expected.append(encoder.encode(obj))
    return expected


def assert_objects(conn, json_objects, omero_ids_objects, dtype="Project",
                   group='-1'):
    """
    Load objects from OMERO, via conn.getObjects(), marshal with
    omero_marshal and compare with json_objects.
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


class TestProjects(IWebTest):
    """
    Tests querying & editing Projects
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

    @pytest.fixture()
    def userB(self, groupA):
        """Returns another new user in the read-only group."""
        return self.new_client_and_user(group=groupA)

    @pytest.fixture()
    def project_hierarchy_userA_groupA(self, userA):
        """
        Returns OMERO Projects with Dataset Children with Image Children

        Note: This returns a list of mixed objects in a specified order
        """

        # Create and name all the objects
        projectA = ProjectI()
        projectA.name = rstring('ProjectA')
        projectB = ProjectI()
        projectB.name = rstring('ProjectB')
        datasetA = DatasetI()
        datasetA.name = rstring('DatasetA')
        datasetB = DatasetI()
        datasetB.name = rstring('DatasetB')
        imageA = self.new_image(name='ImageA')
        imageB = self.new_image(name='ImageB')

        # Link them together like so:
        # projectA
        #   datasetA
        #       imageA
        #       imageB
        #   datasetB
        #       imageB
        # projectB
        #   datasetB
        #       imageB
        projectA.linkDataset(datasetA)
        projectA.linkDataset(datasetB)
        projectB.linkDataset(datasetB)
        datasetA.linkImage(imageA)
        datasetA.linkImage(imageB)
        datasetB.linkImage(imageB)

        to_save = [projectA, projectB]
        projects = get_update_service(userA).saveAndReturnArray(to_save)
        projects.sort(cmp_name_insensitive)

        datasets = projects[0].linkedDatasetList()
        datasets.sort(cmp_name_insensitive)

        images = datasets[0].linkedImageList()
        images.sort(cmp_name_insensitive)

        return projects + datasets + images

    def test_marshal_projects_not_logged_in(self):
        """
        Test marshalling projects without log-in
        """
        django_client = Client()
        version = settings.API_VERSIONS[-1]
        request_url = reverse('api_projects', kwargs={'api_version': version})
        rsp = _get_response_json(django_client, request_url, {},
                                 status_code=403)
        assert rsp['message'] == "Not logged in"

    def test_marshal_projects_no_results(self, userA):
        """
        Test marshalling projects where there are none
        """
        conn = get_connection(userA)
        userName = conn.getUser().getName()
        django_client = self.new_django_client(userName, userName)
        version = settings.API_VERSIONS[-1]
        request_url = reverse('api_projects', kwargs={'api_version': version})
        rsp = _get_response_json(django_client, request_url, {})
        assert rsp['data'] == []

    def test_marshal_projects_user(self, userA, projects_userA_groupA):
        """
        Test marshalling user's own projects in current group
        """
        conn = get_connection(userA)
        userName = conn.getUser().getName()
        django_client = self.new_django_client(userName, userName)
        version = settings.API_VERSIONS[-1]
        request_url = reverse('api_projects', kwargs={'api_version': version})
        rsp = _get_response_json(django_client, request_url, {})
        # Reload projects with group '-1' to get same 'canLink' perms
        # on owner and group permissions
        assert_objects(conn, rsp['data'], projects_userA_groupA)

    def test_marshal_projects_another_user(self, userA, userB,
                                           projects_userB_groupA):
        """
        Test marshalling another user's projects in current group
        Project is Owned by userB. We are testing userA's perms.
        """
        conn = get_connection(userA)
        userName = conn.getUser().getName()
        django_client = self.new_django_client(userName, userName)
        version = settings.API_VERSIONS[-1]
        request_url = reverse('api_projects', kwargs={'api_version': version})
        rsp = _get_response_json(django_client, request_url, {})
        # userA reloads userB's projects
        assert_objects(conn, rsp['data'], projects_userB_groupA)

    def test_marshal_projects_another_group(self, userA, groupB,
                                            projects_userA_groupB):
        """
        Test marshalling user's projects in another group
        """
        conn = get_connection(userA)
        userName = conn.getUser().getName()
        django_client = self.new_django_client(userName, userName)
        version = settings.API_VERSIONS[-1]
        request_url = reverse('api_projects', kwargs={'api_version': version})
        rsp = _get_response_json(django_client, request_url, {})

        # Group A is rwra--  Group B is rwr--
        # userA reloads projects with group '-1' so that permissions on owner
        # are same as owner's default group Group A (rwra--) instead of
        # group that the data is in Group B (rwr--)
        assert_objects(conn, rsp['data'], projects_userA_groupB)

    def test_marshal_projects_all_groups(self, userA, groupA, groupB,
                                         projects_userA):
        """
        Test marshalling all projects for a user regardless of group and
        filtering by group.
        """
        conn = get_connection(userA)
        userName = conn.getUser().getName()
        django_client = self.new_django_client(userName, userName)
        version = settings.API_VERSIONS[-1]
        request_url = reverse('api_projects', kwargs={'api_version': version})

        # All groups
        rsp = _get_response_json(django_client, request_url, {})
        assert_objects(conn, rsp['data'], projects_userA)
        # Filter by group A...
        gid = groupA.id.val
        rsp = _get_response_json(django_client, request_url, {'group': gid})
        assert_objects(conn, rsp['data'], projects_userA, group=gid)
        # ...and group B
        gid = groupB.id.val
        rsp = _get_response_json(django_client, request_url, {'group': gid})
        assert_objects(conn, rsp['data'], projects_userA, group=gid)

    def test_marshal_projects_all_users(self, userA, userB,
                                        projects_userA_groupA,
                                        projects_userB_groupA):
        """
        Test marshalling all projects for a group regardless of owner
        and filtering by owner.
        """
        projects = projects_userA_groupA + projects_userB_groupA
        projects.sort(cmp_name_insensitive)
        conn = get_connection(userA)
        userName = conn.getUser().getName()
        django_client = self.new_django_client(userName, userName)
        version = settings.API_VERSIONS[-1]
        request_url = reverse('api_projects', kwargs={'api_version': version})

        # Both users
        rsp = _get_response_json(django_client, request_url, {})
        assert_objects(conn, rsp['data'], projects)

        eid = userA[1].id.val
        rsp = _get_response_json(django_client, request_url, {'owner': eid})
        assert_objects(conn, rsp['data'], projects_userA_groupA)

        eid = userB[1].id.val
        rsp = _get_response_json(django_client, request_url, {'owner': eid})
        assert_objects(conn, rsp['data'], projects_userB_groupA)

    def test_marshal_projects_pagination(self, userA, userB,
                                         projects_userA_groupA,
                                         projects_userB_groupA):
        """
        Test pagination of projects
        """
        projects = projects_userA_groupA + projects_userB_groupA
        projects.sort(cmp_name_insensitive)
        conn = get_connection(userA)
        userName = conn.getUser().getName()
        django_client = self.new_django_client(userName, userName)
        version = settings.API_VERSIONS[-1]
        request_url = reverse('api_projects', kwargs={'api_version': version})

        # First page, just 2 projects. Page = 1 by default
        limit = 2
        rsp = _get_response_json(django_client, request_url, {'limit': limit})
        assert len(rsp['data']) == limit
        assert_objects(conn, rsp['data'], projects[0:limit])

        # Check that page 2 gives next 2 projects
        page = 2
        payload = {'limit': limit, 'page': page}
        rsp = _get_response_json(django_client, request_url, payload)
        assert_objects(conn, rsp['data'], projects[limit:limit * page])

    def test_marshal_projects_params(self, userA, userB,
                                     projects_userA_groupA,
                                     projects_userB_groupA):
        """
        Tests normalize, childCount and callback params of projects
        """
        projects = projects_userA_groupA + projects_userB_groupA
        projects.sort(cmp_name_insensitive)
        conn = get_connection(userA)
        userName = conn.getUser().getName()
        django_client = self.new_django_client(userName, userName)
        version = settings.API_VERSIONS[-1]
        request_url = reverse('api_projects', kwargs={'api_version': version})

        # Test 'childCount' parameter
        payload = {'childCount': True}
        rsp = _get_response_json(django_client, request_url, payload)
        childCounts = [p['omero:childCount'] for p in rsp['data']]
        assert childCounts == [0, 0, 0, 2, 1]

        # make dict of owners and groups to use in next test...
        owners = {}
        groups = {}
        for p in rsp['data']:
            details = p['omero:details']
            owner = details['owner']
            group = details['group']
            owners[owner['@id']] = owner
            groups[group['@id']] = group

        # Test 'normalize' parameter.
        payload = {'normalize': True}
        rsp = _get_response_json(django_client, request_url, payload)
        for p in rsp['data']:
            details = p['omero:details']
            owner = details['owner']
            group = details['group']
            # owner and group have @id only
            assert owner.keys() == ['@id']
            assert group.keys() == ['@id']
        # check normaliszed owners and groups are same as before
        rsp_owners = {}
        for o in rsp['experimenters']:
            rsp_owners[o['@id']] = o
        rsp_groups = {}
        for g in rsp['experimenterGroups']:
            rsp_groups[g['@id']] = g
        assert owners == rsp_owners
        assert groups == rsp_groups

        # Test 'callback' parameter
        payload = {'callback': 'callback'}
        rsp = _get_response(django_client, request_url, payload,
                            status_code=200)
        assert rsp.get('Content-Type') == 'application/javascript'
        assert rsp.content.startswith('callback(')

    def test_project_create_read(self):
        django_client = self.django_root_client
        version = settings.API_VERSIONS[-1]
        # Need to get the Schema url to create @type
        base_url = reverse('api_base', kwargs={'api_version': version})
        rsp = _get_response_json(django_client, base_url, {})
        schema_url = rsp['schema_url']
        save_url = rsp['save_url']
        projects_url = rsp['projects_url']
        projectName = 'test_api_projects'
        payload = {'Name': projectName,
                   '@type': schema_url + '#Project'}
        rsp = _csrf_post_json(django_client, save_url, payload,
                              status_code=200)
        # We get the complete new Project returned
        assert rsp['Name'] == projectName
        projectId = rsp['@id']

        # Read Project
        project_url = "%s%s/" % (projects_url, projectId)
        rsp = _get_response_json(django_client, project_url, {})
        assert rsp['@id'] == projectId
        conn = BlitzGateway(client_obj=self.root)
        assert_objects(conn, [rsp], [projectId])

    def test_project_create_other_group(self, userA, projects_userA_groupB):
        """
        Test saving to non-default group
        """
        conn = get_connection(userA)
        userName = conn.getUser().getName()
        django_client = self.new_django_client(userName, userName)
        version = settings.API_VERSIONS[-1]
        # We're only using projects_userA_groupB to get groupB id
        groupBid = projects_userA_groupB[0].getDetails().group.id.val
        # This seems to be the minimum details needed to pass group ID
        groupBdetails = {'group': {
                         '@id': groupBid,
                         '@type': OME_SCHEMA_URL + '#ExperimenterGroup'
                         },
                         '@type': 'TBD#Details'}
        save_url = reverse('api_save', kwargs={'api_version': version})
        projectName = 'test_project_create_group'
        payload = {'Name': projectName,
                   '@type': OME_SCHEMA_URL + '#Project',
                   'omero:details': groupBdetails}
        rsp = _csrf_post_json(django_client, save_url, payload,
                              status_code=200)
        newProjectId = rsp['@id']
        assert rsp['omero:details']['group']['@id'] == groupBid
        # Read Project
        project_url = reverse('api_project', kwargs={'api_version': version,
                                                     'pid': newProjectId})
        rsp = _get_response_json(django_client, project_url, {})
        assert rsp['omero:details']['group']['@id'] == groupBid

    def test_project_update(self, userA):
        conn = get_connection(userA)
        userName = conn.getUser().getName()
        django_client = self.new_django_client(userName, userName)

        project = ProjectI()
        project.name = rstring('test_project_update')
        project.description = rstring('Test update')
        project = get_update_service(userA).saveAndReturnObject(project)

        # Update Project in 2 ways...
        version = settings.API_VERSIONS[-1]
        project_url = reverse('api_project', kwargs={'api_version': version,
                                                     'pid': project.id.val})
        save_url = reverse('api_save', kwargs={'api_version': version})
        # 1) Get Project, update and save back
        project_json = _get_response_json(django_client, project_url, {})
        assert project_json['Name'] == 'test_project_update'
        project_json['Name'] = 'new name'
        rsp = _csrf_put_json(django_client, save_url, project_json)
        assert rsp['@id'] == project.id.val
        assert rsp['Name'] == 'new name'    # Name has changed
        assert rsp['Description'] == 'Test update'  # No change

        # 2) Put from scratch (will delete empty fields, E.g. Description)
        payload = {'Name': 'updated name',
                   '@id': project.id.val}
        # Test error message if we don't pass @type:
        rsp = _csrf_put_json(django_client, save_url, payload)
        assert rsp['message'] == 'Need to specify @type attribute'
        # Add @type and try again
        payload['@type'] = project_json['@type']
        rsp = _csrf_put_json(django_client, save_url, payload)
        assert rsp['@id'] == project.id.val
        assert rsp['Name'] == 'updated name'
        # Description should be None, but is an empty string
        # See https://github.com/openmicroscopy/omero-marshal/issues/18
        # assert 'Description' not in rsp
        assert rsp['Description'] == ''
        # Get project again to check update
        prJson = _get_response_json(django_client, project_url, {})
        assert prJson['Name'] == 'updated name'
        # assert 'Description' not in prJson
        assert prJson['Description'] == ''

    def test_project_delete(self, userA):
        conn = get_connection(userA)
        userName = conn.getUser().getName()
        django_client = self.new_django_client(userName, userName)

        project = ProjectI()
        project.name = rstring('test_project_delete')
        project.description = rstring('Test update')
        project = get_update_service(userA).saveAndReturnObject(project)
        version = settings.API_VERSIONS[-1]
        project_url = reverse('api_project', kwargs={'api_version': version,
                                                     'pid': project.id.val})
        # Before delete, we can read
        prJson = _get_response_json(django_client, project_url, {})
        assert prJson['Name'] == 'test_project_delete'
        # Delete
        _csrf_delete_response_json(django_client, project_url, {})
        # Get should now return 404
        rsp = _get_response_json(django_client, project_url, {},
                                 status_code=404)
        assert rsp['message'] == 'Project %s not found' % project.id.val
        # Delete (again) should return 404
        rsp = _csrf_delete_response_json(django_client, project_url, {},
                                         status_code=404)
        assert rsp['message'] == 'Project %s not found' % project.id.val
        save_url = reverse('api_save', kwargs={'api_version': version})
        # TODO: Try to save deleted object - should return 404
        # see https://trello.com/c/qWNt9vLN/178-save-deleted-object
        with pytest.raises(AssertionError):
            rsp = _csrf_put_json(django_client, save_url, prJson,
                                 status_code=404)
            assert rsp['message'] == 'Project %s not found' % project.id.val
