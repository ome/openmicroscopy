/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

import java.util.LinkedList;

import ome.system.Principal;

/**
 * Stack of active {@link Principal} instances.
 * 
 * @see BasicSecuritySystem
 */
public class PrincipalHolder {

    protected ThreadLocal<LinkedList<Principal>> principalHolder = new ThreadLocal<LinkedList<Principal>>() {
        @Override
        protected java.util.LinkedList<Principal> initialValue() {
            return new LinkedList<Principal>();
        }
    };

    public int size() {
        final LinkedList<Principal> l = principalHolder.get();
        return l.size();
    }

    public Principal getLast() {
        final LinkedList<Principal> l = principalHolder.get();
        return l.getLast();
    }

    public void login(Principal principal) {
        principalHolder.get().addLast(principal);
    }

    public int logout() {
        LinkedList<Principal> list = principalHolder.get();
        if (list.size() > 0) {
            list.removeLast();
        }
        return list.size();
    }

}
