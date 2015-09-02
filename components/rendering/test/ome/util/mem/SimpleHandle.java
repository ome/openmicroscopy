/*
 * ome.util.mem.SimpleHandle
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.mem;

/**
 * Supports unit tests for the {@link Handle} class. Takes on the Handle role,
 * the Body is {@link MockBody}. Also, clearly exemplifies how to write a well
 * behaved Handle.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @since OME2.2
 */
class SimpleHandle extends Handle {

    SimpleHandle() {
        super(new MockBody()); // Always create a new Body object.

        // Hold no state, as our state is hold by the Body.
    }

    // WARNING: A well behaved Handle mustn't leak out a reference to its
    // Body. The only purpose of this method is to allow test cases to set
    // expectations on the Mock object.
    MockBody getInitialBody() {
        return (MockBody) getBody();
    }

    // Replicate Body's class interface to forward calls.

    public void readState() {
        // This method only read the Body's state, just forward the call.
        MockBody body = (MockBody) getBody();
        body.readState();

        // Now just discard the reference to the Body. No caching, no leakage.
    }

    public void writeState() {
        // This method writes the Body's state, we must notify Handle first.
        breakSharing();

        // Then we can forward the call.
        MockBody body = (MockBody) getBody();
        body.writeState();

        // Now just discard the reference to the Body. No caching, no leakage.
    }

}
