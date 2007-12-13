/*
 * org.openmicroscopy.shoola.agents.imviewer.view.ToolBar
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.imviewer.view;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.imviewer.actions.ClassifyAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.UserAction;
import org.openmicroscopy.shoola.agents.util.finder.QuickFinder;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Presents the variable drawing controls.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class ToolBar
    extends JPanel
    implements ActionListener
{

	/** Flag to indicate that the image is not compressed. */
	static final int				UNCOMPRESSED = 0;
	
	/** 
	 * Flag to indicate that the image is not compressed using a
	 * medium Level of compression. 
	 */
	static final int				MEDIUM = 1;
	
	/** 
	 * Flag to indicate that the image is not compressed using a
	 * low Level of compression. 
	 */
	static final int				LOW = 2;
	
    /** Default text describing the compression check box.  */
    private static final String		COMPRESSED_DESCRIPTION = 
    				"View your image has umcompressed or select " +
    				"the desired compression quality.";
    
    /** The compression option. */
    private static final String[] compression;
    
    static {
    	compression = new String[3];
    	compression[UNCOMPRESSED] = "No compression";
    	compression[MEDIUM] = "Medium compression";
    	compression[LOW] = "High compression";
    }
    
    /** Reference to the Control. */
    private ImViewerControl controller;
    
    /** Reference to the View. */
    private ImViewerUI		view;
    
    /** The tool bar hosting the controls. */
    private JToolBar        bar;
    
    /** Button used to show or hide the renderer. */
    private JToggleButton	rndButton;
    
    /** Button displaying the category. */
    private JButton			categoryButton;

    /** Box used to present the compression selected. */
    private JComboBox		compressionBox;
    
    /** Reference to the finder. */
    private QuickFinder		finder;
    
    /** Helper method to create the tool bar hosting the buttons. */
    private void createControlsBar()
    {
        bar = new JToolBar();
        bar.setFloatable(false);
        bar.setRollover(true);
        bar.setBorder(null);
        rndButton = new JToggleButton();
        rndButton.setSelected(view.isRendererShown());
        rndButton.setAction(controller.getAction(ImViewerControl.RENDERER));
        //UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(rndButton);
        JButton button =  new JButton(
        			controller.getAction(ImViewerControl.MOVIE));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);    
        button =  new JButton(controller.getAction(ImViewerControl.LENS));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);  
        bar.add(new JSeparator(JSeparator.VERTICAL));
        button =  new JButton(controller.getAction(ImViewerControl.ZOOM_IN));
        UIUtilities.unifiedButtonLookAndFeel(button);
        //bar.add(button);    
        button =  new JButton(controller.getAction(ImViewerControl.ZOOM_OUT));
        UIUtilities.unifiedButtonLookAndFeel(button);
        //bar.add(button);  
        button =  new JButton(controller.getAction(ImViewerControl.ZOOM_FIT));
        UIUtilities.unifiedButtonLookAndFeel(button);
        //bar.add(button);  
        //bar.add(new JSeparator(JSeparator.VERTICAL));
        button = new JButton(
        		controller.getAction(ImViewerControl.MEASUREMENT_TOOL));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button); 
        bar.add(new JSeparator(JSeparator.VERTICAL));
        button = new JButton(controller.getAction(ImViewerControl.SAVE));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);
        button = new JButton(
        		controller.getAction(ImViewerControl.IMAGE_DETAILS));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);
        button = new JButton(controller.getAction(ImViewerControl.DOWNLOAD));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);  
        UserAction a = (UserAction) controller.getAction(ImViewerControl.USER);
        button = new JButton(a);
        button.addMouseListener(a);
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);  
        button = new JButton(controller.getAction(ImViewerControl.SEARCH));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);  
    }
    
    /** Initializes the components composing this tool bar. */
    private void initComponents()
    {
    	compressionBox = new JComboBox(compression);
    	compressionBox.setToolTipText(COMPRESSED_DESCRIPTION);
        //compressedBoxsaveOnClose.setSelected(true);
        ClassifyAction a = 
			(ClassifyAction) controller.getAction(ImViewerControl.CATEGORY);
        categoryButton = new JButton(a);
        UIUtilities.unifiedButtonLookAndFeel(categoryButton);
        categoryButton.addMouseListener(a);
        createControlsBar();
    }

    /** 
     * Builds the quick search component.
     * 
     * @return See above.
     */
    private JComponent createQuickSearch()
    {
    	finder = new QuickFinder(ImViewerAgent.getRegistry());
    	return finder;
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
    	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(UIUtilities.buildComponentPanel(bar));
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
        right.add(UIUtilities.buildComponentPanelRight(createQuickSearch()));
        right.add(categoryButton);
        right.setOpaque(true);
        add(UIUtilities.buildComponentPanelRight(right));
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param view			Reference to the view. Mustn't be <code>null</code>.
     * @param controller	Reference to the controller. 
     * 						Mustn't be <code>null</code>.
     */
    ToolBar(ImViewerUI view, ImViewerControl controller)
    {
        if (view == null) throw new NullPointerException("No View.");
        if (controller == null) throw new NullPointerException("No Control.");
        this.view = view;
        this.controller = controller;
        initComponents();
    }
    
    /** 
     * This method should be called straight after the metadata and the
     * rendering settings are loaded.
     */
    void buildComponent()
    { 
//    	Retrieve the preferences.
		ViewerPreferences pref = ImViewerFactory.getPreferences();
		if (pref != null) {
			//Action a = controller.getAction(ImViewerControl.RENDERER);
			//rndButton.removeActionListener(a);
	        rndButton.setSelected(pref.isRenderer());
	        //rndButton.setAction(a);
		}
    	
    	//boolean b = view.isImageCompressed();
    	//if (b) {
		bar.add(new JSeparator(JSeparator.VERTICAL));
		bar.add(compressionBox);
		compressionBox.setSelectedIndex(view.getCompressionLevel());
		compressionBox.addActionListener(this);
    	//}
    	buildGUI(); 
    }
    
    /** Cancels any ongoing search. */
    void discard()
    {
    	if (finder != null) finder.cancel();
    }
    
    /** Selects or deselects the {@link #rndButton}. */
    void displayRenderer() { rndButton.setSelected(view.isRendererShown()); }

    /**
     * Reacts to the selection of the {@link #compressionBox}.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
	public void actionPerformed(ActionEvent e)
	{
		int index = compressionBox.getSelectedIndex();
		view.setCompressionLevel(index);
	}
    
}
