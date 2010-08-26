/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ome.api.IDelete;

import org.springframework.beans.FatalBeanException;

/**
 * Single value of the map entries from spec.xml. A value such as "HARD;/Roi"
 * specifies that the operation with the name "HARD" should be applied to the
 * given path, and that the given path should use a pre-existing specification.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 * @see IDelete
 */
public class DeleteEntry {

    public enum Op {
        HARD, SOFT, REAP, ORPHAN, NULL;
    }

    final public static Op DEFAULT = Op.HARD;

    final private static Pattern opRegex = Pattern
            .compile("^([^;]+?)(;([^;]*?))?$");

    final protected DeleteSpec self;

    final protected String name;

    final protected Op op;

    /* final */private DeleteSpec subSpec;

    public DeleteEntry(DeleteSpec self, String value) {
        checkArgs(self, value);
        this.self = self;
        final Matcher m = getMatcher(value);
        this.name = getName(m);
        this.op = getOp(m);
    }

    public DeleteSpec getSubSpec() {
        return subSpec;
    }

    //
    // Helpers
    //

    protected void checkArgs(Object...values) {
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

    /**
     * Load the spec which has the same name as this entry, but do not load
     * the spec if the name matches {@link #name}
     */
    protected void postProcess(Map<String, DeleteSpec> specs) {
        if (name.equals(self.getName())) {
            return;
        }
        this.subSpec = specs.get(name);
    }

}
