#!/usr/bin/env python
#
# OMERO Tables Runner
# Copyright 2009 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

if __name__ == "__main__":

    # These are optional imports in columns.py, but for this
    # service to run, they must be present.
    __import__("numpy")
    __import__("tables")

    import sys
    import Ice
    import omero
    import omero.clients
    import omero.tables

    # Logging hack
    omero.tables.TablesI.__module__ = "omero.tables"
    omero.tables.TableI.__module__ = "omero.tables"

    app = omero.util.Server(omero.tables.TablesI, "TablesAdapter", Ice.Identity("Tables", ""))
    sys.exit(app.main(sys.argv))
