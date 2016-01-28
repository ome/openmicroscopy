/*
 * org.openmicroscopy.shoola.env.data.views.calls.ExperimenterImagesCounter 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.views.calls;



//Java imports
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import omero.log.LogMessage;

/** 
* Retrieves the images imported by the specified user during various
* periods of time.
*
* @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* @since OME3.0
*/
public class ExperimenterImagesCounter
    extends BatchCallTree
{

    /** The id of the user the count is for. */
    private long userID;

    /** The lastly retrieved count along side the index. */
    private Map<Integer, Object> result;

    /** The nodes to handle. */
    private Map<Integer, TimeRefObject> nodes;

    /** Helper reference to the data service. */
    private OmeroDataService os;

    /** Helper reference to the metadata service. */
    private OmeroMetadataService ms;

    /** The security context.*/
    private SecurityContext ctx;

    /** 
     * Counts the number of images imported during a given period of time.
     * 
     * @param index The index identifying the period.
     * @param start The lower bound of the time interval.
     * @param end The upper bound of the time interval.
     */
    private void countTimeItems(Integer index, Timestamp start, Timestamp end)
    {
        try {
            int number = -1;
            Collection l;
            result = new HashMap<Integer, Object>(1);
            if (start == null || end == null) {
                l = os.getImagesPeriod(ctx, start, end, userID, false);
                if (l != null) number = l.size();
                result.put(index, number);
            } else {
                l = os.getImagesAllPeriodCount(ctx, start, end, userID);
                result.put(index, l);
            }
        } catch (Exception e) {
            LogMessage msg = new LogMessage();
            msg.print("Cannot count the number of items imported during the " +
                    "specified period");
            msg.print(e);
            context.getLogger().error(this, msg);
        }
    }

    /**
     * Counts the number of a given types.
     * 
     * @param index The index identifying the period.
     * @param type  The type of files.
     */
    private void countFileItems(Integer index, int type)
    {
        try {
            result = new HashMap<Integer, Object>();
            result.put(index, ms.countFileType(ctx, userID, type));
        } catch (Exception e) {
            LogMessage msg = new LogMessage();
            msg.print("Cannot count the number of items imported during the " +
                    "specified period");
            msg.print(e);
            context.getLogger().error(this, msg);
        }
    }

    /**
     * Counts the number of items imported during a period of time.
     * 
     * @param index	The index identifying the period.
     * @param ref	The object containing period information.
     */
    private void countItems(Integer index, TimeRefObject ref)
    {
        switch (ref.getIndex()) {
        case TimeRefObject.TIME:
            countTimeItems(index, ref.getStartTime(), ref.getEndTime());
            break;
        case TimeRefObject.FILE:
            countFileItems(index, ref.getFileType());
        }
    }

    /**
     * Adds a {@link BatchCall} to the tree for each container.
     * The batch call invokes {@link #countItems(Integer, TimeRefObject)}.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree()
    {
        String description;
        Iterator<Integer> i = nodes.keySet().iterator();
        while (i.hasNext()) {
            final Integer index = i.next();
            description = "Count items";
            final TimeRefObject ref = nodes.get(index);
            add(new BatchCall(description) {
                public void doCall() { countItems(index, ref); }
            });   
        }
    }

    /**
     * Returns the lastly retrieved count.
     * This will be packed by the framework into a feedback event and
     * sent to the provided call observer, if any.
     * 
     * @return 	A Map whose key is the index of the time node and the value
     * 			the number of items.
     */
    protected Object getPartialResult() { return result; }

    /**
     * Returns <code>null</code> as there's no final result.
     * In fact, count are progressively delivered with feedback events. 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return null; }

    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param userID The id of the user the count is for.
     * @param m The elements to handle.
     */
    public ExperimenterImagesCounter(SecurityContext ctx, long userID,
            Map<Integer, TimeRefObject> m)
    {
        if (m == null || m.size() == 0)
            throw new IllegalArgumentException("No nodes specified.");
        this.userID = userID;
        this.ctx = ctx;
        nodes = m;
        os = context.getDataService();
        ms = context.getMetadataService();
    }

}
