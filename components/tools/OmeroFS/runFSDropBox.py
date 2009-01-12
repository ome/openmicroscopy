#!/usr/bin/env python
"""
    OMERO.fs runFSDropBox

    Rather than pass in a pathToWatch or other configuration here,
    the drop box will need to ask the server for that information.
    Similarly, the configuration file will be passed via Ice.Config
    from icegridnode.
"""

import sys
import FSDropBox

#: Client object reference
app = FSDropBox.DropBox()
sys.exit(app.main(sys.argv))
