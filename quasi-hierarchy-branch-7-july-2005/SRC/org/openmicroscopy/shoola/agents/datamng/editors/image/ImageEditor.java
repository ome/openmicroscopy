/*
 * org.openmicroscopy.shoola.agents.datamng.editors.image.ImageEditor
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

package org.openmicroscopy.shoola.agents.datamng.editors.image;

//Java imports
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.agents.datamng.DataManagerUIF;
import org.openmicroscopy.shoola.agents.datamng.IconManager;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.util.ui.TitlePanel;

/** 
 * Image's property sheet.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
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
public class ImageEditor
	extends JPanel
{
	
    /** Message displayed when no thumbnail is specified. */
    private static final String     MSG = "No thumbnail available",
                                    TITLE = "Thumbnail Size: ";
    
	/** Reference to the manager. */
	private ImageEditorManager 		manager;
	
	/** Reference to the registry. */
	private DataManagerCtrl        agentCtrl;
	
	private ImageGeneralPane		generalPane;
	private ImageInfoPane			infoPane;
	private ImageOwnerPane			ownerPane;
	private ImageEditorBar			bar;
	
	public ImageEditor(DataManagerCtrl agentCtrl, ImageData model, 
                        Image thumbnail)
	{
		this.agentCtrl = agentCtrl;
		manager = new ImageEditorManager(this, agentCtrl, model);
		generalPane = new ImageGeneralPane(manager, 
                    buildThumbnailPanel(thumbnail));
		infoPane = new ImageInfoPane(manager, buildThumbnailPanel(thumbnail));
		ownerPane = new ImageOwnerPane(manager);
		bar = new ImageEditorBar(agentCtrl.getRegistry());
		buildGUI();
		manager.initListeners();
	}
	
    /** Display the image Thumbnail in a JPanel. */
    private JPanel buildThumbnailPanel(Image thumbnail)
    {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout()); 
        JLabel l;
        String title = "";
        if (thumbnail != null) {
            l = new JLabel(new ImageIcon(thumbnail));
            title = TITLE+thumbnail.getWidth(null)+"x"+
                    thumbnail.getHeight(null);
        } else l = new JLabel(MSG);
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(l, BorderLayout.CENTER);
        return p;
    }
    
    /** Returns the view button displayed in {@link ImageEditorBar}. */
    JButton getViewButton() { return bar.viewButton; }
    
	/** Returns the save button displayed in {@link ImageEditorBar}. */
	JButton getSaveButton() { return bar.saveButton; }
	
	/** Returns the TextArea displayed in {@link ImageGeneralPane}. */
	JTextArea getDescriptionArea() { return generalPane.descriptionArea; }

	/** Returns the textfield displayed in {@link ImageGeneralPane}. */
	JTextArea getNameField() { return generalPane.nameField; }
	
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
		//create and initialize the tabs
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, 
										  JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.setAlignmentX(LEFT_ALIGNMENT);
        Registry registry = agentCtrl.getRegistry();
		IconManager im = IconManager.getInstance(registry);
		Font font = (Font) registry.lookup("/resources/fonts/Titles");
		
		tabs.addTab("General", im.getIcon(IconManager.IMAGE), generalPane);
		tabs.addTab("Info", im.getIcon(IconManager.INFO), infoPane);
		tabs.addTab("Owner", im.getIcon(IconManager.OWNER), ownerPane);
		tabs.setSelectedComponent(generalPane);
		tabs.setFont(font);
		tabs.setForeground(DataManagerUIF.STEELBLUE);
        String s = "Editing Image: "+manager.getImageData().getName()+".";
		TitlePanel tp = new TitlePanel("Edit Image", s, 
								im.getIcon(IconManager.IMAGE_BIG));
		//set layout and add components
		setLayout(new BorderLayout(0, 0));
		add(tp, BorderLayout.NORTH);
		add(tabs, BorderLayout.CENTER);
		add(bar, BorderLayout.SOUTH);		
	}

}
