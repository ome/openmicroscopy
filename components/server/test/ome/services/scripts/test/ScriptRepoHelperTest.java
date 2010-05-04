/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.scripts.test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import ome.model.core.OriginalFile;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.scripts.ScriptRepoHelper;
import ome.system.Roles;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * ScriptRepoHelper is the logic used by the ScriptRepositoryI to register
 * official scripts. We're testing here to not need to deploy, have Ice running,
 * etc.
 *
 * Where possible, use of {@link FileUtils} and {@link FilenameUtils} should be
 * avoided in favor of having that logic in the helper itself.
 */
@Test(groups = "integration")
public class ScriptRepoHelperTest extends AbstractManagedContextTest {

    ScriptRepoHelper.RepoFile path;
    List<OriginalFile> files;
    ScriptRepoHelper helper;
    File dir;

    @BeforeMethod
    public void setup() throws Exception {
        mkdir();
        loginRoot();
        helper = new ScriptRepoHelper(uuid(), dir, this.executor,
                this.loginAop.p, new Roles());
        assertEmptyRepo();
    }

    @AfterMethod
    public void teardown() throws Exception {
        FileUtils.deleteDirectory(dir); // 1 of 2 uses of FileUtils
    }

    @AfterClass
    public void delete() throws Exception {
        FileUtils.deleteDirectory(dir.getParentFile()); // 2 of 2 uses of FileUtils
    }

    public void testLoadAddsObjects() throws Exception {
        path = generateFile();
        assertEquals(1, helper.countOnDisk());
        assertEquals(0, helper.countInDb());
        files = helper.loadAll(false);
        assertContains(helper.iterate(), path);
        assertEquals(1, files.size());
        assertEquals(1, helper.countInDb());
        assertEquals(path.rel,
                files.get(0).getPath() + files.get(0).getName());
    }

    public void testFindInDb() throws Exception {
        testLoadAddsObjects();
        assertEquals(files.get(0).getId(), helper.findInDb(path.rel, true));
        assertEquals(files.get(0).getId(), helper.findInDb(path.fs.path, false));
    }

    public void testFileModificationsAreFoundManually() throws Exception {
        testLoadAddsObjects();
        Long fileID = files.get(0).getId();
        helper.write(path.rel, "changed", true, false);
        files = helper.loadAll(false);
        assertEquals(fileID, files.get(0).getId());
        helper.modificationCheck();
        files = helper.loadAll(false);
        assertFalse(fileID.equals(files.get(0).getId()));
    }

    public void testFileModificationsCanBeFoundOnLoad() throws Exception {
        testLoadAddsObjects();
        Long fileID = files.get(0).getId();
        helper.write(path.rel, "changed", true, false);
        files = helper.loadAll(true);
        assertFalse(fileID.equals(files.get(0).getId()));
    }

    public void testHiddenFiles() throws Exception {
        path = generateFile(".test", ".py", "import omero");
        files = helper.loadAll(false);
        assertEmptyRepo();
    }

    public void testEmptyFiles() throws Exception {
        path = generateFile("test", ".py", "");
        files = helper.loadAll(false);
        assertEmptyRepo();
    }

    public void testTxtFiles() throws Exception {
        path = generateFile("test", ".txt", "import omero");
        files = helper.loadAll(false);
        assertEmptyRepo();
    }

    public void testFilesAreAddedToUserGroup() throws Exception {
        path = generateFile();
        files = helper.loadAll(false);
        assertEquals(Long.valueOf(new Roles().getUserGroupId()), files.get(0)
                .getDetails().getGroup().getId());
    }

    public void testReplacedFilesAreRemovedFromRepo() throws Exception {
        path = generateFile();
        files = helper.loadAll(false);
        Long oldID = files.get(0).getId();
        helper.write(path.rel, "updated", true, false);
        helper.modificationCheck();
        assertFalse(helper.isInRepo(oldID));

    }

    public void testFilesCanBeDeletedByRelativeValue() throws Exception {
        path = generateFile();
        files = helper.loadAll(false);
        Long id = files.get(0).getId();
        assertTrue(new File(path.fs.path).exists());
        helper.delete(id);
        assertFalse(helper.isInRepo(id));
        assertFalse(new File(path.fs.path).exists());
    }

    // Helpers
    // =========================================================================

    protected void assertEmptyRepo() {
        assertEquals(0, helper.countOnDisk());
        assertEquals(0, helper.countInDb());
        files = helper.loadAll(false);
        assertEquals(0, files.size());
    }

    protected ScriptRepoHelper.RepoFile generateFile() throws Exception {
        return generateFile("test", ".py", "import omero");
    }

    protected ScriptRepoHelper.RepoFile generateFile(String prefix, String suffix, String contents)
            throws Exception {
        File f = File.createTempFile(prefix, suffix, new File(helper
                .getScriptDir()));
        return helper.write(f.getAbsolutePath(), contents, false, false);
    }

    protected void assertContains(Iterator<File> it, ScriptRepoHelper.RepoFile repo) {
        boolean found = false;
        while (it.hasNext()) {
            File f = it.next();
            if (repo.matches(f)) {
                found = true;
            }
        }
        assertTrue(found);
    }

    protected void mkdir() throws IOException {
        File f = new File(".");
        File target = new File(f, "target");
        if (!target.exists()) {
            throw new RuntimeException("Can't find target");
        }

        File repos = new File(target, "repos");
        repos.mkdirs();

        dir = File.createTempFile("scripts_repo", "", repos);
        dir.delete();
        dir.mkdirs();
    }
}
