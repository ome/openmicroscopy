/*
 * org.openmicroscopy.shoola.env.rnd.data.MockPixelsServiceAdapter
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

package org.openmicroscopy.shoola.env.rnd.data;


//Java imports
import java.io.InputStream;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.DataFactory;
import org.openmicroscopy.ds.XmlRpcCaller;
import org.openmicroscopy.ds.st.Pixels;
import org.openmicroscopy.is.ImageServerException;
import org.openmicroscopy.shoola.env.data.PixelsServiceAdapter;
import util.mocks.IMock;
import util.mocks.MethodSignature;
import util.mocks.MockSupport;
import util.mocks.MockedCall;

/** 
 * Mock object.
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
public class MockPixelsServiceAdapter
    extends PixelsServiceAdapter
    implements IMock
{
    
    private static final MethodSignature getStackStream = 
        new MethodSignature(
            MethodSignature.PUBLIC, InputStream.class, 
            "getStackStream", 
            new Class[] {Pixels.class, int.class, int.class, boolean.class});
    
    
    private MockSupport     mockSupport;
    
    
    MockPixelsServiceAdapter()
    {
        super(new DataFactory(new XmlRpcCaller()));
        mockSupport = new MockSupport();
    }
 
    //Used in set up mode.  If e != null then it must be either an instance
    //of ImageServerException or RuntimeException.  If e != null then it will
    //be thrown in verification mode. 
    public void getStackStream(Pixels pixels, int theC, int theT,
            boolean bigEndian, InputStream retVal, Exception e)
    {
        Object[] args = new Object[] {pixels, new Integer(theC), 
                new Integer(theT), new Boolean(bigEndian)};
        MockedCall mc = new MockedCall(getStackStream, args, retVal);
        if (e != null) mc.setException(e);
        mockSupport.add(mc);
    }
    
    //Used in verification mode.
    public InputStream getStackStream(Pixels pixels, int theC, int theT,
            boolean bigEndian)
        throws ImageServerException
    {
        Object[] args = new Object[] {pixels, new Integer(theC), 
                new Integer(theT), new Boolean(bigEndian)};
        MockedCall mc = new MockedCall(getStackStream, 
                                        args, (InputStream) null);
        mc = mockSupport.verifyCall(mc);
        if (mc.hasException()) {
            Exception e = (Exception) mc.getException();
            if (e instanceof ImageServerException) 
                throw (ImageServerException) e;
            throw (RuntimeException) e;
        }
        InputStream r = (InputStream) mc.getResult();
        return r;
    }
    
    /**
     * @see util.mocks.IMock#activate()
     */
    public void activate()
    {
        mockSupport.activate(); 
    }
    
    /**
     * @see util.mocks.IMock#verify()
     */
    public void verify()
    {
        mockSupport.verifyCallSequence();
    }
    
}
