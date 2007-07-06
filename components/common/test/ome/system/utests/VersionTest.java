/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.system.utests;

import java.text.SimpleDateFormat;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.system.Version;

import org.testng.annotations.Test;

import junit.framework.TestCase;

@Test
public class VersionTest extends TestCase {

	public void testGetVersionFromBundle() throws Exception {
		String versionStr = Version.OMERO;
		assertNotNull(versionStr);
	}
	
	@Test( groups = {"svnonly","ignore"})
    public void testGetSvnKeyword() throws Exception {
        String versionStr = Version.stringFromSvnString("$Revision$");
        Integer version = Integer.valueOf(versionStr);
        checkVersion(version);
    }

	@Test( groups = {"svnonly","ignore"})
    public void testGetBlankRevision() throws Exception {
        Integer version = Version.getRevision(Blank.class);
        checkVersion(version);
    }

	@Test( groups = {"svnonly","ignore"})
    public void testGetBlankDate() throws Exception {
        assertNotNull(Version.getDate(Blank.class));
    }

	@Test( groups = {"svnonly","ignore"})
    public void testBlankSvnDateFormat() throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat(
                Version.SVN_DATE_FORMAT);
        String dateStr = Blank.class.getAnnotation(RevisionDate.class).value();
        java.util.Date date = formatter.parse(Version
                .stringFromSvnString(dateStr));
    }
    
    public void testGetDummyRevision() throws Exception {
        Integer version = Version.getRevision(Dummy.class);
        checkVersion(version);
    }

    public void testGetDummyDate() throws Exception {
        assertNotNull(Version.getDate(Dummy.class));
    }

    public void testDummySvnDateFormat() throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat(
                Version.SVN_DATE_FORMAT);
        String dateStr = Dummy.class.getAnnotation(RevisionDate.class).value();
        java.util.Date date = formatter.parse(Version
                .stringFromSvnString(dateStr));
    }
    
    
    // ~ Helpers
    // =========================================================================
    
    private void checkVersion(Integer version) {
        assertNotNull(version);
        assertTrue(0 < version.intValue());
    }
}

@RevisionNumber("$Revision$")
@RevisionDate("$Date$")
class Blank {
}

@RevisionDate("$Date: 1970-01-01 00:00:01 +0100 (Thu, 01 Jan 1970) $")
@RevisionNumber("$Revision: 1 $")
class Dummy {
}
