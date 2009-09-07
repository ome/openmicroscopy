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
#    - Temporary files?
#    - Deleting files/cleaning up services
#    - Closing files / Deleting them
#

import os
import sys
import time
import numpy
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
import omero_api_Tables_ice

from omero.rtypes import *

tables = __import__("tables") # Pytables

class HdfStorage(object):
    """
    Provides HDF-storage for measurement results. Instances simply
    provide utility methods and initializaiton. Since fields are
    accessed directly, no locking or other checks are provided
    here.
    """

    def __init__(self, hdf_dir, allow_read_only = False):
        """
        hdf_dir should be the path to a directory where this HDF instance
        can be stored (Not None or Empty). Once this method is finished,
        self.hdf is guaranteed to be a PyTables HDF file, but not necesarrily
        initialized (see self.initialized)
        """

        if hdf_dir is None or hdf_dir == "":
            raise omero.ValidationException(None, None, "Invalid hdf_dir")

        self.log = logging.getLogger("omero.tables.HdfStorage")
        self.dir = path(hdf_dir)
        self.hdf_path = self.dir / "main.h5"
        self.read_write = True # Our intention

        if not self.dir.exists():
            self.dir.mkdir(700)

        # Throws portalocker.LockException if this directory is already locked.
        self.lock = open( self.dir/".lock", "w+" )
        try:
            portalocker.lock(self.lock, portalocker.LOCK_NB|portalocker.LOCK_EX)
        except portalocker.LockException, le:
            if allow_read_only:
                self.read_write = False # Downgrading
            else:
                raise omero.LockTimeout(None, None, "Cannot acquire exclusive write lock on: %s" % self.dir)

        if self.read_write:
            if self.hdf_path.exists():
                self.initialized = False
            else:
                self.initialized = True
            self.hdf = self.openfile("a")
        else:
            count = 3
            while count > 0:
                try:
                    self.hdf = self.openfile("r")
                except IOError:
                    count = count -1
            msg = "Failed to acquire read-only file: %s" % self.hdf_path
            log.error(msg)
            raise omero.LockTimeout(None, None, "File not created by lock owner: %s" % self.hdf_path)


    def openfile(self, mode):
        return tables.openFile(self.hdf_path, mode=mode, title="OMERO HDF Measurement Storege", rootUEP="/")


    def initialize(self, names, descs, types, metadata = {}):
        """
        Can only be called if the HDF storage file was created during __init__.
        """

        if self.initialized:
            raise omero.ApiUsageException(None, None, "HDF already initialized")

        if not self.read_write:
            raise omero.ApiUsageException(None, None, "File opened read-only")

        if len(names) != len(descs) or len(descs) != len(types):
            raise omero.ValidationException("Mismatched array size: %s, %s, %s" % (names, descs, types))

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

    def isInitialized(self):
        pass

    def version(self):
        pass

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

    def __init__(self, file_obj, dir_path):
        omero.util.Servant.__init__(self)
        self.file_obj = file_obj
        self.dir_path = dir_path
        # This may throw
        self.storage = HdfStorage(dir_path)

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

    # TABLES READ API ============================

    def getOriginalFile(self, current = None):
        return self.file_obj

    def isWrite(self, current = None):
        raise exceptions.Exception("NYI")

    def getHeaders(self, current = None):
        return self._getColumns(0)

    def _getColumns(self, size):
        names = self.storage.mea.colnames
        types = self.storage.mea.coltypes
        cols = []
        for name in names:
            typ = types[name]
            if type == "int64":
                col = omero.grid.LongColumn(name,"")
                col.values = [0]*size
                cols.append(col)
            else:
                raise omero.ValidationException("BAD COLUMN TYPE: %s" % type)

        return cols

    def getMetadata(self, current = None):
        metadata = {}
        attr = self.storage.mea.attrs
        keys = list(self.storage.mea.attrs._v_attrnamesuser)
        for key in keys:
            val = attr[key]
            if type(val) == numpy.float64:
                val = rfloat(val)
            elif type(val) == numpy.int32:
                val = rint(val)
            elif type(val) == numpy.string_:
                val = rstring(val)
            else:
                raise omero.ValidationException("BAD TYPE: %s" % type(val))
            metadata[key] = val

    def getNumberOfRows(self, current = None):
        return self.storage.mea.nrows

    def getWhereList(self, condition, variables, start, stop, step, current = None):
        if stop == 0:
            stop = None
        if step == 0:
            step = None
        return self.storage.mea.getWhereList(condition, variables, None, start, stop, step)

    def readCoordinates(self, rowNumbers, current = None):
        rows = self.strage.mea.readCoordinates(rowNumbers)
        cols = self.getHeaders(current)
        descr = rows.dtype.descr
        for i in range(len(descr)):
            cols[i].values = rows[i]

        data = omero.grid.Data()
        data.lastModification = self.lastModification
        data.rowNumbers = rowNumbers
        data.columns = cols
        return data

    # TABLES WRITE API ===========================

    def addColumn(self, col, current = None):
        raise omero.ApiUsageException(None, None, "NYI")

    def addData(self, cols, current = None):
        pass


class TablesI(omero.grid.Tables, omero.util.Servant):
    """
    Implementation of the omero.grid.Tables API. Provides
    spreadsheet like functionality across the OMERO.grid.
    This servant serves as a session-less, user-less
    resource for obtaining omero.grid.Sheet proxies.
    """

    def __init__(self):
        omero.util.Servant.__init__(self)
        self.mount = self.communicator().getProperies().getProperty("omero.mount")
        self.sf = omero.util.internal_service_factory()
        self.resources.add(SessionHolder(self.sf))
        self.repo_uuid = "FIXME -- GET A REAL REPO UUID"
        self.repo_obj = self.sf.getQueryObject().findByString(\
            "select r from Repository r where uuid = :uuid",
            ParametersI().addString("uuid",self.repo))

    def getRepository(self, current = None):
        """
        Returns the Repository object for this Tables server.
        """
        return self.repo_obj

    def getTable(self, file_obj, current = None):
        """
        Create and/or register a table servant
        """
        file_obj = self.load_ofile(file_obj.id.val)
        if file_obj.repository.id != self.repo_obj.id:
            return None

        # This might throw based on locking
        dir_path = omero.utils.long_to_path( self.mount, file_obj.id.val )
        table = TableI(file_obj, dir_path)

        self.resources.add(table)
        prx = current.adapter.addWithUUID(table)
        return omero.api.TablePrx.uncheckedCast(prx)

    def load_ofile(self, id):
        return self.sf.getQueryService().get("OriginalFile", id)


class SessionHolder(object):
    """
    Simple session holder to be put into omero.util.Resources
    """

    def __init__(self, sf):
        self.sf = sf

    def check(self):
        self.sf.keepAlive(None)

    def cleanup(self):
        try:
            self.sf.destroy()
        except exceptions.Exception, e:
            self.logger.debug("Exception destroying session: %s" % e)

if __name__ == "__main__":
    app = omero.util.Server(TablesI, "TablesAdapter", Ice.Identity("Tables",""))
    sys.exit(app.main(sys.argv))
