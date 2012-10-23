package ome.services.blitz.test.utests;

import java.io.File;
import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ome.services.blitz.fire.Registry;
import ome.services.blitz.repo.ManagedRepositoryI;
import ome.services.blitz.repo.RepositoryDao;

import omero.grid.Import;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.sys.EventContext;
import omero.util.TempFileManager;

@Test(groups = {"fs"})
public class ManagedRepositoryITest extends MockObjectTestCase {

    Mock daoMock;

    /**
     * The temporary directory which is equivalent to /OMERO/ManagedRepository
     */
    File tmpDir;

    /**
     * The "expanded" template directory which here is mocked to simply
     * "template". This should be used when touch()-ing files under
     * tmpDir.
     */
    File templateDir;

    TestManagedRepositoryI tmri;
    Registry reg;
    Ice.Current curr;

    /**
     * Overrides protected methods from parent class for testing
     *
     * @author bpindelski
     */
    public class TestManagedRepositoryI extends ManagedRepositoryI {

        public TestManagedRepositoryI(String template,
                RepositoryDao repositoryDao, Registry reg) throws Exception {
            super(template, repositoryDao, reg);
        }

        @Override
        public Import suggestOnConflict(String trueRoot, String relPath,
                String basePath, List<String> paths, Ice.Current curr) throws omero.ApiUsageException {
            return super.suggestOnConflict(trueRoot, relPath, basePath, paths, curr);
        }

        @Override
        public String commonRoot(List<String> paths) {
            return super.commonRoot(paths);
        }

        @Override
        public String[] splitLastElement(String path) {
            return super.splitLastElement(path);
        }

        @Override
        public String getStringFromToken(String token, Calendar now,
                Ice.Current curr) {
            return super.getStringFromToken(token, now, curr);
        }

    }

    @BeforeMethod(alwaysRun=true)
    public void setup() throws Exception {
        this.tmpDir = TempFileManager.create_path("repo", "test", true);
        this.templateDir = new File(this.tmpDir, "template");
        Mock mockReg = mock(Registry.class);
        this.daoMock = mock(RepositoryDao.class);
        this.reg = (Registry) mockReg.proxy();
        this.tmri = new TestManagedRepositoryI("/%year%/%month%/%day%",
                (RepositoryDao) daoMock.proxy(), this.reg);
        this.curr = new Ice.Current();
        this.curr.ctx = new HashMap<String, String>();
        this.curr.ctx.put(omero.constants.SESSIONUUID.value, "TEST");

    }

    private String getSuggestion(String base, String...paths) throws Exception {
        Import i = this.tmri.suggestOnConflict(this.tmpDir.getAbsolutePath(),
                "template", base, Arrays.asList(paths), curr);
        return new File(i.sharedPath).getName();
    }

    private void assertReturnFile(Long id) {
        OriginalFile of = new OriginalFileI(id, false);
        daoMock.expects(once()).method("createUserDirectory").will(returnValue(of));
    }

    public void testSuggestOnConflictPassesWithNonconflictingPaths() throws Exception {
        assertReturnFile(1L);
        new File(this.tmpDir, "/my/path");
        String expectedBasePath = "path";
        String suggestedBasePath = getSuggestion("/my/path", "/my/path/foo", "/my/path/bar");

        Assert.assertEquals(expectedBasePath, suggestedBasePath);
    }

    @Test
    public void testSuggestOnConflictReturnsNewPathOnConflict() throws Exception {
        assertReturnFile(1L);
        File upload = new File(this.templateDir, "/upload");
        upload.mkdirs();
        FileUtils.touch(new File(upload, "foo"));
        String expectedBasePath = "upload-1";
        String suggestedBasePath = getSuggestion("/upload", "/upload/foo", "/upload/bar");
        Assert.assertEquals(expectedBasePath, suggestedBasePath);
    }

    @Test
    public void testSuggestOnConflictReturnsBasePathWithEmptyPathsList() throws Exception {
        assertReturnFile(1L);
        String expectedBasePath = "upload";
        String suggestedBasePath = getSuggestion("/upload");
        Assert.assertEquals(expectedBasePath, suggestedBasePath);
    }

    @Test
    public void testCommonRootReturnsTopLevelWithUncommonPaths() {
        String expectedCommonRoot = "/";
        String actualCommonRoot = this.tmri.commonRoot(Arrays.asList("/home/bob/1.jpg",
                "/data/alice/1.jpg"));
        Assert.assertEquals(expectedCommonRoot, actualCommonRoot);
    }

    @Test
    public void testCommonRootReturnsCommonRootForPathList() {
        String expectedCommonRoot = "/bob/files/dv";
        String actualCommonRoot = this.tmri.commonRoot(Arrays.asList(
                expectedCommonRoot + "/file1.dv", expectedCommonRoot + "/file2.dv"));
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

    @Test
    public void testGetStringFromTokenReturnsEmptyStringOnNullToken() {
        String actual = this.tmri.getStringFromToken("", null, null);
        Assert.assertTrue(actual.isEmpty());
    }

    @Test
    public void testGetStringFromTokenReturnsTokenOnMalformedToken() {
        String expected = "foo";
        String actual = this.tmri.getStringFromToken(expected, null, null);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetStringFromTokenReturnsYear() {
        String expected = "2012";
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2012);
        String actual = this.tmri.getStringFromToken("%year%", cal, null);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetStringFromTokenReturnsMonth() {
        String expected = "10";
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Integer.parseInt(expected) - 1);
        String actual = this.tmri.getStringFromToken("%month%", cal, null);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetStringFromTokenReturnsMonthName() {
        String expected = "October";
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, 9);
        String actual = this.tmri.getStringFromToken("%monthname%", cal, null);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetStringFromTokenReturnsDay() {
        String expected = "7";
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 7);
        String actual = this.tmri.getStringFromToken("%day%", cal, null);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetStringFromTokenReturnsUserName() {
        String expected = "user-1";
        EventContext ecStub = new EventContext();
        ecStub.userName = expected;
        this.daoMock.expects(once()).method("getEventContext").with(ANYTHING).will(returnValue(ecStub));
        String actual = this.tmri.getStringFromToken("%user%", null, null);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetStringFromTokenReturnsGroupName() {
        String expected = "group-1";
        EventContext ecStub = new EventContext();
        ecStub.groupName = expected;
        this.daoMock.expects(once()).method("getEventContext").with(ANYTHING).will(returnValue(ecStub));
        String actual = this.tmri.getStringFromToken("%group%", null, null);
        Assert.assertEquals(expected, actual);
    }
}
