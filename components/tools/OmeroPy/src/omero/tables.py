#!/usr/bin/env python
#
# OMERO Tables Interface
# Copyright 2009 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

# TODO:
#  - handle dangling files
#  - 

import os
import sys
import time
import signal
import logging
import tempfile
import threading
import traceback
import subprocess
import exceptions
from path import path

import omero, Ice
from omero.rtypes import *

class TableI(omero.api.Table, omero.util.Servant):
    """
    Spreadsheet implementation based on pytables.
    """

    def __init__(self):
        omero.util.Servant.__init__(self)

    def check(self):
        """
        Called periodically to check the resource is alive. Returns
        False if this resource can be cleaned up. (Resources API)
        """
        self.logger.debug("Checking %s" % self )
        return False

    def __str__(self):
        if hasattr(self, "uuid"):
            return "Table-%s" % self.uuid
        else:
            return "Table-uninitialized"


class TablesI(omero.grid.Tables, omero.util.Servant):
    """
    Implementation of the omero.grid.Tables API. Provides
    spreadsheet like functionality across the OMERO.grid.
    This servant serves as a session-less, user-less
    resource for obtaining omero.grid.Sheet proxies.
    """

    def __init__(self):
        omero.util.Servant.__init__(self)

    def newTable(self, current = None):
        """
        Create new table. The OriginalFile resource will automatically
        be created
        """
        return self._table()
    
    def getTable(self, original_file, current = None):
        """
        """
        return self._table()

    def _table(self):
        """
        Create and/or register a table servant
        """
        table = TableI()
        self.resources.add(table)
        prx = current.adapter.addWithUUID(table)
        return omero.api.TablePrx.uncheckedCast(prx)


if __name__ == "__main__":
    app = omero.util.Server(TablesI, "TablesAdapter", Ice.Identity("Tables",""))
    sys.exit(app.main(sys.argv))
