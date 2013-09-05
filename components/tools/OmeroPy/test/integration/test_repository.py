#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test focused on the Repository API

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest, time
import test.integration.library as lib
import omero
from omero.rtypes import *

class TestRepository(lib.ITest):

    def testRepositoryAcquisition(self):

        repoMap = self.client.sf.sharedResources().repositories()
        self.assert_( len(repoMap.proxies) > 1 )
        repoPrx = repoMap.proxies[0]
        self.assert_( repoPrx )

    # Not all repository methods are implemented in 4.4
    # and so the test below is inavlid
    def DISABLEDtestBasicUsage(self):

        test_file = "FIXME.dv"
        remote_file = "/root/dir1/test.dv"

        write_start = time.time()

        repoMap = self.client.sf.sharedResources().repositories()
        self.assert_( len(repoMap.proxies) > 1 )

        repoPrx = repoMap.proxies[0]
        self.assert_( repoPrx ) # Could be None

        # This is a write-only (no read, no config)
        # version of this service.
        rawFileStore = repoPrx.write(remote_file)
        try:
            offset = 0
            file = open(test_file,"rb")
            try:
                while True:
                    block = file.read(block_size)
                    if not block:
                        break
                    rawFileStore.write(block, offset, len(block))
                    offset += len(block)
            finally:
                file.close()
        finally:
            rawFileStore.close()

        write_end = time.time()

        # Check the SHA1
        file = repoPrx.load(remote_file)
        sha1_remote = file.sha1.val
        sha1_local = self.client.sha1(test_file)


        self.fail("HOW ARE WE CHECKING SHA1 HERE")


        read_start = time.time()

        #
        # Raw pixels
        #
        rawPixelsStore = repoPrx.pixels(remote_file)
        try:
            pass
        finally:
            rawPixelsStore.close()

        read_end = time.time()

        #
        # Rendering
        #

        renderingEngine = repoPrx.render(remote_file)
        try:
            planeDef = omero.romio.PlaneDef()
            planeDef.z = 0
            planeDef.t = 0
            rgbBuffer = renderingEngine.render(planeDef)
        finally:
            renderingEngine.close()

        thumbnailStore = repoPrx.thumbs(remote_file)
        thumbnailStore.close()
        rawFileStore = repoPrx.read(remote_file)
        rawFileStore.close()
        repoPrx.rename(remote_file, remote_file + ".old")
        repoPrx.delete(remote_file + ".old")


if __name__ == '__main__':
    unittest.main()
