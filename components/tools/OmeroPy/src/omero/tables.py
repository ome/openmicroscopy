#!/usr/bin/env python
#
# OMERO Tables Interface
# Copyright 2009 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import os
import Ice
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


import omero # Do we need both??
import omero.clients

# For ease of use
from omero.columns import *
from omero.rtypes import *
from omero.util.decorators import remoted, locked, perf
from omero_ext.functional import wraps


sys = __import__("sys") # Python sys
tables = __import__("tables") # Pytables


def stamped(func, update = False):
    """
    Decorator which takes the first argument after "self" and compares
    that to the last modification time. If the stamp is older, then the
    method call will throw an omero.OptimisticLockException. Otherwise,
    execution will complete normally. If update is True, then the
    last modification time will be updated after the method call if it
    is successful.

    Note: stamped implies locked

    """
    def check_and_update_stamp(*args, **kwargs):
        self = args[0]
        stamp = args[1]
        if stamp < self._stamp:
            raise omero.OptimisticLockException(None, None, "Resource modified by another thread")

        try:
            return func(*args, **kwargs)
        finally:
            if update:
                self._stamp = time.time()
    checked_and_update_stamp = wraps(func)(check_and_update_stamp)
    return locked(check_and_update_stamp)


class HdfList(object):
    """
    Since two calls to tables.openFile() return non-equal files
    with equal fileno's, portalocker cannot be used to prevent
    the creation of two HdfStorage instances from the same
    Python process.
    """

    def __init__(self):
        self._lock = threading.RLock()
        self.__filenos = {}
        self.__paths = {}

    @locked
    def addOrThrow(self, hdfpath, hdffile, hdfstorage, action):
        fileno = hdffile.fileno()
        if fileno in self.__filenos.keys():
            raise omero.LockTimeout(None, None, "File already opened by process: %s" % hdfpath, 0)
        else:
            self.__filenos[fileno] = hdfstorage
            self.__paths[hdfpath] = hdfstorage
            action()

    @locked
    def getOrCreate(self, hdfpath):
        try:
            return self.__paths[hdfpath]
        except KeyError:
            return HdfStorage(hdfpath) # Adds itself.

    @locked
    def remove(self, hdfpath, hdffile):
        del self.__filenos[hdffile.fileno()]
        del self.__paths[hdfpath]

# Global object for maintaining files
HDFLIST = HdfList()

class HdfStorage(object):
    """
    Provides HDF-storage for measurement results. At most a single
    instance will be available for any given physical HDF5 file.
    """


    def __init__(self, file_path):

        """
        file_path should be the path to a file in a valid directory where
        this HDF instance can be stored (Not None or Empty). Once this
        method is finished, self.__hdf_file is guaranteed to be a PyTables HDF
        file, but not necessarily initialized.
        """

        if file_path is None or str(file_path) == "":
            raise omero.ValidationException(None, None, "Invalid file_path")

        self.__log = logging.getLogger("omero.tables.HdfStorage")
        self.__hdf_path = path(file_path)
        self.__hdf_file = self.__openfile("a")
        self.__tables = []

        self._lock = threading.RLock()
        self._stamp = time.time()

        # These are what we'd like to have
        self.__mea = None
        self.__ome = None

        # Now we try to lock the file, if this fails, we rollback
        # any previous initialization (opening the file)
        try:
            fileno = self.__hdf_file.fileno()
            HDFLIST.addOrThrow(self.__hdf_path, self.__hdf_file, self,\
                lambda: portalocker.lock(self.__hdf_file, portalocker.LOCK_NB|portalocker.LOCK_EX))
        except portalocker.LockException, le:
            self.cleanup()
            raise omero.LockTimeout(None, None, "Cannot acquire exclusive lock on: %s" % self.__hdf_path, 0)

        try:
            self.__ome = self.__hdf_file.root.OME
            self.__mea = self.__ome.Measurements
            self.__types = self.__ome.ColumnTypes[:]
            self.__descriptions = self.__ome.ColumnDescriptions[:]
            self.__initialized = True
        except tables.NoSuchNodeError:
            self.__initialized = False

    #
    # Non-locked methods
    #

    def __openfile(self, mode):
        try:
            return tables.openFile(self.__hdf_path, mode=mode, title="OMERO HDF Measurement Storege", rootUEP="/")
        except IOError, io:
            msg = "HDFStorage initialized with bad path: %s" % self.__hdf_path
            self.__log.error(msg)
            raise omero.ValidationException(None, None, msg)

    def __initcheck(self):
        if not self.__initialized:
            raise omero.ApiUsageException(None, None, "Not yet initialized")

    #
    # Locked methods
    #

    @locked
    def initialize(self, cols, metadata = {}):
        """

        """

        if self.__initialized:
            raise omero.ValidationException(None, None, "Already initialized.")

        self.__definition = columns2definition(cols)
        self.__ome = self.__hdf_file.createGroup("/", "OME")
        self.__mea = self.__hdf_file.createTable(self.__ome, "Measurements", self.__definition)

        self.__types = [ x.ice_staticId() for x in cols ]
        self.__descriptions = [ (x.description != None) and x.description or "" for x in cols ]
        self.__hdf_file.createArray(self.__ome, "ColumnTypes", self.__types)
        self.__hdf_file.createArray(self.__ome, "ColumnDescriptions", self.__descriptions)

        self.__mea.attrs.version = "v1"
        self.__mea.attrs.initialized = time.time()
        if metadata:
            for k, v in metadata.items():
                self.__mea.attrs[k] = v
                # See attrs._f_list("user") to retrieve these.

        self.__mea.flush()
        self.__hdf_file.flush()
        self.__initialized = True

    @locked
    def incr(self, table):
        if table in self.__tables:
            raise omero.ApiUsageException(None, Non, "Already added")
        self.__tables.append(table)
        return len(self.__tables)

    @locked
    def decr(self, table):
        if not (table in self.__tables):
            raise omero.ApiUsageException(None, None, "Unknown table")
        self.__tables.remove(table)
        l = len(self.__tables)
        if l == 0:
            self.cleanup()
        return l

    @locked
    def uptodate(self, stamp):
        return self._stamp <= stamp

    @locked
    def rows(self):
        self.__initcheck()
        return self.__mea.nrows

    @locked
    def cols(self, size, current):
        self.__initcheck()
        ic = current.adapter.getCommunicator()
        types = self.__types
        names = self.__mea.colnames
        cols = []
        for i in range(len(types)):
            t = types[i]
            n = names[i]
            try:
                col = ic.findObjectFactory(t).create(t)
                col.name = n
                col.size(size)
                cols.append(col)
            except:
                msg = traceback.format_exc()
                raise omero.ValidationException(None, msg, "BAD COLUMN TYPE: %s for %s" % (t,n))
        return cols

    @locked
    def meta(self):
        self.__initcheck()
        metadata = {}
        attr = self.__mea.attrs
        keys = list(self.__mea.attrs._v_attrnamesuser)
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

    @locked
    def append(self, cols):
        # Optimize!
        arrays = []
        names = []
        for col in cols:
            names.append(col.name)
            arrays.append(col.array())
        data = numpy.rec.fromarrays(arrays, names=names)
        self.__mea.append(data)
        self.__mea.flush()

    #
    # Stamped methods
    #

    @stamped
    def getWhereList(self, stamp, condition, variables, unused, start, stop, step):
        self.__initcheck()
        return self.__mea.getWhereList(condition, variables, None, start, stop, step).tolist()

    def _data(self, cols, rowNumbers):
        data = omero.grid.Data()
        data.columns = cols
        data.rowNumbers = rowNumbers
        data.lastModification = long(self._stamp*1000) # Convert to millis since epoch
        return data

    @stamped
    def readCoordinates(self, stamp, rowNumbers, current):
        self.__initcheck()
        rows = self.__mea.readCoordinates(rowNumbers)
        cols = self.cols(None, current)
        for col in cols:
            col.values = rows[col.name].tolist()
        return self._data(cols, rowNumbers)

    @stamped
    def slice(self, stamp, colNumbers, rowNumbers, current):
        self.__initcheck()
        if rowNumbers is None or len(rowNumbers) == 0:
	    rows = self.__mea.read()
        else:
            rows = self.__mea.readCoordinates(rowNumbers)
        cols = self.cols(None, current)
        rv   = []
        for i in range(len(cols)):
            if colNumbers is None or len(colNumbers) == 0 or i in colNumbers:
                col = cols[i]
                col.values = rows[col.name].tolist()
                rv.append(col)
        return self._data(rv, rowNumbers)

    #
    # Lifecycle methods
    #

    def check(self):
        return False

    @locked
    def cleanup(self):
        self.__log.info("Cleaning storage: %s" % self.__hdf_path)
        if self.__mea:
            self.__mea.flush()
            self.__mea = None
        if self.__ome:
            self.__ome = None
        if self.__hdf_file:
            HDFLIST.remove(self.__hdf_path, self.__hdf_file)
        self.__hdffile = None

# End class HdfStorage


class TableI(omero.grid.Table, omero.util.SimpleServant):
    """
    Spreadsheet implementation based on pytables.
    """

    def __init__(self, ctx, file_obj, storage):
        omero.util.SimpleServant.__init__(self, ctx)
        self.file_obj = file_obj
        self.storage = storage
        self.storage.incr(self)
        self.stamp = time.time()

    def check(self):
        """
        Called periodically to check the resource is alive. Returns
        False if this resource can be cleaned up. (Resources API)
        """
        self.logger.debug("Checking %s" % self)
        return False

    def cleanup(self):
        """
        Decrements the counter on the held storage to allow it to
        be cleaned up.
        """
        if self.storage:
            try:
                self.storage.decr(self)
            finally:
                self.storage = None

    def __str__(self):
        if hasattr(self, "uuid"):
            return "Table-%s" % self.uuid
        else:
            return "Table-uninitialized"

    @remoted
    @perf
    def close(self, current = None):
        self.cleanup()

    # TABLES READ API ============================

    @remoted
    @perf
    def getOriginalFile(self, current = None):
        return self.file_obj

    @remoted
    @perf
    def getHeaders(self, current = None):
        return self.storage.cols(None, current)

    @remoted
    @perf
    def getMetadata(self, current = None):
        return self.storage.meta()

    @remoted
    @perf
    def getNumberOfRows(self, current = None):
        return self.storage.rows()

    @remoted
    @perf
    def getWhereList(self, condition, variables, start, stop, step, current = None):
        if stop == 0:
            stop = None
        if step == 0:
            step = None
        return self.storage.getWhereList(self.stamp, condition, variables, None, start, stop, step)

    @remoted
    @perf
    def readCoordinates(self, rowNumbers, current = None):
        return self.storage.readCoordinates(self.stamp, rowNumbers, current)

    @remoted
    @perf
    def slice(self, colNumbers, rowNumbers, current = None):
        return self.storage.slice(self.stamp, colNumbers, rowNumbers, current)

    # TABLES WRITE API ===========================

    @remoted
    @perf
    def initialize(self, cols, current = None):
        self.storage.initialize(cols)

    @remoted
    @perf
    def addColumn(self, col, current = None):
        raise omero.ApiUsageException(None, None, "NYI")

    @remoted
    @perf
    def addData(self, cols, current = None):
        self.storage.append(cols)


class TablesI(omero.grid.Tables, omero.util.Servant):
    """
    Implementation of the omero.grid.Tables API. Provides
    spreadsheet like functionality across the OMERO.grid.
    This servant serves as a session-less, user-less
    resource for obtaining omero.grid.Table proxies.

    The first major step in initialization is getting
    a session. This will block until the Blitz server
    is reachable.
    """

    def __init__(self,\
        ctx,\
        table_cast = omero.grid.TablePrx.uncheckedCast,\
        internal_repo_cast = omero.grid.InternalRepositoryPrx.checkedCast):

        omero.util.Servant.__init__(self, ctx, needs_session = True)

        # Storing these methods, mainly to allow overriding via
        # test methods. Static methods are evil.
        self._table_cast = table_cast
        self._internal_repo_cast = internal_repo_cast

        self.__stores = []
        self._get_dir()
        self._get_uuid()
        self._get_repo()

    def _get_dir(self):
        """
        Second step in initialization is to find the .omero/repository
        directory. If this is not created, then a required server has
        not started, and so this instance will not start.
        """
        wait = int(self.communicator.getProperties().getPropertyWithDefault("omero.repo.wait", "1"))
        self.repo_dir = self.communicator.getProperties().getProperty("omero.repo.dir")

        if not self.repo_dir:
            # Implies this is the legacy directory. Obtain from server
            self.repo_dir = self.ctx.getSession().getConfigService().getConfigValue("omero.data.dir")

        self.repo_cfg = path(self.repo_dir) / ".omero" / "repository"
        start = time.time()
        while not self.repo_cfg.exists() and wait < (time.time() - start):
            self.logger.info("%s doesn't exist; waiting 5 seconds..." % self.repo_cfg)
            time.sleep(5)
            count -= 1
        if not self.repo_cfg.exists():
            msg = "No repository found: %s" % self.repo_cfg
            self.logger.error(msg)
            raise omero.ResourceError(None, None, msg)

    def _get_uuid(self):
        """
        Third step in initialization is to find the database uuid
        for this grid instance. Multiple OMERO.grids could be watching
        the same directory.
        """
        cfg = self.ctx.getSession().getConfigService()
        self.db_uuid = cfg.getDatabaseUuid()
        self.instance = self.repo_cfg / self.db_uuid

    def _get_repo(self):
        """
        Fourth step in initialization is to find the repository object
        for the UUID found in .omero/repository/<db_uuid>, and then
        create a proxy for the InternalRepository attached to that.
        """

        # Get and parse the uuid from the RandomAccessFile format from FileMaker
        self.repo_uuid = (self.instance / "repo_uuid").lines()[0].strip()
        if len(self.repo_uuid) != 38:
            raise omero.ResourceError("Poorly formed UUID: %s" % self.repo_uuid)
        self.repo_uuid = self.repo_uuid[2:]

        # Using the repo_uuid, find our OriginalFile object
        self.repo_obj = self.ctx.getSession().getQueryService().findByQuery("select f from OriginalFile f where sha1 = :uuid",
            omero.sys.ParametersI().add("uuid", rstring(self.repo_uuid)))
        self.repo_mgr = self.communicator.stringToProxy("InternalRepository-%s" % self.repo_uuid)
        self.repo_mgr = self._internal_repo_cast(self.repo_mgr)
        self.repo_svc = self.repo_mgr.getProxy()

    @remoted
    def getRepository(self, current = None):
        """
        Returns the Repository object for this Tables server.
        """
        return self.repo_svc

    @remoted
    @perf
    def getTable(self, file_obj, current = None):
        """
        Create and/or register a table servant.
        """

        # Will throw an exception if not allowed.
        self.logger.info("getTable: %s" % file_obj and file_obj.id and file_obj.id.val)
        file_path = self.repo_mgr.getFilePath(file_obj)
        p = path(file_path).dirname()
        if not p.exists():
            p.makedirs()

        storage = HDFLIST.getOrCreate(file_path)
        table = TableI(self.ctx, file_obj, storage)
        self.resources.add(table)

        prx = current.adapter.addWithUUID(table)
        return self._table_cast(prx)
