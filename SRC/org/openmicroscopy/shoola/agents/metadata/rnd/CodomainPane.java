/*
 * org.openmicroscopy.shoola.agents.metadata.rnd.CodomainPane 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.rnd;


//Java imports
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;

/** 
 * Pane displaying the controls used to define the transformations happening 
 * in the device space or codomain i.e. sub-interval of [0, 255].
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class CodomainPane 
	extends ControlPane
{

    /** Button to bring up the contrast stretching modal dialog. */
    private JButton         contrastStretchingButton;
    
    /** Button to bring up the plane slicing modal dialog. */
    private JButton         planeSlicingButton;
    
    /** Box to select the  <code>ReverseIntensityContext</code>. */
    private JCheckBox       reverseIntensity;
    
    /** Box to select the <code>ContrastStretchingContext</code>. */
    private JCheckBox       contrastStretching;
    
    /** Box to select the <code>PlaneSlicingContext</code>. */
    private JCheckBox       planeSlicing;
       
    /** Initializes the UI components. */
    private void initComponents()
    {
        IconManager icons = IconManager.getInstance();
        contrastStretchingButton = new JButton(icons.getIcon(
                        IconManager.CONTRAST_STRETCHING));
        contrastStretchingButton.setEnabled(false);
        contrastStretchingButton.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            {
                /*
                CodomainMapContext ctx = 
                    model.getCodomainMap(PlaneSlicingContext.class);
                JDialog dialog = new ContrastStretchingDialog(view, ctx, 
                        model.getCodomainStart(), model.getCodomainEnd());
                dialog.addPropertyChangeListener(controller);
                UIUtilities.centerAndShow(dialog);
                */
                
                
            }
        });
        
        planeSlicingButton = new JButton(icons.getIcon(
                            IconManager.PLANE_SLICING));
        planeSlicingButton.setEnabled(false);
        planeSlicingButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                /*
                CodomainMapContext ctx = 
                    model.getCodomainMap(PlaneSlicingContext.class);
                JDialog dialog = new PlaneSlicingDialog(view, ctx, 
                        model.getCodomainStart(), model.getCodomainEnd());
                dialog.addPropertyChangeListener(controller);
                UIUtilities.centerAndShow(dialog);
                */
            }

        });
        reverseIntensity = new JCheckBox(
                controller.getAction(RendererControl.REVERSE_INTENSITY));
        planeSlicing = new JCheckBox(
                controller.getAction(RendererControl.PLANE_SLICING));
        contrastStretching = new JCheckBox(
                controller.getAction(RendererControl.CONTRAST_STRETCHING));
        setDefaultValues();
    }
    
    /** Sets the default values. */
    private void setDefaultValues()
    {
       
        /*
         *  List cdMaps = model.getCodomainMaps();
        Iterator i = cdMaps.iterator();
        CodomainMapContext ctx;
        while (i.hasNext()) {
            ctx = (CodomainMapContext) i.next();
            if (ctx instanceof ReverseIntensityContext) 
                reverseIntensity.setSelected(true);
            else if (ctx instanceof ContrastStretchingContext)  {
                contrastStretching.setSelected(true);
                contrastStretchingButton.setEnabled(true);
            } else if (ctx instanceof PlaneSlicingContext)  {
                planeSlicing.setSelected(true);
                planeSlicingButton.setEnabled(true);
            } 
        }
        */
    }
    
    /**
     * Lays out the controls.
     * 
     * @return See below.
     */
    private JPanel buildControlsPane()
    {
    	JPanel p = new JPanel();
    	p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
		c.gridy = 0;
		c.gridx = 0;
		p.add(reverseIntensity, c);
		c.gridy++;
        p.add(contrastStretching, c);
        c.gridx = c.gridx+2;
        p.add(contrastStretchingButton, c);
        c.gridx = 0;
        c.gridy++;
        p.add(planeSlicing, c);
        c.gridx = c.gridx+2;
        p.add(planeSlicingButton, c);
        return p;
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
    	setLayout(new FlowLayout(FlowLayout.LEFT));
        add(buildControlsPane());
    }
    
    /** 
     * Returns the name of the component. 
     * @see ControlPane#getPaneName()
     */
    protected String getPaneName() { return "Device Settings"; }

    /**
     * Returns the icon attached to the component.
     * @see ControlPane#getPaneIcon()
     */
    protected Icon getPaneIcon()
    {
        IconManager icons = IconManager.getInstance();
        return icons.getIcon(IconManager.CODOMAIN);
    }

    /**
     * Returns the brief description of the component.
     * @see ControlPane#getPaneDescription()
     */
    protected String getPaneDescription()
    {
        return "Selects the transformations happening in the device space.";
    }
    
    /**
     * Returns the index of the component.
     * @see ControlPane#getPaneIndex()
     */
    protected int getPaneIndex() { return ControlPane.CODOMAIN_PANE_INDEX; }

    /**
     * Resets the default rendering settings. 
     * @see ControlPane#resetDefaultRndSettings()
     */
    protected void resetDefaultRndSettings()
    {
        reverseIntensity.removeActionListener(
                controller.getAction(RendererControl.REVERSE_INTENSITY));
        planeSlicing.removeActionListener(
                controller.getAction(RendererControl.PLANE_SLICING));
        contrastStretching.removeActionListener(
                controller.getAction(RendererControl.CONTRAST_STRETCHING));
        setDefaultValues();
        reverseIntensity.setAction(
                controller.getAction(RendererControl.REVERSE_INTENSITY));
        planeSlicing.setAction(
                controller.getAction(RendererControl.PLANE_SLICING));
        contrastStretching.setAction(
                controller.getAction(RendererControl.CONTRAST_STRETCHING));
    }
    
    /**
     * Sets the enabled flag of the UI components. 
     * @see ControlPane#onStateChange(boolean)
     */
	protected void onStateChange(boolean b) {}
	
    /**
     * Resets the value of the various controls when the user selects 
     * a new rendering control
     * @see ControlPane#resetDefaultRndSettings()
     */
    protected void switchRndControl() { resetDefaultRndSettings(); }
    
    /**
     * Creates a new instance.
     * 
     * @param model         Reference to the Model.
     *                      Mustn't be <code>null</code>.
     * @param controller    Reference to the Control.
     *                      Mustn't be <code>null</code>.
     */
    CodomainPane(RendererModel model, RendererControl controller)
    {
        super(model, controller);
        initComponents();
        buildGUI();
    }

    /**
     * Updates the corresponding controls when a codomain transformation
     * is added.
     * 
     * @param mapType The type of codomain transformation. 
     */
    void addCodomainMap(Class mapType)
    {
    	/*
        if (mapType.equals(PlaneSlicingContext.class))
            planeSlicingButton.setEnabled(true);
        else if (mapType.equals(ContrastStretchingContext.class))
            contrastStretchingButton.setEnabled(true);
            */
    }
    
    /**
     * Updates the corresponding controls when a codomain transformation
     * is added.
     * 
     * @param mapType The type of codomain transformation. 
     */
    void removeCodomainMap(Class mapType)
    {
    	/*
        if (mapType.equals(PlaneSlicingContext.class))
            planeSlicingButton.setEnabled(false);
        else if (mapType.equals(ContrastStretchingContext.class))
            contrastStretchingButton.setEnabled(false);
            */
    }
    
}
