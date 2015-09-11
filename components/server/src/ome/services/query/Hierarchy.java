/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ome.model.containers.Dataset;
import ome.model.containers.DatasetImageLink;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;

import org.hibernate.Criteria;
import org.hibernate.transform.BasicTransformerAdapter;
import org.hibernate.transform.ResultTransformer;

/**
 * single-point of entry for walking of OME container hierarchies.
 *
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since OMERO 3.0
 */
public class Hierarchy {

    //TODO Nodes currently uses statically defined arrays containing the
    //Hierarchy information. This needs to be refactored to use ITypes.
    /**
     * The rather complicated data structures are used to efficiently produce
     * chains which represent a walk up from Image to higher-level containers,
     * or a walk down from any container to Image.
     * 
     * klass and depth are fairly self-descriptive. child and parent represent
     * the name of the fields which lead down and up, respectively. ptr is a
     * pointer to the index in the other arrays of the child. image (without a
     * child) is -1.
     */
    static class Nodes {

        static Class[] klass = new Class[] { Project.class, Dataset.class,
                Image.class };

        static int[] depth = new int[] { 2, 1, 0, 1, 2 };

        static Class[] link = new Class[] { ProjectDatasetLink.class,
                DatasetImageLink.class, null };

        static String[] child = new String[] { "datasetLinks", "imageLinks",
                null, "imageLinks", "categoryLinks" };

        static String[] parent = new String[] { "projectLinks", "datasetLinks",
                null, "categoryLinks", "categoryGroupLinks" };

        static int[] ptr = new int[] { 1, 2, -1, 2, 3 };

        /** pointer to the right index in the arrays */
        private static Map<Class, Integer> lookup = new HashMap<Class, Integer>();
        static {
            lookup.put(Project.class, 0);
            lookup.put(Dataset.class, 1);
            lookup.put(Image.class, 2);
        }

        /**
         * uses the {@link #lookup} map to get the proper index. Values less
         * than zero signal an unknown class.
         */
        static int lookup(Class k) {
            int i = lookup.containsKey(k) ? lookup.get(k).intValue() : -1;
            return i;
        }

        /**
         * calls {@link #lookup(Class)} and throws an exception if the value is
         * less than 0.
         */
        static int lookupWithError(Class k) {
            int i = lookup(k);
            if (i < 0) {
                throw new IllegalArgumentException("Unknown class:" + k);
            }
            return i;
        }

        /**
         * a container is defined to be any known class (see {@link #lookup})
         * which is not an {@link Image}
         */
        static boolean isContainer(Class k) {
            int i = lookup(k);
            if (i < 0) {
                return false;
            }
            if (Image.class.isAssignableFrom(klass[i])) {
                return false;
            }
            return true;
        }

        // Walking hierarchy

        static LinkedList<Integer> getList(Class k) {
            int i = lookupWithError(k);
            LinkedList<Integer> retVal = new LinkedList<Integer>();
            if (!isContainer(k)) {
                return retVal;
            }

            retVal.add(i);
            retVal.addAll(Nodes.getList(klass[ptr[i]]));
            return retVal;
        }

        // Produce lists of link names for walking the graph

        static List<String> pathToParent(Class k) {
            int i = lookupWithError(k);
            LinkedList<Integer> list = getList(k);
            List<String> retVal = new ArrayList<String>();
            for (int j = list.size() - 1; j >= 0; j--) {
                retVal.add(parent[list.get(j)]);
            }
            return retVal;
        }

        static List<String> pathToChildFrom(Class k) {
            int i = lookupWithError(k);
            LinkedList<Integer> list = getList(k);
            List<String> retVal = new ArrayList<String>();
            for (int j = 0; j < list.size(); j++) {
                retVal.add(child[list.get(j)]);
            }
            return retVal;
        }

        // Produce map of aliases to class names for ResultTransformer

        static Map<String, String> childMap(Class k) {
            Map<String, String> map = new HashMap<String, String>();
            LinkedList<Integer> list = getList(k);

            buildMap(k, map, list);

            return map;
        }

        static Map<String, String> parentMap(Class k) {
            Map<String, String> map = new HashMap<String, String>();
            LinkedList<Integer> list = getList(k);
            Collections.reverse(list);

            buildMap(k, map, list);

            return map;
        }

        private static void buildMap(Class k, Map<String, String> map,
                LinkedList<Integer> list) {
            int count = 0;
            int current = -1;

            current = list.poll();
            assert k.equals(klass[current]);
            map.put("this", k.getName());

            while (list.size() > 0) {
                current = list.poll();
                count++;
                map.put("genitem_" + count, klass[current].getName());
            }
        }

    }

    public static Criteria[] fetchParents(Criteria c, Class klass, int stopDepth) {

        if (!Nodes.isContainer(klass)) {
            throw new IllegalStateException(
                    "Invalid class for parent hierarchy:" + klass);
        }

        return walk(c, klass, Nodes.pathToParent(klass), "parent", stopDepth,
                Query.LEFT_JOIN);
    }

    public static Criteria[] fetchChildren(Criteria c, Class klass,
            int stopDepth) {

        if (!Nodes.isContainer(klass)) {
            throw new IllegalStateException(
                    "Invalid class for child hierarchy:" + klass);
        }

        return walk(c, klass, Nodes.pathToChildFrom(klass), "child", stopDepth,
                Query.LEFT_JOIN);
    }

    // TODO used?
    public static Criteria[] joinParents(Criteria c, Class klass, int stopDepth) {

        if (!Nodes.isContainer(klass)) {
            throw new IllegalStateException(
                    "Invalid class for parent hierarchy:" + klass);
        }

        return walk(c, klass, Nodes.pathToParent(klass), "parent", stopDepth,
                Query.INNER_JOIN);
    }

    public static Criteria[] joinChildren(Criteria c, Class klass, int stopDepth) {

        if (!Nodes.isContainer(klass)) {
            throw new IllegalStateException(
                    "Invalid class for child hierarchy:" + klass);
        }

        return walk(c, klass, Nodes.pathToChildFrom(klass), "child", stopDepth,
                Query.INNER_JOIN);
    }

    private static Criteria[] walk(Criteria c, Class k, List<String> links,
            String step, int stopDepth, int joinStyle) {
        int index = Nodes.lookup(k);
        int depth = Math.min(stopDepth, Nodes.depth[index]);
        String[][] path = new String[2][2];
        Criteria[] retVal = new Criteria[depth * 2];

        for (int i = 0; i < depth; i++) {
            path[i][0] = (i > 0 ? path[i - 1][1] + "." : "") + links.get(i);
            path[i][1] = path[i][0] + "." + step;
        }

        switch (depth) {
        case 2:
            retVal[3] = c.createCriteria(path[1][1], "genitem_2", joinStyle);
            retVal[2] = c.createCriteria(path[1][0], "genlink_2", joinStyle);
        case 1:
            retVal[1] = c.createCriteria(path[0][1], "genitem_1", joinStyle);
            retVal[0] = c.createCriteria(path[0][0], "genlink_1", joinStyle);
        case 0:
            return retVal;
        default:
            throw new RuntimeException("Unhandled container depth.");
        }
    }

    public static ResultTransformer getChildTransformer(Class klass) {
        Map trans = Nodes.childMap(klass);
        return new HierarchyToMapTransformer(trans);
    }

    public static ResultTransformer getParentTransformer(Class klass) {
        Map trans = Nodes.parentMap(klass);
        return new HierarchyToMapTransformer(trans);
    }

}

/**
 * transforms a hierarchy to a map by using the aliases provided to the
 * constructor.
 * 
 * @see ome.services.query.PojosCGCPathsQueryDefinition
 * 
 */
class HierarchyToMapTransformer extends BasicTransformerAdapter {

    /**
     * 
     */
    private static final long serialVersionUID = -3530890859099882786L;
    Map _aliases;

    HierarchyToMapTransformer(Map aliases) {
        this._aliases = aliases;
    }

    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {
        Map result = new HashMap();
        for (int i = 0; i < tuple.length; i++) {
            String alias = aliases[i];
            if (alias != null && _aliases.containsKey(alias)) {
                result.put(_aliases.get(alias), tuple[i]);
            }
        }
        return result;
    }
}