/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import java.io.File;
import java.util.Map;

import ome.io.nio.OriginalFilesService;
import ome.model.IAnnotated;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.FileAnnotation;
import ome.model.annotations.TextAnnotation;
import ome.model.core.OriginalFile;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.util.CBlock;
import ome.util.DetailsFieldBridge;
import ome.util.Utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.builtin.DateBridge;

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
public class FullTextBridge implements FieldBridge {

    private final static Log log = LogFactory.getLog(FullTextBridge.class);

    // TODO add combined_fields to constants
    public final static String COMBINED = "combined_fields";

    public final static DateBridge dateBridge = new DateBridge(Resolution.DAY);

    final protected OriginalFilesService files;
    final protected Map<String, FileParser> parsers;

    /**
     * Since this constructor provides the instance with no way of parsing
     * {@link OriginalFile} binaries, all files will be assumed to have blank
     * content.
     */
    public FullTextBridge() {
        files = null;
        parsers = null;
    }

    public FullTextBridge(OriginalFilesService files,
            Map<String, FileParser> parsers) {
        this.files = files;
        this.parsers = parsers;
    }

    public void set(final String name, final Object value,
            final Document document, final Field.Store store2,
            final Field.Index index, final Float boost) {

        try {
            // TODO Temporarily storing all values for easier testing;
            final Field.Store store = Field.Store.YES;
            IObject object = (IObject) value;

            // Store class in COMBINED
            String cls = Utils.trueClass(object.getClass()).getName();
            add(document, null, cls, store, index, boost);

            if (object instanceof OriginalFile) {
                OriginalFile file = (OriginalFile) object;
                for (String parsed : parse(file)) {
                    add(document, "file", parsed, store, index, boost);
                }
            }

            if (object instanceof IAnnotated) {
                IAnnotated annotated = (IAnnotated) object;

                annotated.eachLinkedAnnotation(new CBlock<Annotation>() {

                    public Annotation call(IObject object) {
                        Annotation annotation = (Annotation) object;
                        try {
                            if (annotation instanceof TextAnnotation) {
                                TextAnnotation text = (TextAnnotation) annotation;
                                add(document, "annotation",
                                        text.getTextValue(), store, index,
                                        boost);
                            } else if (annotation instanceof FileAnnotation) {
                                FileAnnotation fileAnnotation = (FileAnnotation) annotation;
                                OriginalFile file = fileAnnotation.getFile();
                                for (String parsed : parse(file)) {
                                    add(document, "annotation", parsed, store,
                                            index, boost);
                                }
                            }
                        } catch (NullValueException nve) {
                            throw nve.convert(object);
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
                    String update = dateBridge.objectToString(updateEvent
                            .getTime());
                    add(document, "update", update, Store.YES, index, boost);
                }

                Permissions perms = details.getPermissions();
                if (perms != null) {
                    add(document, "permissions", perms.toString(), Store.YES,
                            index, boost);
                }
            }
        } catch (NullValueException nve) {
            throw nve.convert(value);
        }
    }

    protected void add(Document d, String field, String value,
            Field.Store store, Field.Index index, Float boost)
            throws NullValueException {

        Field f;

        if (value == null) {
            throw new NullValueException(field);
        }

        // If the field == null, then we ignore it, to allow easy addition
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

    /**
     * Attempts to parse the given {@link OriginalFile}. If any of the
     * necessary components is null, then it will return an empty, but not null
     * {@link Iterable}. Also looks for the catch all parser under "*"
     * 
     * @param file
     *            Can be null.
     * @return will not be null.
     */
    protected Iterable<String> parse(OriginalFile file) {
        if (files != null && parsers != null) {
            if (file != null && file.getFormat() != null) {
                String path = files.getFilesPath(file.getId());
                String format = file.getFormat().getValue();
                FileParser parser = parsers.get(format);
                if (parser != null) {
                    return parser.parse(new File(path));
                } else {
                    parser = parsers.get("*");
                    if (parser != null) {
                        return parser.parse(new File(path));
                    }
                }
            }
        }
        return FileParser.EMPTY;
    }

    private static class NullValueException extends Exception {

        String field;

        public NullValueException(String field) {
            this.field = field;
        }

        /**
         * Takes the cause of the exception and converts itself to a
         * {@link RuntimeException}
         */
        public RuntimeException convert(Object o) {
            throw new RuntimeException(String.format(
                    "Object %s had a null value in the field %s", o, field));
        }
    }
}