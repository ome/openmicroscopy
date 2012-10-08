package ome.services.blitz.test.utests;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;
import ome.services.blitz.fire.Registry;
import ome.services.blitz.repo.ManagedRepositoryI;
import ome.services.util.Executor;
import ome.system.Principal;
import omero.util.TempFileManager;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ManagedRepositoryITest extends MockObjectTestCase {

    File tmpDir;
    TestManagedRepositoryI tmri;
    Registry reg;

    public class TestManagedRepositoryI extends ManagedRepositoryI {

        public TestManagedRepositoryI(String template, Executor executor,
                Principal principal, Registry reg) throws Exception {
            super(template, executor, principal, reg);
        }

        @Override
        public String suggestOnConflict(String trueRoot, String relPath,
                String basePath, List<String> paths) {
            return super.suggestOnConflict(trueRoot, relPath, basePath, paths);
        }

    }

    // TODO: Create registry mock and let the test fail. Then fix by adding
    // mock methods.
    // seealso: CheckedPath test, this one might look similar

    @BeforeClass
    public void setup() throws Exception {
        this.tmpDir = TempFileManager.create_path("repo", "test", true);
        Mock mockReg = mock(Registry.class);
        this.reg = (Registry) mockReg.proxy();
        this.tmri = new TestManagedRepositoryI("/%year%/%month%/%day%", null,
                null, this.reg);
    }

    @Test
    public void testSuggestOnConflictPassesWithNonconflictingPaths() {
        File realPath = new File(this.tmpDir, "/my/path");
        String expectedBasePath = "path";
        String suggestedBasePath = this.tmri.suggestOnConflict(realPath.getAbsolutePath(),
                null, "/my/path", Arrays.asList("/my/path/foo", "/my/path/bar"));
        Assert.assertEquals(expectedBasePath, suggestedBasePath);
    }

    @Test
    public void testSuggestOnConflictReturnsNewPathOnConflict() {
        fail();
    }

}
