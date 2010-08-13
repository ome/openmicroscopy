#!/usr/bin/env python

"""
   Library for integration tests

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import Ice
import sys
import time
import weakref
import unittest
import tempfile
import traceback
import exceptions
import subprocess

import omero

from omero.util.temp_files import create_path
from omero.rtypes import rstring, rtime
from uuid import uuid4 as uuid
from path import path


class Clients(object):

    def __init__(self):
        self.__clients = set()

    def  __del__(self):
        try:
            for client_ref in self.__clients:
                client = client_ref()
                if client:
                    client.__del__()
        finally:
            self.__clients = set()

    def add(self, client):
        self.__clients.add(weakref.ref(client))


class ITest(unittest.TestCase):

    def setUp(self):

        self.OmeroPy = self.omeropydir()

        self.__clients = Clients()

        p = Ice.createProperties(sys.argv)
        rootpass = p.getProperty("omero.rootpass")

        name = None
        pasw = None
        if rootpass:
            self.root = omero.client()
            self.__clients.add(self.root)
            self.root.setAgent("OMERO.py.root_test")
            self.root.createSession("root", rootpass)
            newuser = self.new_user()
            name = newuser.omeName.val
            pasw = "1"
        else:
            self.root = None

        self.client = omero.client()
        self.__clients.add(self.client)
        self.client.setAgent("OMERO.py.test")
        self.sf = self.client.createSession(name, pasw)

        self.update = self.sf.getUpdateService()
        self.query = self.sf.getQueryService()


    def omeropydir(self):
        count = 10
        searched = []
        p = path(".").abspath()
        while str(p.basename()) not in ("OmeroPy", ""): # "" means top of directory
            searched.append(p)
            p = p / ".." # Walk up, in case test runner entered a subdirectory
            p = p.abspath()
            count -= 1
            if not count:
                break
        if str(p.basename()) == "OmeroPy":
            return p
        else:
            self.fail("Could not find OmeroPy/; searched %s" % searched)

    def uuid(self):
        return str(uuid())

    def login_args(self):
        p = self.client.ic.getProperties()
        host = p.getProperty("omero.host")
        key = self.sf.ice_getIdentity().name
        return ["-s", host, "-k", key] # TODO PORT

    def root_login_args(self):
        p = self.root.ic.getProperties()
        host = p.getProperty("omero.host")
        key = self.root.sf.ice_getIdentity().name
        return ["-s", host, "-k", key] # TODO PORT

    def tmpfile(self):
        return str(create_path())

    def new_group(self, experimenters = None, perms = None):
        admin = self.root.sf.getAdminService()
        gname = str(uuid())
        group = omero.model.ExperimenterGroupI()
        group.name = rstring(gname)
        if perms:
            group.details.permissions = omero.model.PermissionsI(perms)
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

    def import_image(self, filename = None):
        if filename is None:
            filename = self.OmeroPy / ".." / ".." / ".." / "components" / "common" / "test" / "tinyTest.d3d.dv"


        server = self.client.getProperty("omero.host")
        key = self.client.getSessionId()

        # Search up until we find "OmeroPy"
        dist_dir = self.OmeroPy / ".." / ".." / ".." / "dist"
        args = ["python"]
        args.append(str(path(".") / "bin" / "omero"))
        args.extend(["-s", server, "-k", key, "import", filename])
        popen = subprocess.Popen(args, cwd=str(dist_dir), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        out, err = popen.communicate()
        rc = popen.wait()
        if rc != 0:
            raise exceptions.Exception("import failed: %s\n%s" % (rc, err))
        pix_ids = []
        for x in out.split("\n"):
            if x and x.find("Created") < 0 and x.find("#") < 0:
                try:    # if the line has an image ID...
                    imageId = str(long(x.strip()))
                    pix_ids.append(imageId)
                except: pass
        return pix_ids

    def index(self, *objs):
        if objs:
            for obj in objs:
                self.root.sf.getUpdateService().indexObject(obj)

    def new_user(self, group = None, perms = None):

        if not self.root:
            raise exceptions.Exception("No root client. Cannot create user")

        admin = self.root.getSession().getAdminService()
        name = self.uuid()

        # Create group if necessary
        if not group:
            g = self.new_group(perms = perms)
            group = g.name.val
        elif isinstance(group, omero.model.ExperimenterGroup):
            g = group
            group = g.name.val
        else:
            pass # Group is already name

        # Create user
        e = omero.model.ExperimenterI()
        e.omeName = rstring(name)
        e.firstName = rstring(name)
        e.lastName = rstring(name)
        uid = admin.createUser(e, group)
        return admin.getExperimenter(uid)

    def new_client(self, group = None, user = None, perms = None):
        """
        Like new_user() but returns an active client.
        """
        if user is None:
            user = self.new_user(group, perms)
        props = self.root.getPropertyMap()
        props["omero.user"] = user.omeName.val
        props["omero.pass"] = user.omeName.val
        client = omero.client(props)
        self.__clients.add(client)
        client.setAgent("OMERO.py.new_client_test")
        client.createSession()
        return client

    def new_client_and_user(self, group = None):
        user = self.new_user(group)
        client = self.new_client(group, user)
        return client, user

    def timeit(self, func, *args, **kwargs):
        start = time.time()
        rv = func(*args, **kwargs)
        stop = time.time()
        elapsed = stop - start
        return elapsed, rv

    def tearDown(self):
        failure = False
        self.__clients.__del__()
