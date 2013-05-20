/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import java.io.Reader;
import java.util.List;
import java.util.Map;

import ome.io.nio.OriginalFilesService;
import ome.model.IAnnotated;
import ome.model.ILink;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.FileAnnotation;
import ome.model.annotations.TagAnnotation;
import ome.model.annotations.TextAnnotation;
import ome.model.annotations.TermAnnotation;
import ome.model.core.OriginalFile;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.util.DetailsFieldBridge;
import ome.util.Utils;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.builtin.DateBridge;

/**
 * Primary definition of what will be indexed via Hibernate Search. This class
 * is delegated to by the {@link DetailsFieldBridge}, and further delegates to
 * classes as defined under "SearchBridges".
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 * @DEV.TODO insert/update OR delete regular type OR annotated type OR
 *           originalfile
 * @see <a href="http://trac.openmicroscopy.org.uk/ome/FileParsers">Parsers</a
 *      href>
 * @see <a href="http://trac.openmicroscopy.org.uk/ome/SearchBridges">Bridges</a
 *      href>
 */
public class FullTextBridge extends BridgeHelper {

    final protected OriginalFilesService files;
    final protected Map<String, FileParser> parsers;
    final protected Class<FieldBridge>[] classes;

    /**
     * Since this constructor provides the instance with no way of parsing
     * {@link OriginalFile} binaries, all files will be assumed to have blank
     * content. Further, no custom bridges are provided and so only the default
     * indexing will take place.
     */
    public FullTextBridge() {
        this(null, null);
    }

    /**
     * Constructor which provides an empty set of custom
     * {@link FieldBridge bridges}.
     */
    @SuppressWarnings("unchecked")
    public FullTextBridge(OriginalFilesService files,
            Map<String, FileParser> parsers) {
        this(files, parsers, new Class[] {});
    }

    /**
     * Main constructor.
     * 
     * @param files
     *            {@link OriginalFileServce} for getting access to binary files.
     * @param parsers
     *            List of {@link FileParser} instances which are currently
     *            configured.
     * @param bridgeClasses
     *            set of {@link FieldBridge bridge classes} which will be
     *            instantiated via a no-arg constructor.
     * @see <a
     *      href="http://trac.openmicroscopy.org.uk/ome/SearchBridges">Bridges</a
     *      href>
     */
    @SuppressWarnings("unchecked")
    public FullTextBridge(OriginalFilesService files,
            Map<String, FileParser> parsers, Class<FieldBridge>[] bridgeClasses) {
        this.files = files;
        this.parsers = parsers;
        this.classes = bridgeClasses == null ? new Class[] {} : bridgeClasses;
    }

    /**
     * Default implementation of the
     * {@link #set(String, Object, Document, Store, org.apache.lucene.document.Field.Index, Float)}
     * method which calls
     * {@link #set_file(String, IObject, Document, Store, org.apache.lucene.document.Field.Index, Float)}
     * {@link #set_annotations(String, Object, Document, Store, org.apache.lucene.document.Field.Index, Float)},
     * {@link #set_details(String, Object, Document, Store, org.apache.lucene.document.Field.Index, Float)},
     * and finally
     * {@link #set_custom(String, Object, Document, Store, org.apache.lucene.document.Field.Index, Float)}.
     * as well as all {@link Annotation annotations}.
     */
    @Override
    public void set(String name, Object value, Document document, LuceneOptions opts) {

        IObject object = (IObject) value;

        // Store class in COMBINED
        String cls = Utils.trueClass(object.getClass()).getName();
        add(document, null, cls, opts);

        set_file(name, object, document, opts);
        set_annotations(name, object, document, opts);
        set_details(name, object, document, opts);
        set_custom(name, object, document, opts);

    }

    /**
     * Uses {@link #parse(OriginalFile)} to get a {@link Reader} for the given
     * file which is then passed to
     * {@link #add(Document, String, Reader, Float)} using the field name
     * "file".
     * 
     * @param name
     * @param object
     * @param document
     * @param store
     * @param index
     * @param boost
     */
    public void set_file(final String name, final IObject object,
            final Document document, final LuceneOptions opts) {

        if (object instanceof OriginalFile) {
            OriginalFile file = (OriginalFile) object;
            addContents(document, "file.contents", file, files, parsers, opts);
        }

    }

    /**
     * Walks the various {@link Annotation} instances attached to the object
     * argument and adds various levels to the index.
     * 
     * @param name
     * @param object
     * @param document
     * @param store
     * @param index
     * @param boost
     */
    public void set_annotations(final String name, final IObject object,
            final Document document, final LuceneOptions opts) {

        if (object instanceof ILink) {
            ILink link = (ILink) object;
            if (link.getChild() instanceof Annotation) {
                reindex(link.getParent());
            }
        }
        if (object instanceof IAnnotated) {
            IAnnotated annotated = (IAnnotated) object;
            List<Annotation> list = annotated.linkedAnnotationList();
            for (Annotation annotation : list) {
                String at = annotationTypeString(annotation);
                add(document, "annotation.type", at, opts);
                if (annotation.getNs() != null) {
                    add(document, "annotation.ns", annotation.getNs(), opts);
                }
                if (annotation instanceof TermAnnotation) {
                    TermAnnotation term = (TermAnnotation) annotation;
                    String termValue = term.getTermValue();
                    termValue = termValue == null ? "" : termValue;
                    add(document, "term", termValue, opts);
                } else if (annotation instanceof TextAnnotation) {
                    TextAnnotation text = (TextAnnotation) annotation;
                    String textValue = text.getTextValue();
                    textValue = textValue == null ? "" : textValue;
                    add(document, "annotation", textValue, opts);
                    if (annotation instanceof TagAnnotation) {
                        add(document, "tag", textValue, opts);
                        List<Annotation> list2 = annotation
                                .linkedAnnotationList();
                        for (Annotation annotation2 : list2) {
                            if (annotation2 instanceof TextAnnotation) {
                                TextAnnotation text2 = (TextAnnotation) annotation2;
                                String textValue2 = text2.getTextValue();
                                textValue2 = textValue2 == null ? ""
                                        : textValue2;
                                add(document, "annotation", textValue2, opts);
                            }
                        }
                    }
                } else if (annotation instanceof FileAnnotation) {
                    FileAnnotation fileAnnotation = (FileAnnotation) annotation;
                    handleFileAnnotation(document, opts,
                            fileAnnotation);
                }
            }
        }

        // Have to be careful here, since Annotations are also IAnnotated.
        // Don't use if/else
        if (object instanceof FileAnnotation) {
            FileAnnotation fileAnnotation = (FileAnnotation) object;
            handleFileAnnotation(document, opts, fileAnnotation);

        }
    }

    /**
     * Parses all ownership and time-based details to the index for the given
     * object.
     * 
     * @param name
     * @param object
     * @param document
     * @param store
     * @param index
     * @param boost
     */
    public void set_details(final String name, final IObject object,
            final Document document, final LuceneOptions opts) {

        final LuceneOptions stored = new SimpleLuceneOptions(opts, Store.YES);
        final LuceneOptions storedNotAnalyzed = new SimpleLuceneOptions(opts, Index.NOT_ANALYZED, Store.YES);

        Details details = object.getDetails();
        if (details != null) {
            Experimenter e = details.getOwner();
            if (e != null && e.isLoaded()) {
                String omename = e.getOmeName();
                String firstName = e.getFirstName();
                String lastName = e.getLastName();
                add(document, "details.owner.omeName", omename, stored);
                add(document, "details.owner.firstName", firstName, opts);
                add(document, "details.owner.lastName", lastName, opts);
            }

            ExperimenterGroup g = details.getGroup();
            if (g != null && g.isLoaded()) {
                String groupName = g.getName();
                add(document, "details.group.name", groupName, stored);
            }

            Event creationEvent = details.getCreationEvent();
            if (creationEvent != null) {
                add(document, "details.creationEvent.id", creationEvent.getId()
                        .toString(), storedNotAnalyzed);
                if (creationEvent.isLoaded()) {
                    String creation = DateBridge.DATE_SECOND
                            .objectToString(creationEvent.getTime());
                    add(document, "details.creationEvent.time", creation,
                            storedNotAnalyzed);
                }
            }

            Event updateEvent = details.getUpdateEvent();
            if (updateEvent != null) {
                add(document, "details.updateEvent.id", updateEvent.getId()
                        .toString(), storedNotAnalyzed);
                if (updateEvent.isLoaded()) {
                    String update = DateBridge.DATE_SECOND
                            .objectToString(updateEvent.getTime());
                    add(document, "details.updateEvent.time", update,
                            storedNotAnalyzed);
                }
            }

            Permissions perms = details.getPermissions();
            if (perms != null) {
                add(document, "details.permissions", perms.toString(), stored);
            }
        }

    }

    /**
     * Loops over each {@link #classes field bridge class} and calls its
     * {@link FieldBridge#set(String, Object, Document, Store, org.apache.lucene.document.Field.Index, Float)}
     * method. Any exceptions are logged but do not cancel execution.
     * 
     * @param name
     * @param object
     * @param document
     * @param store
     * @param index
     * @param boost
     */
    public void set_custom(final String name, final IObject object,
            final Document document, final LuceneOptions opts) {

        for (Class<FieldBridge> bridgeClass : classes) {
            if (bridgeClass != null) {
                FieldBridge bridge = null;
                try {
                    bridge = bridgeClass.newInstance();
                    if (bridge instanceof BridgeHelper) {
                        BridgeHelper helper = (BridgeHelper) bridge;
                        helper.setApplicationEventPublisher(publisher);
                    }
                    bridge.set(name, object, document, opts);
                } catch (Exception e) {
                    final String msg = String
                            .format(
                                    "Error calling set on custom bridge type:%s; instance:%s",
                                    bridgeClass, bridge);
                    logger().error(msg, e);
                }
            }
        }

    }

    /**
     * Creates {@link Field} instances for {@link FileAnnotation} objects.
     * 
     * @param document
     * @param store
     * @param index
     * @param boost
     * @param fileAnnotation
     */
    private void handleFileAnnotation(final Document document,
            final LuceneOptions opts, FileAnnotation fileAnnotation) {
        OriginalFile file = fileAnnotation.getFile();
        if (file != null) {
            // None of these values can be null
            add(document, "file.name", file.getName(), opts);
            add(document, "file.path", file.getPath(), opts);
            if (file.getHasher() != null) {
                add(document, "file.hasher", file.getHasher().getValue(), opts);
            }
            if (file.getHash() != null) {
                add(document, "file.hash", file.getHash(), opts);
            }
            if (file.getMimetype() != null) {
                add(document, "file.format", file.getMimetype(), opts);
                // ticket:2211 - duplicating for backwards compatibility
                add(document, "file.mimetype", file.getMimetype(), opts);
            }
            addContents(document, "file.contents", file, files, parsers, opts);
        }
    }

    /**
     * Return the short type name of an {@link Annotation}. If the instance is
     * an {@link ome.model.annotations.TextAnnotation} the returned value will
     * be "TextAnnotation".
     * 
     * @param annotation
     * @return
     */
    private String annotationTypeString(Annotation annotation) {
        Class ac = Utils.trueClass(annotation.getClass());
        int dot = ac.getName().lastIndexOf('.');
        if (dot < 0) {
            dot = -1;
        }
        String at = ac.getName().substring(dot + 1, ac.getName().length());
        return at;
    }

}
