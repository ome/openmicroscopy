/*
 * Copyright (C) 2012-2013 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package integration;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.ImportLibrary.ImportCallback;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.util.ProportionalTimeEstimatorImpl;
import ome.formats.importer.util.TimeEstimator;
import ome.services.blitz.repo.path.ClientFilePathTransformer;
import ome.services.blitz.repo.path.FilePathRestrictionInstance;
import ome.services.blitz.repo.path.FilePathRestrictions;
import ome.services.blitz.repo.path.FsFile;
import ome.services.blitz.repo.path.MakePathComponentSafe;
import ome.services.blitz.util.ChecksumAlgorithmMapper;
import ome.util.checksum.ChecksumProvider;
import ome.util.checksum.ChecksumProviderFactory;
import ome.util.checksum.ChecksumProviderFactoryImpl;
import ome.util.checksum.ChecksumType;
import omero.LockTimeout;
import omero.RType;
import omero.ServerError;
import omero.api.RawFileStorePrx;
import omero.cmd.CmdCallbackI;
import omero.cmd.HandlePrx;
import omero.grid.ImportLocation;
import omero.grid.ImportProcessPrx;
import omero.grid.ImportRequest;
import omero.grid.ManagedRepositoryPrx;
import omero.grid.ManagedRepositoryPrxHelper;
import omero.grid.RepositoryMap;
import omero.grid.RepositoryPrx;
import omero.model.ChecksumAlgorithm;
import omero.model.OriginalFile;
import omero.sys.EventContext;
import omero.sys.Parameters;
import omero.util.TempFileManager;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Collections of tests for the <code>ManagedRepository</code> service.
 *
 * @author Colin Blackburn &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:c.blackburn@dundee.ac.uk">c.blackburn@dundee.ac.uk</a>
 * @author m.t.b.carroll@dundee.ac.uk
 */
@Test(groups = { "integration", "fs" })
public class ManagedRepositoryTest extends AbstractServerTest {
    /* temporary file manager for sources of file uploads */
    private static final TempFileManager tempFileManager = new TempFileManager(
            "test-" + ManagedRepositoryTest.class.getSimpleName());

    /** Reference to the managed repository. */
    ManagedRepositoryPrx repo;

    /** Description object representing this repository */
    OriginalFile obj;

    /* client file path transformer for comparing local and repo paths */
    private ClientFilePathTransformer cfpt = null;

    @BeforeClass
    public void setRepo() throws Exception {
        RepositoryMap rm = factory.sharedResources().repositories();
        for (int i = 0; i < rm.proxies.size(); i++) {
            final RepositoryPrx prx = rm.proxies.get(i);
            final OriginalFile obj = rm.descriptions.get(i);
            ManagedRepositoryPrx tmp = ManagedRepositoryPrxHelper
                    .checkedCast(prx);
            if (tmp != null) {
                this.repo = tmp;
                this.obj = obj;
            }
        }
        if (repo == null) {
            throw new Exception("Unable to find managed repository");
        }
    }

    @BeforeClass
    public void setupClientFilePathTransformer() {
        final FilePathRestrictions conservativeRules = FilePathRestrictionInstance
                .getFilePathRestrictions(FilePathRestrictionInstance.values());
        this.cfpt = new ClientFilePathTransformer(new MakePathComponentSafe(
                conservativeRules));
    }

    @AfterClass
    public void teardownClientFilePathTransformer() {
        this.cfpt = null;
    }

    /**
     * Makes sure that the OMERO file exists of the given name
     *
     * @param path
     *            The absolute filename.
     */
    void assertFileExists(String message, String path) throws ServerError {
        assertTrue(message + path, repo.fileExists(path));
    }

    /**
     * Makes sure that the OMERO file exists of the given name
     *
     * @param path
     *            The absolute filename.
     */
    void assertFileDoesNotExist(String message, String path) throws ServerError {
        assertFalse(message + path, repo.fileExists(path));
    }

    /**
     * Import the given files. Like {@link #importFileset(List, int)} but with
     * all the srcPaths to be uploaded.
     *
     * @param srcPaths
     *            the source paths
     * @return the resulting import location
     * @throws Exception
     *             unexpected
     */
    ImportLocation importFileset(List<String> srcPaths) throws Exception {
        return importFileset(srcPaths, srcPaths.size());
    }

    /**
     * Import the given files.
     *
     * @param srcPaths
     *            the source paths
     * @param numberToUpload
     *            how many of the source paths to actually upload
     * @return the resulting import location
     * @throws Exception
     *             unexpected
     */
    ImportLocation importFileset(List<String> srcPaths, int numberToUpload)
            throws Exception {

        // Setup that should be easier, most likely a single ctor on IL
        OMEROMetadataStoreClient client = new OMEROMetadataStoreClient();
        client.initialize(this.client);
        OMEROWrapper wrapper = new OMEROWrapper(new ImportConfig());
        ImportLibrary lib = new ImportLibrary(client, wrapper);

        // This should also be simplified.
        ImportContainer container = new ImportContainer(new File(
                srcPaths.get(0)), null /* target */, null /* user pixels */,
                "FakeReader", srcPaths.toArray(new String[srcPaths.size()]),
                false /* isspw */);

        // Now actually use the library.
        ImportProcessPrx proc = lib.createImport(container);

        // The following is largely a copy of ImportLibrary.importImage
        final String[] srcFiles = container.getUsedFiles();
        final List<String> checksums = new ArrayList<String>();
        final byte[] buf = new byte[client.getDefaultBlockSize()];
        final ChecksumProviderFactory cpf = new ChecksumProviderFactoryImpl();
        final TimeEstimator estimator = new ProportionalTimeEstimatorImpl(
                container.getUsedFilesTotalSize());

        for (int i = 0; i < numberToUpload; i++) {
            checksums.add(lib.uploadFile(proc, srcFiles, i, cpf, estimator,
                    buf));
        }

        // At this point the import is running, check handle for number of
        // steps.
        final HandlePrx handle = proc.verifyUpload(checksums);
        final ImportRequest req = (ImportRequest) handle.getRequest();
        final ImportCallback cb = lib.createCallback(proc, handle, container);
        cb.loop(60 * 60, 1000); // Wait 1 hr per step.
        assertNotNull(cb.getImportResponse());
        return req.location;
    }

    /**
     * Make sure that the given filename exists in the given directory, creating
     * it if necessary.
     *
     * @param directory
     *            the directory in which the file should exist
     * @param filename
     *            the name of the file
     * @return a File instance corresponding to the file in the given directory
     * @throws IOException
     *             if the file could not be created
     */
    private static File ensureFileExists(File directory, String filename)
            throws IOException {
        final File file = new File(directory, filename);

        if (!file.exists()) {
            final FileWriter out = new FileWriter(file);
            out.write("client-side file name is " + filename);
            out.close();
        }

        return file;
    }

    /**
     * Provide the managed repository path of the specified used file.
     *
     * @param importLocation
     *            an import location
     * @param index
     *            a used file index within the import location's used files
     * @return the used file's managed repository path
     */
    private static String pathToUsedFile(ImportLocation importLocation,
            int index) {
        final StringBuffer sb = new StringBuffer();
        sb.append(importLocation.sharedPath);
        sb.append(FsFile.separatorChar);
        sb.append(importLocation.usedFiles.get(index));
        return sb.toString();
    }

    /**
     * Test that the expected repository path is returned for a single image
     * file if new or already uploaded.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testGetCurrentRepoDirSimple() throws Exception {
        final File uniquePath = tempFileManager.createPath(UUID.randomUUID()
                .toString(), null, true);
        final File file1 = ensureFileExists(uniquePath, UUID.randomUUID()
                .toString() + ".fake");
        final File file2 = ensureFileExists(uniquePath, UUID.randomUUID()
                .toString() + ".fake");
        final String destPath1 = cfpt.getFsFileFromClientFile(file1, 2)
                .toString();
        final String destPath2 = cfpt.getFsFileFromClientFile(file2, 2)
                .toString();

        final List<String> srcPaths = new ArrayList<String>();
        final Set<String> usedFile2s = new HashSet<String>();

        // Completely new file
        srcPaths.add(file1.getAbsolutePath());
        ImportLocation data = importFileset(srcPaths);
        assertEndsWith(pathToUsedFile(data, 0), destPath1);

        // Different files that should go in same directory
        srcPaths.add(file2.getAbsolutePath());
        data = importFileset(srcPaths);
        assertEndsWith(pathToUsedFile(data, 0), destPath1);
        assertEndsWith(pathToUsedFile(data, 1), destPath2);
        for (final String usedFile : data.usedFiles) {
            /* all in the same directory below data.sharedPath */
            assertEquals(-1, usedFile.indexOf(FsFile.separatorChar));
        }
        assertTrue(usedFile2s.add(pathToUsedFile(data, 1)));

        // Same file that should go in new directory
        srcPaths.remove(0);
        data = importFileset(srcPaths);
        assertEndsWith(pathToUsedFile(data, 0), destPath2);
        assertTrue(usedFile2s.add(pathToUsedFile(data, 0)));

        // Same file again that should go in new directory
        data = importFileset(srcPaths);
        assertEndsWith(pathToUsedFile(data, 0), destPath2);
        assertTrue(usedFile2s.add(pathToUsedFile(data, 0)));
    }

    /**
     * Test that the expected repository path is returned for multiple files if
     * new or already uploaded.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testGetCurrentRepoDirMultipleFiles() throws Exception {
        final File uniquePath = tempFileManager.createPath(UUID.randomUUID()
                .toString(), null, true);
        final File file1 = ensureFileExists(uniquePath, UUID.randomUUID()
                .toString() + ".fake");
        final File file2 = ensureFileExists(uniquePath, UUID.randomUUID()
                .toString() + ".fake");
        final File file3 = ensureFileExists(uniquePath, UUID.randomUUID()
                .toString() + ".fake");
        final File file4 = ensureFileExists(uniquePath, UUID.randomUUID()
                .toString() + ".fake");
        final File file5 = ensureFileExists(uniquePath, UUID.randomUUID()
                .toString() + ".fake");
        final String destPath1 = cfpt.getFsFileFromClientFile(file1, 2)
                .toString();
        final String destPath2 = cfpt.getFsFileFromClientFile(file2, 2)
                .toString();
        final String destPath3 = cfpt.getFsFileFromClientFile(file3, 2)
                .toString();
        final String destPath4 = cfpt.getFsFileFromClientFile(file4, 2)
                .toString();
        final String destPath5 = cfpt.getFsFileFromClientFile(file5, 2)
                .toString();

        final List<String> srcPaths = new ArrayList<String>();
        final List<String> destPaths = new ArrayList<String>();
        final Set<String> sharedPaths = new HashSet<String>();

        // Completely new files
        srcPaths.add(file1.getAbsolutePath());
        srcPaths.add(file2.getAbsolutePath());
        destPaths.add(destPath1);
        destPaths.add(destPath2);
        ImportLocation data = importFileset(srcPaths);
        assertTrue(data.usedFiles.size() == destPaths.size());
        for (int i = 0; i < data.usedFiles.size(); i++) {
            assertEndsWith(pathToUsedFile(data, i), destPaths.get(i));
        }
        for (final String usedFile : data.usedFiles) {
            /* all in the same directory below data.sharedPath */
            assertEquals(-1, usedFile.indexOf(FsFile.separatorChar));
        }
        assertTrue(sharedPaths.add(data.sharedPath));

        // One identical file both should go in a new directory
        srcPaths.set(1, file3.getAbsolutePath());
        destPaths.set(1, destPath3);
        data = importFileset(srcPaths);
        assertTrue(data.usedFiles.size() == destPaths.size());
        for (int i = 0; i < data.usedFiles.size(); i++) {
            assertEndsWith(pathToUsedFile(data, i), destPaths.get(i));
        }
        for (final String usedFile : data.usedFiles) {
            /* all in the same directory below data.sharedPath */
            assertEquals(-1, usedFile.indexOf(FsFile.separatorChar));
        }
        assertTrue(sharedPaths.add(data.sharedPath));

        // Two different files that should go in new directory
        srcPaths.set(0, file4.getAbsolutePath());
        srcPaths.set(1, file5.getAbsolutePath());
        destPaths.set(0, destPath4);
        destPaths.set(1, destPath5);
        data = importFileset(srcPaths);
        assertTrue(data.usedFiles.size() == destPaths.size());
        for (int i = 0; i < data.usedFiles.size(); i++) {
            assertEndsWith(pathToUsedFile(data, i), destPaths.get(i));
        }
        for (final String usedFile : data.usedFiles) {
            /* all in the same directory below data.sharedPath */
            assertEquals(-1, usedFile.indexOf(FsFile.separatorChar));
        }
        assertTrue(sharedPaths.add(data.sharedPath));

        // Two identical files that should go in a new directory
        data = importFileset(srcPaths);
        assertTrue(data.usedFiles.size() == destPaths.size());
        for (int i = 0; i < data.usedFiles.size(); i++) {
            assertEndsWith(pathToUsedFile(data, i), destPaths.get(i));
        }
        for (final String usedFile : data.usedFiles) {
            /* all in the same directory below data.sharedPath */
            assertEquals(-1, usedFile.indexOf(FsFile.separatorChar));
        }
        assertTrue(sharedPaths.add(data.sharedPath));
    }

    /**
     * Test that the expected repository path is returned for multiple nested
     * files if new or already uploaded.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testGetCurrentRepoDirNested() throws Exception {
        final File uniquePath = tempFileManager.createPath(UUID.randomUUID()
                .toString(), null, true);
        final File uniquePathSubDir = new File(uniquePath, UUID.randomUUID()
                .toString());
        final File uniquePathSubSubDir = new File(uniquePathSubDir, UUID
                .randomUUID().toString());
        uniquePathSubDir.mkdir();
        uniquePathSubSubDir.mkdir();
        final File file1 = ensureFileExists(uniquePath, UUID.randomUUID()
                .toString() + ".fake");
        final File file2 = ensureFileExists(uniquePathSubDir, UUID.randomUUID()
                .toString() + ".fake");
        final File file3 = ensureFileExists(uniquePathSubSubDir, UUID
                .randomUUID().toString() + ".fake");
        final FsFile destFsFile1 = cfpt.getFsFileFromClientFile(file1, 2);
        final FsFile destFsFile2 = cfpt.getFsFileFromClientFile(file2, 3);
        final FsFile destFsFile3 = cfpt.getFsFileFromClientFile(file3, 4);

        assertEquals(2, destFsFile1.getComponents().size());
        assertEquals(3, destFsFile2.getComponents().size());
        assertEquals(4, destFsFile3.getComponents().size());
        assertEquals(destFsFile1.getComponents().get(0), destFsFile2
                .getComponents().get(0));
        assertEquals(destFsFile1.getComponents().get(0), destFsFile3
                .getComponents().get(0));
        assertEquals(destFsFile2.getComponents().get(1), destFsFile3
                .getComponents().get(1));

        final List<String> srcPaths = new ArrayList<String>();
        final List<String> destPaths = new ArrayList<String>();

        // Completely new files
        srcPaths.add(file1.getAbsolutePath());
        srcPaths.add(file2.getAbsolutePath());
        srcPaths.add(file3.getAbsolutePath());
        destPaths.add(destFsFile1.toString());
        destPaths.add(destFsFile2.toString());
        destPaths.add(destFsFile3.toString());
        ImportLocation data1 = importFileset(srcPaths);
        assertTrue(data1.usedFiles.size() == destPaths.size());
        for (int i = 0; i < data1.usedFiles.size(); i++) {
            assertEndsWith(pathToUsedFile(data1, i), destPaths.get(i));
        }

        // Same files should go into new directory
        ImportLocation data2 = importFileset(srcPaths);
        assertTrue(data2.usedFiles.size() == destPaths.size());
        for (int i = 0; i < data2.usedFiles.size(); i++) {
            assertEndsWith(pathToUsedFile(data2, i), destPaths.get(i));
        }
        assertNotSame(data1.sharedPath, data2.sharedPath);
        for (int index = 0; index < destPaths.size(); index++) {
            assertEquals(data1.usedFiles.get(index), data2.usedFiles.get(index));
        }
    }

    /**
     * Test that a single uploaded file can be deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "broken")
    public void testDeleteUploadedFileSimple() throws Exception {
        final File uniquePath = tempFileManager.createPath(UUID.randomUUID()
                .toString(), null, true);
        final File file1 = ensureFileExists(uniquePath, UUID.randomUUID()
                .toString() + ".fake");

        final List<String> srcPaths = new ArrayList<String>();

        srcPaths.add(file1.getAbsolutePath());
        ImportLocation data = importFileset(srcPaths);

        for (int index = 0; index < data.usedFiles.size(); index++) {
            assertFileExists("Upload failed. File does not exist: ",
                    pathToUsedFile(data, index));
        }

        assertDeletePaths(data);

        for (int index = 0; index < data.usedFiles.size(); index++) {
            assertFileDoesNotExist("Delete failed. File not deleted: ",
                    pathToUsedFile(data, index));
        }
    }

    /**
     * Test that multiple uploaded files can be deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "broken")
    public void testDeleteUploadedMultipleFilesSimple() throws Exception {
        final File uniquePath = tempFileManager.createPath(UUID.randomUUID()
                .toString(), null, true);
        final File file1 = ensureFileExists(uniquePath, UUID.randomUUID()
                .toString() + ".fake");
        final File file2 = ensureFileExists(uniquePath, UUID.randomUUID()
                .toString() + ".fake");

        final List<String> srcPaths = new ArrayList<String>();

        srcPaths.add(file1.getAbsolutePath());
        srcPaths.add(file2.getAbsolutePath());
        ImportLocation data = importFileset(srcPaths);

        for (int index = 0; index < data.usedFiles.size(); index++) {
            assertFileExists("Upload failed. File does not exist: ",
                    pathToUsedFile(data, index));
        }

        assertDeletePaths(data);

        for (int index = 0; index < data.usedFiles.size(); index++) {
            assertFileDoesNotExist("Delete failed. File not deleted: ",
                    pathToUsedFile(data, index));
        }
    }

    /**
     * Test that a partial upload can be deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "broken")
    public void testDeleteUploadedPartialFiles() throws Exception {
        final File uniquePath = tempFileManager.createPath(UUID.randomUUID()
                .toString(), null, true);
        final File file1 = ensureFileExists(uniquePath, UUID.randomUUID()
                .toString() + ".fake");
        final File file2 = ensureFileExists(uniquePath, UUID.randomUUID()
                .toString() + ".fake");

        final List<String> srcPaths = new ArrayList<String>();

        srcPaths.add(file1.getAbsolutePath());
        srcPaths.add(file2.getAbsolutePath());
        // TODO: due to verifyUpload one cannot obtain the import location
        // without uploading both files
        ImportLocation data = importFileset(srcPaths, 2);

        assertFileExists("Upload failed. File does not exist: ",
                pathToUsedFile(data, 0));
        assertFileDoesNotExist("Something wrong. File does exist!: ",
                pathToUsedFile(data, 1));

        assertDeletePaths(data);

        for (int index = 0; index < data.usedFiles.size(); index++) {
            assertFileDoesNotExist("Delete failed. File not deleted: ",
                    pathToUsedFile(data, index));
        }
    }

    /**
     * Test that multiple nested files can be deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "broken")
    public void testDeleteUploadedMultipleFilesNested() throws Exception {
        final File uniquePath = tempFileManager.createPath(UUID.randomUUID()
                .toString(), null, true);
        final File uniquePathSubDir = new File(uniquePath, UUID.randomUUID()
                .toString());
        final File uniquePathSubSubDir = new File(uniquePathSubDir, UUID
                .randomUUID().toString());
        uniquePathSubDir.mkdir();
        uniquePathSubSubDir.mkdir();
        final File file1 = ensureFileExists(uniquePath, UUID.randomUUID()
                .toString() + ".fake");
        final File file2 = ensureFileExists(uniquePathSubDir, UUID.randomUUID()
                .toString() + ".fake");
        final File file3 = ensureFileExists(uniquePathSubSubDir, UUID
                .randomUUID().toString() + ".fake");

        final List<String> srcPaths = new ArrayList<String>();

        srcPaths.add(file1.getAbsolutePath());
        srcPaths.add(file2.getAbsolutePath());
        srcPaths.add(file3.getAbsolutePath());
        ImportLocation data = importFileset(srcPaths);

        for (int index = 0; index < data.usedFiles.size(); index++) {
            assertFileExists("Upload failed. File does not exist: ",
                    pathToUsedFile(data, index));
        }

        assertDeletePaths(data);

        for (int index = 0; index < data.usedFiles.size(); index++) {
            assertFileDoesNotExist("Delete failed. File not deleted: ",
                    pathToUsedFile(data, index));
        }
    }

    /**
     * Test that with multiple files only those for a particular image are
     * deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "broken")
    public void testDeleteUploadedMultipleSetsDeleteOneSet() throws Exception {
        final File uniquePath = tempFileManager.createPath(UUID.randomUUID()
                .toString(), null, true);
        final File file1 = ensureFileExists(uniquePath, UUID.randomUUID()
                .toString() + ".fake");
        final File file2 = ensureFileExists(uniquePath, UUID.randomUUID()
                .toString() + ".fake");
        final File file3 = ensureFileExists(uniquePath, UUID.randomUUID()
                .toString() + ".fake");
        final File file4 = ensureFileExists(uniquePath, UUID.randomUUID()
                .toString() + ".fake");

        final List<String> srcPaths = new ArrayList<String>();

        srcPaths.add(file1.getAbsolutePath());
        srcPaths.add(file2.getAbsolutePath());
        ImportLocation data1 = importFileset(srcPaths);

        srcPaths.set(0, file3.getAbsolutePath());
        srcPaths.set(1, file4.getAbsolutePath());
        ImportLocation data2 = importFileset(srcPaths);

        for (int index = 0; index < data1.usedFiles.size(); index++) {
            assertFileExists("Upload failed. File does not exist: ",
                    pathToUsedFile(data1, index));
        }
        for (int index = 0; index < data2.usedFiles.size(); index++) {
            assertFileExists("Upload failed. File does not exist: ",
                    pathToUsedFile(data2, index));
        }

        assertDeletePaths(data1);

        for (int index = 0; index < data1.usedFiles.size(); index++) {
            assertFileDoesNotExist("Delete failed. File not deleted: ",
                    pathToUsedFile(data1, index));
        }
        for (int index = 0; index < data2.usedFiles.size(); index++) {
            assertFileExists("Delete failed. File deleted!: ",
                    pathToUsedFile(data2, index));
        }
    }

    /**
     * Make sure that the call to delete the import location's used files
     * returns with success.
     *
     * @param data
     *            an import location
     * @throws ServerError
     *             unexpected
     * @throws InterruptedException
     *             unexpected
     * @throws LockTimeout
     *             unexpected
     */
    protected void assertDeletePaths(ImportLocation data) throws ServerError,
            InterruptedException, LockTimeout {
        final String[] pathsToDelete = new String[data.usedFiles.size()];
        for (int index = 0; index < data.usedFiles.size(); index++) {
            pathsToDelete[index] = pathToUsedFile(data, index);
        }
        HandlePrx handle = repo.deletePaths(pathsToDelete, false, false);
        CmdCallbackI cb = new CmdCallbackI(client, handle);
        cb.loop(10, 500);
        assertCmd(cb, true);
    }

    /**
     * Assert that the destination path ends with the used file.
     *
     * @param usedFile
     *            the used file
     * @param destPath
     *            the destination path
     */
    private static void assertEndsWith(String usedFile, String destPath) {
        assertTrue("\nExpected: " + destPath + "\nActual: " + usedFile,
                usedFile.endsWith(destPath));
    }

    /* the contents of the sample file used in file hash tests */
    private static final byte[] SAMPLE_FILE_CONTENTS = new byte[] {1, 2, 3, 4, 5};

    /* the source of hashers used in test methods */
    private static final ChecksumProviderFactory CHECKSUM_PROVIDER_FACTORY = new ChecksumProviderFactoryImpl();

    /**
     * Get the original file that has the given ID.
     * @param fileId the ID of the file to retrieve, must exist
     * @return the corresponding original file with the hasher property joined
     * @throws ServerError unexpected
     */
    private OriginalFile getFile(long fileId) throws ServerError {
        final String query = "FROM OriginalFile o LEFT OUTER JOIN FETCH o.hasher WHERE o.id = :id";
        final Parameters params = new Parameters();
        params.map = ImmutableMap.<String, omero.RType>of("id", omero.rtypes.rlong(fileId));

        final RType queryResult = iQuery.projection(query, params).get(0).get(0);
        return (OriginalFile) ((omero.RObject) queryResult).getValue();
    }

    /**
     * Upload the sample file to the repository.
     * @return the new original file's ID
     * @throws ServerError unexpected
     */
    private long uploadSampleFile() throws ServerError {
        final EventContext ctx = iAdmin.getEventContext();
        final StringBuffer path = new StringBuffer();
        path.append(ctx.userName);
        path.append('_');
        path.append(ctx.userId);
        path.append(FsFile.separatorChar);
        path.append("test-");
        path.append(getClass());
        path.append(FsFile.separatorChar);
        path.append(System.currentTimeMillis());

        repo.makeDir(path.toString(), true);

        path.append(FsFile.separatorChar);
        path.append(System.nanoTime());

        final RawFileStorePrx rfs = repo.file(path.toString(), "rw");
        rfs.write(SAMPLE_FILE_CONTENTS, 0, SAMPLE_FILE_CONTENTS.length);
        final long fileId = rfs.save().getId().getValue();
        rfs.close();

        /* clear any checksum set by the repository */
        final OriginalFile file = getFile(fileId);
        file.setHasher(null);
        file.setHash(null);
        iUpdate.saveObject(file);

        return fileId;
    }

    /**
     * Corrupt the existing checksum of the given file.
     * @param fileId the original file's ID
     * @throws ServerError unexpected
     */
    private void corruptChecksum(long fileId) throws ServerError {
        final OriginalFile file = getFile(fileId);
        file.setHash(omero.rtypes.rstring("corrupted hash"));
        iUpdate.saveObject(file);
    }

    /**
     * Assert that the checksum of the given file is as expected.
     * @param fileId the ID of the original file to check
     * @param expectedHasher the expected hasher of the file, may be {@code null}
     * @param expectedHash the expected hash of the file, may be {@code null}
     * @throws ServerError unexpected
     */
    private void assertFileChecksum(long fileId, String expectedHasher, String expectedHash) throws ServerError {
        final OriginalFile file = getFile(fileId);
        final String actualHasher = file.getHasher() == null ? null : file.getHasher().getValue().getValue();
        final String actualHash = file.getHash() == null ? null : file.getHash().getValue();
 
        Assert.assertEquals(actualHasher, expectedHasher, "expected correct hasher");
        Assert.assertEquals(actualHash, expectedHash, "expected correct hash");
    }

    /**
     * Test that a checksum can be added to an already uploaded file.
     * @throws ServerError unexpected
     */
    @Test
    public void testNewChecksumFromNone() throws ServerError {
        /* find the file's expected hash */
        final ChecksumAlgorithm murmur128Algorithm = ChecksumAlgorithmMapper.getChecksumAlgorithm("Murmur3-128");
        final ChecksumType murmur128Type = ChecksumAlgorithmMapper.getChecksumType(murmur128Algorithm);
        final ChecksumProvider murmur128 = CHECKSUM_PROVIDER_FACTORY.getProvider(murmur128Type);
        murmur128.putBytes(SAMPLE_FILE_CONTENTS);
        final String murmur128Hash = murmur128.checksumAsString();

        /* upload the file */
        final long fileId = uploadSampleFile();

        /* check that the file does not have a checksum */
        assertFileChecksum(fileId, null, null);

        /* set the file to now have a checksum */
        final List<Long> changedIds = repo.setChecksumAlgorithm(murmur128Algorithm, ImmutableList.of(fileId));

        /* check that the file's checksum is reported to have been set */
        Assert.assertEquals(changedIds.size(), 1, "expected to change one file's hash");
        Assert.assertEquals((long) changedIds.get(0), fileId, "expected to change hash of specified file");

        /* check that the file has been set with the new checksum */
        assertFileChecksum(fileId, murmur128Algorithm.getValue().getValue(), murmur128Hash);
    }

    /**
     * Test that the checksum of a file can be changed.
     * @throws ServerError unexpected
     */
    @Test
    public void testNewChecksumFromDifferent() throws ServerError {
        /* find the file's expected hash before */
        final ChecksumAlgorithm md5Algorithm = ChecksumAlgorithmMapper.getChecksumAlgorithm("MD5-128");
        final ChecksumType md5Type = ChecksumAlgorithmMapper.getChecksumType(md5Algorithm);
        final ChecksumProvider md5 = CHECKSUM_PROVIDER_FACTORY.getProvider(md5Type);
        md5.putBytes(SAMPLE_FILE_CONTENTS);
        final String md5Hash = md5.checksumAsString();

        /* find the file's expected hash after */
        final ChecksumAlgorithm murmur128Algorithm = ChecksumAlgorithmMapper.getChecksumAlgorithm("Murmur3-128");
        final ChecksumType murmur128Type = ChecksumAlgorithmMapper.getChecksumType(murmur128Algorithm);
        final ChecksumProvider murmur128 = CHECKSUM_PROVIDER_FACTORY.getProvider(murmur128Type);
        murmur128.putBytes(SAMPLE_FILE_CONTENTS);
        final String murmur128Hash = murmur128.checksumAsString();

        /* upload the file and set its checksum */
        final long fileId = uploadSampleFile();
        repo.setChecksumAlgorithm(md5Algorithm, ImmutableList.of(fileId));

        /* check that the file has been set with the old checksum */
        assertFileChecksum(fileId, md5Algorithm.getValue().getValue(), md5Hash);

        /* set the file to its new checksum */
        final List<Long> changedIds = repo.setChecksumAlgorithm(murmur128Algorithm, ImmutableList.of(fileId));

        /* check that the file's checksum is reported to have been set */
        Assert.assertEquals(changedIds.size(), 1, "expected to change one file's hash");
        Assert.assertEquals((long) changedIds.get(0), fileId, "expected to change hash of specified file");

        /* check that the file has been set with the new checksum */
        assertFileChecksum(fileId, murmur128Algorithm.getValue().getValue(), murmur128Hash);
    }

    /**
     * Test that nothing happens when the checksum of a file is set to what it already is.
     * @throws ServerError unexpected
     */
    @Test
    public void testNewChecksumFromSame() throws ServerError {
        /* find the file's expected hash */
        final ChecksumAlgorithm md5Algorithm = ChecksumAlgorithmMapper.getChecksumAlgorithm("MD5-128");
        final ChecksumType md5Type = ChecksumAlgorithmMapper.getChecksumType(md5Algorithm);
        final ChecksumProvider md5 = CHECKSUM_PROVIDER_FACTORY.getProvider(md5Type);
        md5.putBytes(SAMPLE_FILE_CONTENTS);
        final String md5Hash = md5.checksumAsString();

        /* upload the file and set its checksum */
        final long fileId = uploadSampleFile();
        repo.setChecksumAlgorithm(md5Algorithm, ImmutableList.of(fileId));

        /* check that the file has been set with the checksum */
        assertFileChecksum(fileId, md5Algorithm.getValue().getValue(), md5Hash);

        /* set the file to the same checksum */
        final List<Long> changedIds = repo.setChecksumAlgorithm(md5Algorithm, ImmutableList.of(fileId));

        /* check that the file's checksum is not reported to have been set */
        Assert.assertTrue(changedIds.isEmpty(), "expected to change no file's hash");

        /* check that the file retains its checksum */
        assertFileChecksum(fileId, md5Algorithm.getValue().getValue(), md5Hash);
    }

    /**
     * Test that the checksum of a file is not changed, and an error is thrown, if the file was corrupted.
     * @throws ServerError because the file's old checksum is wrong
     */
    @Test(expectedExceptions = ServerError.class)
    public void testNewChecksumAborts() throws ServerError {
        /* find the file's expected hash before */
        final ChecksumAlgorithm md5Algorithm = ChecksumAlgorithmMapper.getChecksumAlgorithm("MD5-128");
        final ChecksumType md5Type = ChecksumAlgorithmMapper.getChecksumType(md5Algorithm);
        final ChecksumProvider md5 = CHECKSUM_PROVIDER_FACTORY.getProvider(md5Type);
        md5.putBytes(SAMPLE_FILE_CONTENTS);
        final String md5Hash = md5.checksumAsString();

        /* find the file's new hasher */
        final ChecksumAlgorithm murmur128Algorithm = ChecksumAlgorithmMapper.getChecksumAlgorithm("Murmur3-128");

        /* upload the file and set its checksum */
        final long fileId = uploadSampleFile();
        repo.setChecksumAlgorithm(md5Algorithm, ImmutableList.of(fileId));

        /* check that the file has been set with the old checksum */
        assertFileChecksum(fileId, md5Algorithm.getValue().getValue(), md5Hash);

        /* corrupt the file's old checksum */
        corruptChecksum(fileId);

        /* attempt to set the file to its new checksum */
        repo.setChecksumAlgorithm(murmur128Algorithm, ImmutableList.of(fileId));
    }

    /**
     * Test that bad file checksums are correctly reported.
     * @throws ServerError unexpected
     */
    public void testVerifyChecksums() throws ServerError {
        /* upload the files */
        final long fileId1 = uploadSampleFile();
        final long fileId2 = uploadSampleFile();
        final long fileId3 = uploadSampleFile();
        final long fileId4 = uploadSampleFile();
        final List<Long> fileIds = ImmutableList.of(fileId1, fileId2, fileId3, fileId4);

        /* set the files' checksum */
        final ChecksumAlgorithm shaAlgorithm = ChecksumAlgorithmMapper.getChecksumAlgorithm("SHA1-160");
        final List<Long> changedIds = repo.setChecksumAlgorithm(shaAlgorithm, fileIds);
        Assert.assertEquals(changedIds.size(), fileIds.size(), "expected to have changed the files' checksum");

        /* corrupt some files' checksums */
        final List<Long> corruptedFileIds = ImmutableList.of(fileId2, fileId3);
        for (final long corruptedFileId : corruptedFileIds) {
            corruptChecksum(corruptedFileId);
        }

        /* check that only the expected files have a bad checksum */
        final List<Long> failedVerificationIds = repo.verifyChecksums(fileIds);
        Assert.assertEqualsNoOrder(failedVerificationIds.toArray(), corruptedFileIds.toArray(),
                "expected the exactly corrupted files to fail checksum verification");
    }
}
