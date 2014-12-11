#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013-2014 University of Dundee & Open Microscopy Environment.
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

import test.integration.library as lib
import pytest
import omero
from omero_model_ProjectI import ProjectI
from omero_model_ProjectAnnotationLinkI import ProjectAnnotationLinkI
from omero_model_TagAnnotationI import TagAnnotationI
from omero.rtypes import rstring


class AnnotationPermissions(lib.ITest):

    def setup_method(self, method, perms):
        lib.ITest.setup_method(self, method)

        # Tag names and namespaces
        uuid = self.uuid()
        self.proj_name = "Project-%s" % uuid
        self.tag_text = "Test-Tag-%s" % uuid
        self.tag_ns = "test/omero/tag/%s" % uuid

        self.users = set(["member1", "member2", "owner", "admin"])
        # create group and users
        self.group = self.new_group(perms=perms)
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
        for user in self.users:
            self.clients[user] = self.new_client(
                user=self.exps[user], group=self.group)
            self.updateServices[user] = self.clients[
                user].sf.getUpdateService()
            self.queryServices[user] = self.clients[user].sf.getQueryService()
            self.project[user] = self.createProjectAs(user)

    def teardown_method(self, method):
        lib.ITest.teardown_method(self, method)
        for user in self.users:
            self.clients[user].closeSession()

    def chmodGroupAs(self, user, perms):
        client = self.clients[user]
        # Using the deprecated method since
        # it was using a specific group context.
        client.sf.getAdminService().changePermissions(
            self.group, omero.model.PermissionsI(perms))

    def createProjectAs(self, user):
        """ Adds a Project. """
        project = ProjectI()
        project.name = rstring(user + "_" + self.proj_name)
        project = self.updateServices[user].saveAndReturnObject(project)
        return project

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

    def setup_method(self, method):
        AnnotationPermissions.setup_method(self, method, 'rw----')

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

    def setup_method(self, method):
        AnnotationPermissions.setup_method(self, method, 'rwr---')

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

    def setup_method(self, method):
        AnnotationPermissions.setup_method(self, method, 'rwra--')

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
        AnnotationPermissions.setup_method(self, method, 'rwra--')

    @pytest.mark.parametrize("admin_type", ("root", "admin"))
    def testAddTagMakePrivate(self, admin_type):
        """ see ticket:11479 """
        project = self.createProjectAs("member1")
        tag = self.createTagAs("member2")
        self.linkTagAs("member1", project, tag)
        with pytest.raises(omero.SecurityViolation):
            self.chmodGroupAs(admin_type, "rw----")

        for x in ("member1", "member2"):
            # Check reading
            self.getTagLinkAs(x, project)
            self.getTagViaLinkAs(x, project)
            self.getTagAs(x, tag.id.val)
