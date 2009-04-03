/*
 * org.openmicroscopy.shoola.agents.executions.ExecutionsWindow
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

package org.openmicroscopy.shoola.agents.executions.ui;

//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.AnalysisChainEvent;
import org.openmicroscopy.shoola.agents.events.DatasetEvent;
import org.openmicroscopy.shoola.agents.events.MouseOverDataset;
import org.openmicroscopy.shoola.agents.events.MouseOverAnalysisChain;
import org.openmicroscopy.shoola.agents.events.SelectAnalysisChain;
import org.openmicroscopy.shoola.agents.events.SelectDataset;
import org.openmicroscopy.shoola.agents.executions.Executions;
import org.openmicroscopy.shoola.agents.executions.data.ExecutionsDataManager;
import org.openmicroscopy.shoola.agents.executions.data.ExecutionsLoader;
import org.openmicroscopy.shoola.agents.executions.data.ExecutionsData;
import org.openmicroscopy.shoola.agents.executions.ui.model.ExecutionsModel;
import org.openmicroscopy.shoola.agents.executions.ui.model.GridModel;
import org.openmicroscopy.shoola.env.config.IconFactory;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.AnalysisChainData;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.env.ui.TopWindowManager;
import org.openmicroscopy.shoola.util.data.ContentGroup;
import org.openmicroscopy.shoola.util.data.ContentGroupSubscriber;
import org.openmicroscopy.shoola.util.ui.Constants;



/** 
 * A top level window containing chain executions that can be clicked on.
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </smalbl>
 * @since OME2.2
 */
public class ExecutionsWindow extends TopWindow implements AgentEventListener,
	ActionListener, ContentGroupSubscriber {
	
	private static final int SLIDER_FUDGE=3;
	private static final String RUN_ICON="nuvola-run16.png";
	private static final int NOT_LOADED=0;
	private static final int LOADING=1;
	private static final int LOADED=2;
	
	/* a pointer to the registry */
	private Registry registry;
	
	/* the canvas */
	private ExecutionsCanvas execCanvas;
	
	private LongRangeSlider slider;
	
	private JButton reset;
	
	
	private ExecutionsModel execsModel;
	
	private JComboBox combo;
	
	private ExecutionsDataManager dataManager;
	
	/* state of data loading process */
	private int dataState = NOT_LOADED;
	
	private TopWindowManager topWindowManager;
	
	/**
	 * Creates a new instance.
	 */
	public ExecutionsWindow(Registry registry,ExecutionsDataManager manager)
	{
		//We have to specify the title of the window to the superclass
		//constructor and pass a reference to the TaskBar, which we get
		//from the Registry.
		super("Execution Manager", registry.getTaskBar());
		this.dataManager = manager;
		this.registry = registry;
		configureDisplayButtons();
		registry.getEventBus().register(this, 
				new Class[] {DatasetEvent.class,
							AnalysisChainEvent.class,
							MouseOverDataset.class,
							MouseOverAnalysisChain.class,
							SelectDataset.class,
							SelectAnalysisChain.class});
	}
	
	/**
	 * Specifies names, icons, and tooltips for the quick-launch button and the
	 * window menu entry in the task bar.
	 */
	private void configureDisplayButtons()
	{
		IconFactory icons = (IconFactory)  
			registry.lookup("/resources/icons/MyFactory");
		Icon execsIcon = icons.getIcon(RUN_ICON);
		configureQuickLaunchBtn(execsIcon, "Execution Viewer");
		configureWinMenuEntry("Execution Viewer", icons.getIcon(RUN_ICON));
	}
	
	/** Builds and lays out this window. */
	public void buildGUI()
	{
		System.err.println("trying to build executions gui");
		Container content = getContentPane();
		content.setLayout(new BorderLayout());
		
		content.setBackground(Constants.CANVAS_BACKGROUND_COLOR);
		// execution canvas
		execCanvas = new ExecutionsCanvas(execsModel,registry);
		// slider
		slider  = execsModel.getSlider();
		slider.setEnabled(true);
		slider.setBackground(Constants.CANVAS_BACKGROUND_COLOR);
		
		execCanvas.setSlider(slider);
		
		// panel for the slider
		JPanel sliderPanel = new JPanel();
		sliderPanel.setBackground(Constants.CANVAS_BACKGROUND_COLOR);
		sliderPanel.setLayout(new BoxLayout(sliderPanel,BoxLayout.X_AXIS));
		sliderPanel.add(Box.createRigidArea(new Dimension(GridModel.LEFT_GAP,0)));
		sliderPanel.add(slider);
		// slider appears to stick out SLIDER_FUDGE pixels to the right of 
		// where you'd think...
		sliderPanel.add(Box.createRigidArea(
				new Dimension(GridModel.RIGHT_GAP-SLIDER_FUDGE,0)));
		
		// reset button
		IconFactory icons = (IconFactory)  
		registry.lookup("/resources/icons/MyFactory");
		Icon resetIcon = icons.getIcon("reset.png");
	
		reset = new JButton(resetIcon);
		reset.addActionListener(this);
		
		// panel with button and slider
		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls,BoxLayout.X_AXIS));
		controls.setBackground(Constants.CANVAS_BACKGROUND_COLOR);
		controls.add(reset);
		
		combo = buildComboBox();
		controls.add(combo);
		
		// add stuff to content pane
		content.add(controls,BorderLayout.NORTH);
		content.add(execCanvas,BorderLayout.CENTER);
		content.add(sliderPanel,BorderLayout.SOUTH);
		
		// listen to events
		pack();
		show();
		enableButtons(true);
	}
	
	private JComboBox buildComboBox() {
		JComboBox combo = new JComboBox(ExecutionsModel.modes);
		combo.setEditable(false);
		combo.setSelectedIndex(0);
		combo.addActionListener(this);
		
		return combo;
		
	}
	
	public void eventFired(AgentEvent e) {
		/*if (e instanceof LoadChainExecutionsEvent) {
			LoadChainExecutionsEvent event = (LoadChainExecutionsEvent) e;
			execsModel = new ExecutionsModel(event);
			if (execsModel.size() >0)
				buildGUI();
		}
		else */ 
		if (e instanceof AnalysisChainEvent) {
			AnalysisChainEvent event = (AnalysisChainEvent) e;
			AnalysisChainData chain = event.getAnalysisChain();
			if (execCanvas != null) 
				execCanvas.selectChain(chain);
		}
		else if (e instanceof DatasetEvent) {
			DatasetEvent event = (DatasetEvent) e;
			DatasetData dataset = event.getDataset();
			if (execCanvas != null) 
				execCanvas.selectDataset(dataset);
		}
		
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == reset )  {
			if (execsModel != null)
					execsModel.resetRangeProperties();
		}
		else if (e.getSource() == combo) {
			String choice = (String) ((JComboBox) e.getSource()).getSelectedItem();
			execsModel.setRenderingOrder(choice);
			
			execCanvas.repaint();
		}
	}
	
	long start =0;
	
	public void preHandleDisplay(TopWindowManager manager) {
		if (dataState == NOT_LOADED) {
			topWindowManager = manager;
			ContentGroup group  = new ContentGroup(this);
			if (Executions.DEBUG_TIMING) 
				start=System.currentTimeMillis();
			new ExecutionsLoader(dataManager,group);
			group.setAllLoadersAdded();
			dataState = LOADING;
		}
		else if (dataState == LOADED) {
			topWindowManager.continueHandleDisplay();
		}
	}
	
	public void contentComplete() {
		if (dataManager.getChainExecutions() != null) {
			if (Executions.DEBUG_TIMING) {
				long end = System.currentTimeMillis()-start;
				System.err.println("Time to load executions: "+end);
				start=System.currentTimeMillis();
			}
			ExecutionsData execs = dataManager.getChainExecutions();
			execsModel = new ExecutionsModel(execs);
			if (execsModel.size() >0)
				buildGUI();
			if (Executions.DEBUG_TIMING) {
				long end = System.currentTimeMillis()-start;
				System.err.println("Time to build gui: "+end);
				start=System.currentTimeMillis();
			}
		
			dataState =LOADED;
		}
	}
	
}
