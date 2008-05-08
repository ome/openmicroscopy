/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import java.io.Reader;
import java.util.List;

import ome.conditions.ApiUsageException;
import ome.model.IObject;
import ome.services.messages.RegisterServiceCleanupMessage;
import ome.services.messages.ReindexMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.hibernate.search.bridge.FieldBridge;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

/**
 * Base class for building custom {@link FieldBridge} implementations.
 * 
 * To force handling of null values, the
 * {@link #add(Document, String, String, Store, org.apache.lucene.document.Field.Index, Float)}
 * methods throw {@link NullValueException} which can convert itself to a
 * {@link RuntimeException} via {@link NullValueException#convert(Object)} if
 * that is the simplest course of action. Alternatively, you could re-add the
 * value with a null-token like "null".
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public abstract class BridgeHelper implements FieldBridge,
        ApplicationEventPublisherAware {

    /**
     * Name of the {@link Field} which contains the union of all fields. This is
     * also the default search field, so users need not append the value to
     * search the full index. A field name need only be added to a search to
     * eliminate other fields.
     * 
     * @DEV.TODO add to constants
     */
    public final static String COMBINED = "combined_fields";

    private final Log log = LogFactory.getLog(getClass());

    private ApplicationEventPublisher publisher;

    public final Log logger() {
        return log;
    }

    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Method to be implemented by all {@link FieldBridge bridges}. The "value"
     * argument is an active Hibernate object, and so the full graph can be
     * walked.
     */
    public abstract void set(final String name, final Object value,
            final Document document, final Field.Store store2,
            final Field.Index index, final Float boost);

    /**
     * Helper method which takes the parameters from the
     * {@link #set(String, Object, Document, Store, org.apache.lucene.document.Field.Index, Float)}
     * method (possibly modified) as well as the parsed {@link String} value
     * which should be added to the index, and adds two fields. One with the
     * given field name and another to the {@link #COMBINED} field which is the
     * default search provided to users.
     * 
     * @param d
     *            Document as passed to the set method. Do not modify.
     * @param field
     *            Field name which probably <em/>should</em> be modified. If
     *            this value is null, then the "value" will only be added to the
     *            {@link #COMBINED} field.
     * @param value
     *            Value which has been parsed out for this field. Should
     *            <em/>not</em> be null. If you need to store a null value in
     *            the index, use a null token like "null".
     * @param store
     *            Whether or not to store the string value in the index. Note:
     *            no values are stored in the {@link #COMBINED} field to prevent
     *            duplication.
     * @param index
     *            Whether or not to make the string searchable.
     * @param boost
     *            Positive float which increases or decreases search importance
     *            for a field. Default is 1.0.
     */
    protected void add(Document d, String field, String value,
            Field.Store store, Field.Index index, Float boost) {

        if (value == null) {
            throw new RuntimeException(
                    "Value for indexing cannot be null. Use a null token instead.");
        }

        // If the field == null, then we ignore it, to allow easy addition
        // of Fields as COMBINED
        if (field != null) {
            final Field named_field = new Field(field, value, store, index);
            if (boost != null) {
                named_field.setBoost(boost);
            }
            d.add(named_field);
        }

        // Never storing in combined fields, since it's duplicated
        final Field combined_field = new Field(COMBINED, value, Store.NO, index);
        if (boost != null) {
            combined_field.setBoost(boost);
        }
        d.add(combined_field);
    }

    /**
     * Second helper method used when parsing files. The {@link Reader} will be
     * read until it signals an end, however it is not the responsibility of
     * this instance to close the Reader since this happens asynchronously. If
     * you would like to have this {@link Reader} closed, raise an
     * {@link RegisterServiceCleanupMessage} as in {@link FileParser}:
     * 
     * <code>
     * FileReader reader = new FileReader(file);
     *  BufferedReader buffered = new BufferedReader(reader);
     *  context.publishEvent(new RegisterServiceCleanupMessage(this, buffered) {
     *      public void close() {
     *          try {
     *              Reader r = (Reader) resource;
     *              r.close();
     *          } catch (Exception e) {
     *              log.debug("Error closing " + resource, e);
     *          }
     *      }
     *  });
     *  </code>
     * 
     * @param d
     *            {@link Document} as passed to set. Do not modify.
     * @param field
     *            Field name which probably <em/>should</em> be modified. If
     *            this value is null, then the "value" will only be added to the
     *            {@link #COMBINED} field.
     * @param reader
     *            Non-null {@link Reader} to be read. If it is necessary to
     *            specifiy that the {@link Reader} is null, then use a null
     *            token like "null".
     * @param boost
     *            Positive float which increases or decreases search importance
     *            for a field. Default is 1.0.
     */
    protected void add(Document d, String field, Reader reader, Float boost) {

        Field f;

        if (reader == null) {
            throw new RuntimeException(
                    "Reader cannot be null. Either do not attempt to add anything for this field, or use a null token like \"null\" instead.");
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

        // However, we're not copying Reader information to the combined field
        // here, because can only read from a reader once.

        // Never storing in combined fields, since it's duplicated
        /*
         * f = new Field(COMBINED, reader); if (boost != null) {
         * f.setBoost(boost); } d.add(f);
         */
    }

    /**
     * Publishes a {@link ReindexMessage} which will get processed
     * asynchronously.
     */
    protected <T extends IObject> void reindex(T object) {
        if (publisher == null) {
            throw new ApiUsageException(
                    "Bridge is not configured for sending messages.");
        }
        final ReindexMessage<T> rm = new ReindexMessage<T>(this, object);
        publisher.publishEvent(rm);
    }

    /**
     * Publishes a {@link ReindexMessage} which will get processed
     * asynchronously.
     */
    protected <T extends IObject> void reindexAll(List<T> list) {
        if (publisher == null) {
            throw new ApiUsageException(
                    "Bridge is not configured for sending messages.");
        }
        final ReindexMessage<T> rm = new ReindexMessage<T>(this, list);
        publisher.publishEvent(rm);
    }

}