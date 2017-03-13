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
   Library for integration tests

"""

import platform
import locale
import os
import sys
import time
import weakref
import logging
import subprocess
import pytest
import uuid

import Ice
import Glacier2
import omero
import omero.gateway

from omero_version import omero_version
from collections import defaultdict
from omero.cmd import DoAll, State, ERR, OK, Chmod2, Chgrp2, Delete2
from omero.callbacks import CmdCallbackI
from omero.model import DatasetI, DatasetImageLinkI, ImageI, ProjectI
from omero.model import Annotation, FileAnnotationI, TagAnnotationI
from omero.model import OriginalFileI
from omero.model import DimensionOrderI, PixelsI, PixelsTypeI
from omero.model import Experimenter, ExperimenterI
from omero.model import ExperimenterGroup, ExperimenterGroupI
from omero.model import ProjectDatasetLinkI, ImageAnnotationLinkI
from omero.model import PermissionsI
from omero.model import ChecksumAlgorithmI
from omero.model import NamedValue as NV
from omero.rtypes import rbool, rstring, rlong, rtime, rint, unwrap
from omero.util.temp_files import create_path
from path import path


class Clients(object):

    def __init__(self):
        self.__clients = set()

    def __del__(self):
        try:
            for client_ref in self.__clients:
                client = client_ref()
                if client:
                    client.__del__()
        finally:
            self.__clients = set()

    def add(self, client):
        self.__clients.add(weakref.ref(client))


class ITest(object):

    log = logging.getLogger("ITest")
    # Default permissions for the group created in setup_class
    # Can be overriden by test instances
    DEFAULT_PERMS = 'rw----'
    # If the new user created in setup_class should own their group
    # Can be overriden by test instances
    DEFAULT_GROUP_OWNER = False

    @classmethod
    def setup_class(cls):

        cls.omero_dist = cls.omerodistdir()

        cls.__clients = Clients()

        # Create a root client
        p = Ice.createProperties(sys.argv)
        rootpass = p.getProperty("omero.rootpass")

        try:
            cls.root = omero.client()  # ok because adds self
            cls.__clients.add(cls.root)
            cls.root.setAgent("OMERO.py.root_test")
            cls.root.createSession("root", rootpass)
            cls.root.getSession().keepAlive(None)
        except:
            raise Exception("Could not initiate a root connection")

        cls.group = cls.new_group(perms=cls.DEFAULT_PERMS)
        cls.user = cls.new_user(group=cls.group, owner=cls.DEFAULT_GROUP_OWNER)
        cls.client = omero.client()  # ok because adds self
        cls.__clients.add(cls.client)
        cls.client.setAgent("OMERO.py.test")
        cls.sf = cls.client.createSession(
            cls.user.omeName.val, cls.user.omeName.val)
        cls.ctx = cls.sf.getAdminService().getEventContext()
        cls.update = cls.sf.getUpdateService()
        cls.query = cls.sf.getQueryService()

    @classmethod
    def teardown_class(cls):
        cls.root.killSession()
        cls.root = None
        cls.__clients.__del__()

    def keepRootAlive(self):
        """
        Keeps root connection alive.
        """
        try:
            if self.root.sf is None:
                p = Ice.createProperties(sys.argv)
                rootpass = p.getProperty("omero.rootpass")
                self.root.createSession("root", rootpass)
            else:
                self.root.sf.keepAlive(None)
        except Exception:
            raise

    @classmethod
    def omerodistdir(cls):
        def travers(p):
            dist = None
            for root, dirs, files in os.walk(p):
                for f in files:
                    t = path(os.path.join(root, f))
                    # find OMERO dist by searching for bin/omero
                    # exclude OmeroPy as is a source
                    if (str(t.dirname().dirname().basename())
                            not in ('OmeroPy', 'build', 'target') and
                            str(t.basename()) == 'omero' and
                            str(t.dirname().basename()) == 'bin'):
                        dist = t.dirname().dirname()
            return dist
        count = 10
        searched = []
        p = path(".").abspath()
        # "" means top of directory
        dist_dir = None
        while dist_dir is None:
            dist_dir = travers(p)
            searched.append(p)
            p = p / ".."  # Walk up, in case test runner entered a subdirectory
            p = p.abspath()
            count -= 1
            if not count:
                break
        if dist_dir is not None:
            return dist_dir
        else:
            assert False, ("Could not find bin/omero executable; "
                           "searched %s" % searched)

    def skip_if(self, config_key, condition, message=None):
        """Skip test if configuration does not meet condition"""
        config_service = self.root.sf.getConfigService()
        config_value = config_service.getConfigValue(config_key)
        if condition(config_value):
            pytest.skip(message or '%s:%s does not meet condition'
                        % (config_key, config_value))

    @classmethod
    def uuid(cls):
        return str(uuid.uuid4())

    @classmethod
    def login_args(cls, client=None):
        p = cls.client.ic.getProperties()
        host = p.getProperty("omero.host")
        port = p.getProperty("omero.port")
        if not client:
            key = cls.sf.ice_getIdentity().name
        else:
            key = client.sf.ice_getIdentity().name
        return ["-q", "-s", host, "-k", key, "-p", port]

    @classmethod
    def root_login_args(cls):
        p = cls.root.ic.getProperties()
        host = p.getProperty("omero.host")
        port = p.getProperty("omero.port")
        key = cls.root.sf.ice_getIdentity().name
        return ["-s", host, "-k", key, "-p", port]

    def tmpfile(self):
        return str(create_path())

    # Administrative methods
    @classmethod
    def new_group(cls, experimenters=None, perms=None,
                  config=None, gname=None):
        admin = cls.root.sf.getAdminService()
        if gname is None:
            gname = cls.uuid()
        group = ExperimenterGroupI()
        group.name = rstring(gname)
        group.ldap = rbool(False)
        group.config = config
        if perms:
            group.details.permissions = PermissionsI(perms)
        gid = admin.createGroup(group)
        group = admin.getGroup(gid)
        cls.add_experimenters(group, experimenters)
        return group

    @classmethod
    def add_experimenters(cls, group, experimenters):
        admin = cls.root.sf.getAdminService()
        if experimenters:
            for exp in experimenters:
                user, name = cls.user_and_name(exp)
                admin.addGroups(user, [group])

    def add_groups(self, experimenter, groups, owner=False):
        admin = self.root.sf.getAdminService()
        for group in groups:
            user, name = self.user_and_name(experimenter)
            admin.addGroups(user, [group])
            if owner:
                admin.setGroupOwner(group, user)

    def remove_experimenters(self, group, experimenters):
        admin = self.root.sf.getAdminService()
        if experimenters:
            for exp in experimenters:
                user, name = self.user_and_name(exp)
                admin.removeGroups(user, [group])

    def set_context(self, client, gid):
        rv = client.getStatefulServices()
        for prx in rv:
            prx.close()
        client.sf.setSecurityContext(ExperimenterGroupI(gid, False))

    # Import methods
    def import_image(self, filename=None, client=None, extra_args=None,
                     skip="all", **kwargs):
        if filename is None:
            filename = self.omero_dist / ".." / \
                "components" / "common" / "test" / "tinyTest.d3d.dv"
        if client is None:
            client = self.client

        server = client.getProperty("omero.host")
        port = client.getProperty("omero.port")
        key = client.getSessionId()

        args = [sys.executable]
        args.append(str(path(".") / "bin" / "omero"))
        args.extend(["-s", server, "-k", key, "-p", port, "import"])
        if skip:
            args.extend(["--skip", skip])
        args.extend(["--"])
        if extra_args:
            args.extend(extra_args)
        args.append(filename)

        popen = subprocess.Popen(args, cwd=str(self.omero_dist),
                                 stdout=subprocess.PIPE,
                                 stderr=subprocess.PIPE)
        out, err = popen.communicate()
        rc = popen.wait()
        if rc != 0:
            raise Exception("import failed: [%r] %s\n%s" % (args, rc, err))
        pix_ids = []
        for x in out.split("\n"):
            if x and x.find("Created") < 0 and x.find("#") < 0:
                try:    # if the line has an image ID...
                    imageId = str(long(x.strip()))
                    # Occasionally during tests an id is duplicated on stdout
                    if imageId not in pix_ids:
                        pix_ids.append(imageId)
                except:
                    pass
        return pix_ids

    """
    Creates a fake file with one image, imports
    the file and then return the image.
    """

    def importSingleImage(self, name=None, client=None,
                          with_companion=False, **kwargs):
        if client is None:
            client = self.client
        if name is None:
            name = "importSingleImage"

        images = self.importMIF(1, name=name, client=client,
                                with_companion=with_companion,
                                **kwargs)
        return images[0]

    """
    Creates a fake file with one image and a companion file, imports
    the file and then return the image..
    """

    def importSingleImageWithCompanion(self, name=None, client=None):
        if client is None:
            client = self.client
        if name is None:
            name = "importSingleImageWithCompanion"

        images = self.importMIF(1, name=name, client=client,
                                with_companion=True)
        return images[0]

    """
    Creates a fake file with a seriesCount of images, imports
    the file and then return the list of images.
    """

    def importMIF(self, seriesCount=0, name=None, client=None,
                  with_companion=False, skip="all", **kwargs):
        if client is None:
            client = self.client
        if name is None:
            name = "importMIF"

        try:
            globalMetadata = kwargs.pop("GlobalMetadata")
        except:
            globalMetadata = None
        if globalMetadata:
            with_companion = True

        append = ""

        # Only include series count if enabled; in the case of plates,
        # this will be unused
        if seriesCount >= 1:
            append = "series=%d%s" % (seriesCount, append)

        if kwargs:
            for k, v in kwargs.items():
                append += "&%s=%s" % (k, v)

        query = client.sf.getQueryService()
        fake = create_path(name, "&%s.fake" % append)
        if with_companion:
            ini = open(fake.abspath() + ".ini", "w")
            if globalMetadata:
                ini.write("[GlobalMetadata]\n")
                for k, v in globalMetadata.items():
                    ini.write("%s=%s\n" % (k, v))
            ini.close()

        pixelIds = self.import_image(
            filename=fake.abspath(), client=client, skip=skip, **kwargs)

        if seriesCount >= 1:
            assert seriesCount == len(pixelIds)

        images = []
        for pixIdStr in pixelIds:
            pixels = query.get("Pixels", long(pixIdStr))
            images.append(pixels.getImage())
        return images

    def importPlates(
        self, client=None,
        plates=1, plateAcqs=1,
        plateCols=1, plateRows=1,
        fields=1, screens=0, **kwargs
    ):

        if client is None:
            client = self.client

        kwargs["screens"] = screens
        kwargs["plates"] = plates
        kwargs["plateAcqs"] = plateAcqs
        kwargs["plateCols"] = plateCols
        kwargs["plateRows"] = plateRows
        kwargs["fields"] = fields
        images = self.importMIF(client=client, **kwargs)
        images = [x.id.val for x in images]

        query = client.sf.getQueryService()
        plates = query.findAllByQuery((
            "select p from Plate p "
            "join p.wells as w "
            "join w.wellSamples as ws "
            "join ws.image as i "
            "where i.id in (:ids)"),
            omero.sys.ParametersI().addIds(images))
        return plates

    def createTestImage(self, sizeX=16, sizeY=16, sizeZ=1, sizeC=1, sizeT=1,
                        session=None, thumb=True):
        """
        Creates a test image of the required dimensions, where each pixel
        value is set to the value of x+y.
        Returns the image (ImageI)
        """
        from numpy import fromfunction, int16
        from omero.util import script_utils

        if session is None:
            session = self.root.sf
        renderingEngine = session.createRenderingEngine()
        queryService = session.getQueryService()
        pixelsService = session.getPixelsService()
        rawPixelStore = session.createRawPixelsStore()
        containerService = session.getContainerService()

        def f1(x, y):
            return y

        def f2(x, y):
            return (x + y) / 2

        def f3(x, y):
            return x

        pType = "int16"
        # look up the PixelsType object from DB
        # omero::model::PixelsType
        pixelsType = queryService.findByQuery(
            "from PixelsType as p where p.value='%s'" % pType, None)
        if pixelsType is None and pType.startswith("float"):    # e.g. float32
            # omero::model::PixelsType
            pixelsType = queryService.findByQuery(
                "from PixelsType as p where p.value='%s'" % "float", None)
        if pixelsType is None:
            print "Unknown pixels type for: " % pType
            raise Exception("Unknown pixels type for: " % pType)

        # code below here is very similar to combineImages.py
        # create an image in OMERO and populate the planes with numpy 2D arrays
        channelList = range(1, sizeC + 1)
        iId = pixelsService.createImage(sizeX, sizeY, sizeZ, sizeT,
                                        channelList, pixelsType,
                                        "testImage", "description")
        imageId = iId.getValue()
        image = containerService.getImages("Image", [imageId], None)[0]

        pixelsId = image.getPrimaryPixels().getId().getValue()
        rawPixelStore.setPixelsId(pixelsId, True)

        colourMap = {0: (0, 0, 255, 255), 1: (0, 255, 0, 255),
                     2: (255, 0, 0, 255), 3: (255, 0, 255, 255)}
        fList = [f1, f2, f3]
        for theC in range(sizeC):
            minValue = 0
            maxValue = 0
            f = fList[theC % len(fList)]
            for theZ in range(sizeZ):
                for theT in range(sizeT):
                    plane2D = fromfunction(f, (sizeY, sizeX), dtype=int16)
                    script_utils.uploadPlane(
                        rawPixelStore, plane2D, theZ, theC, theT)
                    minValue = min(minValue, plane2D.min())
                    maxValue = max(maxValue, plane2D.max())
            pixelsService.setChannelGlobalMinMax(
                pixelsId, theC, float(minValue), float(maxValue))
            rgba = None
            if theC in colourMap:
                rgba = colourMap[theC]
        for theC in range(sizeC):
            script_utils.resetRenderingSettings(
                renderingEngine, pixelsId, theC, minValue, maxValue, rgba)

        renderingEngine.close()
        rawPixelStore.close()

        if thumb:
            # See #9070. Forcing a thumbnail creation
            tb = session.createThumbnailStore()
            try:
                s = tb.getThumbnailByLongestSideSet(rint(16), [pixelsId])
                assert s[pixelsId] != ''

            finally:
                tb.close()

        # Reloading image to prevent error on old pixels updateEvent
        image = containerService.getImages("Image", [imageId], None)[0]
        return image

    def get_fileset(self, i, client=None):
        """
        Takes an image object and return a fileset object
        """
        if client is None:
            client = self.client
        query = client.sf.getQueryService()

        params = omero.sys.ParametersI()
        params.addIds([x.id.val for x in i])
        query1 = "select fs from Fileset fs "\
            "left outer join fetch fs.images as image "\
            "where image.id in (:ids)"
        rv = unwrap(query.projection(query1, params))
        return rv[0][0]

    def index(self, *objs):
        if objs:
            for obj in objs:
                self.root.sf.getUpdateService().indexObject(
                    obj, {"omero.group": "-1"})

    def waitOnCmd(self, client, handle, loops=10, ms=500, passes=True):
        """
        Wait on an omero.cmd.HandlePrx to finish processing
        and then assert pass or fail. The callback is returned
        for accessing the Response and Status elements.
        """
        callback = omero.callbacks.CmdCallbackI(client, handle)
        callback.loop(loops, ms)  # throws on timeout
        rsp = callback.getResponse()
        is_ok = isinstance(rsp, OK)
        assert passes == is_ok, str(rsp)
        return callback

    @classmethod
    def new_user(cls, group=None, perms=None,
                 owner=False, system=False, uname=None,
                 email=None):
        """
        :owner: If user is to be an owner of the created group
        :system: If user is to be a system admin
        """

        if not cls.root:
            raise Exception("No root client. Cannot create user")

        adminService = cls.root.getSession().getAdminService()
        if uname is None:
            uname = cls.uuid()

        # Create group if necessary
        if not group:
            g = cls.new_group(perms=perms)
            group = g.getName().getValue()
        else:
            g, group = cls.group_and_name(group)

        # Create user
        e = ExperimenterI()
        e.omeName = rstring(uname)
        e.firstName = rstring(uname)
        e.lastName = rstring(uname)
        e.ldap = rbool(False)
        e.email = rstring(email)
        listOfGroups = list()
        listOfGroups.append(adminService.lookupGroup('user'))
        uid = adminService.createExperimenterWithPassword(
            e, rstring(uname), g, listOfGroups)
        e = adminService.lookupExperimenter(uname)
        if owner:
            adminService.setGroupOwner(g, e)
        if system:
            adminService.addGroups(e, [ExperimenterGroupI(0, False)])

        return adminService.getExperimenter(uid)

    def new_client(self, group=None, user=None, perms=None,
                   owner=False, system=False, session=None,
                   password=None, email=None):
        """
        Like new_user() but returns an active client.

        Passing user= or session= will prevent self.new_user
        from being called, and instead the given user (by name
        or ExperimenterI) or session will be logged in.
        """
        props = self.root.getPropertyMap()
        if session is not None:
            if user is not None:
                self.log.warning("user= argument will be ignored: %s", user)
            session = unwrap(session)
            props["omero.user"] = session
            props["omero.pass"] = session
        else:
            if user is not None:
                user, name = self.user_and_name(user)
            else:
                user = self.new_user(group, perms, owner=owner,
                                     system=system, email=email)
            props["omero.user"] = user.omeName.val
            if password is not None:
                props["omero.pass"] = password
            else:
                props["omero.pass"] = user.omeName.val

        client = omero.client(props)
        self.__clients.add(client)
        client.setAgent("OMERO.py.new_client_test")
        client.createSession()
        return client

    def new_client_and_user(self, group=None, perms=None,
                            owner=False, system=False):
        user = self.new_user(group, owner=owner, system=system, perms=perms)
        client = self.new_client(
            group, user, perms=perms, owner=owner, system=system)
        return client, user

    def timeit(self, func, *args, **kwargs):
        start = time.time()
        rv = func(*args, **kwargs)
        stop = time.time()
        elapsed = stop - start
        return elapsed, rv

    @classmethod
    def group_and_name(cls, group):
        group = unwrap(group)
        admin = cls.root.sf.getAdminService()
        if isinstance(group, (int, long)):
            group = admin.getGroup(group)
            name = group.name.val
        elif isinstance(group, ExperimenterGroup):
            if group.isLoaded():
                name = group.name.val
                group = admin.lookupGroup(name)
            else:
                group = admin.getGroup(group.id.val)
                name = group.name.val
        elif isinstance(group, (str, unicode)):
            name = group
            group = admin.lookupGroup(name)
        elif isinstance(group, Experimenter):
            assert False,\
                "group is a user! Try adding group= to your method invocation"
        else:
            assert False, "Unknown type: %s=%s" % (type(group), group)

        return group, name

    @classmethod
    def user_and_name(cls, user):
        user = unwrap(user)
        admin = cls.root.sf.getAdminService()
        if isinstance(user, omero.clients.BaseClient):
            admin = user.sf.getAdminService()
            ec = admin.getEventContext()
            name = ec.userName
            user = admin.lookupExperimenter(name)
        elif isinstance(user, Experimenter):
            if user.isLoaded():
                name = user.omeName.val
                user = admin.lookupExperimenter(name)
            else:
                user = admin.getExperimenter(user.id.val)
                name = user.omeName.val
        elif isinstance(user, (str, unicode)):
            name = user
            user = admin.lookupExperimenter(name)
        elif isinstance(user, ExperimenterGroup):
            assert False,\
                "user is a group! Try adding user= to your method invocation"
        else:
            assert False, "Unknown type: %s=%s" % (type(user), user)

        return user, name

    #
    # Data methods
    #

    def missing_pyramid(self, client=None):
        """
        Creates and returns a pixels whose shape changes from
        1,1,4000,4000,1 to 4000,4000,1,1,1 making it a pyramid
        candidate but without the pyramid which is created on
        initial import in 4.3+. This simulates a big image that
        was imported in 4.2.
        """

        if client is None:
            client = self.client

        fake = create_path("missing_pyramid", "&sizeX=4000&sizeY=4000.fake")
        pixelsId = self.import_image(filename=fake.abspath(), client=client,
                                     skip="all")
        return pixelsId[0]

    def pix(self, x=10, y=10, z=10, c=3, t=50, client=None):
        """
        Creates an int8 pixel of the given size in the database.
        No data is written.
        """
        image = self.new_image()
        pixels = PixelsI()
        pixels.sizeX = rint(x)
        pixels.sizeY = rint(y)
        pixels.sizeZ = rint(z)
        pixels.sizeC = rint(c)
        pixels.sizeT = rint(t)
        pixels.sha1 = rstring("")
        pixels.pixelsType = PixelsTypeI()
        pixels.pixelsType.value = rstring("int8")
        pixels.dimensionOrder = DimensionOrderI()
        pixels.dimensionOrder.value = rstring("XYZCT")
        image.addPixels(pixels)

        if client is None:
            client = self.client
        update = client.sf.getUpdateService()
        image = update.saveAndReturnObject(image)
        pixels = image.getPrimaryPixels()
        return pixels

    def write(self, pix, rps):
        """
        Writes byte arrays consisting of [5] to as
        either planes or tiles depending on the pixel
        size.
        """
        if not rps.requiresPixelsPyramid():
            # By plane
            bytes_per_plane = pix.sizeX.val * pix.sizeY.val  # Assuming int8
            for z in range(pix.sizeZ.val):
                for c in range(pix.sizeC.val):
                    for t in range(pix.sizeT.val):
                        rps.setPlane([5] * bytes_per_plane, z, c, t)
        else:
            # By tile
            w, h = rps.getTileSize()
            bytes_per_tile = w * h  # Assuming int8
            for z in range(pix.sizeZ.val):
                for c in range(pix.sizeC.val):
                    for t in range(pix.sizeT.val):
                        for x in range(0, pix.sizeX.val, w):
                            for y in range(0, pix.sizeY.val, h):

                                changed = False
                                if x + w > pix.sizeX.val:
                                    w = pix.sizeX.val - x
                                    changed = True
                                if y + h > pix.sizeY.val:
                                    h = pix.sizeY.val - y
                                    changed = True
                                if changed:
                                    # Again assuming int8
                                    bytes_per_tile = w * h

                                args = ([5] * bytes_per_tile,
                                        z, c, t, x, y, w, h)
                                rps.setTile(*args)

    def open_jpeg_buffer(self, buf):
        try:
            from PIL import Image
        except ImportError:
            try:
                import Image
            except ImportError:
                assert False, "Pillow not installed"
        from cStringIO import StringIO
        tfile = StringIO(buf)
        jpeg = Image.open(tfile)  # Raises if invalid
        return jpeg

    def loginAttempt(self, name, t, pw="BAD", less=False):
        """
        Checks that login happens in less than or greater than
        the given time. By default, the password "BAD" is used,
        and the expectation is that login will take greather
        than the specified time since the password won't match.
        To check that logins happen more quickly, pass the
        correct password and less=True:

            loginAttempt("user", 0.15, pw="REALVALUE", less=True)

        See integration.tickets4000 and 5000
        """
        c = omero.client()  # ok because followed by __del__
        try:
            t1 = time.time()
            try:
                c.createSession(name, pw)
                if pw == "BAD":
                    assert False, "Should not reach this point"
            except Glacier2.PermissionDeniedException:
                if pw != "BAD":
                    raise
            t2 = time.time()
            T = (t2 - t1)
            if less:
                assert T < t, "%s > %s" % (T, t)
            else:
                assert T > t, "%s < %s" % (T, t)
        finally:
            c.__del__()

    def doSubmit(self, request, client, test_should_pass=True,
                 omero_group=None):
        """
        Performs the request(s), waits on completion and checks that the
        result is not an error. The request can either be a single command
        or a list of commands. If the latter then the request list will be
        wrapped in a DoAll.
        """
        if isinstance(request, list):
            request = DoAll(request)

        sf = client.sf
        if omero_group is not None:
            prx = sf.submit(request, {'omero.group': str(omero_group)})
        else:
            prx = sf.submit(request)

        assert State.FAILURE not in prx.getStatus().flags

        cb = CmdCallbackI(client, prx)
        cb.loop(20, 500)

        assert prx.getResponse() is not None

        rsp = prx.getResponse()

        if test_should_pass:
            if isinstance(rsp, ERR):
                assert False, (
                    "Found ERR when test_should_pass==true: %s (%s) params=%s"
                    % (rsp.category, rsp.name, rsp.parameters))
            assert State.FAILURE not in prx.getStatus().flags
        else:
            if isinstance(rsp, OK):
                assert False, (
                    "Found OK when test_should_pass==false: %s" % rsp)
            assert State.FAILURE in prx.getStatus().flags

        return rsp

    # Object methods
    def new_object(self, classname, name=None, description=None):
        obj = classname()
        if not name:
            name = self.uuid()
        obj.setName(rstring(name))
        obj.setDescription(rstring(description))
        return obj

    def new_image(self, name=None, description=None, date=0):
        """
        Creates a new image object.
        If no name has been provided, a UUID string shall be used.
        :param name: The image name. If None, a UUID string will be used
        :param description: The image description
        :param date: The image acquisition date
        """
        img = self.new_object(ImageI, name=name, description=description)
        img.acquisitionDate = rtime(date)
        return img

    def new_project(self, name=None, description=None):
        """
        Creates a new project object.
        :param name: The project name. If None, a UUID string will be used
        :param description: The project description
        """
        return self.new_object(ProjectI, name=name, description=description)

    def new_dataset(self, name=None, description=None):
        """
        Creates a new dataset object.
        :param name: The dataset name. If None, a UUID string will be used
        :param description: The dataset description
        """
        return self.new_object(DatasetI, name=name, description=description)

    def new_tag(self, name=None, ns=None):
        """
        Creates a new tag object.
        :param name: The tag name. If None, a UUID string will be used
        :param ns: The namespace for the annotation. If None, do not set.
        """
        tag = self.new_object(TagAnnotationI, name=name)
        tag.setTextValue(rstring(name))
        if ns is not None:
            tag.setNs(rstring(ns))
        return tag

    def make_image(self, name=None, description=None, date=0, client=None):
        """
        Creates a new image instance and returns the persisted object.
        :param name: The image name. If None, a UUID string will be used
        :param description: The image description
        :param date: The image acquisition date
        :param client: The client to use to create the object
        """
        if client is None:
            client = self.client
        image = self.new_image(name=name, description=description, date=date)
        return client.sf.getUpdateService().saveAndReturnObject(image)

    def make_project(self, name=None, description=None, client=None):
        """
        Creates a new project instance and returns the persisted object.
        :param name: The project name. If None, a UUID string will be used
        :param description: The project description
        :param client: The client to use to create the object
        """
        if client is None:
            client = self.client
        project = self.new_project(name=name, description=description)
        return client.sf.getUpdateService().saveAndReturnObject(project)

    def make_dataset(self, name=None, description=None, client=None):
        """
        Creates a new dataset instance and returns the persisted object.
        :param name: The dataset name. If None, a UUID string will be used
        :param description: The dataset description
        :param client: The client to use to create the object
        """
        if client is None:
            client = self.client
        dataset = self.new_dataset(name=name, description=description)
        return client.sf.getUpdateService().saveAndReturnObject(dataset)

    def make_tag(self, name=None, client=None, ns=None):
        """
        Creates a new tag instance and returns the persisted object.
        :param name: The tag name. If None, a UUID string will be used
        :param client: The client to use to create the object
        :param ns: The namespace for the annotation. If None, do not set.
        """
        if client is None:
            client = self.client
        tag = self.new_tag(name=name, ns=ns)
        return client.sf.getUpdateService().saveAndReturnObject(tag)

    def createDatasets(self, count, baseName, client=None):
        """
        Creates a list of the given number of Dataset instances with names of
        the form "name [1]", "name [2]" etc and returns them in a list.
        :param count: The number of datasets to create
        :param description: The base name of the dataset
        :param client: The client to use to create the object
        """

        if client is None:
            client = self.client

        update = client.sf.getUpdateService()
        dsets = []
        for i in range(count):
            name = baseName + " [" + str(i + 1) + "]"
            dsets.append(self.new_dataset(name=name))
        return update.saveAndReturnArray(dsets)

    def make_file_annotation(self, name=None, binary=None, format=None,
                             client=None, ns=None):
        """
        Creates a new DatasetI instance and returns the persisted object.
        If no name has been provided, a UUID string shall be used.

        :param name: the name of the project
        :param client: The client to use to create the object
        :param ns: The namespace for the annotation
        """

        if client is None:
            client = self.client
        update = client.sf.getUpdateService()

        # file
        if format is None:
            format = "application/octet-stream"
        if binary is None:
            binary = "12345678910"
        if name is None:
            name = str(self.uuid())

        oFile = OriginalFileI()
        oFile.setName(rstring(name))
        oFile.setPath(rstring(str(self.uuid())))
        oFile.setSize(rlong(len(binary)))
        oFile.hasher = ChecksumAlgorithmI()
        oFile.hasher.value = rstring("SHA1-160")
        oFile.setMimetype(rstring(str(format)))
        oFile = update.saveAndReturnObject(oFile)

        # save binary
        store = client.sf.createRawFileStore()
        store.setFileId(oFile.id.val)
        store.write(binary, 0, len(binary))
        oFile = store.save()  # See ticket:1501
        store.close()

        fa = FileAnnotationI()
        fa.setFile(oFile)
        if ns is not None:
            fa.setNs(rstring(ns))
        return update.saveAndReturnObject(fa)

    def link(self, obj1, obj2, client=None):
        """
        Links two linkable model entities together by creating an instance of
        the correct link entity (e.g. ProjectDatasetLinkI) and persisting it
        in the DB. Accepts client instance to allow calls to happen in correct
        user contexts. Currently support links are:
          * project/dataset
          * dataset/image
          * image/annotation

        :param obj1: parent object
        :param obj2: child object
        :param client: The client to use to create the link
        """
        if client is None:
            client = self.client
        if isinstance(obj1, ProjectI):
            if isinstance(obj2, DatasetI):
                link = ProjectDatasetLinkI()
        elif isinstance(obj1, DatasetI):
            if isinstance(obj2, ImageI):
                link = DatasetImageLinkI()
        elif isinstance(obj1, ImageI):
            if isinstance(obj2, Annotation):
                link = ImageAnnotationLinkI()
        else:
            assert False, "Object type not supported."

        """check if object exist or not"""
        if obj1.id is None:
            link.setParent(obj1)
        else:
            link.setParent(obj1.proxy())
        if obj2.id is None:
            link.setChild(obj2)
        else:
            link.setChild(obj2.proxy())
        return client.sf.getUpdateService().saveAndReturnObject(link)

    def delete(self, objs):
        """
        Deletes model entities (ProjectI, DatasetI, ImageI, etc)
        by creating Delete2 commands and calling
        :func:`~test.ITest.doSubmit`.

        :param obj: a list of objects to be deleted
        """
        to_delete = defaultdict(list)
        for obj in objs:
            t = obj.__class__.__name__
            to_delete[t].append(obj.id.val)
        command = Delete2(targetObjects=to_delete)
        self.doSubmit(command, self.client)

    def change_group(self, obj, target, client=None):
        """
        Moves a list of model entities (ProjectI, DatasetI or ImageI)
        to the target group. Accepts a client instance to guarantee calls
        in correct user contexts. Creates Chgrp2 commands and calls
        :func:`~test.ITest.doSubmit`.

        :param obj: a list of objects to be moved
        :param target: the ID of the target group
        :param client: user context
        """
        if client is None:
            client = self.client
        if isinstance(obj[0], ProjectI):
            t = "Project"
        elif isinstance(obj[0], DatasetI):
            t = "Dataset"
        elif isinstance(obj[0], ImageI):
            t = "Image"
        else:
            assert False, "Object type not supported."

        ids = [i.id.val for i in obj]
        command = Chgrp2(targetObjects={t: ids}, groupId=target)

        self.doSubmit(command, client)

    def change_permissions(self, gid, perms, client=None):
        """
        Changes the permissions of an ExperimenterGroup object.
        Accepts a client instance to guarantee calls in correct user contexts.
        Creates Chmod2 commands and calls :func:`~test.ITest.doSubmit`.

        :param gid: id of an ExperimenterGroup
        :param perms: permissions string
        :param client: user context
        """
        if client is None:
            client = self.client

        command = Chmod2(
            targetObjects={'ExperimenterGroup': [gid]}, permissions=perms)

        self.doSubmit(command, client)

    def create_share(self, description="", timeout=None,
                     objects=[], experimenters=[], guests=[],
                     enabled=True, client=None):
        """
        Create share object

        :param objects: a list of objects to include in the share
        :param description: a string containing the description of the share
        :param timeout: the timeout of the share
        :param experimenters: a list of users associated with the share
        :param client: The client to use to create the share
        """
        if client is None:
            client = self.client
        share = client.sf.getShareService()
        return share.createShare(description, timeout, objects,
                                 experimenters, guests, enabled)


class ProjectionFixture(object):
    """
    Used to test the return values from:
        'select x.permissions from Object x'
    """

    def __init__(self, perms, writer, reader,
                 canRead,
                 canAnnotate=False, canDelete=False,
                 canEdit=False, canLink=False):
        self.perms = perms
        self.writer = writer
        self.reader = reader

        self.canRead = canRead
        self.canAnnotate = canAnnotate
        self.canDelete = canDelete
        self.canEdit = canEdit
        self.canLink = canLink

    def get_name(self):
        name = self.perms
        for e in [self.writer, self.reader]:
            name += "-"
            if "admin" in e:
                name += "admin"
            elif "owner" in e:
                name += "owner"
            else:
                name += "member"
        return name

PF = ProjectionFixture
PFS = (
    # Private group as root
    PF("rw----", "system-admin", "system-admin", 1, 0, 1, 1, 0),
    PF("rw----", "system-admin", "group-owner", 1, 0, 1, 1, 0),
    PF("rw----", "system-admin", "member2", 0),
    # Private group as group-owner
    PF("rw----", "group-owner", "system-admin", 1, 0, 1, 1, 0),
    PF("rw----", "group-owner", "group-owner", 1, 0, 1, 1, 0),
    PF("rw----", "group-owner", "member2", 0),
    # Private group as member
    PF("rw----", "member1", "system-admin", 1, 0, 1, 1, 0),
    PF("rw----", "member1", "group-owner", 1, 0, 1, 1, 0),
    PF("rw----", "member1", "member2", 0),
    # Read-only group as root
    PF("rwr---", "system-admin", "system-admin", 1, 1, 1, 1, 1),
    PF("rwr---", "system-admin", "group-owner", 1, 1, 1, 1, 1),
    PF("rwr---", "system-admin", "member2", 1, 0, 0, 0, 0),
    # Read-only group as group-owner
    PF("rwr---", "group-owner", "system-admin", 1, 1, 1, 1, 1),
    PF("rwr---", "group-owner", "group-owner", 1, 1, 1, 1, 1),
    PF("rwr---", "group-owner", "member2", 1, 0, 0, 0, 0),
    # Read-only group as member
    PF("rwr---", "member1", "system-admin", 1, 1, 1, 1, 1),
    PF("rwr---", "member1", "group-owner", 1, 1, 1, 1, 1),
    PF("rwr---", "member1", "member2", 1, 0, 0, 0, 0),
    # Read-annotate group as root
    PF("rwra--", "system-admin", "system-admin", 1, 1, 1, 1, 1),
    PF("rwra--", "system-admin", "group-owner", 1, 1, 1, 1, 1),
    PF("rwra--", "system-admin", "member2", 1, 1, 0, 0, 0),
    # Read-annotate group as group-owner
    PF("rwra--", "group-owner", "system-admin", 1, 1, 1, 1, 1),
    PF("rwra--", "group-owner", "group-owner", 1, 1, 1, 1, 1),
    PF("rwra--", "group-owner", "member2", 1, 1, 0, 0, 0),
    # Read-annotate group as member
    PF("rwra--", "member1", "system-admin", 1, 1, 1, 1, 1),
    PF("rwra--", "member1", "group-owner", 1, 1, 1, 1, 1),
    PF("rwra--", "member1", "member2", 1, 1, 0, 0, 0),
    # Read-write group as root
    PF("rwrw--", "system-admin", "system-admin", 1, 1, 1, 1, 1),
    PF("rwrw--", "system-admin", "group-owner", 1, 1, 1, 1, 1),
    PF("rwrw--", "system-admin", "member2", 1, 1, 1, 1, 1),
    # Read-write group as group-owner
    PF("rwrw--", "group-owner", "system-admin", 1, 1, 1, 1, 1),
    PF("rwrw--", "group-owner", "group-owner", 1, 1, 1, 1, 1),
    PF("rwrw--", "group-owner", "member2", 1, 1, 1, 1, 1),
    # Read-write group as member
    PF("rwrw--", "member1", "system-admin", 1, 1, 1, 1, 1),
    PF("rwrw--", "member1", "group-owner", 1, 1, 1, 1, 1),
    PF("rwrw--", "member1", "member2", 1, 1, 1, 1, 1),
)


class AbstractRepoTest(ITest):

    def setup_method(self, method):
        self.unique_dir = self.test_dir()

    def test_dir(self, client=None):
        if client is None:
            client = self.client
        mrepo = self.getManagedRepo(client=client)
        user_dir = self.user_dir(client=client)
        unique_dir = user_dir + "/" + self.uuid()  # ok
        mrepo.makeDir(unique_dir, True)
        return unique_dir

    def user_dir(self, client=None):
        if client is None:
            client = self.client
        ec = client.sf.getAdminService().getEventContext()
        return "%s_%s" % (ec.userName, ec.userId)

    def all(self, client):
        ctx = dict(client.getImplicitContext().getContext())
        ctx["omero.group"] = "-1"
        return ctx

    def getManagedRepo(self, client=None):
        if client is None:
            client = self.client
        mrepo = client.getManagedRepository()
        assert mrepo
        return mrepo

    def createFile(self, mrepo1, filename):
        rfs = mrepo1.file(filename, "rw")
        try:
            rfs.write("hi", 0, 2)
            ofile = rfs.save()
            return ofile
        finally:
            rfs.close()

    def assertWrite(self, mrepo2, filename, ofile):
        def _write(rfs):
            try:
                rfs.write("bye", 0, 3)
                assert "bye" == rfs.read(0, 3)
                # Resetting for other expectations
                rfs.truncate(2)
                rfs.write("hi", 0, 2)
                assert "hi" == rfs.read(0, 2)
            finally:
                rfs.close()

        # TODO: fileById is always "r"
        # rfs = mrepo2.fileById(ofile.id.val)
        # _write(rfs)

        rfs = mrepo2.file(filename, "rw")
        _write(rfs)

    def assertNoWrite(self, mrepo2, filename, ofile):
        def _nowrite(rfs):
            try:
                pytest.raises(omero.SecurityViolation,
                              rfs.write, "bye", 0, 3)
                assert "hi" == rfs.read(0, 2)
            finally:
                rfs.close()

        rfs = mrepo2.fileById(ofile.id.val)
        _nowrite(rfs)

        rfs = mrepo2.file(filename, "r")
        _nowrite(rfs)

        # Can't even acquire a writeable-rfs.
        pytest.raises(omero.SecurityViolation,
                      mrepo2.file, filename, "rw")

    def assertDirWrite(self, mrepo2, dirname):
        self.createFile(mrepo2, dirname + "/file2.txt")

    def assertNoDirWrite(self, mrepo2, dirname):
        # Also check that it's not possible to write
        # in someone else's directory.
        pytest.raises(omero.SecurityViolation,
                      self.createFile, mrepo2, dirname + "/file2.txt")

    def assertNoRead(self, mrepo2, filename, ofile):
        pytest.raises(omero.SecurityViolation,
                      mrepo2.fileById, ofile.id.val)
        pytest.raises(omero.SecurityViolation,
                      mrepo2.file, filename, "r")

    def assertRead(self, mrepo2, filename, ofile, ctx=None):
        def _read(rfs):
            try:
                assert "hi" == rfs.read(0, 2)
            finally:
                rfs.close()

        rfs = mrepo2.fileById(ofile.id.val, ctx)
        _read(rfs)

        rfs = mrepo2.file(filename, "r", ctx)
        _read(rfs)

    def assertListings(self, mrepo1, unique_dir):
        assert [unique_dir + "/b"] == mrepo1.list(unique_dir + "/")
        assert [unique_dir + "/b/c"] == mrepo1.list(unique_dir + "/b/")
        assert [
            unique_dir + "/b/c/file.txt"] == mrepo1.list(unique_dir + "/b/c/")

    def raw(self, command, args, client=None):
        if client is None:
            client = self.client
        mrepo = self.getManagedRepo(self.client)
        obj = mrepo.root()
        sha = obj.hash.val
        raw_access = omero.grid.RawAccessRequest()
        raw_access.repoUuid = sha
        raw_access.command = command
        raw_access.args = args
        handle = client.sf.submit(raw_access)
        return CmdCallbackI(client, handle)

    def assertPasses(self, cb, loops=10, wait=500):
        cb.loop(loops, wait)
        rsp = cb.getResponse()
        if isinstance(rsp, omero.cmd.ERR):
            raise Exception(rsp)
        return rsp

    def assertError(self, cb, loops=10, wait=500):
        cb.loop(loops, wait)
        rsp = cb.getResponse()
        assert isinstance(rsp, omero.cmd.ERR)

    def create_test_dir(self):
        folder = create_path(folder=True)
        (folder / "a.fake").touch()
        (folder / "b.fake").touch()
        return folder

    def create_fileset(self, folder):
        fileset = omero.model.FilesetI()
        for f in folder.files():
            entry = omero.model.FilesetEntryI()
            entry.setClientPath(rstring(str(f.abspath())))
            fileset.addFilesetEntry(entry)

        # Fill version info
        system, node, release, version, machine, processor = platform.uname()

        clientVersionInfo = [
            NV('omero.version', omero_version),
            NV('os.name', system),
            NV('os.version', release),
            NV('os.architecture', machine)
        ]
        try:
            clientVersionInfo.append(
                NV('locale', locale.getdefaultlocale()[0]))
        except:
            pass

        upload = omero.model.UploadJobI()
        upload.setVersionInfo(clientVersionInfo)
        fileset.linkJob(upload)
        return fileset

    def create_settings(self):
        settings = omero.grid.ImportSettings()
        settings.doThumbnails = rbool(True)
        settings.noStatsInfo = rbool(False)
        settings.userSpecifiedTarget = None
        settings.userSpecifiedName = None
        settings.userSpecifiedDescription = None
        settings.userSpecifiedAnnotationList = None
        settings.userSpecifiedPixels = None
        settings.checksumAlgorithm = omero.model.ChecksumAlgorithmI()
        settings.checksumAlgorithm.value = omero.rtypes.rstring("SHA1-160")
        return settings

    def upload_folder(self, proc, folder):
        ret_val = []
        for i, fobj in enumerate(folder.files()):  # Assuming same order
            rfs = proc.getUploader(i)
            try:
                f = fobj.open()
                try:
                    offset = 0
                    block = []
                    rfs.write(block, offset, len(block))  # Touch
                    while True:
                        block = f.read(1000 * 1000)
                        if not block:
                            break
                        rfs.write(block, offset, len(block))
                        offset += len(block)
                    ret_val.append(self.client.sha1(fobj.abspath()))
                finally:
                    f.close()
            finally:
                rfs.close()
        return ret_val

    def fullImport(self, client):
        """
        Re-usable method for a basic import
        """
        mrepo = self.getManagedRepo(client)
        folder = self.create_test_dir()
        fileset = self.create_fileset(folder)
        settings = self.create_settings()

        proc = mrepo.importFileset(fileset, settings)
        try:
            return self.assertImport(client, proc, folder)
        finally:
            proc.close()

    def assertImport(self, client, proc, folder):
        hashes = self.upload_folder(proc, folder)
        handle = proc.verifyUpload(hashes)
        cb = CmdCallbackI(client, handle)
        rsp = self.assertPasses(cb)
        assert 1 == len(rsp.pixels)
        return rsp
