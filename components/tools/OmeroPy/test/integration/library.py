#!/usr/bin/env python

"""
   Library for integration tests

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import Ice
import sys
import unittest
import omero
import tempfile
import traceback
import exceptions
from omero.rtypes import rstring, rtime
from uuid import uuid4 as uuid


class ITest(unittest.TestCase):

    def setUp(self):
        self.tmpfiles = []

        p = Ice.createProperties(sys.argv)
        rootpass = p.getProperty("omero.rootpass")

        name = None
        pasw = None
        if rootpass:
            self.root = omero.client()
            self.root.setAgent("OMERO.py.root_test")
            self.root.createSession("root", rootpass)
            newuser = self.new_user()
            name = newuser.omeName.val
            pasw = "1"
        else:
            self.root = None

        self.client = omero.client()
        self.client.setAgent("OMERO.py.test")
        self.sf = self.client.createSession(name, pasw)

        self.update = self.sf.getUpdateService()
        self.query = self.sf.getQueryService()

    def uuid(self):
        return str(uuid())

    def login_args(self):
        p = self.client.ic.getProperties()
        host = p.getProperty("omero.host")
        key = self.sf.ice_getIdentity().name
        return ["-s", host, "-k", key] # TODO PORT

    def tmpfile(self):
        tmpfile = tempfile.NamedTemporaryFile(mode='w+t')
        self.tmpfiles.append(tmpfile)
        return tmpfile

    def new_group(self, experimenters = None):
        admin = self.root.sf.getAdminService()
        gname = str(uuid())
        group = omero.model.ExperimenterGroupI()
        group.name = rstring(gname)
        gid = admin.createGroup(group)
        group = admin.getGroup(gid)
        if experimenters:
            for exp in experimenters:
                admin.addGroups(exp, [group])
        return group

    def new_image(self, name = ""):
        img = omero.model.ImageI()
        img.name = rstring(name)
        img.acquisitionDate = rtime(0)
        return img

    def new_user(self, group = None, perms = "rwr---"):

        if not self.root:
            raise exceptions.Exception("No root client. Cannot create user")

        admin = self.root.getSession().getAdminService()
        name = self.uuid()

        # Create group if necessary
        if not group:
            group = name
            g = omero.model.ExperimenterGroupI()
            g.name = rstring(group)
            g.details.permissions = omero.model.PermissionsI(perms)
            gid = admin.createGroup(g)
            g = omero.model.ExperimenterGroupI(gid, False)

        # Create user
        e = omero.model.ExperimenterI()
        e.omeName = rstring(name)
        e.firstName = rstring(name)
        e.lastName = rstring(name)
        uid = admin.createUser(e, group)
        return admin.getExperimenter(uid)

    def new_client(self, group = None):
        """
        Like new_user() but returns an active client.
        """
        user = self.new_user(group)
        props = self.root.getPropertyMap()
        props["omero.user"] = user.omeName.val
        props["omero.pass"] = user.omeName.val
        client = omero.client(props)
        client.setAgent("OMERO.py.new_client_test")
        client.createSession()
        return client

    def tearDown(self):
        failure = False
        try:
            self.client.closeSession()
        except:
            traceback.print_exc()
            failure = True
        if self.root:
            try:
                self.root.closeSession()
            except:
                traceback.print_exc()
                failure = True
        for tmpfile in self.tmpfiles:
            try:
                tmpfile.close()
            except:
                print "Error closing:"+tmpfile
        if failure:
           raise exceptions.Exception("Exception on client.closeSession")
