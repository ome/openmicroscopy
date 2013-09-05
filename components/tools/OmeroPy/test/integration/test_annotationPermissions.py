#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test for adding annotations to Project.
"""
import unittest
import test.integration.library as lib
import omero
from omero_model_ProjectI import ProjectI
from omero_model_ProjectAnnotationLinkI import ProjectAnnotationLinkI
from omero_model_TagAnnotationI import TagAnnotationI
from omero.rtypes import *

class AnnotationPermissions(lib.ITest):
    def setUp(self, perms):
        lib.ITest.setUp(self)

        # Tag names and namespaces
        uuid = self.uuid()
        self.proj_name = "Project-%s" % uuid
        self.tag_text = "Test-Tag-%s" % uuid
        self.tag_ns = "test/omero/tag/%s" % uuid

        self.users = set(["member1", "member2", "owner", "admin"])
        # create group and users
        group = self.new_group(perms=perms)
        self.exps = {}
        self.exps["owner"] = self.new_user(group=group, admin = True)
        self.exps["member1"] = self.new_user(group=group)
        self.exps["member2"] = self.new_user(group=group)
        self.exps["admin"] = self.new_user(group=group, system = True)

        # clients and services
        self.clients = {}
        self.updateServices = {}
        self.queryServices = {}
        self.project = {}
        for user in self.users:
            self.clients[user] = self.new_client(user=self.exps[user], group=group)
            self.updateServices[user] = self.clients[user].sf.getUpdateService()
            self.queryServices[user] = self.clients[user].sf.getQueryService()
            self.project[user] = self.createProjectAs(user)

    def tearDown(self):
        lib.ITest.tearDown(self)
        for user in self.users:
            self.clients[user].closeSession()

    def createProjectAs(self, user):
        """ Adds a Project. """
        project = ProjectI()
        project.name = rstring(user + "_" + self.proj_name)
        project = self.updateServices[user].saveAndReturnObject(project)
        return project

    def addTagAs(self, user, project):
        """ Adds and links a Tag. """
        tag = TagAnnotationI()
        tag.setTextValue(rstring(self.tag_text))
        tag.setNs(rstring(self.tag_ns))
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
        query = "select l from ProjectAnnotationLink as l join fetch l.child as a where l.parent.id=:pid and a.ns=:ns"
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

class PrivateGroup(AnnotationPermissions):

    def setUp(self):
        AnnotationPermissions.setUp(self, 'rw----')

        self.canAdd =    { "member1":set(["member1"]),
                           "member2":set(["member2"]),
                           "owner":  set(["owner"]),
                           "admin":  set(["admin"]) }

        self.canView =    { "member1":set(["member1", "owner", "admin"]),
                            "member2":set(["member2", "owner", "admin"]),
                            "owner":  set(["owner", "admin"]),
                            "admin":  set(["owner", "admin"]) }

        self.canRemove =    { "member1":set(["member1", "owner", "admin"]),
                              "member2":set(["member2", "owner", "admin"]),
                              "owner":  set(["owner", "admin"]),
                              "admin":  set(["owner", "admin"]) }

    def testAddTag(self):
        for creator in self.users:

            for user in self.canAdd[creator]:
                tag = self.addTagAs(creator, self.project[creator])
                self.assertEquals(tag.getTextValue().getValue(), self.tag_text)

            for user in (self.users - self.canAdd[creator]):
                try:
                    self.addTagAs(user, self.project[creator])
                    self.fail("Should have thrown SecurityViolation")
                except omero.SecurityViolation, sv:
                    pass # good

    def testReadTag(self):
        for creator in self.users:
            tag = self.addTagAs(creator, self.project[creator])
            tagId = tag.id.val

            for user in self.canView[creator]:
                tag = self.getTagViaLinkAs(user, self.project[creator])
                self.assertEquals(tag.getTextValue().getValue(), self.tag_text)
                tag = self.getTagAs(user, tagId)
                self.assertEquals(tag.getTextValue().getValue(), self.tag_text)

            for user in (self.users - self.canView[creator]):
                tag = self.getTagViaLinkAs(user, self.project[creator])
                self.assertEquals(tag, None)
                try:
                    tag = self.getTagAs(user, tagId)
                    self.fail("Should have thrown SecurityViolation")
                except omero.SecurityViolation, sv:
                    pass # good

    def testRemoveTag(self):
        for creator in self.users:
            tag = self.addTagAs(creator, self.project[creator])
            tagId = tag.id.val

            for user in self.canRemove[creator]:
                self.removeTagAs(user, self.project[creator], tag)
                # Link should be gone
                tagLink = self.getTagLinkAs(creator, self.project[creator])
                self.assertEquals(tagLink, None)
                # ...but tag should still exist
                tag = self.getTagAs(creator, tagId)
                self.assertEquals(tag.getTextValue().getValue(), self.tag_text)
                # relink tag
                self.linkTagAs(creator, self.project[creator], tag)

            for user in (self.users - self.canRemove[creator]):
                try:
                    self.removeTagAs(user, self.project[creator], tag)
                    self.fail("Should have thrown SecurityViolation")
                except omero.SecurityViolation, sv:
                    pass # good
                # Link and tag should still be there
                tag = self.getTagViaLinkAs(creator, self.project[creator])
                self.assertEquals(tag.getTextValue().getValue(), self.tag_text)

class ReadOnlyGroup(AnnotationPermissions):

    def setUp(self):
        AnnotationPermissions.setUp(self, 'rwr---')

        self.canAdd =    { "member1":set(["member1", "owner", "admin"]),
                           "member2":set(["member2", "owner", "admin"]),
                           "owner":  set(["owner", "admin"]),
                           "admin":  set(["owner", "admin"]) }

        self.canView =    { "member1":self.users,
                            "member2":self.users,
                            "owner":  self.users,
                            "admin":  self.users }

        self.canRemove =    { "member1":set(["member1", "owner", "admin"]),
                              "member2":set(["member2", "owner", "admin"]),
                              "owner":  set(["owner", "admin"]),
                              "admin":  set(["owner", "admin"]) }

    def testAddTag(self):
        for creator in self.users:

            for user in self.canAdd[creator]:
                tag = self.addTagAs(creator, self.project[creator])
                self.assertEquals(tag.getTextValue().getValue(), self.tag_text)

            for user in (self.users - self.canAdd[creator]):
                try:
                    self.addTagAs(user, self.project[creator])
                    self.fail("Should have thrown SecurityViolation")
                except omero.SecurityViolation, sv:
                    pass # good

    def testReadTag(self):
        for creator in self.users:
            tag = self.addTagAs(creator, self.project[creator])
            tagId = tag.id.val

            for user in self.canView[creator]:
                tag = self.getTagViaLinkAs(user, self.project[creator])
                self.assertEquals(tag.getTextValue().getValue(), self.tag_text)
                tag = self.getTagAs(user, tagId)
                self.assertEquals(tag.getTextValue().getValue(), self.tag_text)

            for user in (self.users - self.canView[creator]):
                tag = self.getTagViaLinkAs(user, self.project[creator])
                self.assertEquals(tag, None)
                try:
                    tag = self.getTagAs(user, tagId)
                    self.fail("Should have thrown SecurityViolation")
                except omero.SecurityViolation, sv:
                    pass # good

    def testRemoveTag(self):
        for creator in self.users:
            tag = self.addTagAs(creator, self.project[creator])
            tagId = tag.id.val

            for user in self.canRemove[creator]:
                self.removeTagAs(user, self.project[creator], tag)
                # Link should be gone
                tagLink = self.getTagLinkAs(creator, self.project[creator])
                self.assertEquals(tagLink, None)
                # ...but tag should still exist
                tag = self.getTagAs(creator, tagId)
                self.assertEquals(tag.getTextValue().getValue(), self.tag_text)
                # relink tag
                self.linkTagAs(creator, self.project[creator], tag)

            for user in (self.users - self.canRemove[creator]):
                try:
                    self.removeTagAs(user, self.project[creator], tag)
                    self.fail("Should have thrown SecurityViolation")
                except omero.SecurityViolation, sv:
                    pass # good
                # Link should still be there
                tag = self.getTagViaLinkAs(creator, self.project[creator])
                self.assertEquals(tag.getTextValue().getValue(), self.tag_text)

class ReadAnnotateGroup(AnnotationPermissions):

    def setUp(self):
        AnnotationPermissions.setUp(self, 'rwra--')

        self.canAdd =    { "member1":self.users,
                           "member2":self.users,
                           "owner":  self.users,
                           "admin":  self.users }

        self.canView =    { "member1":self.users,
                            "member2":self.users,
                            "owner":  self.users,
                            "admin":  self.users }

        self.canRemove =    { "member1":set(["member1", "owner", "admin"]),
                              "member2":set(["member2", "owner", "admin"]),
                              "owner":  set(["owner", "admin"]),
                              "admin":  set(["owner", "admin"]) }

    def testAddTag(self):
        for creator in self.users:

            for user in self.canAdd[creator]:
                tag = self.addTagAs(creator, self.project[creator])
                self.assertEquals(tag.getTextValue().getValue(), self.tag_text)

            for user in (self.users - self.canAdd[creator]):
                try:
                    self.addTagAs(user, self.project[creator])
                    self.fail("Should have thrown SecurityViolation")
                except omero.SecurityViolation, sv:
                    pass # good

    def testReadTag(self):
        for creator in self.users:
            tag = self.addTagAs(creator, self.project[creator])
            tagId = tag.id.val

            for user in self.canView[creator]:
                tag = self.getTagViaLinkAs(user, self.project[creator])
                self.assertEquals(tag.getTextValue().getValue(), self.tag_text)
                tag = self.getTagAs(user, tagId)
                self.assertEquals(tag.getTextValue().getValue(), self.tag_text)

            for user in (self.users - self.canView[creator]):
                tag = self.getTagViaLinkAs(user, self.project[creator])
                self.assertEquals(tag, None)
                try:
                    tag = self.getTagAs(user, tagId)
                    self.fail("Should have thrown SecurityViolation")
                except omero.SecurityViolation, sv:
                    pass # good

    def testRemoveTag(self):
        for creator in self.users:
            tag = self.addTagAs(creator, self.project[creator])
            tagId = tag.id.val

            for user in self.canRemove[creator]:
                self.removeTagAs(user, self.project[creator], tag)
                # Link should be gone
                tagLink = self.getTagLinkAs(creator, self.project[creator])
                self.assertEquals(tagLink, None)
                # ...but tag should still exist
                tag = self.getTagAs(creator, tagId)
                self.assertEquals(tag.getTextValue().getValue(), self.tag_text)
                # relink tag
                self.linkTagAs(creator, self.project[creator], tag)

            for user in (self.users - self.canRemove[creator]):
                try:
                    self.removeTagAs(user, self.project[creator], tag)
                    self.fail("Should have thrown SecurityViolation")
                except omero.SecurityViolation, sv:
                    pass # good
                # Link should still be there
                tag = self.getTagViaLinkAs(creator, self.project[creator])
                self.assertEquals(tag.getTextValue().getValue(), self.tag_text)

if __name__ == '__main__':
    unittest.main()
