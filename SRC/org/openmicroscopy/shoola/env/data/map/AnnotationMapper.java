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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    public static Criteria buildImageAnnotationCriteria(List ids, int userID)
    {
        Criteria c = new Criteria();
        fillAnnotationCriteria(c);
        c.addWantedField("TheZ");
        c.addWantedField("TheT");
        c.addWantedField("image");
        if (ids != null) c.addFilter("image_id", "IN", ids);
        //Retrieve user's annotation only
        if (userID != -1)
            c.addFilter("module_execution.experimenter_id", 
                    new Integer(userID));
        return c;
    }
    
    public static Criteria buildBasicCriteria(int id)
    {
        Criteria c = new Criteria();
        c.addWantedField("Valid");
        c.addFilter("id", new Integer(id));
        return c;
    }
   
    public static Criteria buildDatasetAnnotationCriteria(List datasetIDs, 
                                                    int userID)
    {
        Criteria c = new Criteria();
        fillAnnotationCriteria(c);
        c.addWantedField("dataset");
        if (datasetIDs != null) c.addFilter("dataset_id", "IN", datasetIDs);
        //Retrieve user's annotation only
        if (userID != -1)
            c.addFilter("module_execution.experimenter_id", 
                    new Integer(userID));
        return c;
    }
    
    public static Criteria buildDatasetAnnotationCriteria(int datasetID)
    {
        Criteria c = new Criteria();
        fillAnnotationCriteria(c);
        c.addWantedField("dataset");
        c.addFilter("dataset_id", new Integer(datasetID));
        return c;
    }
    
    public static void fillAnnotationCriteria(Criteria c)
    {
        c.addWantedField("Content");
        c.addWantedField("module_execution");
        c.addWantedField("module_execution", "timestamp");
        c.addWantedField("module_execution", "experimenter");
        //Specify which fields we want for the owner.
        c.addWantedField("module_execution.experimenter", "FirstName");
        c.addWantedField("module_execution.experimenter", "LastName");
        c.addFilter("Valid", Boolean.TRUE);
    }
    
    public static Map reverseListImageAnnotations(List annotations)
    {
        Map map = new HashMap();
        if (annotations == null) return map;
        Iterator i = annotations.iterator();
        ImageAnnotation annotation;
        while (i.hasNext()) {
            annotation = (ImageAnnotation) i.next();
            map.put(new Integer(annotation.getImage().getID()), annotation);
        }
        return map;
    }
    
    public static Map reverseListDatasetAnnotations(List annotations)
    {
        Map map = new HashMap();
        if (annotations == null) return map;
        Iterator i = annotations.iterator();
        DatasetAnnotation annotation;
        while (i.hasNext()) {
            annotation = (DatasetAnnotation) i.next();
            map.put(new Integer(annotation.getDataset().getID()), annotation);
        }
        return map;
    }
    
    /** Fill in the corresponding Annotation object. */
    public static void fillImageAnnotations(List l, Map map)
    {
        Iterator i = l.iterator();
        List list;
        Object[] results;
        while (i.hasNext()) {
            results = getImageAnnotation((ImageAnnotation) i.next());
            list = new ArrayList();
            list.add(results[0]);
            map.put(results[1], list);
        }
    }
    
    public static AnnotationData fillImageAnnotation(ImageAnnotation annotation)
    {
        if (annotation == null) return null;
        Object[] results = getImageAnnotation(annotation);
        return (AnnotationData) results[0];
    }

    /** 
     * Given a list of {@link DatasetAnnotation}, 
     * fill the corresponding map. 
     */
    public static void fillDatasetAnnotations(List l, Map map)
    {
        Iterator i = l.iterator();
        List list;
        Object[] results;
        while (i.hasNext()) {
            results = getDatasetAnnotation((DatasetAnnotation) i.next());
            list = new ArrayList();
            list.add(results[0]);
            map.put(results[1], list);
        }
    }
    
    public static AnnotationData fillDatasetAnnotation(DatasetAnnotation 
                                                        annotation)
    {
        if (annotation == null) return null;
        Object[] results = getDatasetAnnotation(annotation);
        return (AnnotationData) results[0];
    }

    /** Fill in the AnnotationData object. */
    private static Object[] getDatasetAnnotation(DatasetAnnotation annotation)
    {
        Object[] results = new Object[2];
        AnnotationData data;
        if (annotation != null) {
            Timestamp time = null;
            ModuleExecution mex = annotation.getModuleExecution();
            if (mex.getTimestamp() != null)
                time = PrimitiveTypesMapper.getTimestamp(mex.getTimestamp());
            else time = PrimitiveTypesMapper.getDefaultTimestamp();
            Experimenter experimenter = mex.getExperimenter();
            int ownerID = experimenter.getID();
            data = new AnnotationData(annotation.getID(), ownerID, time);
            data.setAnnotation(annotation.getContent());
            data.setOwnerFirstName(experimenter.getFirstName());
            data.setOwnerLastName(experimenter.getLastName());
            results[0] = data;
            results[1] = new Integer(ownerID);
        }
        return results;
    }
     
    /** Fill in the AnnotationData object. */
    private static Object[] getImageAnnotation(ImageAnnotation annotation)
    {
        Object[] results = new Object[2];
        AnnotationData data;
        if (annotation != null) {
            int ownerID;
            Timestamp time = null;
            Experimenter experimenter;
            ModuleExecution mex;
            mex = annotation.getModuleExecution();
            if (mex.getTimestamp() != null)
                time = PrimitiveTypesMapper.getTimestamp(mex.getTimestamp());
            else time = PrimitiveTypesMapper.getDefaultTimestamp();
            experimenter = mex.getExperimenter();
            ownerID = experimenter.getID();
            data = new AnnotationData(annotation.getID(), ownerID, time);
            data.setAnnotation(annotation.getContent());
            data.setOwnerFirstName(experimenter.getFirstName());
            data.setOwnerLastName(experimenter.getLastName());
            if (annotation.getTheZ() != null)
                data.setTheZ(annotation.getTheZ().intValue());
            if (annotation.getTheT() != null)
                data.setTheT(annotation.getTheT().intValue());
            results[0] = data;
            results[1] = new Integer(ownerID);
        }
        return results;  
    }
    
}
