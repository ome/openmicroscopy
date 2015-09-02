/*
 * util.mocks.IMock
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package util.mocks;

/**
 * Specifies the life-cycle of mock objects. A mock object's life-cycle is as
 * follows:
 * <ol>
 * <li>After creation, the object is in expectations set up mode. In this state
 * the object shall accept calls to collect invocation expectations. An
 * expectation is encapsulated by an instance of {@link MockedCall}. </li>
 * <li>When the {@link #activate() activate} method is called, the object
 * transitions to verification mode. In this state, the object shall verify that
 * each call set during the expectations set up phase is performed and return
 * the orginal expected value, if any, as set in the original expectation call.
 * If the verification outcome is not positive, the test must fail.</li>
 * <li>When the {@link #verify() verify} method is called, the object shall
 * verify that all the calls that were set in set up mode have actually been
 * performed in verification mode. If the verification outcome is not positive,
 * the test must fail.</li>
 * </ol>
 * <p>
 * A mock object typically fullfils the contract set out by this interface by
 * using an instance of {@link MockSupport}.
 * </p>
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @since OME2.2
 */
public interface IMock {

    /**
     * Transitions the mock object from expectations set up mode to verification
     * mode.
     */
    public void activate();

    /**
     * Verifies that all calls in the expected sequence were performed. This
     * method has to be called at the end of the test and will fail the test if
     * not all calls were performed.
     */
    public void verify();

}
