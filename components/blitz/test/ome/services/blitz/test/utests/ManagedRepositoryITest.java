package ome.services.blitz.test.utests;

import java.io.File;
import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
import omero.model.PermissionsI;
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
    Calendar cal;

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
            throw new RuntimeException("NYI");
            //return super.suggestOnConflict(trueRoot, relPath, basePath, paths, curr);
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
        public String expandTemplate(String template, Ice.Current curr) {
            return super.expandTemplate(template, curr);
        }
    }

    @BeforeMethod(alwaysRun=true)
    public void setup() throws Exception {
        this.cal = Calendar.getInstance();
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

    private EventContext newEventContext() {
        EventContext ec = new EventContext();
        ec.userName = "";
        ec.userId = -1L;
        ec.groupName = "";
        ec.groupId = -1L;
        ec.sessionUuid = "";
        ec.sessionId = -1L;
        ec.eventId = -1L;
        ec.groupPermissions = new PermissionsI();
        this.daoMock.expects(once()).method("getEventContext")
            .with(ANYTHING).will(returnValue(ec));
        return ec;
    }

    private String getSuggestion(String base, String...paths) throws Exception {
        Import i = this.tmri.suggestOnConflict(this.tmpDir.getAbsolutePath(),
                "template", base, Arrays.asList(paths), curr);
        return new File(i.sharedPath).getName();
    }

    private void assertReturnFile(Long id) {
        OriginalFile of = new OriginalFileI(id, false);
        daoMock.expects(once()).method("register").will(returnValue(of));
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
    public void testExpandTemplateEmptyStringOnNullToken() {
        newEventContext();
        String actual = this.tmri.expandTemplate(null, curr);
        Assert.assertEquals(0, actual.length());
    }

    @Test
    public void testExpandTemplateTokenOnMalformedToken() {
        newEventContext();
        String expected = "foo";
        String actual = this.tmri.expandTemplate(expected, curr);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testExpandTemplateYear() {
        newEventContext();
        String expected = Integer.toString(cal.get(Calendar.YEAR));
        String actual = this.tmri.expandTemplate("%year%", curr);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testExpandTemplateMonth() {
        newEventContext();
        String expected = Integer.toString(cal.get(Calendar.MONTH)+1);
        String actual = this.tmri.expandTemplate("%month%", curr);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testExpandTemplateMonthName() {
        newEventContext();
        DateFormatSymbols dateFormat = new DateFormatSymbols();
        String expected = dateFormat.getMonths()
                [cal.get(Calendar.MONTH)];
        String actual = this.tmri.expandTemplate("%monthname%", curr);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testExpandTemplateDay() {
        newEventContext();
        String expected = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
        String actual = this.tmri.expandTemplate("%day%", curr);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testExpandTemplateUserName() {
        String expected = "user-1";
        EventContext ecStub = newEventContext();
        ecStub.userName = expected;
        String actual = this.tmri.expandTemplate("%user%", curr);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testExpandTemplateGroupName() {
        String expected = "group-1";
        EventContext ecStub = newEventContext();
        ecStub.groupName = expected;
        String actual = this.tmri.expandTemplate("%group%", curr);
        Assert.assertEquals(expected, actual);
    }
    @Test
    public void testExpandTemplateGroupNamePerms() {
        String expected = "group-1-rwrwrw";
        EventContext ecStub = newEventContext();
        ecStub.groupName = "group-1";
        ecStub.groupPermissions = new PermissionsI("rwrwrw");
        String actual = this.tmri.expandTemplate("%group%-%perms%", curr);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testExpandTemplateSession() {
        String expected = UUID.randomUUID().toString();
        EventContext ecStub = newEventContext();
        ecStub.sessionUuid = expected;
        String actual = this.tmri.expandTemplate("%session%", curr);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testExpandTemplateEscape() {
        String expected = "%%";
        newEventContext();
        String actual = this.tmri.expandTemplate("%%", curr);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testExpandTemplateEscape2() {
        String expected = "%%-grp";
        EventContext ecStub = newEventContext();
        ecStub.groupName = "grp";
        String actual = this.tmri.expandTemplate("%%-%group%", curr);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testExpandTemplateEscape3() {
        String expected = "%%george";
        newEventContext();
        String actual = this.tmri.expandTemplate("%%george", curr);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testExpandTemplateUnknown() {
        String expected = "%bjšrk%";
        newEventContext();
        String actual = this.tmri.expandTemplate("%bjšrk%", curr);
        Assert.assertEquals(expected, actual);
    }
}
