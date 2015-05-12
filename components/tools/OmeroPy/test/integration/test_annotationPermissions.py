#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013-2015 University of Dundee & Open Microscopy Environment.
# All rights reserved. Use is subject to license terms supplied in LICENSE.txt
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

"""
   Integration test for adding annotations to Project using
   various permissions.

"""

import library as lib
import pytest
import omero
from omero_model_ProjectAnnotationLinkI import ProjectAnnotationLinkI
from omero_model_TagAnnotationI import TagAnnotationI
from omero.rtypes import rstring


class AnnotationPermissions(lib.ITest):

    @classmethod
    def setup_class(self):
        super(AnnotationPermissions, self).setup_class()

        # Tag names and namespaces
        uuid = self.uuid()
        self.proj_name = "Project-%s" % uuid
        self.tag_text = "Test-Tag-%s" % uuid
        self.tag_ns = "test/omero/tag/%s" % uuid

        self.users = set(["member1", "member2", "owner", "admin"])
        # create group and users
        self.exps = {}
        self.exps["owner"] = self.new_user(group=self.group, owner=True)
        self.exps["member1"] = self.new_user(group=self.group)
        self.exps["member2"] = self.new_user(group=self.group)
        self.exps["admin"] = self.new_user(group=self.group, system=True)

        # clients and services
        self.clients = {"root": self.root}
        self.updateServices = {}
        self.queryServices = {}
        self.project = {}

    def setup_method(self, method):
        for user in self.users:
            self.clients[user] = self.new_client(
                user=self.exps[user], group=self.group)
            self.updateServices[user] = self.clients[
                user].sf.getUpdateService()
            self.queryServices[user] = self.clients[user].sf.getQueryService()
            self.project[user] = self.createProjectAs(user)

    def teardown_method(self, method):
        for user in self.users:
            self.clients[user].closeSession()

    def chmodGroupAs(self, user, perms, succeed=True):
        client = self.clients[user]
        chmod = omero.cmd.Chmod2(
            targetObjects={'ExperimenterGroup': [self.group.id.val]},
            permissions=perms)
        self.doSubmit(chmod, client, test_should_pass=succeed)

    def createProjectAs(self, user):
        """ Adds a Project. """
        project = self.make_project(name=user + "_" + self.proj_name,
                                    client=self.clients[user])
        return project

    def getProjectAs(self, user, id):
        """ Gets a Project via its id. """
        return self.queryServices[user].find("Project", id)

    def makeTag(self):
        tag = TagAnnotationI()
        tag.setTextValue(rstring(self.tag_text))
        tag.setNs(rstring(self.tag_ns))
        return tag

    def createTagAs(self, user):
        """ Create a tag linked to nothing """
        tag = self.makeTag()
        tag = self.updateServices[user].saveAndReturnObject(tag)
        return tag

    def addTagAs(self, user, project):
        """ Adds and links a Tag. """
        tag = self.makeTag()
        tag = self.updateServices[user].saveAndReturnObject(tag)
        self.linkTagAs(user, project, tag)
        return tag

    def linkTagAs(self, user, project, tag):
        """ Adds a Tag. """
        l = ProjectAnnotationLinkI()
        project = project.__class__(project.id.val, False)
        l.setParent(project)
        l.setChild(tag)
        l = self.updateServices[user].saveAndReturnObject(l)

    def removeTagAs(self, user, project, tag):
        """ Removes (unlinks) a Tag. """
        project.unlinkAnnotation(tag)
        return self.updateServices[user].saveAndReturnObject(project)

    def deleteTagAs(self, user, tag):
        """ Deletes a Tag. """
        pass

    def getTagLinkAs(self, user, project):
        """ Gets a Tag's Link. """
        p = omero.sys.Parameters()
        p.map = {}
        p.map["pid"] = project.getId()
        p.map["ns"] = rstring(self.tag_ns)
        query = "select l from ProjectAnnotationLink as l join\
            fetch l.child as a where l.parent.id=:pid and a.ns=:ns"
        return self.queryServices[user].findByQuery(query, p)

    def getTagViaLinkAs(self, user, project):
        """ Gets a Tag via its link. """
        tagLink = self.getTagLinkAs(user, project)
        tag = None
        if tagLink:
            tag = tagLink.child
        return tag

    def getTagAs(self, user, id):
        """ Gets a Tag via its id. """
        return self.queryServices[user].find("TagAnnotation", id)


class TestPrivateGroup(AnnotationPermissions):

    DEFAULT_PERMS = 'rw----'

    def setup_method(self, method):
        AnnotationPermissions.setup_method(self, method)
        self.canAdd = {"member1": set(["member1"]),
                       "member2": set(["member2"]),
                       "owner":  set(["owner"]),
                       "admin":  set(["admin"])}

        self.canView = {"member1": set(["member1", "owner", "admin"]),
                        "member2": set(["member2", "owner", "admin"]),
                        "owner":  set(["owner", "admin"]),
                        "admin":  set(["owner", "admin"])}

        self.canRemove = {"member1": set(["member1", "owner", "admin"]),
                          "member2": set(["member2", "owner", "admin"]),
                          "owner":  set(["owner", "admin"]),
                          "admin":  set(["owner", "admin"])}

    def testAddTag(self):
        for creator in self.users:

            for user in self.canAdd[creator]:
                tag = self.addTagAs(creator, self.project[creator])
                assert tag.getTextValue().getValue() == self.tag_text

            for user in (self.users - self.canAdd[creator]):
                with pytest.raises(omero.SecurityViolation):
                    self.addTagAs(user, self.project[creator])

    def testReadTag(self):
        for creator in self.users:
            tag = self.addTagAs(creator, self.project[creator])
            tagId = tag.id.val

            for user in self.canView[creator]:
                tag = self.getTagViaLinkAs(user, self.project[creator])
                assert tag.getTextValue().getValue() == self.tag_text
                tag = self.getTagAs(user, tagId)
                assert tag.getTextValue().getValue() == self.tag_text

            for user in (self.users - self.canView[creator]):
                tag = self.getTagViaLinkAs(user, self.project[creator])
                assert tag is None
                with pytest.raises(omero.SecurityViolation):
                    self.getTagAs(user, tagId)

    def testRemoveTag(self):
        for creator in self.users:
            tag = self.addTagAs(creator, self.project[creator])
            tagId = tag.id.val

            for user in self.canRemove[creator]:
                self.removeTagAs(user, self.project[creator], tag)
                # Link should be gone
                tagLink = self.getTagLinkAs(creator, self.project[creator])
                assert tagLink is None
                # ...but tag should still exist
                tag = self.getTagAs(creator, tagId)
                assert tag.getTextValue().getValue() == self.tag_text
                # relink tag
                self.linkTagAs(creator, self.project[creator], tag)

            for user in (self.users - self.canRemove[creator]):
                with pytest.raises(omero.SecurityViolation):
                    self.removeTagAs(user, self.project[creator], tag)
                    assert False, "Should have thrown SecurityViolation"
                # Link and tag should still be there
                tag = self.getTagViaLinkAs(creator, self.project[creator])
                assert tag.getTextValue().getValue() == self.tag_text


class TestReadOnlyGroup(AnnotationPermissions):

    DEFAULT_PERMS = 'rwr---'

    def setup_method(self, method):
        AnnotationPermissions.setup_method(self, method)

        self.canAdd = {"member1": set(["member1", "owner", "admin"]),
                       "member2": set(["member2", "owner", "admin"]),
                       "owner":  set(["owner", "admin"]),
                       "admin":  set(["owner", "admin"])}

        self.canView = {"member1": self.users,
                        "member2": self.users,
                        "owner":  self.users,
                        "admin":  self.users}

        self.canRemove = {"member1": set(["member1", "owner", "admin"]),
                          "member2": set(["member2", "owner", "admin"]),
                          "owner":  set(["owner", "admin"]),
                          "admin":  set(["owner", "admin"])}

    def testAddTag(self):
        for creator in self.users:

            for user in self.canAdd[creator]:
                tag = self.addTagAs(creator, self.project[creator])
                assert tag.getTextValue().getValue() == self.tag_text

            for user in (self.users - self.canAdd[creator]):
                with pytest.raises(omero.SecurityViolation):
                    self.addTagAs(user, self.project[creator])

    def testReadTag(self):
        for creator in self.users:
            tag = self.addTagAs(creator, self.project[creator])
            tagId = tag.id.val

            for user in self.canView[creator]:
                tag = self.getTagViaLinkAs(user, self.project[creator])
                assert tag.getTextValue().getValue() == self.tag_text
                tag = self.getTagAs(user, tagId)
                assert tag.getTextValue().getValue() == self.tag_text

            for user in (self.users - self.canView[creator]):
                tag = self.getTagViaLinkAs(user, self.project[creator])
                assert tag is None
                with pytest.raises(omero.SecurityViolation):
                    self.getTagAs(user, tagId)
                    assert False, "Should have thrown SecurityViolation"

    def testRemoveTag(self):
        for creator in self.users:
            tag = self.addTagAs(creator, self.project[creator])
            tagId = tag.id.val

            for user in self.canRemove[creator]:
                self.removeTagAs(user, self.project[creator], tag)
                # Link should be gone
                tagLink = self.getTagLinkAs(creator, self.project[creator])
                assert tagLink is None
                # ...but tag should still exist
                tag = self.getTagAs(creator, tagId)
                assert tag.getTextValue().getValue() == self.tag_text
                # relink tag
                self.linkTagAs(creator, self.project[creator], tag)

            for user in (self.users - self.canRemove[creator]):
                with pytest.raises(omero.SecurityViolation):
                    self.removeTagAs(user, self.project[creator], tag)
                # Link should still be there
                tag = self.getTagViaLinkAs(creator, self.project[creator])
                assert tag.getTextValue().getValue() == self.tag_text


class TestReadAnnotateGroup(AnnotationPermissions):

    DEFAULT_PERMS = 'rwra--'

    def setup_method(self, method):
        AnnotationPermissions.setup_method(self, method)

        self.canAdd = {"member1": self.users,
                       "member2": self.users,
                       "owner":  self.users,
                       "admin":  self.users}

        self.canView = {"member1": self.users,
                        "member2": self.users,
                        "owner":  self.users,
                        "admin":  self.users}

        self.canRemove = {"member1": set(["member1", "owner", "admin"]),
                          "member2": set(["member2", "owner", "admin"]),
                          "owner":  set(["owner", "admin"]),
                          "admin":  set(["owner", "admin"])}

    def testAddTag(self):
        for creator in self.users:

            for user in self.canAdd[creator]:
                tag = self.addTagAs(creator, self.project[creator])
                assert tag.getTextValue().getValue() == self.tag_text

            for user in (self.users - self.canAdd[creator]):
                with pytest.raises(omero.SecurityViolation):
                    self.addTagAs(user, self.project[creator])

    def testReadTag(self):
        for creator in self.users:
            tag = self.addTagAs(creator, self.project[creator])
            tagId = tag.id.val

            for user in self.canView[creator]:
                tag = self.getTagViaLinkAs(user, self.project[creator])
                assert tag.getTextValue().getValue() == self.tag_text
                tag = self.getTagAs(user, tagId)
                assert tag.getTextValue().getValue() == self.tag_text

            for user in (self.users - self.canView[creator]):
                tag = self.getTagViaLinkAs(user, self.project[creator])
                assert tag is None
                with pytest.raises(omero.SecurityViolation):
                    self.getTagAs(user, tagId)

    def testRemoveTag(self):
        for creator in self.users:
            tag = self.addTagAs(creator, self.project[creator])
            tagId = tag.id.val

            for user in self.canRemove[creator]:
                self.removeTagAs(user, self.project[creator], tag)
                # Link should be gone
                tagLink = self.getTagLinkAs(creator, self.project[creator])
                assert tagLink is None
                # ...but tag should still exist
                tag = self.getTagAs(creator, tagId)
                assert tag.getTextValue().getValue() == self.tag_text
                # relink tag
                self.linkTagAs(creator, self.project[creator], tag)

            for user in (self.users - self.canRemove[creator]):
                with pytest.raises(omero.SecurityViolation):
                    self.removeTagAs(user, self.project[creator], tag)
                # Link should still be there
                tag = self.getTagViaLinkAs(creator, self.project[creator])
                assert tag.getTextValue().getValue() == self.tag_text


class TestMovePrivatePermissions(AnnotationPermissions):

    def setup_method(self, method):
        super(TestMovePrivatePermissions, self).setup_method(method)

        # Make the group read-annotate for every test run
        self.chmodGroupAs("root", "rwra--")

    @pytest.mark.parametrize("admin_type", ("root", "admin", "owner"))
    def testAddTagMakePrivate(self, admin_type):
        """ see ticket:11479 """

        # Have member1 tag their project with member2's tag
        project = self.createProjectAs("member1")
        tag = self.createTagAs("member2")
        self.linkTagAs("member1", project, tag)

        # Make the group private
        self.chmodGroupAs(admin_type, "rw----")

        # Link should be gone
        tagLink = self.getTagLinkAs("member1", project)
        assert tagLink is None

        # Check that the project remains
        project = self.getProjectAs("member1", project.id.val)
        assert project is not None

        # Check that the tag remains
        tag = self.getTagAs("member2", tag.id.val)
        assert tag is not None
