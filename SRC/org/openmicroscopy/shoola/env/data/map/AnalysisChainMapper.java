/*
 * org.openmicroscopy.shoola.env.data.map.AnalysisChainMapper
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.dto.AnalysisChain;
import org.openmicroscopy.ds.dto.AnalysisLink;
import org.openmicroscopy.ds.dto.AnalysisNode;
import org.openmicroscopy.ds.dto.Module;
import org.openmicroscopy.ds.dto.FormalInput;
import org.openmicroscopy.ds.dto.FormalOutput;
import org.openmicroscopy.ds.dto.SemanticType;
import org.openmicroscopy.ds.st.Experimenter;
import org.openmicroscopy.shoola.env.data.model.AnalysisChainData;
import org.openmicroscopy.shoola.env.data.model.AnalysisLinkData;
import org.openmicroscopy.shoola.env.data.model.AnalysisNodeData;
import org.openmicroscopy.shoola.env.data.model.FormalInputData;
import org.openmicroscopy.shoola.env.data.model.FormalOutputData;
import org.openmicroscopy.shoola.env.data.model.ModuleData;
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
public class AnalysisChainMapper
{
		
	public static HashMap stMap;
	/** 
	 * Create the criteria by which the object graph is pulled out.
	 * Criteria built for retrieving modules
	 * 
	 * @param userID	user ID.
	 */
	public static Criteria buildChainCriteria()
	{
		Criteria criteria = new Criteria();
	
		//Specify which fields we want for the chain.
		criteria.addWantedField("id");
		criteria.addWantedField("name");
		criteria.addWantedField("description");
		criteria.addWantedField("nodes");
		criteria.addWantedField("links");
		criteria.addWantedField("locked");
		criteria.addWantedField("owner");
		criteria.addWantedField("owner","FirstName");
		criteria.addWantedField("owner","LastName");
		
		
		// stuff for nodes
		criteria.addWantedField("nodes","module");
		criteria.addWantedField("nodes","id");
		criteria.addWantedField("nodes.module","id");
		criteria.addWantedField("nodes.module","name");
		
		// links
		criteria.addWantedField("links","from_node");
		criteria.addWantedField("links.from_node","id");
		
		criteria.addWantedField("links","to_node");
		criteria.addWantedField("links.to_node","id");
		criteria.addWantedField("links","from_output");
		criteria.addWantedField("links","to_input");
		
		criteria.addWantedField("links.from_output","id");
		criteria.addWantedField("links.to_output","id");
		
		criteria.addWantedField("links.from_output","semantic_type");
		criteria.addWantedField("links.to_input","semantic_type");
		
		criteria.addWantedField("links.from_output.semantic_type","id");
		criteria.addWantedField("links.to_input.semantic_type","id");
		criteria.addWantedField("links.from_output.semantic_type","name");
		criteria.addWantedField("links.to_input.semantic_type","name");
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
	public static List fillChains(List chains,AnalysisChainData acProto,
		AnalysisLinkData alProto,AnalysisNodeData anProto,ModuleData mdProto,
		FormalInputData finProto,FormalOutputData foutProto,SemanticTypeData
		stProto)
	{
		List chainList= new ArrayList();  //The returned summary list.
		Iterator i = chains.iterator();
		
		AnalysisChain c;
		AnalysisChainData chain;
		Iterator j;
		Experimenter exp;

		stMap = new HashMap();
		//For each m in modules....
		while (i.hasNext()) {
			c = (AnalysisChain) i.next();
			
			//Make a new DataObject and fill it up.
			chain = (AnalysisChainData) acProto.makeNew();
			chain.setID(c.getID());
			chain.setName(c.getName());
			chain.setDescription(c.getDescription());
			chain.setIsLocked(c.isLocked().booleanValue());
			
			exp = c.getOwner();
			chain.setOwner(exp.getFirstName()+" "+exp.getLastName());
			getNodes(chain,c,anProto,mdProto);
			getLinks(chain,c,anProto,alProto,finProto,foutProto,stProto);
			
			chainList.add(chain);
		}
		
		stMap = null; // so we can collect it.
		return chainList;
	}
	
	private static void getNodes(AnalysisChainData chain,
			AnalysisChain c,AnalysisNodeData anProto,ModuleData mdProto) {
		ArrayList nodesList = new ArrayList();
		List nodes = c.getNodes();
		Iterator i = nodes.iterator();
		AnalysisNode an;
		AnalysisNodeData analysisNode;
		Module mod;
		ModuleData moduleData;
			
		while (i.hasNext()) {
			an = (AnalysisNode) i.next();
			analysisNode = (AnalysisNodeData) anProto.makeNew();
			analysisNode.setID(an.getID());
			mod = an.getModule();
			moduleData = (ModuleData) mdProto.makeNew();
			moduleData.setModuleDTO(mod);
			moduleData.setID(mod.getID());
			moduleData.setName(mod.getName());
			analysisNode.setModule(moduleData);

			nodesList.add(analysisNode);
		}
		chain.setNodes(nodesList);
	}
	
	private static void getLinks(AnalysisChainData chain,AnalysisChain c,
		AnalysisNodeData anProto,AnalysisLinkData alProto,FormalInputData 
		finProto,FormalOutputData foutProto,SemanticTypeData stProto) {
		
		ArrayList linksList = new ArrayList();
		List links = c.getLinks();
		Iterator i = links.iterator();
		int id;
		AnalysisLink al;
		AnalysisLinkData analysisLink;
		AnalysisNode an;
		AnalysisNodeData analysisNode;
		FormalOutput fout;
		FormalInput fin;
		FormalOutputData foutData;
		FormalInputData  finData;
		SemanticType st;
		SemanticTypeData stData;
		
		
		while(i.hasNext()) {
			al = (AnalysisLink) i.next();
			analysisLink = (AnalysisLinkData) alProto.makeNew();
			analysisLink.setID(al.getID());
			
			// from 
			an = al.getFromNode();
			id = an.getID();
			analysisNode = getNode(chain,id);
			analysisLink.setFromNode(analysisNode);
			fout = al.getFromOutput();
			foutData = (FormalOutputData) foutProto.makeNew();
			foutData.setID(fout.getID());
			st = fout.getSemanticType();
			if (st != null) {
				stData = getSemanticTypeData(st,stProto);
				foutData.setSemanticType(stData);
			}
			analysisLink.setFromOutput(foutData);
			
			analysisNode.addOutputLink(analysisLink);
			
			
			// to
			an = al.getToNode();
			id = an.getID();
			analysisNode = getNode(chain,id);
			analysisLink.setToNode(analysisNode);
			fin = al.getToInput();
			finData = (FormalInputData) finProto.makeNew();
			finData.setID(fin.getID());
			st = fin.getSemanticType();
			if (st!= null) {
				stData = getSemanticTypeData(st,stProto);
				finData.setSemanticType(stData);
			}
			analysisLink.setToInput(finData);
			analysisNode.addInputLink(analysisLink);
			linksList.add(analysisLink);
						
		}
		
		chain.setLinks(linksList);
	}
	
	
	// since we get nodes before we get links, we simply find pre-found
	// node in the list of nodes.
	private static AnalysisNodeData getNode(AnalysisChainData chain,int id) {
		List nodes = chain.getNodes();
		
		Iterator iter = nodes.iterator();
		while (iter.hasNext()) {
			AnalysisNodeData analysisNode = (AnalysisNodeData) iter.next();
			if (analysisNode.getID() == id)
				return analysisNode;
		}
		return null;
	}
	
	private static SemanticTypeData getSemanticTypeData(SemanticType st,
			SemanticTypeData stProto) {
		
		int id = st.getID();
		Integer ID = new Integer(id);
		SemanticTypeData std = (SemanticTypeData) stMap.get(ID);
		if (std == null) {
			std = (SemanticTypeData) stProto.makeNew();
			std.setID(id);
			std.setName(st.getName());
			stMap.put(ID,std);
		}
		return std;
	}
}
