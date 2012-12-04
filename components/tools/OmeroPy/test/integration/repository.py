#!/usr/bin/env python

"""
   Integration test focused on the Repository API

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest, time, os
import integration.library as lib
import omero
from omero.rtypes import *

class AbstractRepoTest(lib.ITest):

    def getManagedRepo(self, client=None):
        if client is None:
            client = self.client
        repoMap = client.sf.sharedResources().repositories()
        prx = None
        found = False
        for prx in repoMap.proxies:
            if not prx: continue
            prx = omero.grid.ManagedRepositoryPrx.checkedCast(prx)
            if prx:
                found = True
                break
        self.assert_(found)
        return prx


class TestRepository(AbstractRepoTest):

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

    def testSanityCheckRepos(self):
        # Repos should behave sensibly when it comes
        # to listing their path and objects as well
        # as what items they return.
        repoMap = self.client.sf.sharedResources().repositories()
        managed = None
        public = None
        script = None
        for obj, prx in zip(repoMap.descriptions, repoMap.proxies):
            if prx:
                root = prx.root()
                assert ".omero" not in prx.list(root.path.val + root.name.val)
                assert ".omero" not in \
                        [x.name.val for x in prx.listFiles(root.path.val + root.name.val)]
                for x in ("id", "path", "name"):
                    a = getattr(obj, x)
                    b = getattr(root, x)
                    if a is None:
                        self.assertEquals(a, b)
                    else:
                        self.assertEquals(a.val, b.val)

    def testManagedRepo(self):
        mrepo = self.getManagedRepo(self.client)

        # Create a file in the repo
        path = mrepo.getCurrentRepoDir(["testManagedRepo.txt"])[0]
        base = os.path.dirname(path)
        mrepo.makeDir(base)
        rfs = mrepo.file(path, "rw")
        rfs.write("hi".encode("utf-8"), 0, 2)
        rfs.close()

        # Query it
        assert "testManagedRepo.txt" in mrepo.list(base)[0]
        mime = mrepo.mimetype(path)

        # Register the file. This is currently necessary,
        # but likely will need to be done one rfs.close()
        obj = mrepo.register(path, omero.rtypes.rstring(mime))

        # Now we try to look it up with __redirect
        rfs = self.client.sf.createRawFileStore()
        rfs.setFileId(obj.id.val)
        self.assertEquals("hi", unicode(rfs.read(0, 2), "utf-8"))
        rfs.close()

class TestManagedRepositoryMultiUser(AbstractRepoTest):

    def setup2RepoUsers(self, perms="rw----"):
        group = self.new_group(perms=perms)
        client1, user1 = self.new_client_and_user(group=group)
        client2, user2 = self.new_client_and_user(group=group)

        mrepo1 = self.getManagedRepo(client1)
        mrepo2 = self.getManagedRepo(client2)
        return client1, mrepo1, client2, mrepo2

    def createFile(self, mrepo1, filename):
        rfs = mrepo1.file(filename, "rw")
        try:
            rfs.write("hi", 0, 2)
            ofile = rfs.save()
            return ofile
        finally:
            rfs.close()

    def assertWrite(self, mrepo2, filename, ofile):
        def _write(rfs):
            try:
                rfs.write("bye", 0, 3)
                self.assertEquals("bye", rfs.read(0, 3))
                # Resetting for other expectations
                rfs.truncate(2)
                rfs.write("hi", 0, 2)
                self.assertEquals("hi", rfs.read(0, 2))
            finally:
                rfs.close()

        # TODO: fileById is always "r"
        # rfs = mrepo2.fileById(ofile.id.val)
        # _write(rfs)

        rfs = mrepo2.file(filename, "rw")
        _write(rfs)

    def assertNoWrite(self, mrepo2, filename, ofile):
        def _nowrite(rfs):
            try:
                self.assertRaises(omero.SecurityViolation,
                    rfs.write, "bye", 0, 3)
                self.assertEquals("hi", rfs.read(0, 2))
            finally:
                rfs.close()

        rfs = mrepo2.fileById(ofile.id.val)
        _nowrite(rfs)

        rfs = mrepo2.file(filename, "r")
        _nowrite(rfs)

        # Can't even acquire a writeable-rfs.
        self.assertRaises(omero.SecurityViolation,
            mrepo2.file, filename, "rw")

    def assertDirWrite(self, mrepo2, dirname):
        self.createFile(mrepo2, dirname+"/file2.txt")

    def assertNoDirWrite(self, mrepo2, dirname):
        # Also check that it's not possible to write
        # in someone elses directory.
        self.assertRaises(omero.SecurityViolation,
            self.createFile, mrepo2, dirname+"/file2.txt")


    def assertNoRead(self, mrepo2, filename, ofile):
        self.assertRaises(omero.SecurityViolation,
            mrepo2.fileById, ofile.id.val)
        self.assertRaises(omero.SecurityViolation,
            mrepo2.file, filename, "r")

    def assertRead(self, mrepo2, filename, ofile):
        def _read(rfs):
            try:
                self.assertEquals("hi", rfs.read(0, 2))
            finally:
                rfs.close()

        rfs = mrepo2.fileById(ofile.id.val)
        _read(rfs)

        rfs = mrepo2.file(filename, "r")
        _read(rfs)

    def assertListings(self, mrepo1, uuid):
        self.assertEquals(["/"+uuid], mrepo1.list("."))
        self.assertEquals(["/"+uuid+"/b"], mrepo1.list(uuid+"/"))
        self.assertEquals(["/"+uuid+"/b/c"], mrepo1.list(uuid+"/b/"))
        self.assertEquals(["/"+uuid+"/b/c/file.txt"], mrepo1.list(uuid+"/b/c/"))

    def testTopPrivateGroup(self):

        filename = self.uuid() + ".txt"
        client1, mrepo1, client2, mrepo2 = self.setup2RepoUsers("rw----")

        # No intermediate directories
        ofile = self.createFile(mrepo1, filename)

        self.assertRead(mrepo1, filename, ofile)
        self.assertWrite(mrepo1, filename, ofile)

        self.assertNoRead(mrepo2, filename, ofile)

        self.assertEquals(0, len(mrepo2.listFiles(".")))

    def testDirPrivateGroup(self):

        uuid = self.uuid()
        dirname = uuid + "/b/c"
        filename = dirname + "/file.txt"

        client1, mrepo1, client2, mrepo2 = self.setup2RepoUsers("rw----")

        mrepo1.makeDir(dirname)
        ofile = self.createFile(mrepo1, filename)

        self.assertRead(mrepo1, filename, ofile)
        self.assertListings(mrepo1, uuid)
        self.assertWrite(mrepo1, filename, ofile)

        self.assertNoRead(mrepo2, filename, ofile)
        self.assertRaises(omero.SecurityViolation,
            mrepo2.listFiles, dirname)

    def testDirReadOnlyGroup(self):

        uuid = self.uuid()
        dirname = uuid + "/b/c"
        filename = dirname + "/file.txt"

        client1, mrepo1, client2, mrepo2 = self.setup2RepoUsers("rwr---")

        mrepo1.makeDir(dirname)
        ofile = self.createFile(mrepo1, filename)

        self.assertRead(mrepo1, filename, ofile)
        self.assertListings(mrepo1, uuid)
        self.assertWrite(mrepo1, filename, ofile)

        self.assertRead(mrepo2, filename, ofile)
        self.assertListings(mrepo2, uuid)
        self.assertNoWrite(mrepo2, filename, ofile)
        self.assertNoDirWrite(mrepo2, dirname)

    def testDirReadWriteGroup(self):

        uuid = self.uuid()
        dirname = uuid + "/b/c"
        filename = dirname + "/file.txt"

        client1, mrepo1, client2, mrepo2 = self.setup2RepoUsers("rwrw--")

        mrepo1.makeDir(dirname)
        ofile = self.createFile(mrepo1, filename)

        self.assertRead(mrepo1, filename, ofile)
        self.assertListings(mrepo1, uuid)
        self.assertWrite(mrepo1, filename, ofile)

        self.assertRead(mrepo2, filename, ofile)
        self.assertWrite(mrepo2, filename, ofile)
        self.assertListings(mrepo2, uuid)
        self.assertDirWrite(mrepo2, dirname)

    def testDirReadAnnotateGroup(self):

        uuid = self.uuid()
        dirname = uuid + "/b/c"
        filename = dirname + "/file.txt"

        client1, mrepo1, client2, mrepo2 = self.setup2RepoUsers("rwra--")

        mrepo1.makeDir(dirname)
        ofile = self.createFile(mrepo1, filename)

        self.assertRead(mrepo1, filename, ofile)
        self.assertListings(mrepo1, uuid)
        self.assertWrite(mrepo1, filename, ofile)

        self.assertRead(mrepo2, filename, ofile)
        self.assertListings(mrepo2, uuid)
        self.assertNoWrite(mrepo2, filename, ofile)
        self.assertDirWrite(mrepo2, dirname)

if __name__ == '__main__':
    unittest.main()
