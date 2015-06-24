/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.graphs;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.model.IObject;
import ome.tools.hibernate.ExtendedMetadata;
import ome.tools.hibernate.QueryBuilder;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.springframework.beans.FatalBeanException;

/**
 * {@link GraphSpec} specialized for processing hierarchies of
 * other types. Adds options for which classes to process.
 *
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.4.4
 * @see IGraph
 * @see ticket:9435
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public abstract class AbstractHierarchyGraphSpec extends BaseGraphSpec {

    private final static Logger log = LoggerFactory
        .getLogger(AbstractHierarchyGraphSpec.class);

    /**
     * Whether or not the type of types[i] is abstract. If true, no processing
     * will take place, since otherwise duplicate multiple actions would be
     * attempted for each id.
     */
    protected final boolean[] isAbstract;

    /**
     * Type of each sub-step. TheSuperClass.class.isAssignableFrom(types[i]) is
     * <code>true</code>.
     */
    protected final Class<?>[] types;

    /**
     * Original string contained in the options {@link Map} before parsing. The
     * value may come from one of several sources, e.g. for a FileAnnotation the
     * keys checked are:
     * <pre>
     *     /abspath/FileAnnotation
     *     /FileAnnotation
     *     /abspath/TypeAnnotation
     *     /TypeAnnotation
     *     /abspath/Annotation
     *     /Annotation
     * </pre>
     */
    protected final String[] rawOption;

    /**
     * Key from the options map which was the source of the rawOption value of
     * the same index. I.e. if rawOption[2] contains "KEEP", then optSource[2]
     * should point to an entry in options which equals "KEEP".
     */
    protected final String[] optSource;

    //
    // Initialization-time values
    //

    /**
     * Creates a new instance.
     *
     * @param entries
     *            The entries to handle.
     */
    public AbstractHierarchyGraphSpec(List<String> entries) {
        super(entries);
        isAbstract = new boolean[entries.size()];
        types = new Class[entries.size()];
        rawOption = new String[entries.size()];
        optSource = new String[entries.size()];
    }

    /**
     * Return the root class for this hierarchy.
     */
    @SuppressWarnings("rawtypes")
    protected abstract Class getRoot();

    /**
     * Returns all the types that are known for this hierarchy.
     */
    protected abstract <T> Set<Class<T>> getTypes(ExtendedMetadata em);

    /**
     * Performs sanity checks on the entries found in {@link ExtendedMetadata}.
     * Primarily, this prevents new types from not being properly specified
     * in spec.xml.
     */
    @Override
    public void setExtendedMetadata(ExtendedMetadata em) {
        super.setExtendedMetadata(em);

        // First calculate the number of unique top-level paths
        List<String> uniquePaths = new ArrayList<String>();
        for (GraphEntry entry : entries) {
            String topLevel = entry.path("")[0];
            if (!uniquePaths.contains(topLevel)) {
                uniquePaths.add(topLevel);
            }
        }

        // Now we check if this represents all the hierarchy types
        // in the system.
        Set<Class<IObject>> types = getTypes(em);
        if (types.size() != uniquePaths.size()) {
            throw new FatalBeanException(
                    "Mismatch between types defined and those found: "
                            + entries + "<> " + types);
        }

        TYPE: for (Class<?> type : types) {
            String simpleName = type.getSimpleName();
            for (int i = 0; i < entries.size(); i++) {
                GraphEntry entry = entries.get(i);
                if (entry.path("").length > 1) {
                    // This not part of our hierarchy, but some subpath
                    // ignore it.
                    continue;
                }
                if (simpleName.equals(entry.getName().substring(1))) {
                    this.types[i] = type;
                    if (Modifier.isAbstract(type.getModifiers())) {
                        this.isAbstract[i] = true;
                    }
                    continue TYPE;
                }
            }
            throw new FatalBeanException("Could not find entry: " + simpleName);
        }
    }

   /**
    * In order to allow subclasses like the AnnotationGraphSpec to make use
    * of this method, hooks have been added.
    *
    * @see #handleOptions(int, String[])
    */
    @Override
    public int initialize(long id, String superspec, Map<String, String> dontmodify)
        throws GraphException {

        Map<String, String> options = null;
        if (dontmodify != null) {
            options = new HashMap<String, String>();
            options.putAll(dontmodify);
        }

        // Before the calculation of ops and other initialization events
        // happen, we apply all the options which match superclasses,
        // to the related subclasses and strip off any specific
        // clauses, e.g. KEEP;excludes=%companionFile will be passed as "KEEP"
        if (options != null) {
            for (int i = 0; i < types.length; i++) {
                GraphEntry entry = entries.get(i);
                Class<?> type = types[i];

                // If the type is null, then this is not part of our hierarchy
                // but but object under an instance, like
                // /FileAnnotation/OriginalFile.
                if (type == null) {
                    continue;
                }

                String simpleName = type.getSimpleName();
                String last = "/" + simpleName;
                String[] path = entry.path(superspec);
                String absolute = "/" + StringUtils.join(path, "/");

                if (options.containsKey(absolute)) {
                    rawOption[i] = options.get(absolute);
                    optSource[i] = absolute;

                } else if (options.containsKey(last)) {
                    rawOption[i] = options.get(last);
                    optSource[i] = last;
                }

                // Here we make sure that an absolute will be set for the source
                // when replacing values in the options map below.
                if (optSource[i] == null) {
                    optSource[i] = absolute;
                }
            }

            // Here we go from the bottom (most subclass) to the top of the
            // hierarchy, taking the first non-null value we find from a
            // superclass
            final Class<?> root = getRoot();
            for (int i = 0; i < types.length; i++) {
                Class<?> type = types[i];

                if (type == null) {
                    continue;
                }

                NULLCHECK: while (rawOption[i] == null
                        && !root.equals(type)) {
                    type = type.getSuperclass();
                    for (int j = 0; j < types.length; j++) {
                        if (type.equals(types[j])) {
                            rawOption[i] = rawOption[j]; // May also be null
                            continue NULLCHECK;
                        }
                    }
                    throw new FatalBeanException("Couldn't find supertype: "
                            + type);
                }

            }

            // Now that we've parsed out the raw options based on the
            // hierarchy, we break off the first item and if not
            // empty, that becomes the Op which is passed onto
            // super.initialize
            for (int i = 0; i < rawOption.length; i++) {
                String raw = rawOption[i];
                if (raw != null) {
                    String[] parts = raw.split(";");
                    if (parts[0].length() > 0) {
                        options.put(optSource[i], parts[0]);
                    } else {
                        options.remove(optSource[i]);
                    }
                    handleOptions(i, parts);
                }
            }

            postProcessOptions();

        }

        // Then all the options into structures for
        //

        int steps = super.initialize(id, superspec, options);
        return steps;
    }

    /**
     * Called once per loop iteration throw rawOptions during
     * {@link #initialize(long, String, Map)}
     *
     * @param i Index into {@link #rawOption} which is being processed.
     * @param parts semi-colon split values of rawOptions[i]
     */
    protected abstract void handleOptions(int i, String[] parts);

    /**
     * If any options exist, this method will be called to finalize any of
     * the processing done by {@link #handleOptions(int, String[])}.
     */
    protected abstract void postProcessOptions();

    @Override
    public long[][] queryBackupIds(Session session, int step, GraphEntry subpath,
            QueryBuilder and) throws GraphException {

        if (isAbstract[step]) {
            return new long[0][0];
        }

        if (and != null) {
            throw new GraphException("Unexpected non-null and: " + and);
        }

        final Class<?> root = getRoot();

        // Copying the entry since we cannot currently find relationships
        // to subclasses, i.e. getRelationship(ImageAnnotationLink,
        // LongAnnotation)==null.
        final GraphEntry dontuseentry = entries.get(step);
        final String[] dontuse = dontuseentry.path("");
        final String klass = dontuse[0];
        dontuse[0] = "/" + root.getSimpleName(); // Reset the value.
        final String newpath = StringUtils.join(dontuse, "/");
        final GraphEntry copy = new GraphEntry(this, newpath, subpath);

        final String[] sub = copy.path(superspec);
        final int which = sub.length - dontuse.length;
        final String alias = "ROOT" + which;

        and = new QueryBuilder();
        and.whereClause();
        if (root.getSimpleName().equals(klass)) {
            // ticket:5793 - ROOT.class = Annotation will never
            // return true since it is an abstract type. We should
            // really check for any abstract type here and produce
            // a query including all of the subtypes.
        } else {
            and.and(alias + ".class = " + klass);
        }

        // If we have excludes and this is a KEEP, we only want
        // to query for those types which match the excludes value.
        if (copy.isKeep()) {
            if (!isOverrideKeep(step, and, alias)) {
                // If there is no excludes, then none of this type should
                // be loaded.
                return new long[0][0]; // EARLY EXIT!
            }
        }
        if ("".equals(and.queryString().trim())) {
            and = null;
        }

        return super.queryBackupIds(session, step, copy, and);
    }

    /**
     * If {@link #overrideKeep()} returns true for a subclass, then this method
     * will likely want to (at least sometimes) return a true value. When it
     * returns true, then it should also modify the {@link QueryBuilder} in
     * order to properly filter for only those objects of interest. If a false
     * is returned, then no IDs will be queried for this entity.
     */
    protected abstract boolean isOverrideKeep(int step, QueryBuilder and,
        String alias);

}
