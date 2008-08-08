 /*
 * fields.IField 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package treeModel.fields;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * This interface specifies methods of a Field, which corresponds to a node
 * in the data tree.
 * IField extends the IValue interface, which has getAttribute() and setAttribute() 
 * methods.
 * A field may contain several parameters, so the IField has methods to get the 
 * count of parameters, and to get a parameter by index.
 * 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public interface IField 
	extends IAttributes {
	
	/**
	 * Returns the number of parameters for this field
	 * 
	 * @return 	the number of parameters for this field
	 */
	public int getParamCount();
	
	/**
	 * Returns a parameter object at the specified index
	 * 
	 * @param index		
	 * @return
	 */
	public IParam getParamAt(int index);
	
	/**
	 * Adds a parameter object to the field
	 * 
	 * @param param
	 */
	public void addParam(IParam param);
	
	/**
	 * Adds a parameter to the list for this field,
	 * at the specified index.
	 */
	public void addParam(int index, IParam param);
	
	/**
	 * Removes a parameter object from the field
	 * 
	 * @param param		The object to remove
	 * @return		True if the object was found and removed.
	 */
	public boolean removeParam(IParam param);
	
	/**
	 * Indicates whether all parameters have been filled out. 
	 * 
	 * @return		True if all the parameters have been filled. 
	 */
	public boolean isFieldFilled();

	/**
	 * UI classes may want to set display attributes that do not affect
	 * the data (eg collapsed state, description visible). 
	 * 
	 * @param name	The name of the display attribute. 
	 * @param value		The new value of the attribute
	 */
	public void setDisplayAttribute(String name, String value);
	
	
	/**
	 * UI classes may want to get display attributes that do not affect
	 * the data (eg collapsed state, description visible). 
	 * 
	 * @param name		The named attribute
	 * @return 			The value of the attribute
	 */
	public String getDisplayAttribute(String name);
	
}
