/*
 *   Copyright (C) 2007-2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.tools.utests;

import junit.framework.TestCase;

/**
 * Simple unit tests of the ome.tools.RepositoryTask utility.
 *
 * @author David L. Whitehurst &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:david@glencoesoftware.com">david@glencoesoftware.com</a>
 * @since 3.0
 */
public class RepositoryTaskTest extends TestCase {
	
//	RepositoryTask task = null;
//	private OmeroContext ctx;
//	private SimpleJdbcTemplate template;
//	private static int setupCount = 0;
//	
//	@Configuration(beforeTestMethod=true)
//    protected void setUp() throws Exception {
//		setupCount++;
//		
//		/**
//		 * run this once only, set to 1 to create files and add rows to
//		 * the eventlog table
//		 */
//		
//		if (setupCount == 1) {
//	        addTestFiles();
//	        //addTestRows();
//		}
//		task = new RepositoryTask();
//    }
//    @Configuration(afterTestMethod=true)
//    protected void tearDown() throws Exception {
//        task = null;
//    }
//	
//	@Test
//    public void testGetFileIds() throws Exception {
//		List<Long> ids = task.getFileIds();
//		assertTrue(ids.size() == 2);
//}	
//
//	@Test
//    public void testGetPixelIds() throws Exception {
//		List<Long> ids = task.getPixelIds();
//		assertTrue(ids.size() == 596);
//	}	
//	
//	@Test
//    public void testGetThumbnailIds() throws Exception {
//		List<Long> ids = task.getThumbnailIds();
//		assertTrue(ids.size() == 617);
//	}	
//
//	/**
//	 * private utility method to create files for deleting
//	 *
//	 */
//	private void addTestFiles() {
//		// create empty files for tests
//		File origFile1, origFile2, pixelFile1, pixelFile2, thumbnailFile1;
//
//		origFile1 = new File("/OMERO/Files/63");
//		origFile2 = new File("/OMERO/Files/100");
//		pixelFile1 = new File("/OMERO/Pixels/Dir-002/2613");
//		pixelFile2 = new File("/OMERO/Pixels/Dir-004/4189");
//		thumbnailFile1 = new File("/OMERO/Thumbnails/Dir-003/3331");
//
//		try {
//			if (!origFile1.exists()) {
//				origFile1.createNewFile();
//			}
//			if (!origFile2.exists()) {
//				origFile2.createNewFile();
//			}
//			
//			if (!pixelFile1.exists()) {
//				pixelFile1.createNewFile();
//			}
//			if (!pixelFile2.exists()) {
//				pixelFile2.createNewFile();
//			}
//
//			if (!thumbnailFile1.exists()) {
//				thumbnailFile1.createNewFile();
//			}
//			
//		} catch (IOException ioex) {
//			ioex.printStackTrace();
//		}
//	}
//	
//	private void addTestRows() {
//		ctx = OmeroContext.getManagedServerContext();
//		template = (SimpleJdbcTemplate) ctx.getBean("simpleJdbcTemplate");
//		
//		String[] inserts = {
//				"insert into eventlog (id, entityid, action, entitytype, permissions, event) values (99000,63,'DELETE','ome.model.core.OriginalFile',-1,999)",
//				"insert into eventlog (id, entityid, action, entitytype, permissions, event) values (99001,100,'DELETE','ome.model.core.OriginalFile',-1,999)",
//				"insert into eventlog (id, entityid, action, entitytype, permissions, event) values (99002,2613,'DELETE','ome.model.core.Pixels',-1,999)",
//				"insert into eventlog (id, entityid, action, entitytype, permissions, event) values (99003,4189,'DELETE','ome.model.core.Pixels',-1,999)",
//				"insert into eventlog (id, entityid, action, entitytype, permissions, event) values (99004,3331,'DELETE','ome.model.display.Thumbnail',-1,999)",
//		};
//		
//		for (int i=0; i<inserts.length; i++) {
//			template.getJdbcOperations().execute(inserts[i]);
//		}
//	}
}
