/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.export;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.model.IObject;
import ome.services.graphs.GraphEntry;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphSpec;
import ome.services.graphs.GraphState;
import ome.services.graphs.GraphStep;
import ome.services.graphs.GraphStepFactory;
import ome.services.graphs.GraphSteps;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.tools.hibernate.ExtendedMetadata;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;

/**
 * Counter which can be passed into the {@link GraphState} constructor and later
 * used during export. As calls to
 * {@link #create(int, List, GraphSpec, GraphEntry, long[])} are made, the
 * factory keeps links to several object types. After {@link GraphState}
 * initialization, the other methods on the factory can be used to load the
 * objects based on their index.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.3
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class ExporterStepFactory implements GraphStepFactory {

    private final static String[] TOP_LEVEL = new String[] {
            "BooleanAnnotation", "CommentAnnotation", "Dataset",
            "DoubleAnnotation", "Experiment", "Experimenter",
            "ExperimenterGroup", "FileAnnotation", "Image", "Instrument",
            "ListAnnotation", "LongAnnotation", "Plate", "Project", "Roi",
            "Screen", "TagAnnotation", "TermAnnotation", "TimestampAnnotation",
            "XMLAnnotation" };

    static {
        Arrays.sort(TOP_LEVEL);
    }

    private final Executor ex;

    private final Principal p;

    private final ExtendedMetadata em;

    private final Map<String, ExporterIndex> data = new HashMap<String, ExporterIndex>();

    public ExporterStepFactory(Executor ex, Principal p, ExtendedMetadata em) {
        this.ex = ex;
        this.p = p;
        this.em = em;
    }

    public GraphStep create(int idx, List<GraphStep> stack, GraphSpec spec,
            GraphEntry entry, long[] ids) throws GraphException {
        ExporterStep step = new ExporterStep(em, idx, stack, spec, entry, ids);
        update(spec, entry, ids, step);
        return step;
    }

    public GraphSteps postProcess(List<GraphStep> steps) {
        return new GraphSteps(steps);
    }

    public int getCount(String name) {
        ExporterIndex index = data.get(name);
        if (index == null) {
            return -1;
        }
        return index.size();
    }

    @SuppressWarnings("unchecked")
    public <T extends IObject> T getObject(String name, int order)
            throws GraphException {
        return (T) load(name, byOrder(name, order));
    }

    //
    // Helpers classes and methods
    //

    /**
     * determines the proper object name (i.e. class) for the given entry, and
     * records all the ids in the current tally.
     */
    private void update(GraphSpec spec, GraphEntry entry, long[] ids,
            GraphStep step) throws GraphException {

        if (ids == null) {
            return; // This is a parent-spec
        }

        String[] path = entry.path(spec.getSuperSpec());
        String key = path[path.length - 1];
        ExporterIndex v = data.get(key);
        if (v == null) {
            int indicesNeeded = depth(path);
            v = new ExporterIndex(indicesNeeded);
            data.put(key, v);
        }
        v.add(step, ids);
    }

    /**
     * Starts at the back of the provided path, looking for top-level objects.
     * If none is found, then a {@link GraphException} is thrown. Otherwise, the
     * length of the index array which should be passed to
     * {@link #id(String, int[])} is returned.
     *
     * @param path
     * @return
     * @throws GraphException
     */
    private int depth(String[] path) throws GraphException {
        for (int i = path.length - 1; i >= 0; i--) {
            String part = path[i];
            if (0 <= Arrays.binarySearch(TOP_LEVEL, part)) {
                return path.length - i; // length of search indexes needed
            }
        }
        throw new GraphException("Path without top-level:"
                + StringUtils.join(path));
    }

    /**
     * Returns the {@link ExporterIndex} with the given name or throws a
     * {@link GraphException}.
     *
     * @param name
     * @return
     * @throws GraphException
     */
    private ExporterIndex indexOrThrow(String name) throws GraphException {
        ExporterIndex v = data.get(name);
        if (v == null) {
            throw new GraphException("No indexes for " + name
                    + ". Use getCount first!");
        }
        return v;
    }

    /**
     * Lookup the object id for the object with the given name at the given
     * indexes. If the length of the indexes does not match those stored an
     * exception will be thrown.
     *
     * UNUSED.
     */
    private long id(String name, int...idx) throws GraphException {
        ExporterIndex v = indexOrThrow(name);

        if (v.indicesNeeded != idx.length) {
            throw new GraphException("Wrong index sizes! Expected:" +
                    v.indicesNeeded + ". Got: " + idx.length);

        }

        throw new UnsupportedOperationException("NYI");
    }


    private long byOrder(String name, int order) throws GraphException {
        ExporterIndex v = indexOrThrow(name);
        return v.getIdByOrder(order);
    }

    private IObject load(String name, long id) {
        return (IObject) ex.execute(p, new Load(this, name, id));
    }

    /**
     * {@link Executor.SimpleWork} implementation which loads an object based on
     * its class and id. The returned object will be disconnected from any
     * session and can be used just as a single DB row.
     */
    private static class Load extends Executor.SimpleWork {

        private final String name;

        private final Long id;

        public Load(ExporterStepFactory factory, String name, Long id) {
            super(factory, "load", name, id);
            this.name = name;
            this.id = id;
        }

        public Object doWork(Session session, ServiceFactory sf) {
            return session.get(name, id);
        }

    }

}
