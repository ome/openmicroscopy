/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.graphs;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ome.services.graphs.GraphOpts.Op;
import ome.system.EventContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * Single value of the map entries from spec.xml. A value such as "HARD;/Roi"
 * specifies that the operation with the name "HARD" should be applied to the
 * given path, and that the given path should use a pre-existing specification.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 * @see IGraph
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class GraphEntry {

    private final static Logger log = LoggerFactory.getLogger(GraphEntry.class);

    final public static Op DEFAULT = Op.HARD;

    final private static Pattern opRegex = Pattern
            .compile("^([^;]+?)(;([^;]*?))?(;([^;]*?))?$");

    final private GraphSpec self;

    final private String name;

    final private String[] parts;

    final private String path;

    /**
     * Operation which should be performed for this entry.
     *
     * No longer protected since the {@link #initialize(String, Map)} phase can
     * change the operation based on the options map.
     *
     * This operation may be modified by other operations in the stack.
     */
    private Op operation;

    /**
     * Whether or not the {@link #operation} field was modified from its
     * original setting. This would happen via the user passed options map
     * on {@link #initialize(long, String, Map)}.
     */
    private boolean modifiedOp;

    /**
     * {@link GraphSpec Subspec} found by looking for the {@link #name} of this
     * {@link GraphEntry} during {@link #postProcess(ListableBeanFactory)}. If
     * this is non-null, then many actions will have to iterate over all the
     * {@link #subStepCount} steps of the {@link #subSpec}.
     */
    /* final */private GraphSpec subSpec;

    /**
     * Number of steps in the {@link #subSpec}, calculated during
     * {@link #initialize(long, String, Map)}.
     */
    /* final */private int subStepCount = 0;

    /**
     * Value of the superspec passed in during {@link #initialize(long, String, Map)}.
     * This will be used to determine where this entry is in the overall graph,
     * as opposed to just within its own {@link #self GraphSpec}.
     */
    /* final */private String superspec;

    /**
     * Value of the target id passed in during {@link #initialize(long, String, Map)}.
     */
    /* final */private long id;

    public GraphEntry(GraphSpec self, String value) {
        checkArgs(self, value);
        this.self = self;
        final Matcher m = getMatcher(value);
        this.name = getName(m);
        this.operation = getOp(m);
        this.path = getPath(m);
        this.parts = split(name);
    }

    public GraphEntry(GraphSpec self, String name, GraphEntry entry) {
        this.self = self;
        this.name = name;
        this.operation = entry.operation;
        this.path = entry.path;
        this.parts = split(name);
    }

    public String getName() {
        return name;
    }

    public boolean isKeep() {
        return Op.KEEP == operation;
    }

    public boolean isReap() {
        return Op.REAP == operation;
    }

    /**
     * Splits the name of the entry into the path components. Any suffixes
     * prefixed with a "+" are stripped.
     */
    private static String[] split(String name) {
        if (name == null) {
            return new String[0];
        }
        String[] parts0 = name.split("/");
        String part = null;
        for (int i = 0; i < parts0.length; i++) {
            part = parts0[i];
            int idx = part.indexOf("+");
            if (idx > 0) {
                parts0[i] = part.substring(0, idx);
            }
        }
        String[] parts1 = new String[parts0.length - 1];
        System.arraycopy(parts0, 1, parts1, 0, parts1.length);
        return parts1;
    }

    private static String[] prepend(String superspec, String path,
            String[] ownParts) {
        String[] superParts = split(superspec);
        String[] pathParts = split(path);
        String[] totalParts = new String[superParts.length + pathParts.length
                + ownParts.length];
        System.arraycopy(superParts, 0, totalParts, 0, superParts.length);
        System.arraycopy(pathParts, 0, totalParts, superParts.length,
                pathParts.length);
        System.arraycopy(ownParts, 0, totalParts, superParts.length
                + pathParts.length, ownParts.length);
        return totalParts;
    }

    public GraphSpec getSubSpec() {
        return subSpec;
    }

    public String getSuperSpec() {
        return superspec;
    }

    public long getId() {
        return id;
    }

    public String[] path(String superspec) {
        return prepend(superspec, path, parts);
    }

    /**
     * Returns the size of {@link #parts} and {@link #path} combined.
     * This value plus the {@link #split(String)} size of superspec is
     * the full size of the {@link #path(String)} return value.
     */
    public int ownParts() {
        return parts.length + split(path).length;
    }

    //
    // Helpers
    //

    protected void checkArgs(Object... values) {
        for (Object value : values) {
            if (value == null) {
                throw new FatalBeanException("Null argument");
            }
        }
    }

    protected Matcher getMatcher(String operation) {
        Matcher m = opRegex.matcher(operation);
        if (!m.matches()) {
            throw new FatalBeanException(String.format(
                    "Operation %s does not match pattern %s", operation,
                    opRegex));
        }
        return m;
    }

    protected String getName(Matcher m) {
        String name = m.group(1);
        if (name == null || name.length() == 0) { // Should be prevent by regex
            throw new FatalBeanException("Empty name");
        }
        return name;
    }

    protected Op getOp(Matcher m) {
        String name = null;
        name = m.group(3);
        if (name == null || name.length() == 0) {
            return DEFAULT;
        }

        try {
            return Op.valueOf(name);
        } catch (IllegalArgumentException iae) {
            throw new FatalBeanException(String.format(
                    "Unknown operation %s for entry %s", name, name));
        }
    }

    protected String getPath(Matcher m) {
        String path = m.group(5);
        if (path == null) {
            return "";
        }
        return path;
    }

    /**
     * Load the spec which has the same name as this entry, but do not load the
     * spec if the name matches {@link #name}. This is called early in the
     * {@link GraphEntry} lifecycle, by {@link GraphSpec}.
     */
    protected void postProcess(ListableBeanFactory factory) {
        if (name.equals(self.getName())) {
            return;
        } else if (factory.containsBeanDefinition(name) &&
                GraphSpec.class.isAssignableFrom(factory.getType(name))) {
            this.subSpec = factory.getBean(name, GraphSpec.class);
            this.subSpec.postProcess(factory);
        }
    }

    /**
     * Called during {@link GraphSpec#initialize(long, String, Map)} to give
     * the entry a chance to modify its {@link #op} based on the options and to
     * initialize subspecs.
     *
     * The superspec is passed in so that both the absolute path as well as the
     * last path element can be checked. Further, a key of "/" apply to all
     * entries.
     */
    public int initialize(long id, String superspec, Map<String, String> options) throws GraphException {

        this.id = id;
        this.superspec = superspec;

        if (options != null) {
            final String[] path = path(superspec);
            final String absolute = "/" + StringUtils.join(path, "/");
            final String last = "/" + path[path.length - 1];

            String option = null;
            for (String string : Arrays.asList(absolute, last, "/")) {
                option = options.get(string);
                if (option != null) {
                    String[] parts = option.split(";"); // Just in case
                    operation = Op.valueOf(parts[0]);
                    modifiedOp = true;
                    break;
                }

            }
        }

        if (subSpec != null) {
            if (subSpec == this) {
                throw new GraphException("Self-reference subspec:" + this);
            }
            subStepCount = subSpec.initialize(id, superspec + this.path,
                    options);
        }
        return subStepCount;

    }


    /**
     * A KEEP setting is a way of putting a KEEP suggestion to vote. If there is
     * a subspec, however, that vote must be passed down. If the KEEP is vetoed,
     * it is the responsibility of the subspec to make sure that only the proper
     * parts are kept or not kept.
     */
    public boolean skip() {
        if (isKeep()) {
            GraphSpec spec = this.getSubSpec();
            if (spec != null) {
                return ! spec.overrideKeep();
            }
        }
        return false;
    }

    public boolean isNull() {
        return Op.NULL.equals(operation);
    }

    public boolean isSoft() {
        return Op.SOFT.equals(operation);
    }

    /**
     * Primarily used for passing back a string representation of the default
     * operation to users for setting options.
     */
    public String getOpString() {
        return operation.toString();
    }

    //
    // GraphOpts interaction. Necessary since this instance hides its
    // "operation" field.
    //

    public void push(GraphOpts opts, EventContext ec) throws GraphException {
        opts.push(operation, modifiedOp, ec);
    }

    public void pop(GraphOpts opts) {
        opts.pop();
    }

    //
    // Misc
    //

    @Override
    public String toString() {
        return "GraphEntry [name=" + name + ", parts="
                + Arrays.toString(parts) + ", op=" + operation + ", path=" + path
                + (subSpec == null ? "" : ", subSpec=" + subSpec.getName())
                + "]";
    }

    /**
     * Similar to {@link #toString()} but used to
     * @param superspec2
     * @return
     */
    public String log(String superspec) {
        StringBuilder sb = new StringBuilder();
        sb.append("/");
        sb.append(StringUtils.join(path(superspec), "/"));
        sb.append(";");
        sb.append(operation.toString());
        return sb.toString();
    }

}
