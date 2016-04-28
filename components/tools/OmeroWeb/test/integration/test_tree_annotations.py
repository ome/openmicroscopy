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
Integration tests for annotations methods in the "tree" module.
"""

import pytest
import library as lib
from datetime import datetime

from omero.gateway import BlitzGateway
from omero.model import ProjectI, \
    TagAnnotationI, ProjectAnnotationLinkI
from omero.rtypes import rstring
from omeroweb.webclient.tree import marshal_annotations


def cmp_id(x, y):
    """Identifier comparator."""
    return cmp(unwrap(x.id), unwrap(y.id))


def unwrap(x):
    """Handle case where there is no value because attribute is None"""
    if x is not None:
        return x.val
    return None


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


def get_update_service(user):
    """
    Get the update_service for the given user's client
    """
    return user[0].getSession().getUpdateService()


# Projects
@pytest.fixture(scope='function')
def project_userA(request, userA):
    """
    Returns new OMERO Project
    """
    project = ProjectI()
    project.name = rstring("test_tree_annnotations")
    project = get_update_service(userA).saveAndReturnObject(project)
    return project


@pytest.fixture(scope='function')
def projects_userA(request, userA):
    """
    Returns new OMERO Project
    """
    to_save = []
    for name in "test_ann1", "test_ann2":
        project = ProjectI()
        project.name = rstring(name)
        to_save.append(project)
    projects = get_update_service(userA).saveAndReturnArray(
        to_save)
    return projects


@pytest.fixture(scope='function')
def tags_userA_userB(request, userA, userB):
    """
    Returns new OMERO Tags
    """
    tags = []
    for name, user in zip(["userAtag", "userBtag"], [userA, userB]):
        tag = TagAnnotationI()
        tag.textValue = rstring(name)
        tag = get_update_service(user).saveAndReturnObject(tag)
        tags.append(tag)
    tags.sort(cmp_id)
    return tags


@pytest.fixture(scope='function')
def annotate_project(ann, project, user):
    """
    Returns userA's Tag linked to userB's Project
    by userA and userB
    """
    link = ProjectAnnotationLinkI()
    link.parent = ProjectI(project.id.val, False)
    link.child = ann
    link = get_connection(user).getUpdateService().saveAndReturnObject(link)
    return link


def expected_date(time):
    d = datetime.fromtimestamp(time/1000)
    return d.isoformat() + 'Z'


def expected_experimenter(experimenter):
    return {
        'id': experimenter.id.val,
        'omeName': experimenter.omeName.val,
        'firstName': unwrap(experimenter.firstName),
        'lastName': unwrap(experimenter.lastName)
    }


def expected_tags(links):

    annotations = []
    exps = {}
    for link in links:

        tag = link.child
        parent = link.parent

        creation = tag.details.creationEvent._time.val
        linkDate = link.details.creationEvent._time.val
        tagPerms = tag.details.permissions
        linkPerms = link.details.permissions

        exps[link.details.owner.id.val] = link.details.owner
        exps[tag.details.owner.id.val] = tag.details.owner

        annotations.append({
            'class': 'TagAnnotationI',
            'date': expected_date(creation),
            'link': {
                'owner': {
                    'id': link.details.owner.id.val
                },
                'date': expected_date(linkDate),
                'id': link.id.val,
                'parent': {
                    'class': parent.__class__.__name__,
                    'id': parent.id.val,
                    'name': parent.name.val
                },
                'permissions': {
                    'canAnnotate': linkPerms.canAnnotate(),
                    'canEdit': linkPerms.canEdit(),
                    'canDelete': linkPerms.canDelete(),
                    'canLink': linkPerms.canLink()
                }
            },
            'textValue': tag.textValue.val,
            'owner': {
                'id': tag.details.owner.id.val
            },
            'ns': unwrap(tag.ns),
            'id': tag.id.val,
            'permissions': {
                'canAnnotate': tagPerms.canAnnotate(),
                'canEdit': tagPerms.canEdit(),
                'canDelete': tagPerms.canDelete(),
                'canLink': tagPerms.canLink()
            }
        })
    # remove duplicates
    experimenters = [expected_experimenter(e) for e in exps.values()]
    experimenters.sort(key=lambda x: x['id'])

    return annotations, experimenters


class TestTreeAnnotations(lib.ITest):
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

    # Create a read-only group
    @pytest.fixture(scope='function')
    def groupA(self):
        """Returns a new read-annotate group."""
        return self.new_group(perms='rwra--')

    # Create a read-only group
    @pytest.fixture(scope='function')
    def groupB(self):
        """Returns a new read-only group."""
        return self.new_group(perms='rwr---')

    # Create users in groups
    @pytest.fixture()
    def userA(self, groupA, groupB):
        """Returns a new user in the groupB (default) also add to groupA"""
        user = self.new_client_and_user(group=groupB)
        self.add_groups(user[1], [groupA])
        return user

    @pytest.fixture()
    def userB(self, groupA):
        """Returns another new user in the read-only group."""
        return self.new_client_and_user(group=groupA)

    def test_single_annotate(self, userA, project_userA, tags_userA_userB):
        """
        Test
        """
        conn = get_connection(userA)
        tag = tags_userA_userB[0]
        project = project_userA
        link = annotate_project(tag, project, userA)
        expected = expected_tags([link])
        marshaled = marshal_annotations(conn=conn,
                                        project_ids=[project.id.val])
        annotations, experimenters = marshaled
        anns, exps = expected

        assert annotations == anns
        assert experimenters == exps
