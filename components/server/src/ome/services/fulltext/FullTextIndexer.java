/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import java.io.File;
import java.util.Map;

import ome.conditions.InternalException;
import ome.io.nio.OriginalFilesService;
import ome.model.IAnnotated;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.FileAnnotation;
import ome.model.annotations.TextAnnotation;
import ome.model.core.OriginalFile;
import ome.model.enums.Format;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.meta.Event;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.services.util.Executor;
import ome.services.util.Executor.Work;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.util.CBlock;
import ome.util.DetailsFieldBridge;
import ome.util.Utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.builtin.DateBridge;
import org.springframework.transaction.TransactionStatus;

/**
 * Simple action which can be done in an asynchronous thread in order to index
 * all full text items. This class also acts as a delegate for the
 * {@link DetailsFieldBridge}.
 * 
 * 
 * insert/update OR delete regular type OR annotated type OR originalfile
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class FullTextIndexer implements Runnable, FieldBridge, Work {

    private final static Log log = LogFactory.getLog(FullTextIndexer.class);

    public interface Parser {
        String parse(File file);
    }

    abstract class Action {
        Class type;
        long id;
        IObject obj;

        abstract void go(FullTextSession session);

        abstract void log(Log log);
    }

    class Purge extends Action {
        Purge(Class type, long id) {
            this.type = type;
            this.id = id;
        }

        @Override
        void go(FullTextSession session) {
            session.purge(type, id);
        }

        @Override
        void log(Log log) {
            log.info(String.format("Purged: %s:Id_%d", type, id));
        }
    }

    class Index extends Action {

        Index(IObject obj) {
            this.obj = obj;
        }

        @Override
        void go(FullTextSession session) {
            session.index(obj);
        }

        @Override
        void log(Log log) {
            log.info(String.format("Indexed: %s", obj));
        }
    }

    final protected Executor executor;
    final protected EventLogLoader loader;
    final protected Principal p = new Principal("root", "system", "FullText");
    final protected OriginalFilesService files;
    final protected Map<String, Parser> parsers;

    /**
     * Since this constructor provides the instance with no way of parsing
     * {@link OriginalFile} binaries, all files will be assumed to have blank
     * content.
     */
    public FullTextIndexer(Executor executor, EventLogLoader ll) {
        this(executor, ll, null, null);
    }

    public FullTextIndexer(Executor executor, EventLogLoader ll,
            OriginalFilesService ofs, Map<String, Parser> parsers) {
        this.loader = ll;
        this.executor = executor;
        this.files = ofs;
        this.parsers = parsers;
    }

    public void run() {
        DetailsFieldBridge.lock();
        try {
            DetailsFieldBridge.setFieldBridge(this);
            this.executor.execute(p, this);
        } finally {
            DetailsFieldBridge.unlock();
        }
    }

    public void doWork(TransactionStatus status, Session session,
            ServiceFactory sf) {
        FullTextSession fullTextSession = Search.createFullTextSession(session);
        fullTextSession.setFlushMode(FlushMode.MANUAL);
        fullTextSession.setCacheMode(CacheMode.IGNORE);
        Transaction transaction = fullTextSession.beginTransaction();
        doIndexing(fullTextSession);
        transaction.commit();
        session.clear();
    }

    public void doIndexing(FullTextSession session) {

        int count = 0;

        for (EventLog eventLog : loader) {
            // Three retries
            while (count < 3 && eventLog != null) {
                try {
                    String act = eventLog.getAction();
                    Class type = asClassOrThrow(eventLog.getEntityType());
                    long id = eventLog.getEntityId();

                    Action action;
                    if ("DELETE".equals(act)) {
                        action = new Purge(type, id);
                    } else if ("UPDATE".equals(act) || "INSERT".equals(act)) {
                        action = new Index((IObject) session.get(type, id));
                    } else {
                        throw new InternalException("Unknown action type: "
                                + act);
                    }

                    action.go(session);
                    action.log(log);
                    eventLog = null;
                    count = 0;
                } catch (Exception e) {
                    log.error(String.format("Failed to index %s %d times",
                            eventLog, count), e);
                }
            }

            // Failed; Giving up
            if (count > 0) {
                throw new InternalException("Failed to index entry. Giving up.");
            }
        }

    }

    protected Class asClassOrThrow(String str) {
        try {
            return Class.forName(str);
        } catch (ClassNotFoundException e) {
            throw new InternalException("Unknown entity type in database: "
                    + str);
        }
    }

    // FieldBidge role
    // =========================================================================

    // TODO add combined_fields to constants
    public final static String COMBINED = "combined_fields";

    public final static DateBridge dateBridge = new DateBridge(Resolution.DAY);

    public void set(final String name, final Object value,
            final Document document, final Field.Store store2,
            final Field.Index index, final Float boost) {

        // TODO Temporarily storing all values for easier testing;
        final Field.Store store = Field.Store.YES;

        IObject object = (IObject) value;

        // Store class in COMBINED
        String cls = Utils.trueClass(object.getClass()).getName();
        add(document, null, cls, store, index, boost);

        if (object instanceof OriginalFile) {
            OriginalFile file = (OriginalFile) object;
            String parsed = parse(file);
            add(document, "file", parsed, store, index, boost);
        }

        if (object instanceof IAnnotated) {
            IAnnotated annotated = (IAnnotated) object;

            annotated.eachLinkedAnnotation(new CBlock<Annotation>() {

                public Annotation call(IObject object) {
                    Annotation annotation = (Annotation) object;
                    if (annotation instanceof TextAnnotation) {
                        TextAnnotation text = (TextAnnotation) annotation;
                        add(document, "annotation", text.getTextValue(), store,
                                index, boost);
                    } else if (annotation instanceof FileAnnotation) {
                        FileAnnotation fileAnnotation = (FileAnnotation) annotation;
                        OriginalFile file = fileAnnotation.getFile();
                        String parsed = parse(file);
                        add(document, "annotation", parsed, store, index, boost);
                    }
                    return annotation;
                }
            });
        }

        Details details = object.getDetails();
        if (details != null) {
            Experimenter e = details.getOwner();
            if (e != null && e.isLoaded()) {
                String omename = e.getOmeName();
                String firstName = e.getFirstName();
                String lastName = e.getLastName();
                add(document, "owner", omename, Store.YES, index, boost);
                add(document, "firstname", firstName, store, index, boost);
                add(document, "lastName", lastName, store, index, boost);
            }

            ExperimenterGroup g = details.getGroup();
            if (g != null && g.isLoaded()) {
                String groupName = g.getName();
                add(document, "group", groupName, Store.YES, index, boost);

            }

            Event creationEvent = details.getCreationEvent();
            if (creationEvent != null && creationEvent.isLoaded()) {
                String creation = dateBridge.objectToString(creationEvent
                        .getTime());
                add(document, "creation", creation, Store.YES, index, boost);
            }

            Event updateEvent = details.getUpdateEvent();
            if (updateEvent != null && updateEvent.isLoaded()) {
                String update = dateBridge
                        .objectToString(updateEvent.getTime());
                add(document, "update", update, Store.YES, index, boost);
            }

            Permissions perms = details.getPermissions();
            if (perms != null) {
                add(document, "permissions", perms.toString(), Store.YES,
                        index, boost);
            }
        }
    }

    protected void add(Document d, String field, String value,
            Field.Store store, Field.Index index, Float boost) {

        Field f;

        // If the field == null, then we ignore it, to all easy addition
        // of Fields as COMBINED
        if (field != null) {
            f = new Field(field, value, store, index);
            if (boost != null) {
                f.setBoost(boost);
            }
            d.add(f);
        }

        // Never storing in combined fields, since it's duplicated
        f = new Field(COMBINED, value, Store.NO, index);
        if (boost != null) {
            f.setBoost(boost);
        }
        d.add(f);
    }

    protected String parse(OriginalFile file) {
        if (files != null && parsers != null) {
            String path = files.getPixelsPath(file.getId());
            Format format = file.getFormat();
            Parser parser = parsers.get(format.getValue());
            return parser.parse(new File(path));
        } else {
            return "";
        }
    }
}