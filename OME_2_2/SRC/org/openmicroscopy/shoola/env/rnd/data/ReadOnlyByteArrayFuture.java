/*
 * org.openmicroscopy.shoola.env.rnd.data.ReadOnlyByteArrayFuture
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.concur.tasks.ExecException;
import org.openmicroscopy.shoola.util.concur.tasks.Future;
import org.openmicroscopy.shoola.util.mem.ReadOnlyByteArray;

/** 
 * Allows to collect a {@link ReadOnlyByteArray} object that is the result of
 * an asnchronous data retrieval.
 * It simply wraps the {@link Future} object that is used to collect the
 * result.
 *
 * @see org.openmicroscopy.shoola.util.concur.tasks.Future
 * @see org.openmicroscopy.shoola.util.concur.tasks.CmdProcessor
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
class ReadOnlyByteArrayFuture
    extends ReadOnlyByteArray
{

    /** 
     * The actual data.  
     * Will be <code>null</code> until the data retrieval is finished. 
     */
    private ReadOnlyByteArray   data;
    
    /** Used to collect the result of the data retrieval. */
    private Future              dataFuture;

    
    /**
     * Creates a new instance.
     * 
     * @param dataFuture To collect the result of the data retrieval.
     */
    ReadOnlyByteArrayFuture(Future dataFuture)
    {
        super(new byte[0], 0, 0);
        if (dataFuture == null) 
            throw new NullPointerException("No data future.");
        this.dataFuture = dataFuture;
        this.data = null;
    }
    
    /**
     * Returns the result of the data retrieval.
     * If the data hasn't been retrieved yet, it blocks until it becomes
     * available or an error occurs.
     * 
     * @return See above.
     * @throws DataSourceException If an error occurs during the retrieval
     *                              of the data.
     */
    ReadOnlyByteArray getData()
        throws DataSourceException
    {
        if (data != null) return data;
        try {
            data = (ReadOnlyByteArray) dataFuture.getResult();
            dataFuture = null;  //Allow GC.
        } catch (InterruptedException ie) {
            //This should never happen if rendering is done in the Swing thread,
            //as we should never interrupt said thread.  If rendering is done
            //in a separate thread (see RenderingManager), then this is caused
            //by cancellation.  In this case the exception is irrelevant.
            throw new DataSourceException(ie);  
        } catch (ExecException ee) {
            throw new DataSourceException(ee);
        }
        return data;
    }
    
}
