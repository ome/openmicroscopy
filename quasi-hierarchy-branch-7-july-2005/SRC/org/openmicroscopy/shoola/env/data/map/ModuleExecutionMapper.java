/*
 * org.openmicroscopy.shoola.env.data.map.ModuleExecutionMapper
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.dto.Module;
import org.openmicroscopy.ds.dto.ModuleExecution;
import org.openmicroscopy.ds.dto.ActualInput;
import org.openmicroscopy.ds.dto.FormalInput;
import org.openmicroscopy.ds.dto.FormalOutput;
import org.openmicroscopy.ds.dto.SemanticType;
import org.openmicroscopy.shoola.env.data.model.ActualInputData;
import org.openmicroscopy.shoola.env.data.model.FormalInputData;
import org.openmicroscopy.shoola.env.data.model.FormalOutputData;
import org.openmicroscopy.shoola.env.data.model.ModuleData;
import org.openmicroscopy.shoola.env.data.model.ModuleExecutionData;
import org.openmicroscopy.shoola.env.data.model.SemanticTypeData;


/** 
 * A mapper for filling in module execution details. Similar to what's in chain 
 * execution manager, but specifically used for histories. 
 *
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ModuleExecutionMapper
{
	public static List fillHistoryMexes(List mexes,ModuleExecutionData mexData,
			ModuleData modData,ActualInputData inpData,FormalInputData finData,
			FormalOutputData foutData,SemanticTypeData stData) {
		ArrayList mexList = new ArrayList();
		Iterator iter  = mexes.iterator();
		ModuleExecution mex;
		ModuleExecutionData newMex;
		
		while (iter.hasNext()) {
			mex = (ModuleExecution) iter.next();
			newMex = fillHistoryMex(mex,mexData,modData,inpData,finData,
					foutData,stData);
			mexList.add(newMex);
		}
		return mexList;
	}
	
	private static ModuleExecutionData fillHistoryMex(ModuleExecution mex,
				ModuleExecutionData mexData,ModuleData modData,
				ActualInputData inpData,FormalInputData finData,
				FormalOutputData foutData,SemanticTypeData stData) {

		ModuleExecutionData data = (ModuleExecutionData) mexData.makeNew();
		
		data.setID(mex.getID());
		data.setTimestamp(mex.getTimestamp());
		
		// module
		Module m = mex.getModule();
		ModuleData mod = (ModuleData) modData.makeNew();
		mod.setID(m.getID());
		mod.setName(m.getName());
		data.setModule(mod);
		
		// inputs
		ArrayList inputData = new ArrayList();
		List inputs = mex.getInputs();
		ActualInput in;
		ActualInputData input;
		
		Iterator iter =inputs.iterator();
		while (iter.hasNext()) {
			in = (ActualInput) iter.next();
			input = getInputData(in,inpData,mexData,finData,foutData,stData);
			inputData.add(input);
		}
		data.setInputs(inputData);
		return data;
	}
	
	private static ActualInputData getInputData(ActualInput in,
			ActualInputData inpData,ModuleExecutionData mexData,
			FormalInputData finData,FormalOutputData foutData,
			SemanticTypeData stData) {
		
		ActualInputData input = (ActualInputData) inpData.makeNew();
		input.setID(in.getID());
		
		// input mex
		ModuleExecution inMex = in.getInputMEX();
		ModuleExecutionData inMexData = (ModuleExecutionData) mexData.makeNew();
		inMexData.setID(inMex.getID());
		input.setInputMex(inMexData);
		
		// mex
		ModuleExecution mex = in.getModuleExecution();
		ModuleExecutionData newMexData = (ModuleExecutionData) mexData.makeNew();
		newMexData.setID(mex.getID());
		input.setMex(newMexData);
		
		// formal input
		FormalInput fi = in.getFormalInput();
		FormalInputData fin = (FormalInputData) finData.makeNew();
		fin.setID(fi.getID());
		fin.setName(fi.getName());
		
		SemanticType st = fi.getSemanticType();
		SemanticTypeData finSemanticType = (SemanticTypeData) stData.makeNew();
		finSemanticType.setID(st.getID());
		finSemanticType.setName(st.getName());
		fin.setSemanticType(finSemanticType);
	
		input.setToInput(fin);
		//formal output. Note we don't need to recreate st - 
		// use same as on formal input
		FormalOutput fo = in.getFormalOutput();
		FormalOutputData fout = (FormalOutputData) foutData.makeNew();
		fout.setID(fo.getID());
		fout.setName(fo.getName());
		
		// if those types don't match, we're in trouble.
		fout.setSemanticType(finSemanticType);
		
		input.setFromOutput(fout);
		
		return input;
		
	}
	
	
	
}
