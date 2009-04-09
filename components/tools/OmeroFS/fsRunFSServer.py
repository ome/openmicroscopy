#!/usr/bin/env python
"""
    OMERO.fs runFSServer module.

"""
import logging
import fsLogger
log = logging.getLogger("fs.fsRunFSServer")

import sys

try:
    log.info('Trying to start OMERO.fs Server')   
    import fsServer
    app = fsServer.Server()
    app.main(sys.argv)
except:
    log.exception("Failed to start Server. Reason: \n")
    sys.exit(-1)

