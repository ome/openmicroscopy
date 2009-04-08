#!/usr/bin/env python
"""
    OMERO.fs runFSDropBox

"""
import logging
import fsLogger
log = logging.getLogger("fs.fsRunFSDropBox")

import sys

try:
    log.info('Trying to start OMERO.fs DropBox client')
    import fsDropBox
    app = fsDropBox.DropBox()
    app.main(sys.argv)
except:
    log.exception("Failed to start DropBox client. Reason:\n")
    sys.exit(-1)