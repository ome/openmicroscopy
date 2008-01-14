/*
 *   $Id$
 *
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
import ome.parameters.Filter;
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
    public boolean idOnly = false;
    public List<String> fetches = new ArrayList<String>();
    // Nulls mean all
    public Timestamp createdStart = null;
    public Timestamp createdStop = null;
    public Timestamp modifiedStart = null;
    public Timestamp modifiedStop = null;
    public Timestamp annotatedStart = null;
    public Timestamp annotatedStop = null;
    public List<Class> onlyTypes = null;
    public List<Class> onlyAnnotations = null;
    public Details ownedBy = null;
    public Details annotatedBy = null;

    public void copy(SearchValues values) {
        this.caseSensitive = values.caseSensitive;
        this.batchSize = values.batchSize;
        this.mergedBatches = values.mergedBatches;
        this.returnUnloaded = values.returnUnloaded;
        this.useProjections = values.useProjections;
        this.idOnly = values.idOnly;
        this.fetches = new ArrayList<String>(values.fetches);
        // Nulls mean all
        this.createdStart = copyTimestamp(values.createdStart);
        this.createdStop = copyTimestamp(values.createdStop);
        this.modifiedStart = copyTimestamp(values.modifiedStart);
        this.modifiedStop = copyTimestamp(values.modifiedStop);
        this.annotatedStart = copyTimestamp(values.annotatedStart);
        this.annotatedStop = copyTimestamp(values.annotatedStop);
        this.onlyTypes = copyList(values.onlyTypes);
        this.onlyAnnotations = copyList(values.onlyAnnotations);
        this.ownedBy = copyDetails(values.ownedBy);
        this.annotatedBy = copyDetails(values.annotatedBy);
    }

    /**
     * Copies all known values from Parameters
     * 
     * @param params
     */
    public void copy(Parameters params) {
        if (params != null) {
            Filter filter = params.getFilter();
            batchSize = filter.maxResults();
            if (filter.owner() >= 0) {
                ownedBy = Details.create();
                ownedBy.setOwner(new Experimenter(filter.owner(), false));
            } else if (filter.group() >= 0) {
                ownedBy = Details.create();
                ownedBy.setGroup(new ExperimenterGroup(filter.group(), false));
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
