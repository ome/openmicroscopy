/*
 * org.openmicroscopy.shoola.agents.util.annotator.actions.AnnotatorAction 
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.util.annotator.actions;


//Java imports
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.annotator.view.Annotator;

/** 
 * Top class that each action should extend.
 *
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
public class AnnotatorAction
	extends AbstractAction
	implements ChangeListener
{
	
	/** Reference to the <code>Model</code>. */
	protected Annotator model;
	
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
	public AnnotatorAction(Annotator model)
	{
		super();
        setEnabled(false);
        if (model == null) throw new IllegalArgumentException("No Annotator");
        this.model = model;
        model.addChangeListener(this);
	}
	
	/** 
     * Subclasses implement the method.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {}

    /** 
     * Reacts to state changes fired by the Model.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
	public void stateChanged(ChangeEvent e)
	{
		switch (model.getState()) {
			case Annotator.DISCARDED:
			case Annotator.LOADING:
			case Annotator.SAVING:
				setEnabled(false);
				break;
			default:
				setEnabled(true);
				break;
		}
		
	}

}
