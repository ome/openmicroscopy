#!/usr/bin/env python
"""
    OMERO.fs runFSServer module.

"""
import logging
import fsLogger
log = logging.getLogger("fs.runFSServer")

import sys

try:
    import fsServer
    app = fsServer.Server()
    app.main(sys.argv)
except:
    log.exception("Failed to start FSServer. Reason: \n")
    sys.exit(-1)

