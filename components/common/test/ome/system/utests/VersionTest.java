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

    public void testGetSvnKeyword() throws Exception {
        String versionStr = Version.stringFromSvnString("$Revision$");
        Integer version = Integer.valueOf(versionStr);
        checkVersion(version);
    }

    public void testGetRevision() throws Exception {
        Integer version = Version.getRevision(Blank.class);
        checkVersion(version);
    }

    public void testGetDate() throws Exception {
        assertNotNull(Version.getDate(Blank.class));
    }

    public void testSvnDateFormat() throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat(
                Version.SVN_DATE_FORMAT);
        String dateStr = Blank.class.getAnnotation(RevisionDate.class).value();
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
