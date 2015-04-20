#!/usr/bin/env python
# -*- coding: utf-8 -*-
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

from path import path
from omero.util import get_user_dir, get_user
from omero_ext import portalocker

# Activating logging at a static level
if "DEBUG" in os.environ:
    from omero.util import configure_logging
    configure_logging(loglevel=logging.DEBUG)

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

    def __init__(self, prefix="omero"):
        """
        Initializes a TempFileManager instance with a userDir containing
        the given prefix value, or "omero" by default. Also registers
        an atexit callback to call self.cleanup() on exit.
        """
        self.logger = logging.getLogger("omero.util.TempFileManager")
        self.is_win32 = (sys.platform == "win32")
        self.prefix = prefix

        self.userdir = self.tmpdir() / ("%s_%s" %
                                        (self.prefix, self.username()))
        """
        User-accessible directory of the form $TMPDIR/omero_$USERNAME.
        If the given directory is not writable, an attempt is made
        to use an alternative
        """
        if not self.create(self.userdir) and not self.access(self.userdir):
            i = 0
            while i < 10:
                t = path("%s_%s" % (self.userdir, i))
                if self.create(t) or self.access(t):
                    self.userdir = t
                    break
            raise Exception(
                "Failed to create temporary directory: %s" % self.userdir)
        self.dir = self.userdir / self.pid()
        """
        Directory under which all temporary files and folders will be created.
        An attempt to remove a path not in this directory will lead to an
        exception.
        """

        # Now create the directory. If a later step throws an
        # exception, we should try to rollback this change.
        if not self.dir.exists():
            self.dir.makedirs()
        self.logger.debug("Using temp dir: %s" % self.dir)

        self.lock = None
        try:
            self.lock = open(str(self.dir / ".lock"), "a+")
            """
            .lock file under self.dir which is used to prevent other
            TempFileManager instances (also in other languages) from
            cleaning up this directory.
            """
            try:
                portalocker.lock(
                    self.lock, portalocker.LOCK_EX | portalocker.LOCK_NB)
                atexit.register(self.cleanup)
            except:
                lock = self.lock
                self.lock = None
                if lock:
                    self.lock.close()
                raise
        finally:
            try:
                if not self.lock:
                    self.cleanup()
            except:
                self.logger.warn("Error on cleanup after error", exc_info=True)

    def cleanup(self):
        """
        Releases self.lock and deletes self.dir.
        The lock is released first since on some platforms like Windows
        the lock file cannot be deleted even by the owner of the lock.
        """
        try:
            if self.lock:
                self.lock.close()  # Allow others access
        except:
            self.logger.error("Failed to release lock", exc_info=True)
        self.clean_tempdir()

    def tmpdir(self):
        """
        Returns a platform-specific user-writable temporary directory

        First, the value of "OMERO_TEMPDIR" is attempted (if available),
        then user's home directory, then the global temp director.

        Typical errors for any of the possible temp locations are:
         * non-existence
         * inability to lock

        See: http://trac.openmicroscopy.org.uk/ome/ticket/1653
        """
        locktest = None

        omerotemp = os.environ.get("OMERO_TEMPDIR", None)
        homeprop = None
        try:
            homeprop = get_user_dir()
        except:
            pass  # ticket:3194, ticket:5583
        tempprop = tempfile.gettempdir()
        targets = [omerotemp, homeprop, tempprop]

        name = None
        choice = None
        locktest = None

        for target in targets:

            if target is None:
                continue

            if choice is not None:
                break

            try:

                # 2805
                omero_dir = path(target) / "omero"
                if omero_dir.exists() and not omero_dir.isdir():
                    self.logger.debug(
                        """"omero" is not a directory: %s""" % omero_dir)
                    continue
                tmp_dir = omero_dir / "tmp"
                if tmp_dir.exists() and not tmp_dir.isdir():
                    self.logger.debug(
                        """"tmp" is not a directory: %s""" % tmp_dir)
                    continue

                try:

                    name = self.mkstemp(
                        prefix=".lock_test", suffix=".tmp", dir=target)
                    locktest = open(name, "a+")
                    portalocker.lock(
                        locktest, portalocker.LOCK_EX | portalocker.LOCK_NB)
                    locktest.close()
                    locktest = None
                    choice = target
                    self.logger.debug("Chose global tmpdir: %s", choice)
                finally:
                    if locktest is not None:
                        try:
                            locktest.close()
                        except:
                            self.logger.warn(
                                "Failed to close locktest: %s",
                                name, exc_info=True)

                    if name is not None:
                        try:
                            os.remove(name)
                        except:
                            self.logger.debug("Failed os.remove(%s)", name)

            except Exception, e:
                if "Operation not permitted" in str(e) or \
                   "Operation not supported" in str(e):

                    # This is the issue described in ticket:1653
                    # To prevent printing the warning, we just continue
                    # here.
                    self.logger.debug("%s does not support locking.", target)
                else:
                    self.logger.warn("Invalid tmp dir: %s" %
                                     target, exc_info=True)

        if choice is None:
            raise Exception("Could not find lockable tmp dir")

        return path(choice) / "omero" / "tmp"

    def username(self):
        """
        Returns the current OS-user's name
        """
        return get_user("Unknown")

    def pid(self):
        """
        Returns some representation of the current process's id
        """
        return str(os.getpid())

    def access(self, dir):
        """
        Returns True if the current user can write to the given directory
        """
        dir = str(dir)
        return os.access(dir, os.W_OK)

    def create(self, dir):
        """
        If the given directory doesn't exist, creates it (with mode 0700)
        and returns True. Otherwise False.
        """
        dir = path(dir)
        if not dir.exists():
            dir.makedirs(0700)
            return True
        return False

    def gettempdir(self):
        """
        Returns the directory under which all temporary
        files and folders will be created.
        """
        return self.dir

    def mkstemp(self, prefix, suffix, dir, text=False):
        """
        Similar to tempfile.mkstemp name but immediately closes
        the file descriptor returned and passes back just the name.
        This prevents various Windows issues.
        """
        fd, name = tempfile.mkstemp(
            prefix=prefix, suffix=suffix, dir=dir, text=text)
        self.logger.debug("Added file %s", name)
        try:
            os.close(fd)
        except:
            self.logger.warn("Failed to close fd %s" % fd)
        return name

    def create_path(self, prefix, suffix, folder=False, text=False, mode="r+"):
        """
        Uses tempfile.mkdtemp and tempfile.mkstemp to create temporary
        folders and files, respectively, under self.dir
        """

        if folder:
            name = tempfile.mkdtemp(prefix=prefix, suffix=suffix, dir=self.dir)
            self.logger.debug("Added folder %s", name)
        else:
            name = self.mkstemp(prefix, suffix, self.dir, text)

        return path(name)

    def remove_path(self, name):
        """
        If the given path is under self.dir, then it is deleted
        whether file or folder. Otherwise, an exception is thrown.
        """
        p = path(name)
        parpath = p.parpath(self.dir)
        if len(parpath) < 1:
            raise Exception("%s is not in %s" % (p, self.dir))

        if p.exists():
            if p.isdir():
                try:
                    p.rmtree(onerror=self.on_rmtree)
                    self.logger.debug("Removed folder %s", name)
                except:
                    self.logger.error("Failed to remove folder %s", name)
            else:
                try:
                    p.remove()
                    self.logger.debug("Removed file %s", name)
                except:
                    self.logger.error("Failed to remove file %s", name)

    def clean_tempdir(self):
        """
        Deletes self.dir
        """
        dir = self.gettempdir()
        if dir.exists():
            self.logger.debug("Removing tree: %s", dir)
            dir.rmtree(onerror=self.on_rmtree)

    def clean_userdir(self):
        """
        Attempts to delete all directories under self.userdir
        other than the one owned by this process. If a directory
        is locked, it is skipped.
        """
        self.logger.debug("Cleaning user dir: %s" % self.userdir)
        dirs = self.userdir.dirs()
        for dir in dirs:
            if str(dir) == str(self.dir):
                self.logger.debug("Skipping self: %s", dir)
                continue
            lock = dir / ".lock"
            if lock.exists():  # 1962, on Windows this fails if lock is missing
                f = open(str(lock), "r")
                try:
                    portalocker.lock(
                        f, portalocker.LOCK_EX | portalocker.LOCK_NB)
                    # Must close for Windows, otherwise "...other process"
                    f.close()
                except:
                    print "Locked: %s" % dir
                    continue
            dir.rmtree(onerror=self.on_rmtree)
            print "Deleted: %s" % dir

    def on_rmtree(self, func, name, exc):
        self.logger.error(
            "rmtree error: %s('%s') => %s", func.__name__, name, exc[1])

manager = TempFileManager()
"""
Global TempFileManager instance for use by the current process and
registered with the atexit module for cleaning up all created files on exit.
Other instances can be created for specialized purposes.
"""


def create_path(prefix="omero", suffix=".tmp", folder=False):
    """
    Uses the global TempFileManager to create a temporary file.
    """
    return manager.create_path(prefix, suffix, folder=folder)


def remove_path(file):
    """
    Removes the file from the global TempFileManager. The file will be deleted
    if it still exists.
    """
    return manager.remove_path(file)


def gettempdir():
    """
    Returns the dir value for the global TempFileManager.
    """
    return manager.gettempdir()

if __name__ == "__main__":

    from omero.util import configure_logging

    if len(sys.argv) > 1:
        args = sys.argv[1:]
    else:
        args = []

    # Debug may already be activated. See static block above.
    if "--debug" in args and "DEBUG" not in os.environ:
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
        sys.exit(0)

    print "Usage: %s clean" % sys.argv[0]
    print "   or: %s dir  " % sys.argv[0]
    sys.exit(2)
