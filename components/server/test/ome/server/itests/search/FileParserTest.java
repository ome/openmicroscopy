/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.search;

import java.io.File;
import java.io.Reader;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ome.api.Search;
import ome.model.annotations.FileAnnotation;
import ome.model.core.Image;
import ome.model.core.OriginalFile;
import ome.services.fulltext.FileParser;
import ome.testing.FileUploader;

import org.springframework.util.ResourceUtils;
import org.testng.annotations.Test;

@Test(groups = { "query", "fulltext", "fileparser" })
public class FileParserTest extends AbstractTest {

    @Test
    public void testStringFromParser() throws Exception {
        final String str = UUID.randomUUID().toString();
        FileUploader upload = new FileUploader(this.factory, str, "uuid",
                "/dev/null");
        upload.run();

        String path = getFileService().getFilesPath(upload.getId());
        File file = new File(path);
        FileParser fp = new FileParser();
        fp.setApplicationContext(this.applicationContext);
        StringWriter sw = new StringWriter();
        for (Reader test : fp.parse(file)) {
            while (test.ready()) {
                sw.write(test.read());
            }
        }
        assertEquals(str, sw.toString());
    }

    @Test()
    public void testCreateFile() throws Exception {

        // Test data
        final String str = UUID.randomUUID().toString();

        Map<String, FileParser> parsers = new HashMap<String, FileParser>();
        parsers.put("text/plain", new FileParser());
        parsers.get("text/plain")
                .setApplicationContext(this.applicationContext);

        // Upload
        FileUploader upload = new FileUploader(this.factory, str, "uuid",
                "/dev/null");
        try {
            upload.run();
        } catch (Exception e) {
            // This seems to be throwing an exception
            // when run in the server
        }

        i = new_Image();
        i.setName("annotated with file");
        FileAnnotation fa = new FileAnnotation();
        fa.setNs("");
        fa.setFile(new OriginalFile(upload.getId(), false));
        i.linkAnnotation(fa);
        i = iUpdate.saveAndReturnObject(i);
        iUpdate.indexObject(i);

        loginRootKeepGroup();
        List<Image> imgs = iQuery.findAllByFullText(Image.class, str, null);
        assertEquals(1, imgs.size());
        assertTrue(imgs.get(0).getId().equals(i.getId()));

    }

    @Test()
    public void testPdfFile() throws Exception {

        // Test data

        // Upload
        File file = ResourceUtils
                .getFile("classpath:ome/server/utests/fileparsers/ABC123.pdf");
        FileUploader upload = new FileUploader(this.factory, file);
        upload.setFormat("application/pdf");
        try {
            upload.run();
        } catch (Exception e) {
            // This seems to be throwing an exception
            // when run in the server
        }

        i = new_Image();
        i.setName("annotated"); // Don't put ABC123 here.
        FileAnnotation fa = new FileAnnotation();
        fa.setFile(new OriginalFile(upload.getId(), false));
        i.linkAnnotation(fa);
        i = iUpdate.saveAndReturnObject(i);
        iUpdate.indexObject(i);

        loginRoot();
        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);
        search.onlyIds(i.getId());
        search.byFullText("file.contents:ABC123");
        assertTrue(search.hasNext());
    }

    private Image new_Image() {
        Image i = new Image();
        i.setAcquisitionDate(new Timestamp(0));
        return i;
    }

}
