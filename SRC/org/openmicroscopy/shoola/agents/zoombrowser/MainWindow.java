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

package org.openmicroscopy.shoola.agents.zoombrowser;

//Java imports
import java.awt.Component;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.zoombrowser.data.ContentGroup;
import org.openmicroscopy.shoola.agents.zoombrowser.data.DatasetLoader;
import org.openmicroscopy.shoola.agents.zoombrowser.data.ProjectLoader;
import org.openmicroscopy.shoola.agents.zoombrowser.
	piccolo.ProjectSelectionCanvas;
import org.openmicroscopy.shoola.agents.zoombrowser.
	piccolo.DatasetBrowserCanvas;
import org.openmicroscopy.shoola.env.config.IconFactory;
import org.openmicroscopy.shoola.env.ui.TopWindow;

/** 
 * An example of a top-level window that inherits from {@link TopWindow}.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </smalbl>
 * @since OME2.2
 */
public class MainWindow 
	extends TopWindow
{
	
	/** horizontal extent */
	private static final int SIDE=400;
	
	/** the data manager for this instance */
	private DataManager dataManager;
	
	/** Cached reference to access the icons. */
	private IconFactory icons;
			
	/** The split pane in the window. */
	private JSplitPane split;
	
	/**
	 * Specifies names, icons, and tooltips for the quick-launch button and the
	 * window menu entry in the task bar.
	 */
	private void configureDisplayButtons()
	{
		configureQuickLaunchBtn(icons.getIcon("zoom.png"), 
												"Display the main window.");
		configureWinMenuEntry("Zoomable Browser", icons.getIcon("zoom.png"));
	}
	
	/** Builds and lays out this window. */
	private void buildGUI()
	{
		JPanel framePanel = new JPanel();
		framePanel.setLayout(new BoxLayout(framePanel,BoxLayout.Y_AXIS));
		
		
			
		// create datasets, etc here.
		DatasetBrowserCanvas browser = new DatasetBrowserCanvas();		
		ProjectSelectionCanvas v = new ProjectSelectionCanvas(this);
		ContentGroup group = new ContentGroup();
		
		final DatasetLoader dl = new DatasetLoader(dataManager,browser,group);
		final ProjectLoader pl = new ProjectLoader(dataManager,v,group);
		group.setAllLoadersAdded();
		
		split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,v,browser);
		split.setTopComponent(v);
		split.setOneTouchExpandable(true);
		split.setResizeWeight(0.33);
		split.setAlignmentX(Component.CENTER_ALIGNMENT);
		framePanel.add(split);
		
		// else, do something reasonable when no projects
		getContentPane().add(framePanel);
		pack();
		show();		 
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
		buildGUI();
	}
	
	public void setDividerLocation(int h) {
		if (split != null)
			split.setDividerLocation(h);
	}

}
