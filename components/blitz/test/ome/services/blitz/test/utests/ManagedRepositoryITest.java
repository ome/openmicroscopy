package ome.services.blitz.test.utests;

import java.io.File;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import junit.framework.Assert;
import loci.formats.FormatReader;
import ome.services.blitz.fire.Registry;
import ome.services.blitz.repo.FileMaker;
import ome.services.blitz.repo.ManagedRepositoryI;
import ome.services.blitz.repo.RepositoryDao;
import ome.services.blitz.repo.path.FsFile;
import omero.grid.ImportLocation;
import omero.model.ChecksumAlgorithm;
import omero.model.PermissionsI;
import omero.sys.EventContext;
import omero.util.TempFileManager;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

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
    long uniqueId;

    /**
     * Overrides protected methods from parent class for testing
     *
     * @author bpindelski
     */
    public class TestManagedRepositoryI extends ManagedRepositoryI {
        public static final String UUID = "fake-uuid";

        public TestManagedRepositoryI(String template,
                RepositoryDao repositoryDao) throws Exception {
            super(template, repositoryDao);
            File dir = TempFileManager.create_path("mng-repo.", ".test", true);
            initialize(new FileMaker(dir.getAbsolutePath()),
                    -1L /*id*/, UUID);
        }

        @Override
        public ImportLocation suggestImportPaths(FsFile relPath, FsFile basePath,
                List<FsFile> paths, Class<? extends FormatReader> reader,
                ChecksumAlgorithm checksumAlgorithm, Ice.Current curr)
                        throws omero.ServerError {
            return super.suggestImportPaths(relPath, basePath, paths, reader,
                    checksumAlgorithm, curr);
        }

        @Override
        public FsFile commonRoot(List<FsFile> paths) {
            return super.commonRoot(paths);
        }

        @Override
        public String expandTemplate(String template, Ice.Current curr) {
            return super.expandTemplate(template, curr);
        }

        @Override
        public void createTemplateDir(FsFile template, Ice.Current curr) throws omero.ServerError {
            super.createTemplateDir(template, curr);
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
                (RepositoryDao) daoMock.proxy());
        this.curr = new Ice.Current();
        this.curr.ctx = new HashMap<String, String>();
        this.curr.ctx.put(omero.constants.SESSIONUUID.value, "TEST");
        this.uniqueId = System.nanoTime();

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
        this.daoMock.expects(atLeastOnce()).method("getEventContext")
            .with(ANYTHING).will(returnValue(ec));
        return ec;
    }

    private static List<FsFile> toFsFileList(String... paths) {
        final List<FsFile> fsFiles = new ArrayList<FsFile>(paths.length);
        for (final String path : paths)
            fsFiles.add(new FsFile(path));
        return fsFiles;
    }

    @Test
    public void testCommonRootReturnsTopLevelWithUncommonPaths() {
        FsFile expectedCommonRoot = new FsFile();
        FsFile actualCommonRoot = this.tmri.commonRoot(
                toFsFileList("/home/bob/1.jpg", "/data/alice/1.jpg"));
        Assert.assertEquals(expectedCommonRoot, actualCommonRoot);
    }

    @Test
    public void testCommonRootReturnsCommonRootForPathList() {
        FsFile expectedCommonRoot = new FsFile("/bob/files/dv");
        FsFile actualCommonRoot = this.tmri.commonRoot(toFsFileList(
                expectedCommonRoot + "/file1.dv",
                expectedCommonRoot + "/file2.dv"));
        Assert.assertEquals(expectedCommonRoot, actualCommonRoot);
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
        if (expected.length() == 1)
            expected = '0' + expected;
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
        String expected = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH));
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
        String expected = "%björk%";
        newEventContext();
        String actual = this.tmri.expandTemplate("%björk%", curr);
        Assert.assertEquals(expected, actual);
    }

    /**
     * Test that the checksum algorithms offered by the managed repository
     * correspond to those listed for enum id
     * <tt>ome.model.enums.ChecksumAlgorithm</tt> in
     * <tt>acquisition.ome.xml</tt>.
     */
    @Test
    public void testListChecksumAlgorithms() {
        final Set<String> expectedAlgorithmNames =
                Sets.newHashSet("Adler-32", "CRC-32", "MD5-128", "Murmur3-32",
                        "Murmur3-128", "SHA1-160");
        for (final ChecksumAlgorithm algorithm :
            this.tmri.listChecksumAlgorithms(curr)) {
            Assert.assertTrue(expectedAlgorithmNames.remove(
                    algorithm.getValue().getValue()));
        }
        Assert.assertTrue(expectedAlgorithmNames.isEmpty());
    }
}
