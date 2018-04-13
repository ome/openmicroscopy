#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# OMERO HdfStorage Interface
# Copyright 2009 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import time
import numpy
import logging
import threading
import traceback

from os import W_OK
from path import path

import omero  # Do we need both??
import omero.clients
import omero.callbacks

# For ease of use
from omero.columns import columns2definition
from omero.rtypes import rfloat, rint, rlong, rstring, unwrap
from omero.util.decorators import locked
from omero_ext import portalocker
from functools import wraps


sys = __import__("sys")  # Python sys
tables = __import__("tables")  # Pytables

VERSION = '2'


def internal_attr(s):
    """
    Checks whether this attribute name is reserved for internal use
    """
    return s.startswith('__')


def stamped(func, update=False):
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
            raise omero.OptimisticLockException(
                None, None, "Resource modified by another thread")

        try:
            return func(*args, **kwargs)
        finally:
            if update:
                self._stamp = time.time()
    check_and_update_stamp = wraps(func)(check_and_update_stamp)
    return locked(check_and_update_stamp)


def modifies(func):
    """
    Decorator which always calls flush() on the first argument after the
    method call
    """
    def flush_after(*args, **kwargs):
        self = args[0]
        try:
            return func(*args, **kwargs)
        finally:
            self.flush()
    return wraps(func)(flush_after)


class HdfList(object):

    """
    Since two calls to tables.openFile() return non-equal files
    with equal fileno's, portalocker cannot be used to prevent
    the creation of two HdfStorage instances from the same
    Python process.

    This also holds a global lock for all HDF5 calls since libhdf5 is usually
    compiled without --enable-threadsafe, see
    https://trac.openmicroscopy.org/ome/ticket/10464
    """

    def __init__(self):
        self.logger = logging.getLogger("omero.tables.HdfList")
        self._lock = threading.RLock()
        self.__filenos = {}
        self.__paths = {}

    @locked
    def addOrThrow(self, hdfpath, hdfstorage, read_only=False):

        if hdfpath in self.__paths:
            raise omero.LockTimeout(
                None, None, "Path already in HdfList: %s" % hdfpath)

        parent = path(hdfpath).parent
        if not parent.exists():
            raise omero.ApiUsageException(
                None, None, "Parent directory does not exist: %s" % parent)

        mode = read_only and "r" or "a"
        hdffile = hdfstorage.openfile(mode)
        fileno = hdffile.fileno()

        if not read_only:
            try:
                portalocker.lockno(
                    fileno, portalocker.LOCK_NB | portalocker.LOCK_EX)
            except portalocker.LockException:
                hdffile.close()
                raise omero.LockTimeout(
                    None, None,
                    "Cannot acquire exclusive lock on: %s" % hdfpath, 0)
            except:
                hdffile.close()
                raise

        if fileno in self.__filenos.keys():
            hdffile.close()
            raise omero.LockTimeout(
                None, None, "File already opened by process: %s" % hdfpath, 0)
        else:
            self.__filenos[fileno] = hdfstorage
            self.__paths[hdfpath] = hdfstorage

        return hdffile

    @locked
    def getOrCreate(self, hdfpath, read_only=False):
        try:
            return self.__paths[hdfpath]
        except KeyError:
            # Adds itself to the global list
            return HdfStorage(hdfpath, self._lock, read_only=read_only)

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

    def __init__(self, file_path, hdf5lock, read_only=False):
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
        self.__hdf_file = HDFLIST.addOrThrow(file_path, self, read_only)
        self.__tables = []

        self._lock = hdf5lock
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

        self._modified = False

    #
    # Non-locked methods
    #

    def size(self):
        return self.__hdf_path.size

    def openfile(self, mode, policy='default'):
        # policy is ignored
        try:
            if self.__hdf_path.exists():
                if self.__hdf_path.size == 0:
                    mode = "w"
                elif mode != "r" and not self.__hdf_path.access(W_OK):
                    self.logger.info(
                        "%s not writable (mode=%s). Opening read-only" % (
                            self.__hdf_path, mode))
                    mode = "r"

            return tables.openFile(str(self.__hdf_path), mode=mode,
                                   title="OMERO HDF Measurement Storage",
                                   rootUEP="/")
        except (tables.HDF5ExtError, IOError) as e:
            msg = "HDFStorage initialized with bad path: %s: %s" % (
                self.__hdf_path, e)
            self.logger.error(msg)
            raise omero.ValidationException(None, None, msg)

    def modified(self):
        return self._modified

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
                    raise omero.ApiUsageException(
                        None, None, "Column overflow: %s >= %s"
                        % (maxcol, totcol))
            else:
                raise omero.ApiUsageException(
                    None, None, "Columns not specified: %s" % colNumbers)

        if rowNumbers is not None:
            if len(rowNumbers) > 0:
                maxrow = max(rowNumbers)
                totrow = self.__length()
                if maxrow >= totrow:
                    raise omero.ApiUsageException(
                        None, None, "Row overflow: %s >= %s"
                        % (maxrow, totrow))
            else:
                raise omero.ApiUsageException(
                    None, None, "Rows not specified: %s" % rowNumbers)

    def __getversion(self):
        """
        In OMERO.tables v2 the version attribute name was changed to __version
        """
        self.__initcheck()
        k = '__version'
        try:
            v = self.__mea.attrs[k]
            if isinstance(v, str):
                return v
        except KeyError:
            k = 'version'
            v = self.__mea.attrs[k]
            if v == 'v1':
                return '1'

        msg = "Invalid version attribute (%s=%s) in path: %s" % (
            k, v, self.__hdf_path)
        self.logger.error(msg)
        raise omero.ValidationException(None, None, msg)

    #
    # Locked methods
    #

    @locked
    def flush(self):
        """
        Flush writes to the underlying table, mark this object as modified
        """
        self._modified = True
        if self.__mea:
            self.__mea.flush()
        self.logger.debug("Modified flag set")

    @locked
    @modifies
    def initialize(self, cols, metadata=None):
        """

        """
        if metadata is None:
            metadata = {}

        if self.__initialized:
            raise omero.ValidationException(None, None, "Already initialized.")

        if not cols:
            raise omero.ApiUsageException(None, None, "No columns provided")

        for c in cols:
            if not c.name:
                raise omero.ApiUsageException(
                    None, None, "Column unnamed: %s" % c)
            if internal_attr(c.name):
                raise omero.ApiUsageException(
                    None, None, "Reserved column name: %s" % c.name)

        self.__definition = columns2definition(cols)
        self.__ome = self.__hdf_file.createGroup("/", "OME")
        self.__mea = self.__hdf_file.createTable(
            self.__ome, "Measurements", self.__definition)

        self.__types = [x.ice_staticId() for x in cols]
        self.__descriptions = [
            (x.description is not None) and x.description or "" for x in cols]
        self.__hdf_file.createArray(self.__ome, "ColumnTypes", self.__types)
        self.__hdf_file.createArray(
            self.__ome, "ColumnDescriptions", self.__descriptions)

        md = {}
        if metadata:
            md = metadata.copy()
        md['__version'] = VERSION
        md['__initialized'] = time.time()
        self.add_meta_map(md, replace=True, init=True)

        self.__hdf_file.flush()
        self.__initialized = True

    @locked
    def incr(self, table):
        sz = len(self.__tables)
        self.logger.info("Size: %s - Attaching %s to %s" %
                         (sz, table, self.__hdf_path))
        if table in self.__tables:
            self.logger.warn("Already added")
            raise omero.ApiUsageException(None, None, "Already added")
        self.__tables.append(table)
        return sz + 1

    @locked
    def decr(self, table):
        sz = len(self.__tables)
        self.logger.info(
            "Size: %s - Detaching %s from %s", sz, table, self.__hdf_path)
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
        descs = self.__descriptions
        cols = []
        for i in range(len(types)):
            t = types[i]
            n = names[i]
            d = descs[i]
            try:
                col = ic.findObjectFactory(t).create(t)
                col.name = n
                col.description = d
                col.setsize(size)
                col.settable(self.__mea)
                cols.append(col)
            except:
                msg = traceback.format_exc()
                raise omero.ValidationException(
                    None, msg, "BAD COLUMN TYPE: %s for %s" % (t, n))
        return cols

    @locked
    def get_meta_map(self):
        self.__initcheck()
        metadata = {}
        attr = self.__mea.attrs
        keys = list(self.__mea.attrs._v_attrnamesuser)
        for key in keys:
            val = attr[key]
            if isinstance(val, float):
                val = rfloat(val)
            elif isinstance(val, int):
                val = rint(val)
            elif isinstance(val, long):
                val = rlong(val)
            elif isinstance(val, str):
                val = rstring(val)
            else:
                raise omero.ValidationException("BAD TYPE: %s" % type(val))
            metadata[key] = val
        return metadata

    @locked
    @modifies
    def add_meta_map(self, m, replace=False, init=False):
        if not init:
            if int(self.__getversion()) < 2:
                # Metadata methods were generally broken for v1 tables so
                # the introduction of internal metadata attributes is unlikely
                # to affect anyone.
                # https://trac.openmicroscopy.org/ome/ticket/12606
                msg = 'Tables metadata is only supported for OMERO.tables >= 2'
                self.logger.error(msg)
                raise omero.ApiUsageException(None, None, msg)

            self.__initcheck()
            for k, v in m.iteritems():
                if internal_attr(k):
                    raise omero.ApiUsageException(
                        None, None, "Reserved attribute name: %s" % k)
                if not isinstance(v, (
                        omero.RString, omero.RLong, omero.RInt, omero.RFloat)):
                    raise omero.ValidationException(
                        "Unsupported type: %s" % type(v))

        attr = self.__mea.attrs
        if replace:
            for f in list(attr._v_attrnamesuser):
                if init or not internal_attr(f):
                    del attr[f]
        if not m:
            return

        for k, v in m.iteritems():
            # This uses the default pytables type conversion, which may
            # convert it to a numpy type or keep it as a native Python type
            attr[k] = unwrap(v)

    @locked
    @modifies
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
                    raise omero.ValidationException(
                        "Columns are of differing length")
            arrays.extend(col.arrays())
            dtypes.extend(col.dtypes())
            col.append(self.__mea)  # Potential corruption !!!

        # Convert column-wise data to row-wise records
        records = numpy.array(zip(*arrays), dtype=dtypes)

        self.__mea.append(records)

    #
    # Stamped methods
    #

    @stamped
    @modifies
    def update(self, stamp, data):
        self.__initcheck()
        if data:
            for i, rn in enumerate(data.rowNumbers):
                for col in data.columns:
                    getattr(self.__mea.cols, col.name)[rn] = col.values[i]

    @stamped
    def getWhereList(self, stamp, condition, variables, unused,
                     start, stop, step):
        self.__initcheck()
        try:
            return self.__mea.getWhereList(condition, variables, None,
                                           start, stop, step).tolist()
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
        # Convert to millis since epoch
        data.lastModification = long(self._stamp * 1000)
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
        return self._as_data(rv, range(start, start + l))

    def _getrows(self, start, stop):
        return self.__mea.read(start, stop)

    def _rowstocols(self, rows, colNumbers, cols):
        l = 0
        rv = []
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
        rv = []
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
            self.__mea = None
        if self.__ome:
            self.__ome = None
        if self.__hdf_file:
            HDFLIST.remove(self.__hdf_path, self.__hdf_file)
        hdffile = self.__hdf_file
        self.__hdf_file = None
        hdffile.close()  # Resources freed

# End class HdfStorage
