/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.DOImagesAnnotation
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

package org.openmicroscopy.shoola.agents.treeviewer.editors;




//Java imports
import javax.swing.JButton;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

/** 
 * The panel hosting the annotations of the images contained in the edited
 * container i.e. either a <code>Dataset</code> or <code>Category</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class DOImagesAnnotation
    extends JPanel
{

    /** 
     * The text of the {@link #loadButton} when the annotations
     * haven't been loaded.
     */
    private static final String LOAD = "Load...";
    
    /** 
     * The text of the {@link #loadButton} when the annotations
     * have been loaded.
     */
    private static final String RELOAD = "Reload..";
    
    /** Button to load the data. */
    private JButton         loadButton;
    
    /** Reference to the Control. */
    private EditorControl   controller;
    
    /** Reference to the Model. */
    private EditorModel     model;
    
    /** Initializes the components. */
    private void initComponents()
    {
        loadButton = new JButton(LOAD);
        
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model         Reference to the Model. 
     *                      Mustn't be <code>null</code>.
     * @param controller    Reference to the Control. 
     *                      Mustn't be <code>null</code>.
     */
    DOImagesAnnotation(EditorModel model, EditorControl controller)
    {
        if (model == null)  throw new IllegalArgumentException("No Model.");
        if (controller == null)  
            throw new IllegalArgumentException("No control.");
        this.controller = controller;
        this.model = model;
        initComponents();
        buildGUI();
    }
    
    /** Displays the images' annotations. */
    void showAnnotations()
    {
        // Build the table.
    }
    
}
