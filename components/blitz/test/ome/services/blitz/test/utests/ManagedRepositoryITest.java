package ome.services.blitz.test.utests;

import java.io.File;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import junit.framework.Assert;
import ome.services.blitz.fire.Registry;
import ome.services.blitz.repo.ManagedRepositoryI;
import ome.services.util.Executor;
import ome.system.Principal;
import omero.grid.Import;
import omero.util.TempFileManager;

import org.apache.commons.io.FileUtils;
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
        public Import suggestOnConflict(String trueRoot, String relPath,
                String basePath, List<String> paths) {
            return super.suggestOnConflict(trueRoot, relPath, basePath, paths);
        }

        @Override
        public String commonRoot(List<String> paths) {
            return super.commonRoot(paths);
        }

        @Override
        public String[] splitLastElement(String path) {
            return super.splitLastElement(path);
        }

        public String getStringFromToken(String token, Calendar now,
                DateFormatSymbols dfs) {
            return super.getStringFromToken(token, now, dfs);
        }

    }

    @BeforeClass
    public void setup() throws Exception {
        this.tmpDir = TempFileManager.create_path("repo", "test", true);
        Mock mockReg = mock(Registry.class);
        this.reg = (Registry) mockReg.proxy();
        this.tmri = new TestManagedRepositoryI("/%year%/%month%/%day%", null,
                null, this.reg);
    }

    private String getSuggestion(String base, String...paths) {
        Import i = this.tmri.suggestOnConflict(this.tmpDir.getAbsolutePath(),
                "template", base, Arrays.asList(paths));
        return new File(i.sharedPath).getName();
    }

    @Test
    public void testSuggestOnConflictPassesWithNonconflictingPaths() {
        new File(this.tmpDir, "/my/path");
        String expectedBasePath = "path";
        String suggestedBasePath = getSuggestion("/my/path", "/my/path/foo", "/my/path/bar");

        Assert.assertEquals(expectedBasePath, suggestedBasePath);
    }

    @Test
    public void testSuggestOnConflictReturnsNewPathOnConflict() throws IOException {
        File upload = new File(this.tmpDir, "/upload");
        upload.mkdirs();
        FileUtils.touch(new File(upload, "foo"));
        String expectedBasePath = "upload-1";
        String suggestedBasePath = getSuggestion("/upload", "/upload/foo", "/upload/bar");
        Assert.assertEquals(expectedBasePath, suggestedBasePath);
    }

    @Test
    public void testSuggestOnConflictReturnsBasnePathWithEmptyPathsList() {
        String expectedBasePath = "upload";
        String suggestedBasePath = getSuggestion("/upload");
        Assert.assertEquals(expectedBasePath, suggestedBasePath);
    }

    @Test
    public void testCommonRootReturns() {
        String expectedCommonRoot = "/home";
        String actualCommonRoot = this.tmri.commonRoot(Arrays.asList("/home/bob/myStuff",
                "/home/alice/myOtherStuff"));
        Assert.assertEquals(expectedCommonRoot, actualCommonRoot);
    }

    @Test
    public void testCommonRootReturnsTopLevelWithUncommonPaths() {
        String expectedCommonRoot = "/";
        String actualCommonRoot = this.tmri.commonRoot(Arrays.asList("/home/bob/myStuff",
                "/data/alice"));
        Assert.assertEquals(expectedCommonRoot, actualCommonRoot);
    }

    @Test
    public void testSplitLastElementSlash() throws Exception {
        String[] rv = this.tmri.splitLastElement("/");
        assertEquals(2, rv.length);
        assertEquals("/", rv[0]);
        assertEquals("/", rv[1]);
    }

    @Test
    public void testSplitLastElementEmpty() throws Exception {
        String[] rv = this.tmri.splitLastElement("");
        assertEquals(2, rv.length);
        assertEquals(".", rv[0]);
        assertEquals("", rv[1]);
    }

    @Test
    public void testSplitLastElementBlank() throws Exception {
        String[] rv = this.tmri.splitLastElement(" ");
        assertEquals(2, rv.length);
        assertEquals(".", rv[0]);
        assertEquals(" ", rv[1]);
    }

    @Test
    public void testSplitLastElementAppendsRelNoFinalSeparator() throws Exception {
        String[] rv = this.tmri.splitLastElement("a/b");
        assertEquals(2, rv.length);
        assertEquals("a", rv[0]);
        assertEquals("b", rv[1]);
    }

    @Test
    public void testSplitLastElementAppendsFinalRelSeparator() throws Exception {
        String[] rv = this.tmri.splitLastElement("a/b/");
        assertEquals(2, rv.length);
        assertEquals("a", rv[0]);
        assertEquals("b", rv[1]);
    }

    @Test
    public void testSplitLastElementAppendsFinalSeparatorRelThree() throws Exception {
        String[] rv = this.tmri.splitLastElement("a/b/c");
        assertEquals(2, rv.length);
        assertEquals("a/b", rv[0]);
        assertEquals("c", rv[1]);
    }

    @Test
    public void testSplitLastElementAppendsAbsNoFinalSeparator() throws Exception {
        String[] rv = this.tmri.splitLastElement("/a/b");
        assertEquals(2, rv.length);
        assertEquals("/a", rv[0]);
        assertEquals("b", rv[1]);
    }

    @Test
    public void testSplitLastElementAppendsFinalAbsSeparator() throws Exception {
        String[] rv = this.tmri.splitLastElement("/a/b/");
        assertEquals(2, rv.length);
        assertEquals("/a", rv[0]);
        assertEquals("b", rv[1]);
    }

    @Test
    public void testSplitLastElementAppendsFinalSeparatorAbsThree() throws Exception {
        String[] rv = this.tmri.splitLastElement("/a/b/c");
        assertEquals(2, rv.length);
        assertEquals("/a/b", rv[0]);
        assertEquals("c", rv[1]);
    }

    public void testGetStringFromTokenReturnsEmptyStringOnNullToken() {
        String actual = this.tmri.getStringFromToken("", Calendar.getInstance(),
                DateFormatSymbols.getInstance());
        Assert.assertTrue(actual.isEmpty());
    }

    @Test
    public void testGetStringFromTokenReturnsTokenOnMalformedToken() {
        String expected = "foo";
        String actual = this.tmri.getStringFromToken(expected, Calendar.getInstance(),
                DateFormatSymbols.getInstance());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetStringFromTokenReturnsYear() {
        String expected = "2012";
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2012);
        String actual = this.tmri.getStringFromToken("%year%", cal,
                DateFormatSymbols.getInstance());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetStringFromTokenReturnsMonth() {
        String expected = "10";
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Integer.parseInt(expected) - 1);
        String actual = this.tmri.getStringFromToken("%month%", cal,
                DateFormatSymbols.getInstance());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetStringFromTokenReturnsMonthName() {
        String expected = "October";
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, 9);
        String actual = this.tmri.getStringFromToken("%monthname%", cal,
                DateFormatSymbols.getInstance());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetStringFromTokenReturnsDay() {
        String expected = "7";
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 7);
        String actual = this.tmri.getStringFromToken("%day%", cal,
                DateFormatSymbols.getInstance());
        Assert.assertEquals(expected, actual);
    }
}
