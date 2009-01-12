"""
    OMERO.fs DropBox application

    This is a dummy application, but should probably be
    subclassing FSClient.
"""

import Ice

class DropBox(Ice.Application):
    def run(self, args):
        adapter = self.communicator().createObjectAdapter("omerofs.DropBox")
        adapter.activate()
        print "IConfig.get(omero.data.dir)"
        print "IAdmin.lookupExperimenters()"
        print "create a directory per user ${omero.data.dir}/DropBox/${user} or similar"
        print "add a monitor which does the hard work"
        self.communicator().waitForShutdown()


