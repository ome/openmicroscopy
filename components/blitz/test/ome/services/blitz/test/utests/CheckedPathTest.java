package ome.services.blitz.test.utests;

import java.io.File;

import ome.services.blitz.repo.CheckedPath;
import ome.services.blitz.repo.path.FilePathTransformerOnServer;
import ome.services.blitz.repo.path.MakePathComponentSafe;
import omero.ValidationException;
import omero.util.TempFileManager;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class CheckedPathTest {

    File dir;
    CheckedPath root;
    FilePathTransformerOnServer serverPaths;

    @BeforeClass
    public void setup() throws Exception {
        this.dir = TempFileManager.create_path("repo", "test", true);
        this.serverPaths = new FilePathTransformerOnServer();
        this.serverPaths.setBaseDirFile(this.dir);
        this.serverPaths.setPathSanitizer(new MakePathComponentSafe());
    }

    @Test
    public void testCtorWithRootPathPasses() throws Exception {
        CheckedPath cp = new CheckedPath(this.serverPaths, "");
        Assert.assertTrue(cp.isRoot);
    }

    @Test(expectedExceptions=ValidationException.class)
    public void testCtorWithPathAboveRootThrows() throws ValidationException {
        new CheckedPath(this.serverPaths, "..");
    }

    @Test
    public void testCtorWithPathBelowRootPasses() throws Exception {
        CheckedPath cp = new CheckedPath(this.serverPaths, "foo");
        Assert.assertFalse(cp.isRoot);
    }

    @Test
    public void testMustExistPassesWithExistingFile() throws Exception {
        File f = new File(this.dir, "foo");
        FileUtils.touch(f);
        CheckedPath cp = new CheckedPath(this.serverPaths, f.getName());
        Assert.assertEquals(cp.mustExist(), cp);
    }

    @Test(expectedExceptions=ValidationException.class)
    public void testMustExistThrowsWithNonexistingFile()
            throws ValidationException {
        new CheckedPath(this.serverPaths, "bar");
    }
}
