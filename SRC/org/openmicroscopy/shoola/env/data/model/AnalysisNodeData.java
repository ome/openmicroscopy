/*
 * org.openmicroscopy.shoola.env.data.model.AnalysisNodeData
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
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies

/** 
 * An analysis node object
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
public class AnalysisNodeData implements DataObject
{

	private int id;
	private AnalysisChainData chain;
	private ModuleData module;
	private String iteratorTag;
	private String newFeatureTag;
	private List inputLinks;
	private List outputLinks;
	
	public AnalysisNodeData() {}
	
	public AnalysisNodeData(int id,AnalysisChainData chain,ModuleData module,String
		iteratorTag,String newFeatureTag,List inputLinks,List outputLinks) 
	{	
		this.id = id;
		this.chain = chain;
		this.module = module;
		this.iteratorTag = iteratorTag;
		this.newFeatureTag = newFeatureTag;
		this.inputLinks = inputLinks;
		this.outputLinks = outputLinks;
	}
	
	/** Required by the DataObject interface. */
	public DataObject makeNew() {
		inputLinks = new ArrayList();
		outputLinks = new ArrayList(); 
	return new AnalysisNodeData(); }
	
	public AnalysisChainData getChain() {
		return chain;
	}

	public int getID() {
		return id;
	}

	public List getInputLinks() {
		return inputLinks;
	}

	public String getIteratorTag() {
		return iteratorTag;
	}

	public ModuleData getModule() {
		return module;
	}

	public String getNewFeatureTag() {
		return newFeatureTag;
	}

	public List getOutputLinks() {
		return outputLinks;
	}

	public void setChain(AnalysisChainData data) {
		chain = data;
	}

	public void setID(int i) {
		id = i;
	}

	public void setInputLinks(List list) {
		inputLinks = list;
	}

	public void setIteratorTag(String string) {
		iteratorTag = string;
	}

	public void setModule(ModuleData data) {
		module = data;
	}

	public void setNewFeatureTag(String string) {
		newFeatureTag = string;
	}

	public void setOutputLinks(List list) {
		outputLinks = list;
	}
	
	public void addInputLink(AnalysisLinkData link) {
		if (inputLinks == null)
			inputLinks = new ArrayList();
		inputLinks.add(link);
	}
	
	public void addOutputLink(AnalysisLinkData link) {
		if (outputLinks == null)
			outputLinks = new ArrayList();		outputLinks.add(link);
	}
}
