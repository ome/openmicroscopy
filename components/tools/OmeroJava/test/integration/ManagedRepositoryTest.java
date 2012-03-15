/*
 * $Id$
 *
 *   Copyright 2012 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.io.File;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ome.formats.OMEROMetadataStoreClient;
import omero.ApiUsageException;
import omero.ResourceError;
import omero.ServerError;
import omero.api.RawFileStorePrx;
import omero.grid.RepositoryMap;
import omero.grid.RepositoryPrx;
import omero.model.OriginalFile;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import pojos.FileAnnotationData;

/** 
* Collections of tests for the <code>ManagedRepository</code> service.
*
* @author Colin Blackburn &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:c.blackburn@dundee.ac.uk">c.blackburn@dundee.ac.uk</a>
*/
@Test(groups = {"integration"})
public class ManagedRepositoryTest 
   extends AbstractServerTest
{
	
    // FIXME: The repository itself should alread include this path element
    private final static String MANAGED_REPO_PATH = "ManagedRepository";

	/** Reference to the managed repository. */
	RepositoryPrx repo;

	/** Managed repository directory. */
	private String repoDir; 

	/** Managed repository template. */
	private String template; 

	/**
	 * Set the data directory for the tests. This is needed to find the 
	 * correct repository to test whether deletes have been successful.
	 */
	@BeforeClass
	public void setRepoAndRepoDir() throws Exception {
		String dataDir = root.getSession().getConfigService().getConfigValue(
		        "omero.data.dir");
		template = root.getSession().getConfigService().getConfigValue("omero.fslite.path");
	    EventContext ec = iAdmin.getEventContext();
		repoDir = FilenameUtils.concat(dataDir, MANAGED_REPO_PATH);
		// Force user prefix for now: see PublicRepositoryI.java
        repoDir = FilenameUtils.concat(repoDir, ec.userName);
        String dir;
        String[] elements = template.split("/");
        for (String part : elements) {
            String[] subelements = part.split("-");
            dir = getStringFromToken(subelements[0]);
            for (int i = 1; i < subelements.length; i++) {
                dir = dir + "-" + getStringFromToken(subelements[i]);
            }
            repoDir = FilenameUtils.concat(repoDir, dir);
        }

		repo = null;
		RepositoryMap rm = factory.sharedResources().repositories();
		int repoCount = 0;
		String s = dataDir;
		for (OriginalFile desc: rm.descriptions) {
			String repoPath = desc.getPath().getValue() + 
			desc.getName().getValue();
			s += "\nFound repository:" + desc.getPath().getValue() + 
			        desc.getName().getValue();
			if (FilenameUtils.equals(
					FilenameUtils.normalizeNoEndSeparator(dataDir),
					FilenameUtils.normalizeNoEndSeparator(repoPath))) {
				repo = rm.proxies.get(repoCount);
				break;
			}
			repoCount++;
		}
		if (repo == null) {
			throw new Exception("Unable to find legacy repository: " + s);
		}
	}

    // Helper method to provide a little more flexibility
    // when building a path from a template
    private String getStringFromToken(String token) {
        Calendar now = Calendar.getInstance();
        DateFormatSymbols dfs = new DateFormatSymbols();
        String rv;
        if (token.equals("%year%"))
            rv = Integer.toString(now.get(Calendar.YEAR));
        else if (token.equals("%month%"))
            rv = Integer.toString(now.get(Calendar.MONTH)+1);
        else if (token.equals("%monthname%"))
            rv = dfs.getMonths()[now.get(Calendar.MONTH)];
        else if (token.equals("%day%"))
            rv = Integer.toString(now.get(Calendar.DAY_OF_MONTH));
        else if (!token.endsWith("%") && !token.startsWith("%"))
            rv = token;
        else {
            log.warn("Ignored unrecognised token in template: " + token);
            rv = "";
        }
        return rv;
    }

	/**
	 * Makes sure that the OMERO file exists of the given name
	 * 
	 * @param path The absolute filename.
	 */
	void assertFileExists(String message, String path)
	        throws ServerError
	{   
		assertTrue(message + path, repo.fileExists(path));
	}

	/**
	 * Makes sure that the OMERO file exists of the given name
	 * 
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
	        throws ServerError
	{
	    String path = "";
	    for (String element : pathElements) {
	        path = FilenameUtils.concat(path, element);
	    }
	    return path;
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
	    EventContext ec = iAdmin.getEventContext();
	    List<String> srcPaths = new ArrayList<String>();
	    String destPath;
        List<String> repoPaths;

	    String uniquePath = UUID.randomUUID().toString();
	    String file1 = UUID.randomUUID().toString() + ".dv";
	    String file2 = UUID.randomUUID().toString() + ".dv";

	    // Completely new file
	    String[] src = {FilenameUtils.getPrefix(repoDir), ec.userName, uniquePath, file1};
	    srcPaths.add(buildPath(src));
	    String[] dest = {repoDir, uniquePath, file1};
	    destPath = buildPath(dest);
	    repoPaths = repo.getCurrentRepoDir(srcPaths);
		assertTrue("\nExpected :" + destPath + "\nActual  :" 
		        + repoPaths.get(0), destPath.equals(repoPaths.get(0)));
		repo.create(repoPaths.get(0));
        
        // Different file that should go in existing directory
	    src[3] = file2;
	    srcPaths.set(0, buildPath(src));
	    dest[2] = file2;
	    destPath = buildPath(dest);
	    repoPaths = repo.getCurrentRepoDir(srcPaths);
		assertTrue("\nExpected :" + destPath + "\nActual  :" 
		        + repoPaths.get(0), destPath.equals(repoPaths.get(0)));
		repo.create(repoPaths.get(0));

        // Same file that should go in new directory
	    dest[1] = uniquePath + "-1";
	    destPath = buildPath(dest);
	    repoPaths = repo.getCurrentRepoDir(srcPaths);
		assertTrue("\nExpected :" + destPath + "\nActual  :" 
		        + repoPaths.get(0), destPath.equals(repoPaths.get(0)));
		repo.create(repoPaths.get(0));

        // Same file again that should go in new directory
	    dest[1] = uniquePath + "-2";
	    destPath = buildPath(dest);
	    repoPaths = repo.getCurrentRepoDir(srcPaths);
		assertTrue("\nExpected :" + destPath + "\nActual  :" 
		        + repoPaths.get(0), destPath.equals(repoPaths.get(0)));
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
	    EventContext ec = iAdmin.getEventContext();
	    List<String> srcPaths = new ArrayList<String>();
	    List<String> destPaths = new ArrayList<String>();
        List<String> repoPaths;

	    String uniquePath = UUID.randomUUID().toString();
	    String file1 = UUID.randomUUID().toString() + ".dv";
	    String file2 = file1 + ".log";
	    String file3 = UUID.randomUUID().toString() + ".dv";
	    String file4 = file3 + ".log";

	    // Completely new files
	    String[] src = {FilenameUtils.getPrefix(repoDir), ec.userName, uniquePath, file1};
	    srcPaths.add(buildPath(src));
	    src[3] = file2;
	    srcPaths.add(buildPath(src));
	    String[] dest = {repoDir, uniquePath, file1};
	    destPaths.add(buildPath(dest));
	    dest[2] = file2;
	    destPaths.add(buildPath(dest));
	    repoPaths = repo.getCurrentRepoDir(srcPaths);
	    assertTrue(repoPaths.size()==destPaths.size());
	    for (int i=0; i<repoPaths.size(); i++) {
		    assertTrue("\nExpected :" + destPaths.get(i) + "\nActual  :" + repoPaths.get(i),
		            destPaths.get(i).equals(repoPaths.get(i)));
		    repo.create(repoPaths.get(i));
	    }

        // One identical file both should go in a new directory
	    src[3] = file1;
	    srcPaths.set(0, buildPath(src));
	    src[3] = file4;
	    srcPaths.set(1, buildPath(src));
        dest[1] = uniquePath + "-1";
	    dest[2] = file1;
	    destPaths.set(0, buildPath(dest));
	    dest[2] = file4;
	    destPaths.set(1, buildPath(dest));
	    repoPaths = repo.getCurrentRepoDir(srcPaths);
	    assertTrue(repoPaths.size()==destPaths.size());
	    for (int i=0; i<repoPaths.size(); i++) {
		    assertTrue("\nExpected :" + destPaths.get(i) + "\nActual  :" + repoPaths.get(i),
		            destPaths.get(i).equals(repoPaths.get(i)));
		    repo.create(repoPaths.get(i));
	    }

        // Two different files that should go in existing directory
	    src[3] = file3;
	    srcPaths.set(0, buildPath(src));
	    src[3] = file4;
	    srcPaths.set(1, buildPath(src));
        dest[1] = uniquePath;
	    dest[2] = file3;
	    destPaths.set(0, buildPath(dest));
	    dest[2] = file4;
	    destPaths.set(1, buildPath(dest));
	    repoPaths = repo.getCurrentRepoDir(srcPaths);
	    assertTrue(repoPaths.size()==destPaths.size());
	    for (int i=0; i<repoPaths.size(); i++) {
		    assertTrue("\nExpected :" + destPaths.get(i) + "\nActual  :" + repoPaths.get(i),
		            destPaths.get(i).equals(repoPaths.get(i)));
		    repo.create(repoPaths.get(i));
	    }

        // Two identical files that should go in a new directory
        dest[1] = uniquePath + "-2";
	    dest[2] = file3;
	    destPaths.set(0, buildPath(dest));
	    dest[2] = file4;
	    destPaths.set(1, buildPath(dest));
	    repoPaths = repo.getCurrentRepoDir(srcPaths);
	    assertTrue(repoPaths.size()==destPaths.size());
	    for (int i=0; i<repoPaths.size(); i++) {
		    assertTrue("\nExpected :" + destPaths.get(i) + "\nActual  :" + repoPaths.get(i),
		            destPaths.get(i).equals(repoPaths.get(i)));
		    repo.create(repoPaths.get(i));
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
	    EventContext ec = iAdmin.getEventContext();
	    List<String> srcPaths = new ArrayList<String>();
	    List<String> destPaths = new ArrayList<String>();
        List<String> repoPaths;

	    String uniquePath = UUID.randomUUID().toString();
	    String subDir = "sub";
	    String subSubDir = "subsub";
	    String file1 = UUID.randomUUID().toString() + ".mdb";
	    String file2 = UUID.randomUUID().toString() + ".tif";
	    String file3 = UUID.randomUUID().toString() + ".tif";

	    // Completely new files
	    String[] src = {FilenameUtils.getPrefix(repoDir), ec.userName, uniquePath, file1};
	    srcPaths.add(buildPath(src));
	    src[3] = subDir;
	    src = (String[]) ArrayUtils.add(src, file2);
	    srcPaths.add(buildPath(src));
	    src[4] = subSubDir;
	    src = (String[]) ArrayUtils.add(src,file3);
	    srcPaths.add(buildPath(src));
	    String[] dest = {repoDir, uniquePath, file1};
	    destPaths.add(buildPath(dest));
	    dest[2] = subDir;
	    dest = (String[]) ArrayUtils.add(dest,file2);
	    destPaths.add(buildPath(dest));
	    dest[3] = subSubDir;
	    dest = (String[]) ArrayUtils.add(dest,file3);
	    destPaths.add(buildPath(dest));
	    repoPaths = repo.getCurrentRepoDir(srcPaths);
	    assertTrue(repoPaths.size()==destPaths.size());
	    for (int i=0; i<repoPaths.size(); i++) {
		    assertTrue("\nExpected :" + destPaths.get(i) + "\nActual  :" + repoPaths.get(i),
		            destPaths.get(i).equals(repoPaths.get(i)));
		    repo.create(repoPaths.get(i));
	    }

	    // Same files should go into new directory
	    destPaths.clear();
	    String[] dest2 = {repoDir, uniquePath + "-1", file1};
	    destPaths.add(buildPath(dest2));
	    dest2[2] = subDir;
	    dest2 = (String[]) ArrayUtils.add(dest2,file2);
	    destPaths.add(buildPath(dest2));
	    dest2[3] = subSubDir;
	    dest2 = (String[]) ArrayUtils.add(dest2,file3);
	    destPaths.add(buildPath(dest2));
	    repoPaths = repo.getCurrentRepoDir(srcPaths);
	    assertTrue(repoPaths.size()==destPaths.size());
	    for (int i=0; i<repoPaths.size(); i++) {
		    assertTrue("\nExpected :" + destPaths.get(i) + "\nActual  :" + repoPaths.get(i),
		            destPaths.get(i).equals(repoPaths.get(i)));
		    repo.create(repoPaths.get(i));
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
	    EventContext ec = iAdmin.getEventContext();
	    List<String> srcPaths = new ArrayList<String>();
        List<String> repoPaths;

	    String uniquePath = UUID.randomUUID().toString();
	    String file1 = UUID.randomUUID().toString() + ".dv";

	    String[] src = {FilenameUtils.getPrefix(repoDir), ec.userName, uniquePath, file1};
	    srcPaths.add(buildPath(src));
	    repoPaths = repo.getCurrentRepoDir(srcPaths);
		repo.create(repoPaths.get(0));
		for (String path : repoPaths) {
		    assertFileExists("Upload failed. File does not exist: ", path);
		}
		repo.deleteFiles((String[]) repoPaths.toArray(new String[repoPaths.size()]));
		for (String path : repoPaths) {
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
	    EventContext ec = iAdmin.getEventContext();
	    List<String> srcPaths = new ArrayList<String>();
        List<String> repoPaths;

	    String uniquePath = UUID.randomUUID().toString();
	    String file1 = UUID.randomUUID().toString() + ".dv";
	    String file2 = file1 + ".dv.log";

	    String[] src = {FilenameUtils.getPrefix(repoDir), ec.userName, uniquePath, file1};
	    srcPaths.add(buildPath(src));
	    src[3] = file2;
	    srcPaths.add(buildPath(src));
	    repoPaths = repo.getCurrentRepoDir(srcPaths);
		for (String path : repoPaths) {
		    repo.create(path);
		    assertFileExists("Upload failed. File does not exist: ", path);
		}
		repo.deleteFiles((String[]) repoPaths.toArray(new String[repoPaths.size()]));
		for (String path : repoPaths) {
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
	    EventContext ec = iAdmin.getEventContext();
	    List<String> srcPaths = new ArrayList<String>();
        List<String> repoPaths;

	    String uniquePath = UUID.randomUUID().toString();
	    String file1 = UUID.randomUUID().toString() + ".dv";
	    String file2 = file1 + ".dv.log";

	    String[] src = {FilenameUtils.getPrefix(repoDir), ec.userName, uniquePath, file1};
	    srcPaths.add(buildPath(src));
	    src[3] = file2;
	    srcPaths.add(buildPath(src));
	    repoPaths = repo.getCurrentRepoDir(srcPaths);
		repo.create(repoPaths.get(0));
		assertFileExists("Upload failed. File does not exist: ", repoPaths.get(0));
		assertFileDoesNotExist("Something wrong. File does exist!: ", repoPaths.get(1));

		// Non-existent file should be silently ignored.
		repo.deleteFiles((String[]) repoPaths.toArray(new String[repoPaths.size()]));
		for (String path : repoPaths) {
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
	    EventContext ec = iAdmin.getEventContext();
	    List<String> srcPaths = new ArrayList<String>();
        List<String> repoPaths;

	    String uniquePath = UUID.randomUUID().toString();
	    String subDir = "sub";
	    String subSubDir = "subsub";
	    String file1 = UUID.randomUUID().toString() + ".mdb";
	    String file2 = UUID.randomUUID().toString() + ".tif";
	    String file3 = UUID.randomUUID().toString() + ".tif";

	    String[] src = {FilenameUtils.getPrefix(repoDir), ec.userName, uniquePath, file1};
	    srcPaths.add(buildPath(src));
	    src[3] = subDir;
	    src = (String[]) ArrayUtils.add(src, file2);
	    srcPaths.add(buildPath(src));
	    src[4] = subSubDir;
	    src = (String[]) ArrayUtils.add(src,file3);
	    srcPaths.add(buildPath(src));

	    repoPaths = repo.getCurrentRepoDir(srcPaths);
		for (String path : repoPaths) {
		    repo.create(path);
		    assertFileExists("Upload failed. File does not exist: ", path);
		}
		repo.deleteFiles((String[]) repoPaths.toArray(new String[repoPaths.size()]));
		for (String path : repoPaths) {
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
	    EventContext ec = iAdmin.getEventContext();
	    List<String> srcPaths = new ArrayList<String>();
        List<String> repoPaths1, repoPaths2;

	    String uniquePath = UUID.randomUUID().toString();
	    String file1 = UUID.randomUUID().toString() + ".dv";
	    String file2 = file1 + ".dv.log";

	    String[] src = {FilenameUtils.getPrefix(repoDir), ec.userName, uniquePath, file1};
	    srcPaths.add(buildPath(src));
	    src[3] = file2;
	    srcPaths.add(buildPath(src));
	    repoPaths1 = repo.getCurrentRepoDir(srcPaths);

	    srcPaths.clear();
	    file1 = UUID.randomUUID().toString() + ".dv";
	    file2 = file1 + ".dv.log";
	    src[3] = file1;
	    srcPaths.add(buildPath(src));
	    src[3] = file2;
	    srcPaths.add(buildPath(src));
	    repoPaths2 = repo.getCurrentRepoDir(srcPaths);

		for (String path : repoPaths1) {
		    repo.create(path);
		    assertFileExists("Upload failed. File does not exist: ", path);
		}
		for (String path : repoPaths2) {
		    repo.create(path);
		    assertFileExists("Upload failed. File does not exist: ", path);
		}
		repo.deleteFiles((String[]) repoPaths1.toArray(new String[repoPaths1.size()]));
		// This set should be gone
		for (String path : repoPaths1) {
		    assertFileDoesNotExist("Delete failed. File not deleted: ", path);
		}
		// This set should be still there
		for (String path : repoPaths2) {
		    assertFileExists("Delete failed. File deleted!: ", path);
		}
	}

}
