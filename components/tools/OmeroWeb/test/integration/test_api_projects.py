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

from weblibrary import IWebTest, _get_response_json   # _post_response_json
from django.core.urlresolvers import reverse
from django.conf import settings
import pytest

from omero.gateway import BlitzGateway
from omero.model import ProjectI, DatasetI
from omero.rtypes import unwrap, rstring
from omero_marshal import get_encoder


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


def marshal_objects(objects):
    """ Marshal objects using omero_marshal """
    expected = []
    for obj in objects:
        encoder = get_encoder(obj.__class__)
        expected.append(encoder.encode(obj))
    return expected


def assert_objects(json_objects, omero_objects):
    expected = marshal_objects(omero_objects)
    assert len(json_objects) == len(expected)
    for o1, o2 in zip(json_objects, expected):
        # Ignore 'canLink' on owner and group permissions.
        # Set them to be equal before we do full comparison.
        o1['omero:details']['owner']['omero:details'][
            'permissions']['canLink'] = o2['omero:details']['owner'][
            'omero:details']['permissions']['canLink']
        o1['omero:details']['group']['omero:details'][
            'permissions']['canLink'] = o2['omero:details']['group'][
            'omero:details']['permissions']['canLink']
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

    def test_marshal_projects_no_results(self, userA):
        """
        Test marshalling projects where there are none
        """
        conn = get_connection(userA)
        userName = conn.getUser().getName()
        django_client = self.new_django_client(userName, userName)
        version = settings.WEBGATEWAY_API_VERSIONS[-1]
        request_url = reverse('api_projects', kwargs={'api_version': version})
        rsp = _get_response_json(django_client, request_url, {})
        assert rsp['projects'] == []

    def test_marshal_projects_user(self, userA, projects_userA_groupA):
        """
        Test marshalling user's own projects in current group
        """
        conn = get_connection(userA)
        userName = conn.getUser().getName()
        django_client = self.new_django_client(userName, userName)
        version = settings.WEBGATEWAY_API_VERSIONS[-1]
        request_url = reverse('api_projects', kwargs={'api_version': version})
        rsp = _get_response_json(django_client, request_url, {})
        # json projects won't have Datasets loaded.
        # Unload Datasets from original Projects before we compare
        for p in projects_userA_groupA:
            p.unloadDatasetLinks()
        assert_objects(rsp['projects'], projects_userA_groupA)

    def test_marshal_projects_another_user(self, userA, userB,
                                           projects_userB_groupA):
        """
        Test marshalling another user's projects in current group
        Project is Owned by userB. We are testing userA's perms.
        """
        conn = get_connection(userA)
        userName = conn.getUser().getName()
        django_client = self.new_django_client(userName, userName)
        version = settings.WEBGATEWAY_API_VERSIONS[-1]
        request_url = reverse('api_projects', kwargs={'api_version': version})
        rsp = _get_response_json(django_client, request_url, {})

        # userA reloads userB's projects
        pids = [p.id.val for p in projects_userB_groupA]
        projects = conn.getObjects("Project", pids, respect_order=True)
        projects = [p._obj for p in projects]
        assert_objects(rsp['projects'], projects)
