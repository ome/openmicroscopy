/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer.reactor;

import ome.formats.importer.ImportContainer;

/**
 * Simple strategy interface for interacting with with the {@link ImportReactor}
 * . Each processing loop in the {@link ImportReactor} takes one {@link Fileset}
 * from the queue and passes it to the configured {@link Connector} instance.
 */
public interface Connector {

    String openFileset(Fileset fixture);

    void failFileset(String uuid, Exception exc);

    void handleContainer(String uuid, ImportContainer container);

    void closeFileset(String uuid);
}
