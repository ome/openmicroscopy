/*
 * org.openmicroscopy.shoola.env.data.model.ModuleExecutionData
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
import java.util.Date;
import java.util.List;

//Third-party libraries

//Application-internal dependencies

/** 
 * An module execution object
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 *
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ModuleExecutionData implements DataObject
{

	private int id;
	private DatasetSummary dataset;
	private String dependence;
	private ImageSummary image;
	private String iteratorTag;
	private String newFeatureTag;
	private String timestamp;
	private Float totalTime;
	private String status;
	private String errorMessage;
	private List inputs;
	private boolean virtual;
	
	private Date date;
	
	public ModuleExecutionData() {}
	
	public ModuleExecutionData(int id,DatasetSummary dataset,String dependence,
			ImageSummary image,String iteratorTag,String newFeatureTag,
			String timestamp,Float totalTime,String status,String errorMessage,
			List inputs,boolean  virtual) 
	{	
		this.id = id;
		this.dataset = dataset;
		this.dependence =dependence;
		this.image = image;
		this.iteratorTag = iteratorTag;
		this.newFeatureTag = newFeatureTag;
		this.timestamp = timestamp;
		this.totalTime = totalTime;
		this.status = status;
		this.errorMessage = errorMessage;
		this.inputs = inputs;
		this.virtual = virtual;
	}
	
	/** Required by the DataObject interface. */
	public DataObject makeNew() { return new ModuleExecutionData(); }
	
	public int getID() {
		return id;
	}

	public DatasetSummary getDataset() {
		return dataset;
	}
	
	public String getDependence() {
		return dependence;
	}
	
	public void setID(int i) {
		id = i;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public ImageSummary getImage() {
		return image;
	}
	
	public List getInputs() {
		return inputs;
	}
	
	public String getIteratorTag() {
		return iteratorTag;
	}
	
	public String getNewFeatureTag() {
		return newFeatureTag;
	}
	
	public String getStatus() {
		return status;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	
	public Float getTotalTime() {
		return totalTime;
	}
	
	public Date getDate() {
		return date;
	}
	
	public boolean isVirtual() {
		return virtual;
	}
	
	public void setDataset(DatasetSummary dataset) {
		this.dataset = dataset;
	}
	public void setDependence(String dependence) {
		this.dependence = dependence;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public void setImage(ImageSummary image) {
		this.image = image;
	}
	
	public void setInputs(List inputs) {
		this.inputs = inputs;
	}
	
	public void setIteratorTag(String iteratorTag) {
		this.iteratorTag = iteratorTag;
	}
	
	public void setNewFeatureTag(String newFeatureTag) {
		this.newFeatureTag = newFeatureTag;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setTotalTime(Float totalTime) {
		this.totalTime = totalTime;
	}
	
	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
}
