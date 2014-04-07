#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
/*
 *   $Id$
 *
 *   Copyright 2009-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
"""

import logging
logging.basicConfig(level=0)

import omero.util.temp_files as t_f


class TestTemps(object):

    def testBasicUsage(self):
        p = t_f.create_path("foo", ".bar")
        assert p.exists()
        t_f.remove_path(p)
        assert not p.exists()

    def testBasicUsagePassString(self):
        p = t_f.create_path("foo", ".bar")
        assert p.exists()
        t_f.remove_path(str(p))
        assert not p.exists()

    def testNoCleanUp(self):
        p = t_f.create_path("foo", ".bar")
        assert p.exists()
        # Logger should print out one file

    def testUsingThePath(self):
        p = t_f.create_path("write", ".txt")
        p.write_text("hi")
        assert ["hi"] == p.lines()

    def testUsingThePath2(self):
        p = t_f.create_path("write2", ".txt")
        p.write_text("hi2")
        assert ["hi2"] == p.lines()

    def testUsingThePathAndAFile(self):
        p = t_f.create_path("write", ".txt")
        p.write_text("hi")
        f = open(str(p), "r")
        assert ["hi"] == f.readlines()
        f.close()

    def testFolderSimple(self):
        p = t_f.create_path("close", ".dir", folder=True)
        assert p.exists()
        assert p.isdir()
        return p

    def testFolderWrite(self):
        p = self.testFolderSimple()
        f = p / "file"
        f.write_text("hi")
        return p

    def testFolderDelete(self):
        p = self.testFolderWrite()
        p.rmtree()

    #
    # Misc
    #

    def DISABLEDtestManagerPrefix(self):
        mgr = t_f.TempFileManager(prefix="omero_temp_files_test")
        dir = mgr.gettempdir()
        mgr.clean_tempdir()  # start with a blank dir
        assert not dir.exists()
        mgr.create_path("test", ".tmp")
        assert dir.exists()
        mgr.clean_tempdir()
        # There should still be one file lock
        assert dir.exists()
