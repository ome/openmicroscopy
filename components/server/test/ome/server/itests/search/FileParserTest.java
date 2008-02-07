/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.search;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ome.model.annotations.FileAnnotation;
import ome.model.core.Image;
import ome.model.core.OriginalFile;
import ome.services.fulltext.FileParser;
import ome.services.fulltext.FullTextBridge;
import ome.services.fulltext.FullTextIndexer;
import ome.services.fulltext.FullTextThread;
import ome.testing.FileUploader;

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
        for (String test : fp.parse(file)) {
            assertEquals(test, str);
        }
    }

    @Test()
    public void testCreateFile() throws Exception {

        // Test data
        final String str = UUID.randomUUID().toString();

        Map<String, FileParser> parsers = new HashMap<String, FileParser>();
        parsers.put("text/plain", new FileParser());

        // Upload
        FileUploader upload = new FileUploader(this.factory, str, "uuid",
                "/dev/null");
        try {
            upload.run();
        } catch (Exception e) {
            // This seems to be throwing an exception
            // when run in the server
        }

        i = new Image();
        i.setName("annotated with file");
        FileAnnotation fa = new FileAnnotation();
        fa.setName("");
        fa.setFile(new OriginalFile(upload.getId(), false));
        i.linkAnnotation(fa);
        i = iUpdate.saveAndReturnObject(i);

        CreationLogLoader logs = new CreationLogLoader(i);
        ftb = new FullTextBridge(getFileService(), parsers);
        fti = new FullTextIndexer(logs);
        ftt = new FullTextThread(getManager(), getExecutor(), fti, ftb);
        ftt.run();

        loginRoot();
        List<Image> imgs = iQuery.findAllByFullText(Image.class, str, null);
        assertTrue(imgs.size() == 1);
        assertTrue(imgs.get(0).getId().equals(i.getId()));

    }

}
