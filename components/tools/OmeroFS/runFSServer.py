#!/usr/bin/env python
"""
    OMERO.fs FSServer module.

    The Server class is a wrapper to the MonitorServer. It handles the ICE
    formalities. It controls the shutdown.

    
"""

import sys
import FSServer

import logging
from logger import log

app = FSServer.Server("omerofs.MonitorServer", "FSServer")
log.info('Starting OMERO.fs server')
sys.exit(app.main(sys.argv))


