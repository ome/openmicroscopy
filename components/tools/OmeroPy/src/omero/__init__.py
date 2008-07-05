#!/usr/bin/env python
"""
   Primary OmeroPy types

   Classes:
    omero.client    -- Main OmeroPy connector object

   Copyright 2007 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import exceptions, traceback
import Ice, Glacier2
import api
import model
import util
from omero_ext import pysys
import omero_Constants_ice
import uuid
import omero.constants

class client(object):
    """
    Central blitz entry point. Currently useful for a single session, after closing the
    connection, create another instance.

    Typical usage includes:
    client = omero.client()    # Uses --Ice.Config argument or ICE_CONFIG variable
    """

    def __init__(self, args = pysys.argv, id = Ice.InitializationData()):

        self.sf = None
        self.ic = Ice.initialize(args,id)
        if not self.ic:
            raise ClientError("Improper initialization")
        # Register Object Factory
        self.of = ObjectFactory()
        self.of.registerObjectFactory(self.ic)
        # Define our unique identifier (used during close/detach)
        self.ic.getImplicitContext().put(omero.constants.CLIENTUUID, str(uuid.uuid4()))

    def __del__(self):
        try:
            self.closeSession()
        except exceptions.Exception, e:
            print "Ignoring error in client.__del__:" + str(e.__class__)
            traceback.print_exc()

    def getCommunicator(self):
        return self.ic

    def getSession(self):
        return self.sf

    def getProperties(self):
        return self.ic.getProperties()

    def getProperty(self,key):
        return self.getProperties().getProperty(key)

    def joinSession(self, session):
        """Uses the given session uuid as name
        and password to rejoin a running session"""
        return self.createSession(session, session)

    def createSession(self, username=None, password=None):
        import omero

        # Check the required properties
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

        # Acquire router and get the proxy
        # For whatever reason, we have to set the context
        # on the router context here as well.
        prx = self.ic.getDefaultRouter()
        if not prx:
            raise ClientError("No default router found.")
        prx = prx.ice_context(self.ic.getImplicitContext().getContext())
        router = Glacier2.RouterPrx.checkedCast(prx)
        if not router:
            raise ClientError("Error obtaining Glacier2 router.")
        session = router.createSession(username, password)
        if not session:
            raise ClientError("Obtained null object proxy")

        # Check type
        self.sf = api.ServiceFactoryPrx.checkedCast(session)
        if not self.sf:
            raise ClientError("Obtained object proxy is not a ServiceFactory")
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
                if name:
                    ofile.name = omero.RString(name)
                else:
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
        """
        Closes the Router connection created by createSession(). Due to a bug in Ice,
        only one connection is allowed per communicator, so we also destroy the communicator.
        """

        # If 'sf' exists we remove it, but save it for the weird chance that ic is None
        sf = None
        if hasattr(self, 'sf'):
            sf = self.sf
            self.sf = None

        # If 'ic' does not exist we don't have anything to do
        if not hasattr(self, 'ic') or not self.ic:
            if sf:
                self.ic = sf.ice_getCommunicator()
            else:
                return

        try:
            prx = self.ic.getDefaultRouter()
            router = Glacier2.RouterPrx.checkedCast(prx)

            # Now destroy the actual session if possible,
            # which will always trigger an exception,
            # regardless of actually being connected or not
            if router:
                try:
                    router.destroySession()
                except exceptions.Exception, e:
                    pass
                    # SNEE happens if we call sf.close() before
                    # calling destroySession(). CLE happens since
                    # we are disconnecting

            try:
                self.ic.destroy()
            except (), msg:
                pysys.stderr.write("Ice exception while destroying communicator:")
                pysys.stderr.write(msg)
        finally:
            self.ic = None

    def _env(self, method, *args):
        """ Helper method to access session environment"""
        session = self.getSession()
        if not session:
            raise ClientError("No session active")
        a = session.getAdminService()
        u = a.getEventContext().sessionUuid
        s = session.getSessionService()
        m = getattr(s, method)
        return apply(m, (u,)+args)

    def getInput(self, key):
        return self._env("getInput", key)

    def setInput(self, key, value):
        self._env("setInput", key, value)

    def getOutput(self, key):
        return self._env("getOutput", key)

    def setOutput(self, key, value):
        self._env("setOutput", key, value)

    def getInputKeys(self):
        return self._env("getInputKeys")

    def getOutputKeys(self):
        return self._env("getOutputKeys")


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

