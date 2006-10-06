/*
 * org.openmicroscopy.shoola.agents.imviewer.util.cdm.CodomainMapContextDialog
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

package org.openmicroscopy.shoola.agents.imviewer.util.cdm;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

//Third-party libraries
import ome.model.display.CodomainMapContext;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;


/** 
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
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public abstract class CodomainMapContextDialog
    extends JDialog
{
    
    /** Bound property name to update the codomain map context. */
    public static final String  UPDATE_MAP_CONTEXT_PROPERTY = 
                                "updateMapContext";
    
    /** Note to detail the dialog's working context. */
    static final String         NOTE = "[Note] The output interval " +
                                           "selected in \"Mapping\" defines " +
                                           "the input and output intervals" +
                                           "for this transformation.";
    
    /** Convenience reference for subclasses. */
    protected IconManager           icons;
    
    /** The codomain map context this dialog is for. */
    protected CodomainMapContext    ctx;
    
    /** The lower of the codomain interval. */
    protected int                   cdStart;
    
    /** The upper of the codomain interval. */
    protected int                   cdEnd;
    
    /** Button to close and dispose without saving settings. */
    private JButton                 cancelButton;
    
    /** Close and dispose without saving settings. */
    private void cancel()
    {
        setVisible(false);
        dispose();
    }
    
    /** Initializes the components and the window. */
    private void initialize()
    {
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { cancel(); }
        });
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { cancel(); }
        });
    }
    
    /**
     * Builds the tool bar hosting the controls.
     * 
     * @return See above.
     */
    private JPanel buildToolBar()
    {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setRollover(true);
        bar.add(cancelButton);
        return UIUtilities.buildComponentPanelRight(bar);
    }
    
    /** Updates the codomain map context and fires a property change event. */
    protected void apply()
    {
        updateContext();
        firePropertyChange(UPDATE_MAP_CONTEXT_PROPERTY, null, ctx);
    }

    /** Builds and lays out the GUI. */
    protected void buildGUI()
    {
        Container c = getContentPane();
        c.add(buildBody(), BorderLayout.CENTER);
        c.add(buildToolBar(), BorderLayout.SOUTH);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param owner     The owner of the dialog.
     * @param ctx       The codomain map context this dialog is for.
     * @param cdEnd     The upper bound of the codomain interval.
     * @param cdStart   The lower bound of the codomain interval.
     */
    CodomainMapContextDialog(JFrame owner, CodomainMapContext ctx, 
                int cdEnd, int cdStart)
    {
        super(owner, true); //Modal dialog.
        if (ctx == null)
            throw new IllegalArgumentException("Context cannot be null.");
        this.ctx = ctx;
        this.cdEnd = cdEnd;
        this.cdStart = cdStart;
        icons = IconManager.getInstance();
        initialize();
    }

    /** 
     * Subclasses should build the pane describing the window. 
     * 
     * @return The component hosting the window's information.
     */
    protected abstract JComponent buildTitlePane();
    
    /** 
     * Subclasses should build the body of the dialog. 
     * 
     * @return The component hosting the settings controls.
     */
    protected abstract JComponent buildBody();
    
    /** Updates the codomain map context settings. */
    protected abstract void updateContext();
    
    /** 
     * Sets the title depending on the codomain transformation
     * this window is for.
     */
    protected abstract void setWindowTitle();
}
