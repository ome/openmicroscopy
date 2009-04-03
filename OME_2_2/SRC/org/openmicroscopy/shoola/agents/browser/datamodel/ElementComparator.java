/*
 * org.openmicroscopy.shoola.agents.browser.util.ElementComparator
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser.datamodel;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmicroscopy.ds.dto.Attribute;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;
import org.openmicroscopy.shoola.agents.browser.images.ThumbnailDataModel;

/**
 * Compares and orders thumbnails by the value of a particular element.  The
 * comparator is optimized to retain state if the same object is compared, as
 * computation is not trivial.  Thus, there is an inherent assumption that over
 * the course of the use of this comparator, the values of the thumbnails being
 * compared will not change, --OR-- the clearState() method will be invoked
 * prior to reuse.  This statefulness is used as a quasi-hack to maintain access
 * to recent searches/comparisons
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.1
 * @since OME2.2.1
 */
public class ElementComparator implements Comparator
{
    /**
     * The name of the attribute containing the element to compare by.
     */
    protected String attributeName;
    
    /**
     * The name of the element to compare by.
     */
    protected String elementName;
    
    /**
     * The mode which indicates which value (in the case of multiple-image
     * thumbnails) to extract.
     */
    protected DisplayValueMode multiDisplayMode;
    
    protected Map valueMap;
    
    /**
     * Constructs a comparator and stores the element that it should compare.
     * 
     * @param attributeName The name of the attribute containing the element.
     * @param elementName The name of the element to compare across images.
     * @param multiValueMode How to extract information from a thumbnail with
     *                       multiple attributes of the same type.
     * @throws IllegalArgumentException If either parameter is null.
     */
    public ElementComparator(String attributeName,
                             String elementName,
                             DisplayValueMode multiValueMode)
        throws IllegalArgumentException
    {
        if(attributeName == null || elementName == null)
        {
            throw new IllegalArgumentException("Null parameters");
        }
        this.attributeName = attributeName;
        this.elementName = elementName;
        this.multiDisplayMode = multiValueMode;
        valueMap = new HashMap();
    }
    
    /**
     * Compares the two element of the specified thumbnails.  The accepted objects
     * can be Thumbnails or ThumbnailDataModels.
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     * @throws IllegalArgumentException If the objects are not thumbnails or
     *                                  TDMs.
     */
    public int compare(Object arg0, Object arg1)
        throws IllegalArgumentException
    {
        ThumbnailDataModel tdm1;
        ThumbnailDataModel tdm2;
        
        if(arg0 == null || arg1 == null)
        {
            throw new IllegalArgumentException("Null parameters");
        }
        
        // convert first object
        if(arg0 instanceof Thumbnail) {
            tdm1 = ((Thumbnail)arg0).getModel();
        }
        else if(arg0 instanceof ThumbnailDataModel) {
            tdm1 = (ThumbnailDataModel)arg0;
        }
        else {
            throw new IllegalArgumentException("The first object cannot be compared");
        }
        
        // convert second object
        if(arg1 instanceof Thumbnail) {
            tdm2 = ((Thumbnail)arg1).getModel(); // just get topmost one
        }
        else if(arg1 instanceof ThumbnailDataModel) {
            tdm2 = (ThumbnailDataModel)arg1;
        }
        else {
            throw new IllegalArgumentException("The second object cannot be compared");
        }
        
        return compareTDMs(tdm1,tdm2);
    }
    
    /*
     * Return a comparison value based on two thumbnail data models.  If one of
     * the objects has a null or non-numerical value for the attribute/element
     * pair specified at construction, this method will throw an exception.
     */
    private int compareTDMs(ThumbnailDataModel tdm1,
                            ThumbnailDataModel tdm2)
    {
        double value1, value2;
        
        // NB: here's where the state assumption kicks in.  the TDMs should not
        // change over the lifespan of this comparator, *or* instead if they do,
        // clearState() should be called.
        if(valueMap.containsKey(tdm1))
        {
            value1 = ((Double)valueMap.get(tdm1)).doubleValue();
        }
        else
        {
            AttributeMap am1 = tdm1.getAttributeMap();
            List att1 = am1.getAttributes(attributeName);
            if(att1 == null || att1.size() == 0)
            {
                throw new IllegalArgumentException("Invalid attribute");
            }
            Attribute[] a1 = new Attribute[att1.size()];
            att1.toArray(a1);
            value1 = multiDisplayMode.computeValue(a1,elementName);
            valueMap.put(tdm1,new Double(value1));
        }
            
        if(valueMap.containsKey(tdm2))
        {
            value2 = ((Double)valueMap.get(tdm2)).doubleValue();
        }
        else
        {
            AttributeMap am2 = tdm2.getAttributeMap();
            List att2 = am2.getAttributes(attributeName);
            if(att2 == null || att2.size() == 0)
            {
                throw new IllegalArgumentException("Invalid attribute");
            }
            Attribute[] a2 = new Attribute[att2.size()];
            att2.toArray(a2);
            value2 = multiDisplayMode.computeValue(a2,elementName);
            valueMap.put(tdm2,new Double(value2));
        }
        
        if(value1 == value2)
        {
            return Double.compare(tdm1.getID(),tdm2.getID());
        }
        else return Double.compare(value1,value2);
    }
    
    /**
     * Clears the persistent state maintained by this comparator-- namely, the
     * value extracted from each thumbnail.
     */
    public void clearState()
    {
        valueMap.clear();
    }
    
    /**
     * Returns the value state determined over the course of the comparison.
     * Kind of hackish, but OK.
     * 
     * @param thumbObject The object to lookup.
     * @return The value associated with the object.
     * @throws IllegalArgumentException If the object is not a thumbnail or TDM.
     */
    public Number getState(Object thumbObject)
        throws IllegalArgumentException
    {
        if(thumbObject == null) return null;
        ThumbnailDataModel key = null;
        if(thumbObject instanceof Thumbnail)
        {
            key = ((Thumbnail)thumbObject).getModel();
        }
        else if(thumbObject instanceof ThumbnailDataModel)
        {
            key = (ThumbnailDataModel)thumbObject;
        }
        else
        {
            throw new IllegalArgumentException("Invalid retrieval type");
        }
        return (Number)valueMap.get(key);
    }
}
