/*
 * org.openmicroscopy.shoola.agents.datamng.ExplorerImagePane
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

package org.openmicroscopy.shoola.agents.datamng;


//Java imports
import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.ImageSummary;

/** 
 * 
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
class ImagesPane
	extends JPanel
    implements ISplitPane
{

    /** Action id. */
    static final int                NAME = 0, DATE = 1, MAX_ID = 1;            
    
    /** This UI component's controller and model. */
    ImagesPaneManager               manager;
    
	ImagesPaneBar					bar;
	
    ImagesSplitPane                 imagesSplitPane;

    private DataManagerCtrl         agentCtrl;
    
	/** 
	 * Creates a new instance.
	 *
	 *@param    agentCtrl   The agent's control component.
	 */
	ImagesPane(DataManagerCtrl agentCtrl)
	{
        this.agentCtrl = agentCtrl;
        manager = new ImagesPaneManager(this, agentCtrl);
		initComponents();
        manager.initListeners();
		buildGUI();
	}
	
    /** Forward action to the {@link ImagesSplitPane}. */
    public void addToRightComponent(JComponent c)
    {
        imagesSplitPane.addToRightComponent(c);
    }
    
    /** Forward action to the {@link ImagesSplitPane}. */
    public void removeFromRightComponent()
    {
        imagesSplitPane.removeFromRightComponent();
    }
    
    void updateImage(ImageSummary is)
    {
        agentCtrl.updateImage(is);
    }
    
    /** Display the images in the left panel. */
    void displayImages(Object[] images) 
    {
        imagesSplitPane.displayImages(images);
    }   
    
	/** Initializes the table and the scrollPane. */
	void initComponents()
	{
        bar = new ImagesPaneBar(agentCtrl.getRegistry());
        imagesSplitPane = new ImagesSplitPane(this, agentCtrl.getRegistry());
	}
	
	/** Return the manager of the component. */
	ImagesPaneManager getManager() { return manager; }
	
	/** Builds and lay out the GUI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout(0, 0));
		add(bar, BorderLayout.NORTH);
		add(imagesSplitPane, BorderLayout.CENTER);
	}
 
}
