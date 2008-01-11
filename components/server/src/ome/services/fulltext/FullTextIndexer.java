/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import java.util.ArrayList;
import java.util.List;

import ome.conditions.InternalException;
import ome.model.IAnnotated;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.FileAnnotation;
import ome.model.annotations.TextAnnotation;
import ome.model.core.OriginalFile;
import ome.model.internal.Details;
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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
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

    abstract class Action {
        Class type;
        long id;
        IObject obj;

        abstract void go(FullTextSession session);
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
    }

    class Index extends Action {

        Index(IObject obj) {
            this.obj = obj;
        }

        @Override
        void go(FullTextSession session) {
            session.index(obj);
        }
    }

    final protected Executor executor;
    final protected EventLogLoader loader;
    final protected Principal p = new Principal("root", "system", "FullText");

    public FullTextIndexer(Executor executor, EventLogLoader ll) {
        this.loader = ll;
        this.executor = executor;
    }

    public void run() {
        this.executor.execute(p, this);
    }

    public void doWork(TransactionStatus status, Session session,
            ServiceFactory sf) {
        FullTextSession fullTextSession = Search.createFullTextSession(session);
        fullTextSession.setFlushMode(FlushMode.MANUAL);
        fullTextSession.setCacheMode(CacheMode.IGNORE);
        Transaction transaction = fullTextSession.beginTransaction();
        doIndexing(fullTextSession);
        session.clear();
        transaction.commit();
    }

    public void doIndexing(FullTextSession session) {

        List<EventLog> logs = loader.nextBatch();
        List<Action> actions = new ArrayList<Action>();

        for (EventLog eventLog : logs) {
            String act = eventLog.getAction();
            Class type = asClassOrThrow(eventLog.getEntityType());
            long id = eventLog.getEntityId();

            if ("DELETE".equals(act)) {
                actions.add(new Purge(type, id));
            } else if ("UPDATE".equals(act) || "INSERT".equals(act)) {
                actions.add(new Index((IObject) session.get(type, id)));
            } else {
                throw new InternalException("Unknown action type: " + act);
            }
        }

        // Now we have all our actions, and there should be less likelihood
        // of an exception which might add some but not all of the information
        // to the index.

        for (Action action : actions) {
            action.go(session);
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
            final Document document, final Field.Store store,
            final Field.Index index, final Float boost) {

        IObject object = (IObject) value;

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
                add(document, "owner", omename, store, index, boost);
                add(document, "firstname", firstName, store, index, boost);
                add(document, "lastName", lastName, store, index, boost);
            }

            ExperimenterGroup g = details.getGroup();
            if (g != null && g.isLoaded()) {
                String groupName = g.getName();
                add(document, "group", groupName, store, index, boost);

            }

            Event creationEvent = details.getCreationEvent();
            if (creationEvent != null && creationEvent.isLoaded()) {
                String creation = dateBridge.objectToString(creationEvent
                        .getTime());
                add(document, "creation", creation, store, index, boost);
            }

            Event updateEvent = details.getUpdateEvent();
            if (updateEvent != null && updateEvent.isLoaded()) {
                String update = dateBridge
                        .objectToString(updateEvent.getTime());
                add(document, "update", update, store, index, boost);
            }
        }
    }

    protected void add(Document d, String field, String value,
            Field.Store store, Field.Index index, Float boost) {

        Field f = new Field(field, value, store, index);
        if (boost != null) {
            f.setBoost(boost);
        }
        d.add(f);

        f = new Field(COMBINED, value, store, index);
        if (boost != null) {
            f.setBoost(boost);
        }
        d.add(f);
    }

    protected String parse(OriginalFile file) {
        /*
         * String path = FILES.getPixelsPath(file.getId()); Format format =
         * file.getFormat(); Object o = PARSERS.get(format.getValue()); return
         * path;
         */
        return null;
    }
}
