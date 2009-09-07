#!/usr/bin/env python
#
# OMERO Tables Interface
# Copyright 2009 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

# TODO:
#  - handle dangling files
#  - special columns:
#    - OMERO ID columns
#    - timestamp column for when row was added (for online update not import)
#    - timeseries column
#    - MeasurementRun ID
#

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
import portalocker # Third-party
from path import path

import omero, Ice
from omero.rtypes import *
import omero_api_Tables_ice

tables = __import__("tables") # Pytables

class StorageException(exceptions.Exception):
    pass
class StorageLockedException(StorageException):
    pass

class HdfStorage(object):
    """
    Provides HDF-storage for measurement results.
    """

    def __init__(self, hdf_dir):
        """
        hdf_dir should be the path to a directory where this HDF instance
        can be stored (Not None or Empty)
        """

        if hdf_dir is None or hdf_dir == "":
            raise exceptions.Exception("Invalid hdf_dir")

        self.dir = path(hdf_dir)
        if not self.dir.exists():
            self.dir.mkdir(700)

        # Throws portalocker.LockException if this directory is already locked.
        self.lock = open(".lock","w+")
        try:
            portalocker.lock(self.lock, portalocker.LOCK_NB|portalocker.LOCK_EX)
        except portalocker.LockException, le:
            raise StorageLockedException(le)

        self.hdf_path = self.dir / "main.h5"
        if self.hdf_path.exists():
            self.hdf = self._newfile_("r+")
        else:
            self.hdf = None

    def _newfile_(self, mode):
        return tables.openFile(self.hdf_path, mode=mode, title="OMERO HDF Measurement Storege", rootUEP="/")

    def create(self, names, descs, types, metadata = {}):
        """
        Can only be called if the HDF storage file does not exist.
        """

        if self.hdf:
            raise StorageException("%s already exists" % self.hdf_path)

        if len(names) != len(descs) or len(descs) != len(types):
            raise StorageException("Mismatched array size: %s, %s, %s" % (names, descs, types))

        self.hdf = self._newfile_("a")

        self.definition = {}
        for i in range(len(names)):
            self.definition[names[i]] = types[i](pos=i)
            # Ignoring description for now

        self.ome = self.hdf.createGroup("/","OME")
        self.mea = self.hdf.createTable(self.ome, "Measurements", self.definition)
        self.mea.attrs.version = 1

        for k,v in metadata.items():
            self.mea.attrs[k] = v
            # See attrs._f_list("user") to retrieve these.
        self.hdf.flush()

    def append(self, data):
        row = self.mea.row
        for k,v in data.items():
            row[k] = v
        row.append()

    def close(self):
        if self.hdf:
            self.hdf.close()
        self.lock.close()

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
