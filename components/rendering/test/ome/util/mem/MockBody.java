/*
 * ome.util.mem.MockBody
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.mem;

import util.mocks.IMock;
import util.mocks.MethodSignature;
import util.mocks.MockSupport;
import util.mocks.MockedCall;

/**
 * Mock object to simulate a Body object to attach to a {@link Handle}.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @since OME2.2
 */
class MockBody implements Copiable, IMock {

    private static final MethodSignature readState = new MethodSignature(
            MethodSignature.PUBLIC, void.class, "readState");

    private static final MethodSignature writeState = new MethodSignature(
            MethodSignature.PUBLIC, void.class, "writeState");

    private static final MethodSignature copy = new MethodSignature(
            MethodSignature.PUBLIC, Object.class, "copy");

    private MockSupport mockSupport;

    MockBody() {
        mockSupport = new MockSupport();
    }

    // Used both in set up and verification mode.
    public void readState() {
        MockedCall mc = new MockedCall(readState);
        if (mockSupport.isSetUpMode()) {
            mockSupport.add(mc);
        } else {
            mockSupport.verifyCall(mc);
        }
    }

    // Used both in set up and verification mode.
    public void writeState() {
        MockedCall mc = new MockedCall(writeState);
        if (mockSupport.isSetUpMode()) {
            mockSupport.add(mc);
        } else {
            mockSupport.verifyCall(mc);
        }
    }

    // Used in set up mode.
    public void copy(Object retVal) {
        MockedCall mc = new MockedCall(copy, retVal);
        mockSupport.add(mc);
    }

    // Used in verification mode.
    public Object copy() {
        MockedCall mc = new MockedCall(copy, (Object) null);
        mc = mockSupport.verifyCall(mc);
        return mc.getResult();
    }

    /**
     * @see util.mocks.IMock#activate()
     */
    public void activate() {
        mockSupport.activate();
    }

    /**
     * @see util.mocks.IMock#verify()
     */
    public void verify() {
        mockSupport.verifyCallSequence();
    }

}
