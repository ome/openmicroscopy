#
#   $Id$
# 
#   Copyright 2007 Glencoe Software, Inc. All rights reserved.
#   Use is subject to license terms supplied in LICENSE.txt
# 

import Ice, Glacier2

class client:

    def __init__(self, args = sys.argv, id = Ice.InitializationData()):

        self.ic = None

        try:
            ic = Ice.initialize(args,id)
            if not ic:
                raise omero.ClientError("Improper initialization")
            of = ObjectFactory()
            of.registerObjectFactory(ic)
        except:
            traceback.print_exc()
            status = 1

    def __del__(self):
        if ic:
        try:
            ic.destroy()
        except:
            traceback.print_exc()

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
        router = Glacier2::RouterPrx::checkedCast(prx)
        session = router.createSession(username, password)
        self.sf = omero::api::ServiceFactoryPrx::checkedCast(session)
        if not sf:
            raise omero.ClientError("No session obtained.")

