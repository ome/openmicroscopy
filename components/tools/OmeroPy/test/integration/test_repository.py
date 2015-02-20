#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test focused on the Repository API

   Copyright 2009-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import platform
import locale
import pytest
import library as lib
import omero

from omero import CmdError
from omero.model import NamedValue as NV
from omero.callbacks import CmdCallbackI
from omero.gateway import BlitzGateway
from omero.rtypes import rbool
from omero.rtypes import rstring
from omero.rtypes import unwrap
from omero.util.temp_files import create_path
from omero_version import omero_version


class AbstractRepoTest(lib.ITest):

    def setup_method(self, method):
        self.unique_dir = self.test_dir()

    def test_dir(self, client=None):
        if client is None:
            client = self.client
        mrepo = self.getManagedRepo(client=client)
        user_dir = self.user_dir(client=client)
        unique_dir = user_dir + "/" + self.uuid()  # ok
        mrepo.makeDir(unique_dir, True)
        return unique_dir

    def user_dir(self, client=None):
        if client is None:
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
        mrepo = client.getManagedRepository()
        assert mrepo
        return mrepo

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
                assert "bye" == rfs.read(0, 3)
                # Resetting for other expectations
                rfs.truncate(2)
                rfs.write("hi", 0, 2)
                assert "hi" == rfs.read(0, 2)
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
                pytest.raises(omero.SecurityViolation,
                              rfs.write, "bye", 0, 3)
                assert "hi" == rfs.read(0, 2)
            finally:
                rfs.close()

        rfs = mrepo2.fileById(ofile.id.val)
        _nowrite(rfs)

        rfs = mrepo2.file(filename, "r")
        _nowrite(rfs)

        # Can't even acquire a writeable-rfs.
        pytest.raises(omero.SecurityViolation,
                      mrepo2.file, filename, "rw")

    def assertDirWrite(self, mrepo2, dirname):
        self.createFile(mrepo2, dirname + "/file2.txt")

    def assertNoDirWrite(self, mrepo2, dirname):
        # Also check that it's not possible to write
        # in someone else's directory.
        pytest.raises(omero.SecurityViolation,
                      self.createFile, mrepo2, dirname + "/file2.txt")

    def assertNoRead(self, mrepo2, filename, ofile):
        pytest.raises(omero.SecurityViolation,
                      mrepo2.fileById, ofile.id.val)
        pytest.raises(omero.SecurityViolation,
                      mrepo2.file, filename, "r")

    def assertRead(self, mrepo2, filename, ofile, ctx=None):
        def _read(rfs):
            try:
                assert "hi" == rfs.read(0, 2)
            finally:
                rfs.close()

        rfs = mrepo2.fileById(ofile.id.val, ctx)
        _read(rfs)

        rfs = mrepo2.file(filename, "r", ctx)
        _read(rfs)

    def assertListings(self, mrepo1, unique_dir):
        assert [unique_dir + "/b"] == mrepo1.list(unique_dir + "/")
        assert [unique_dir + "/b/c"] == mrepo1.list(unique_dir + "/b/")
        assert [
            unique_dir + "/b/c/file.txt"] == mrepo1.list(unique_dir + "/b/c/")

    def raw(self, command, args, client=None):
        if client is None:
            client = self.client
        mrepo = self.getManagedRepo(self.client)
        obj = mrepo.root()
        sha = obj.hash.val
        raw_access = omero.grid.RawAccessRequest()
        raw_access.repoUuid = sha
        raw_access.command = command
        raw_access.args = args
        handle = client.sf.submit(raw_access)
        return CmdCallbackI(client, handle)

    def assertPasses(self, cb, loops=10, wait=500):
        cb.loop(loops, wait)
        rsp = cb.getResponse()
        if isinstance(rsp, omero.cmd.ERR):
            raise Exception(rsp)
        return rsp

    def assertError(self, cb, loops=10, wait=500):
        cb.loop(loops, wait)
        rsp = cb.getResponse()
        assert isinstance(rsp, omero.cmd.ERR)

    def create_test_dir(self):
        folder = create_path(folder=True)
        (folder / "a.fake").touch()
        (folder / "b.fake").touch()
        return folder

    def create_fileset(self, folder):
        fileset = omero.model.FilesetI()
        for f in folder.files():
            entry = omero.model.FilesetEntryI()
            entry.setClientPath(rstring(str(f.abspath())))
            fileset.addFilesetEntry(entry)

        # Fill version info
        system, node, release, version, machine, processor = platform.uname()

        clientVersionInfo = [
            NV('omero.version', omero_version),
            NV('os.name', system),
            NV('os.version', release),
            NV('os.architecture', machine)
        ]
        try:
            clientVersionInfo.append(
                NV('locale', locale.getdefaultlocale()[0]))
        except:
            pass

        upload = omero.model.UploadJobI()
        upload.setVersionInfo(clientVersionInfo)
        fileset.linkJob(upload)
        return fileset

    def create_settings(self):
        settings = omero.grid.ImportSettings()
        settings.doThumbnails = rbool(True)
        settings.noStatsInfo = rbool(False)
        settings.userSpecifiedTarget = None
        settings.userSpecifiedName = None
        settings.userSpecifiedDescription = None
        settings.userSpecifiedAnnotationList = None
        settings.userSpecifiedPixels = None
        settings.checksumAlgorithm = omero.model.ChecksumAlgorithmI()
        settings.checksumAlgorithm.value = omero.rtypes.rstring("SHA1-160")
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
                    rfs.write(block, offset, len(block))  # Touch
                    while True:
                        block = f.read(1000 * 1000)
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

    def fullImport(self, client):
        """
        Re-usable method for a basic import
        """
        mrepo = self.getManagedRepo(client)
        folder = self.create_test_dir()
        fileset = self.create_fileset(folder)
        settings = self.create_settings()

        proc = mrepo.importFileset(fileset, settings)
        try:
            return self.assertImport(client, proc, folder)
        finally:
            proc.close()

    def assertImport(self, client, proc, folder):
        hashes = self.upload_folder(proc, folder)
        handle = proc.verifyUpload(hashes)
        cb = CmdCallbackI(client, handle)
        rsp = self.assertPasses(cb)
        assert 1 == len(rsp.pixels)
        return rsp


class TestRepository(AbstractRepoTest):

    def testBasicUsage(self):

        test_file = __file__
        remote_base = "./%s/dir1" % self.unique_dir
        remote_file = "%s/test.dv" % remote_base

        repoMap = self.client.sf.sharedResources().repositories()
        assert len(repoMap.descriptions) > 1
        assert len(repoMap.proxies) > 1

        repoPrx = repoMap.proxies[0]
        assert repoPrx  # Could be None

        # This is a write-only (no read, no config)
        # version of this service.
        repoPrx.makeDir(remote_base, True)
        rawFileStore = repoPrx.file(remote_file, "rw")
        block_size = 1000 * 1000
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
        sha1_remote = ofile.hash.val
        sha1_local = self.client.sha1(test_file)
        assert sha1_remote == sha1_local

        # Pixels and Thumbs now requires a proper import.

    def testSanityCheckRepos(self):
        # Repos should behave sensibly when it comes
        # to listing their path and objects as well
        # as what items they return.
        repoMap = self.client.sf.sharedResources().repositories()
        for obj, prx in zip(repoMap.descriptions, repoMap.proxies):
            if prx:
                root = prx.root()
                assert ".omero" not in prx.list(".")
                for x in prx.listFiles("."):
                    assert ".omero" != x.name.val
                for x in ("id", "path", "name"):
                    a = getattr(obj, x)
                    b = getattr(root, x)
                    if a is None:
                        assert a == b
                    else:
                        assert a.val == b.val

    def testManagedRepoAsPubliRepo(self):
        mrepo = self.getManagedRepo(self.client)

        # Create a file in the repo
        base = self.unique_dir
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
        assert "hi" == unicode(rfs.read(0, 2), "utf-8")
        rfs.close()


class TestFileExists(AbstractRepoTest):
    # Note: difficult to test the case where the file exists
    # but is not in the database, since we cannot assume local
    # access to the FS. Long-term the only solution is likely
    # to launch our own FS instance locally.

    def testFileExistsForDirectory(self):
        mrepo = self.getManagedRepo(self.client)
        base = self.unique_dir + "/t"
        assert not mrepo.fileExists(base)
        mrepo.makeDir(base, True)
        assert mrepo.fileExists(base)

    def testFileExistsForFile(self):
        mrepo = self.getManagedRepo(self.client)
        base = self.unique_dir + "/t"
        file = base + "/myfile.txt"
        assert not mrepo.fileExists(file)
        mrepo.makeDir(base, True)
        assert not mrepo.fileExists(file)
        rfs = mrepo.file(file, "rw")
        rfs.write("hi".encode("utf-8"), 0, 2)
        rfs.close()
        assert mrepo.fileExists(file)


class TestManagedRepositoryMultiUser(AbstractRepoTest):

    class Fixture(object):

        def __init__(self, client, repo, testdir):
            self.client = client
            self.repo = repo
            self.testdir = testdir

    def setup2RepoUsers(self, perms="rw----"):
        group = self.new_group(perms=perms)
        client1, user1 = self.new_client_and_user(group=group)
        client2, user2 = self.new_client_and_user(group=group)

        mrepo1 = self.getManagedRepo(client1)
        mrepo2 = self.getManagedRepo(client2)

        testdir1 = self.test_dir(client1)
        testdir2 = self.test_dir(client2)

        return self.Fixture(client1, mrepo1, testdir1), \
            self.Fixture(client2, mrepo2, testdir2)

    def testTopPrivateGroup(self):
        f1, f2 = self.setup2RepoUsers("rw----")
        filename = f1.testdir + "/file.txt"

        # No intermediate directories
        ofile = self.createFile(f1.repo, filename)

        self.assertRead(f1.repo, filename, ofile)
        self.assertRead(f1.repo, filename, ofile, self.all(f1.client))
        self.assertWrite(f1.repo, filename, ofile)

        self.assertNoRead(f2.repo, filename, ofile)

        assert 0 == len(unwrap(f2.repo.treeList(".")))

    def testDirPrivateGroup(self):
        f1, f2 = self.setup2RepoUsers("rw----")
        dirname = f1.testdir + "/b/c"
        filename = dirname + "/file.txt"

        f1.repo.makeDir(dirname, True)
        ofile = self.createFile(f1.repo, filename)

        self.assertRead(f1.repo, filename, ofile)
        self.assertRead(f1.repo, filename, ofile, self.all(f1.client))
        self.assertListings(f1.repo, f1.testdir)
        self.assertWrite(f1.repo, filename, ofile)

        self.assertNoRead(f2.repo, filename, ofile)
        assert 0 == len(f2.repo.listFiles(dirname))

    def testDirReadOnlyGroup(self):
        f1, f2 = self.setup2RepoUsers("rwr---")
        dirname = f1.testdir + "/b/c"
        filename = dirname + "/file.txt"

        f1.repo.makeDir(dirname, True)
        ofile = self.createFile(f1.repo, filename)

        self.assertRead(f1.repo, filename, ofile)
        self.assertRead(f1.repo, filename, ofile, self.all(f1.client))
        self.assertListings(f1.repo, f1.testdir)
        self.assertWrite(f1.repo, filename, ofile)

        self.assertRead(f2.repo, filename, ofile)
        self.assertRead(f2.repo, filename, ofile, self.all(f2.client))
        self.assertListings(f2.repo, f1.testdir)
        self.assertNoWrite(f2.repo, filename, ofile)
        self.assertNoDirWrite(f2.repo, dirname)

    def testDirReadWriteGroup(self):
        f1, f2 = self.setup2RepoUsers("rwrw--")
        dirname = f1.testdir + "/b/c"
        filename = dirname + "/file.txt"

        f1.repo.makeDir(dirname, True)
        ofile = self.createFile(f1.repo, filename)

        self.assertRead(f1.repo, filename, ofile)
        self.assertRead(f1.repo, filename, ofile, self.all(f1.client))
        self.assertListings(f1.repo, f1.testdir)
        self.assertWrite(f1.repo, filename, ofile)

        self.assertRead(f2.repo, filename, ofile)
        self.assertRead(f2.repo, filename, ofile, self.all(f2.client))
        self.assertWrite(f2.repo, filename, ofile)
        self.assertListings(f2.repo, f1.testdir)
        self.assertDirWrite(f2.repo, dirname)

    def testDirReadAnnotateGroup(self):
        f1, f2 = self.setup2RepoUsers("rwra--")
        dirname = f1.testdir + "/b/c"
        filename = dirname + "/file.txt"

        f1.repo.makeDir(dirname, True)
        ofile = self.createFile(f1.repo, filename)

        self.assertRead(f1.repo, filename, ofile)
        self.assertRead(f1.repo, filename, ofile, self.all(f1.client))
        self.assertListings(f1.repo, f1.testdir)
        self.assertWrite(f1.repo, filename, ofile)

        self.assertRead(f2.repo, filename, ofile)
        self.assertRead(f2.repo, filename, ofile, self.all(f2.client))
        self.assertListings(f2.repo, f1.testdir)
        self.assertNoWrite(f2.repo, filename, ofile)
        self.assertDirWrite(f2.repo, dirname)

    def testMultiGroup(self):
        group1 = self.new_group(perms="rw----")
        client1, user = self.new_client_and_user(group=group1)
        client1.sf.setSecurityContext(group1)
        testdir1 = self.test_dir(client1)
        dirname = testdir1 + "/b/c"
        filename = dirname + "/file.txt"

        group2 = self.new_group(experimenters=[user])
        client2 = self.new_client(group=group2, user=user)
        client2.sf.setSecurityContext(group2)

        mrepo1 = self.getManagedRepo(client1)
        mrepo2 = self.getManagedRepo(client2)

        mrepo1.makeDir(dirname, True)
        ofile = self.createFile(mrepo1, filename)

        self.assertRead(mrepo1, filename, ofile)
        self.assertRead(mrepo1, filename, ofile, self.all(client1))

        with pytest.raises(omero.SecurityViolation):
            self.assertRead(mrepo2, filename, ofile)

        self.assertRead(mrepo2, filename, ofile, self.all(client2))


class TestPythonImporter(AbstractRepoTest):

    def testImportFileset(self):
        client = self.new_client()
        self.fullImport(client)

    # Tests the alternative importPaths method
    def testImportPaths(self):
        client = self.new_client()
        mrepo = self.getManagedRepo(client)
        folder = self.create_test_dir()
        paths = folder.files()

        proc = mrepo.importPaths(paths)
        self.assertImport(client, proc, folder)

    def testReopenRawFileStoresPR2542(self):
        client = self.new_client()
        mrepo = self.getManagedRepo(client)
        folder = self.create_test_dir()
        paths = folder.files()

        proc = mrepo.importPaths(paths)
        for idx in range(len(paths)):
            proc.getUploader(idx).close()
        # Import should continue to work after
        # closing the resources
        self.assertImport(client, proc, folder)

    # Assure that the template functionality supports the same user
    # importing from multiple groups on a given day
    def testImportsFrom2Groups(self):
        group1 = self.new_group(perms="rw----")
        client, user = self.new_client_and_user(group=group1)
        group2 = self.new_group(perms="rw----",
                                experimenters=[user])
        admin = client.sf.getAdminService()
        # from group1
        assert group1.id.val == admin.getEventContext().groupId
        self.fullImport(client)

        # then group 2
        client.sf.setSecurityContext(group2)
        self.fullImport(client)


class TestRawAccess(AbstractRepoTest):

    def testAsNonAdmin(self):
        uuid = self.unique_dir + "./t.txt"
        cb = self.raw("touch", [uuid])
        self.assertError(cb)

    def testAsAdmin(self):
        uuid = self.unique_dir + "/t.txt"
        cb = self.raw("touch", [uuid])
        self.assertError(cb)


class TestDbSync(AbstractRepoTest):

    def testMtime(self):
        filename = self.unique_dir + "/file.txt"
        mrepo = self.getManagedRepo()

        ofile = self.createFile(mrepo, filename)
        assert ofile.mtime is not None

    def testFileExists(self):
        filename = self.unique_dir + "/file.txt"
        mrepo = self.getManagedRepo()

        assert not mrepo.fileExists(filename)
        self.createFile(mrepo, filename)
        assert mrepo.fileExists(filename)
        assert "file.txt" in mrepo.list(self.unique_dir)[0]

    def testNonDbFileNotReturned(self):
        filename = self.unique_dir + "/file.txt"
        fooname = self.unique_dir + "/foo.txt"
        mydir = self.unique_dir + "/mydir"
        mrepo = self.getManagedRepo()

        self.createFile(mrepo, filename)

        # foo.txt is created on the backend but doesn't show up.
        assert ['%s/file.txt' % self.unique_dir] == mrepo.list(self.unique_dir)
        self.assertPasses(self.raw("touch", [fooname], client=self.root))
        assert ['%s/file.txt' % self.unique_dir] == mrepo.list(self.unique_dir)

        # If we try to create such a file, we should receive an exception
        try:
            self.createFile(mrepo, fooname)
            assert False, "Should have thrown"
        except omero.grid.UnregisteredFileException, ufe:
            file = mrepo.register(fooname, None)
            assert file.path == ufe.file.path
            assert file.name == ufe.file.name
            assert file.size == ufe.file.size

        # And if the file is a Dir, we should have a mimetype
        self.assertPasses(self.raw("mkdir", ["-p", mydir], client=self.root))
        try:
            self.createFile(mrepo, mydir)
            assert False, "Should have thrown"
        except omero.grid.UnregisteredFileException, ufe:
            file = mrepo.register(mydir, None)
            assert file.mimetype == ufe.file.mimetype


class TestRecursiveDelete(AbstractRepoTest):

    def setup_method(self, method):
        super(TestRecursiveDelete, self).setup_method(method)
        self.filename = self.unique_dir + "/file.txt"
        self.mrepo = self.getManagedRepo()
        self.ofile = self.createFile(self.mrepo, self.filename)

        # There should me one key in each of the files
        # NB: globs not currently supported.
        self.file_map1 = unwrap(self.mrepo.treeList(self.filename))
        assert 1 == len(self.file_map1)
        self.dir_map = unwrap(self.mrepo.treeList(self.unique_dir))
        assert 1 == len(self.dir_map)
        self.dir_key = self.dir_map.keys()[0]
        self.file_map2 = self.dir_map[self.dir_key]["files"]["file.txt"]

    # treeList is a utility that is useful for
    # testing recursive deletes.
        for self.file_map in (self.file_map1["file.txt"], self.file_map2):
            assert self.ofile.id.val == self.file_map["id"]
            assert self.ofile.size.val == self.file_map["size"]

    # In order to prevent dangling files now that
    # the repository uses the DB strictly for all
    # FS listings, it's necessary to prevent any
    # directories from being directly deleted.
    def testCmdDeleteCantDeleteDirectories(self):
        id = self.dir_map[self.dir_key]["id"]

        gateway = BlitzGateway(client_obj=self.client)
        handle = gateway.deleteObjects("/OriginalFile", [id])
        try:
            with pytest.raises(CmdError):
                gateway._waitOnCmd(handle, failonerror=True)
        finally:
            handle.close()

    # On the other hand, the repository itself can
    # provide a method which enables recursive delete.
    def testRecursiveDeleteMethodAvailable(self):
        handle = self.mrepo.deletePaths([self.unique_dir], True, True)
        self.waitOnCmd(self.client, handle, passes=True)
        rv = unwrap(self.mrepo.treeList(self.unique_dir))
        assert 0 == len(rv)

    # Trying to get up and out of the current directory
    # to delete more. Muahahaha...
    def testDoubleDot(self):
        naughty = self.unique_dir + "/" + ".." + "/" + ".." + "/" + ".."
        pytest.raises(omero.ValidationException,
                      self.mrepo.deletePaths, [naughty], True, True)


class TestDeleteLog(AbstractRepoTest):

    def testSimpleDelete(self):
        filename = self.unique_dir + "/file.txt"
        mrepo = self.getManagedRepo()
        ofile = self.createFile(mrepo, filename)
        gateway = BlitzGateway(client_obj=self.client)

        # Assert contents of file
        rfs = mrepo.fileById(ofile.id.val)
        try:
            assert "hi" == rfs.read(0, 2)
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
            assert "\x00\x00" == rfs.read(0, 2)
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
        uuid = self.uuid()  # ok
        mrepo = self.getManagedRepo()
        pytest.raises(omero.ValidationException, mrepo.makeDir, uuid, True)

    def testCreateUserDirPasses(self):
        mrepo = self.getManagedRepo()
        user_dir = self.user_dir()
        mrepo.makeDir(user_dir, True)

    def testCreateUuidUnderUserDirPasses(self):
        mrepo = self.getManagedRepo()
        user_dir = self.user_dir()
        uuid = self.uuid()  # ok
        mydir = "%s/%s" % (user_dir, uuid)
        mrepo.makeDir(mydir, True)

    # If a user should be able to create a file
    # under her/his own directory regardless of
    # which group s/he is in.
    def testUserDirShouldBeGloballyWriteable(self):
        mrepo = self.getManagedRepo()
        user_dir = self.user_dir()
        aDir = user_dir + "/" + self.uuid()  # ok
        aFile = aDir + "/a.txt"

        # Create a first file in one group
        mrepo.makeDir(aDir, True)
        self.createFile(mrepo, aFile)

        uid = self.client.sf.getAdminService().getEventContext().userId
        users = [omero.model.ExperimenterI(uid, False)]
        group = self.new_group(experimenters=users)
        self.client.sf.getAdminService().getEventContext()  # Refresh
        self.set_context(self.client, group.id.val)

        # Now write a second file from another group
        bDir = user_dir + "/" + self.uuid()  # ok
        bFile = bDir + "/b.txt"
        mrepo.makeDir(bDir, True)
        self.createFile(mrepo, bFile)


class TestFilesetQueries(AbstractRepoTest):

    def project(self, *args, **kwargs):
        self.client.sf.getQueryService().projection(*args, **kwargs)

    def testDeleteQuery(self):
        query = "select fs from Fileset fs "\
                "left outer join fetch fs.images as image "\
                "where image.id in (:imageIds)"
        params = omero.sys.Parameters()
        params.map = {'imageIds': omero.rtypes.wrap([omero.rtypes.rlong(-1)])}
        self.project(query, params)

    def testCountFilesetFiles(self):
        params = omero.sys.Parameters()
        params.map = {'imageId': omero.rtypes.rlong(-1)}
        query = "select count(fse.id) from FilesetEntry as fse join fse.fileset as fs "\
                "left outer join fs.images as image where image.id=:imageId"
        self.project(query, params)

    def testImportedImageFiles(self):
        params = omero.sys.Parameters()
        params.map = {'imageId': omero.rtypes.rlong(-1)}
        query = "select fs from Fileset as fs "\
                "left outer join fetch fs.images as image "\
                "left outer join fetch fs.usedFiles as usedFile " \
                "join fetch usedFile.originalFile where image.id=:imageId"
        self.project(query, params)


class TestOriginalMetadata(AbstractRepoTest):

    def testFakeImport(self):

        # TODO: should likely be in the "fs" namespace
        req = omero.cmd.OriginalMetadataRequest()

        client = self.new_client()
        rsp = self.fullImport(client)  # Note: fake test produces no metadata!
        image = rsp.objects[0]

        req.imageId = image.id.val

        gateway = BlitzGateway(client_obj=client)

        # Load via the gateway
        image = gateway.getObject("Image", image.id.val)
        assert 3 == len(image.loadOriginalMetadata())

        # Load via raw request
        handle = client.sf.submit(req)
        try:
            gateway._waitOnCmd(handle, failonerror=True)
            rsp = handle.getResponse()
            assert dict == type(rsp.globalMetadata)
            assert dict == type(rsp.seriesMetadata)
        finally:
            handle.close()


class TestDeletePerformance(AbstractRepoTest):

    def testImport(self):
        import time
        s1 = time.time()
        client = self.new_client()
        mrepo = self.getManagedRepo(client)
        folder = create_path(folder=True)
        for x in range(200):
            name = "%s.unknown" % x
            (folder / name).touch()
        paths = folder.files()
        proc = mrepo.importPaths(paths)
        hashes = self.upload_folder(proc, folder)
        handle = proc.verifyUpload(hashes)
        req = handle.getRequest()
        fs = req.activity.getParent()
        cb = CmdCallbackI(client, handle)
        self.assertError(cb, loops=200)

        delete = omero.cmd.Delete()
        delete.type = "/Fileset"
        delete.id = fs.id.val
        s2 = time.time()
        print s2 - s1,
        t1 = time.time()
        client.submit(delete, loops=200)
        t2 = time.time()
        print " ", t2-t1,
