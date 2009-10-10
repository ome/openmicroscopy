#!/usr/bin/env python
#
# Copyright 2009 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
"""
OMERO Support for temporary files and directories
"""

import os
import sys
import atexit
import logging
import tempfile
import threading
import traceback
import exceptions
import portalocker

from path import path

logging.basicConfig()

# TODO:
#  - locking for command-line cleanup
#  - plugin for cleaning unlocked files
#  - plugin for counting sizes, etc.
#  - decorator

class TempFileManager(object):
    """
    """
    def __init__(self, prefix = "omero"):
        self.logger = logging.getLogger("omero.util.TempFileManager")
        self.is_win32 = ( sys.platform == "win32" )
        self.prefix = prefix
        self._lock = threading.RLock()
        self._values = {}
        atexit.register(self.cleanup)

    #
    # Methods requiring locking on state
    #

    def add_path(self, name, folder):
        name = str(name)
        self._lock.acquire()
        try:

            if name in self._values:
                raise exceptions.Exception("%s already added!!" % name)

            # DISABLED f = open(name,"r")
            # portalocker.lock(f, portalocker.LOCK_EX|portalocker.LOCK_NB)
            self._values[name] = None # f

            self.logger.debug("Added %s%s (total=%s)",\
                (folder and "folder " or ""), name, len(self._values))

        finally:
            self._lock.release()

    def remove_path(self, name):
        name = str(name)
        self._lock.acquire()
        try:
            try:
                f = self._values[name]
                # DISABLED f.close()
                del self._values[name]
            except KeyError:
                pass
        finally:
            self._lock.release()

        p = path(name)
        if p.exists():
            if p.isdir():
                p.rmtree(onerror = self.on_rmtree)
                self.logger.debug("Removed folder %s (total=%s)", name, len(self._values))
            else:
                p.remove()
                self.logger.debug("Removed %s (total=%s)", name, len(self._values))


    def cleanup(self):
        self.logger.debug("Cleaning...")
        self._lock.acquire()
        try:
            for i in self._values.keys():
                try:
                    self.remove_path(i)
                except:
                    self.logger.info("Failed to remove tempfile: %s" % i)
            del self._values
        finally:
            self._lock.release()

    #
    # Other methods
    #

    def username(self):
        if self.is_win32:
            import win32api
            return win32api.GetUserName()
        else:
            return os.getlogin()

    def access(self, dir):
        dir = str(dir)
        return os.access(dir, os.W_OK)

    def create(self, dir):
        dir = path(dir)
        if not dir.exists():
            dir.makedirs(0700)
            return True
        return False

    def gettempdir(self):
        dir = path(tempfile.gettempdir()) / ("%s_%s" % (self.prefix, self.username()))
        if self.create(dir) or self.access(dir):
            return dir

        i = 0
        while i < 10:
            t = path("%s_%s" % (dir, i))
            if self.create(t) or self.access(t):
                return t
        raise exceptions.Exception("Failed to create temporary directory")

    def tempsubdir(self, categories):
        cat = list(categories)
        dir = self.gettempdir()
        for c in cat:
            dir = dir / c
        self.create(dir)
        return dir

    def create_path(self, categories, prefix, suffix, folder = False, text = False, mode = "r+"):
        dir = self.tempsubdir(categories)

        if folder:
            name = tempfile.mkdtemp(prefix = prefix, suffix = suffix, dir = dir)
        else:
            fd, name = tempfile.mkstemp(prefix = prefix, suffix = suffix, dir = dir, text = text)
            try:
                os.close(fd)
            except:
                self.logger.warn("Failed to close fd %s" % fd)

        try:
            self.add_path(name, folder)
        except:
            try:
                self.remove_path(name)
            except:
                self.logger.warn("Error during exception cleanup", exc_info = True)
            raise

        return path(name)

    def clean_tempdir(self):
        dir = self.gettempdir()
        self.logger.info("Removing tree: %s", dir)
        dir.rmtree(onerror = self.on_rmtree)

    def on_rmtree(self, func, name, exc):
        self.logger.error("rmtree error: %s of %s => %s", func, name, exc)

manager = TempFileManager()
"""
Global TempFileManager instance which is registered with the
atexit module for cleaning up all created files on exit.
"""

def create_path(categories, prefix = "omero", suffix = ".tmp", folder = False):
    """
    Uses the global TempFileManager to create a temporary file.
    """
    return manager.create_path(categories, prefix, suffix, folder = folder)

def remove_path(file):
    """
    Removes the file from the global TempFileManager. The file will be deleted
    if it still exists.
    """
    return manager.remove_path(file)

def gettempdir():
    return manager.gettempdir()

if __name__ == "__main__":
    if len(sys.argv) > 1:
        args = sys.argv[1:]
        if "clean" in args:
            manager.clean_tempdir()
            sys.exit(0)
        elif "dir" in args:
            sys.exit(0)
    else:
        print "Usage: %s clean" % sys.argv[0]
        print "   or: %s dir  " % sys.argv[0]
        sys.exit(2)
