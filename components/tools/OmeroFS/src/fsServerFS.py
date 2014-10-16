#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
    OMERO.fs ServerFS module.

    The Server class is a wrapper to the FileServer. It handles the ICE
    formalities. It controls the shutdown.

    Copyright 2009 University of Dundee. All rights reserved.
    Use is subject to license terms supplied in LICENSE.txt

"""
import logging
log = logging.getLogger("fsserver.FileServer")

import sys
import Ice

from omero.util import configure_server_logging

import fsFileServer


class Server(Ice.Application):

    """
        A fairly vanilla ICE server application.

    """

    def run(self, args):
        """
            Main method called via app.main() below.

            The Ice.Application is set to callbackOnInterrupt so that it can be
            shutdown cleanly by the callback above.

            :param args: Arguments required by the ICE system.
            :return: Exit state.
            :rtype: int
        """

        props = self.communicator().getProperties()
        configure_server_logging(props)

        # Create a FileServer, its adapter and activate it.
        try:
            serverIdString = self.getServerIdString(props)
            serverAdapterName = self.getServerAdapterName(props)
            mServer = fsFileServer.FileServerI()
            adapter = self.communicator().createObjectAdapter(
                serverAdapterName)
            adapter.add(
                mServer, self.communicator().stringToIdentity(serverIdString))
            adapter.activate()
        except:
            log.exception("Failed create OMERO.fs FileServer: \n")
            return -1

        log.info('Started OMERO.fs FileServer')

        # Wait for an interrupt.
        self.communicator().waitForShutdown()

        log.info('Stopping OMERO.fs FileServer')
        return 0

    def getServerIdString(self, props):
        """
            Get fileServerIdString from the communicator properties.

        """
        return props.getPropertyWithDefault("omero.fs.fileServerIdString", "")

    def getServerAdapterName(self, props):
        """
            Get fileServerIdString from the communicator properties.

        """
        return props.getPropertyWithDefault(
            "omero.fs.fileServerAdapterName", "")


if __name__ == '__main__':
    try:
        log.info('Trying to start OMERO.fs FileServer')
        app = Server()
    except:
        log.exception("Failed to start the server:\n")
        log.info("Exiting with exit code: -1")
        sys.exit(-1)

    exitCode = app.main(sys.argv)
    log.info("Exiting with exit code: %d", exitCode)
    sys.exit(exitCode)
