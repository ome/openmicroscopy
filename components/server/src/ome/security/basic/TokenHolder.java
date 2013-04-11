/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

import ome.model.IObject;
import ome.model.internal.GraphHolder;
import ome.model.internal.Token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages a special token (a unique object) which can be inserted into
 * {@link IObject} instances for special almost-administrative handling.
 * 
 * Identifies loose "ownership" of certain objects.
 * 
 * @see IObject#getGraphHolder()
 * @see GraphHolder#hasToken()
 */
public class TokenHolder {

    private static Logger log = LoggerFactory.getLogger(TokenHolder.class);

    private final Token token = new Token();

    public void setToken(GraphHolder gh) {
        gh.setToken(token, token);
    }

    public void clearToken(GraphHolder gh) {
        gh.setToken(token, null);
    }

    public boolean hasPrivilegedToken(IObject obj) {

        if (obj == null) {
            return false;
        }

        GraphHolder gh = obj.getGraphHolder();

        // most objects will not have a token
        if (gh.hasToken()) {
            // check if truly secure.
            if (gh.tokenMatches(token)) {
                return true;
            }
        }
        return false;
    }

    /**
     * copy a token from one {@link IObject} to another. This is currently
     * insecure and should take a third token implying the rights to copy.
     * Should only be called by {@link MergeEventListener}
     */
    public void copyToken(IObject source, IObject copy) {
        if (source == null || copy == null || source == copy) {
            return;
        }

        GraphHolder gh1 = source.getGraphHolder();
        GraphHolder gh2 = copy.getGraphHolder();

        // try our token first
        if (gh1.tokenMatches(token)) {
            gh2.setToken(token, token);
        }

    }
}
