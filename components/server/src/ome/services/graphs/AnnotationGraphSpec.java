/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.graphs;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.model.annotations.Annotation;
import ome.tools.hibernate.ExtendedMetadata;
import ome.tools.hibernate.QueryBuilder;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;

/**
 * {@link AbstractHierarchyGraphSpec} specialized for processing annotations.
 * Adds options for which namespaces to process.
 *
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 * @see IGraph
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class AnnotationGraphSpec extends AbstractHierarchyGraphSpec {

    private final static Logger log = LoggerFactory
        .getLogger(AnnotationGraphSpec.class);

    /**
     * Collection of namespace values (or LIKE values if they contain '%' or
     * '?') which will be included by default.
     */
    public static final Set<String> nsIncludes = Collections
            .unmodifiableSet(new HashSet<String>(Arrays.asList("foo")));

    /**
     * Collection of namespace values (defined as {@link #nsIncludes}) which
     * will be omitted from processing by default.
     */
    public static final Set<String> nsExcludes = Collections
            .unmodifiableSet(new HashSet<String>(Arrays.asList("foo")));

    /**
     * exclude values parsed out from the user options.
     */
    private final String[] excludes;

    /**
     * Default excludes values as determined at Spring configuration time. These
     * are assigned if none are passed via the options during initialize.
     */
    private final Map<String, String> excludeMap = new HashMap<String, String>();

    //
    // Initialization-time values
    //

    /**
     * Creates a new instance.
     *
     * @param entries
     *            The entries to handle.
     */
    public AnnotationGraphSpec(List<String> entries) {
        this(null, entries);
    }

    /**
     * Creates a new instance.
     *
     * @param excludeMap
     *            The namespaces to exclude.
     * @param entries
     *            The entries to handle.
     */
    public AnnotationGraphSpec(Map<String, String> excludeMap,
            List<String> entries) {
        super(entries);
        excludes = new String[entries.size()];
        if (excludeMap != null) {
            this.excludeMap.putAll(excludeMap);
        }
    }

    /**
     * Returns the value from "...;excludes=VALUEHERE" parsed out of the options
     * map during {@link #initialize(long, String, Map<String, String>)}.
     *
     * Used primarily for testing.
     */
    public String getExclude(int step) {
        return excludes[step];
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Class getRoot() {
        return Annotation.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Set<Class<Annotation>> getTypes(ExtendedMetadata em) {
        return em.getAnnotationTypes();
    }

    protected void handleOptions(final int i, final String[] parts) {
        for (int j = 1; j < parts.length; j++) {
            if (parts[j].startsWith("excludes")) {
                if (!"KEEP".equals(parts[0])) {
                    // Currently only supporting KEEP;excludes=
                    // see queryBackupIds if this changes.
                    throw new FatalBeanException(
                        "Currently excludes only compatible with KEEP:" +
                        StringUtils.join(parts, ";"));
                }
                int idx = parts[j].indexOf("=");
                excludes[i] = parts[j].substring(idx + 1);
            }
        }
    }

    protected void postProcessOptions() {
        for (int i = 0; i < excludes.length; i++) {
            if (types[i] != null) {
                String defaultExclude = excludeMap.get("/" + types[i].getSimpleName());
                if (excludes[i] == null && defaultExclude != null) {
                    excludes[i] = defaultExclude;
                }
            }
        }
    }

    /**
     * Returns true to prevent skipping of the entire subspec if there were any
     * "excludes" passed in with the options. These namespaces must be processed
     * anyway.
     */
    @Override
    public boolean overrideKeep() {
        for (int step = 0; step < excludes.length; step++) {
            if (excludes[step] != null && excludes[step].length() > 0) {
                // If we have an exclude, we have to perform the action anyway
                return true;
            }
        }
        return false;
    }

    protected boolean isOverrideKeep(final int step, final QueryBuilder and,
        final String alias) {

        final String exclude = excludes[step];
        if (excludes[step] != null && excludes[step].length() > 0) {
            final String[] parts = exclude.split(",");
            and.and(alias + ".ns in (:excludes)");
            and.paramList("excludes", Arrays.asList(parts));
            log.debug("Exclude statement: " + and.toString());
            return true;
        }
        // There are no excludes, so there is no reason to override KEEP.
        return false;
    }
}
