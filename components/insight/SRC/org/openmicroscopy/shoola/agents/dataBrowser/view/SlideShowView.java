/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
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


import org.openmicroscopy.shoola.agents.dataBrowser.Colors;
import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail;
import org.openmicroscopy.shoola.agents.util.ui.RollOverThumbnailManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Dialog displaying the slideshow.
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
	implements ActionListener, MouseListener, PropertyChangeListener
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
	
	/** ID indicating to go to the next image. */
	private static final int 		PREVIOUS = 14;
	
	/** ID indicating to go to the previous image. */
	private static final int 		NEXT = 15;
	
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
	
	/** The button to go to the next image. */
	private JButton					next;
	
	/** The button to go to the previous image. */
	private JButton					previous;
	
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
    
    /** The scrollpane hosting the nodes. */
    private JScrollPane				nodePane;
    
    /** The component hosting the main image. */
    private SlideShowUI				uiDelegate;
   
    /** The map used to determine the index of the selected node. */
    private Map<ImageNode, Integer> nodesMap;
    
    /**
     * Finds the first {@link ImageDisplay} in <code>x</code>'s containement
     * hierarchy.
     * 
     * @param x A component.
     * @return The parent {@link ImageDisplay} or <code>null</code> if none
     *         was found.
     */
    private ImageDisplay findParentDisplay(Object x)
    {
        while (true) {
            if (x instanceof ImageDisplay) return (ImageDisplay) x;
            if (x instanceof JComponent) x = ((JComponent) x).getParent();
            else break;
        }
        return null;
    }
    
	/** Initializes the components. */
	private void initComponents()
	{
		nodesMap = new HashMap<ImageNode, Integer>();
		IconManager icons = IconManager.getInstance();
		previous = new JButton(icons.getIcon(IconManager.PREVIOUS));
		previous.setActionCommand(""+PREVIOUS);
		previous.addActionListener(this);
		UIUtilities.unifiedButtonLookAndFeel(previous);
		next = new JButton(icons.getIcon(IconManager.NEXT));
		next.setActionCommand(""+NEXT);
		next.addActionListener(this);
		UIUtilities.unifiedButtonLookAndFeel(next);
		pause = new JButton(icons.getIcon(IconManager.PAUSE));
		pause.setActionCommand(""+PAUSE);
		pause.addActionListener(this);
		pause.setEnabled(false);
		forwardPlay = new JToggleButton(icons.getIcon(IconManager.FORWARD));
		forwardPlay.setActionCommand(""+PLAY_FORWARD);
		forwardPlay.addActionListener(this);
		forwardPlay.setEnabled(false);
		backwardPlay = new JToggleButton(icons.getIcon(IconManager.BACKWARD));
		backwardPlay.setActionCommand(""+PLAY_BACKWARD);
		backwardPlay.addActionListener(this);
		backwardPlay.setEnabled(false);

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
    	pane.setResizeWeight(1);
    	pane.setBackground(UIUtilities.BACKGROUND);
	}
	
	/** 
	 * Stops the timer and fires a property indicating that the slide
	 * show is closed.
	 */
	private void attachWindowListener()
	{
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
		
			/**
			 * Stops the timer when closing the dialog.
			 * @see WindowAdapter#windowClosing(WindowEvent)
			 */
			public void windowClosing(WindowEvent e) {
				stop();
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
		if (state == START) stop();
		playingIndex = index;
		forwardPlay.setSelected(playingIndex == PLAY_FORWARD);
		backwardPlay.setSelected(playingIndex == PLAY_BACKWARD);
		state = START;
		timer.start();
	}
	
	/** Plays the slide show. */
	private void stop()
	{
		if (timer == null) return;
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
				n.setHighlight(colors.getColor(Colors.TITLE_BAR));
				n.repaint();
			}
		}
		node.setHighlight(colors.getColor(Colors.TITLE_BAR_HIGHLIGHT));
		node.repaint();
	}
	
	/** Paints the selected images on the canvas. */
	private void paintImage()
	{
		ImageNode node = nodes.get(selectedNodeIndex);
		if (node == null) return;
		setTitle("Slideshow "+node.toString());
		uiDelegate.paintImage(node.getThumbnail().getFullSizeImage());
		setNodeColor(node);
		
		Rectangle viewRect = nodePane.getViewport().getViewRect();
    	Rectangle bounds = node.getBounds();
    	if (!viewRect.contains(bounds)) {
    		nodePane.getVerticalScrollBar().setValue(bounds.y);
    		nodePane.getHorizontalScrollBar().setValue(bounds.x);
    	}
	}
	
	/** 
	 * Builds the UI component hosting the controls.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBar()
	{
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		bar.setBorder(null);
		bar.add(backwardPlay);
		bar.add(pause);
		bar.add(forwardPlay);
		p.add(bar);
		
		JPanel speed = new JPanel();
		speed.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		speed.add(new JLabel("Speed:"));
		speed.add(speeds);
		p.add(speed);
		return p;
	}
	
	/**
	 * Builds the bottom tool bar.
	 * 
	 * @return See above.
	 */
	private JComponent buildBottomBar()
	{
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		bar.setBorder(null);
		bar.add(previous);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(next);
		return UIUtilities.buildComponentPanelCenter(bar);
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
			node.addListenerToComponents(this);
			nodesMap.put(node, index);
			p.add(node);
			index++;
		}
		nodePane = new JScrollPane(p);
		return nodePane;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		Container c = getContentPane();
		pane.setBottomComponent(buildNodesPane());
		c.add(pane, BorderLayout.CENTER);
		
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		JPanel bar = buildToolBar();
		p.add(bar, BorderLayout.LINE_START);
		p.add(buildBottomBar(), BorderLayout.CENTER);
		Dimension d = bar.getPreferredSize();
		p.add(Box.createHorizontalStrut(d.width), BorderLayout.LINE_END);
		c.add(p, BorderLayout.SOUTH);
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
		setTitle("Slideshow");
		this.nodes = nodes;
		initComponents();
		buildGUI();
		attachWindowListener();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = 6*(screenSize.width/10);
        int height = 6*(screenSize.height/10);
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
			pause.setEnabled(true);
			forwardPlay.setEnabled(true);
			backwardPlay.setEnabled(true);
			paintImage();
		}
		pane.revalidate();
		pane.repaint();
	}
	
	/** Closes and disposes of the dialog. */
	void close()
	{
		setVisible(false);
		dispose();
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
				break;
			case NEXT:
				selectedNodeIndex++;
				if (selectedNodeIndex == nodes.size())
					selectedNodeIndex = 0;
				paintImage();
				break;
			case PREVIOUS:
				if (selectedNodeIndex == 0)
					selectedNodeIndex = nodes.size();
				selectedNodeIndex--;
				paintImage();
		}
	}

	/**
	 * Sets the selected node.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (SlideShowCanvas.SELECT_NEXT_PROPERTY.equals(name)) {
			selectedNodeIndex++;
			if (selectedNodeIndex == nodes.size())
				selectedNodeIndex = 0;
			paintImage();
		}
	}
    
	/**
	 * Zooms the image or remove the zooming window from the display.
	 * @see MouseListener#mouseEntered(MouseEvent)
	 */
	public void mouseEntered(MouseEvent e)
	{
		Object src = e.getSource();
        ImageDisplay n = findParentDisplay(src);
         if (n != null && n instanceof ImageNode) {
        	ImageNode node = (ImageNode) n;
        	Thumbnail prv = node.getThumbnail();
            BufferedImage full = prv.getFullScaleThumb();
            if (prv.getScalingFactor() == Thumbnail.MAX_SCALING_FACTOR)
            	full = prv.getZoomedFullScaleThumb();
        	 RollOverThumbnailManager.rollOverDisplay(full, node.getBounds(), 
        			 node.getLocationOnScreen(), node.toString());
         } else RollOverThumbnailManager.stopOverDisplay();
	}

	/**
	 * Removes the zooming window from the display.
	 * @see MouseListener#mouseExited(MouseEvent)
	 */
	public void mouseExited(MouseEvent e)
	{
		RollOverThumbnailManager.stopOverDisplay();
	}

	/**
	 * Sets the selected image.
	 * @see MouseListener#mousePressed(MouseEvent)
	 */
	public void mousePressed(MouseEvent e)
	{
		ImageDisplay node = findParentDisplay(e.getSource());
		if (node == null) return;
		Integer value = nodesMap.get(node);
		if (value != null) {
			selectedNodeIndex = value.intValue();
			paintImage();
		}
	}

	/**
	 * Views the image if the user clicks twice on the thumbnail.
	 * @see MouseListener#mouseReleased(MouseEvent)
	 */
	public void mouseReleased(MouseEvent e)
	{
		Object src = e.getSource();
		/*
        ImageDisplay d = findParentDisplay(src);
        if (d instanceof ImageNode && !(d.getTitleBar() == src) 
            && e.getClickCount() == 2) {
        	EventBus bus = DataBrowserAgent.getRegistry().getEventBus();
        	bus.post(new ViewImage(new ViewImageObject(
        				(ImageData) d.getHierarchyObject()), null));
        }*/
	}
	
    /**
     * Required by the {@link MouseListener} I/F but no-op implementation
     * in our case.
     * @see MouseListener#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent me) {}
    
}
