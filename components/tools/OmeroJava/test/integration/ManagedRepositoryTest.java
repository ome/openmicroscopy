/*
 * $Id$
 *
 *   Copyright 2012 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.util.Utils;

import omero.LockTimeout;
import omero.ServerError;
import omero.api.RawFileStorePrx;
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
import omero.model.IObject;
import omero.model.OriginalFile;
import omero.sys.EventContext;

/**
* Collections of tests for the <code>ManagedRepository</code> service.
*
* @author Colin Blackburn &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:c.blackburn@dundee.ac.uk">c.blackburn@dundee.ac.uk</a>
*/
@Test(groups = {"integration", "fs"})
public class ManagedRepositoryTest
   extends AbstractServerTest
{

	/** Reference to the managed repository. */
	ManagedRepositoryPrx repo;

	/** Description object representing this repository */
	OriginalFile obj;

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
	 * Construct a path from its elements
	 *
	 * @param pathElements Array containing elements of a path.
	 */
	String buildPath(String[] pathElements)
	{
	    String path = "";
	    for (String element : pathElements) {
	        path = FilenameUtils.concat(path, element);
	    }
	    return path;
	}

	// Primarily to get code compiling during the major refactoring
	ImportLocation importFileset(List<String> srcPaths) throws Exception {

	    // Setup that should be easier, mostl likey be a single ctor on IL
	    OMEROMetadataStoreClient client = new OMEROMetadataStoreClient();
	    client.initialize(this.client);
	    OMEROWrapper wrapper = new OMEROWrapper(new ImportConfig());
	    ImportLibrary lib = new ImportLibrary(client, wrapper);

	    // This should also be simplified.
	    ImportContainer container = new ImportContainer(new File(srcPaths.get(0)),
	            null /*target*/, null /*user pixels */, "FakeReader",
	            srcPaths.toArray(new String[0]), false /*isspw*/);

	    // Now actually use the library.
	    ImportProcessPrx proc = lib.createImport(container);
	    
	    // The following is largely a copy of ImportLibrary.importImage
        final String[] srcFiles = container.getUsedFiles();
        final List<String> checksums = new ArrayList<String>();
        final MessageDigest md = Utils.newSha1MessageDigest();
        final byte[] buf = new byte[omero.constants.MESSAGESIZEMAX.value/8];  // 8 MB buffer

        for (int i = 0; i < srcFiles.length; i++) {
            checksums.add(lib.uploadFile(proc, srcFiles, i, md, buf));
        }

        // At this point the import is running, check handle for number of
        // steps.
        final HandlePrx handle = proc.verifyUpload(checksums);
        final ImportRequest req = (ImportRequest) handle.getRequest();
        final Fileset fs = req.activity.getParent();
        final CmdCallbackI cb = lib.createCallback(proc, handle, container);
        cb.loop(60*60, 1000); // Wait 1 hr per step.
        final ImportResponse rsp = lib.getImportResponse(cb, container, fs);
        return req.location;
	}

	// This should not really be necessary, since it's done by the above!
	RawFileStorePrx uploadUsedFile(ImportLocation data, String file) {
	    fail("NYI");
	    return null;
	}

	/**
	 * Test that the expected repository path is returned
	 * for a single image file if new or already uploaded.
	 *
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
	public void testGetCurrentRepoDirSimple()
	        throws Exception
	{
	    List<String> srcPaths = new ArrayList<String>();
	    String destPath;

	    String uniquePath = UUID.randomUUID().toString();
	    String file1 = UUID.randomUUID().toString() + ".dv";
	    String file2 = UUID.randomUUID().toString() + ".dv";

	    // Completely new file
	    String[] src = {uniquePath, file1};
	    srcPaths.add(buildPath(src));
	    String[] dest = {uniquePath, file1};
	    destPath = buildPath(dest);
	    ImportLocation data = importFileset(srcPaths);
		assertContains(data.usedFiles.get(0), destPath);
		touch(uploadUsedFile(data, data.usedFiles.get(0)));

        // Different file that should go in existing directory
	    src[1] = file2;
	    srcPaths.set(0, buildPath(src));
	    dest[1] = file2;
	    destPath = buildPath(dest);
	    data = importFileset(srcPaths);
		assertContains(data.usedFiles.get(0), destPath);
		touch(uploadUsedFile(data, data.usedFiles.get(0)));

        // Same file that should go in new directory
	    dest[0] = uniquePath + "-1";
	    destPath = buildPath(dest);
	    data = importFileset(srcPaths);
		assertContains(data.usedFiles.get(0), destPath);
		touch(uploadUsedFile(data, data.usedFiles.get(0)));

        // Same file again that should go in new directory
	    dest[0] = uniquePath + "-2";
	    destPath = buildPath(dest);
	    data = importFileset(srcPaths);
		assertContains(data.usedFiles.get(0), destPath);
	}

	/**
	 * Test that the expected repository path is returned
	 * for multiple files if new or already uploaded.
	 *
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
	public void testGetCurrentRepoDirMultipleFiles()
	        throws Exception
	{
	    List<String> srcPaths = new ArrayList<String>();
	    List<String> destPaths = new ArrayList<String>();

	    String uniquePath = UUID.randomUUID().toString();
	    String file1 = UUID.randomUUID().toString() + ".dv";
	    String file2 = file1 + ".log";
	    String file3 = UUID.randomUUID().toString() + ".dv";
	    String file4 = file3 + ".log";

	    // Completely new files
	    String[] src = {uniquePath, file1};
	    srcPaths.add(buildPath(src));
	    src[1] = file2;
	    srcPaths.add(buildPath(src));
	    String[] dest = {uniquePath, file1};
	    destPaths.add(buildPath(dest));
	    dest[1] = file2;
	    destPaths.add(buildPath(dest));
	    ImportLocation data = importFileset(srcPaths);
	    assertTrue(data.usedFiles.size()==destPaths.size());
	    for (int i=0; i<data.usedFiles.size(); i++) {
            assertContains(data.usedFiles.get(i), destPaths.get(i));
		    touch(uploadUsedFile(data, data.usedFiles.get(i)));
	    }

        // One identical file both should go in a new directory
	    src[1] = file1;
	    srcPaths.set(0, buildPath(src));
	    src[1] = file4;
	    srcPaths.set(1, buildPath(src));
        dest[0] = uniquePath + "-1";
	    dest[1] = file1;
	    destPaths.set(0, buildPath(dest));
	    dest[1] = file4;
	    destPaths.set(1, buildPath(dest));
	    data = importFileset(srcPaths);
	    assertTrue(data.usedFiles.size()==destPaths.size());
	    for (int i=0; i<data.usedFiles.size(); i++) {
            assertContains(data.usedFiles.get(i), destPaths.get(i));
		    touch(uploadUsedFile(data, data.usedFiles.get(i)));
	    }

        // Two different files that should go in existing directory
	    src[1] = file3;
	    srcPaths.set(0, buildPath(src));
	    src[1] = file4;
	    srcPaths.set(1, buildPath(src));
        dest[0] = uniquePath;
	    dest[1] = file3;
	    destPaths.set(0, buildPath(dest));
	    dest[1] = file4;
	    destPaths.set(1, buildPath(dest));
	    data = importFileset(srcPaths);
	    assertTrue(data.usedFiles.size()==destPaths.size());
	    for (int i=0; i<data.usedFiles.size(); i++) {
            assertContains(data.usedFiles.get(i), destPaths.get(i));
		    touch(uploadUsedFile(data, data.usedFiles.get(i)));
	    }

        // Two identical files that should go in a new directory
        dest[0] = uniquePath + "-2";
	    dest[1] = file3;
	    destPaths.set(0, buildPath(dest));
	    dest[1] = file4;
	    destPaths.set(1, buildPath(dest));
	    data = importFileset(srcPaths);
	    assertTrue(data.usedFiles.size()==destPaths.size());
	    for (int i=0; i<data.usedFiles.size(); i++) {
            assertContains(data.usedFiles.get(i), destPaths.get(i));
		    touch(uploadUsedFile(data, data.usedFiles.get(i)));
	    }
    }

	/**
	 * Test that the expected repository path is returned
	 * for multiple nested files if new or already uploaded.
	 *
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
	public void testGetCurrentRepoDirNested()
	        throws Exception
	{
	    List<String> srcPaths = new ArrayList<String>();
	    List<String> destPaths = new ArrayList<String>();

	    String uniquePath = UUID.randomUUID().toString();
	    String subDir = "sub";
	    String subSubDir = "subsub";
	    String file1 = UUID.randomUUID().toString() + ".mdb";
	    String file2 = UUID.randomUUID().toString() + ".tif";
	    String file3 = UUID.randomUUID().toString() + ".tif";

	    // Completely new files
	    String[] src = {uniquePath, file1};
	    srcPaths.add(buildPath(src));
	    src[1] = subDir;
	    src = (String[]) ArrayUtils.add(src, file2);
	    srcPaths.add(buildPath(src));
	    src[2] = subSubDir;
	    src = (String[]) ArrayUtils.add(src,file3);
	    srcPaths.add(buildPath(src));
	    String[] dest = {uniquePath, file1};
	    destPaths.add(buildPath(dest));
	    dest[1] = subDir;
	    dest = (String[]) ArrayUtils.add(dest,file2);
	    destPaths.add(buildPath(dest));
	    dest[2] = subSubDir;
	    dest = (String[]) ArrayUtils.add(dest,file3);
	    destPaths.add(buildPath(dest));
	    ImportLocation data = importFileset(srcPaths);
	    assertTrue(data.usedFiles.size()==destPaths.size());
	    for (int i=0; i<data.usedFiles.size(); i++) {
            assertContains(data.usedFiles.get(i), destPaths.get(i));
		    touch(uploadUsedFile(data, data.usedFiles.get(i)));
	    }

	    // Same files should go into new directory
	    destPaths.clear();
	    String[] dest2 = {uniquePath + "-1", file1};
	    destPaths.add(buildPath(dest2));
	    dest2[1] = subDir;
	    dest2 = (String[]) ArrayUtils.add(dest2,file2);
	    destPaths.add(buildPath(dest2));
	    dest2[2] = subSubDir;
	    dest2 = (String[]) ArrayUtils.add(dest2,file3);
	    destPaths.add(buildPath(dest2));
	    data = importFileset(srcPaths);
	    assertTrue(data.usedFiles.size()==destPaths.size());
	    for (int i=0; i<data.usedFiles.size(); i++) {
            assertContains(data.usedFiles.get(i), destPaths.get(i));
		    touch(uploadUsedFile(data, data.usedFiles.get(i)));
	    }
	}

	/**
	 * Test that
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
	public void testDeleteUploadedFileSimple()
	        throws Exception
	{
	    List<String> srcPaths = new ArrayList<String>();

	    String uniquePath = UUID.randomUUID().toString();
	    String file1 = UUID.randomUUID().toString() + ".dv";

	    String[] src = {uniquePath, file1};
	    srcPaths.add(buildPath(src));
	    ImportLocation data = importFileset(srcPaths);
		touch(uploadUsedFile(data, data.usedFiles.get(0)));
		for (String path : data.usedFiles) {
		    assertFileExists("Upload failed. File does not exist: ", path);
		}

		assertDeletePaths(data);

		for (String path : data.usedFiles) {
		    assertFileDoesNotExist("Delete failed. File not deleted: ", path);
		}
	}

	/**
	 * Test that
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
	public void testDeleteUploadedMultipleFilesSimple()
	        throws Exception
	{
	    List<String> srcPaths = new ArrayList<String>();

	    String uniquePath = UUID.randomUUID().toString();
	    String file1 = UUID.randomUUID().toString() + ".dv";
	    String file2 = file1 + ".dv.log";

	    String[] src = {uniquePath, file1};
	    srcPaths.add(buildPath(src));
	    src[1] = file2;
	    srcPaths.add(buildPath(src));
	    ImportLocation data = importFileset(srcPaths);
		for (String path : data.usedFiles) {
		    touch(uploadUsedFile(data, path));
		    assertFileExists("Upload failed. File does not exist: ", path);
		}

		assertDeletePaths(data);

		for (String path : data.usedFiles) {
		    assertFileDoesNotExist("Delete failed. File not deleted: ", path);
		}
	}

	/**
	 * Test that
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
	public void testDeleteUploadedPartialFiles()
	        throws Exception
	{
	    List<String> srcPaths = new ArrayList<String>();

	    String uniquePath = UUID.randomUUID().toString();
	    String file1 = UUID.randomUUID().toString() + ".dv";
	    String file2 = file1 + ".dv.log";

	    String[] src = {uniquePath, file1};
	    srcPaths.add(buildPath(src));
	    src[1] = file2;
	    srcPaths.add(buildPath(src));
	    ImportLocation data = importFileset(srcPaths);
		touch(uploadUsedFile(data, data.usedFiles.get(0)));
		assertFileExists("Upload failed. File does not exist: ", data.usedFiles.get(0));
		assertFileDoesNotExist("Something wrong. File does exist!: ", data.usedFiles.get(1));

		// Non-existent file should be silently ignored.
		assertDeletePaths(data);

		for (String path : data.usedFiles) {
		    assertFileDoesNotExist("Delete failed. File not deleted: ", path);
		}
	}

	/**
	 * Test that
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
	public void testDeleteUploadedMultipleFilesNested()
	        throws Exception
	{
	    List<String> srcPaths = new ArrayList<String>();

	    String uniquePath = UUID.randomUUID().toString();
	    String subDir = "sub";
	    String subSubDir = "subsub";
	    String file1 = UUID.randomUUID().toString() + ".mdb";
	    String file2 = UUID.randomUUID().toString() + ".tif";
	    String file3 = UUID.randomUUID().toString() + ".tif";

	    String[] src = {uniquePath, file1};
	    srcPaths.add(buildPath(src));
	    src[1] = subDir;
	    src = (String[]) ArrayUtils.add(src, file2);
	    srcPaths.add(buildPath(src));
	    src[2] = subSubDir;
	    src = (String[]) ArrayUtils.add(src,file3);
	    srcPaths.add(buildPath(src));

	    ImportLocation data = importFileset(srcPaths);
		for (String path : data.usedFiles) {
		    touch(uploadUsedFile(data, path));
		    assertFileExists("Upload failed. File does not exist: ", path);
		}

		assertDeletePaths(data);

		for (String path : data.usedFiles) {
		    assertFileDoesNotExist("Delete failed. File not deleted: ", path);
		}
	}

	/**
	 * Test that
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
	public void testDeleteUploadedMultipleSetsDeleteOneSet()
	        throws Exception
	{
	    List<String> srcPaths = new ArrayList<String>();

	    String uniquePath = UUID.randomUUID().toString();
	    String file1 = UUID.randomUUID().toString() + ".dv";
	    String file2 = file1 + ".dv.log";

	    String[] src = {uniquePath, file1};
	    srcPaths.add(buildPath(src));
	    src[1] = file2;
	    srcPaths.add(buildPath(src));
	    ImportLocation data1 = importFileset(srcPaths);

	    srcPaths.clear();
	    file1 = UUID.randomUUID().toString() + ".dv";
	    file2 = file1 + ".dv.log";
	    src[1] = file1;
	    srcPaths.add(buildPath(src));
	    src[1] = file2;
	    srcPaths.add(buildPath(src));
	    ImportLocation data2 = importFileset(srcPaths);

		for (String path : data1.usedFiles) {
		    touch(uploadUsedFile(data1, path));
		    assertFileExists("Upload failed. File does not exist: ", path);
		}
		for (String path : data2.usedFiles) {
		    touch(uploadUsedFile(data2, path));
		    assertFileExists("Upload failed. File does not exist: ", path);
		}
		assertDeletePaths(data1);
		
		// This set should be gone
		for (String path : data1.usedFiles) {
		    assertFileDoesNotExist("Delete failed. File not deleted: ", path);
		}
		// This set should be still there
		for (String path : data2.usedFiles) {
		    assertFileExists("Delete failed. File deleted!: ", path);
		}
	}

    protected void assertDeletePaths(ImportLocation data1) throws ServerError,
            InterruptedException, LockTimeout {
        HandlePrx handle = repo.deletePaths(
		        data1.usedFiles.toArray(new String[data1.usedFiles.size()]),
		        false, false);
		CmdCallbackI cb = new CmdCallbackI(client, handle);
		cb.loop(5, 100);
		assertCmd(cb, true);
    }

    private void assertContains(String usedFile, String destPath)
    {
        assertTrue("\nExpected :" + destPath + "\nActual  :"
                + usedFile, usedFile.contains(destPath));
    }

    private void touch(RawFileStorePrx prx) throws ServerError
	{
	    try
	    {
	        prx.write(new byte[]{0}, 0, 1);
	    }
	    finally
	    {
	        if (prx != null)
	        {
	            prx.close();
	        }
	    }
	}
}
