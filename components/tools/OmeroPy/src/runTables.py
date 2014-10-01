#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# OMERO Tables Runner
# Copyright 2009 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

if __name__ == "__main__":

    import sys
    import Ice
    import omero
    import omero.clients
    import omero.tables
    from omero.util import Dependency

    # Logging hack
    omero.tables.TablesI.__module__ = "omero.tables"
    omero.tables.TableI.__module__ = "omero.tables"

    class TablesDependency(Dependency):

        def __init__(self):
            Dependency.__init__(self, "tables")

        def get_version(self, target):
            self.target = target
            ver = "%s, hdf=%s" % (target.__version__, self.optional("hdf5", 1))
            return ver

        def optional(self, key, idx):
            try:
                x = self.target.whichLibVersion(key)
                if x is not None:
                    return x[idx]
                else:
                    return "unknown"
            except:
                return "error"

    app = omero.util.Server(
        omero.tables.TablesI, "TablesAdapter", Ice.Identity("Tables", ""),
        dependencies=(Dependency("numpy"), TablesDependency()))

    sys.exit(app.main(sys.argv))
