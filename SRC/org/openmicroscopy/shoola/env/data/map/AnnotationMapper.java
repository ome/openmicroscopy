/*
 * org.openmicroscopy.shoola.env.data.map.AnnotationMapper
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

package org.openmicroscopy.shoola.env.data.map;

//Java imports
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.st.DatasetAnnotation;
import org.openmicroscopy.ds.st.Experimenter;
import org.openmicroscopy.ds.st.ImageAnnotation;
import org.openmicroscopy.shoola.env.data.model.AnnotationData;


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
public class AnnotationMapper
{
    
    /** 
     * 
     * @param imageID      specified project to retrieve.
     */
    public static Criteria buildImageAnnotationCriteria(int imageID)
    {
        Criteria c = new Criteria();
        fillAnnotationCriteria(c);
        c.addWantedField("TheZ");
        c.addWantedField("TheT");
        c.addFilter("image_id", new Integer(imageID));
        return c;
    }
    
    public static Criteria buildBasicCriteria(String g, int id)
    {
        Criteria c = new Criteria();
        fillBasic(c, g, id);
        return c;
    }
    
    public static Criteria buildBasicImageCriteria(String g, int id)
    {
        Criteria c = new Criteria();
        fillBasic(c, g, id);
        c.addWantedField("TheZ");
        c.addWantedField("TheT");
        return c;
    }
    
    private static void fillBasic(Criteria c, String g, int id)
    {
        c.addWantedField("Valid");
        c.addWantedField("Content");
        String column = (String) STSMapper.granularities.get(g);
        if (column != null) c.addFilter(column, new Integer(id));
    }
    
    public static Criteria buildDatasetAnnotationCriteria(int datasetID)
    {
        Criteria c = new Criteria();
        fillAnnotationCriteria(c);
        c.addFilter("dataset_id", new Integer(datasetID));
        return c;
    }
    
    public static void fillAnnotationCriteria(Criteria c)
    {
        //c.addWantedField("id");
        c.addWantedField("Content");
        c.addWantedField("Timestamp");
        c.addWantedField("Experimenter");
        //Specify which fields we want for the owner.
        c.addWantedField("Experimenter", "id");
        c.addWantedField("Experimenter", "FirstName");
        c.addWantedField("Experimenter", "LastName");
        c.addFilter("Valid", Boolean.TRUE);
    }
    
    public static Map fillImageAnnotations(List l, TreeMap map)
    {
        Iterator i = l.iterator();
        ImageAnnotation imgA;
        AnnotationData data;
        int ownerID;
        List list;
        Timestamp time = null;
        Experimenter experimenter;
        while (i.hasNext()) {
            imgA = (ImageAnnotation) i.next();
            list = new ArrayList();
            if (imgA.getTimestamp() != null)
                time = new Timestamp(imgA.getTimestamp().longValue());
            else time = getTimestamp();
            experimenter = imgA.getExperimenter();
            ownerID = experimenter.getID();
            data = new AnnotationData(imgA.getID(), ownerID, time);
            data.setAnnotation(imgA.getContent());
            data.setOwnerFirstName(experimenter.getFirstName());
            data.setOwnerLastName(experimenter.getLastName());
            if (imgA.getTheZ() != null)
                data.setTheZ(imgA.getTheZ().intValue());
            if (imgA.getTheT() != null)
                data.setTheT(imgA.getTheT().intValue());
            list.add(data);
            map.put(new Integer(ownerID), list);
        }
        return map;
    }

    public static void fillDatasetAnnotations(List l, Map map)
    {
        Iterator i = l.iterator();
        DatasetAnnotation imgA;
        AnnotationData data;
        int ownerID;
        List list;
        Timestamp time = null;
        while (i.hasNext()) {
            imgA = (DatasetAnnotation) i.next();
            list = new ArrayList();
            if (imgA.getTimestamp() != null)
                time = new Timestamp(imgA.getTimestamp().longValue());
            else time = getTimestamp();
            Experimenter experimenter = imgA.getExperimenter();
            ownerID = experimenter.getID();
            data = new AnnotationData(imgA.getID(), ownerID, time);
            data.setAnnotation(imgA.getContent());
            data.setOwnerFirstName(experimenter.getFirstName());
            data.setOwnerLastName(experimenter.getLastName());
            list.add(data);
            map.put(new Integer(ownerID), list);
        }
    }
    
    public static Timestamp getTimestamp()
    {
        java.util.Date today = new java.util.Date();
        return new Timestamp(today.getTime());
    }
    
}
