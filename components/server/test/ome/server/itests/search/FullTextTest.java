/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.search;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.CommentAnnotation;
import ome.model.annotations.FileAnnotation;
import ome.model.annotations.TagAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.OriginalFile;
import ome.model.internal.Permissions;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.services.fulltext.EventLogLoader;
import ome.services.fulltext.FileParser;
import ome.services.fulltext.FullTextBridge;
import ome.services.fulltext.FullTextIndexer;
import ome.services.fulltext.FullTextThread;
import ome.services.fulltext.PersistentEventLogLoader;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.testing.FileUploader;

import org.hibernate.Session;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.testng.annotations.Test;

public class FullTextTest extends AbstractTest {

    @Test(enabled = false, groups = "manual")
    public void testIndexWholeDb() throws Exception {
        ome.services.fulltext.Main.indexFullDb();
    }

    @Test(enabled = true, groups = "manual")
    public void testCheckThatProcessStarts() {

        long start = System.currentTimeMillis();
        while ((System.currentTimeMillis() - start) < (30 * 1000)) {
            i = newImageUuid();
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                // ok continue
            }
        }
    }

    @Test(enabled = false, groups = "manual")
    public void testWholeDbWithPersistentELL() throws Exception {
        final Principal p = new Principal("root", "system", "FullText");
        final PersistentEventLogLoader pell = (PersistentEventLogLoader) this.applicationContext
                .getBean("persistentEventLogLoader");
        final EventLog max;
        final long id;
        max = (EventLog) getExecutor().execute(p, new Executor.SimpleWork(this, "whole db") {
            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                pell.deleteCurrentId();
                return pell.lastEventLog();
            }
        });

        ftb = new FullTextBridge();
        fti = new FullTextIndexer(pell);
        ftt = new FullTextThread(getManager(), getExecutor(), fti, ftb, true);
        ftt.run(); // Single run to do initialization

        // Can't use more() here since it will always return true
        // since PELL is designed to be called by a timer.
        // Instead we only do the whole database once.
        id = (Long) getExecutor().execute(p, new Executor.SimpleWork(this, "whole db 2") {
            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                return pell.getCurrentId();
            }
        });
        while (id < max.getId()) {
            ftt.run();
        }

    }

    public void testMimeTypes() throws Exception {
        Properties p = PropertiesLoaderUtils
                .loadAllProperties("classpath:mime.properties");
        System.out.println(p);
    }

    public void testSimpleCreation() throws Exception {
        ftb = new FullTextBridge();
        fti = new FullTextIndexer(getLogs());
        ftt = new FullTextThread(getManager(), getExecutor(), fti, ftb, true);
        ftt.run();
    }

    // These two types of bad event logs should not throw exceptions. They can
    // occur especially during database upgrades. In that case, the entry should
    // be skipped.
    public void testBadEventLog() throws Exception {
        ftb = new FullTextBridge();
        fti = new FullTextIndexer(new EventLogLoader() {

            int count = 3;

            @Override
            protected EventLog query() {
                count--;
                EventLog l = new EventLog();
                l.setEntityId(0L);
                switch (count) {
                case 3:
                    l.setAction("INSERT"); // good
                    l.setEntityType("BAD");
                    break;
                case 2:
                    l.setAction("BAD");
                    l.setEntityType("ome.model.meta.Experimenter"); // good
                    break;
                case 1:
                    l.setAction("INSERT"); // good
                    l.setEntityType("ome.model.meta.Experimenter"); // good
                case 0:
                    l = null;
                }
                return l;
            }

            @Override
            public long more() {
                return 0;
            }
        });
        ftt = new FullTextThread(getManager(), getExecutor(), fti, ftb, true);
        ftt.run();
    }

    public void testTagDescription() throws Exception {
        String uuid = UUID.randomUUID().toString();
        CommentAnnotation description = new CommentAnnotation();
        description.setTextValue(uuid);
        TagAnnotation tag = new TagAnnotation();
        tag.linkAnnotation(description);
        i = newImageString("tag+described", tag);
        i = iUpdate.saveAndReturnObject(i);
        Image i2 = iQuery.findByQuery("select i from Image i "
                + "left outer join fetch i.annotationLinks l1 "
                + "left outer join fetch l1.child a1 "
                + "left outer join fetch a1.annotationLinks l2 "
                + "left outer join fetch l2.child a2 where i.id = :id",
                new Parameters().addId(i.getId()));
        iUpdate.indexObject(i);
        // Forcing a link to be indexed for backlog testing, ticket:1102
        iUpdate.indexObject(i.unmodifiableAnnotationLinks().iterator().next());

        Annotation a = i.linkedAnnotationList().get(0);
        assertEquals(1, a.sizeOfAnnotationLinks());

        this.loginRoot();
        List<Image> list = iQuery.findAllByFullText(Image.class, uuid, null);
        assertEquals(1, list.size());
        assertTrue(list.get(0).getId().equals(i.getId()));
    }

    public void testUniqueImage() throws Exception {
        i = newImageUuid();
        iUpdate.indexObject(i);

        this.loginRootKeepGroup();
        List<Image> list = iQuery.findAllByFullText(Image.class, i.getName(),
                null);
        assertEquals(1, list.size());
        assertEquals(i.getId(), list.get(0).getId());
    }

    public void testUniquePrivateImage() throws Exception {
        testUniqueImage();
        iAdmin.changePermissions(i, Permissions.USER_PRIVATE);

        this.loginNewUser();
        List<Image> list = iQuery.findAllByFullText(Image.class, i.getName(),
                null);
        assertEquals(0, list.size());
    }

    public void testUniqueImageBelongingToOnlyUser() throws Exception {
        testUniqueImage();
        Experimenter e = this.loginNewUser();

        // Create an image with the same name
        Image i2 = newImageString(i.getName());

        iUpdate.indexObject(i2);
        loginUser(e.getOmeName()); // After indexing, must relogin
        long id = iAdmin.getEventContext().getCurrentUserId();

        List<Image> list = iQuery.findAllByFullText(Image.class, i.getName(),
                new Parameters(new Filter().owner(id)));
        assertEquals(1, list.size());

        list = iQuery.findAllByFullText(Image.class, i.getName(), null);
        assertEquals(2, list.size());

    }

    public void testUniqueImageBelongingToOnlyGroup() throws Exception {
        testUniqueImageBelongingToOnlyUser();
        long id = iAdmin.getEventContext().getCurrentGroupId();

        List<Image> list = iQuery.findAllByFullText(Image.class, i.getName(),
                new Parameters(new Filter().group(id)));
        assertEquals(1, list.size());

        list = iQuery.findAllByFullText(Image.class, i.getName(), null);
        assertEquals(2, list.size());

    }

    /*
     * This test shows first that ProjectsWithImageNameBridge works, but also
     * that when an object is reparsed, that its Document in the index is
     * completely replaced.
     * 
     * Currently disabled since this Bridge is deactivate by default.
     */
    @Test(enabled = false)
    public void testProjectsWithImagesCustomBridge() throws Exception {

        final String before = UUID.randomUUID().toString();
        final String after = UUID.randomUUID().toString();
        final String query = "image_name:" + before;

        ome.model.containers.Project p = new ome.model.containers.Project();
        Dataset d = new Dataset("middle");
        java.sql.Timestamp testTimestamp = new java.sql.Timestamp(System
                .currentTimeMillis());
        Image i = newImageString(before);

        // Save the project and the image should be found
        p.setName("bridged");
        p.linkDataset(d);
        d.linkImage(i);
        p = iUpdate.saveAndReturnObject(p);
        iUpdate.indexObject(p);

        List<Project> list;

        list = iQuery.findAllByFullText(Project.class, query, null);
        assertTrue("should find it now", list.size() == 1);
        list = iQuery.findAllByFullText(Project.class, before, null);
        assertTrue("should find it now in combined", list.size() == 1);

        // Change the name and the project should be changed too
        i = p.linkedDatasetList().get(0).linkedImageList().get(0);
        i.setName(after);
        iUpdate.saveAndReturnObject(i);

        // Re-indexing project
        iUpdate.indexObject(p);

        list = iQuery.findAllByFullText(Project.class, query, null);
        assertTrue("should NOT find it now", list.size() == 0);
        list = iQuery.findAllByFullText(Project.class, before, null);
        assertTrue("should NOT find it now in combined", list.size() == 0);
        list = iQuery.findAllByFullText(Project.class, after, null);
        assertTrue("BUT should find it with new name", list.size() == 1);

    }

    public void testUserOverridesGroup() throws Exception {
        fail("nyi");
    }

    public void testCreateFile() throws Exception {

        // Test data
        final String str = UUID.randomUUID().toString();

        // Parser setup
        FileParser parser = new StringBasedFileParser(str);
        Map<String, FileParser> parsers = new HashMap<String, FileParser>();
        parsers.put("text/plain", parser);

        // Upload
        FileUploader upload = new FileUploader(this.factory, str, "uuid",
                "/dev/null");
        try {
            upload.run();
        } catch (Exception e) {
            // This seems to be throwing an exception
            // when run in the server
        }

        iUpdate.indexObject(new OriginalFile(upload.getId(), false));

    }

    public void testNamePathEtcFromFileAreParsed() throws Exception {

        // Test data
        final String str = UUID.randomUUID().toString();

        // Parser setup
        FileParser parser = new StringBasedFileParser(str);
        Map<String, FileParser> parsers = new HashMap<String, FileParser>();
        parsers.put("text/plain", parser);

        // Upload
        FileUploader upload = new FileUploader(this.factory, str, str, str);
        try {
            upload.run();
        } catch (Exception e) {
            // This seems to be throwing an exception
            // when run in the server
        }

        java.sql.Timestamp testTimestamp = new java.sql.Timestamp(System
                .currentTimeMillis());
        Image i = newImageString(str);
        FileAnnotation fa = new FileAnnotation();
        fa.setFile(new OriginalFile(upload.getId(), false));
        i.linkAnnotation(fa);
        i = iUpdate.saveAndReturnObject(i);
        iUpdate.indexObject(i);

        List<? extends IObject> list;
        list = iQuery.findAllByFullText(Image.class, str, null);
        assertTrue("combined_fields", list.size() == 1);
        list = iQuery.findAllByFullText(Image.class, "name:" + str, null);
        assertTrue("name", list.size() == 1);
        list = iQuery.findAllByFullText(Image.class, "file.name:" + str, null);
        assertTrue("file.name", list.size() == 1);
        list = iQuery.findAllByFullText(Image.class, "file.path:" + str, null);
        assertTrue("file.path", list.size() == 1);
        list = iQuery.findAllByFullText(Image.class, "file.contents:" + str,
                null);
        assertTrue("file.contents", list.size() == 1);

    }

    public void testLinksFile() throws Exception {

        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        // Parser setup
        FileParser parser = new FileParser();
        Map<String, FileParser> parsers = new HashMap<String, FileParser>();
        parsers.put("text/plain", parser);

        // Upload
        FileUploader upload = new FileUploader(this.factory, "<html>\n"
                + "<a href=\"secret.html\">Ooh hoo</a></html>", uuid,
                "/path/uuid");
        try {
            upload.run();
        } catch (Exception e) {
            // This seems to be throwing an exception
            // when run in the server
        }

        java.sql.Timestamp testTimestamp = new java.sql.Timestamp(System
                .currentTimeMillis());
        Image i = newImageString(name + "_links.txt");
        FileAnnotation fa = new FileAnnotation();
        fa.setFile(new OriginalFile(upload.getId(), false));
        i.linkAnnotation(fa);
        i = iUpdate.saveAndReturnObject(i);
        iUpdate.indexObject(i);

        List<? extends IObject> list;
        list = iQuery.findAllByFullText(Image.class, name + "_links.txt", null);
        assertTrue("name_links", list.size() == 1);
        list = iQuery.findAllByFullText(Image.class, name + "*", null);
        assertTrue("name*", list.size() == 1);
        list = iQuery.findAllByFullText(Image.class, "secret.html", null);
        assertTrue("secret.html", list.size() >= 1);
        list = iQuery.findAllByFullText(Image.class, "secret*", null);
        assertTrue("secret.*", list.size() >= 1);

    }

    public void testPossiblyCorruptPdf() throws Exception {
        File article = ResourceUtils.getFile("classpath:gantt_article.pdf");
        FileUploader uploader = new FileUploader(factory, article);
        uploader.run();
        iUpdate.indexObject(new OriginalFile(uploader.getId(), false));
    }

    // Helpers
    // ==========================================================

    class StringBasedFileParser extends FileParser {

        final String str;

        public StringBasedFileParser(String str) {
            this.str = str;
        }

        @Override
        public Iterable<Reader> doParse(File file) {
            return wrap(new Iterator<Reader>() {
                StringReader next = new StringReader(str);

                public boolean hasNext() {
                    return next != null;
                }

                public Reader next() {
                    StringReader rv = next;
                    next = null;
                    return rv;
                }

                public void remove() {
                    next = null;
                }
            });
        }
    }
    

    private Image newImageUuid() {
        return newImageString(UUID.randomUUID().toString());
    }
    
    private Image newImageString(String str, Annotation...anns) {
        Image _i = new Image();
        _i.setName(str);
        _i.setAcquisitionDate(new Timestamp(0));
        for (Annotation annotation : anns) {
            _i.linkAnnotation(annotation);
        }
        _i = iUpdate.saveAndReturnObject(_i);
        return _i;
    }
}
