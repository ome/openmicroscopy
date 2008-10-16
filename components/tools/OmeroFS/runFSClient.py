#!/usr/bin/env python
"""
    OMERO.fs Example Client module

    
"""

import sys
import FSClient

#: Example path.
pathToWatch = '/Users/cblackburn/tmp/'
#: Example whitelist of extensions.
whitelist = ['.jpg']

#: Client object reference
app = FSClient.Client('Create', pathToWatch, whitelist, [''], 'Flat', 
                      'omerofs.MonitorServer', 'omerofs.MonitorClient', 'monitorClient')
sys.exit(app.main(sys.argv, "config.client"))
