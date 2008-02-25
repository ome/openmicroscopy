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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import ome.model.annotations.Annotation;
import ome.model.annotations.TagAnnotation;
import ome.model.annotations.TextAnnotation;
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
import org.springframework.transaction.TransactionStatus;
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
            i = new Image();
            i.setName(UUID.randomUUID().toString());
            i = iUpdate.saveAndReturnObject(i);
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
        max = (EventLog) getExecutor().execute(p, new Executor.Work() {
            public Object doWork(TransactionStatus status, Session session,
                    ServiceFactory sf) {
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
        id = (Long) getExecutor().execute(p, new Executor.Work() {
            public Object doWork(TransactionStatus status, Session session,
                    ServiceFactory sf) {
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
    // occur especially during databse upgrades. In that case, the entry should
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
        });
        ftt = new FullTextThread(getManager(), getExecutor(), fti, ftb, true);
        ftt.run();
    }

    public void testTagDescription() throws Exception {
        String uuid = UUID.randomUUID().toString();
        TextAnnotation description = new TextAnnotation();
        description.setTextValue(uuid);
        TagAnnotation tag = new TagAnnotation();
        tag.linkAnnotation(description);
        i = new Image();
        i.setName("tag+described");
        i.linkAnnotation(tag);
        i = iUpdate.saveAndReturnObject(i);
        Image i2 = iQuery.findByQuery("select i from Image i "
                + "left outer join fetch i.annotationLinks l1 "
                + "left outer join fetch l1.child a1 "
                + "left outer join fetch a1.annotationLinks l2 "
                + "left outer join fetch l2.child a2 where i.id = :id",
                new Parameters().addId(i.getId()));
        iUpdate.indexObject(i);

        Annotation a = i.linkedAnnotationList().get(0);
        assertEquals(1, a.sizeOfAnnotationLinks());

        this.loginRoot();
        List<Image> list = iQuery.findAllByFullText(Image.class, uuid, null);
        assertEquals(1, list.size());
        assertTrue(list.get(0).getId().equals(i.getId()));
    }

    public void testUniqueImage() throws Exception {
        i = new Image();
        i.setName(UUID.randomUUID().toString());
        i = iUpdate.saveAndReturnObject(i);
        iUpdate.indexObject(i);

        this.loginRoot();
        List<Image> list = iQuery.findAllByFullText(Image.class, i.getName(),
                null);
        assertTrue(list.size() == 1);
        assertTrue(list.get(0).getId().equals(i.getId()));
    }

    public void testUniquePrivateImage() throws Exception {
        testUniqueImage();
        iAdmin.changePermissions(i, Permissions.USER_PRIVATE);

        this.loginNewUser();
        List<Image> list = iQuery.findAllByFullText(Image.class, i.getName(),
                null);
        assertTrue(list.size() == 0);
    }

    public void testUniqueImageBelongingToOnlyUser() throws Exception {
        testUniqueImage();
        Experimenter e = this.loginNewUser();

        // Create an image with the same name
        Image i2 = new Image();
        i2.setName(i.getName());
        i2 = iUpdate.saveAndReturnObject(i2);

        iUpdate.indexObject(i2);
        loginUser(e.getOmeName()); // After indexing, must relogin
        long id = iAdmin.getEventContext().getCurrentUserId();

        List<Image> list = iQuery.findAllByFullText(Image.class, i.getName(),
                new Parameters(new Filter().owner(id)));
        assertTrue(list.size() == 1);

        list = iQuery.findAllByFullText(Image.class, i.getName(), null);
        assertTrue(list.size() == 2);

    }

    public void testUniqueImageBelongingToOnlyGroup() throws Exception {
        testUniqueImageBelongingToOnlyUser();
        long id = iAdmin.getEventContext().getCurrentGroupId();

        List<Image> list = iQuery.findAllByFullText(Image.class, i.getName(),
                new Parameters(new Filter().group(id)));
        assertTrue(list.size() == 1);

        list = iQuery.findAllByFullText(Image.class, i.getName(), null);
        assertTrue(list.size() == 2);

    }

    public void testUserOverridesGroup() throws Exception {
        fail("nyi");
    }

    public void testCreateFile() throws Exception {

        // Test data
        final String str = UUID.randomUUID().toString();

        // Parser setup
        FileParser parser = new FileParser() {
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
        };
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

}
