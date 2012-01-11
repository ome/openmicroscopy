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
from omero.rtypes import rstring, rtime, rint
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
        import omero_ext.uuid as _uuid # see ticket:3774
        return str(_uuid.uuid4())

    def login_args(self):
        p = self.client.ic.getProperties()
        host = p.getProperty("omero.host")
        port = p.getProperty("omero.port")
        key = self.sf.ice_getIdentity().name
        return ["-s", host, "-k", key, "-p", port]

    def root_login_args(self):
        p = self.root.ic.getProperties()
        host = p.getProperty("omero.host")
        port = p.getProperty("omero.port")
        key = self.root.sf.ice_getIdentity().name
        return ["-s", host, "-k", key, "-p", port]

    def tmpfile(self):
        return str(create_path())

    def new_group(self, experimenters = None, perms = None):
        admin = self.root.sf.getAdminService()
        gname = self.uuid()
        group = omero.model.ExperimenterGroupI()
        group.name = rstring(gname)
        if perms:
            group.details.permissions = omero.model.PermissionsI(perms)
        gid = admin.createGroup(group)
        group = admin.getGroup(gid)
        if experimenters:
            for exp in experimenters:
                user, name = self.user_and_name(exp)
                admin.addGroups(user, [group])
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
        port = self.client.getProperty("omero.port")
        key = self.client.getSessionId()

        # Search up until we find "OmeroPy"
        dist_dir = self.OmeroPy / ".." / ".." / ".." / "dist"
        args = [sys.executable]
        args.append(str(path(".") / "bin" / "omero"))
        args.extend(["-s", server, "-k", key, "-p", port, "import", filename])
        popen = subprocess.Popen(args, cwd=str(dist_dir), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        out, err = popen.communicate()
        rc = popen.wait()
        if rc != 0:
            raise exceptions.Exception("import failed: [%r] %s\n%s" % (args, rc, err))
        pix_ids = []
        for x in out.split("\n"):
            if x and x.find("Created") < 0 and x.find("#") < 0:
                try:    # if the line has an image ID...
                    imageId = str(long(x.strip()))
                    pix_ids.append(imageId)
                except: pass
        return pix_ids
    
    
    def createTestImage(self, sizeX = 16, sizeY = 16, sizeZ = 1, sizeC = 1, sizeT = 1, session=None):
        """
        Creates a test image of the required dimensions, where each pixel value is set 
        to the value of x+y. 
        Returns the image (omero.model.ImageI)
        """
        from numpy import fromfunction, int16
        from omero.util import script_utils
        import random
        
        if session is None:
            session = self.root.sf
        renderingEngine = session.createRenderingEngine()
        queryService = session.getQueryService()
        pixelsService = session.getPixelsService()
        rawPixelStore = session.createRawPixelsStore()
        containerService = session.getContainerService()

        def f1(x,y):
            return y
        def f2(x,y):
            return (x+y)/2
        def f3(x,y):
            return x

        pType = "int16"
        # look up the PixelsType object from DB
        pixelsType = queryService.findByQuery("from PixelsType as p where p.value='%s'" % pType, None) # omero::model::PixelsType
        if pixelsType == None and pType.startswith("float"):    # e.g. float32
            pixelsType = queryService.findByQuery("from PixelsType as p where p.value='%s'" % "float", None) # omero::model::PixelsType
        if pixelsType == None:
            print "Unknown pixels type for: " % pType
            raise "Unknown pixels type for: " % pType

        # code below here is very similar to combineImages.py
        # create an image in OMERO and populate the planes with numpy 2D arrays
        channelList = range(1, sizeC+1)
        iId = pixelsService.createImage(sizeX, sizeY, sizeZ, sizeT, channelList, pixelsType, "testImage", "description")
        imageId = iId.getValue()
        image = containerService.getImages("Image", [imageId], None)[0]

        pixelsId = image.getPrimaryPixels().getId().getValue()
        rawPixelStore.setPixelsId(pixelsId, True)

        colourMap = {0: (0,0,255,255), 1:(0,255,0,255), 2:(255,0,0,255), 3:(255,0,255,255)}
        fList = [f1, f2, f3]
        for theC in range(sizeC):
            minValue = 0
            maxValue = 0
            f = fList[theC % len(fList)]
            for theZ in range(sizeZ):
                for theT in range(sizeT):
                    plane2D = fromfunction(f,(sizeY,sizeX),dtype=int16)
                    script_utils.uploadPlane(rawPixelStore, plane2D, theZ, theC, theT)
                    minValue = min(minValue, plane2D.min())
                    maxValue = max(maxValue, plane2D.max())
            pixelsService.setChannelGlobalMinMax(pixelsId, theC, float(minValue), float(maxValue))
            rgba = None
            if theC in colourMap:
                rgba = colourMap[theC]
            script_utils.resetRenderingSettings(renderingEngine, pixelsId, theC, minValue, maxValue, rgba)

        renderingEngine.close()
        rawPixelStore.close()

        # Reloading image to prevent error on old pixels updateEvent
        image = containerService.getImages("Image", [imageId], None)[0]
        return image

    def index(self, *objs):
        if objs:
            for obj in objs:
                self.root.sf.getUpdateService().indexObject(obj, {"omero.group":"-1"})

    def new_user(self, group = None, perms = None, admin = False):

        if not self.root:
            raise exceptions.Exception("No root client. Cannot create user")

        adminService = self.root.getSession().getAdminService()
        name = self.uuid()

        # Create group if necessary
        if not group:
            g = self.new_group(perms = perms)
            group = g.name.val
        else:
            g, group = self.group_and_name(group)

        # Create user
        e = omero.model.ExperimenterI()
        e.omeName = rstring(name)
        e.firstName = rstring(name)
        e.lastName = rstring(name)
        uid = adminService.createUser(e, group)
        e = adminService.lookupExperimenter(name)
        if admin:
            adminService.setGroupOwner(g, e)
        return adminService.getExperimenter(uid)

    def new_client(self, group = None, user = None, perms = None, admin = False):
        """
        Like new_user() but returns an active client.
        """
        if user is None:
            user = self.new_user(group, perms, admin)
        props = self.root.getPropertyMap()
        props["omero.user"] = user.omeName.val
        props["omero.pass"] = user.omeName.val
        client = omero.client(props)
        self.__clients.add(client)
        client.setAgent("OMERO.py.new_client_test")
        client.createSession()
        return client

    def new_client_and_user(self, group = None, perms = None, admin = False):
        user = self.new_user(group)
        client = self.new_client(group, user, perms, admin)
        return client, user

    def timeit(self, func, *args, **kwargs):
        start = time.time()
        rv = func(*args, **kwargs)
        stop = time.time()
        elapsed = stop - start
        return elapsed, rv

    def group_and_name(self, group):
        admin = self.root.sf.getAdminService()
        if isinstance(group, omero.model.ExperimenterGroup):
            if group.isLoaded():
                name = group.name.val
                group = admin.lookupGroup(name)
            else:
                group = admin.getGroup(group.id.val)
                name = group.name.val
        elif isinstance(group, (str, unicode)):
            name = group
            group = admin.lookupGroup(name)
        else:
            self.fail("Unknown type: %s=%s" % (type(group), group))

        return group, name

    def user_and_name(self, user):
        admin = self.root.sf.getAdminService()
        if isinstance(user, omero.model.Experimenter):
            if user.isLoaded():
                name = user.omeName.val
                user = admin.lookupExperimenter(name)
            else:
                user = admin.getExperimenter(user.id.val)
                name = user.omeName.val
        elif isinstance(user, (str, unicode)):
            name = user
            user = admin.lookupExperimenter(name)
        else:
            self.fail("Unknown type: %s=%s" % (type(user), user))

        return user, name

    #
    # Data methods
    #

    def missing_pyramid(self):
        """
        Creates and returns a pixels whose shape changes from
        1,1,4000,4000,1 to 4000,4000,1,1,1 making it a pyramid
        candidate but without the pyramid which is created on
        initial import in 4.3+. This simulates a big image that
        was imported in 4.2.
        """
        pix = self.pix(x=1, y=1, z=4000, t=4000, c=1)
        rps = self.client.sf.createRawPixelsStore()
        try:
            rps.setPixelsId(pix.id.val, True)
            for t in range(4000):
                rps.setTimepoint([5]*4000, t) # Assuming int8
            pix = rps.save()
        finally:
            rps.close()

        pix.sizeX = rint(4000)
        pix.sizeY = rint(4000)
        pix.sizeZ = rint(1)
        pix.sizeT = rint(1)
        return self.update.saveAndReturnObject(pix)

    def pix(self, x=10, y=10, z=10, c=3, t=50):
        """
        Creates an int8 pixel of the given size in the database.
        No data is written.
        """
        image = self.new_image()
        pixels = omero.model.PixelsI()
        pixels.sizeX = rint(x)
        pixels.sizeY = rint(y)
        pixels.sizeZ = rint(z)
        pixels.sizeC = rint(c)
        pixels.sizeT = rint(t)
        pixels.sha1 = rstring("")
        pixels.pixelsType = omero.model.PixelsTypeI()
        pixels.pixelsType.value = rstring("int8")
        pixels.dimensionOrder = omero.model.DimensionOrderI()
        pixels.dimensionOrder.value = rstring("XYZCT")
        image.addPixels(pixels)
        image = self.update.saveAndReturnObject(image)
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
            bytes_per_plane = pix.sizeX.val * pix.sizeY.val # Assuming int8
            for z in range(pix.sizeZ.val):
                for c in range(pix.sizeC.val):
                    for t in range(pix.sizeT.val):
                        rps.setPlane([5]*bytes_per_plane, z, c, t)
        else:
            # By tile
            w, h = rps.getTileSize()
            bytes_per_tile = w * h # Assuming int8
            for z in range(pix.sizeZ.val):
                for c in range(pix.sizeC.val):
                    for t in range(pix.sizeT.val):
                        for x in range(0, pix.sizeX.val, w):
                            for y in range(0, pix.sizeY.val, h):

                                changed = False
                                if x+w > pix.sizeX.val:
                                    w = pix.sizeX.val - x
                                    changed = True
                                if y+h > pix.sizeY.val:
                                    h = pix.sizeY.val - y
                                    changed = True
                                if changed:
                                    bytes_per_tile = w * h # Again assuming int8

                                args = ([5]*bytes_per_tile, z, c, t, x, y, w, h)
                                rps.setTile(*args)

    def open_jpeg_buffer(self, buf):
        try:
            from PIL import Image, ImageDraw # see ticket:2597
        except ImportError:
            try:
                import Image, ImageDraw # see ticket:2597
            except ImportError:
                print "PIL not installed"
        from cStringIO import StringIO
        tfile = StringIO(buf)
        jpeg = Image.open(tfile) # Raises if invalid
        return jpeg

    def tearDown(self):
        failure = False
        self.__clients.__del__()
