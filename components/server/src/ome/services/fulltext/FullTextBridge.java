/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import java.io.File;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import ome.io.nio.OriginalFilesService;
import ome.model.IAnnotated;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.FileAnnotation;
import ome.model.annotations.TagAnnotation;
import ome.model.annotations.TextAnnotation;
import ome.model.core.OriginalFile;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.util.DetailsFieldBridge;
import ome.util.Utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.builtin.DateBridge;

/**
 * Primary definition of what will be indexed via Hibernate Search. This class
 * is delegated to by the {@link DetailsFieldBridge}.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 * @DEV.TODO insert/update OR delete regular type OR annotated type OR
 *           originalfile
 */
public class FullTextBridge implements FieldBridge {

    private final static Log log = LogFactory.getLog(FullTextBridge.class);

    // TODO add combined_fields to constants
    public final static String COMBINED = "combined_fields";

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
                for (Reader parsed : parse(file)) {
                    add(document, "file", parsed, boost);
                }
            }

            if (object instanceof IAnnotated) {
                IAnnotated annotated = (IAnnotated) object;
                List<Annotation> list = annotated.linkedAnnotationList();
                for (Annotation annotation : list) {
                    try {
                        if (annotation instanceof TextAnnotation) {
                            TextAnnotation text = (TextAnnotation) annotation;
                            String textValue = text.getTextValue();
                            textValue = textValue == null ? "" : textValue;
                            add(document, "annotation", textValue, store,
                                    index, boost);
                            if (annotation instanceof TagAnnotation) {
                                add(document, "tag", textValue, store, index,
                                        boost);
                                List<Annotation> list2 = annotation
                                        .linkedAnnotationList();
                                for (Annotation annotation2 : list2) {
                                    if (annotation2 instanceof TextAnnotation) {
                                        TextAnnotation text2 = (TextAnnotation) annotation2;
                                        String textValue2 = text2
                                                .getTextValue();
                                        textValue2 = textValue2 == null ? ""
                                                : textValue2;
                                        add(document, "annotation", textValue2,
                                                store, index, boost);
                                    }
                                }
                            }
                        } else if (annotation instanceof FileAnnotation) {
                            FileAnnotation fileAnnotation = (FileAnnotation) annotation;
                            OriginalFile file = fileAnnotation.getFile();
                            for (Reader parsed : parse(file)) {
                                add(document, "annotation", parsed, boost);
                            }
                        }
                    } catch (NullValueException nve) {
                        throw nve.convert(object);
                    }

                }
            }

            Details details = object.getDetails();
            if (details != null) {
                Experimenter e = details.getOwner();
                if (e != null && e.isLoaded()) {
                    String omename = e.getOmeName();
                    String firstName = e.getFirstName();
                    String lastName = e.getLastName();
                    add(document, "details.owner.omeName", omename, Store.YES,
                            index, boost);
                    add(document, "details.owner.firstName", firstName, store,
                            index, boost);
                    add(document, "details.owner.lastName", lastName, store,
                            index, boost);
                }

                ExperimenterGroup g = details.getGroup();
                if (g != null && g.isLoaded()) {
                    String groupName = g.getName();
                    add(document, "details.group.name", groupName, Store.YES,
                            index, boost);

                }

                Event creationEvent = details.getCreationEvent();
                if (creationEvent != null) {
                    add(document, "details.creationEvent.id", creationEvent
                            .getId().toString(), Store.YES,
                            Field.Index.UN_TOKENIZED, boost);
                    if (creationEvent.isLoaded()) {
                        String creation = DateBridge.DATE_SECOND
                                .objectToString(creationEvent.getTime());
                        add(document, "details.creationEvent.time", creation,
                                Store.YES, Field.Index.UN_TOKENIZED, boost);
                    }
                }

                Event updateEvent = details.getUpdateEvent();
                if (updateEvent != null) {
                    add(document, "details.updateEvent.id", updateEvent.getId()
                            .toString(), Store.YES, Field.Index.UN_TOKENIZED,
                            boost);
                    if (updateEvent.isLoaded()) {
                        String update = DateBridge.DATE_SECOND
                                .objectToString(updateEvent.getTime());
                        add(document, "details.updateEvent.time", update,
                                Store.YES, Field.Index.UN_TOKENIZED, boost);
                    }
                }

                Permissions perms = details.getPermissions();
                if (perms != null) {
                    add(document, "details.permissions", perms.toString(),
                            Store.YES, index, boost);
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

    protected void add(Document d, String field, Reader reader, Float boost)
            throws NullValueException {

        Field f;

        if (reader == null) {
            throw new NullValueException(field);
        }

        // If the field == null, then we ignore it, to allow easy addition
        // of Fields as COMBINED
        if (field != null) {
            f = new Field(field, reader);
            if (boost != null) {
                f.setBoost(boost);
            }
            d.add(f);
        }

        // Never storing in combined fields, since it's duplicated
        f = new Field(COMBINED, reader);
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
    protected Iterable<Reader> parse(OriginalFile file) {
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