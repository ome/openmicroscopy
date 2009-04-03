/*
 * org.openmicroscopy.shoola.util.concur.tasks.TestInvocationChainAdapter
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.concur.tasks;


//Java imports

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies

/** 
 * Routine unit test for {@link InvocationChainAdapter}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class TestInvocationChainAdapter
    extends TestCase
{
    
    private InvocationChainAdapter   target;  //Object under test.
    private MockInvocation[]         chain;  //Mocks.
    private Object[]                 results;  //Call results.
    
    
    private void makeMockChain(int size)
    {
        chain = new MockInvocation[size];
        results = new Object[size];
        for (int i = 0; i < chain.length; i++) {
             chain[i] = new MockInvocation();
             results[i] = new Object();
        }
        target = new InvocationChainAdapter(chain);
    }
    
    private void setUpExpectedCalls()
    {
        for (int i = 0; i < chain.length; i++)
            chain[i].call(results[i]);
    }
    
    private void transitionMocksToVerificationMode()
    {
        for (int i = 0; i < chain.length; i++)
            chain[i].activate();
    }
    
    private void ensureAllExpectedCallsWerePerformed()
    {
        for (int i = 0; i < chain.length; i++)
            chain[i].verify();
    }
    
    private void doTestIsDone(int chainSize)
        throws Exception
    {
        makeMockChain(chainSize);
        setUpExpectedCalls();
        transitionMocksToVerificationMode();
        
        //Test.
        for (int i = 0; i < chainSize; i++) {
            assertFalse((chainSize-i)+" more steps to go!", target.isDone());
            target.doStep();
        }
        assertTrue("No more steps to go!", target.isDone());
        target.doStep();
        assertTrue("No more steps to go!", target.isDone());
        
        ensureAllExpectedCallsWerePerformed();
    }
    
    private void doTestDoStep(int chainSize) 
        throws Exception
    {
        makeMockChain(chainSize);
        setUpExpectedCalls();
        transitionMocksToVerificationMode();
        
        //Test.
        for (int i = 0; i < chainSize; i++) {
            assertEquals("Should return the same value as call["+i+"].", 
                    results[i], target.doStep());
        }
        assertEquals("Shouldn't have executed and should have returned null"+
                " after the last call.", 
                null, target.doStep());
        
        ensureAllExpectedCallsWerePerformed();
    }
    
    public void setUp()
    {
        chain = null;
        results = null;
        target = null;
    }
    
    public void testInvocationChainAdapter()
    {
        try {
            new InvocationChainAdapter(null);
            fail("Shouldn't accept null.");
        } catch (NullPointerException npe) {
            //OK, expected.
        }
    }
    
    public void testInvocationChainAdapter2()
    {
        try {
            new InvocationChainAdapter(new Invocation[0]);
            fail("Shouldn't accept 0-length array.");
        } catch (IllegalArgumentException iae) {
            //OK, expected.
        }
    }
    
    public void testInvocationChainAdapter3()
    {
        Invocation call = new MockInvocation();
        Invocation[] chain = new Invocation[] {null, call};
        try {
            new InvocationChainAdapter(chain);
            fail("Shouldn't accept null elements.");
        } catch (NullPointerException npe) {
            //OK, expected.
        }
        chain[0] = call;
        chain[1] = null;
        try {
            new InvocationChainAdapter(chain);
            fail("Shouldn't accept null elements.");
        } catch (NullPointerException npe) {
            //OK, expected.
        }
    }

    public void testIsDone() 
        throws Exception
    {
        for(int chainSize = 1; chainSize <= 10; ++chainSize) 
            doTestIsDone(chainSize);
    }
    
    public void testDoStep() 
        throws Exception
    {
        for(int chainSize = 1; chainSize <= 10; ++chainSize) 
            doTestDoStep(chainSize);
    }

}
