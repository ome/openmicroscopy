#
#   $Id$
#
#   Copyright 2007 Glencoe Software, Inc. All rights reserved.
#   Use is subject to license terms supplied in LICENSE.txt
#

#import os
#dirname = __path__[0]
#__path__.append(os.path.join(dirname, "_gen"))
import exceptions
import Ice, Glacier2
import api
import model
import util
from omero_ext import pysys
import omero_Constants_ice

class client(object):

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

    def createSession(self):
        username = self.getProperty(constants.USERNAME)
        password = self.getProperty(constants.PASSWORD)

        prx = self.ic.getDefaultRouter()
        router = Glacier2.RouterPrx.checkedCast(prx)
        if not router:
            raise ClientError("No default router found.")
        session = router.createSession(username, password)
        self.sf = api.ServiceFactoryPrx.checkedCast(session)
        if not self.sf:
            raise ClientError("No session obtained.")
        return self.sf

    def closeSession(self):
        self.sf.close() 

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
            raise ClientError("Unknown type:"+type)
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

