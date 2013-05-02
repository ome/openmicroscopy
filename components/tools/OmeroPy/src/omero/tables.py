#!/usr/bin/env python
# -*- coding: utf-8 -*-
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
import threading
import traceback
import subprocess
import portalocker # Third-party

from path import path


import omero # Do we need both??
import omero.clients
import omero.callbacks

# For ease of use
from omero.columns import *
from omero.rtypes import *
from omero.util.decorators import remoted, locked, perf
from omero_ext.functional import wraps


sys = __import__("sys") # Python sys
tables = __import__("tables") # Pytables

def slen(rv):
    """
    Returns the length of the argument or None
    if the argument is None
    """
    if rv is None:
        return None
    return len(rv)

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
        self.logger = logging.getLogger("omero.tables.HdfList")
        self._lock = threading.RLock()
        self.__filenos = {}
        self.__paths = {}
        self.__locks = {}

    @locked
    def addOrThrow(self, hdfpath, hdfstorage):

        if hdfpath in self.__locks:
            raise omero.LockTimeout(None, None, "Path already in HdfList: %s" % hdfpath)

        parent = path(hdfpath).parent
        if not parent.exists():
            raise omero.ApiUsageException(None, None, "Parent directory does not exist: %s" % parent)

        lock = None
        try:
            lock = open(hdfpath, "a+")
            portalocker.lock(lock, portalocker.LOCK_NB|portalocker.LOCK_EX)
            self.__locks[hdfpath] = lock
        except portalocker.LockException, le:
            if lock:
                lock.close()
            raise omero.LockTimeout(None, None, "Cannot acquire exclusive lock on: %s" % hdfpath, 0)
        except:
            if lock:
                lock.close()
            raise

        hdffile = hdfstorage.openfile("a")
        fileno = hdffile.fileno()
        if fileno in self.__filenos.keys():
            hdffile.close()
            raise omero.LockTimeout(None, None, "File already opened by process: %s" % hdfpath, 0)
        else:
            self.__filenos[fileno] = hdfstorage
            self.__paths[hdfpath] = hdfstorage

        return hdffile

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
        try:
            if hdfpath in self.__locks:
                try:
                    lock = self.__locks[hdfpath]
                    lock.close()
                finally:
                    del self.__locks[hdfpath]
        except Exception, e:
            self.logger.warn("Exception on remove(%s)" % hdfpath, exc_info=True)

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

        self.logger = logging.getLogger("omero.tables.HdfStorage")

        self.__hdf_path = path(file_path)
        # Locking first as described at:
        # http://www.pytables.org/trac/ticket/185
        self.__hdf_file = HDFLIST.addOrThrow(file_path, self)
        self.__tables = []

        self._lock = threading.RLock()
        self._stamp = time.time()

        # These are what we'd like to have
        self.__mea = None
        self.__ome = None

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

    def size(self):
        return self.__hdf_path.size

    def openfile(self, mode):
        try:
            if self.__hdf_path.exists() and self.__hdf_path.size == 0:
                mode = "w"
            return tables.openFile(self.__hdf_path, mode=mode,\
                title="OMERO HDF Measurement Storage", rootUEP="/")
        except (tables.HDF5ExtError, IOError), io:
            msg = "HDFStorage initialized with bad path: %s" % self.__hdf_path
            self.logger.error(msg)
            raise omero.ValidationException(None, None, msg)

    def __initcheck(self):
        if not self.__initialized:
            raise omero.ApiUsageException(None, None, "Not yet initialized")

    def __width(self):
        return len(self.__types)

    def __length(self):
        return self.__mea.nrows

    def __sizecheck(self, colNumbers, rowNumbers):
        if colNumbers is not None:
            if len(colNumbers) > 0:
                maxcol = max(colNumbers)
                totcol = self.__width()
                if maxcol >= totcol:
                    raise omero.ApiUsageException(None, None, "Column overflow: %s >= %s" % (maxcol, totcol))
            else:
                raise omero.ApiUsageException(None, None, "Columns not specified: %s" % colNumbers)


        if rowNumbers is not None:
            if len(rowNumbers) > 0:
                maxrow = max(rowNumbers)
                totrow = self.__length()
                if maxrow >= totrow:
                    raise omero.ApiUsageException(None, None, "Row overflow: %s >= %s" % (maxrow, totrow))
            else:
                raise omero.ApiUsageException(None, None, "Rows not specified: %s" % rowNumbers)

    #
    # Locked methods
    #

    @locked
    def initialize(self, cols, metadata = None):
        """

        """
        if metadata is None: metadata = {}

        if self.__initialized:
            raise omero.ValidationException(None, None, "Already initialized.")

        if not cols:
            raise omero.ApiUsageException(None, None, "No columns provided")

        for c in cols:
            if not c.name:
                raise omero.ApiUsageException(None, None, "Column unnamed: %s" % c)

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
        sz = len(self.__tables)
        self.logger.info("Size: %s - Attaching %s to %s" % (sz, table, self.__hdf_path))
        if table in self.__tables:
            self.logger.warn("Already added")
            raise omero.ApiUsageException(None, None, "Already added")
        self.__tables.append(table)
        return sz + 1

    @locked
    def decr(self, table):
        sz = len(self.__tables)
        self.logger.info("Size: %s - Detaching %s from %s", sz, table, self.__hdf_path)
        if not (table in self.__tables):
            self.logger.warn("Unknown table")
            raise omero.ApiUsageException(None, None, "Unknown table")
        self.__tables.remove(table)
        if sz <= 1:
            self.cleanup()
        return sz - 1

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
                col.setsize(size)
                col.settable(self.__mea)
                cols.append(col)
            except:
                msg = traceback.format_exc()
                raise omero.ValidationException(None, msg, "BAD COLUMN TYPE: %s for %s" % (t,n))
        return cols

    @locked
    def get_meta_map(self):
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
            elif type(val) == numpy.int64:
                val = rlong(val)
            elif type(val) == numpy.string_:
                val = rstring(val)
            else:
                raise omero.ValidationException("BAD TYPE: %s" % type(val))
            metadata[key] = val
        return metadata

    @locked
    def add_meta_map(self, m):
        if not m:
            return
        self.__initcheck()
        attr = self.__mea.attrs
        for k, v in m.items():
            attr[k] = unwrap(v)
        self.__mea.flush()

    @locked
    def append(self, cols):
        self.__initcheck()
        # Optimize!
        arrays = []
        dtypes = []
        sz = None
        for col in cols:
            if sz is None:
                sz = col.getsize()
            else:
                if sz != col.getsize():
                    raise omero.ValidationException("Columns are of differing length")
            arrays.extend(col.arrays())
            dtypes.extend(col.dtypes())
            col.append(self.__mea) # Potential corruption !!!

        # Convert column-wise data to row-wise records
        records = numpy.array(zip(*arrays), dtype=dtypes)

        self.__mea.append(records)
        self.__mea.flush()

    #
    # Stamped methods
    #

    @stamped
    def update(self, stamp, data):
        self.__initcheck()
        if data:
            for i, rn in enumerate(data.rowNumbers):
                for col in data.columns:
                    getattr(self.__mea.cols, col.name)[rn] = col.values[i]
        self.__mea.flush()

    @stamped
    def getWhereList(self, stamp, condition, variables, unused, start, stop, step):
        self.__initcheck()
        try:
            return self.__mea.getWhereList(condition, variables, None, start, stop, step).tolist()
        except (NameError, SyntaxError, TypeError, ValueError), err:
            aue = omero.ApiUsageException()
            aue.message = "Bad condition: %s, %s" % (condition, variables)
            aue.serverStackTrace = "".join(traceback.format_exc())
            aue.serverExceptionClass = str(err.__class__.__name__)
            raise aue

    def _as_data(self, cols, rowNumbers):
        """
        Constructs a omero.grid.Data object for returning to the client.
        """
        data = omero.grid.Data()
        data.columns = cols
        data.rowNumbers = rowNumbers
        data.lastModification = long(self._stamp*1000) # Convert to millis since epoch
        return data

    @stamped
    def readCoordinates(self, stamp, rowNumbers, current):
        self.__initcheck()
        self.__sizecheck(None, rowNumbers)
        cols = self.cols(None, current)
        for col in cols:
            col.readCoordinates(self.__mea, rowNumbers)
        return self._as_data(cols, rowNumbers)

    @stamped
    def read(self, stamp, colNumbers, start, stop, current):
        self.__initcheck()
        self.__sizecheck(colNumbers, None)
        cols = self.cols(None, current)

        rows = self._getrows(start, stop)
        rv, l = self._rowstocols(rows, colNumbers, cols)
        return self._as_data(rv, range(start, start+l))

    def _getrows(self, start, stop):
        return self.__mea.read(start, stop)

    def _rowstocols(self, rows, colNumbers, cols):
        l = 0
        rv   = []
        for i in colNumbers:
            col = cols[i]
            col.fromrows(rows)
            rv.append(col)
            if not l:
                l = len(col.values)
        return rv, l

    @stamped
    def slice(self, stamp, colNumbers, rowNumbers, current):
        self.__initcheck()

        if colNumbers is None or len(colNumbers) == 0:
            colNumbers = range(self.__width())
        if rowNumbers is None or len(rowNumbers) == 0:
            rowNumbers = range(self.__length())

        self.__sizecheck(colNumbers, rowNumbers)
        cols = self.cols(None, current)
        rv   = []
        for i in colNumbers:
            col = cols[i]
            col.readCoordinates(self.__mea, rowNumbers)
            rv.append(col)
        return self._as_data(rv, rowNumbers)

    #
    # Lifecycle methods
    #

    def check(self):
        return True

    @locked
    def cleanup(self):
        self.logger.info("Cleaning storage: %s", self.__hdf_path)
        if self.__mea:
            self.__mea.flush()
            self.__mea = None
        if self.__ome:
            self.__ome = None
        if self.__hdf_file:
            HDFLIST.remove(self.__hdf_path, self.__hdf_file)
        hdffile = self.__hdf_file
        self.__hdf_file = None
        hdffile.close() # Resources freed

# End class HdfStorage


class TableI(omero.grid.Table, omero.util.SimpleServant):
    """
    Spreadsheet implementation based on pytables.
    """

    def __init__(self, ctx, file_obj, factory, storage, uuid = "unknown", \
            call_context = None):
        self.uuid = uuid
        self.file_obj = file_obj
        self.factory = factory
        self.storage = storage
        self.call_context = call_context
        self.can_write = factory.getAdminService().canUpdate(file_obj, call_context)
        omero.util.SimpleServant.__init__(self, ctx)

        self.stamp = time.time()
        self.storage.incr(self)

        self._closed = False

    def assert_write(self):
        """
        Checks that the current user can write to the given object
        at the database level. If not, no FS level writes are permitted
        either.

        ticket:2910
        """
        if not self.can_write:
            raise omero.SecurityViolation("Current user cannot write to file %s" % self.file_obj.id.val)

    def check(self):
        """
        Called periodically to check the resource is alive. Returns
        False if this resource can be cleaned up. (Resources API)
        """
        self.logger.debug("Checking %s" % self)
        if self._closed:
            return False

        idname = 'UNKNOWN'
        try:
            idname = self.factory.ice_getIdentity().name
            clientSession = self.ctx.getSession().getSessionService() \
                .getSession(idname)
            if clientSession.getClosed():
                self.logger.debug("Client session closed: %s" % idname)
                return False
            return True
        except Exception:
            self.logger.debug("Client session not found: %s" % idname)
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
        return "Table-%s" % self.uuid

    @remoted
    @perf
    def close(self, current = None):

        size = None
        if self.storage is not None:
            size = self.storage.size() # Size to reset the server object to

        try:
            self.cleanup()
            self.logger.info("Closed %s", self)
        except:
            self.logger.warn("Closed %s with errors", self)

        self._closed = True

        if self.file_obj is not None and self.can_write:
            fid = self.file_obj.id.val
            if not self.file_obj.isLoaded() or\
                self.file_obj.getDetails() is None or\
                self.file_obj.details.group is None:
                self.logger.warn("Cannot update file object %s since group is none", fid)
            else:
                gid = self.file_obj.details.group.id.val
                client_uuid = self.factory.ice_getIdentity().category[8:]
                ctx = {"omero.group": str(gid), omero.constants.CLIENTUUID: client_uuid}
                try:
                    rfs = self.factory.createRawFileStore(ctx)
                    try:
                        rfs.setFileId(fid, ctx)
                        if size:
                            rfs.truncate(size, ctx)     # May do nothing
                            rfs.write([], size, 0, ctx) # Force an update
                        else:
                            rfs.write([], 0, 0, ctx)    # No-op
                        file_obj = rfs.save(ctx)
                    finally:
                        rfs.close(ctx)
                    self.logger.info("Updated file object %s to sha1=%s (%s bytes)",\
                        self.file_obj.id.val, file_obj.sha1.val, file_obj.size.val)
                except:
                    self.logger.warn("Failed to update file object %s", self.file_obj.id.val, exc_info=1)

    # TABLES READ API ============================

    @remoted
    @perf
    def getOriginalFile(self, current = None):
        msg = "unknown"
        if self.file_obj:
            if self.file_obj.id:
                msg = self.file_obj.id.val
        self.logger.info("%s.getOriginalFile() => id=%s", self, msg)
        return self.file_obj

    @remoted
    @perf
    def getHeaders(self, current = None):
        rv = self.storage.cols(None, current)
        self.logger.info("%s.getHeaders() => size=%s", self, slen(rv))
        return rv

    @remoted
    @perf
    def getNumberOfRows(self, current = None):
        rv = self.storage.rows()
        self.logger.info("%s.getNumberOfRows() => %s", self, rv)
        return long(rv)

    @remoted
    @perf
    def getWhereList(self, condition, variables, start, stop, step, current = None):
        variables = unwrap(variables)
        if stop == 0:
            stop = None
        if step == 0:
            step = None
        rv = self.storage.getWhereList(self.stamp, condition, variables, None, start, stop, step)
        self.logger.info("%s.getWhereList(%s, %s, %s, %s, %s) => size=%s", self, condition, variables, start, stop, step, slen(rv))
        return rv

    @remoted
    @perf
    def readCoordinates(self, rowNumbers, current = None):
        self.logger.info("%s.readCoordinates(size=%s)", self, slen(rowNumbers))
        try:
            return self.storage.readCoordinates(self.stamp, rowNumbers, current)
        except tables.HDF5ExtError, err:
            aue = omero.ApiUsageException()
            aue.message = "Error reading coordinates. Most likely out of range"
            aue.serverStackTrace = "".join(traceback.format_exc())
            aue.serverExceptionClass = str(err.__class__.__name__)
            raise aue

    @remoted
    @perf
    def read(self, colNumbers, start, stop, current = None):
        self.logger.info("%s.read(%s, %s, %s)", self, colNumbers, start, stop)
        if start == 0L and stop == 0L:
            stop = None
        try:
            return self.storage.read(self.stamp, colNumbers, start, stop, current)
        except tables.HDF5ExtError, err:
            aue = omero.ApiUsageException()
            aue.message = "Error reading coordinates. Most likely out of range"
            aue.serverStackTrace = "".join(traceback.format_exc())
            aue.serverExceptionClass = str(err.__class__.__name__)
            raise aue

    @remoted
    @perf
    def slice(self, colNumbers, rowNumbers, current = None):
        self.logger.info("%s.slice(size=%s, size=%s)", self, slen(colNumbers), slen(rowNumbers))
        return self.storage.slice(self.stamp, colNumbers, rowNumbers, current)

    # TABLES WRITE API ===========================

    @remoted
    @perf
    def initialize(self, cols, current = None):
        self.assert_write()
        self.storage.initialize(cols)
        if cols:
            self.logger.info("Initialized %s with %s col(s)", self, slen(cols))

    @remoted
    @perf
    def addColumn(self, col, current = None):
        self.assert_write()
        raise omero.ApiUsageException(None, None, "NYI")

    @remoted
    @perf
    def addData(self, cols, current = None):
        self.assert_write()
        self.storage.append(cols)
        sz = 0
        if cols and cols[0] and cols[0].getsize():
            self.logger.info("Added %s row(s) of data to %s", cols[0].getsize(), self)

    @remoted
    @perf
    def update(self, data, current = None):
        self.assert_write()
        if data:
            self.storage.update(self.stamp, data)
            self.logger.info("Updated %s row(s) of data to %s", slen(data.rowNumbers), self)

    @remoted
    @perf
    def delete(self, current = None):
        self.assert_write()
        self.close()
        prx = self.factory.getDeleteService()
        dc = omero.api.delete.DeleteCommand("/OriginalFile", self.file_obj.id.val, None)
        handle = prx.queueDelete([dc])
        self.file_obj = None
        # TODO: possible just return handle?
        cb = omero.callbacks.DeleteCallbackI(current.adapter, handle)
        count = 10
        while count:
            count -= 1
            rv = cb.block(500)
            if rv is not None:
                report = handle.report()[0]
                if rv > 0:
                    raise omero.InternalException(None, None, report.error)
                else:
                    return
        raise omero.InternalException(None, None, "delete timed-out")


    # TABLES METADATA API ===========================

    @remoted
    @perf
    def getMetadata(self, key, current = None):
        rv = self.storage.get_meta_map()
        rv = rv.get(key)
        self.logger.info("%s.getMetadata() => %s", self, unwrap(rv))
        return rv

    @remoted
    @perf
    def getAllMetadata(self, current = None):
        rv = self.storage.get_meta_map()
        self.logger.info("%s.getMetadata() => size=%s", self, slen(rv))
        return rv

    @remoted
    @perf
    def setMetadata(self, key, value, current = None):
        self.assert_write()
        self.storage.add_meta_map({key: value})
        self.logger.info("%s.setMetadata() => %s=%s", self, key, unwrap(value))

    @remoted
    @perf
    def setAllMetadata(self, value, current = None):
        self.assert_write()
        self.storage.add_meta_map({"key": wrap(value)})
        self.logger.info("%s.setMetadata() => number=%s", self, slen(value))

    # Column methods missing

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
            self.stop_event.wait(5)
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
    def getTable(self, file_obj, factory, current = None):
        """
        Create and/or register a table servant.
        """

        # Will throw an exception if not allowed.
        file_id = None
        if file_obj is not None and file_obj.id is not None:
            file_id = file_obj.id.val
        self.logger.info("getTable: %s %s", file_id, current.ctx)

        file_path = self.repo_mgr.getFilePath(file_obj)
        p = path(file_path).dirname()
        if not p.exists():
            p.makedirs()

        storage = HDFLIST.getOrCreate(file_path)
        id = Ice.Identity()
        id.name = Ice.generateUUID()
        table = TableI(self.ctx, file_obj, factory, storage, uuid = id.name, \
                call_context=current.ctx)
        self.resources.add(table)

        prx = current.adapter.add(table, id)
        return self._table_cast(prx)
