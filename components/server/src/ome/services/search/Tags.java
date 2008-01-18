/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import ome.services.SearchBean;
import ome.system.ServiceFactory;

import org.hibernate.Session;
import org.springframework.transaction.TransactionStatus;

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

    public void doWork(TransactionStatus status, Session session,
            ServiceFactory sf) {
        throw new UnsupportedOperationException();
    }
}
