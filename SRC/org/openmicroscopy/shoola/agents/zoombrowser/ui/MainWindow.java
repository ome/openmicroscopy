/*
 * org.openmicroscopy.shoola.agents.zoombrowser.MainWindow
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

package org.openmicroscopy.shoola.agents.zoombrowser.ui;

//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.events.AnalysisChainEvent;
import org.openmicroscopy.shoola.agents.events.LoadChainExecutionsEvent;
import org.openmicroscopy.shoola.agents.events.MouseOverAnalysisChain;
import org.openmicroscopy.shoola.agents.events.MouseOverChainExecutionEvent;
import org.openmicroscopy.shoola.agents.events.MouseOverDataset;
import org.openmicroscopy.shoola.agents.events.SelectAnalysisChain;
import org.openmicroscopy.shoola.agents.events.SelectChainExecutionEvent;
import org.openmicroscopy.shoola.agents.events.SelectDataset;
//import org.openmicroscopy.shoola.agents.executions.data.ExecutionsData;
import org.openmicroscopy.shoola.agents.zoombrowser.DataManager;
import org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserDatasetData;
import org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserProjectSummary;
import org.openmicroscopy.shoola.agents.zoombrowser.data.DatasetLoader;
import org.openmicroscopy.shoola.agents.zoombrowser.data.ProjectLoader;
import org.openmicroscopy.shoola.agents.zoombrowser.piccolo.DatasetBrowserCanvas;
import org.openmicroscopy.shoola.agents.zoombrowser.
	piccolo.ProjectSelectionCanvas;
import org.openmicroscopy.shoola.env.config.IconFactory;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.AnalysisChainData;
import org.openmicroscopy.shoola.env.data.model.ChainExecutionData;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.env.ui.TopWindowManager;
import org.openmicroscopy.shoola.util.data.ContentGroup;
import org.openmicroscopy.shoola.util.data.ContentGroupSubscriber;
import org.openmicroscopy.shoola.util.ui.Constants;


/** 
 * A top-level window for a zoomable project browser 
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </smalbl>
 * @since OME2.2
 */
public class MainWindow extends TopWindow implements ComponentListener, 
	AgentEventListener,  ContentGroupSubscriber
{
	
	/** horizontal extent */
	private static final int SIDE=400;
	
	/** the data manager for this instance */
	private DataManager dataManager;
	
	/** Cached reference to access the icons. */
	private IconFactory icons;
		
	/** contents container */
	private Container contents;
	
	/** canvases contained in this window */
	private DatasetBrowserCanvas datasetBrowser;
	private ProjectSelectionCanvas projectBrowser;
	
	
	//private ExecutionsData chainExecutions;
	
	// the top window manager for this window
	private TopWindowManager topWindowManager;
	
	
	/* has the data been loaded ?*/
	private int dataState=0;
	
	private final static int NOT_LOADED=0;
	private final static int LOADING=1;
	private final static int LOADED=2;
	/**
	 * Specifies names, icons, and tooltips for the quick-launch button and the
	 * window menu entry in the task bar.
	 */
	private void configureDisplayButtons()
	{
		
		configureQuickLaunchBtn(icons.getIcon("zoom.png"), 
						"Zoomable project/dataset browser.");
		configureWinMenuEntry("Zoomable Browser", icons.getIcon("zoom.png"));
	}
	
	/** Builds and lays out this window. */
	public void buildGUI()
	{
		contents = getContentPane();
	
		contents.setLayout(new BorderLayout());
		
		
			
		// create datasets, etc here.
		
		Border empty = BorderFactory.createEmptyBorder(5,5,5,5);
		Border raised = BorderFactory.createRaisedBevelBorder();
		Border lowered = BorderFactory.createLoweredBevelBorder();
		Border compound = BorderFactory.createCompoundBorder(raised,lowered);
		Border fullBorder = BorderFactory.createCompoundBorder(empty,compound);
		
		
		datasetBrowser = new DatasetBrowserCanvas(this,dataManager.getRegistry());
		projectBrowser = new ProjectSelectionCanvas(this);
		datasetBrowser.setContents(dataManager.getDatasets());
		projectBrowser.setContents(dataManager.getProjects());
		
		projectBrowser.layoutContents();
		projectBrowser.completeInitialization();
	
  
		JPanel projectsPanel = new JPanel();
		projectsPanel.setBorder(
				BorderFactory.createTitledBorder(fullBorder,"Projects"));
		projectsPanel.setLayout(new BorderLayout());
		projectsPanel.setBackground(Constants.CANVAS_BACKGROUND_COLOR);
		projectsPanel.add(projectBrowser,BorderLayout.CENTER);
		
		JPanel datasetPanel = new JPanel();
		datasetPanel.setBorder(
				BorderFactory.createTitledBorder(fullBorder,"Datasets"));
		datasetPanel.setLayout(new BorderLayout());
		datasetPanel.setBackground(Constants.CANVAS_BACKGROUND_COLOR);
		datasetPanel.add(datasetBrowser,BorderLayout.CENTER);
		contents.add(projectsPanel,BorderLayout.NORTH);
		contents.add(datasetPanel,BorderLayout.CENTER);
		pack();
		datasetBrowser.layoutContents();
		datasetBrowser.completeInitialization();
	
	
		addComponentListener(this);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param config	A reference to this agent's registry.
	 */
	MainWindow(DataManager dataManager)
	{
		//We have to specify the title of the window to the superclass
		//constructor and pass a reference to the TaskBar, which we get
		//from the Registry.
		super("Zoomable Browser", dataManager.getTaskBar());
		
		this.dataManager = dataManager;
		icons = dataManager.getIconFactory();
		
		
		configureDisplayButtons();
		Registry registry = dataManager.getRegistry();
		registry.getEventBus().register(this,
			new Class[] { 
				SelectAnalysisChain.class,
				MouseOverAnalysisChain.class,
				LoadChainExecutionsEvent.class,
				MouseOverChainExecutionEvent.class,
				SelectChainExecutionEvent.class});
	}
		
	

	public void setRolloverProject(BrowserProjectSummary proj) {
	 	datasetBrowser.setRolloverProject(proj);
	}
	
	public void setRolloverDataset(BrowserDatasetData dataset) {
		projectBrowser.setRolloverDataset(dataset);
		MouseOverDataset event = new MouseOverDataset(dataset);
		dataManager.getRegistry().getEventBus().post(event);
	}
	
	public void setSelectedProject(BrowserProjectSummary proj) {
		datasetBrowser.setSelectedProject(proj);
	}
	
	public void setSelectedDataset(BrowserDatasetData dataset) {
		projectBrowser.setSelectedDataset(dataset);
		SelectDataset event = new SelectDataset(dataset);
		dataManager.getRegistry().getEventBus().post(event);
	}
	
	public void eventFired(AgentEvent e) {
		if (e instanceof AnalysisChainEvent &&
					datasetBrowser != null) {
			AnalysisChainEvent event = (AnalysisChainEvent) e;
			AnalysisChainData chain = event.getAnalysisChain();
			if (event instanceof SelectAnalysisChain)
				datasetBrowser.selectAnalysisChain(chain);
			else if (event instanceof MouseOverAnalysisChain)
				datasetBrowser.mouseOverAnalysisChain(chain);
		}
		else if (e instanceof MouseOverChainExecutionEvent &&
				datasetBrowser!=null) {
			MouseOverChainExecutionEvent event = (MouseOverChainExecutionEvent) e;
			ChainExecutionData exec = event.getChainExecution();
			datasetBrowser.mouseOverChainExecution(exec);
			
		}
		else if (e instanceof SelectChainExecutionEvent && datasetBrowser != null) {
			SelectChainExecutionEvent  event = (SelectChainExecutionEvent) e;
			ChainExecutionData exec = event.getChainExecution();
			datasetBrowser.selectChainExecution(exec);
		}
		else if (e instanceof LoadChainExecutionsEvent) {
			LoadChainExecutionsEvent event = (LoadChainExecutionsEvent) e;
			//chainExecutions = event.getExecutionsData();
		}
        /*
		else if (e instanceof LoadDataset && datasetBrowser!= null) {
			LoadDataset event = (LoadDataset) e;
			// get the dataset
			BrowserDatasetData dataset = 
			//	dataManager.getDataset(event.getDatasetID());
			// select it.
			datasetBrowser.respondToDatasetLoad(dataset);
		}
        */
	}
	
    /*
	public ExecutionsData getChainExecutions() {
		return chainExecutions;
	}
    */
	
	public void componentHidden(ComponentEvent e) {
	}
	
	public void componentMoved(ComponentEvent e) {
	}
	
	public void componentResized(ComponentEvent e) {
		
	}
	
	public void componentShown(ComponentEvent e) {
		if (datasetBrowser != null)
			datasetBrowser.scaleToSize();
	}
	
	public void preHandleDisplay(TopWindowManager manager) {
		if (dataState == NOT_LOADED) {
			topWindowManager = manager;
			ContentGroup group = new ContentGroup(this);
			final DatasetLoader dl = new DatasetLoader(dataManager,group);
			final ProjectLoader pl = new ProjectLoader(dataManager,group);
			group.setAllLoadersAdded();
			dataState = LOADING;
		}
		else if (dataState == LOADED) { // already done just go ahead and show the window
			//topWindowManager.continueHandleDisplay();
		}
		// otherwise, we're loading. disregard.
	}
	
	public void contentComplete() {
		if (dataManager.getDatasets() != null || dataManager.getProjects() != null) {
			
			buildGUI();
			//topWindowManager.continueHandleDisplay();
			dataState = LOADED;
		}
	}
	
}
