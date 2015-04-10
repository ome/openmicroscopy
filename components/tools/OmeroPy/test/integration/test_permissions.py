#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2008-2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
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
   Tests of the permissions

"""

import pytest
import library as lib
import omero
from omero_model_PermissionsI import PermissionsI
from omero_model_TagAnnotationI import TagAnnotationI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_sys_ParametersI import ParametersI
from omero.rtypes import rbool, rstring, unwrap


class CallContextFixture(object):

    """
    Provides overwriteable methods for testing the call context
    workflow (See #3529). The primary purpose is to reduce
    the copied code between the many call context test methods.
    """

    def __init__(self, test):
        self.test = test

    def client_and_user(self):
        """
        Creates a new client and a user for the fixture.
        By default, this calls new_client_and_user on the
        test object, but this can be overwritten.
        """
        return self.test.new_client_and_user()

    def setup(self):
        """
        Called to run the configuration from the client.
        Most likely this should not be modified.
        """
        self.client, self.user = self.client_and_user()
        self.sf = self.client.sf
        self.img = self.test.new_image()
        self.img = self.sf.getUpdateService().saveAndReturnObject(self.img)

        self.group2 = self.test.new_group([self.user])
        self.sf.getAdminService().getEventContext()  # Refresh
        self.sf.setSecurityContext(self.group2)

        # At this point, the fixture shouldn't be able
        # to load the image
        with pytest.raises(omero.SecurityViolation):
            self.sf.getQueryService().get("Image", self.img.id.val)

    def prepare(self):
        """
        This method should be once in order to set
        globally the call context if so desired.
        """
        pass

    def query_service(self):
        """
        This method should return a query service.
        By default, it simply returns the value of
        sf.getQueryService(), but this can be
        overwritten.
        """
        return self.sf.getQueryService()

    def get_image(self, query):
        """
        This method should make the IQuery.get call.
        By default, it simply makes the call, but this
        can be overwritten.
        """
        return query.get("Image", self.img.id.val)

    def assertCallContext(self):
        self.setup()
        self.prepare()
        query = self.query_service()
        img = self.get_image(query)
        assert img is not None


class TestPermissions(lib.ITest):

    def testLoginToPublicGroupTicket1940(self):
        # As root create a new group
        uuid = self.uuid()
        g = ExperimenterGroupI()
        g.name = rstring(uuid)
        g.ldap = rbool(False)
        g.details.permissions = PermissionsI("rwrwrw")
        self.root.sf.getAdminService().createGroup(g)

        # As a regular user, login to that group
        rv = self.root.getPropertyMap()
        ec = self.client.sf.getAdminService().getEventContext()
        public_client = omero.client(rv)
        public_client.getImplicitContext().put("omero.group", uuid)
        sf = public_client.createSession(ec.userName, ec.userName)
        ec = sf.getAdminService().getEventContext()
        assert uuid == ec.groupName

        # But can the user write anything?
        tag = TagAnnotationI()
        sf.getUpdateService().saveObject(tag)
        # And link?
        # And edit? cF. READ-ONLY & READ-LINK

    def testLinkingInPrivateGroup(self):

        uuid = self.uuid()
        group = self.new_group(perms="rw----")
        client, user = self.new_client_and_user(group=group, owner=True)

        project = self.make_project(name="project1_%s" % uuid)
        dataset = self.make_dataset(name="dataset1_%s" % uuid)
        self.link(project, dataset, client=self.client)

    def testCreatAndUpdatePrivateGroup(self):
        # this is the test of creating private group and updating it
        # including changes in #1434
        uuid = self.uuid()
        admin = self.root.sf.getAdminService()

        # create group1
        new_gr1 = ExperimenterGroupI()
        new_gr1.name = rstring("group1_%s" % uuid)
        new_gr1.ldap = rbool(False)
        p = PermissionsI()
        p.setUserRead(True)
        p.setUserWrite(True)
        p.setGroupRead(False)
        p.setGroupAnnotate(False)
        p.setGroupWrite(False)
        p.setWorldRead(False)
        p.setWorldAnnotate(False)
        p.setWorldWrite(False)
        new_gr1.details.permissions = p
        g1_id = admin.createGroup(new_gr1)

        # update name of group1
        gr1 = admin.getGroup(g1_id)
        assert 'rw----' == str(gr1.details.permissions)
        new_name = "changed_name_group1_%s" % uuid
        gr1.name = rstring(new_name)
        admin.updateGroup(gr1)
        gr1_u = admin.getGroup(g1_id)
        assert new_name == gr1_u.name.val

    def testCreatAndUpdatePublicGroupReadOnly(self):
        # this is the test of creating public group read-only and updating it
        # including changes in #1434
        uuid = self.uuid()
        admin = self.root.sf.getAdminService()

        # create group1
        new_gr1 = ExperimenterGroupI()
        new_gr1.name = rstring("group1_%s" % uuid)
        new_gr1.ldap = rbool(False)
        p = PermissionsI()
        p.setUserRead(True)
        p.setUserWrite(True)
        p.setGroupRead(True)
        p.setGroupAnnotate(False)
        p.setGroupWrite(False)
        p.setWorldRead(False)
        p.setWorldAnnotate(False)
        p.setWorldWrite(False)
        new_gr1.details.permissions = p
        g1_id = admin.createGroup(new_gr1)

        # update name of group1
        gr1 = admin.getGroup(g1_id)
        assert 'rwr---' == str(gr1.details.permissions)
        new_name = "changed_name_group1_%s" % uuid
        gr1.name = rstring(new_name)
        admin.updateGroup(gr1)
        gr1_u = admin.getGroup(g1_id)
        assert new_name == gr1_u.name.val

    def testCreatAndUpdatePublicGroupReadAnnotate(self):
        # this is the test of creating public group and updating it
        uuid = self.uuid()
        admin = self.root.sf.getAdminService()

        # create group1
        new_gr1 = ExperimenterGroupI()
        new_gr1.name = rstring("group1_%s" % uuid)
        new_gr1.ldap = rbool(False)
        p = PermissionsI()
        p.setUserRead(True)
        p.setUserWrite(True)
        p.setGroupRead(True)
        p.setGroupAnnotate(True)
        p.setGroupWrite(False)
        p.setWorldRead(False)
        p.setWorldAnnotate(False)
        p.setWorldWrite(False)
        new_gr1.details.permissions = p
        g1_id = admin.createGroup(new_gr1)

        # update name of group1
        gr1 = admin.getGroup(g1_id)
        assert 'rwra--' == str(gr1.details.permissions)
        new_name = "changed_name_group1_%s" % uuid
        gr1.name = rstring(new_name)
        admin.updateGroup(gr1)
        gr1_u = admin.getGroup(g1_id)
        assert new_name == gr1_u.name.val

    def testCreatAndUpdatePublicGroup(self):
        # this is the test of creating public group and updating it
        # including changes in #1434
        uuid = self.uuid()
        admin = self.root.sf.getAdminService()

        # create group1
        new_gr1 = ExperimenterGroupI()
        new_gr1.name = rstring("group1_%s" % uuid)
        new_gr1.ldap = rbool(False)
        p = PermissionsI()
        p.setUserRead(True)
        p.setUserWrite(True)
        p.setGroupRead(True)
        p.setGroupWrite(True)
        p.setWorldRead(False)
        p.setWorldAnnotate(False)
        p.setWorldWrite(False)
        new_gr1.details.permissions = p
        g1_id = admin.createGroup(new_gr1)

        # update name of group1
        gr1 = admin.getGroup(g1_id)
        assert 'rwrw--' == str(gr1.details.permissions)
        new_name = "changed_name_group1_%s" % uuid
        gr1.name = rstring(new_name)
        admin.updateGroup(gr1)
        gr1_u = admin.getGroup(g1_id)
        assert new_name == gr1_u.name.val

    def testCreatGroupAndchangePermissions(self):
        # this is the test of updating group permissions
        # including changes in #1434
        uuid = self.uuid()
        admin = self.root.sf.getAdminService()

        # create group1
        new_gr1 = ExperimenterGroupI()
        new_gr1.name = rstring("group1_%s" % uuid)
        new_gr1.ldap = rbool(False)
        p = PermissionsI()
        p.setUserRead(True)
        p.setUserWrite(True)
        p.setGroupRead(False)
        p.setGroupAnnotate(False)
        p.setGroupWrite(False)
        p.setWorldRead(False)
        p.setWorldAnnotate(False)
        p.setWorldWrite(False)
        new_gr1.details.permissions = p
        g1_id = admin.createGroup(new_gr1)

        # increase permissions of group1 to rwr---
        p1 = PermissionsI()
        p1.setUserRead(True)
        p1.setUserWrite(True)
        p1.setGroupRead(True)
        p1.setGroupAnnotate(False)
        p1.setGroupWrite(False)
        p1.setWorldRead(False)
        p1.setWorldAnnotate(False)
        p1.setWorldWrite(False)
        self.change_permissions(g1_id, str(p1), self.root)
        gr2 = admin.getGroup(g1_id)
        assert 'rwr---' == str(gr2.details.permissions)

        # increase permissions of group1 to rwra--
        gr2 = admin.getGroup(g1_id)
        p2 = PermissionsI()
        p2.setUserRead(True)
        p2.setUserWrite(True)
        p2.setGroupRead(True)
        p2.setGroupAnnotate(True)
        p2.setGroupWrite(False)
        p2.setWorldRead(False)
        p2.setWorldAnnotate(False)
        p2.setWorldWrite(False)
        self.change_permissions(g1_id, str(p2), self.root)
        gr3 = admin.getGroup(g1_id)
        assert 'rwra--' == str(gr3.details.permissions)

        # increase permissions of group1 to rwrw--
        gr3 = admin.getGroup(g1_id)
        p3 = PermissionsI()
        p3.setUserRead(True)
        p3.setUserWrite(True)
        p3.setGroupRead(True)
        p3.setGroupWrite(True)
        p3.setWorldRead(False)
        p3.setWorldAnnotate(False)
        p3.setWorldWrite(False)
        self.change_permissions(g1_id, str(p3), self.root)
        gr4 = admin.getGroup(g1_id)
        assert 'rwrw--' == str(gr4.details.permissions)

    def testGroupOwners(self):
        # this is the test of creating private group and updating it
        # including changes in #1434
        uuid = self.uuid()
        admin = self.root.sf.getAdminService()

        # create group1
        new_gr1 = ExperimenterGroupI()
        new_gr1.name = rstring("group1_%s" % uuid)
        new_gr1.ldap = rbool(False)
        p = PermissionsI()
        p.setUserRead(True)
        p.setUserWrite(True)
        p.setGroupRead(True)
        p.setGroupAnnotate(False)
        p.setGroupWrite(False)
        p.setWorldRead(False)
        p.setWorldAnnotate(False)
        p.setWorldWrite(False)
        new_gr1.details.permissions = p
        g1_id = admin.createGroup(new_gr1)
        gr1 = admin.getGroup(g1_id)

        # create user1
        new_exp1 = ExperimenterI()
        new_exp1.omeName = rstring("user1_%s" % uuid)
        new_exp1.firstName = rstring("New")
        new_exp1.lastName = rstring("Test")
        new_exp1.ldap = rbool(False)
        new_exp1.email = rstring("newtest@emaildomain.com")

        uuid = self.uuid()
        uuidGroup = ExperimenterGroupI()
        uuidGroup.name = rstring(uuid)
        uuidGroup.ldap = rbool(False)
        uuidGroupId = admin.createGroup(uuidGroup)
        uuidGroup = ExperimenterGroupI(uuidGroupId, False)
        listOfGroups = list()
        listOfGroups.append(admin.lookupGroup("user"))
        eid1 = admin.createExperimenterWithPassword(
            new_exp1, rstring("ome"), uuidGroup, listOfGroups)
        exp1 = admin.getExperimenter(eid1)

        # set owner of the group (user is not a member of)
        admin.addGroupOwners(gr1, [exp1])
        # chech if is the leader
        leaderOfGroups = admin.getLeaderOfGroupIds(exp1)
        assert gr1.id.val in leaderOfGroups

        # remove group owner
        admin.removeGroupOwners(gr1, [exp1])
        # chech if no longer is the leader
        leaderOfGroups = admin.getLeaderOfGroupIds(exp1)
        assert gr1.id.val not in leaderOfGroups

        """
        Controller method shows how it is used in practice

        available = request.POST.getlist('available')
        owners = request.POST.getlist('owners')

        def setOwnersOfGroup(self, available, owners):
            # available - current list rest of the users
            # owners - current list of chosen users
            experimenters = admin.lookupExperimenters()
            old_owners = admin.containedOwners(gr1_id)
            old_available = list()
            for e in experimenters:
                flag = False
                for m in old_owners:
                    if e.id == m.id:
                        flag = True
                if not flag:
                    old_available.append(e)

            add_exps = list()
            rm_exps = list()
            for om in old_owners:
                for a in available:
                    if om.id == long(str(a)):
                        rm_exps.append(om._obj)
            for oa in old_available:
                for o in owners:
                    if oa.id == long(str(o)):
                        add_exps.append(oa._obj)

            #final save
            admin_serv.addGroupOwners(gr1, add_exps)
            admin_serv.removeGroupOwners(gr1, rm_exps)
        """

    def testSearchAllGroups(self):
        """
        Seeing if by setting omero.group < 0, we
        can load all possible objects.

        see ticket:2950 - was prevented
        see ticket:3529 - now enabled
        """

        uuid = self.uuid()
        hsql = """select t from TagAnnotation t where t.ns = :ns"""
        params = omero.sys.ParametersI()
        params.addString("ns", uuid)

        def get_tag(iquery, context):
            return iquery.findByQuery(hsql, params, context)

        update = self.client.sf.getUpdateService()
        query = self.client.sf.getQueryService()

        # As the regular user create an object
        tag = omero.model.TagAnnotationI()
        tag.ns = rstring(uuid)
        tag = update.saveAndReturnObject(tag)
        tid = tag.id.val

        # As root, try to load it
        root_query = self.root.sf.getQueryService()
        tag = get_tag(root_query, {})
        assert None == tag

        # Now try to load it again, with a context
        tag = get_tag(root_query, {"omero.group": "-1"})
        assert tid == tag.id.val

        # If the user tries that, there will be an exception
        get_tag(query, {"omero.group": "-1"})

    @pytest.mark.broken(ticket="11494")
    def test3136(self):
        """
        Calls to updateGroup were taking too long
        because the default value of permissions
        returned by the server was triggering a
        full changePermissions event.
        """
        admin = self.root.sf.getAdminService()
        group = self.new_group(perms="rw----")

        # Change the name but not the permissions
        group.name = rstring(self.uuid())
        elapsed1, rv = self.timeit(admin.updateGroup, group)

        # Now change the name and the permissions
        group.name = rstring(self.uuid())
        group.details.permissions = omero.model.PermissionsI("rwr---")
        elapsed2, rv = self.timeit(admin.updateGroup, group)

        # Locally this test always fails as the two times are
        # the same order of magnitude. This may be an indication that
        # the relevant ticket:
        # http://trac.openmicroscopy.org/ome/ticket/3136
        # is still valid. Does the ticket need re-opening
        # or does the test condition need relaxing?
        assert elapsed1 < (0.1 * elapsed2),\
            "elapsed1=%s, elapsed2=%s" % (elapsed1, elapsed2)

    #
    # Different API usages of call context (#3529)
    #

    def testOGContextParameter(self):
        # """ test omero.group can be set on the method call """

        class F(CallContextFixture):

            def get_image(this, query):
                return query.get(
                    "Image", this.img.id.val, {"omero.group": "-1"})
        F(self).assertCallContext()

    def testOGSetImplicitContext(self):
        # """ test omero.group can be set on the implicit context """

        class F(CallContextFixture):

            def prepare(this):
                return this.client.getImplicitContext().put(
                    "omero.group", "-1")
        F(self).assertCallContext()

    def testOGSetProxyContext(self):
        # """ test omero.group can be set on a proxy """

        class F(CallContextFixture):

            def query_service(this):
                service = this.sf.getQueryService()
                ctx = this.client.ic.getImplicitContext().getContext()
                ctx["omero.group"] = "-1"
                return service.ice_context(ctx)
        F(self).assertCallContext()

    #
    # Still UNSUPPORTED API usages of call context (#3529)
    #

    def testOGSetSecurityContext(self):
        # """ test omero.group can be set on session """

        class F(CallContextFixture):

            def prepare(this):
                return this.sf.setSecurityContext(
                    ExperimenterGroupI(-1, False))

        with pytest.raises(omero.ApiUsageException):
            F(self).assertCallContext()

    def testOGArg(self):
        # """ test omero.group can be set as an argument """

        class F(CallContextFixture):

            def client_and_user(this):
                user = this.test.new_user()
                props = this.test.client.getPropertyMap()
                props["omero.user"] = user.omeName.val
                props["omero.pass"] = user.omeName.val
                client = omero.client(props, ["--omero.group=-1"])
                self._ITest__clients.add(client)
                client.setAgent("OMERO.py.new_client_test")
                client.createSession()
                admin = client.sf.getAdminService()
                userId = admin.getEventContext().userId
                user = admin.getExperimenter(userId)
                return client, user

        import Glacier2
        with pytest.raises(Glacier2.CannotCreateSessionException):
            F(self).assertCallContext()

    # Write tests with omero.group set.
    # ==============================================

    # See ticket 11374
    @pytest.mark.broken(ticket="11374")
    def testSaveWithNegOneExplicit(self):

        # Get a user and services
        client, user = self.new_client_and_user()

        # Create a new object with an explicit group
        admin = client.sf.getAdminService()
        ec = admin.getEventContext()
        grp = omero.model.ExperimenterGroupI(ec.groupId, False)
        tag = omero.model.TagAnnotationI()
        tag.details.group = grp

        # Now try to save it in the -1 context
        update = client.sf.getUpdateService()
        all_context = {"omero.group": "-1"}
        update.saveAndReturnObject(tag, all_context)

    def testSaveWithNegOneNotExplicit(self):

        # Get a user and services
        client, user = self.new_client_and_user()

        # Create a new object without any
        # explicit group
        tag = omero.model.TagAnnotationI()

        # Now try to save it in the -1 context
        update = client.sf.getUpdateService()
        all_context = {"omero.group": "-1"}
        # An internal exception is raised when
        # Hibernate tries to access the annotations
        # for the null group set on the obj.
        # This isn't optimal but will work for
        # the moment.
        with pytest.raises(omero.ApiUsageException):
            update.saveAndReturnObject(tag, all_context)

    # See ticket 11374
    @pytest.mark.broken(ticket="11374")
    def testSaveWithNegBadLink(self):  # ticket:8194

        # Get a user and services
        client, user = self.new_client_and_user()
        admin = client.sf.getAdminService()
        group1 = admin.getGroup(admin.getEventContext().groupId)
        group2 = self.new_group(experimenters=[user])
        for x in (group1, group2):
            x.unload()
        admin.getEventContext()  # Refresh

        # Create a new object with a bad link
        image = self.new_image()
        image.details.group = group1
        tag = omero.model.TagAnnotationI()
        tag.details.group = group2
        link = image.linkAnnotation(tag)
        link.details.group = group2

        # Now try to save it in the -1 context
        update = client.sf.getUpdateService()
        all_context = {"omero.group": "-1"}
        # Bad links should be detected and
        # a security violation raised.
        with pytest.raises(omero.GroupSecurityViolation):
            update.saveAndReturnObject(image, all_context)

    # The following test is spun off from the one above Without
    # the -1 a GSV should be raised. See ticket 11375
    @pytest.mark.broken(ticket="11375")
    def testSaveBadLink(self):

        # Get a user and services
        client, user = self.new_client_and_user()
        admin = client.sf.getAdminService()
        group1 = admin.getGroup(admin.getEventContext().groupId)
        group2 = self.new_group(experimenters=[user])
        for x in (group1, group2):
            x.unload()
        admin.getEventContext()  # Refresh

        # Create a new object with a bad link
        image = self.new_image()
        image.details.group = group1
        tag = omero.model.TagAnnotationI()
        tag.details.group = group2
        link = image.linkAnnotation(tag)
        link.details.group = group2

        # Now try to save it
        update = client.sf.getUpdateService()
        # Bad links should be detected and
        # a security violation raised.
        with pytest.raises(omero.GroupSecurityViolation):
            update.saveAndReturnObject(image)

    # Reading with private groups
    # ==============================================

    def testPrivateGroupCallContext(self):

        # Setup groups as per Carlos' instructions (Feb 23)
        groupX = self.new_group(perms="rwrw--")
        clientA, userA = self.new_client_and_user(group=groupX)
        gid = str(groupX.id.val)

        groupY = self.new_group(perms="rw----")
        clientB, userB = self.new_client_and_user(group=groupY)
        self.add_experimenters(groupX, [userB])
        clientB.sf.getAdminService().getEventContext()  # Refresh

        # Create the object as user A
        tag = omero.model.TagAnnotationI()
        tag = clientA.sf.getUpdateService().saveAndReturnObject(tag)
        tid = tag.id.val

        # Now try to read it in different ways
        qa = clientA.sf.getQueryService()
        qb = clientB.sf.getQueryService()
        qr = self.root.sf.getQueryService()

        negone = {"omero.group": "-1"}
        specific = {"omero.group": gid}

        qa.get("TagAnnotation", tid)
        qa.get("TagAnnotation", tid, specific)
        qa.get("TagAnnotation", tid, negone)
        qr.get("TagAnnotation", tid, specific)  # Not currently in gid
        qr.get("TagAnnotation", tid, negone)
        qb.get("TagAnnotation", tid, specific)  # Not currently in gid
        qb.get("TagAnnotation", tid, negone)

    # Reading with an admin user
    # ==============================================

    def testAdminCanQueryWithGroupMinusOneTicket9632(self):
        q = self.root.sf.getQueryService()
        ctx = self.root.ic.getImplicitContext().getContext()
        ctx["omero.group"] = "-1"
        q.findAllByQuery('select p from Project as p', None, ctx)

    # Use of omero.user
    # ==============================================

    def private_image_and_user(self):
        client, user = self.new_client_and_user(system=False, perms="rw----")
        ec = client.sf.getAdminService().getEventContext()
        group = omero.model.ExperimenterGroupI(ec.groupId, False)
        image = self.new_image()
        image = client.sf.getUpdateService().saveAndReturnObject(image)
        return image, user, group

    def testOmeroUserAsAdmin(self):
        client, user = self.new_client_and_user(system=True)
        admin = client.sf.getAdminService()
        assert admin.getEventContext().isAdmin

        image, user, group = self.private_image_and_user()
        self.assertAsUser(client, image, user, group)

    def testOmeroUserAsNonAdmin(self):
        client, user = self.new_client_and_user(system=False)
        admin = client.sf.getAdminService()
        assert not admin.getEventContext().isAdmin

        image, user, group = self.private_image_and_user()
        with pytest.raises(omero.SecurityViolation):
            self.assertAsUser(client, image, user, group)

    def assertAsUser(self, client, image, user, group):
        callcontext = {"omero.user": str(user.id.val),
                       "omero.group": str(group.id.val)}
        query = client.sf.getQueryService()
        query.get("Image", image.id.val, callcontext)

    # chmod
    # ==============================================

    def testImmutablePermissions(self):
        # See #8277 permissions returned from the server
        # should now be immutable.

        # Test on the raw object
        p = omero.model.PermissionsI()
        p.ice_postUnmarshal()
        with pytest.raises(omero.ClientError):
            p.setPerm1(1)

        # and on one returned from the server
        c = omero.model.CommentAnnotationI()
        c = self.update.saveAndReturnObject(c)
        p = c.details.permissions
        with pytest.raises(omero.ClientError):
            p.setPerm1(1)

    def testDisallow(self):
        p = omero.model.PermissionsI()
        assert p.canAnnotate()
        assert p.canEdit()

    def testClientSet(self):
        c = omero.model.CommentAnnotationI()
        c = self.update.saveAndReturnObject(c)
        d = c.getDetails()
        assert d.getClient() is not None
        assert d.getSession() is not None
        assert d.getCallContext() is not None
        assert d.getEventContext() is not None

    # raw pixels bean
    # ==================================================

    def testAdminUseOfRawPixelsBean(self):
        owner = self.new_client()
        image1 = self.createTestImage(session=owner.sf)
        pixid1 = image1.getPrimaryPixels().getId().getValue()
        image2 = self.createTestImage(session=owner.sf)
        pixid2 = image2.getPrimaryPixels().getId().getValue()

        rps = self.root.sf.createRawPixelsStore()
        try:
            rps.setPixelsId(pixid1, False, {'omero.group': '-1'})
            rps.getByteWidth()
            rps.setPixelsId(pixid2, False, {'omero.group': '-1'})
            rps.getByteWidth()
        finally:
            rps.close()

    # raw file bean
    # ==================================================

    def assertValidScript(self, f):
        user = self.new_client()
        srv = user.sf.getScriptService()
        script = srv.getScripts()[0]
        store = user.sf.createRawFileStore()
        params = f(script)
        # ticket:9192. For some actions to be possible
        # server-side, it's necessary to use a copy of
        # the implicit context in order to have the
        # client uuid present.
        ctx = user.getImplicitContext().getContext().copy()
        ctx.update(params)
        store.setFileId(script.id.val, ctx)

        data = store.read(0, long(script.size.val))
        assert script.size.val == len(data)
        try:
            store.close()
        except:
            pass

    def testUseOfRawFileBeanScriptReadGroupMinusOne(self):
        self.assertValidScript(lambda v: {'omero.group': '-1'})

    def testUseOfRawFileBeanScriptReadCorrectGroup(self):
        self.assertValidScript(lambda v: {'omero.group':
                                          str(v.details.group.id.val)})

    @pytest.mark.broken(ticket="11539")
    def testUseOfRawFileBeanScriptReadCorrectGroupAndUser(self):
        self.assertValidScript(lambda v: {
            'omero.group': str(v.details.group.id.val),
            'omero.user': str(v.details.owner.id.val)
        })


class TestPermissionProjections(lib.ITest):

    _group = None
    _other = dict()
    _cache = dict()

    def writer(self, fixture):
        client = self._new_client(fixture.reader, fixture.perms)
        return client.sf.getUpdateService()

    def reader(self, fixture):
        client = self._new_client(fixture.reader, fixture.perms)
        return client.sf.getQueryService()

    def _new_client(self, who, perms):
        if self._group is None:
            self._group = self.new_group(perms=perms)
        if who == "system-admin":
            self._system_admin = self.new_client(
                group=self._group, system=True)
            return self._system_admin
        elif who == "group-owner":
            self._group_owner = self.new_client(
                group=self._group, owner=True)
            return self._group_owner
        else:
            self._other[who] = self.new_client(group=self._group)
            return self._other[who]

    def assertPerms(self, perms, fixture):
        found_arr = []
        expected_arr = []
        for x in ("Annotate", "Delete", "Edit", "Link"):
            key = "can%s" % x
            found = bool(perms[key])
            found_arr.append(found)
            expected = bool(getattr(fixture, key))
            expected_arr.append(expected)
        assert expected_arr == found_arr

    @pytest.mark.xfail(ticket="12474")
    @pytest.mark.parametrize("fixture", lib.PFS,
                             ids=[x.get_name() for x in lib.PFS])
    def testProjectionPermissions(self, fixture):
        writer = self.writer(fixture)
        reader = self.reader(fixture)
        project = self.make_project(name="testProjectPermissions",
                                    client=writer)
        project = writer.saveAndReturnObject(project)
        try:
            perms = unwrap(reader.projection(
                "select p.details.permissions from Project p where p.id = :id",
                ParametersI().addId(project.id.val)))[0][0]
            assert fixture.canRead
        except IndexError:
            # No permissions were returned.
            assert not fixture.canRead
        else:
            self.assertPerms(perms, fixture)

    @pytest.mark.parametrize("fixture", lib.PFS,
                             ids=[x.get_name() for x in lib.PFS])
    def testProjectionPermissionsWorkaround(self, fixture):
        writer = self.writer(fixture)
        reader = self.reader(fixture)
        project = self.make_project(name="testProjectPermissions",
                                    client=writer)

        group = project.details.group.id.val
        owner = project.details.owner.id.val
        key = (group, owner)
        try:
            if key in self._cache:
                # In pytest, this is never actually reached,
                # since each test is a new instance, but this
                # would be the performance speed up needed in
                # clients. See #12474
                assert False
            else:
                proj = unwrap(reader.projection(
                    "select p from Project p where p.id = :id",
                    ParametersI().addId(project.id.val),
                    {"omero:group": str(group)}))[0][0]
                assert fixture.canRead
                perms = proj.details.permissions
                value = {
                    "perms": str(perms),
                    "canAnnotate": perms.canAnnotate(),
                    "canDelete": perms.canDelete(),
                    "canEdit": perms.canEdit(),
                    "canLink": perms.canLink(),
                }
                self._cache[key] = value
        except IndexError:
            # No permissions were returned.
            assert not fixture.canRead
        else:
            self.assertPerms(self._cache[key], fixture)

    # extended restrictions
    # ==================================================

    RESTR_OBJS = (
        omero.model.PlateI(),
        omero.model.WellI(),
        omero.model.ImageI(),
        omero.model.FileAnnotationI(),
        # File tested by test_downloads.py
    )
    RESTR_NAMES = [x.__class__.__name__ for x in RESTR_OBJS]

    @pytest.mark.parametrize("obj", RESTR_OBJS,
                             ids=RESTR_NAMES)
    def testExtendedRestrictions(self, obj):

        # Check if this test should run
        self.skip_if("omero.policy.binary_access",
                     lambda x: x != "repository")

        # Now set up a shared group
        admin = self.sf.getAdminService()
        uid = admin.getEventContext().userId
        user = admin.getExperimenter(uid)
        group = self.new_group(experimenters=[user],
                               perms="rwr---")
        self.add_experimenters(group, [user])
        admin.getEventContext()  # Refresh
        other = self.new_client(group=group)
        self.set_context(self.client, group.id.val)

        # Save the object and see that the restriction is off
        if "name" in obj._field_info._fields:
            obj.name = omero.rtypes.rstring("restr-test")
        elif "Well" in obj.__class__.__name__:
            obj.setPlate(omero.model.PlateI())
            obj.getPlate().setName(
                omero.rtypes.rstring("restr-test"))
        obj = self.update.saveAndReturnObject(obj)
        assert not obj.details.permissions.isRestricted(
            omero.constants.permissions.DOWNLOAD)

        # Now see if the restriction is on for the other user
        obj = other.sf.getQueryService().get(
            obj.__class__.__name__, obj.id.val)

        assert obj.details.permissions.isRestricted(
            omero.constants.permissions.DOWNLOAD)
