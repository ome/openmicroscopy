/*
 * org.openmicroscopy.shoola.agents.history.HistoryAgent
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

package org.openmicroscopy.shoola.agents.history;

//Java imports
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.SelectChainExecutionEvent;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ActualInputData;
import org.openmicroscopy.shoola.env.data.model.FormalInputData;
import org.openmicroscopy.shoola.env.data.model.FormalOutputData;
import org.openmicroscopy.shoola.env.data.model.SemanticTypeData;
import org.openmicroscopy.shoola.env.data.model.ChainExecutionData;
import org.openmicroscopy.shoola.env.data.model.ModuleData;
import org.openmicroscopy.shoola.env.data.model.ModuleExecutionData;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;

/** 
 * The data history agent
 *
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * after code by 
 *  @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
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
public class HistoryAgent  
	implements Agent, AgentEventListener 
{
	
	
	/* debug level. 0 is no warnings, 5 is most detail.*/
	public static final int DEBUG=0;
	/** 
	 * Holds the agent's configuration and lets the agent access all services.
	 */
	private Registry	registry;
	
	
	/** The data manager */
	private HistoryDataManager dataManager;
	
	public static void debug(String s,int level) {
		if (DEBUG >= level)
			System.err.println(s);
	}
	
	/**
	 * Does nothing.
	 * However, all agents must have a no-params public constructor.
	 */
	public HistoryAgent() {}
	
	public void setContext(Registry ctx)
	{			
		registry = ctx;  //The container built our registry, store a reference.
		EventBus bus = registry.getEventBus();
		bus.register(this,SelectChainExecutionEvent.class);
	}
	
	public void activate() 
	{
		dataManager = new HistoryDataManager(registry);
	}
	
	public boolean canTerminate() 
	{
		return true;  //This agent can be shut down at any time.
	}
	
	public void terminate() 
	{
	}
	
	public void eventFired(AgentEvent e) {
		if (e instanceof SelectChainExecutionEvent) {
			handleChainExecution((SelectChainExecutionEvent)e);
		}
	}
	
	private void handleChainExecution(SelectChainExecutionEvent e ) {
		ChainExecutionData d = e.getChainExecution();
		System.err.println("chain execution.."+d.getID());
		Collection modules = dataManager.getModules();
		List mexes = dataManager.getChainExecutionHistory(d.getID());
		System.err.println(mexes.size()+" mexes on preceding list");
		
		Iterator iter = mexes.iterator();
		while (iter.hasNext()) {
			ModuleExecutionData mex = (ModuleExecutionData) iter.next();
			dumpMex(mex);
		}
	}
	
	private void dumpMex(ModuleExecutionData mex) {
		System.err.println("\n\nMEX: "+mex.getID()+" time "+mex.getTimestamp());
		ModuleData mod = mex.getModule();
		System.err.println("Module ..."+mod.getID()+", "+mod.getName());
		List predecessors = mex.getPredecessors();

		List inputs = mex.getInputs();
		System.err.println(inputs.size()+" inputs.");
		Iterator iter = inputs.iterator();
		while (iter.hasNext()) {
			ActualInputData input = (ActualInputData) iter.next();
			dumpInput(input);
		}
	}
	
	private void dumpInput(ActualInputData input) {
		System.err.println("Actual input id "+input.getID()+", input mex "
				+input.getInputMex().getID());
		FormalInputData fin = input.getToInput();
		System.err.println("input "+fin.getID()+", "+fin.getName());
		SemanticTypeData std = fin.getSemanticType();
		System.err.println("Semantic type... "+std.getID()+", "+std.getName());
		
		FormalOutputData fout = input.getFromOutput();
		System.err.println("output "+fout.getID()+", "+fout.getName());
		
	}
}
