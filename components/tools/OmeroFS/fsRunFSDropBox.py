#!/usr/bin/env python
"""
    OMERO.fs runFSDropBox

"""
import logging
import fsLogger
log = logging.getLogger("fs.runFSDropBox")

import sys

try:
    import fsDropBox
    app = fsDropBox.DropBox()
    app.main(sys.argv)
except:
    log.exception("Failed to start FSDropBox. Reason:\n")
    sys.exit(-1)