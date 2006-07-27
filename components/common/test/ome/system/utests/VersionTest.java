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
		assertEquals(
				"100",
				Version.stringFromSvnString("$Revision: 100 $"));
	}
	
	public void testGetRevision() throws Exception {
		assertEquals(
				(Integer)100,
				(Integer)Version.getRevision(Blank.class));
	}
	
	public void testGetDate() throws Exception {
		assertNotNull(Version.getDate(Blank.class));
	}
	
	public void testSvnDateFormat() throws Exception
	{
		SimpleDateFormat formatter = new SimpleDateFormat(Version.SVN_DATE_FORMAT);
		String dateStr = Blank.class.getAnnotation(RevisionDate.class).value();
		java.util.Date date = formatter.parse(Version.stringFromSvnString(dateStr));
	}
}

@RevisionNumber("$Revision: 100 $")
@RevisionDate("$Date: 2002-07-22 21:42:37 -0700 (Mon, 22 Jul 2002) $")
class Blank {}
