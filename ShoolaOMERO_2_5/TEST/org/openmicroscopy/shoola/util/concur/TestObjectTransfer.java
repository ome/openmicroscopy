/*
 * org.openmicroscopy.shoola.util.concur.TestObjectTransfer
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

package org.openmicroscopy.shoola.util.concur;


//Java imports

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies

/** 
 * Verifies that {@link ObjectTransfer} works as a synchronous channel.
 * We use a separate thread to play the role of the producer/consumer, but
 * we don't take synchronization or state dependence explicitely into account
 * here &#151; even though most of the tests implicitly prove that the state 
 * dependence constructs are working correctly.
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
public class TestObjectTransfer
    extends TestCase
{

    private ObjectTransfer  target;  //Object under test;
    private Object          handOffObject;  //Producer puts it into target.
    private Object          collectObject;  //Sotres what consumer's collected.
    private Throwable       exc;  //To transfer any (unexpected) exc to JUnit.
    private class Producer implements Runnable
    {
        public void run() {
            try {
                target.handOff(handOffObject);
            } catch (Throwable interruptedException) {
                //Should never happen, as we don't interrupt.  Moreover, we're
                //not expecting anything different from IE.  But just in case:
                exc = interruptedException;
            }
        }
    }
    private class Consumer implements Runnable
    {
        public void run() {
            try {
                collectObject = target.collect();
            } catch (Throwable interruptedException) {
                //Should never happen, as we don't interrupt.  Moreover, we're
                //not expecting anything different from IE.  But just in case:
                exc = interruptedException;
            }
        }
    }
    
    public void setUp()
    {
        target = new ObjectTransfer();
        handOffObject = new Object();
        collectObject = new Object();
        exc = null;
    }
    
    public void testHandOffNull()
        throws InterruptedException
    {
        try {
            target.handOff(null);
            fail("handOff shouldn't accept null.");
        } catch (NullPointerException npe) {
            //OK, expected.
        }
        try {
            target.handOff(null, 100);
            fail("handOff shouldn't accept null.");
        } catch (NullPointerException npe) {
            //OK, expected.
        }
        try {
            target.handOff(null, 0);
            fail("handOff shouldn't accept null.");
        } catch (NullPointerException npe) {
            //OK, expected.
        }
    }
    
    public void testHandOffUnboundedWait()
        throws InterruptedException
    {
        Thread consumer = new Thread(new Consumer()); 
        consumer.start();
        target.handOff(handOffObject);  //Returns only after collect().
        
        //Wait for consumer to die b/f accessing any data written by it.
        //We need to be sure its working memory will be flushed and changes
        //made visible to JUnit thread b/f accessing exc and collectObject.
        consumer.join();
        
        assertNull("Unexpected exception in the consumer thread.", exc);
        assertEquals("Consumer didn't retrieve object handed off by producer.", 
                handOffObject, collectObject);
    }
    
    public void testHandOffBoundedWait()
        throws InterruptedException
    {
        Thread consumer = new Thread(new Consumer()); 
        consumer.start();
        target.handOff(handOffObject, 100);  //Returns only after collect().
        
        //Wait for consumer to die b/f accessing any data written by it.
        //We need to be sure its working memory will be flushed and changes
        //made visible to JUnit thread b/f accessing exc and collectObject.
        consumer.join();
        
        assertNull("Unexpected exception in the consumer thread.", exc);
        assertEquals("Consumer didn't retrieve object handed off by producer.", 
                handOffObject, collectObject);
    }
    
    public void testHandOffNoWait()
        throws InterruptedException
    {
        Thread consumer = new Thread(new Consumer()); 
        consumer.start();
        target.handOff(handOffObject, 0);  //Returns only after collect().
        
        //Wait for consumer to die b/f accessing any data written by it.
        //We need to be sure its working memory will be flushed and changes
        //made visible to JUnit thread b/f accessing exc and collectObject.
        consumer.join();

        assertNull("Unexpected exception in the consumer thread.", exc);
        assertEquals("Consumer didn't retrieve object handed off by producer.", 
                handOffObject, collectObject);
    }
    
    public void testCollectUnboundedWait()
        throws InterruptedException
    {
        new Thread(new Producer()).start();
        collectObject = target.collect();  //Returns only after obj handed off.
        assertNull("Unexpected exception in the producer thread.", exc);
        assertEquals("Consumer didn't retrieve object handed off by producer.", 
                handOffObject, collectObject);
    }
    
    public void testCollectBoundedWait()
        throws InterruptedException
    {
        new Thread(new Producer()).start();
        do {  //The following will return null until object's gone thru.
            collectObject = target.collect(100);  
        } while (collectObject == null);
        assertNull("Unexpected exception in the producer thread.", exc);
        assertEquals("Consumer didn't retrieve object handed off by producer.", 
                handOffObject, collectObject);
    }
    
    public void testCollectNoWait()
        throws InterruptedException
    {
        new Thread(new Producer()).start();
        do {  //The following will return null until object's gone thru.
            collectObject = target.collect(0);
        } while (collectObject == null);
        assertNull("Unexpected exception in the producer thread.", exc);
        assertEquals("Consumer didn't retrieve object handed off by producer.", 
                handOffObject, collectObject);
    }
    
}
