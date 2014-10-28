#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2014 Glencoe Software, Inc.
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
Simple integration tests for the "tree" module.
"""

import pytest
import test.integration.library as lib

from omero.gateway import BlitzGateway
from omero.model import ProjectI
from omero.rtypes import rstring
from omeroweb.webclient.tree import marshal_projects


def cmp_id(x, y):
    """Identifier comparator."""
    return cmp(x.id.val, y.id.val)


def cmp_name(x, y):
    """Name comparator."""
    return cmp(x.name.val, y.name.val)


def cmp_name_insensitive(x, y):
    """Case-insensitive name comparator."""
    return cmp(x.name.val.lower(), y.name.val.lower())


def cmp_omename_insensitive(x, y):
    """Case-insensitive name comparator."""
    print x.omeName.val.lower(), y.omeName.val.lower()
    return cmp(x.omeName.val.lower(), y.omeName.val.lower())


def get_connection(user):
    """
    Get a BlitzGateway connection for the given user's client
    """
    connection = BlitzGateway(client_obj=user[0])
    connection.SERVICE_OPTS.setOmeroGroup(-1)
    return connection


def get_update_service(user):
    """
    Get the update_service for the given user's client
    """
    return user[0].getSession().getUpdateService()


@pytest.fixture(scope='function')
def itest(request):
    """
    Returns a new L{test.integration.library.ITest} instance.  With
    attached finalizer so that pytest will clean it up.
    """
    o = lib.ITest()
    o.setup_method(None)

    def finalizer():
        o.teardown_method(None)
    request.addfinalizer(finalizer)
    return o


# Create groups of the possible types
@pytest.fixture(scope='function')
def group_private(request, itest):
    """Returns a new private group."""
    return itest.new_group(perms='rw----')


@pytest.fixture(scope='function')
def group_read_only(request, itest):
    """Returns a new read-only group."""
    return itest.new_group(perms='rwr---')


@pytest.fixture(scope='function')
def group_read_annotate(request, itest):
    """Returns a new read-annotate group."""
    return itest.new_group(perms='rwra--')


# Create user in the possible group types
@pytest.fixture(scope='function')
def user_private(request, itest, group_private):
    """Returns a new user in the private group."""
    return itest.new_client_and_user(group=group_private)


@pytest.fixture(scope='function')
def user_read_only(request, itest, group_read_only):
    """Returns a new user in the read-only group."""
    return itest.new_client_and_user(group=group_read_only)


@pytest.fixture(scope='function')
def user_read_annotate(request, itest, group_read_annotate):
    """Returns a user in the read-annotate group."""
    return itest.new_client_and_user(group=group_read_annotate)


# Create another user in the possible group types
@pytest.fixture(scope='function')
def user_other_private(request, itest, group_private):
    """Returns another new user in the private group."""
    return itest.new_client_and_user(group=group_private)


@pytest.fixture(scope='function')
def user_other_read_only(request, itest, group_read_only):
    """Returns another new user in the read-only group."""
    return itest.new_client_and_user(group=group_read_only)


@pytest.fixture(scope='function')
def user_other_read_annotate(request, itest, group_read_annotate):
    """Returns another user in the read-annotate group."""
    return itest.new_client_and_user(group=group_read_annotate)


# Create an owner of the possible group types
@pytest.fixture(scope='function')
def user_owner_private(request, itest, group_private):
    """Returns an owner user in the private group."""
    return itest.new_client_and_user(group=group_private, admin=True)


@pytest.fixture(scope='function')
def user_owner_read_only(request, itest, group_read_only):
    """Returns an owner user in the read-only group."""
    return itest.new_client_and_user(group=group_read_only, admin=True)


@pytest.fixture(scope='function')
def user_owner_read_annotate(request, itest, group_read_annotate):
    """Returns an owner user in the read-annotate group."""
    return itest.new_client_and_user(group=group_read_annotate, admin=True)


# Collection of users for ach group type
@pytest.fixture(scope='function')
def user_all_private(request, user_private, user_other_private,
                     user_owner_private):
    """Returns all users in the private group."""
    return [user_private, user_other_private, user_owner_private]


@pytest.fixture(scope='function')
def user_all_read_only(request, user_read_only, user_other_read_only,
                       user_owner_read_only):
    """Returns all users in the read-only group."""
    return [user_read_only, user_other_read_only, user_owner_read_only]


@pytest.fixture(scope='function')
def user_all_read_annotate(request, user_read_annotate,
                           user_other_read_annotate,
                           user_owner_read_annotate):
    """Returns all users in the read-annotate group."""
    return [user_read_annotate, user_other_read_annotate,
            user_owner_read_annotate]


# Create an admin user which is not in any other significant groups
@pytest.fixture(scope='function')
def user_admin(request, itest):
    """Returns an owner user in the read-annotate group."""
    return itest.new_client_and_user(system=True)


# Some names
@pytest.fixture(scope='module')
def names(request):
    return ('Apple', 'bat', 'atom', 'Butter')


# Some more names
@pytest.fixture(scope='module')
def names_other(request):
    return ('Axe',)


# Projects for the various groups, these belong to the ordinary users
@pytest.fixture(scope='function')
def projects_private(request, names, user_private):
    """
    Returns four new OMERO Projects with required fields set and with names
    that can be used to exercise sorting semantics.
    """
    to_save = []
    for name in names:
        project = ProjectI()
        project.name = rstring(name)
        to_save.append(project)
    return get_update_service(user_private).saveAndReturnArray(to_save)


@pytest.fixture(scope='function')
def projects_read_only(request, names, user_read_only):
    """
    Returns four new OMERO Projects with required fields set and with names
    that can be used to exercise sorting semantics.
    """
    to_save = []
    for name in names:
        project = ProjectI()
        project.name = rstring(name)
        to_save.append(project)
    return get_update_service(user_read_only).saveAndReturnArray(to_save)


@pytest.fixture(scope='function')
def projects_read_annotate(request, names, user_read_annotate):
    """
    Returns four new OMERO Projects with required fields set and with names
    that can be used to exercise sorting semantics.
    """
    to_save = []
    for name in names:
        project = ProjectI()
        project.name = rstring(name)
        to_save.append(project)
    return get_update_service(user_read_annotate).saveAndReturnArray(to_save)


# Projects for the various groups, these belong to the other users
@pytest.fixture(scope='function')
def projects_other_private(request, names_other, user_other_private):
    """
    Returns a new OMERO Projects with required fields set and with a name
    that can be used to exercise sorting semantics.
    """
    to_save = []
    for name in names_other:
        project = ProjectI()
        project.name = rstring(name)
        to_save.append(project)
    return get_update_service(user_other_private).saveAndReturnArray(to_save)


@pytest.fixture(scope='function')
def projects_other_read_only(request, names_other, user_other_read_only):
    """
    Returns a new OMERO Projects with required fields set and with a name
    that can be used to exercise sorting semantics.
    """
    to_save = []
    for name in names_other:
        project = ProjectI()
        project.name = rstring(name)
        to_save.append(project)
    return get_update_service(user_other_read_only).saveAndReturnArray(to_save)


@pytest.fixture(scope='function')
def projects_other_read_annotate(request, names_other,
                                 user_other_read_annotate):
    """
    Returns a new OMERO Projects with required fields set and with a name
    that can be used to exercise sorting semantics.
    """
    to_save = []
    for name in names_other:
        project = ProjectI()
        project.name = rstring(name)
        to_save.append(project)
    return get_update_service(
        user_other_read_annotate).saveAndReturnArray(to_save)


# All projects for the various groups, irrespective of user
@pytest.fixture(scope='function')
def projects_all_private(request, projects_private,
                         projects_other_private):
    """
    Returns OMERO Projects for both users in private group
    """
    return projects_private + projects_other_private


@pytest.fixture(scope='function')
def projects_all_read_only(request, projects_read_only,
                           projects_other_read_only):
    """
    Returns OMERO Projects for both users in read-only group
    """
    return projects_read_only + projects_other_read_only


@pytest.fixture(scope='function')
def projects_all_read_annotate(request, projects_read_annotate,
                               projects_other_read_annotate):
    """
    Returns OMERO Projects for both users in read-annotate group
    """
    return projects_read_annotate + projects_other_read_annotate


class TestTree(object):
    """
    Tests to ensure that OMERO.web "tree" infrastructure is working
    correctly.

    These tests make __extensive__ use of pytest fixtures.  In particular
    the scoping semantics allowing re-use of instances populated by the
    *request fixtures.  It is recommended that the pytest fixture
    documentation be studied in detail before modifications or attempts to
    fix failing tests are made:

     * https://pytest.org/latest/fixture.html
    """

    def test_marshal_projects_no_results(self, user_private):
        '''
        Test marshalling projects where there are none
        '''
        conn = get_connection(user_private)
        assert marshal_projects(conn, -1) == []

    def test_marshal_projects(self, user_private, projects_private):
        """
        Test marshalling user's own projects
        """
        conn = get_connection(user_private)
        project_a, project_b, project_c, project_d = projects_private
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        # Order is important to test desired HQL sorting semantics.
        expected = [{
            'id': project_a.id.val,
            'name': 'Apple',
            'isOwned': True,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_c.id.val,
            'name': 'atom',
            'isOwned': True,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_b.id.val,
            'name': 'bat',
            'isOwned': True,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_d.id.val,
            'name': 'Butter',
            'isOwned': True,
            'childCount': 0,
            'permsCss': perms_css
        }]

        marshaled = marshal_projects(conn, user_private[1].id)
        assert marshaled == expected

    def test_marshal_projects_group_private(self, user_private,
                                            user_other_private):
        """
        Test marshalling another user's projects in a private group
        """
        conn = get_connection(user_other_private)
        expected = []

        marshaled = marshal_projects(conn, user_private[1].id)
        assert marshaled == expected

    # TODO Known to fail
    def test_marshal_projects_group_read_only(self, user_read_only,
                                              user_other_read_only,
                                              projects_read_only):
        """
        Test marshalling another user's projects in a read-only group
        """
        conn = get_connection(user_other_read_only)
        project_a, project_b, project_c, project_d = projects_read_only
        perms_css = ''
        # Order is important to test desired HQL sorting semantics.
        expected = [{
            'id': project_a.id.val,
            'name': 'Apple',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_c.id.val,
            'name': 'atom',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_b.id.val,
            'name': 'bat',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_d.id.val,
            'name': 'Butter',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }]

        marshaled = marshal_projects(conn, user_read_only[1].id)
        assert marshaled == expected

    # TODO Known to fail
    def test_marshal_projects_group_read_annotate(self, user_read_annotate,
                                                  user_other_read_annotate,
                                                  projects_read_annotate):
        """
        Test marshalling another user's projects in a read-annotate group
        """
        conn = get_connection(user_other_read_annotate)
        project_a, project_b, project_c, project_d = projects_read_annotate
        perms_css = 'canAnnotate'
        # Order is important to test desired HQL sorting semantics.
        expected = [{
            'id': project_a.id.val,
            'name': 'Apple',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_c.id.val,
            'name': 'atom',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_b.id.val,
            'name': 'bat',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_d.id.val,
            'name': 'Butter',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }]

        marshaled = marshal_projects(conn, user_read_annotate[1].id)
        assert marshaled == expected

    # TODO Known to fail
    def test_marshal_projects_group_private_owner(
            self, user_private, user_owner_private, projects_private):
        """
        Test marshalling another user's projects in a private group as
        the owner
        """
        conn = get_connection(user_owner_private)
        project_a, project_b, project_c, project_d = projects_private
        perms_css = 'canEdit canDelete'
        # Order is important to test desired HQL sorting semantics.
        expected = [{
            'id': project_a.id.val,
            'name': 'Apple',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_c.id.val,
            'name': 'atom',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_b.id.val,
            'name': 'bat',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_d.id.val,
            'name': 'Butter',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }]

        marshaled = marshal_projects(conn, user_private[1].id)
        assert marshaled == expected

    def test_marshal_projects_group_read_only_owner(
            self, user_read_only, user_owner_read_only, projects_read_only):
        """
        Test marshalling another user's projects in a read-only group as
        the owner
        """
        conn = get_connection(user_owner_read_only)
        project_a, project_b, project_c, project_d = projects_read_only
        perms_css = 'canEdit canAnnotate canLink canDelete'
        # Order is important to test desired HQL sorting semantics.
        expected = [{
            'id': project_a.id.val,
            'name': 'Apple',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_c.id.val,
            'name': 'atom',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_b.id.val,
            'name': 'bat',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_d.id.val,
            'name': 'Butter',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }]

        marshaled = marshal_projects(conn, user_read_only[1].id)
        assert marshaled == expected

    def test_marshal_projects_group_read_annotate_owner(
            self, user_read_annotate, user_owner_read_annotate,
            projects_read_annotate):
        """
        Test marshalling another user's projects in a read-annotate group as
        the owner
        """
        conn = get_connection(user_owner_read_annotate)
        project_a, project_b, project_c, project_d = projects_read_annotate
        perms_css = 'canEdit canAnnotate canLink canDelete'
        # Order is important to test desired HQL sorting semantics.
        expected = [{
            'id': project_a.id.val,
            'name': 'Apple',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_c.id.val,
            'name': 'atom',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_b.id.val,
            'name': 'bat',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_d.id.val,
            'name': 'Butter',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }]

        marshaled = marshal_projects(conn, user_read_annotate[1].id)
        assert marshaled == expected

    def test_marshal_projects_group_private_admin(
            self, user_private, user_admin, projects_private):
        """
        Test marshalling another user's projects in a private group as
        an admin
        """
        conn = get_connection(user_admin)
        project_a, project_b, project_c, project_d = projects_private
        perms_css = 'canEdit canDelete canChgrp'
        # Order is important to test desired HQL sorting semantics.
        expected = [{
            'id': project_a.id.val,
            'name': 'Apple',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_c.id.val,
            'name': 'atom',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_b.id.val,
            'name': 'bat',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_d.id.val,
            'name': 'Butter',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }]

        marshaled = marshal_projects(conn, user_private[1].id)
        assert marshaled == expected

    def test_marshal_projects_group_read_only_admin(
            self, user_read_only, user_admin, projects_read_only):
        """
        Test marshalling another user's projects in a private group as
        an admin
        """
        conn = get_connection(user_admin)
        project_a, project_b, project_c, project_d = projects_read_only
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        # Order is important to test desired HQL sorting semantics.
        expected = [{
            'id': project_a.id.val,
            'name': 'Apple',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_c.id.val,
            'name': 'atom',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_b.id.val,
            'name': 'bat',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_d.id.val,
            'name': 'Butter',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }]

        marshaled = marshal_projects(conn, user_read_only[1].id)
        assert marshaled == expected

    def test_marshal_projects_group_read_annotate_admin(
            self, user_read_annotate, user_admin, projects_read_annotate):
        """
        Test marshalling another user's projects in a private group as
        an admin
        """
        conn = get_connection(user_admin)
        project_a, project_b, project_c, project_d = projects_read_annotate
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        # Order is important to test desired HQL sorting semantics.
        expected = [{
            'id': project_a.id.val,
            'name': 'Apple',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_c.id.val,
            'name': 'atom',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_b.id.val,
            'name': 'bat',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_d.id.val,
            'name': 'Butter',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }]

        marshaled = marshal_projects(conn, user_read_annotate[1].id)
        assert marshaled == expected

    def test_marshal_projects_all_private(self, user_private,
                                          projects_private):
        """
        Test marshalling all projects in private group
        """
        conn = get_connection(user_private)
        project_a, project_b, project_c, project_d = projects_private
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        # Order is important to test desired HQL sorting semantics.
        expected = [{
            'id': project_a.id.val,
            'name': 'Apple',
            'isOwned': True,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_c.id.val,
            'name': 'atom',
            'isOwned': True,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_b.id.val,
            'name': 'bat',
            'isOwned': True,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_d.id.val,
            'name': 'Butter',
            'isOwned': True,
            'childCount': 0,
            'permsCss': perms_css
        }]

        marshaled = marshal_projects(conn)
        assert marshaled == expected

    def test_marshal_projects_all_read_only(self, user_read_only,
                                            projects_all_read_only):
        """
        Test marshalling all projects in read-only group
        """
        conn = get_connection(user_read_only)
        project_a, project_b, project_c, project_d, project_e = \
            projects_all_read_only
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        perms_css_other = ''
        # Order is important to test desired HQL sorting semantics.
        expected = [{
            'id': project_a.id.val,
            'name': 'Apple',
            'isOwned': True,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_c.id.val,
            'name': 'atom',
            'isOwned': True,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_e.id.val,
            'name': 'Axe',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css_other
        }, {
            'id': project_b.id.val,
            'name': 'bat',
            'isOwned': True,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_d.id.val,
            'name': 'Butter',
            'isOwned': True,
            'childCount': 0,
            'permsCss': perms_css
        }]

        marshaled = marshal_projects(conn)
        assert marshaled == expected

    def test_marshal_projects_all_read_annotate(self, user_read_annotate,
                                                projects_all_read_annotate):
        """
        Test marshalling all projects in read-annotate group
        """
        conn = get_connection(user_read_annotate)
        project_a, project_b, project_c, project_d, project_e = \
            projects_all_read_annotate
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        perms_css_other = 'canAnnotate'
        # Order is important to test desired HQL sorting semantics.
        expected = [{
            'id': project_a.id.val,
            'name': 'Apple',
            'isOwned': True,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_c.id.val,
            'name': 'atom',
            'isOwned': True,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_e.id.val,
            'name': 'Axe',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css_other
        }, {
            'id': project_b.id.val,
            'name': 'bat',
            'isOwned': True,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_d.id.val,
            'name': 'Butter',
            'isOwned': True,
            'childCount': 0,
            'permsCss': perms_css
        }]

        marshaled = marshal_projects(conn)
        assert marshaled == expected

    ###### All Projects as Owner ######
    def test_marshal_projects_all_private_owner(self, user_owner_private,
                                                projects_all_private):
        """
        Test marshalling all projects in private group as owner
        """
        conn = get_connection(user_owner_private)
        project_a, project_b, project_c, project_d, project_e = \
            projects_all_private
        perms_css = 'canEdit canDelete'
        # Order is important to test desired HQL sorting semantics.
        expected = [{
            'id': project_a.id.val,
            'name': 'Apple',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_c.id.val,
            'name': 'atom',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_e.id.val,
            'name': 'Axe',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_b.id.val,
            'name': 'bat',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_d.id.val,
            'name': 'Butter',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }]

        marshaled = marshal_projects(conn)
        for x in marshaled:
            print x
        assert marshaled == expected

    def test_marshal_projects_all_read_only_owner(self, user_owner_read_only,
                                                  projects_all_read_only):
        """
        Test marshalling all projects in read-only group as owner
        """
        conn = get_connection(user_owner_read_only)
        project_a, project_b, project_c, project_d, project_e = \
            projects_all_read_only
        perms_css = 'canEdit canAnnotate canLink canDelete'
        # Order is important to test desired HQL sorting semantics.
        expected = [{
            'id': project_a.id.val,
            'name': 'Apple',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_c.id.val,
            'name': 'atom',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_e.id.val,
            'name': 'Axe',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_b.id.val,
            'name': 'bat',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_d.id.val,
            'name': 'Butter',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }]

        marshaled = marshal_projects(conn)
        assert marshaled == expected

    def test_marshal_projects_all_read_annotate_owner(
            self, user_owner_read_annotate, projects_all_read_annotate):
        """
        Test marshalling all projects in read-annotate group as owner
        """
        conn = get_connection(user_owner_read_annotate)
        project_a, project_b, project_c, project_d, project_e = \
            projects_all_read_annotate
        perms_css = 'canEdit canAnnotate canLink canDelete'
        # Order is important to test desired HQL sorting semantics.
        expected = [{
            'id': project_a.id.val,
            'name': 'Apple',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_c.id.val,
            'name': 'atom',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_e.id.val,
            'name': 'Axe',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_b.id.val,
            'name': 'bat',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_d.id.val,
            'name': 'Butter',
            'isOwned': False,
            'childCount': 0,
            'permsCss': perms_css
        }]

        marshaled = marshal_projects(conn)
        assert marshaled == expected

    ###### All Projects as Admin ######
    # This is commented out because it is difficult to test. The admin user
    # is not in the private/read-only/read-annoate groups and thus will
    # return nothing because there is no projects in its group
    # If cross-group querying is turned on with:
    # connection.SERVICE_OPTS.setOmeroGroup(-1)
    # then all projects in the system are returned. It is impossible
    # to have the right expectation as the projects in the system will
    # evolve over time
    # def test_marshal_projects_all_private_admin(self, user_admin,
    #                                             projects_private):
    #     """
    #     Test marshalling all projects in private group as admin
    #     """
    #     conn = get_connection(user_admin)
    #     project_a, project_b, project_c, project_d, project_e = \
    #         projects_private
    #     perms_css = 'canEdit canDelete canChgrp'
    #     # Order is important to test desired HQL sorting semantics.
    #     expected = [{
    #         'id': project_a.id.val,
    #         'name': 'Apple',
    #         'isOwned': False,
    #         'childCount': 0,
    #         'permsCss': perms_css
    #     }, {
    #         'id': project_c.id.val,
    #         'name': 'atom',
    #         'isOwned': False,
    #         'childCount': 0,
    #         'permsCss': perms_css
    #     }, {
    #         'id': project_e.id.val,
    #         'name': 'Axe',
    #         'isOwned': False,
    #         'childCount': 0,
    #         'permsCss': perms_css_other
    #     }, {
    #         'id': project_b.id.val,
    #         'name': 'bat',
    #         'isOwned': False,
    #         'childCount': 0,
    #         'permsCss': perms_css
    #     }, {
    #         'id': project_d.id.val,
    #         'name': 'Butter',
    #         'isOwned': False,
    #         'childCount': 0,
    #         'permsCss': perms_css
    #     }]

    #     marshaled = marshal_projects(conn)
    #     assert marshaled == expected

    # def test_marshal_projects_all_read_only_admin(self, user_admin,
    #                                               projects_all_read_only):
    #     """
    #     Test marshalling all projects in read-only group as admin
    #     """
    #     conn = get_connection(user_admin)
    #     project_a, project_b, project_c, project_d, project_e = \
    #         projects_all_read_only
    #     perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
    #     # Order is important to test desired HQL sorting semantics.
    #     expected = [{
    #         'id': project_a.id.val,
    #         'name': 'Apple',
    #         'isOwned': False,
    #         'childCount': 0,
    #         'permsCss': perms_css
    #     }, {
    #         'id': project_c.id.val,
    #         'name': 'atom',
    #         'isOwned': False,
    #         'childCount': 0,
    #         'permsCss': perms_css
    #     }, {
    #         'id': project_e.id.val,
    #         'name': 'Axe',
    #         'isOwned': False,
    #         'childCount': 0,
    #         'permsCss': perms_css
    #     }, {
    #         'id': project_b.id.val,
    #         'name': 'bat',
    #         'isOwned': False,
    #         'childCount': 0,
    #         'permsCss': perms_css
    #     }, {
    #         'id': project_d.id.val,
    #         'name': 'Butter',
    #         'isOwned': False,
    #         'childCount': 0,
    #         'permsCss': perms_css
    #     }]

    #     marshaled = marshal_projects(conn)
    #     assert marshaled == expected

    # def test_marshal_projects_all_read_annotate_admin(
    #     self, user_admin, projects_all_read_annotate):
    #     """
    #     Test marshalling all projects in read-annotate group as admin
    #     """
    #     conn = get_connection(user_admin)
    #     project_a, project_b, project_c, project_d, project_e = \
    #         projects_all_read_annotate
    #     perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
    #     # Order is important to test desired HQL sorting semantics.
    #     expected = [{
    #         'id': project_a.id.val,
    #         'name': 'Apple',
    #         'isOwned': False,
    #         'childCount': 0,
    #         'permsCss': perms_css
    #     }, {
    #         'id': project_c.id.val,
    #         'name': 'atom',
    #         'isOwned': False,
    #         'childCount': 0,
    #         'permsCss': perms_css
    #     }, {
    #         'id': project_e.id.val,
    #         'name': 'Axe',
    #         'isOwned': False,
    #         'childCount': 0,
    #         'permsCss': perms_css
    #     }, {
    #         'id': project_b.id.val,
    #         'name': 'bat',
    #         'isOwned': False,
    #         'childCount': 0,
    #         'permsCss': perms_css
    #     }, {
    #         'id': project_d.id.val,
    #         'name': 'Butter',
    #         'isOwned': False,
    #         'childCount': 0,
    #         'permsCss': perms_css
    #     }]

    #     marshaled = marshal_projects(conn)
    #     assert marshaled == expected

    # This concludes the very thorough testing of project including all
    # possible group permissions and user role combinations.
    # TODO The above permission variations should perhaps really be tested
    # somewhere else to keep the permutations here minimal.
