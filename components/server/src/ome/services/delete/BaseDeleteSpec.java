/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ome.api.IDelete;

import org.hibernate.Session;
import org.springframework.beans.factory.BeanNameAware;

/**
 * {@link DeleteSpec} which takes the id of an image as the root of deletion.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 * @see IDelete
 */
public class BaseDeleteSpec implements DeleteSpec, BeanNameAware {

    private/* final */String beanName = null;

    private/* final */long id;

    private/* final */Map<String, String> options;

    protected final List<DeleteEntry> entries;

    /**
     * Simplified constructor, primarily used for testing.
     */
    public BaseDeleteSpec(String name, String...entries) {
        this(Arrays.asList(entries));
        this.beanName = name;
    }

    public BaseDeleteSpec(List<String> entries) {
        this.entries = makeList(entries);
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    //
    // Interface
    //

    public String getName() {
        return this.beanName;
    }

    public void postProcess(Map<String, DeleteSpec> specs) {
        for (DeleteEntry entry : entries) {
            entry.postProcess(specs);
        }
    }

    public int initialize(long id, Map<String, String> options)
            throws DeleteException {
        return 0;
    }

    public String delete(Session session, int step) throws DeleteException {
        for (DeleteEntry entry : entries) {
            // How do we link from the entry to ourselves
            // What if it's a subspec
        }
        return null; // No warning
    }

    //
    // Helpers
    //

    private List<DeleteEntry> makeList(List<String> entries) {
        List<DeleteEntry> rv = new ArrayList<DeleteEntry>();
        if (entries != null) {
            for (String entry : entries) {
                rv.add(new DeleteEntry(this, entry));
            }
        }
        return Collections.unmodifiableList(rv);
    }

}
