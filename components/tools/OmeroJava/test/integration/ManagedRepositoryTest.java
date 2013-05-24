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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.ImportLibrary.ImportCallback;
import ome.formats.importer.OMEROWrapper;
import ome.services.blitz.repo.path.ClientFilePathTransformer;
import ome.services.blitz.repo.path.FilePathRestrictionInstance;
import ome.services.blitz.repo.path.FilePathRestrictions;
import ome.services.blitz.repo.path.FsFile;
import ome.services.blitz.repo.path.MakePathComponentSafe;
import ome.util.checksum.ChecksumProviderFactory;
import ome.util.checksum.ChecksumProviderFactoryImpl;
import omero.LockTimeout;
import omero.ServerError;
import omero.cmd.CmdCallbackI;
import omero.cmd.HandlePrx;
import omero.grid.ImportLocation;
import omero.grid.ImportProcessPrx;
import omero.grid.ImportRequest;
import omero.grid.ImportResponse;
import omero.grid.ManagedRepositoryPrx;
import omero.grid.ManagedRepositoryPrxHelper;
import omero.grid.RepositoryMap;
import omero.grid.RepositoryPrx;
import omero.model.Fileset;
import omero.model.OriginalFile;
import omero.util.TempFileManager;

/**
* Collections of tests for the <code>ManagedRepository</code> service.
*
* @author Colin Blackburn &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:c.blackburn@dundee.ac.uk">c.blackburn@dundee.ac.uk</a>
* @author m.t.b.carroll@dundee.ac.uk
*/
@Test(groups = {"integration", "fs"})
public class ManagedRepositoryTest
   extends AbstractServerTest
{
    /* temporary file manager for sources of file uploads */
    private static final TempFileManager tempFileManager =
            new TempFileManager("test-" + ManagedRepositoryTest.class.getSimpleName());

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
        final FilePathRestrictions conservativeRules =
                FilePathRestrictionInstance.getFilePathRestrictions(FilePathRestrictionInstance.values());
        this.cfpt = new ClientFilePathTransformer(new MakePathComponentSafe(conservativeRules));
    }

    @AfterClass
    public void teardownClientFilePathTransformer() {
        this.cfpt = null;
    }

	/**
	 * Makes sure that the OMERO file exists of the given name
	 * @param path The absolute filename.
	 */
	void assertFileExists(String message, String path)
	        throws ServerError
	{
	    assertTrue(message + path, repo.fileExists(path));
	}

	/**
	 * Makes sure that the OMERO file exists of the given name
	 * @param path The absolute filename.
	 */
	void assertFileDoesNotExist(String message, String path)
	        throws ServerError
	{
	    assertFalse(message + path, repo.fileExists(path));
	}

    /**
     * Import the given files.
     * Like {@link #importFileset(List, int)} but with all the srcPaths to be uploaded.
     * @param srcPaths the source paths
     * @return the resulting import location
     * @throws Exception unexpected
     */
    ImportLocation importFileset(List<String> srcPaths) throws Exception {
        return importFileset(srcPaths, srcPaths.size());
    }

	/**
	 * Import the given files.
	 * @param srcPaths the source paths
	 * @param numberToUpload how many of the source paths to actually upload
	 * @return the resulting import location
	 * @throws Exception unexpected
	 */
	ImportLocation importFileset(List<String> srcPaths, int numberToUpload) throws Exception {

	    // Setup that should be easier, most likely a single ctor on IL
	    OMEROMetadataStoreClient client = new OMEROMetadataStoreClient();
	    client.initialize(this.client);
	    OMEROWrapper wrapper = new OMEROWrapper(new ImportConfig());
	    ImportLibrary lib = new ImportLibrary(client, wrapper);

	    // This should also be simplified.
	    ImportContainer container = new ImportContainer(new File(srcPaths.get(0)),
	            null /*target*/, null /*user pixels */, "FakeReader",
	            srcPaths.toArray(new String[srcPaths.size()]), false /*isspw*/);

	    // Now actually use the library.
	    ImportProcessPrx proc = lib.createImport(container);

	    // The following is largely a copy of ImportLibrary.importImage
        final String[] srcFiles = container.getUsedFiles();
        final List<String> checksums = new ArrayList<String>();
        final byte[] buf = new byte[client.getDefaultBlockSize()];
        final ChecksumProviderFactory cpf = new ChecksumProviderFactoryImpl();

        for (int i = 0; i < numberToUpload; i++) {
            checksums.add(lib.uploadFile(proc, srcFiles, i, cpf, buf));
        }

        // At this point the import is running, check handle for number of
        // steps.
        final HandlePrx handle = proc.verifyUpload(checksums);
        final ImportRequest req = (ImportRequest) handle.getRequest();
        final ImportCallback cb = lib.createCallback(proc, handle, container);
        cb.loop(60*60, 1000); // Wait 1 hr per step.
        assertNotNull(cb.getImportResponse());
        return req.location;
	}

    /**
     * Make sure that the given filename exists in the given directory, creating it if necessary.
     * @param directory the directory in which the file should exist
     * @param filename the name of the file
     * @return a File instance corresponding to the file in the given directory
     * @throws IOException if the file could not be created
     */
    private static File ensureFileExists(File directory, String filename) throws IOException {
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
     * @param importLocation an import location
     * @param index a used file index within the import location's used files
     * @return the used file's managed repository path
     */
    private static String pathToUsedFile(ImportLocation importLocation, int index) {
        final StringBuffer sb = new StringBuffer();
        sb.append(importLocation.sharedPath);
        sb.append(FsFile.separatorChar);
        sb.append(importLocation.usedFiles.get(index));
        return sb.toString();
    }

    /**
     * Test that the expected repository path is returned
     * for a single image file if new or already uploaded.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testGetCurrentRepoDirSimple() throws Exception {
        final File uniquePath = tempFileManager.createPath(UUID.randomUUID().toString(), null, true);
        final File file1 = ensureFileExists(uniquePath, UUID.randomUUID().toString() + ".fake");
        final File file2 = ensureFileExists(uniquePath, UUID.randomUUID().toString() + ".fake");
        final String destPath1 = cfpt.getFsFileFromClientFile(file1, 2).toString();
        final String destPath2 = cfpt.getFsFileFromClientFile(file2, 2).toString();

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
     * Test that the expected repository path is returned
     * for multiple files if new or already uploaded.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testGetCurrentRepoDirMultipleFiles() throws Exception {
        final File uniquePath = tempFileManager.createPath(UUID.randomUUID().toString(), null, true);
        final File file1 = ensureFileExists(uniquePath, UUID.randomUUID().toString() + ".fake");
        final File file2 = ensureFileExists(uniquePath, UUID.randomUUID().toString() + ".fake");
        final File file3 = ensureFileExists(uniquePath, UUID.randomUUID().toString() + ".fake");
        final File file4 = ensureFileExists(uniquePath, UUID.randomUUID().toString() + ".fake");
        final File file5 = ensureFileExists(uniquePath, UUID.randomUUID().toString() + ".fake");
        final String destPath1 = cfpt.getFsFileFromClientFile(file1, 2).toString();
        final String destPath2 = cfpt.getFsFileFromClientFile(file2, 2).toString();
        final String destPath3 = cfpt.getFsFileFromClientFile(file3, 2).toString();
        final String destPath4 = cfpt.getFsFileFromClientFile(file4, 2).toString();
        final String destPath5 = cfpt.getFsFileFromClientFile(file5, 2).toString();

        final List<String> srcPaths = new ArrayList<String>();
        final List<String> destPaths = new ArrayList<String>();
        final Set<String> sharedPaths = new HashSet<String>();

	    // Completely new files
        srcPaths.add(file1.getAbsolutePath());
        srcPaths.add(file2.getAbsolutePath());
        destPaths.add(destPath1);
        destPaths.add(destPath2);
        ImportLocation data = importFileset(srcPaths);
        assertTrue(data.usedFiles.size()==destPaths.size());
        for (int i=0; i<data.usedFiles.size(); i++) {
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
        assertTrue(data.usedFiles.size()==destPaths.size());
        for (int i=0; i<data.usedFiles.size(); i++) {
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
        assertTrue(data.usedFiles.size()==destPaths.size());
        for (int i=0; i<data.usedFiles.size(); i++) {
            assertEndsWith(pathToUsedFile(data, i), destPaths.get(i));
        }
        for (final String usedFile : data.usedFiles) {
            /* all in the same directory below data.sharedPath */
            assertEquals(-1, usedFile.indexOf(FsFile.separatorChar));
        }
        assertTrue(sharedPaths.add(data.sharedPath));

        // Two identical files that should go in a new directory
        data = importFileset(srcPaths);
        assertTrue(data.usedFiles.size()==destPaths.size());
        for (int i=0; i<data.usedFiles.size(); i++) {
            assertEndsWith(pathToUsedFile(data, i), destPaths.get(i));
        }
        for (final String usedFile : data.usedFiles) {
            /* all in the same directory below data.sharedPath */
            assertEquals(-1, usedFile.indexOf(FsFile.separatorChar));
        }
        assertTrue(sharedPaths.add(data.sharedPath));
    }

    /**
     * Test that the expected repository path is returned
     * for multiple nested files if new or already uploaded.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testGetCurrentRepoDirNested() throws Exception {
        final File uniquePath = tempFileManager.createPath(UUID.randomUUID().toString(), null, true);
        final File uniquePathSubDir = new File(uniquePath, UUID.randomUUID().toString());
        final File uniquePathSubSubDir = new File(uniquePathSubDir, UUID.randomUUID().toString());
        uniquePathSubDir.mkdir();
        uniquePathSubSubDir.mkdir();
        final File file1 = ensureFileExists(uniquePath, UUID.randomUUID().toString() + ".fake");
        final File file2 = ensureFileExists(uniquePathSubDir, UUID.randomUUID().toString() + ".fake");
        final File file3 = ensureFileExists(uniquePathSubSubDir, UUID.randomUUID().toString() + ".fake");
        final FsFile destFsFile1 = cfpt.getFsFileFromClientFile(file1, 2);
        final FsFile destFsFile2 = cfpt.getFsFileFromClientFile(file2, 3);
        final FsFile destFsFile3 = cfpt.getFsFileFromClientFile(file3, 4);

        assertEquals(2, destFsFile1.getComponents().size());
        assertEquals(3, destFsFile2.getComponents().size());
        assertEquals(4, destFsFile3.getComponents().size());
        assertEquals(destFsFile1.getComponents().get(0), destFsFile2.getComponents().get(0));
        assertEquals(destFsFile1.getComponents().get(0), destFsFile3.getComponents().get(0));
        assertEquals(destFsFile2.getComponents().get(1), destFsFile3.getComponents().get(1));

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
        assertTrue(data1.usedFiles.size()==destPaths.size());
        for (int i=0; i<data1.usedFiles.size(); i++) {
            assertEndsWith(pathToUsedFile(data1, i), destPaths.get(i));
        }

        // Same files should go into new directory
        ImportLocation data2 = importFileset(srcPaths);
        assertTrue(data2.usedFiles.size()==destPaths.size());
        for (int i=0; i<data2.usedFiles.size(); i++) {
            assertEndsWith(pathToUsedFile(data2, i), destPaths.get(i));
        }
        assertNotSame(data1.sharedPath, data2.sharedPath);
        for (int index = 0; index < destPaths.size(); index++) {
            assertEquals(data1.usedFiles.get(index), data2.usedFiles.get(index));
        }
    }

    /**
     * Test that a single uploaded file can be deleted.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteUploadedFileSimple() throws Exception {
        final File uniquePath = tempFileManager.createPath(UUID.randomUUID().toString(), null, true);
        final File file1 = ensureFileExists(uniquePath, UUID.randomUUID().toString() + ".fake");

        final List<String> srcPaths = new ArrayList<String>();

        srcPaths.add(file1.getAbsolutePath());
        ImportLocation data = importFileset(srcPaths);

        for (int index = 0; index < data.usedFiles.size(); index++) {
            assertFileExists("Upload failed. File does not exist: ", pathToUsedFile(data, index));
        }

        assertDeletePaths(data);

        for (int index = 0; index < data.usedFiles.size(); index++) {
            assertFileDoesNotExist("Delete failed. File not deleted: ", pathToUsedFile(data, index));
        }
    }

    /**
     * Test that multiple uploaded files can be deleted.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteUploadedMultipleFilesSimple() throws Exception {
        final File uniquePath = tempFileManager.createPath(UUID.randomUUID().toString(), null, true);
        final File file1 = ensureFileExists(uniquePath, UUID.randomUUID().toString() + ".fake");
        final File file2 = ensureFileExists(uniquePath, UUID.randomUUID().toString() + ".fake");

        final List<String> srcPaths = new ArrayList<String>();

        srcPaths.add(file1.getAbsolutePath());
        srcPaths.add(file2.getAbsolutePath());
        ImportLocation data = importFileset(srcPaths);

        for (int index = 0; index < data.usedFiles.size(); index++) {
            assertFileExists("Upload failed. File does not exist: ", pathToUsedFile(data, index));
        }

        assertDeletePaths(data);

        for (int index = 0; index < data.usedFiles.size(); index++) {
            assertFileDoesNotExist("Delete failed. File not deleted: ", pathToUsedFile(data, index));
        }
    }

    /**
     * Test that a partial upload can be deleted.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteUploadedPartialFiles() throws Exception {
        final File uniquePath = tempFileManager.createPath(UUID.randomUUID().toString(), null, true);
        final File file1 = ensureFileExists(uniquePath, UUID.randomUUID().toString() + ".fake");
        final File file2 = ensureFileExists(uniquePath, UUID.randomUUID().toString() + ".fake");

        final List<String> srcPaths = new ArrayList<String>();

        srcPaths.add(file1.getAbsolutePath());
        srcPaths.add(file2.getAbsolutePath());
        // TODO: due to verifyUpload one cannot obtain the import location without uploading both files
        ImportLocation data = importFileset(srcPaths, 2);

        assertFileExists("Upload failed. File does not exist: ", pathToUsedFile(data, 0));
        assertFileDoesNotExist("Something wrong. File does exist!: ", pathToUsedFile(data, 1));
 
        assertDeletePaths(data);

        for (int index = 0; index < data.usedFiles.size(); index++) {
            assertFileDoesNotExist("Delete failed. File not deleted: ", pathToUsedFile(data, index));
        }
    }

    /**
     * Test that multiple nested files can be deleted.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteUploadedMultipleFilesNested() throws Exception {
        final File uniquePath = tempFileManager.createPath(UUID.randomUUID().toString(), null, true);
        final File uniquePathSubDir = new File(uniquePath, UUID.randomUUID().toString());
        final File uniquePathSubSubDir = new File(uniquePathSubDir, UUID.randomUUID().toString());
        uniquePathSubDir.mkdir();
        uniquePathSubSubDir.mkdir();
        final File file1 = ensureFileExists(uniquePath, UUID.randomUUID().toString() + ".fake");
        final File file2 = ensureFileExists(uniquePathSubDir, UUID.randomUUID().toString() + ".fake");
        final File file3 = ensureFileExists(uniquePathSubSubDir, UUID.randomUUID().toString() + ".fake");

        final List<String> srcPaths = new ArrayList<String>();

        srcPaths.add(file1.getAbsolutePath());
        srcPaths.add(file2.getAbsolutePath());
        srcPaths.add(file3.getAbsolutePath());
        ImportLocation data = importFileset(srcPaths);

        for (int index = 0; index < data.usedFiles.size(); index++) {
            assertFileExists("Upload failed. File does not exist: ", pathToUsedFile(data, index));
        }

        assertDeletePaths(data);

        for (int index = 0; index < data.usedFiles.size(); index++) {
            assertFileDoesNotExist("Delete failed. File not deleted: ", pathToUsedFile(data, index));
        }
    }

    /**
     * Test that with multiple files only those for a particular image are deleted.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteUploadedMultipleSetsDeleteOneSet() throws Exception {
        final File uniquePath = tempFileManager.createPath(UUID.randomUUID().toString(), null, true);
        final File file1 = ensureFileExists(uniquePath, UUID.randomUUID().toString() + ".fake");
        final File file2 = ensureFileExists(uniquePath, UUID.randomUUID().toString() + ".fake");
        final File file3 = ensureFileExists(uniquePath, UUID.randomUUID().toString() + ".fake");
        final File file4 = ensureFileExists(uniquePath, UUID.randomUUID().toString() + ".fake");

        final List<String> srcPaths = new ArrayList<String>();

        srcPaths.add(file1.getAbsolutePath());
        srcPaths.add(file2.getAbsolutePath());
        ImportLocation data1 = importFileset(srcPaths);

        srcPaths.set(0, file3.getAbsolutePath());
        srcPaths.set(1, file4.getAbsolutePath());
        ImportLocation data2 = importFileset(srcPaths);

        for (int index = 0; index < data1.usedFiles.size(); index++) {
            assertFileExists("Upload failed. File does not exist: ", pathToUsedFile(data1, index));
        }
        for (int index = 0; index < data2.usedFiles.size(); index++) {
            assertFileExists("Upload failed. File does not exist: ", pathToUsedFile(data2, index));
        }

        assertDeletePaths(data1);

        for (int index = 0; index < data1.usedFiles.size(); index++) {
            assertFileDoesNotExist("Delete failed. File not deleted: ", pathToUsedFile(data1, index));
        }
        for (int index = 0; index < data2.usedFiles.size(); index++) {
            assertFileExists("Delete failed. File deleted!: ", pathToUsedFile(data2, index));
        }
    }

    /**
     * Make sure that the call to delete the import location's used files returns with success.
     * @param data an import location
     * @throws ServerError unexpected
     * @throws InterruptedException unexpected
     * @throws LockTimeout unexpected
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
     * @param usedFile the used file
     * @param destPath the destination path
     */
    private static void assertEndsWith(String usedFile, String destPath)
    {
        assertTrue("\nExpected: " + destPath + "\nActual: "
                + usedFile, usedFile.endsWith(destPath));
    }
}
