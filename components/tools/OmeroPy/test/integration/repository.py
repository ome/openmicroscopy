#!/usr/bin/env python

"""
   Integration test focused on the Repository API

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import platform
import locale
import unittest
import integration.library as lib
import omero

from omero.callbacks import CmdCallbackI
from omero.cmd import ERR
from omero.rtypes import rbool
from omero.rtypes import rstring
from omero.util.temp_files import create_path
from omero_version import omero_version


class AbstractRepoTest(lib.ITest):

    def all(self, client):
        ctx = dict(client.getImplicitContext().getContext())
        ctx["omero.group"] = "-1"
        return ctx

    def getManagedRepo(self, client=None):
        if client is None:
            client = self.client
        repoMap = client.sf.sharedResources().repositories()
        prx = None
        found = False
        for prx in repoMap.proxies:
            if not prx:
                continue
            prx = omero.grid.ManagedRepositoryPrx.checkedCast(prx)
            if prx:
                found = True
                break
        self.assert_(found)
        return prx


class TestRepository(AbstractRepoTest):

    def testBasicUsage(self):

        test_file = __file__
        remote_base = "./%s/dir1" % self.uuid()
        remote_file = "%s/test.dv" % remote_base

        repoMap = self.client.sf.sharedResources().repositories()
        self.assert_(len(repoMap.descriptions) > 1)
        self.assert_(len(repoMap.proxies) > 1)

        repoPrx = repoMap.proxies[0]
        self.assert_(repoPrx)  # Could be None

        # This is a write-only (no read, no config)
        # version of this service.
        repoPrx.makeDir(remote_base, True)
        rawFileStore = repoPrx.file(remote_file, "rw")
        block_size = 1000*1000
        try:
            offset = 0
            file = open(test_file, "rb")
            try:
                while True:
                    block = file.read(block_size)
                    if not block:
                        break
                    rawFileStore.write(block, offset, len(block))
                    offset += len(block)
                ofile = rawFileStore.save()
            finally:
                file.close()
        finally:
            rawFileStore.close()

        # Check the SHA1
        sha1_remote = ofile.sha1.val
        sha1_local = self.client.sha1(test_file)
        self.assertEquals(sha1_remote, sha1_local)

        # Pixels and Thumbs now requires a proper import.

    def testSanityCheckRepos(self):
        # Repos should behave sensibly when it comes
        # to listing their path and objects as well
        # as what items they return.
        repoMap = self.client.sf.sharedResources().repositories()
        for obj, prx in zip(repoMap.descriptions, repoMap.proxies):
            if prx:
                root = prx.root()
                assert ".omero" not in prx.list(root.path.val + root.name.val)
                for x in prx.listFiles(root.path.val + root.name.val):
                    assert ".omero" != x.name.val
                for x in ("id", "path", "name"):
                    a = getattr(obj, x)
                    b = getattr(root, x)
                    if a is None:
                        self.assertEquals(a, b)
                    else:
                        self.assertEquals(a.val, b.val)

    def testManagedRepoAsPubliRepo(self):
        mrepo = self.getManagedRepo(self.client)

        # Create a file in the repo
        base = self.uuid()
        path = base + "/testManagedRepo.txt"
        mrepo.makeDir(base, True)
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


class TestFileExists(AbstractRepoTest):
    # Note: difficult to test the case where the file exists
    # but is not in the database, since we cannot assume local
    # access to the FS. Long-term the only solution is likely
    # to launch our own FS instance locally.

    def testFileExistsForDirectory(self):
        mrepo = self.getManagedRepo(self.client)
        base = "./" + self.uuid()
        self.assertFalse(mrepo.fileExists(base))
        mrepo.makeDir(base, True)
        self.assertTrue(mrepo.fileExists(base))

    def testFileExistsForFile(self):
        mrepo = self.getManagedRepo(self.client)
        base = "./" + self.uuid()
        file = base + "/myfile.txt"
        self.assertFalse(mrepo.fileExists(file))
        mrepo.makeDir(base, True)
        self.assertFalse(mrepo.fileExists(file))
        rfs = mrepo.file(file, "rw")
        rfs.write("hi".encode("utf-8"), 0, 2)
        rfs.close()
        self.assertTrue(mrepo.fileExists(file))


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

    def assertRead(self, mrepo2, filename, ofile, ctx=None):
        def _read(rfs):
            try:
                self.assertEquals("hi", rfs.read(0, 2))
            finally:
                rfs.close()

        rfs = mrepo2.fileById(ofile.id.val, ctx)
        _read(rfs)

        rfs = mrepo2.file(filename, "r", ctx)
        _read(rfs)

    def assertListings(self, mrepo1, uuid):
        self.assertEquals(["/"+uuid], mrepo1.list("."))
        self.assertEquals(["/"+uuid+"/b"], mrepo1.list(uuid+"/"))
        self.assertEquals(["/"+uuid+"/b/c"], mrepo1.list(uuid+"/b/"))
        self.assertEquals(["/"+uuid+"/b/c/file.txt"],
                          mrepo1.list(uuid+"/b/c/"))

    def testTopPrivateGroup(self):

        filename = self.uuid() + ".txt"
        client1, mrepo1, client2, mrepo2 = self.setup2RepoUsers("rw----")

        # No intermediate directories
        ofile = self.createFile(mrepo1, filename)

        self.assertRead(mrepo1, filename, ofile)
        self.assertRead(mrepo1, filename, ofile, self.all(client1))
        self.assertWrite(mrepo1, filename, ofile)

        self.assertNoRead(mrepo2, filename, ofile)

        self.assertEquals(0, len(mrepo2.listFiles(".")))

    def testDirPrivateGroup(self):

        uuid = self.uuid()
        dirname = uuid + "/b/c"
        filename = dirname + "/file.txt"

        client1, mrepo1, client2, mrepo2 = self.setup2RepoUsers("rw----")

        mrepo1.makeDir(dirname, True)
        ofile = self.createFile(mrepo1, filename)

        self.assertRead(mrepo1, filename, ofile)
        self.assertRead(mrepo1, filename, ofile, self.all(client1))
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

        mrepo1.makeDir(dirname, True)
        ofile = self.createFile(mrepo1, filename)

        self.assertRead(mrepo1, filename, ofile)
        self.assertRead(mrepo1, filename, ofile, self.all(client1))
        self.assertListings(mrepo1, uuid)
        self.assertWrite(mrepo1, filename, ofile)

        self.assertRead(mrepo2, filename, ofile)
        self.assertRead(mrepo2, filename, ofile, self.all(client2))
        self.assertListings(mrepo2, uuid)
        self.assertNoWrite(mrepo2, filename, ofile)
        self.assertNoDirWrite(mrepo2, dirname)

    def testDirReadWriteGroup(self):

        uuid = self.uuid()
        dirname = uuid + "/b/c"
        filename = dirname + "/file.txt"

        client1, mrepo1, client2, mrepo2 = self.setup2RepoUsers("rwrw--")

        mrepo1.makeDir(dirname, True)
        ofile = self.createFile(mrepo1, filename)

        self.assertRead(mrepo1, filename, ofile)
        self.assertRead(mrepo1, filename, ofile, self.all(client1))
        self.assertListings(mrepo1, uuid)
        self.assertWrite(mrepo1, filename, ofile)

        self.assertRead(mrepo2, filename, ofile)
        self.assertRead(mrepo2, filename, ofile, self.all(client2))
        self.assertWrite(mrepo2, filename, ofile)
        self.assertListings(mrepo2, uuid)
        self.assertDirWrite(mrepo2, dirname)

    def testDirReadAnnotateGroup(self):

        uuid = self.uuid()
        dirname = uuid + "/b/c"
        filename = dirname + "/file.txt"

        client1, mrepo1, client2, mrepo2 = self.setup2RepoUsers("rwra--")

        mrepo1.makeDir(dirname, True)
        ofile = self.createFile(mrepo1, filename)

        self.assertRead(mrepo1, filename, ofile)
        self.assertRead(mrepo1, filename, ofile, self.all(client1))
        self.assertListings(mrepo1, uuid)
        self.assertWrite(mrepo1, filename, ofile)

        self.assertRead(mrepo2, filename, ofile)
        self.assertRead(mrepo2, filename, ofile, self.all(client2))
        self.assertListings(mrepo2, uuid)
        self.assertNoWrite(mrepo2, filename, ofile)
        self.assertDirWrite(mrepo2, dirname)

    def testMultiGroup(self):

        uuid = self.uuid()
        dirname = uuid + "/b/c"
        filename = dirname + "/file.txt"

        group1 = self.new_group(perms="rw----")
        client1, user = self.new_client_and_user(group=group1)
        client1.sf.setSecurityContext(group1)

        group2 = self.new_group(experimenters=[user])
        client2 = self.new_client(group=group2, user=user)
        client2.sf.setSecurityContext(group2)

        mrepo1 = self.getManagedRepo(client1)
        mrepo2 = self.getManagedRepo(client2)

        mrepo1.makeDir(dirname, True)
        ofile = self.createFile(mrepo1, filename)

        self.assertRead(mrepo1, filename, ofile)
        self.assertRead(mrepo1, filename, ofile, self.all(client1))

        try:
            self.assertRead(mrepo2, filename, ofile)
            self.fail("secvio")
        except omero.SecurityViolation:
            pass
        self.assertRead(mrepo2, filename, ofile, self.all(client2))


class TestPythonImporter(AbstractRepoTest):

    def create_test_dir(self):
        folder = create_path(folder=True)
        (folder / "a.fake").touch()
        (folder / "b.fake").touch()
        return folder

    def create_fileset(self, folder):
        fileset = omero.model.FilesetI()
        for f in folder.files():
            entry = omero.model.FilesetEntryI()
            entry.setClientPath(rstring(f.abspath()))
            fileset.addFilesetEntry(entry)

        # Fill BF info
        system, node, release, version, machine, processor = platform.uname()
        try:
            preferred_locale = locale.getdefaultlocale()[0]
        except:
            preferred_locale = "Unknown"

        clientVersionInfo = omero.model.FilesetVersionInfoI()
        clientVersionInfo.setBioformatsReader(rstring("DirectoryReader"))
        clientVersionInfo.setBioformatsVersion(rstring("Unknown"))
        clientVersionInfo.setOmeroVersion(rstring(omero_version));
        clientVersionInfo.setOsArchitecture(rstring(machine))
        clientVersionInfo.setOsName(rstring(system))
        clientVersionInfo.setOsVersion(rstring(release))
        clientVersionInfo.setLocale(rstring(preferred_locale))

        upload = omero.model.UploadJobI()
        upload.setVersionInfo(clientVersionInfo)
        fileset.linkJob(upload)
        return fileset

    def create_settings(self):
        settings = omero.grid.ImportSettings()
        settings.doThumbnails = rbool(True)
        settings.userSpecifiedTarget = None
        settings.userSpecifiedName = None
        settings.userSpecifiedDescription = None
        settings.userSpecifiedAnnotationList = None
        settings.userSpecifiedPixels = None
        return settings

    def upload_folder(self, proc, folder):
        ret_val = []
        for i, fobj in enumerate(folder.files()):  # Assuming same order
            rfs = proc.getUploader(i)
            try:
                f = fobj.open()
                try:
                    offset = 0
                    block = []
                    rfs.write(block, offset, len(block)) # Touch
                    while True:
                        block = f.read(1000*1000)
                        if not block:
                            break
                        rfs.write(block, offset, len(block))
                        offset += len(block)
                    ret_val.append(self.client.sha1(fobj.abspath()))
                finally:
                    f.close()
            finally:
                rfs.close()
        return ret_val

    def testImportFileset(self):
        client = self.new_client()
        mrepo = self.getManagedRepo(client)
        folder = self.create_test_dir()
        fileset = self.create_fileset(folder)
        settings = self.create_settings()

        proc = mrepo.importFileset(fileset, settings)
        self.assertImport(client, proc, folder)

    def testImportPaths(self):
        client = self.new_client()
        mrepo = self.getManagedRepo(client)
        folder = self.create_test_dir()
        paths = folder.files()

        proc = mrepo.importPaths(paths)
        self.assertImport(client, proc, folder)

    def assertImport(self, client, proc, folder):
        hashes = self.upload_folder(proc, folder)
        handle = proc.verifyUpload(hashes)
        cb = CmdCallbackI(client, handle)
        cb.loop(5, 500)
        rsp = cb.getResponse()
        if isinstance(rsp, ERR):
            self.fail(rsp)
        else:
            self.assertEquals(1, len(rsp.pixels))

if __name__ == '__main__':
    unittest.main()
