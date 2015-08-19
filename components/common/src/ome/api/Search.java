/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import ome.annotations.NotNull;
import ome.conditions.ApiUsageException;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.core.OriginalFile;
import ome.model.internal.Details;
import ome.parameters.Parameters;
import ome.util.search.LuceneQueryBuilder;

import org.hibernate.search.ProjectionConstants;

/**
 * Central search interface, allowing Web2.0 style queries. Each {@link Search}
 * instance keeps up with several queries and lazily-loads the results as
 * {@link #hasNext()}, {@link #next()} and {@link #results()} are called. These
 * queries are created by the "by*" methods.
 * 
 * Each instance also has a number of settings which can all be changed from
 * their defaults via accessors, e.g.{@link #setBatchSize(int)} or
 * {@link #setCaseSentivice(boolean)}.
 * 
 * The only methods which are required for the proper functioning of a
 * {@link Search} instance are:
 * <ul>
 * <li>{@link #onlyType(Class)}, {@link #onlyTypes(Class...)} OR
 * {@link #allTypes()}</li>
 * <li>Any by* method to create a query</li>
 * </ul>
 * Use of the {@link #allTypes()} method is discouraged, since it is possibly
 * very resource intensive, which is why any attempt to receive results without
 * specifically setting types or allowing all is prohibited.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 * @see ome.api.IQuery
 */
public interface Search extends ome.api.StatefulServiceInterface,
        java.util.Iterator<IObject> {

    // Constants ~
    // =========================================================================

    /**
     * {@link String} constant used to look up the score value of Lucene queries
     * returned by this interface. Not all queries will fill this value.
     */
    public final static String SCORE = ProjectionConstants.SCORE;

    /**
     * Default {@link #getBatchSize() batch size}
     */
    public final static int DEFAULT_BATCH_SIZE = 1000;

    /**
     * Default {@link #isMergedBatches() merged-batches}
     */
    public final static boolean DEFAULT_MERGED_BATCHES = false;

    /**
     * Default {@link #isCaseSensitive() case sensitivity}
     */
    public final static boolean DEFAULT_CASE_SENSITIVTY = false;

    /**
     * Default {@link #isUseProjections() use-projections}
     */
    public final static boolean DEFAULT_USE_PROJECTIONS = false;

    /**
     * Default {@link #isReturnUnloaded() return-unloaded}
     */
    public final static boolean DEFAULT_RETURN_UNLOADED = false;

    /**
     * Default {@link #isAllowLeadingWildcard() leading-wildcard setting}
     */
    public final static boolean ALLOW_LEADING_WILDCARD = false;
    
    /**
     * Treat date range as import date
     */
    public static final String DATE_TYPE_IMPORT = LuceneQueryBuilder.DATE_IMPORT;

    /**
     * Treat date range as acquisition date
     */
    public static final String DATE_TYPE_ACQUISITION = LuceneQueryBuilder.DATE_ACQUISITION;

    // Non-Query State ~
    // =========================================================================

    /**
     * Returns the number of active queries. This means that
     * <code>activeQueries</code> gives the minimum number of remaining calls
     * to {@link #results()} when batches are not
     * {@link #isMergedBatches() merged}.
     * 
     * @return number of active queries
     */
    int activeQueries();

    /**
     * Sets the maximum number of results that will be returned by one call to
     * {@link #results()}. If batches are not {@link #isMergedBatches() merged},
     * then results may often be less than the batch size. If batches are
     * {@link #isMergedBatches() merged}, then only the last call to
     * {@link #results()} can be less than batch size.
     * 
     * Note: some query types may not support batching at the query level, and
     * all instances must then be loaded into memory simultaneously.
     * 
     * @param size
     *            maximum number of results per call to {@link #results()}
     */
    void setBatchSize(int size);

    /**
     * Returns the current batch size. If {@link #setBatchSize(int)} has not
     * been called, the {@link #DEFAULT_BATCH_SIZE default value} will be in
     * effect.
     * 
     * @return maximum number of results per call to {@link #results()}
     * @see #DEFAULT_BATCH_SIZE
     */
    int getBatchSize();

    /**
     * Set whether or not results from two separate queries can be returned in
     * the same call to {@link #results()}.
     */
    void setMergedBatches(boolean merge);

    /**
     * Returns the current merged-batches setting. If
     * {@link #setMergedBatches(boolean)} has not been called, the
     * {@link #DEFAULT_MERGED_BATCHES default value} will be in effect.
     */
    boolean isMergedBatches();

    /**
     * Sets the case sensitivity on all queries where case-sensitivity is
     * supported.
     */
    void setCaseSentivice(boolean caseSensitive);

    /**
     * Returns the current case sensitivity setting. If
     * {@link #setCaseSentivice(boolean)} has not been called, the
     * {@link #DEFAULT_CASE_SENSITIVTY default value} will be in effect.
     */
    boolean isCaseSensitive();

    /**
     * Determines if Lucene queries should not hit the database. Instead all
     * values which are stored in the index will be loaded into the object,
     * which includes the id. However, the entity will not be marked unloaded
     * and therefore it is especially important to not allow a
     * projection-instance to be saved back to the server. This can result in
     * DATA LOSS.
     */
    void setUseProjections(boolean useProjections);

    /**
     * Returns the current use-projection setting. If true, the client must be
     * careful with all results that are returned. See
     * {@link #setUseProjections(boolean) for more.} If
     * {@link #setUseProjections(boolean)} has not been called, the
     * {@link #DEFAULT_USE_PROJECTIONS} will be in effect.
     */
    boolean isUseProjections();

    /**
     * Determines if all results should be returned as unloaded objects. This is
     * particularly useful for creating lists for further querying via
     * {@link IQuery}. This value overrides the
     * {@link #setUseProjections(boolean)} setting.
     */
    void setReturnUnloaded(boolean returnUnloaded);

    /**
     * Returns the current return-unloaded setting. If true, all returned
     * entities will be unloaded. If {@link #setReturnUnloaded(boolean)} has not
     * been called, the {@link #DEFAULT_RETURN_UNLOADED default value} will be
     * in effect.
     */
    boolean isReturnUnloaded();

    /**
     * Permits full-text queries with a leading query if true.
     * 
     * @see #isAllowLeadingWildcard()
     * @see #byFullText(String)
     * @see #bySomeMustNone(String[], String[], String[])
     */
    void setAllowLeadingWildcard(boolean allowLeadingWildcard);

    /**
     * Returns the current leading-wildcard setting. If false,
     * {@link #byFullText(String)} and
     * {@link #bySomeMustNone(String[], String[], String[])} will throw an
     * {@link ApiUsageException}, since leading-wildcard searches are quite
     * slow. Use {@link #setAllowLeadingWildcard(boolean)} in order to permit
     * this usage.
     */
    boolean isAllowLeadingWildcard();

    // Filters ~~~~~~~~~~~~~~~~~~~~~

    /**
     * Restricts the search to a single type. All return values will match this
     * type.
     */
    <T extends IObject> void onlyType(Class<T> klass);

    /**
     * Restricts searches to a set of types. The entities returned are
     * guaranteed to be one of these types.
     */
    <T extends IObject> void onlyTypes(Class<T>... classes);

    /**
     * Permits all types to be returned. For some types of queries, this carries
     * a performance penalty as every database table must be hit.
     */
    void allTypes();

    /**
     * Restricts the set of {@link IObject#getId() ids} which will be checked.
     * This is useful for testing one of the given restrictions on a reduced set
     * of objects.
     * 
     * @param ids
     *            Can be null, in which case the previous restriction is
     *            removed.
     */
    void onlyIds(Long... ids);

    /**
     * Uses the {@link Details#getOwner()} and {@link Details#getGroup()}
     * information to restrict the entities which will be returned. If both are
     * non-null, the two restrictions are joined by an AND.
     * 
     * @param d
     *            Can be null, in which case the previous restriction is
     *            removed.
     */
    void onlyOwnedBy(Details d);

    /**
     * Uses the {@link Details#getOwner()} and {@link Details#getGroup()}
     * information to restrict the entities which will be returned. If both are
     * non-null, the two restrictions are joined by an AND.
     * 
     * @param d
     *            Can be null, in which case the previous restriction is
     *            removed.
     */
    void notOwnedBy(Details d);

    /**
     * Restricts the time between which an entity may have been created.
     * 
     * @param start
     *            Can be null, i.e. interval open to negative infinity.
     * @param stop
     *            Can be null, i.e. interval opens to positive infinity.
     */
    void onlyCreatedBetween(java.sql.Timestamp start, java.sql.Timestamp stop);

    /**
     * Restricts the time between which an entity may have last been modified.
     * 
     * @param start
     *            Can be null, i.e. interval open to negative infinity.
     * @param stop
     *            Can be null, i.e. interval open to positive infinity.
     */
    void onlyModifiedBetween(java.sql.Timestamp start, java.sql.Timestamp stop);

    /**
     * Restricts entities by the time in which any annotation (which matches the
     * other filters) was added them. This matches the
     * {@link Details#getCreationEvent() creation event} of the
     * {@link Annotation}.
     * 
     * @param start
     *            Can be null, i.e. interval open to negative infinity.
     * @param stop
     *            Can be null, i.e. interval open to positive infinity.
     */
    void onlyAnnotatedBetween(Timestamp start, Timestamp stop);

    /**
     * Restricts entities by who has annotated them with an {@link Annotation}
     * matching the other filters. As {@link #onlyOwnedBy(Details)}, the
     * {@link Details#getOwner()} and {@link Details#getGroup()} information is
     * combined with an AND condition.
     * 
     * @param d
     *            Can be null, in which case any previous restriction is
     *            removed.
     */
    void onlyAnnotatedBy(Details d);

    /**
     * Restricts entities by who has not annotated them with an
     * {@link Annotation} matching the other filters. As
     * {@link #notOwnedBy(Details)}, the {@link Details#getOwner()} and
     * {@link Details#getGroup()} information is combined with an AND condition.
     * 
     * @param d
     *            Can be null, in which case any previous restriction is
     *            removed.
     */
    void notAnnotatedBy(Details d);

    /**
     * Restricts entities to having an {@link Annotation} of all the given
     * types. This is useful in combination with the other onlyAnnotated*
     * methods to say, e.g., only annotated with a file by user X. By default,
     * this value is <code>null</code> and imposes no restriction. Passing an
     * empty array implies an object that is not annotated at all.
     * 
     * 
     * Note: If the semantics were OR, then a client would have to query each
     * class individually, and compare all the various values, checking which
     * ids are where. However, since this method defaults to AND, multiple calls
     * (optionally with {@link #isMergedBatches()} and
     * {@link #isReturnUnloaded()}) and combine the results. Duplicate ids are
     * still possible, so a set of some form should be used to collect the
     * results.
     * 
     * @param classes
     *            Can be empty, in which case restriction is removed.
     */
    void onlyAnnotatedWith(Class... classes);

    // Fetches, order, counts, etc ~~~~~~~~~~~~~~~~~~~~~~

    /**
     * A path from the target entity which will be added to the current stack of
     * order statements applied to the query.
     * 
     * @param path
     *            Non-null.
     * @see #unordered()
     */
    void addOrderByAsc(@NotNull
    String path);

    /**
     * A path from the target entity which will be added to the current stack of
     * order statements applied to the query.
     * 
     * @param path
     *            Non-null.
     * @see #unordered()
     */
    void addOrderByDesc(@NotNull
    String path);

    /**
     * Removes the current stack of order statements.
     * 
     * @see #addOrderByAsc(String)
     * @see #addOrderByDesc(String)
     */
    void unordered();

    /**
     * Queries the database for all {@link Annotation annotations} of the given
     * types for all returned instances.
     * 
     * @param classes
     *            Can be empty, which removes previous fetch setting.
     */
    void fetchAnnotations(Class... classes);

    /**
     * Adds a fetch clause for loading non-annotation fields of returned
     * entities. Each fetch is a hibernate clause in dot notation.
     * 
     * @param fetches
     *            Can be empty, which removes previous fetch setting.
     */
    void fetchAlso(String... fetches);

    // Reset ~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Resets all settings (non-query state) to the original default values, as
     * if the instance had just be created.
     */
    void resetDefaults();

    // Query State ~
    // =========================================================================

    /**
     * Returns transient (without ID)
     * {@link ome.model.annotations.TextAnnotation} instances which represent
     * terms which are similar to the given terms. For example, if the argument
     * is "cell", one return value might have as its textValue: "cellular"
     * while another has "cellularize".
     * 
     * No filtering or fetching is performed.
     * 
     * @param terms
     *            Cannot be empty.
     */
    void bySimilarTerms(String...terms);
    
    /**
     * Returns transient (without ID)
     * {@link ome.model.annotations.TagAnnotation} instances which
     * represent all the {@link ome.model.annotations.TagAnnotation tags} in
     * the given group. The entities are transient and without ownership since
     * multiple users can own the same tag. This method will override settings
     * for types.
     * 
     * @param group
     *            Can be null or empty to return all tags.
     */
    void byGroupForTags(String group);

    /**
     * Creates a query which will return transient (without ID)
     * {@link ome.model.annotations.TagAnnotation} instances which represent
     * all the {@link ome.model.annotations.TagAnnotation tag groups} which
     * the given tag belongs to. The entities are transient and without
     * ownership since multiple users can own the same tag group. This method
     * will override settings for types.
     * 
     * @param tag
     *            Can be null or empty to return all groups.
     */
    void byTagForGroups(String tag);

    /**
     * Passes the query as is to the Lucene backend.
     * 
     * @param query
     *            May not be null or of zero length.
     */
    void byFullText(String query);

    /**
     * Builds a Lucene query and passes it to the Lucene backend.
     * 
     * @param fields   The fields (comma separated) to search in (name, description, ...)
     * @param from     The date range from, in the form YYYYMMDD (may be null)
     * @param to       The date range to (inclusive), in the form YYYYMMDD (may be null)
     * @param dateType {@link #DATE_TYPE_ACQUISITION} or {@link #DATE_TYPE_IMPORT}
     * @param query
     *            May not be null or of zero length.
     */
    void byLuceneQueryBuilder(String fields, String from,
            String to, String dateType, String query);
    
    /*
     * TODO: An idea: void byWildcardSql();
     */

    /**
     * Builds a Lucene query and passes it to {@link #byFullText(String)}.
     * 
     * @param some
     *            Some (at least one) of these terms must be present in the
     *            document. May be null.
     * @param must
     *            All of these terms must be present in the document. May be
     *            null.
     * @param none
     *            None of these terms may be present in the document. May be
     *            null.
     */
    void bySomeMustNone(String[] some, String[] must, String[] none);

    /**
     * Delegates to {@link IQuery#findAllByQuery(String, Parameters)} method to
     * take advantage of the {@link #and()}, {@link #or()}, and
     * {@link #not()} methods, or queue-semantics.
     * 
     * @param query
     *            Not null.
     * @param p
     *            May be null. Defaults are then in effect.
     * @see IQuery#findAllByQuery(String, Parameters)
     */
    void byHqlQuery(String query, Parameters p);

    /**
     * Finds entities annotated with an {@link Annotation} similar to the
     * example. This does not use Hibernate's
     * {@link IQuery#findByExample(IObject) Query-By-Example} mechanism, since
     * that cannot handle joins. The fields which are used are:
     * <ul>
     * <li>the main content of the annotation : String,
     * {@link OriginalFile#getId()}, etc.</li>
     * </ul>
     * <p>
     * If the main content is <em>null</em> it is assumed to be a wildcard
     * searched, and only the type of the annotation is searched. Currently,
     * ListAnnotations are not supported.
     * <p>
     * 
     * @param examples
     *            Not empty.
     */
    void byAnnotatedWith(Annotation... examples);

    /**
     * Applies the next by* method to the previous by* method, so that a call
     * {@link #hasNext()}, {@link #next()}, or {@link #results()} sees only
     * the union of the two calls.
     * 
     * For example,
     * 
     * <pre>
     * service.onlyType(Image.class);
     * service.byFullText(&quot;foo&quot;);
     * service.or();
     * service.onlyType(Dataset.class);
     * service.byFullText(&quot;foo&quot;);
     * </pre>
     * 
     * will return both Images and Datasets together.
     * 
     * Calling this method overrides a previous setting of {@link #and()} or
     * {@link #not()}. If there is no active queries (i.e.
     * {@link #activeQueries()} > 0), then an {@link ApiUsageException} will be
     * thrown.
     */
    void or();

    /**
     * Applies the next by* method to the previous by* method, so that a call
     * {@link #hasNext()}, {@link #next()}, or {@link #results()} sees only
     * the intersection of the two calls.
     * 
     * For example,
     * 
     * <pre>
     * service.onlyType(Image.class);
     * service.byFullText(&quot;foo&quot;);
     * service.intersection();
     * service.byAnnotatedWith(TagAnnotation.class);
     * </pre>
     * 
     * will return only the Images with TagAnnotations.
     * 
     * <p>
     * Calling this method overrides a previous setting of {@link #or()} or
     * {@link #not()}. If there is no active queries (i.e.
     * {@link #activeQueries()} > 0), then an {@link ApiUsageException} will be
     * thrown.
     * </p>
     */
    void and();

    /**
     * Applies the next by* method to the previous by* method, so that a call
     * {@link #hasNext()}, {@link #next()}, or {@link #results()} sees only
     * the intersection of the two calls.
     * 
     * For example,
     * 
     * <pre>
     * service.onlyType(Image.class);
     * service.byFullText(&quot;foo&quot;);
     * service.complement();
     * service.byAnnotatedWith(TagAnnotation.class);
     * </pre>
     * 
     * will return all the Images <em>not</em> annotated with TagAnnotation.
     * <p>
     * Calling this method overrides a previous setting of {@link #or()} or
     * {@link #and()}. If there is no active queries (i.e.
     * {@link #activeQueries()} > 0), then an {@link ApiUsageException} will be
     * thrown.
     * </p>
     */
    void not();

    /**
     * Returns entities with the given UUID strings
     * 
     * @param uuids
     */
    // These are currently not used often enough to be worth implementing
    // the query.
    // void byUUID(String[] uuids);
    /**
     * Removes all active queries (leaving {@link #resetDefaults() settings}
     * alone), such that {@link #activeQueries()} will return 0.
     */
    void clearQueries();

    // Retrieval ~
    // =========================================================================

    /**
     * Returns true if another call to {@link #next()} is valid. A call to
     * {@link #next()} may throw an exception for another reason, however.
     */
    boolean hasNext();

    /**
     * Returns the next entity from the current query. If the previous call
     * returned the last entity from a given query, the first entity from the
     * next query will be returned and {@link #activeQueries()} decremented.
     * Since this method only returns the entity itself, a single call to
     * {@link #currentMetadata()} may follow this call to gather the extra
     * metadata which is returned in the map via {@link #results}.
     * 
     * @throws ApiUsageException
     *             if {@link #hasNext()} returns false.
     */
    IObject next() throws ApiUsageException;

    /**
     * Provides access to the extra query information (for example Lucene score
     * and boost values) for a single call to {@link #next()}. This method may
     * only be called once for any given call to {@link #next()}.
     */
    Map<String, Annotation> currentMetadata();

    /**
     * Unsupported operation.
     */
    void remove() throws UnsupportedOperationException;

    /**
     * Returns up to {@link #getBatchSize() batch size} number of results along
     * with the related query metadata. If
     * {@link #isMergedBatches() batches are merged} then the results from
     * multiple queries may be returned together.
     * 
     * @throws ApiUsageException
     *             if {@link #hasNext()} returns false.
     */
    <T extends IObject> List<T> results() throws ApiUsageException;

    /**
     * Provides access to the extra query information (for example Lucene score
     * and boost values) for a single call to {@link #results()}. This method
     * may only be called once for any given call to {@link #results()}.
     */
    List<Map<String, Annotation>> currentMetadataList();

}
