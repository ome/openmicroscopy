/*
 * org.openmicroscopy.shoola.env.data.model.CategoryData
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

package org.openmicroscopy.shoola.env.data.model;




//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
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
public class CategoryData
    implements DataObject
{

    private int                     id;
    private String                  name;
    private String                  description;
    private Map                     classifications;
    private CategoryGroupData       categoryGroupData;
    
    public CategoryData() {}
    
    public CategoryData(int id, String name, String description, 
                    CategoryGroupData categoryGroupData, Map classifications)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.classifications = classifications;
        this.categoryGroupData = categoryGroupData; 
    }
    
    public CategoryData(int id, String name, String description)
    {
        this.id = id;
        this.name = name;
        this.description = description;
    }
    
    /** Required by the DataObject interface. */
    public DataObject makeNew() {  return new CategoryData(); }
    
    public String toString() { return name; }

    public String getDescription() { return description; }
    
    public void setDescription(String description) 
    {
        this.description = description;
    }
    
    public int getID() { return id; }
    
    public void setID(int id) { this.id = id; }
    
    public String getName() { return name; }
    
    public void setName(String name) { this.name = name; }
    
    public void setClassifications(Map map) { classifications = map; }
    
    public Map getClassifications() { return classifications; }
    
    public float getConfidence(ImageSummary is)
    { 
        if (classifications == null)
            throw new NullPointerException("No classifications");
        if (is == null)
           throw new NullPointerException("ImageSummary cannot be null");
        return ((ClassificationData) classifications.get(is)).getConfidence();
    }
    
    public List getImages()
    { 
        List images = new ArrayList();
        if (classifications != null) {
            Iterator k = classifications.keySet().iterator();
            
            while (k.hasNext())
                images.add(((ImageSummary) k.next()).copyObject());  
        }
        return images;
    }
    
    public Map getImageClassifications()
    {
        Map ids = new HashMap();
        if (classifications != null) {
            Iterator k = classifications.keySet().iterator();
            ImageSummary is;
            Integer imageID;
            List l;
            while (k.hasNext()) {
                is = (ImageSummary) k.next();
                imageID = new Integer(is.getID());
                l = (List) ids.get(imageID);
                if (l == null) {
                    l = new ArrayList();
                    ids.put(imageID, l);
                }
                l.add(
                 ((ClassificationData) classifications.get(is)).copyObject());
            } 
        }
        return ids;
    }
    
    public CategoryGroupData getCategoryGroup() { return categoryGroupData; }
    
    public void setCategoryGroup(CategoryGroupData data)
    { 
        categoryGroupData = data;
    }
    
}
