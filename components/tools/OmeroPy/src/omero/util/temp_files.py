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

# TODO:
#  - locking for command-line cleanup
#  - plugin for cleaning unlocked files
#  - plugin for counting sizes, etc.
#  - decorator

class TempFileManager(object):
    """
    Creates temporary files and folders and makes a best effort
    to remove them on exit (or sooner). Typically only a single
    instance of this class will exist ("manager" variable in this
    module below)
    """

    def __init__(self, prefix = "omero"):
        self.logger = logging.getLogger("omero.util.TempFileManager")
        self.is_win32 = ( sys.platform == "win32" )
        self.prefix = prefix
        self.userdir = path(tempfile.gettempdir()) / ("%s_%s" % (self.prefix, self.username()))
        # If the given userdir is not accessible, we attempt to use an alternative
        if not self.create(self.userdir) and not self.access(self.userdir):
            i = 0
            while i < 10:
                t = path("%s_%s" % (self.userdir, i))
                if self.create(t) or self.access(t):
                    self.userdir = t
                    break
            raise exceptions.Exception("Failed to create temporary directory: %s" % self.userdir)
        self.dir = self.userdir / str(os.getpid())
        if not self.dir.exists():
            self.dir.makedirs()
        self.lock = open(str(self.dir / ".lock"), "a+")
        try:
            portalocker.lock(self.lock, portalocker.LOCK_EX|portalocker.LOCK_NB)
        except:
            self.lock.close()
            raise
        atexit.register(self.cleanup)

    def cleanup(self):
        self.logger.debug("Cleaning...")
        self.clean_tempdir()
        self.lock.close() # Allow others access

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
        return self.dir

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
            self.logger.debug("Added folder %s", name)
        else:
            fd, name = tempfile.mkstemp(prefix = prefix, suffix = suffix, dir = dir, text = text)
            self.logger.debug("Added file %s", name)
            try:
                os.close(fd)
            except:
                self.logger.warn("Failed to close fd %s" % fd)

        return path(name)

    def remove_path(self, name):
        p = path(name)
        parpath = p.parpath(self.dir)
        if len(parpath) < 1:
            raise exceptions.Exception("%s is not in %s" % (p, self.dir))

        if p.exists():
            if p.isdir():
                p.rmtree(onerror = self.on_rmtree)
                self.logger.debug("Removed folder %s", name)
            else:
                p.remove()
                self.logger.debug("Removed %s", name)

    def clean_tempdir(self):
        dir = self.gettempdir()
        self.logger.debug("Removing tree: %s", dir)
        dir.rmtree(onerror = self.on_rmtree)

    def clean_userdir(self):
        self.logger.debug("Cleaning user dir: %s" % self.userdir)
        dirs = self.userdir.dirs()
        for dir in dirs:
            if str(dir) == str(self.dir):
                self.logger.debug("Skipping self: %s", dir)
                continue
            lock = dir / ".lock"
            f = open(str(lock),"r")
            try:
                portalocker.lock(f, portalocker.LOCK_EX|portalocker.LOCK_NB)
            except:
                print "Locked: %s" % dir
                continue
            dir.rmtree(self.on_rmtree)
            print "Deleted: %s" % dir

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

    from omero.util import configure_logging

    if len(sys.argv) > 1:
        args = sys.argv[1:]

        if "--debug" in args:
            configure_logging(loglevel=logging.DEBUG)
        else:
            configure_logging()

        if "clean" in args:
            manager.clean_userdir()
            sys.exit(0)
        elif "dir" in args:
            print manager.gettempdir()
            sys.exit(0)
        elif "lock" in args:
            print "Locking %s" % manager.gettempdir()
            raw_input("Waiting on user input...")
    else:
        print "Usage: %s clean" % sys.argv[0]
        print "   or: %s dir  " % sys.argv[0]
        sys.exit(2)
