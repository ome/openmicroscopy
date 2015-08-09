/*
 * ome.model.internal.GraphHolder
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.model.internal;

// Java imports

// Third-party libraries

// Application-internal dependencies
import ome.conditions.SecurityViolation;
import ome.model.IObject;

/**
 * holds information regarding the graph to which an {@link ome.model.IObject}
 * belongs.
 * 
 * {@link GraphHolder#hasToken()}, {@link GraphHolder#tokenMatches(Token)},
 * and {@link GraphHolder#setToken(Token, Token)} are final so that subclasses
 * cannot intercept tokens.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 3.0
 * @author josh
 */
public final class GraphHolder {

    private IObject replacement;

    /**
     * a replacement is a <em>managed</em> entity instance which has the same
     * primary key as this instance. Storing this value here allows for several
     * optimizations.
     * 
     * @return entity
     */
    public IObject getReplacement() {
        return replacement;
    }

    /**
     * used mostly by {@code ome.api.IUpdate}. Improper use of this method may
     * cause erratic behavior.
     * 
     * @param replacement
     */

    public void setReplacement(IObject replacement) {
        this.replacement = replacement;
    }

    private Token token;

    /**
     * tests if this {@link GraphHolder} contains a {@link Token} reference.
     */
    public final boolean hasToken() {
        return this.token != null;
    }

    /**
     * check the {@link Token} for the {@link IObject} represented by this
     * {@link GraphHolder}. This can be seen to approximate "ownership" of this
     * Object within the JVM.
     * 
     * @return true only if the two instances are identical.
     */
    public final boolean tokenMatches(Token token) {
        return this.token == token;
    }

    /**
     * set the {@link Token} for this {@link GraphHolder} but only if you posses
     * the current {@link Token}. The first call to
     * {@link #setToken(Token, Token)} will succeed when {@link #token} is null.
     * 
     * @param previousToken
     * @param newToken
     */
    public final void setToken(Token previousToken, Token newToken) {
        if (token == null || previousToken == token) {
            this.token = newToken;
        }

        else {
            throw new SecurityViolation("Tokens do not match.");
        }
    }
}
