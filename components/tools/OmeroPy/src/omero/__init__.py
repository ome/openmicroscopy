#
#   $Id$
#
#   Copyright 2007 Glencoe Software, Inc. All rights reserved.
#   Use is subject to license terms supplied in LICENSE.txt
#

#import os
#dirname = __path__[0]
#__path__.append(os.path.join(dirname, "_gen"))
import sys, exceptions
import Ice, Glacier2
import api
import model
import util

class client(object):

    def __init__(self, args = sys.argv, id = Ice.InitializationData()):

        self.ic = None
        ic = Ice.initialize(args,id)
        if not ic:
            raise omero.ClientError("Improper initialization")
        of = ObjectFactory()
        of.registerObjectFactory(ic)
        self.ic = ic

    def __del__(self):
        if self.ic:
            try:
                self.ic.destroy()
            except (), msg:
                sys.stderr.write("Ice exception while destroying communicator:")
                sys.stderr.write(msg)

    def getCommunicator(self):
        return self.ic

    def getSession(self):
        return self.sf

    def getProperties(self):
        return self.ic.getProperties()

    def getProperty(self,key):
        self.getProperties().getProperty(key)

    def createSession(self):
        username = self.getProperty("OMERO.username")
        password = self.getProperty("OMERO.password")

        prx = self.ic.getDefaultRouter()
        router = Glacier2.RouterPrx.checkedCast(prx)
        session = router.createSession(username, password)
        self.sf = omero.api.ServiceFactoryPrx.checkedCast(session)
        if not sf:
            raise omero.ClientError("No session obtained.")

import util.FactoryMap
class ObjectFactory(Ice.ObjectFactory):

    def __init__(self, map = util.FactoryMap.map()):
        self.__m = map

    def registerObjectFactory(self, ic):
        for key in self.__m:
            if not ic.findObjectFactory(key):
                ic.addObjectFactory(self,key)

    def create(self, type):
        generator = self.__m[type]
        if generator == None:
            raise omero.ClientError("Unknown type:"+type)
        return generator.next()

    def destroy(self):
        # Nothing to do
        pass

class ClientError(exceptions.Exception):
    pass

class UnloadedEntityException(ClientError):
    pass

class UnloadedCollectionException(ClientError):
    pass

