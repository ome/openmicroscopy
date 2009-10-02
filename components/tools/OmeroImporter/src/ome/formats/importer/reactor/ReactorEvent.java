/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer.reactor;

import ome.formats.importer.ImportEvent;
import ome.formats.importer.reactor.ImportReactor.ReactorState;

/**
 * Specialized {@link ImportEvent events} raised by the {@link ImportReactor}.
 */
public class ReactorEvent extends ImportEvent {

    public static class REACTOR_STATE_CHANGE extends ReactorEvent {
        public final ReactorState state;

        public REACTOR_STATE_CHANGE(ReactorState state) {
            this.state = state;
        }
    }

    public abstract static class FILESET_EVENT extends ReactorEvent {
        public final Fileset fixture;

        public FILESET_EVENT(Fileset fixture) {
            this.fixture = fixture;
        }
    }

    public static class QUEUE_APPEND extends FILESET_EVENT {
        public QUEUE_APPEND(Fileset fixture) {
            super(fixture);
        }
    }

    public static class QUEUE_STATE_CHANGE extends FILESET_EVENT {
        public QUEUE_STATE_CHANGE(Fileset fixture) {
            super(fixture);
        }
    }

    public static class QUEUE_REMOVE extends FILESET_EVENT {
        public final int index;

        public QUEUE_REMOVE(Fileset fixture, int index) {
            super(fixture);
            this.index = index;
        }
    }

    public static class SUCCESS extends FILESET_EVENT {
        public SUCCESS(Fileset fixture) {
            super(fixture);
        }
    }

    public static class FAILURE extends FILESET_EVENT {
        public FAILURE(Fileset fixture) {
            super(fixture);
        }
    }

}
