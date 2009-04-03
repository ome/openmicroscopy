/*
 * org.openmicroscopy.shoola.agents.spots.ui.StatusPanel
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

package org.openmicroscopy.shoola.agents.spots.ui;

//Java imports
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

//Third-party libraries

//Application-internal dependencies

/** 
 * The panel holding the progress bar that indicates the displayed portion of the 
 * result set 
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */

public class StatusPanel  extends JPanel {

	private int visible;
	private int count;
	private JProgressBar progress;
	
	public StatusPanel() {
		super();
		progress = new JProgressBar();
		add(progress);
	}
	
	public void setCount(int count) {
		this.count = count;
		setVals(count);
	}
	
	public void setVals(int visible) {
		String change = Integer.toString(visible)+"/"+Integer.toString(count);
		progress.setValue(visible);
		progress.setMaximum(count);
		progress.setString(change);
		progress.setStringPainted(true);
	}
		
	public Dimension getMinimumSize() {
		return new Dimension(200,20);
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(200,20);
	}
	
}
