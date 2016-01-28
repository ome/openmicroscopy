/*
 * util.mocks.MockSupport
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package util.mocks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

/**
 * Provides mocks with the means to easily implement the life-cycle required by
 * the {@link IMock} interface.
 * <p>
 * A mock object makes a new a <code>MockSupport</code> and links it to itself
 * at creation time. The mock object is now in expectations set up mode (as
 * required by the life-cycle contract) and so is its <code>MockSupport</code>.
 * All the mock object has to do is to collect expectations in the form of
 * {@link MockedCall}'s and append them to the <code>MockSupport</code>'s
 * list (calling {@link #add(MockedCall)}), in the same order as they were
 * collected. This is important as the order in which expectations are appended
 * to the list is also the order in which calls will be verified.
 * </p>
 * <p>
 * When the {@link IMock#activate() activate} method is called on the mock
 * object, it simply has to forward this call to its <code>MockSupport</code>,
 * which transitions to verification mode. Now it's not possible to collect
 * expectations any more and the {@link #add(MockedCall) add} method will throw
 * an exception if an attempt is made &#151; this means the mock object won't
 * have to keep track of the state, its <code>MockSupport</code> already does
 * that.
 * </p>
 * <p>
 * At this point the mock object is in verification mode (as required by the
 * life-cycle contract) and so is its <code>MockSupport</code>. Upon
 * invocation, each mocked method within the mock object will first create a
 * {@link MockedCall} to reflect the current invocation, then pass this new
 * object to the {@link #verifyCall(MockedCall) verifyCall} method, and finally
 * get back the original {@link MockedCall} that was expected &#151; this is
 * done so that the method may extract the return value, if any, from the
 * original expected call (the one set by means of {@link #add(MockedCall)})
 * and return it. If the {@link MockedCall} object passed to
 * {@link #verifyCall(MockedCall) verifyCall} doesn't match the current call
 * expectation (as set in the original sequence), the test fails.
 * </p>
 * <p>
 * The {@link #verifyCallSequence() verifyCallSequence} method is meant to be
 * invoked at the end of the test to verify that all expected calls were
 * performed. If less calls were performed, then the test fails. So all the mock
 * object has to do is to call this method when its
 * {@link IMock#verify() verify} method is invoked.
 * </p>
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @since OME2.2
 */
public class MockSupport {

    /** The sequence of calls that the mock object is expecting. */
    private List expectedCalls;

    /** Iterates through {@link #expectedCalls} when in verification mode. */
    private Iterator expectationsIterator;

    /**
     * Counts the calls that have been performed. This counter is only updated
     * in verification-mode.
     */
    private int performedCalls;

    /**
     * Tells whether we're in expectations set up mode (<code>false</code>)
     * or verification mode (<code>true</code>).
     */
    private boolean isActive;

    /**
     * Creates a new instance. The newly created object will be in expectations
     * set up mode.
     */
    public MockSupport() {
        expectedCalls = new ArrayList();
        isActive = false;
        performedCalls = 0;
        expectationsIterator = null;
    }

    /**
     * Appends the specified call to the expectations list. This method can only
     * be invoked in expectations set up mode, that is, any time after creation
     * up until a call to {@link #activate()}.
     * 
     * @param mc
     *            The next call in the sequence of expected calls.
     */
    public void add(MockedCall mc) {
        if (isActive) {
            throw new IllegalAccessError(
                    "Can't set expectations while they're verified.");
        }
        if (mc == null) {
            throw new NullPointerException("No mocked call was specified.");
        }
        expectedCalls.add(mc);
    }

    /**
     * Transitions this object into verification mode. The expecations list will
     * be frozen (no further additions allowed) and only the verify methods can
     * be invoked from now on.
     */
    public void activate() {
        if (isActive) {
            throw new IllegalAccessError("Can't re-activate.");
        }
        isActive = true;
        expectationsIterator = expectedCalls.iterator();
    }

    /**
     * Tells whether this object is in expectations set up mode. That is, if
     * {@link #activate()} has not been called yet.
     * 
     * @return <code>true</code> if in expectations set up mode,
     *         <code>false</code> otherwise.
     */
    public boolean isSetUpMode() {
        return !isActive;
    }

    /**
     * Tells whether this object is in verification mode. That is, if
     * {@link #activate()} has been called.
     * 
     * @return <code>true</code> if in verification mode, <code>false</code>
     *         otherwise.
     */
    public boolean isVerificationMode() {
        return isActive;
    }

    /**
     * Verifies the passed call against the current call expectation. If the
     * expectation is not met, the test fails.
     * 
     * @param actual
     *            The actual call.
     * @return The expected call, so that the original return value, if any, can
     *         be extracted.
     */
    public MockedCall verifyCall(MockedCall actual) {
        if (!isActive) {
            throw new IllegalAccessError(
                    "Can't verify expectations while they're set.");
        }
        if (!expectationsIterator.hasNext()) {
            failTooManyCalls(actual);
        }
        performedCalls++;
        MockedCall expected = (MockedCall) expectationsIterator.next();
        if (!expected.isSameCall(actual)) {
            failUnexpectedCall(expected, actual, performedCalls);
        }
        return expected;
    }

    /**
     * Verifies that all the expected calls were actually performed. If this
     * condition is not met, the test fails.
     */
    public void verifyCallSequence() {
        if (!isActive) {
            throw new IllegalAccessError(
                    "Can't verify expectations while they're set.");
        }
        if (expectationsIterator.hasNext()) {
            failMoreCallsExpected();
        }
    }

    /**
     * Helper method to fail the test if not all calls in the expected sequence
     * were performed.
     */
    private void failMoreCallsExpected() {
        StringBuffer buf = new StringBuffer();
        buf.append("Uncompleted call sequence. Expected calls: ");
        buf.append(expectedCalls.size());
        buf.append(" - performed: ");
        buf.append(performedCalls);
        buf.append(".");
        Assert.fail(buf.toString());
    }

    /**
     * Helper method to fail the test if all expected calls have already been
     * performed and a new call is carried out.
     * 
     * @param unexpected
     *            The exceeding call.
     */
    private void failTooManyCalls(MockedCall unexpected) {
        StringBuffer buf = new StringBuffer();
        buf.append("Exceeding call: ");
        buf.append(unexpected);
        buf.append(".");
        Assert.fail(buf.toString());
    }

    /**
     * Helper method to fail the test if the current call doesn't match the
     * expected call in the sequence.
     * 
     * @param expected
     *            The expected call.
     * @param unexpected
     *            The last performed out call.
     * @param index
     *            The index of the expected call in the sequence.
     */
    private void failUnexpectedCall(MockedCall expected, MockedCall unexpected,
            int index) {
        StringBuffer buf = new StringBuffer();
        buf.append("Unexpected call: ");
        buf.append(unexpected);
        buf.append(". ");
        buf.append("Was expecting (sequence=");
        buf.append(index);
        buf.append("): ");
        buf.append(expected);
        buf.append(".");
        Assert.fail(buf.toString());
    }

}
