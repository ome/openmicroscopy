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
from omero.gateway import BlitzGateway
from omero.rtypes import rbool
from omero.rtypes import rstring
from omero.rtypes import unwrap
from omero.util.temp_files import create_path
from omero_version import omero_version


class AbstractRepoTest(lib.ITest):

    def setUp(self):
        super(AbstractRepoTest, self).setUp()
        mrepo = self.getManagedRepo()
        user_dir = self.user_dir()
        unique_dir = user_dir + "/" + self.uuid()
        mrepo.makeDir(unique_dir, True)
        self.unique_dir = unique_dir

    def user_dir(self, client=None):
        if client == None:
            client = self.client
        ec = client.sf.getAdminService().getEventContext()
        return "%s_%s" % (ec.userName, ec.userId)

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


    def raw(self, command, args, client=None):
        if client == None:
            client = self.client
        mrepo = self.getManagedRepo(self.client)
        obj = mrepo.root()
        sha = obj.sha1.val
        raw_access = omero.grid.RawAccessRequest()
        raw_access.repoUuid = sha
        raw_access.command = command
        raw_access.args = args
        handle = client.sf.submit(raw_access)
        return CmdCallbackI(client, handle)

    def assertPasses(self, cb, loops=10, wait=500):
        cb.loop(loops, wait)
        rsp = cb.getResponse()
        if not isinstance(rsp, omero.cmd.OK):
            raise Exception(rsp)
        return rsp

    def assertError(self, cb, loops=10, wait=500):
        cb.loop(loops, wait)
        rsp = cb.getResponse()
        self.assertTrue(isinstance(rsp, omero.cmd.ERR))


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

class TestRawAccess(AbstractRepoTest):

    def testAsNonAdmin(self):
        uuid = self.uuid()
        cb = self.raw("touch", ["./"+uuid])
        self.assertError(cb)

    def testAsAdmin(self):
        uuid = self.uuid()
        cb = self.raw("touch", ["./"+uuid])
        self.assertError(cb)

class TestDbSync(AbstractRepoTest):

    def testMtime(self):
        uuid = self.uuid()
        filename = uuid + "/file.txt"
        mrepo = self.getManagedRepo()

        mrepo.makeDir(uuid, True)
        ofile = self.createFile(mrepo, filename)
        self.assertTrue(ofile.mtime is not None)

    def testFileExists(self):
        uuid = self.uuid()
        filename = uuid + "/file.txt"
        mrepo = self.getManagedRepo()

        mrepo.makeDir(uuid, True)
        self.assertFalse(mrepo.fileExists(filename))

        self.createFile(mrepo, filename)
        self.assertTrue(mrepo.fileExists(filename))
        self.assertTrue("file.txt" in mrepo.list(uuid)[0])

    def testNonDbFileNotReturned(self):
        uuid = self.uuid()
        filename = uuid + "/file.txt"
        fooname = uuid + "/foo.txt"
        mydir = uuid + "/mydir"
        mrepo = self.getManagedRepo()

        mrepo.makeDir(uuid, True)
        ofile = self.createFile(mrepo, filename)

        # foo.txt is created on the backend but doesn't show up.
        self.assertEquals(['/%s/file.txt' % uuid], mrepo.list("./"+uuid))
        self.assertPasses(self.raw("touch", [fooname], client=self.root))
        self.assertEquals(['/%s/file.txt' % uuid], mrepo.list("./"+uuid))

        # If we try to create such a file, we should receive an exception
        try:
            self.createFile(mrepo, fooname)
            self.fail("Should have thrown")
        except omero.grid.UnregisteredFileException, ufe:
            file = mrepo.register(fooname, None)
            self.assertEquals(file.path, ufe.file.path)
            self.assertEquals(file.name, ufe.file.name)
            self.assertEquals(file.size, ufe.file.size)

        # And if the file is a Dir, we should have a mimetype
        self.assertPasses(self.raw("mkdir", ["-p", mydir], client=self.root))
        try:
            self.createFile(mrepo, mydir)
            self.fail("Should have thrown")
        except omero.grid.UnregisteredFileException, ufe:
            file = mrepo.register(mydir, None)
            self.assertEquals(file.mimetype, ufe.file.mimetype)


class TestRecursiveDelete(AbstractRepoTest):

    # treeList is a utility that is useful for
    # testing recursive deletes.
    def testTreeList(self):
        filename = self.unique_dir + "/file.txt"
        mrepo = self.getManagedRepo()
        ofile = self.createFile(mrepo, filename)

        # There should me one key in each of the files
        # NB: globs not currently supported.
        file_map1 = unwrap(mrepo.treeList(filename))
        self.assertEquals(1, len(file_map1))
        dir_map = unwrap(mrepo.treeList(self.unique_dir))
        self.assertEquals(1, len(dir_map))
        dir_key = dir_map.keys()[0]

        file_map2 = dir_map[dir_key]["files"]["file.txt"]

        for file_map in (file_map1["file.txt"], file_map2):
            self.assertEquals(ofile.id.val, file_map["id"], msg=str(file_map))
            self.assertEquals(ofile.size.val, file_map["size"], msg=str(file_map))

    # In order to prevent dangling files now that
    # the repository uses the DB strictly for all
    # FS listings, it's necessary to prevent any
    # directories from being directly deleted.
    def testCmdDeleteCantDeleteDirectories(self):
        mrepo = self.getManagedRepo()
        dir_map = unwrap(mrepo.treeList(self.unique_dir))
        self.assertEquals(1, len(dir_map))
        dir_key = dir_map.keys()[0]
        id = dir_map[dir_key]["id"]

        gateway = BlitzGateway(client_obj=self.client)
        handle = gateway.deleteObjects("/OriginalFile", [id])
        try:
            self.assertRaises(Exception,
                    gateway._waitOnCmd, handle, failonerror=True)
        finally:
            handle.close()

    # On the other hand, the repository itself can
    # provide a method which enables recursive delete.
    def testRecursiveDeleteMethodAvailable(self):
        pass


class TestDeleteLog(AbstractRepoTest):

    def testSimpleDelete(self):
        uuid = self.uuid()
        filename = uuid + "/file.txt"
        mrepo = self.getManagedRepo()
        mrepo.makeDir(uuid, True)
        ofile = self.createFile(mrepo, filename)
        gateway = BlitzGateway(client_obj=self.client)

        # Assert contents of file
        rfs = mrepo.fileById(ofile.id.val)
        try:
            self.assertEquals("hi", rfs.read(0, 2))
        finally:
            rfs.close()

        handle = gateway.deleteObjects("/OriginalFile", [ofile.id.val])
        try:
            gateway._waitOnCmd(handle)
        finally:
            handle.close()

        # Trying to open the file should not throw an UnregisteredFileException
        # But should just be an empty file.
        rfs = mrepo.file(filename, "rw")
        try:
            self.assertEquals("\x00\x00", rfs.read(0, 2))
        finally:
            rfs.close()


class TestUserTemplate(AbstractRepoTest):
    """
    The top-level directories directory under the root
    of a managed repository are intended solely for
    individual user use. In other words, creating a directory
    at the top is only possible if it is your user directory.
    """

    def testCreateUuidFails(self):
        uuid = self.uuid()
        mrepo = self.getManagedRepo()
        self.assertRaises(omero.ValidationException, mrepo.makeDir, uuid, True)

    def testCreateUserDirPasses(self):
        mrepo = self.getManagedRepo()
        user_dir = self.user_dir()
        mrepo.makeDir(user_dir, True)

    def testCreateUuidUnderUserDirPasses(self):
        mrepo = self.getManagedRepo()
        user_dir = self.user_dir()
        uuid = self.uuid()
        mydir = "%s/%s" % (user_dir, uuid)
        mrepo.makeDir(mydir, True)

    # If a user should be able to create a file
    # under her/his own directory regardless of
    # which group s/he is in.
    def testUserDirShouldBeGloballyWriteable(self):
        mrepo = self.getManagedRepo()
        user_dir = self.user_dir()
        aDir = user_dir + "/" + self.uuid()
        aFile = aDir + "/a.txt"

        # Create a first file in one group
        mrepo.makeDir(aDir, True)
        self.createFile(mrepo, aFile)

        uid = self.client.sf.getAdminService().getEventContext().userId
        users = [omero.model.ExperimenterI(uid, False)]
        group = self.new_group(experimenters=users)
        self.client.sf.getAdminService().getEventContext() # Refresh
        self.set_context(self.client, group.id.val)

        # Now write a second file from another group
        bDir = user_dir + "/" + self.uuid()
        bFile = bDir + "/b.txt"
        mrepo.makeDir(bDir, True)
        self.createFile(mrepo, bFile)



if __name__ == '__main__':
    unittest.main()
