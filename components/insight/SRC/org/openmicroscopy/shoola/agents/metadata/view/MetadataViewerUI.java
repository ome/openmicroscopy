/*
 * org.openmicroscopy.shoola.agents.metadata.view.MetadataViewerUI 
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
package org.openmicroscopy.shoola.agents.metadata.view;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.RenderingControlLoader;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.border.FrameBorder;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * The View.
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
class MetadataViewerUI 
	extends TopWindow
{

    /** The text corresponding to the creation of a <code>Project</code>. */
    private static final String     PROJECT_MSG = "Project";
    
    /** The text corresponding to the creation of a <code>Dataset</code>. */
    private static final String     DATASET_MSG = "Dataset";
    
    /** The text corresponding to the creation of a <code>Image</code>. */
    private static final String     IMAGE_MSG = "Image";
    
    /** The title of the dialog. */
    private static final String		TITLE = "Add metadata";
    
    /** The description of the dilaog.. */
    private static final String		DESCRIPTION = "Add comments, tags, etc., " +
    		"to the selected items.";
    
	/** Reference to the Control. */
	private MetadataViewerControl 		controller;

	/** Reference to the Model. */
	private MetadataViewerModel   		model;
	
	/** The component hosting the UI components. */
	private JPanel						uiDelegate;
	
	/** The header of the component. */
	private TitlePanel 					titlePanel;
	
	/** 
     * Returns the message corresponding to the <code>DataObject</code>.
     * 
     * @return See above
     */
    private String getMessage()
    {
        Class nodeType = model.getRefObject().getClass();
        if (nodeType.equals(ProjectData.class)) return PROJECT_MSG;
        else if (nodeType.equals(DatasetData.class)) return DATASET_MSG;
        else if (nodeType.equals(ImageData.class)) return IMAGE_MSG;
        return "";
    }
    
	/** Builds and lays out the GUI. */
    private void buildGUI()
    {
    	IconManager icons = IconManager.getInstance();
		titlePanel = new TitlePanel(TITLE, DESCRIPTION, 
                 icons.getIcon(IconManager.METADATA_48));
		
		
    	Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        //c.add(model.getEditor().getUI());
        
        JSplitPane pane = new JSplitPane();
        pane.setResizeWeight(1.0);
        pane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        pane.setOneTouchExpandable(true);
        pane.setContinuousLayout(true);
        pane.setLeftComponent(model.getEditor().getUI());
        //pane.setRightComponent(model.getBrowser().getUI());
        uiDelegate = new JPanel();
        uiDelegate.setLayout(new BorderLayout(0, 0));
        uiDelegate.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        //uiDelegate.add(titlePanel, BorderLayout.NORTH);
        uiDelegate.add(model.getEditor().getUI(), BorderLayout.CENTER);
        c.add(titlePanel, BorderLayout.NORTH);
        c.add(uiDelegate, BorderLayout.CENTER);
    }
    
	/**
	 * Creates a new instance.
	 * The 
	 * {@link #initialize(MetadataViewerControl, MetadataViewerModel) initialize} 
	 * method should be called straight after to link this View 
	 * to the Controller.
	 * 
	 * @param title The window title.
	 */
	MetadataViewerUI()
	{
		super("");
	}
	
	/**
	 * Links this View to its Controller and Model.
	 * 
	 * @param controller    Reference to the Control.
	 *                      Mustn't be <code>null</code>.
	 * @param model         Reference to the Model.
	 *                      Mustn't be <code>null</code>.
	 */
	void initialize(MetadataViewerControl controller, MetadataViewerModel model)
	{
		if (controller == null) throw new NullPointerException("No control.");
		if (model == null) throw new NullPointerException("No model.");
		this.controller = controller;
		this.model = model;
		buildGUI();
	}
	
	/** Updates display when the new root node is set. */
	void setRootObject()
	{
		String message = getMessage();
		titlePanel.setTitle(message);
		titlePanel.setSubtitle("Edit the "+ message.toLowerCase()+": "+
				model.getRefObjectName());
		uiDelegate.revalidate();
		uiDelegate.repaint();
	}
	
	/**
	 * Sets the specified thumbnail 
	 * 
	 * @param thumbnail The thumbnail to set.
	 */
	void setThumbnail(BufferedImage thumbnail)
	{
		JLabel label = new JLabel(new ImageIcon(thumbnail));
        label.setBorder(new FrameBorder(Color.BLACK));
        label.addMouseListener(new MouseAdapter() {
            
            /**
             * Views the image if the user double-clicks on the thumbnail.
             */
            public void mouseClicked(MouseEvent e)
            {
                //if (e.getClickCount() == 2) 
                    //model.browse(model.getHierarchyObject());
            }
        });
        //titlePanel.setIconComponent(label);
        uiDelegate.revalidate();
		uiDelegate.repaint();
	}
	
	/**
	 * Returns the UI.
	 * 
	 * @return See above.
	 */
	JComponent getUI() { return uiDelegate; }
	
	/**
	 * Indicates that the color of the passed channel has changed.
	 * 
	 * @param index The index of the channel.
	 */
	void onChannelColorChanged(int index)
	{
		model.getEditor().onChannelColorChanged(index);
	}
	
	/** Overrides the {@link #setOnScreen() setOnScreen} method. */
    public void setOnScreen()
    {
    	/*
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(8*(screenSize.width/10), 8*(screenSize.height/10));
        UIUtilities.centerAndShow(this);
        */
    }

}
