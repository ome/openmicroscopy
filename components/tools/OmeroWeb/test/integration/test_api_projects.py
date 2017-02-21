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

from omeroweb.testlib import IWebTest, _get_response_json, \
    _csrf_post_json, _csrf_put_json, _csrf_delete_response_json
from django.core.urlresolvers import reverse
from omeroweb.api import api_settings
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
def projects_user1_group1(request, names1, user1,
                          project_hierarchy_user1_group1):
    """
    Returns new OMERO Projects with required fields set and with names
    that can be used to exercise sorting semantics.
    """
    to_save = []
    for name in names1:
        project = ProjectI()
        project.name = rstring(name)
        to_save.append(project)
    projects = get_update_service(user1).saveAndReturnArray(to_save)
    projects.extend(project_hierarchy_user1_group1[:2])
    projects.sort(cmp_name_insensitive)
    return projects


@pytest.fixture(scope='function')
def projects_user2_group1(request, names2, user2):
    """
    Returns a new OMERO Project with required fields set and with a name
    that can be used to exercise sorting semantics.
    """
    to_save = []
    for name in names2:
        project = ProjectI()
        project.name = rstring(name)
        to_save.append(project)
    projects = get_update_service(user2).saveAndReturnArray(
        to_save)
    projects.sort(cmp_name_insensitive)
    return projects


@pytest.fixture(scope='function')
def projects_user1_group2(request, names3, user1, group2):
    """
    Returns new OMERO Projects with required fields set and with names
    that can be used to exercise sorting semantics.
    """
    to_save = []
    for name in names3:
        project = ProjectI()
        project.name = rstring(name)
        to_save.append(project)
    conn = get_connection(user1, group2.id.val)
    projects = conn.getUpdateService().saveAndReturnArray(to_save,
                                                          conn.SERVICE_OPTS)
    projects.sort(cmp_name_insensitive)
    return projects


@pytest.fixture(scope='function')
def projects_user1(request, projects_user1_group1,
                   projects_user1_group2):
    """
    Returns OMERO Projects for user1 in both group1 and group2
    """
    projects = projects_user1_group1 + projects_user1_group2
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
        # remove any urls from json
        for key in o1.keys():
            if key.startswith('url:'):
                del(o1[key])
        assert o1 == o2


class TestProjects(IWebTest):
    """
    Tests querying & editing Projects
    """

    # Create a read-annotate group
    @pytest.fixture(scope='function')
    def group1(self):
        """Returns a new read-only group."""
        return self.new_group(perms='rwra--')

    # Create a read-only group
    @pytest.fixture(scope='function')
    def group2(self):
        """Returns a new read-only group."""
        return self.new_group(perms='rwr---')

    # Create users in the read-only group
    @pytest.fixture()
    def user1(self, group1, group2):
        """Returns a new user in the group1 group and also add to group2"""
        user = self.new_client_and_user(group=group1)
        self.add_groups(user[1], [group2])
        return user

    @pytest.fixture()
    def user2(self, group1):
        """Returns another new user in the read-only group."""
        return self.new_client_and_user(group=group1)

    @pytest.fixture()
    def project_hierarchy_user1_group1(self, user1):
        """
        Returns OMERO Projects with Dataset Children with Image Children

        Note: This returns a list of mixed objects in a specified order
        """

        # Create and name all the objects
        project1 = ProjectI()
        project1.name = rstring('Project1')
        project2 = ProjectI()
        project2.name = rstring('Project2')
        dataset1 = DatasetI()
        dataset1.name = rstring('Dataset1')
        dataset2 = DatasetI()
        dataset2.name = rstring('Dataset2')
        image1 = self.new_image(name='Image1')
        image2 = self.new_image(name='Image2')

        # Link them together like so:
        # project1
        #   dataset1
        #       image1
        #       image2
        #   dataset2
        #       image2
        # project2
        #   dataset2
        #       image2
        project1.linkDataset(dataset1)
        project1.linkDataset(dataset2)
        project2.linkDataset(dataset2)
        dataset1.linkImage(image1)
        dataset1.linkImage(image2)
        dataset2.linkImage(image2)

        to_save = [project1, project2]
        projects = get_update_service(user1).saveAndReturnArray(to_save)
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
        version = api_settings.API_VERSIONS[-1]
        request_url = reverse('api_projects', kwargs={'api_version': version})
        rsp = _get_response_json(django_client, request_url, {},
                                 status_code=403)
        assert rsp['message'] == "Not logged in"

    def test_marshal_projects_no_results(self, user1):
        """
        Test marshalling projects where there are none
        """
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]
        request_url = reverse('api_projects', kwargs={'api_version': version})
        rsp = _get_response_json(django_client, request_url, {})
        assert rsp['data'] == []

    def test_marshal_projects_user(self, user1, projects_user1_group1):
        """
        Test marshalling user's own projects in current group
        """
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]
        request_url = reverse('api_projects', kwargs={'api_version': version})
        rsp = _get_response_json(django_client, request_url, {})
        assert_objects(conn, rsp['data'], projects_user1_group1)
        assert rsp['meta']['totalCount'] == len(rsp['data'])

    def test_marshal_projects_another_user(self, user1, user2,
                                           projects_user2_group1):
        """
        Test marshalling another user's projects in current group
        Project is Owned by user2. We are testing user1's perms.
        """
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]
        request_url = reverse('api_projects', kwargs={'api_version': version})
        rsp = _get_response_json(django_client, request_url, {})
        # user1 reloads user2's projects
        assert_objects(conn, rsp['data'], projects_user2_group1)
        assert rsp['meta']['totalCount'] == len(rsp['data'])

    def test_marshal_projects_another_group(self, user1, group2,
                                            projects_user1_group2):
        """
        Test marshalling user's projects in another group
        """
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]
        request_url = reverse('api_projects', kwargs={'api_version': version})
        rsp = _get_response_json(django_client, request_url, {})

        # Group 1 is rwra--  Group 2 is rwr--
        # user1 reloads projects with group '-1' so that permissions on owner
        # are same as owner's default group Group 1 (rwra--) instead of
        # group that the data is in Group 2 (rwr--)
        assert_objects(conn, rsp['data'], projects_user1_group2)
        assert rsp['meta']['totalCount'] == len(rsp['data'])

    def test_marshal_projects_all_groups(self, user1, group1, group2,
                                         projects_user1):
        """
        Test marshalling all projects for a user regardless of group and
        filtering by group.
        """
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]
        request_url = reverse('api_projects', kwargs={'api_version': version})

        # All groups
        rsp = _get_response_json(django_client, request_url, {})
        assert_objects(conn, rsp['data'], projects_user1)
        assert rsp['meta']['totalCount'] == len(projects_user1)
        # Filter by group A...
        gid = group1.id.val
        rsp = _get_response_json(django_client, request_url, {'group': gid})
        assert_objects(conn, rsp['data'], projects_user1, group=gid)
        assert rsp['meta']['totalCount'] == len(rsp['data'])
        # ...and group B
        gid = group2.id.val
        rsp = _get_response_json(django_client, request_url, {'group': gid})
        assert_objects(conn, rsp['data'], projects_user1, group=gid)
        assert rsp['meta']['totalCount'] == len(rsp['data'])

    def test_marshal_projects_all_users(self, user1, user2,
                                        projects_user1_group1,
                                        projects_user2_group1):
        """
        Test marshalling all projects for a group regardless of owner
        and filtering by owner.
        """
        projects = projects_user1_group1 + projects_user2_group1
        projects.sort(cmp_name_insensitive)
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]
        request_url = reverse('api_projects', kwargs={'api_version': version})

        # Both users
        rsp = _get_response_json(django_client, request_url, {})
        assert_objects(conn, rsp['data'], projects)
        assert rsp['meta']['totalCount'] == len(projects)

        eid = user1[1].id.val
        rsp = _get_response_json(django_client, request_url, {'owner': eid})
        assert_objects(conn, rsp['data'], projects_user1_group1)
        assert rsp['meta']['totalCount'] == len(projects_user1_group1)

        eid = user2[1].id.val
        rsp = _get_response_json(django_client, request_url, {'owner': eid})
        assert_objects(conn, rsp['data'], projects_user2_group1)
        assert rsp['meta']['totalCount'] == len(projects_user2_group1)

    def test_marshal_projects_pagination(self, user1, user2,
                                         projects_user1_group1,
                                         projects_user2_group1):
        """
        Test pagination of projects
        """
        projects = projects_user1_group1 + projects_user2_group1
        projects.sort(cmp_name_insensitive)
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]
        request_url = reverse('api_projects', kwargs={'api_version': version})

        # Test a range of limits. offset = 0 by default
        for limit in range(1, 4):
            rsp = _get_response_json(django_client, request_url,
                                     {'limit': limit})
            assert len(rsp['data']) == limit
            assert_objects(conn, rsp['data'], projects[0:limit])
            assert rsp['meta']['totalCount'] == len(projects)

            # Check that page 2 gives next n projects
            payload = {'limit': limit, 'offset': limit}
            rsp = _get_response_json(django_client, request_url, payload)
            assert_objects(conn, rsp['data'], projects[limit:limit * 2])
            assert rsp['meta']['totalCount'] == len(projects)

    def test_marshal_projects_params(self, user1, user2,
                                     projects_user1_group1,
                                     projects_user2_group1):
        """
        Tests normalize and childCount params of projects
        """
        projects = projects_user1_group1 + projects_user2_group1
        projects.sort(cmp_name_insensitive)
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]
        request_url = reverse('api_projects', kwargs={'api_version': version})

        # Test 'childCount' parameter
        payload = {'childCount': 'true'}
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
        payload = {'normalize': 'true'}
        rsp = _get_response_json(django_client, request_url, payload)
        assert rsp['meta']['totalCount'] == len(rsp['data'])
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

    def test_project_create_read(self):
        """
        Tests creation by POST to /save and reading with GET of /project/:id/
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
        projects_url = rsp['url:projects']
        project_name = 'test_api_projects'
        payload = {'Name': project_name,
                   '@type': schema_url + '#Project'}
        rsp = _csrf_post_json(django_client, save_url, payload,
                              status_code=201)
        new_proj = rsp['data']
        # We get the complete new Project returned
        assert new_proj['Name'] == project_name
        project_id = new_proj['@id']

        # Read Project
        project_url = "%s%s/" % (projects_url, project_id)
        rsp = _get_response_json(django_client, project_url, {})
        new_proj = rsp['data']
        assert new_proj['@id'] == project_id
        conn = BlitzGateway(client_obj=self.root)
        assert_objects(conn, [new_proj], [project_id])

    def test_project_create_other_group(self, user1, projects_user1_group2):
        """
        Test saving to non-default group
        """
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]
        # We're only using projects_user1_group2 to get group2 id
        group2_id = projects_user1_group2[0].getDetails().group.id.val
        # This seems to be the minimum details needed to pass group ID
        group2_details = {'group': {
                          '@id': group2_id,
                          '@type': OME_SCHEMA_URL + '#ExperimenterGroup'
                          },
                          '@type': 'TBD#Details'}
        save_url = reverse('api_save', kwargs={'api_version': version})
        project_name = 'test_project_create_group'
        payload = {'Name': project_name,
                   '@type': OME_SCHEMA_URL + '#Project'}
        # Saving fails with NO group specified
        rsp = _csrf_post_json(django_client, save_url, payload,
                              status_code=400)
        assert rsp['message'] == ("Specify Group in omero:details or "
                                  "query parameters ?group=:id")
        # Add group details and try again
        payload['omero:details'] = group2_details
        rsp = _csrf_post_json(django_client, save_url, payload,
                              status_code=201)
        new_proj = rsp['data']
        new_project_id = new_proj['@id']
        assert new_proj['omero:details']['group']['@id'] == group2_id
        # Read Project
        project_url = reverse('api_project',
                              kwargs={'api_version': version,
                                      'object_id': new_project_id})
        rsp = _get_response_json(django_client, project_url, {})
        assert rsp['data']['omero:details']['group']['@id'] == group2_id

    def test_project_update(self, user1):
        conn = get_connection(user1)
        group = conn.getEventContext().groupId
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)

        project = ProjectI()
        project.name = rstring('test_project_update')
        project.description = rstring('Test update')
        project = get_update_service(user1).saveAndReturnObject(project)

        # Update Project in 2 ways...
        version = api_settings.API_VERSIONS[-1]
        project_url = reverse('api_project',
                              kwargs={'api_version': version,
                                      'object_id': project.id.val})
        save_url = reverse('api_save', kwargs={'api_version': version})
        # 1) Get Project, update and save back
        rsp = _get_response_json(django_client, project_url, {})
        project_json = rsp['data']
        assert project_json['Name'] == 'test_project_update'
        project_json['Name'] = 'new name'
        rsp = _csrf_put_json(django_client, save_url, project_json)
        project_json = rsp['data']
        assert project_json['@id'] == project.id.val
        assert project_json['Name'] == 'new name'    # Name has changed
        assert project_json['Description'] == 'Test update'  # No change

        # 2) Put from scratch (will delete empty fields, E.g. Description)
        save_url += '?group=' + str(group)
        payload = {'Name': 'updated name',
                   '@id': project.id.val}
        # Test error message if we don't pass @type:
        rsp = _csrf_put_json(django_client, save_url, payload, status_code=400)
        assert rsp['message'] == 'Need to specify @type attribute'
        # Add @type and try again
        payload['@type'] = project_json['@type']
        rsp = _csrf_put_json(django_client, save_url, payload)
        project_json = rsp['data']
        assert project_json['@id'] == project.id.val
        assert project_json['Name'] == 'updated name'
        assert 'Description' not in project_json
        # Get project again to check update
        rsp = _get_response_json(django_client, project_url, {})
        pr_json = rsp['data']
        assert pr_json['Name'] == 'updated name'
        assert 'Description' not in pr_json
        # Now add description and save again
        pr_json['Description'] = 'New test description update'
        _csrf_put_json(django_client, save_url, pr_json)
        # Read to check
        rsp = _get_response_json(django_client, project_url, {})
        assert rsp['data']['Description'] == 'New test description update'

    def test_project_delete(self, user1):
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)

        project = ProjectI()
        project.name = rstring('test_project_delete')
        project.description = rstring('Test update')
        project = get_update_service(user1).saveAndReturnObject(project)
        version = api_settings.API_VERSIONS[-1]
        project_url = reverse('api_project',
                              kwargs={'api_version': version,
                                      'object_id': project.id.val})
        # Before delete, we can read
        pr_json = _get_response_json(django_client, project_url, {})
        assert pr_json['data']['Name'] == 'test_project_delete'
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
        # TODO: Try to save deleted object - should return ApiException
        # see https://trello.com/c/qWNt9vLN/178-save-deleted-object
        with pytest.raises(AssertionError):
            rsp = _csrf_put_json(django_client, save_url, pr_json,
                                 status_code=400)
            assert rsp['message'] == 'Project %s not found' % project.id.val
