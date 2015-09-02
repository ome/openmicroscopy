/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import ome.api.Search;
import ome.model.internal.Details;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.parameters.Parameters;
import ome.services.SearchBean;

/**
 * Values used by {@link SearchBean} to store user requests.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class SearchValues implements Serializable {

    private static final long serialVersionUID = 1L;

    public boolean caseSensitive = Search.DEFAULT_CASE_SENSITIVTY;
    public int batchSize = Search.DEFAULT_BATCH_SIZE;
    public boolean mergedBatches = Search.DEFAULT_MERGED_BATCHES;
    public boolean returnUnloaded = Search.DEFAULT_RETURN_UNLOADED;
    public boolean useProjections = Search.DEFAULT_USE_PROJECTIONS;
    public boolean leadingWildcard = Search.ALLOW_LEADING_WILDCARD;
    public boolean idOnly = false;
    public List<Class> fetchAnnotations = new ArrayList<Class>();
    public List<String> fetches = new ArrayList<String>();
    public List<String> orderBy = new ArrayList<String>();
    // Nulls mean all
    public Timestamp createdStart = null;
    public Timestamp createdStop = null;
    public Timestamp modifiedStart = null;
    public Timestamp modifiedStop = null;
    public Timestamp annotatedStart = null;
    public Timestamp annotatedStop = null;
    public List<Long> onlyIds = null;
    public List<Class> onlyTypes = null;
    public List<Class> onlyAnnotatedWith = null;
    public Details ownedBy = null;
    public Details notOwnedBy = null;
    public Details annotatedBy = null;
    public Details notAnnotatedBy = null;

    public void copy(SearchValues values) {
        this.caseSensitive = values.caseSensitive;
        this.batchSize = values.batchSize;
        this.mergedBatches = values.mergedBatches;
        this.returnUnloaded = values.returnUnloaded;
        this.useProjections = values.useProjections;
        this.leadingWildcard = values.leadingWildcard;
        this.idOnly = values.idOnly;
        this.fetchAnnotations = new ArrayList<Class>(values.fetchAnnotations);
        this.fetches = new ArrayList<String>(values.fetches);
        this.orderBy = new ArrayList<String>(values.orderBy);
        // Nulls mean all
        this.createdStart = copyTimestamp(values.createdStart);
        this.createdStop = copyTimestamp(values.createdStop);
        this.modifiedStart = copyTimestamp(values.modifiedStart);
        this.modifiedStop = copyTimestamp(values.modifiedStop);
        this.annotatedStart = copyTimestamp(values.annotatedStart);
        this.annotatedStop = copyTimestamp(values.annotatedStop);
        this.onlyIds = copyList(values.onlyIds);
        this.onlyTypes = copyList(values.onlyTypes);
        this.onlyAnnotatedWith = copyList(values.onlyAnnotatedWith);
        this.ownedBy = copyDetails(values.ownedBy);
        this.annotatedBy = copyDetails(values.annotatedBy);
        this.notOwnedBy = copyDetails(values.notOwnedBy);
        this.notAnnotatedBy = copyDetails(values.notAnnotatedBy);
    }

    /**
     * Copies all known values from Parameters
     * 
     * @param params
     */
    public void copy(Parameters params) {
        if (params != null) {
            if (params.getLimit() != null) {
                batchSize = params.getLimit();
            }
            if (params.owner() >= 0) {
                ownedBy = Details.create();
                ownedBy.setOwner(new Experimenter(params.owner(), false));
            } else if (params.group() >= 0) {
                ownedBy = Details.create();
                ownedBy.setGroup(new ExperimenterGroup(params.group(), false));
            }
        }

    }

    public static <T> List<T> copyList(List<T> old) {
        if (old == null) {
            return null;
        }
        List<T> list = new ArrayList<T>(old);
        return list;
    }

    public static Details copyDetails(Details old) {
        Details d = old == null ? null : Details.create();
        if (d != null) {
            d.copy(old);
        }
        return d;
    }

    public static Timestamp copyTimestamp(Timestamp old) {
        Timestamp t = old == null ? null : new Timestamp(old.getTime());
        return t;
    }

    @SuppressWarnings("unchecked")
    public static Class[] copyClassListToArray(List<Class> old) {
        if (old == null) {
            return new Class[] {};
        } else {
            return old.toArray(new Class[] {});
        }
    }
}
