/*
 * org.openmicroscopy.shoola.env.data.map.ModuleMapper
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.dto.Module;
import org.openmicroscopy.ds.dto.ModuleCategory;
import org.openmicroscopy.ds.dto.FormalInput;
import org.openmicroscopy.ds.dto.FormalOutput;
import org.openmicroscopy.ds.dto.SemanticType;
import org.openmicroscopy.shoola.env.data.model.ModuleCategoryData;
import org.openmicroscopy.shoola.env.data.model.ModuleData;
import org.openmicroscopy.shoola.env.data.model.FormalInputData;
import org.openmicroscopy.shoola.env.data.model.FormalOutputData;
import org.openmicroscopy.shoola.env.data.model.SemanticTypeData;

/** 
 * Mapper for module catgegories
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
public class ModuleMapper
{
		
	/** 
	 * Create the criteria by which the object graph is pulled out.
	 * Criteria built for retrieving modules
	 * 
	 * @param userID	user ID.
	 */
	public static Criteria buildModulesCriteria()
	{
		Criteria criteria = new Criteria();
	
		//Specify which fields we want for the project.
		criteria.addWantedField("id");
		criteria.addWantedField("name");
		criteria.addWantedField("description");
		criteria.addWantedField("category");
		criteria.addWantedField("inputs");
		criteria.addWantedField("outputs");
	
		//Specify which fields we want for the datasets.
		criteria.addWantedField("category", "id");
		criteria.addWantedField("category", "name");
		// what do we want about inputs and outputs 
		criteria.addWantedField("inputs","id");
		criteria.addWantedField("inputs","name");
		criteria.addWantedField("inputs","semantic_type");
		criteria.addWantedField("inputs.semantic_type","id");
		criteria.addWantedField("inputs.semantic_type","name");
		criteria.addWantedField("outputs","id");
		criteria.addWantedField("outputs","name");
		criteria.addWantedField("outputs","semantic_type");
		criteria.addWantedField("outputs.semantic_type","id");
		criteria.addWantedField("outputs.semantic_type","name");
		
		criteria.addOrderBy("name");
		return criteria;
	}


	
	/**
	 * Create list of project summary objects.
	 * 
	 * @param projects	OMEDS.
	 * @param pProto	
	 * @param dProto
	 * @return 
	 */
	public static List fillModules(List modules,ModuleData mProto,
		ModuleCategoryData mcProto,FormalInputData finProto,
		FormalOutputData foutProto,SemanticTypeData stProto)
	{
		Map	stMap = new HashMap();
		List modList= new ArrayList();  //The returned summary list.
		Iterator i = modules.iterator();
		ModuleData mod;
		Module m;
		
		
		ModuleCategory mc;
		ModuleCategoryData mcd;
		Iterator j;
		List params;
		FormalInput fin;
		FormalOutput fout;
		FormalInputData finData;
		FormalOutputData foutData;
		SemanticType st;
		SemanticTypeData std;

		
		//For each m in modules....
		while (i.hasNext()) {
			m = (Module) i.next();
			
			//Make a new DataObject and fill it up.
			mod = (ModuleData) mProto.makeNew();
			mod.setModuleDTO(m);
			mod.setID(m.getID());
			mod.setName(m.getName());
			
			
			// category
			mc = m.getCategory();
			if (mc != null) {
				mcd = (ModuleCategoryData) mcProto.makeNew();
				mcd.setID(mc.getID());
				mcd.setName(mc.getName());
				mod.setModuleCategory(mcd);
			}
				
			// inputs
			
			j = m.getFormalInputs().iterator();
			params = new ArrayList();
			while (j.hasNext()) {
				fin  = (FormalInput) j.next();
				int id = fin.getID();
				
				//Make a new DataObject and fill it up.
				finData = (FormalInputData) finProto.makeNew();
				finData.setFormalInputDTO(fin);
				finData.setID(id);
				finData.setName(fin.getName());
		
				// semantic type for the parameter.
				st = fin.getSemanticType();
				if (st != null) {
					std = getSemanticTypeData(st,stProto,stMap);
					finData.setSemanticType(std);
				} 
				//Add the input to this module's list.
				params.add(finData);	
			}
			
			//Link the inputs to this module.
			mod.setFormalInputs(params);
			
			// outputs
			
			j = m.getFormalOutputs().iterator();
			params = new ArrayList();
			while (j.hasNext()) {
				fout  = (FormalOutput) j.next();
				int id = fout.getID();
				
				foutData = (FormalOutputData) foutProto.makeNew();
				foutData.setFormalOutputDTO(fout);
				foutData.setID(id);
				foutData.setName(fout.getName());
				
				//	semantic type for the parameter.
				 st = fout.getSemanticType();
				 if (st != null) {
				 	std = getSemanticTypeData(st,stProto,stMap);
				 	foutData.setSemanticType(std);
				 } 
				//Add the input to this module's list.
				params.add(foutData);	
			}

			//Link the inputs to this module.
			mod.setFormalOutputs(params);
			
			//Add the module to the list of returned modules;.
			modList.add(mod);
		}
		
		return modList;
	}
	
	private static SemanticTypeData getSemanticTypeData(SemanticType st,
				SemanticTypeData stProto,Map stMap) {
		SemanticTypeData std;	
		
		int id = st.getID();
		Integer ID = new Integer(id);
		std = (SemanticTypeData) stMap.get(ID);
		if (std == null) {
			std = (SemanticTypeData) stProto.makeNew();
			std.setID(id);
			std.setName(st.getName());
			stMap.put(ID,std);
		} 
		return std;
	}
	
}
