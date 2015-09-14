/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.itests.search;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ome.api.Search;
import ome.model.annotations.FileAnnotation;
import ome.model.core.Image;
import ome.model.core.OriginalFile;
import ome.services.fulltext.FileParser;
import ome.services.util.Executor;
import ome.system.ServiceFactory;
import ome.server.itests.FileUploader;

import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;
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

        final String path = getFileService().getFilesPath(upload.getId());
        final File file = new File(path);
        final FileParser fp = new FileParser();
        fp.setApplicationContext(this.applicationContext);
        final StringWriter sw = new StringWriter();

        // Has to be run in an executor in order to register the cleanup
        // Though we could also pass in a mock.
        executor.execute(loginAop.p, new Executor.SimpleWork(this, "parse") {
            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                for (Reader test : fp.parse(file)) {
                    try {
                        while (test.ready()) {
                            sw.write(test.read());
                        }
                    } catch (IOException ioe) {
                        throw new RuntimeException(ioe);
                    }
                }
                return null;
            }});

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
        upload.setMimetype("application/pdf");
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

    @Test(groups = "ticket:2098")
    public void testParseMissingFile() {

        String uuid = uuid();
        OriginalFile f = new OriginalFile();
        f.setName(uuid);
        f.setMimetype("text/plain");
        f.setPath("/tmp/empty");

        i = new_Image();
        i.setName("annotated with file");
        FileAnnotation fa = new FileAnnotation();
        fa.setNs("");
        fa.setFile(f);
        i.linkAnnotation(fa);
        i = iUpdate.saveAndReturnObject(i);
        iUpdate.indexObject(i);

        loginRootKeepGroup();
        List<Image> imgs = iQuery.findAllByFullText(Image.class, uuid, null);
        assertEquals(1, imgs.size());
        assertTrue(imgs.get(0).getId().equals(i.getId()));
    }

    private Image new_Image() {
        Image i = new Image();
        return i;
    }

}
