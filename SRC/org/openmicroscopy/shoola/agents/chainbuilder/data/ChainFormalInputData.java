/*
 * org.openmicroscopy.shoola.agents.zoombrowser.data.ChainFormalInputData
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

package org.openmicroscopy.shoola.agents.chainbuilder.data;

//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.piccolo.FormalInput;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.FormalInputData;
import org.openmicroscopy.shoola.env.data.model.SemanticTypeData;
import org.openmicroscopy.shoola.util.data.MatchMapper;

/** 
 * An extension of 
 * {@link org.openmicroscopy.shoola.env.data.model.FormalInputData}, 
 * adding some state to track {@link FormalInput} visual representations
 * of each module
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ChainFormalInputData  extends FormalInputData implements Comparable
{
	/** 
	 * A hash for associating instances of this class with  
	 * {@link FormalParameter} instances 
	 * 
	 */
	private static MatchMapper inputMatches = new MatchMapper();
	
	/** the longest string associated with an  input */
	private static ChainFormalInputData longestInput;
	 
	public static List getInputsForType(SemanticTypeData std) {
		if (std !=null) 
			return inputMatches.getMatches(std.getID());
		else 
			return null;
	}
	
	public static ChainFormalInputData getLongestInput() {
		return longestInput;
	}
	
	
	public ChainFormalInputData() {}
	
	
	/** Required by the DataObject interface. */
	public DataObject makeNew() { return new ChainFormalInputData(); }

		
	public void addFormalInput(FormalInput mod) {
		SemanticTypeData st = getSemanticType();
		if (st !=null)
			inputMatches.addMatch(st.getID(),mod);
	}
	
	public List getFormalInputs() {
		SemanticTypeData st = getSemanticType();
		if (st != null)
			return inputMatches.getMatches(st.getID());
		else 
			return null;
	}
	
	public int compareTo(Object o) {
		if (!(o instanceof ChainFormalInputData))
			return -1;
		ChainFormalInputData d = (ChainFormalInputData) o;
		return getID()-d.getID();
	}
	
	public void setName(String name ) {
		super.setName(name);
		if (longestInput == null || name.length() > longestInput.getName().length())
			longestInput = this;
	}
}
