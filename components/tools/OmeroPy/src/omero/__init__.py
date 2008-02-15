#!/usr/bin/env python
"""
   Primary OmeroPy types

   Classes:
    omero.client    -- Main OmeroPy connector object

   Copyright 2007 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import exceptions
import Ice, Glacier2
import api
import model
import util
from omero_ext import pysys
import omero_Constants_ice

class client(object):
    """
    Central blitz entry point

    Typical usage includes:
    client = omero.client()    # Uses --Ice.Config argument or ICE_CONFIG variable
    """

    def __init__(self, args = pysys.argv, id = Ice.InitializationData()):

        self.ic = None
        ic = Ice.initialize(args,id)
        if not ic:
            raise ClientError("Improper initialization")
        self.of = ObjectFactory()
        self.of.registerObjectFactory(ic)
        self.ic = ic

    def __del__(self):
        if self.ic:
            try:
                self.ic.destroy()
            except (), msg:
                pysys.stderr.write("Ice exception while destroying communicator:")
                pysys.stderr.write(msg)

    def getCommunicator(self):
        return self.ic

    def getSession(self):
        return self.sf

    def getProperties(self):
        return self.ic.getProperties()

    def getProperty(self,key):
        return self.getProperties().getProperty(key)

    def createSession(self, username=None, password=None):
        import omero
        if not username:
            username = self.getProperty("omero.user")
        elif isinstance(username,omero.RString):
            username = username.val
        if not username or len(username) == 0:
            raise ClientError("No username specified")
        if not password:
            password = self.getProperty("omero.pass")
        elif isinstance(password,omero.RString):
            password = password.val
        if not password or len(password) == 0:
            raise ClientError("No password specified")

        prx = self.ic.getDefaultRouter()
        if not prx:
            raise ClientError("No default router found.")
        router = Glacier2.RouterPrx.checkedCast(prx)
        if not router:
            raise ClientError("Error obtaining Glacier2 router.")
        session = router.createSession(username, password)
        self.sf = api.ServiceFactoryPrx.checkedCast(session)
        if not self.sf:
            raise ClientError("No session obtained.")
        return self.sf

    def sha1(self, filename):
        """
        Calculates the local sha1 for a file.
        """
        import sha
        digest = sha.new()
        file = open(filename, 'rb')
        try:
            while True:
                block = file.read(1024)
                if not block:
                    break
                digest.update(block)
        finally:
            file.close()
        return digest.hexdigest()

    def upload(self, filename, name = None, path = None,
               type = None, ofile = None, block_size = 1024):
        """
        Utility method to upload a file to the server.
        """
        if not self.sf:
            raise ClientError("No session. Use createSession first.")

        import os, types
        if not filename or not isinstance(filename, types.StringType):
            raise ClientError("Non-null filename must be provided")

        if not os.path.exists(filename):
            raise ClientError("File does not exist: " + filename)

        file = open(filename, 'rb')
        try:

            size = os.path.getsize(file.name)
            if block_size > size:
                block_size = size

            from omero_model_OriginalFileI import OriginalFileI
            from omero_model_FormatI import FormatI
            import omero

            if not ofile:
                ofile = OriginalFileI()

            ofile.size = omero.RLong(size)
            ofile.sha1 = omero.RString(self.sha1(file.name))

            if not ofile.name:
               ofile.name = omero.RString(file.name)

            if not ofile.path:
               ofile.path = omero.RString(os.path.abspath(file.name))

            if not ofile.format:
                if not type:
                    # ofile.format = FormatI("unknown")
                    # Or determine type from file ending
                    raise ClientError("no format given")
                else:
                    ofile.format = FormatI()
                    ofile.format.value = omero.RString(type)

            up = self.sf.getUpdateService()
            ofile = up.saveAndReturnObject(ofile)

            prx = self.sf.createRawFileStore()
            prx.setFileId(ofile.id.val)
            offset = 0
            while True:
                block = file.read(block_size)
                if not block:
                    break
                prx.write(block, offset, len(block))
                offset += len(block)
            prx.close()
        finally:
            file.close()

        return ofile

    def download(self, ofile, filename, block_size = 1024):
        file = open(filename, 'wb')
        try:
            prx = self.sf.createRawFileStore()
            try:
                if not ofile or not ofile.id:
                    raise ClientError("No file to download")
                ofile = self.sf.getQueryService().get("OriginalFile", ofile.id.val)

                if block_size > ofile.size.val:
                    block_size = ofile.size.val

                prx.setFileId(ofile.id.val)
                offset = 0
                while offset < ofile.size.val:
                    block = prx.read(offset, block_size)
                    if not block:
                        break
                    file.write(block)
                    offset += len(block)
            finally:
                prx.close()
        finally:
            file.close()

    def closeSession(self):
        # If 'sf' does not exist we don't have a session at all
        if not hasattr(self, 'sf'):
            return
        # But even if we do have 'sf', the connection may have been lost and 'close' will fail
        try:
            self.sf.close()
        except:
            pass
        # Now destroy the actual session, which will always trigger an exception, regardless of
        # actually being connected or not
        prx = self.ic.getDefaultRouter()
        router = Glacier2.RouterPrx.checkedCast(prx)
        try:
            router.destroySession()
        except Ice.ConnectionLostException:
            pass

    # The following {get,set}{Input,Output} methods
    # are temporary and only unintended to allow
    # script development while OmeroSessions are
    # being developed.

    def getInput(self, key):
        if not hasattr(self, "inputs"):
            self.inputs = {}
        try:
            rv = self.inputs[key]
        except KeyError, ke:
            rv = self.getProperty(key)
        return rv

    def setInput(self, key, value):
        s = self.getSession().getSessionService()
        
        if not hasattr(self, "inputs"):
            self.inputs = {}
        self.inputs[key] = value

    def getOutput(self, key):
        if not hasattr(self, "outputs"):
            self.outputs = {}
        try:
            rv = self.outputs[key]
        except KeyError, ke:
            rv = self.getProperty(key)
        return rv

    def setOutput(self, key, value):
        if not hasattr(self, "outputs"):
            self.outputs = {}
        self.outputs[key] = value

import util.FactoryMap
class ObjectFactory(Ice.ObjectFactory):
    """
    Responsible for instantiating objects during deserialization.
    """

    def __init__(self, map = util.FactoryMap.map()):
        self.__m = map

    def registerObjectFactory(self, ic):
        for key in self.__m:
            if not ic.findObjectFactory(key):
                ic.addObjectFactory(self,key)

    def create(self, type):
        generator = self.__m[type]
        if generator == None:
            raise ClientError("Unknown type:"+type)
        return generator.next()

    def destroy(self):
        # Nothing to do
        pass

class ClientError(exceptions.Exception):
    """
    Top of client exception hierarchy.
    """
    pass

class UnloadedEntityException(ClientError):
    pass

class UnloadedCollectionException(ClientError):
    pass

