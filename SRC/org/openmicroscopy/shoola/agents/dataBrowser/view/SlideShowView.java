/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.SlideShowView 
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.Timer;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.Colors;
import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Browser;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import com.sun.corba.se.impl.ior.iiop.JavaSerializationComponent;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class SlideShowView 
	extends JDialog
	implements ActionListener, PropertyChangeListener
{

	/** Bound property indicating to close the slide view. */
	static final String				CLOSE_SLIDE_VIEW_PROPERTY =
											"closeSlideView";
	
    /** The delay between two images for medium speed. */
	private static final int  		DELAY_MEDIUM = 2000;
	
	/** The delay between two images for slow speed. */
	private static final int  		DELAY_SLOW = 3000;
	
	/** The delay between two images for fast speed. */
	private static final int  		DELAY_FAST = 1000;
	
	/** ID indicating to play the show at a slow speed. */
	private static final int 		SLOW_SPEED = 0;
	
	/** ID indicating to play the show at a medium speed. */
	private static final int 		MEDIUM_SPEED = 1;
	
	/** ID indicating to play the show at a fast speed. */
	private static final int 		FAST_SPEED = 2;
	
	/** ID indicating to modify the speed of play. */
	private static final int 		SPEED = 10;
	
	/** ID indicating to pause the show. */
	private static final int 		PAUSE = 11;
	
	/** ID indicating to play the show forward. */
	private static final int 		PLAY_FORWARD = 12;
	
	/** ID indicating to play the show forward. */
	private static final int 		PLAY_BACKWARD = 13;
	
	 /** Indicates the <i>Start</i> state of the timer. */
    public static final int     	START = 0;
    
    /** Indicates the <i>Stop</i> state of the timer. */
    public static final int     	STOP = 1;
    
    /** The speed options. */
	private static final String[]	SPEEDS;
	
	static {
		SPEEDS = new String[3];
		SPEEDS[SLOW_SPEED] = "slow";
		SPEEDS[MEDIUM_SPEED] = "medium";
		SPEEDS[FAST_SPEED] = "fast";
	}
	
	/** The collection of nodes to show. */
	private List<ImageNode> 		nodes;
	
	/** The button to pause the movie. */
	private JButton					pause;

	/** The button to show the images forward. */
	private JToggleButton			forwardPlay;

	/** The button to show the images backward. */
	private JToggleButton			backwardPlay;
	
	/** Label indicating to show images at a slow speed. */
	private JComboBox				speeds;
	
	/** The currently selected node. */
	private int						selectedNodeIndex;
	
	 /** The timer controlling the display. */
	private Timer     				timer;
    
    /** The state of the timer. */
	private int       				state;
    
    /** The delay used by the timer. */
	private int       				delay;
    
	/** Either {@link #PLAY_FORWARD} or {@link #PLAY_BACKWARD}. */
	private int 					playingIndex;

    /** The Horizontal split pane. */
    private JSplitPane				pane;
    
    /** The component hosting the selected node. */
    private JComponent				nodesPane;

    /** The UI component hosting the image's name. */
    private JLabel					titleLabel;
    
    
    /** The component hosting the main image. */
    private SlideShowUI				uiDelegate;
   
    /** The map used to determine the index of the selected node. */
    private Map<ImageNode, Integer> nodesMap;
    
	/** Initializes the components. */
	private void initComponents()
	{
		nodesMap = new HashMap<ImageNode, Integer>();
		IconManager icons = IconManager.getInstance();
		pause = new JButton(icons.getIcon(IconManager.PAUSE));
		pause.setActionCommand(""+PAUSE);
		pause.addActionListener(this);
		forwardPlay = new JToggleButton(icons.getIcon(IconManager.FORWARD));
		forwardPlay.setActionCommand(""+PLAY_FORWARD);
		forwardPlay.addActionListener(this);
		backwardPlay = new JToggleButton(icons.getIcon(IconManager.BACKWARD));
		backwardPlay.setActionCommand(""+PLAY_BACKWARD);
		backwardPlay.addActionListener(this);
		

		speeds = new JComboBox(SPEEDS);
		speeds.setSelectedIndex(MEDIUM_SPEED);
		speeds.addActionListener(this);
		speeds.setActionCommand(""+SPEED);
		delay = DELAY_MEDIUM;
		timer = new Timer(delay, this);
        timer.setInitialDelay(delay/10);
        timer.setCoalesce(true);
        state = STOP;
        playingIndex = -1;
        selectedNodeIndex = 0;
        
        uiDelegate = new SlideShowUI(this);
        pane = new JSplitPane();
        pane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        pane.setOneTouchExpandable(true);
        pane.setContinuousLayout(true);
        pane.setTopComponent(uiDelegate);
    	pane.setResizeWeight(0.9);
    	pane.setBackground(UIUtilities.BACKGROUND);
    	titleLabel = new JLabel();
	}
	
	/** 
	 * Stops the timer and fires a property indicating that the slide
	 * show is closed.
	 */
	private void attachWindowListener()
	{
		addWindowListener(new WindowAdapter() {
		
			/**
			 * Stops the timer when closing the dialog.
			 * @see WindowAdapter#windowClosed(WindowEvent)
			 */
			public void windowClosed(WindowEvent e) {
				super.windowClosed(e);
				if (timer != null) stop();
				Iterator<ImageNode> i = nodes.iterator();
				while (i.hasNext()) {
					(i.next()).setListenToBorder(true);
				}
				firePropertyChange(CLOSE_SLIDE_VIEW_PROPERTY, Boolean.FALSE, 
									Boolean.TRUE);
			}
		
		});
	}
	/** Sets the speed of play. */
	private void setSpeed()
	{
		switch (speeds.getSelectedIndex()) {
			case SLOW_SPEED:
				delay = DELAY_SLOW;
				break;
			case MEDIUM_SPEED:
				delay = DELAY_MEDIUM;
				break;
			case FAST_SPEED:
				delay = DELAY_FAST;
				break;	
		}
		timer.setDelay(delay);
	    timer.setInitialDelay(delay);
	}
	
	/** 
	 * Plays the slide show.
	 * 
	 *  @param index 	The playing index, 
	 *  				either {@link #PLAY_FORWARD} or {@link #PLAY_BACKWARD}.
	 */
	private void play(int index)
	{
		if (index == playingIndex) return;
		if (state == START) {
			stop();
		}
		playingIndex = index;
		forwardPlay.setSelected(playingIndex == PLAY_FORWARD);
		backwardPlay.setSelected(playingIndex == PLAY_BACKWARD);
		state = START;
		timer.start();
	}
	
	/** Plays the slide show. */
	private void stop()
	{
		if (state == STOP) return;
		playingIndex = -1;
		state = STOP;
		forwardPlay.setSelected(false);
		backwardPlay.setSelected(false);
		timer.stop();
	}

	/**
	 * Sets the color of the specified node.
	 * 
	 * @param node The selected node.
	 */
	private void setNodeColor(ImageNode node)
	{
		Iterator<ImageNode> i = nodes.iterator();
		Colors colors = Colors.getInstance();
		ImageNode n;
		while (i.hasNext()) {
			n = i.next();
			if (n != node) {
				n.setHighlight(colors.getDeselectedHighLight(n));
				n.repaint();
			}
		}
		node.setHighlight(Colors.getInstance().getSelectedHighLight(node));
		node.repaint();
	}
	
	/** Paints the selected images on the canvas. */
	private void paintImage()
	{
		ImageNode node = nodes.get(selectedNodeIndex);
		if (node == null) return;
		titleLabel.setText(node.getNodeName());
		uiDelegate.paintImage(node.getThumbnail().getFullSizeImage());
		setNodeColor(node);
	}
	
	/** 
	 * Builds the UI component hosting the controls.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBar()
	{
		JPanel p = new JPanel();
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		bar.setBorder(null);
		bar.add(backwardPlay);
		bar.add(pause);
		bar.add(forwardPlay);
		p.add(bar);
		JPanel speed = new JPanel();
		speed.add(new JLabel("Speed:"));
		speed.add(speeds);
		p.add(UIUtilities.buildComponentPanel(speed));
		p.add(Box.createHorizontalStrut(10));
		p.add(UIUtilities.buildComponentPanel(titleLabel));
		return UIUtilities.buildComponentPanel(p);
	}
	
	/**
	 * Builds the component hosting the nodes.
	 * 
	 * @return See above.
	 */
	private JComponent buildNodesPane()
	{
		JPanel p = new JPanel();
		p.setBackground(UIUtilities.BACKGROUND);
		Iterator<ImageNode> i = nodes.iterator();
		ImageNode node;
		int index = 0;
		while (i.hasNext()) {
			node = i.next();
			node.setListenToBorder(false);
			nodesMap.put(node, index);
			p.add(node);
			index++;
		}
		nodesPane = new JScrollPane(nodesPane);
		return p;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		Container c = getContentPane();
		c.add(buildToolBar(), BorderLayout.NORTH);
		//
		pane.setBottomComponent(buildNodesPane());
		c.add(pane, BorderLayout.CENTER);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent 	The parent of this dialog.
	 * @param nodes		The collection of nodes to display.
	 */
	SlideShowView(JFrame parent, List<ImageNode> nodes)
	{
		super(parent);
		setTitle("Slide Show");
		this.nodes = nodes;
		initComponents();
		buildGUI();
		attachWindowListener();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = 8*(screenSize.width/10);
        int height = 8*(screenSize.height/10);
        setSize(width, height); 
	}
	
	/**
     * Sets the value of the progress bar.
     * 
     * @param hide  Pass <code>true</code> to remove the progress bar, 
     * 				and replace it by the canvas.
     * @param value  The value to set.
     */
	void setProgress(boolean hide, int value)
	{
		uiDelegate.setProgress(hide, value);
		if (hide) {
			paintImage();
		}
		pane.revalidate();
		pane.repaint();
	}
	
	/**
	 * Plays the slide show.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == timer) {
			int size = nodes.size();
			switch (playingIndex) {
				case PLAY_FORWARD:
					selectedNodeIndex++;
					if (selectedNodeIndex == size)
						selectedNodeIndex = 0;
					break;
				case PLAY_BACKWARD:
					if (selectedNodeIndex == 0)
						selectedNodeIndex = size;
					selectedNodeIndex--;
					break;
			}
			paintImage();
			return;
		}
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case PAUSE:
				stop();
				break;
			case PLAY_BACKWARD:
				play(PLAY_BACKWARD);
				break;
			case PLAY_FORWARD:
				play(PLAY_FORWARD);
				break;
			case SPEED:
				setSpeed();
		}
	}

	/**
	 * Sets the selected node.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (Browser.SELECTED_DISPLAY_PROPERTY.equals(name)) {
			ImageDisplay node = (ImageDisplay) evt.getNewValue();
            if (node == null) return;
            Integer value = nodesMap.get(node);
            if (value != null) {
            	selectedNodeIndex = value.intValue();
            	paintImage();
            }
		} else if (SlideShowCanvas.SELECT_PROPERTY.equals(name)) {
			selectedNodeIndex++;
			if (selectedNodeIndex == nodes.size())
				selectedNodeIndex = 0;
			paintImage();
		}
	}
	
}
