"""
/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
"""

import logging
logging.basicConfig(level=0)

import os
import unittest
import omero.util.temp_files as t_f

from path import path
from portalocker import lock, LockException, LOCK_NB, LOCK_EX


class TestTemps(unittest.TestCase):

    def testBasicUsage(self):
        p = t_f.create_path(["test"],"foo",".bar")
        self.assertTrue(p.exists())
        t_f.remove_path(p)
        self.assertFalse(p.exists())

    def testBasicUsagePassString(self):
        p = t_f.create_path(["test"],"foo",".bar")
        self.assertTrue(p.exists())
        t_f.remove_path(str(p))
        self.assertFalse(p.exists())

    def testNoCleanUp(self):
        p = t_f.create_path(["test"],"foo",".bar")
        self.assertTrue(p.exists())
        # Logger should print out one file

    def testLocking(self):
        pass
        #self.assertRaises(LockException, lock, f, LOCK_NB)

    def testUsingThePath(self):
        p = t_f.create_path(["test"], "write", ".txt")
        p.write_text("hi")
        self.assertEquals(["hi"], p.lines())

    def testUsingThePath2(self):
        p = t_f.create_path(["test"], "write2", ".txt")
        p.write_text("hi2")
        self.assertEquals(["hi2"], p.lines())

    def testUsingThePathAndAFile(self):
        p = t_f.create_path(["test"], "write", ".txt")
        p.write_text("hi")
        f = open(str(p), "r")
        self.assertEquals(["hi"], f.readlines())
        f.close()

    def testFolderSimple(self):
        p = t_f.create_path(["test"], "close", ".dir", folder = True)
        self.assertTrue(p.exists())
        self.assertTrue(p.isdir())
        return p

    def testFolderDelete(self):
        p = self.testFolderSimple()
        f = p / "file"
        f.write_text("hi")
        p.rmtree()

    #
    # Folder
    #

    def testCreateFolder(self):
        p = t_f

    #
    # Misc
    #

    def DISABLEDtestManagerPrefix(self):
        mgr = t_f.TempFileManager(prefix="omero_temp_files_test")
        dir = mgr.gettempdir()
        mgr.clean_tempdir() # start with a blank dir
        self.assertFalse(dir.exists())
        p = mgr.create_path(["test"], "test",".tmp")
        self.assertTrue(dir.exists())
        mgr.clean_tempdir()
        # There should still be one file lock
        self.assertTrue(dir.exists())

if __name__ == '__main__':
    unittest.main()
