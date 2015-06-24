/*
 * org.openmicroscopy.shoola.env.data.model.DeletableObject 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.env.data.model;


//Java imports
import java.util.List;

import org.openmicroscopy.shoola.env.data.util.SecurityContext;

//Third-party libraries

//Application-internal dependencies
import pojos.DatasetData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.MapAnnotationData;
import pojos.PlateAcquisitionData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ROIData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.TermAnnotationData;

/** 
 * Hosts the parameters to delete. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class DeletableObject
{

	/** The data object to delete. */
	private pojos.DataObject	objectToDelete;
	
	/** 
	 * Flag indicating to delete the objects contained in the object to delete,
	 * the flag will be taken into account when the object to delete is a 
	 * container e.g. <code>Project</code>.
	 */
	private boolean				content;
	
	/** The collection of annotations to keep e.g. TagAnnotationData. */
	private List<Class> 		annotations;
	
	/** The report of the delete action. */
	private List<String>		report;
	
	/** The number of errors. */
	private int		            numberOfErrors;
	
	/** The security context.*/
	private SecurityContext ctx;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param objectToDelete 	The object to delete.
	 * @param content			Pass <code>true</code> to delete the objects
	 * 							contained in the object to delete, 
	 * 							<code>false</code> otherwise.
	 */
	public DeletableObject(pojos.DataObject objectToDelete, boolean content)
	{
		if (objectToDelete == null) 
			throw new IllegalArgumentException("No object to delete.");
		this.objectToDelete = objectToDelete;
		this.content = content;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param objectToDelete The object to delete.
	 */
	public DeletableObject(pojos.DataObject objectToDelete)
	{
		this(objectToDelete, false);
	}
	
	/** 
	 * Returns <code>true</code> if the objects contained in the object to 
	 * delete have to be deleted, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean deleteContent() { return content; }
	
	/**
	 * Returns the types of keep. All annotations will be deleted if 
	 * empty or <code>null</code>.
	 * 
	 * @return See above.
	 */
	public List<Class> getAnnotations() { return annotations; }
	
	/**
	 * Sets the types of annotations to keep.
	 * 
	 * @param annotations The types of annotations to keep.
	 */
	public void setAttachmentTypes(List<Class> annotations)
	{
		this.annotations = annotations;
	}

	/**
	 * Returns the object to delete.
	 * 
	 * @return See above.
	 */
	public pojos.DataObject getObjectToDelete() { return objectToDelete; }
	
	/**
	 * Returns the identifier of the group.
	 * 
	 * @return See above.
	 */
	public long getGroupId()
	{
		if (ctx != null) return ctx.getGroupID();
		return objectToDelete.getGroupId();
	}
	
	/**
	 * Sets the report of the delete action.
	 * 
	 * @param report The value to set.
	 */
	public void setReport(List<String> report) { this.report = report; }
	
	/**
	 * Returns the report of the delete action.
	 * 
	 * @return See above.
	 */
	public List<String> getReport() { return report; }

	/**
	 * Returns the number of reports.
	 * 
	 * @return See above.
	 */
	public int getNumberOfErrors() { return numberOfErrors; }
	
	/** 
	 * Returns the type of object to delete.
	 * 
	 * @return See above.
	 */
	public String getType()
	{
		if (objectToDelete instanceof ProjectData) {
			return "Project";
		} else if (objectToDelete instanceof DatasetData) {
			return "Dataset";
		} else if (objectToDelete instanceof ImageData) {
			return "Image";
		} else if (objectToDelete instanceof ScreenData) {
			return "Screen";
		} else if (objectToDelete instanceof ROIData) {
			return "Roi";
		} else if (objectToDelete instanceof TagAnnotationData) {
			return "Tag";
		} else if (objectToDelete instanceof TermAnnotationData) {
			return "Ontology Term";
		} else if (objectToDelete instanceof PlateData) {
			return "Plate";
		} else if (objectToDelete instanceof PlateAcquisitionData) {
			return "PlateAcquisition";
		} else if (objectToDelete instanceof FileAnnotationData) {
			return "File";
		} else if (objectToDelete instanceof MapAnnotationData) {
			return "Map Annotation";
		}
		return "";
	}
	
	/**
	 * Returns the type of object to delete as a string.
	 * 
	 * @return See above.
	 */
	public String getMessage()
	{
		if (objectToDelete instanceof ProjectData) {
			return ((ProjectData) objectToDelete).getName();
		} else if (objectToDelete instanceof DatasetData) {
			return ((DatasetData) objectToDelete).getName();
		} else if (objectToDelete instanceof ImageData) {
			return ((ImageData) objectToDelete).getName();
		} else if (objectToDelete instanceof ScreenData) {
			return ((ScreenData) objectToDelete).getName();
		} else if (objectToDelete instanceof ROIData) {
			return "ROI for:"+
			((ROIData) objectToDelete).getImage().getName();
		} else if (objectToDelete instanceof TagAnnotationData) {
			return ((TagAnnotationData) objectToDelete).getTagValue();
		} else if (objectToDelete instanceof TermAnnotationData) {
			return ((TermAnnotationData) objectToDelete).getTerm();
		} else if (objectToDelete instanceof PlateData) {
			return ((PlateData) objectToDelete).getName();
		} else if (objectToDelete instanceof PlateAcquisitionData) {
			return ((PlateAcquisitionData) objectToDelete).getLabel();
		} else if (objectToDelete instanceof FileAnnotationData) {
			return ((FileAnnotationData) objectToDelete).getFileName();
		} else if (objectToDelete instanceof MapAnnotationData) {
			return "Map Annotation";
		}
		return "";
	}

	/**
	 * Returns the security context.
	 * 
	 * @return See above.
	 */
	public SecurityContext getSecurityContext() { return ctx; }
	
	/**
	 * Sets the security context.
	 * 
	 * @param ctx The security context.
	 */
	public void setSecurityContext(SecurityContext ctx) { this.ctx = ctx; }
	
}
