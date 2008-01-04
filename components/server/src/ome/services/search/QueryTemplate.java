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
import ome.services.SearchBean;

/**
 * Query template used by {@link SearchBean} to store user requests.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class QueryTemplate implements Serializable {

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
    public List<Class<?>> onlyTypes = null;
    public List<Class<?>> onlyAnnotations = null;
    public Details ownedBy = null;
    public Details annotatedBy = null;

    public void copy(QueryTemplate template) {
        this.caseSensitive = template.caseSensitive;
        this.batchSize = template.batchSize;
        this.mergedBatches = template.mergedBatches;
        this.returnUnloaded = template.returnUnloaded;
        this.useProjections = template.useProjections;
        this.idOnly = template.idOnly;
        this.fetches = new ArrayList<String>(template.fetches);
        // Nulls mean all
        this.createdStart = copyTimestamp(template.createdStart);
        this.createdStop = copyTimestamp(template.createdStop);
        this.modifiedStart = copyTimestamp(template.modifiedStart);
        this.modifiedStop = copyTimestamp(template.modifiedStop);
        this.annotatedStart = copyTimestamp(template.annotatedStart);
        this.annotatedStop = copyTimestamp(template.annotatedStop);
        this.onlyTypes = copyList(template.onlyTypes);
        this.onlyAnnotations = copyList(template.onlyAnnotations);
        this.ownedBy = copyDetails(template.ownedBy);
        this.annotatedBy = copyDetails(template.annotatedBy);
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

}
