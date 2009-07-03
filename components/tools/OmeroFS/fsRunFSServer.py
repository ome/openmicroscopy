#!/usr/bin/env python
"""
    OMERO.fs runFSServer module.

"""
import logging
import fsLogger
log = logging.getLogger("fsserver.fsRunFSServer")

import sys

try:
    log.info('Trying to start OMERO.fs Server')   
    import fsServer
    app = fsServer.Server()
except:
    log.exception("Failed to start the server:\n")
    log.info("Exiting with exit code: -1")
    sys.exit(-1)

exitCode = app.main(sys.argv)
log.info("Exiting with exit code: %d", exitCode)
sys.exit(exitCode)
