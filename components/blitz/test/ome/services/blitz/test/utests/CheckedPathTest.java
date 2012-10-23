package ome.services.blitz.test.utests;

import java.io.File;

import ome.services.blitz.repo.CheckedPath;
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

    @BeforeClass
    public void setup() throws Exception {
        this.dir = TempFileManager.create_path("repo", "test", true);
        this.root = new CheckedPath(null, this.dir.getAbsolutePath());
    }

    @Test
    public void testCtorWithRootPathPasses() throws Exception {
        CheckedPath cp = new CheckedPath(this.root, this.dir.getAbsolutePath());
        Assert.assertTrue(cp.isRoot);
    }

    @Test(expectedExceptions=ValidationException.class)
    public void testCtorWithPathAboveRootThrows() throws ValidationException {
        new CheckedPath(this.root, this.dir.getParent());
    }

    @Test
    public void testCtorWithPathBelowRootPasses() throws Exception {
        CheckedPath cp = new CheckedPath(this.root,
                new File(this.dir, "foo").getAbsolutePath());
        Assert.assertFalse(cp.isRoot);
    }

    @Test
    public void testMustExistPassesWithExistingFile() throws Exception {
        File f = new File(this.dir, "foo");
        FileUtils.touch(f);
        CheckedPath cp = new CheckedPath(this.root, f.getPath());
        Assert.assertEquals(cp.mustExist(), cp);
    }

    @Test(expectedExceptions=ValidationException.class)
    public void testMustExistThrowsWithNonexistingFile()
            throws ValidationException {
        new CheckedPath(this.root, "bar");
    }

}
