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
import org.openmicroscopy.ds.dto.ModuleExecution;
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
        c.addWantedField("Valid");
        String column = (String) STSMapper.granularities.get(g);
        if (column != null) c.addFilter(column, new Integer(id));
        return c;
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
        c.addWantedField("module_execution");
        c.addWantedField("module_execution", "timestamp");
        c.addWantedField("module_execution", "experimenter");
        //Specify which fields we want for the owner.
        c.addWantedField("module_execution.experimenter", "id");
        c.addWantedField("module_execution.experimenter", "FirstName");
        c.addWantedField("module_execution.experimenter", "LastName");
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
        ModuleExecution mex;
        while (i.hasNext()) {
            imgA = (ImageAnnotation) i.next();
            list = new ArrayList();
            mex = imgA.getModuleExecution();
            if (mex.getTimestamp() != null)
                time = PrimitiveTypesMapper.getTimestamp(mex.getTimestamp());
            else time = PrimitiveTypesMapper.getDefaultTimestamp();
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
        DatasetAnnotation da;
        AnnotationData data;
        int ownerID;
        List list;
        Timestamp time = null;
        ModuleExecution mex;
        while (i.hasNext()) {
            da = (DatasetAnnotation) i.next();
            list = new ArrayList();
            mex = da.getModuleExecution();
            if (mex.getTimestamp() != null)
                time = PrimitiveTypesMapper.getTimestamp(mex.getTimestamp());
            else time = PrimitiveTypesMapper.getDefaultTimestamp();
            Experimenter experimenter = mex.getExperimenter();
            ownerID = experimenter.getID();
            data = new AnnotationData(da.getID(), ownerID, time);
            data.setAnnotation(da.getContent());
            data.setOwnerFirstName(experimenter.getFirstName());
            data.setOwnerLastName(experimenter.getLastName());
            list.add(data);
            map.put(new Integer(ownerID), list);
        }
    }
       
}
