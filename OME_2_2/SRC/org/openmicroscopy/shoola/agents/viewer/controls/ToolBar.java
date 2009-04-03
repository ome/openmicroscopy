/*
 * org.openmicroscopy.shoola.agents.viewer.controls.ToolBar
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

package org.openmicroscopy.shoola.agents.viewer.controls;


//Java imports
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.IconManager;
import org.openmicroscopy.shoola.agents.viewer.Viewer;
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
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
public class ToolBar
	extends JPanel
{

	/** Bring up the 3Dimage viewer. */
	private JButton					viewer3D;
	
	/** Bring up the save image widget. */
	private JButton					saveAs;
	
	/** Bring up the  image inspector widget. */
	private JButton					inspector;
																																							
	/** Control to post an event to bring up the rendering widget. */
	private JButton					render;
	
    /** Control to bring up the movie widget. */
    private JButton                 movie;
	
	/** Fields displaying the current z-section and the current timepoint. */
	private JTextField				zField, tField;
		
	/** Labels displaying the number of timepoints and of z-sections. */
	private JLabel					zLabel, tLabel;
	
	private JPanel					ztPanel;
	
	private ToolBarManager			manager;
	
	private int 					txtWidth;
	
	/** 
	 * 
	 * @param control	reference to the {@link ViewerCtrl control}.
	 * @param registry	reference to the {@link Registry registry}.
	 * @param sizeT		timepoint-1 b/c OME values start at 0.
	 * @param sizeZ		number of section-1 b/c OME values start at 0.
	 * @param t			current timepoint.
	 * @param z			current z-section.
	 */
	public ToolBar(ViewerCtrl control, Registry registry, int sizeT,
							int sizeZ, int t, int z)
	{
		initTxtWidth();
		initComponents(registry, sizeZ, sizeT);
		initTextFields(t, z, sizeT, sizeZ);
		manager = new ToolBarManager(control, this, sizeT, t, sizeZ, z);
		manager.attachListeners();
		buildToolBar();
	}

	public JButton getMovie() { return movie; }
	
	public JButton getViewer3D() { return viewer3D; }
	
	public JButton getSaveAs() { return saveAs; }

	public JLabel getZLabel() { return zLabel; }
	
	public JLabel getTLabel() { return tLabel; }
	
	public JButton getInspector() { return inspector; } 

	public JButton getRender() { return render; }

	public JTextField getTField() { return tField; }

	public JTextField getZField() { return zField; }
	
	public ToolBarManager getManager() { return manager; }
	
	/** Initialize the buttons components. */
	private void initComponents(Registry registry, int maxZ, int maxT)
	{
		//buttons
		IconManager im = IconManager.getInstance(registry);
		viewer3D = new JButton(im.getIcon(IconManager.VIEWER3D));
		viewer3D.setToolTipText(
			UIUtilities.formatToolTipText("Bring up the image3D viewer."));
		viewer3D.setEnabled(maxZ == 0);	
		saveAs = new JButton(im.getIcon(IconManager.SAVEAS));
		saveAs.setToolTipText(
			UIUtilities.formatToolTipText("Bring up the save image window."));	
		inspector  =  new JButton(im.getIcon(IconManager.INSPECTOR));
		inspector.setToolTipText(
			UIUtilities.formatToolTipText("Bring up the inspector panel."));
		render =  new JButton(im.getIcon(IconManager.RENDER));
		render.setToolTipText(
			UIUtilities.formatToolTipText("Bring up the rendering panel."));
		
        movie =  new JButton(im.getIcon(IconManager.MOVIE));
        movie.setToolTipText(
            UIUtilities.formatToolTipText("Bring up the movie panel."));
        if (maxT == 0 && maxZ == 0) movie.setEnabled(true);
        
	}
    
	/** 
	 * Initializes the text Fields displaying the current z-section and the
	 * the current timepoint.
	 */
	private void initTextFields(int t, int z, int maxT, int maxZ)
	{
		zLabel = new JLabel("/"+maxZ);
		tLabel = new JLabel("/"+maxT);
		tField = new JTextField(""+t, (""+maxT).length());
		//if (maxT == 0) tField.setEditable(false);
		tField.setEditable(maxT != 0);
		tField.setForeground(Viewer.STEELBLUE);
		tField.setToolTipText(
			UIUtilities.formatToolTipText("Enter a timepoint."));
		zField = new JTextField(""+z, (""+maxZ).length());
		zField.setForeground(Viewer.STEELBLUE);
		zField.setToolTipText(
			UIUtilities.formatToolTipText("Enter a Z point"));
		//if (maxZ-1 == 0) zField.setEditable(maxZ != 0);
		zField.setEditable(maxZ != 0);
		ztPanel = textFieldsPanel((""+maxZ).length(), (""+maxT).length());
	}
	
	/** Build the main tool bar. */
	private void buildToolBar() 
	{
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(buildBar());
		add(ztPanel);
	}
	
	/** Build a toolBar with buttons. */
	private JToolBar buildBar()
	{
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		bar.add(render);
		bar.add(inspector);
		bar.add(viewer3D);
		bar.add(movie);
		bar.add(saveAs);
		bar.add(new JSeparator(SwingConstants.VERTICAL));
		return bar;
	}
	
	/** Build panel with labels and text fields. */
	private JPanel textFieldsPanel(int zLength, int tLength)
	{
		JPanel p = new JPanel();
		JLabel l = new JLabel(" Z ");
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		p.setLayout(gridbag);
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(l, c);
		p.add(l);
		c.gridx = 1;
		Insets insets = zField.getInsets();
		c.ipadx = insets.left+zLength*txtWidth+insets.right;
		gridbag.setConstraints(zField, c);
		p.add(zField);
		c.gridx = 2;
		gridbag.setConstraints(zLabel, c);
		p.add(zLabel);
		c.gridx = 3;
		c.ipadx = 0;
		l = new JLabel(" T ");
		gridbag.setConstraints(l, c);
		p.add(l);
		c.gridx = 4;
		insets = tField.getInsets();
		c.ipadx = insets.left+tLength*txtWidth+insets.right;
		gridbag.setConstraints(tField, c);
		p.add(tField);
		c.gridx = 5;
		gridbag.setConstraints(tLabel, c);
		p.add(tLabel);
		return p;
	}
	
	/** Initializes the width of the text. */
	private void initTxtWidth()
	{
		FontMetrics metrics = getFontMetrics(getFont());
		txtWidth = metrics.charWidth('m');
	}
	
}
