/*
 * org.openmicroscopy.shoola.agents.datamng.ToolBar
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
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
class ToolBar
	extends JPanel
{

    
	/** Dimension of the separator. */
	private static final Dimension	        SEPARATOR = new Dimension(15, 0);
	

    JToolBar                                explToolBar, classifierToolBar;
    JButton                                 project, dataset, image;
	
    JButton                                 createGAndC;
    
	ToolBar(DataManagerCtrl control, Registry registry)
	{
		initButtons(registry);
		new ToolBarManager(control, this);
		buildToolBar();
	}

	/** Initialize the control buttons. */
	private void initButtons(Registry registry)
	{
		IconManager im = IconManager.getInstance(registry);
		project =  new JButton(im.getIcon(IconManager.CREATE_PROJECT));
		project.setToolTipText(
			UIUtilities.formatToolTipText("Create a new project."));
		dataset =  new JButton(im.getIcon(IconManager.CREATE_DATASET));
		dataset.setToolTipText(
			UIUtilities.formatToolTipText("Create a new dataset."));
		image =  new JButton(im.getIcon(IconManager.IMPORT_IMAGE));
		image.setToolTipText(
			UIUtilities.formatToolTipText("Import a new image."));
        createGAndC = new JButton(
                im.getIcon(IconManager.CREATE_CG));
        createGAndC.setToolTipText(
                UIUtilities.formatToolTipText("Create a new category group."));
	}
    
	/** Build and lay out the explorer toolBar. */
	private JToolBar ExplToolBar()
	{
        explToolBar = new JToolBar();
        explToolBar.setBorder(BorderFactory.createEtchedBorder());
        explToolBar.putClientProperty("JToolBar.isRollover", new Boolean(true));
        explToolBar.setFloatable(false);
        explToolBar.add(project);
        explToolBar.addSeparator(SEPARATOR);
        explToolBar.add(dataset);
        explToolBar.addSeparator(SEPARATOR);
        return explToolBar;
	}

    /** Build and lay out the explorer toolBar. */
    private JToolBar ClassifierToolBar()
    {
        classifierToolBar = new JToolBar();
        classifierToolBar.setBorder(BorderFactory.createEtchedBorder());
        classifierToolBar.putClientProperty("JToolBar.isRollover", 
                                        new Boolean(true));
        classifierToolBar.setFloatable(false);
        classifierToolBar.add(createGAndC);
        classifierToolBar.addSeparator(SEPARATOR);
        return classifierToolBar;
    }
    
    /** Build and lay out the ToolBar. */
    private void buildToolBar()
    {
        JPanel bars = new JPanel();
        bars.setBorder(null);
        bars.setLayout(new BoxLayout(bars, BoxLayout.X_AXIS));
        bars.add(ExplToolBar());
        bars.add(ClassifierToolBar());
        setBorder(null);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(bars);
        //add(Box.createRigidArea(new Dimension(100, 16)));
        //add(Box.createHorizontalGlue()); 
    }
    
}
