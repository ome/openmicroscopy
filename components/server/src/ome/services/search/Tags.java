/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import java.util.List;

import ome.model.IObject;
import ome.services.SearchBean;

/**
 * Query template used by {@link SearchBean} to store user requests.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class Tags extends SearchAction {

    private static final long serialVersionUID = 1L;

    private final String[] tags;

    public Tags(SearchValues values, String[] tags) {
        super(values);
        if (tags == null || tags.length < 1) {
            throw new IllegalArgumentException("Tags must be non-empty");
        }
        this.tags = new String[tags.length];
        for (int i = 0; i < tags.length; i++) {
            if (tags[i] == null || tags[i].length() < 1) {
                throw new IllegalArgumentException("Tag at " + i
                        + " must be non-empty");
            }
            this.tags[i] = tags[i];
        }
    }

    @Override
    public <T extends IObject> List<T> getNext() {
        return null;
    }
}
