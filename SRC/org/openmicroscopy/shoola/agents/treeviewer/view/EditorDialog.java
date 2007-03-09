/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.EditorDialog
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

package org.openmicroscopy.shoola.agents.treeviewer.view;



//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.WindowConstants;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.editors.Editor;

/** 
 * Basic modal dialog brought up when the use wants to create a new 
 * container.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class EditorDialog
    extends JDialog
    implements ComponentListener
{

    /** The default size of the dialog. */
    private static final Dimension WIN_DIM = new Dimension(600, 350);
   
    /** The default title of the window. */
    private static final String		TITLE = "Create new element";
   
    /** The editor displayed. */
    private Editor	editor;
    
    /**
     * Creates a new instance.
     * 
     * @param owner 	The owner of the frame.
     * @param editor 	The editor to display. Mustn't be <code>null</code>.
     */
    EditorDialog(JFrame owner, final Editor editor)
    {
        super(owner);
        if (editor == null)
        	throw new IllegalArgumentException("No editor.");
        this.editor = editor;
        setTitle(TITLE);
        setModal(true);
        getContentPane().add(editor.getUI(), BorderLayout.CENTER);
        setSize(WIN_DIM);
        editor.setSize(WIN_DIM);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
        	public void windowOpened(WindowEvent e) {
        		editor.setFocusOnName();
        	} 
        });
        addComponentListener(this);
        editor.setDefaultButton(getRootPane());
    }
    
    /** Closes and disposes. */
    void close()
    {
        setVisible(false);
        dispose();
    }

	/** 
     * Resizes the editor when the window is resized.
     * @see ComponentListener#componentResized(ComponentEvent)
     */
	public void componentResized(ComponentEvent e) 
	{
		editor.setSize(getSize());
	}

	/** 
     * Required by {@link ComponentListener} interface but no-op implementation 
     * in our case. 
     * @see ComponentListener#componentHidden(ComponentEvent)
     */
	public void componentHidden(ComponentEvent e) {}

	/** 
     * Required by {@link ComponentListener} interface but no-op implementation 
     * in our case. 
     * @see ComponentListener#componentMoved(ComponentEvent)
     */
	public void componentMoved(ComponentEvent e) {}
	
	/** 
     * Required by {@link ComponentListener} interface but no-op implementation 
     * in our case. 
     * @see ComponentListener#componentShown(ComponentEvent)
     */
	public void componentShown(ComponentEvent e) {}

}
