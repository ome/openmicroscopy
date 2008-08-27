/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions;

public interface SessionCallback { // like new Element(key,object);

    String getName();

    Object getObject();

    void join(String session);// <--or in blitz use createSession() with

    // session name in context.

    void close();
}