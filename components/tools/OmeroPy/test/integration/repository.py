#!/usr/bin/env python

"""
   Integration test focused on the Repository API

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest, time
import integration.library as lib
import omero
from omero.rtypes import *

class TestRepository(lib.ITest):

    def testBasicUsage(self):

        test_file = "FIXME.dv"
        remote_file = "/root/dir1/test.dv"

        write_start = time.time()

        repoMap = self.client.sf.sharedResources().repositories()
        self.assert_( len(repoMap.descriptions) > 1 )
        self.assert_( len(repoMap.proxies) > 1 )

        repoPrx = repoMap.proxies[0]
        self.assert_( repoPrx ) # Could be None

        # This is a write-only (no read, no config)
        # version of this service.
        if False:
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

    def testAllMethods(self):
        path = self.root.sf.getConfigService().getConfigValue("omero.data.dir")
        repoMap = self.client.sf.sharedResources().repositories()
        for obj, prx in zip(repoMap.descriptions, repoMap.proxies):
            self.assertRepo(path, obj, prx)

    def assertRepo(self, path, obj, prx):
        print path
        root = prx.root()
        for x in ("id", "path", "name"):
            a = getattr(obj, x)
            b = getattr(root, x)
            if a is None:
                self.assertEquals(a, b)
            else:
                self.assertEquals(a.val, b.val)
        print prx.list(root.path.val + root.name.val + "/omero")



if __name__ == '__main__':
    unittest.main()
