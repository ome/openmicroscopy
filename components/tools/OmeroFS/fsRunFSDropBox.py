#!/usr/bin/env python
"""
    OMERO.fs runFSDropBox

"""
import logging
import fsLogger
log = logging.getLogger("fsclient.fsRunFSDropBox")

import sys

try:
    log.info('Trying to start OMERO.fs DropBox client')
    import fsDropBox
    app = fsDropBox.DropBox()
except:
    log.exception("Failed to start the client:\n")
    log.info("Exiting with exit code: -1")
    sys.exit(-1)

exitCode = app.main(sys.argv)
log.info("Exiting with exit code: %d", exitCode)
sys.exit(exitCode)
