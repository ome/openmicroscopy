/*
 * org.openmicroscopy.shoola.agents.hiviewer.controls.PopupMenu
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

package org.openmicroscopy.shoola.agents.hiviewer.view;



//Java imports
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.border.BevelBorder;

//Third-party libraries

//Application-internal dependencies

/** 
* Pop-up menu for nodes in the browser display.
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
class PopupMenu
	extends JPopupMenu
{
  
	/** 
	 * Button to bring up the property sheet of a hierarchy object &#151; 
	 * project, dataset, category group, category, or image.
	 */
	private JMenuItem   properties;

	/** Button to bring up the widget to annotate a dataset or an image. */
	private JMenuItem   annotate;

	/** Button to bring up the widget to classify an image. */
	private JMenuItem   classify;

	/** Button to bring up the widget to declassify an image. */
	private JMenuItem   declassify;

	/** Button to browse a container or bring up the Viewer for an image. */
	private JMenuItem   view;

	/** Button to remove items from a container. */
	private JMenuItem   remove;

	/** Button to classify the images contained in the selected element. */
	private JMenuItem	classifyChildren;

	/** Button to annotate the images contained in the selected element. */
	private JMenuItem	annotateChildren;

	/** Button to paste the rendering settings. */
	private JMenuItem	pasteRndSettings;
	
	/** Button to reset the rendering settings. */
	private JMenuItem	resetRndSettings;
	
	/** Button to copy the rendering settings. */
	private JMenuItem	copyRndSettings;

	/**
	 * Creates the menu items with the given actions.
	 * 
	 * @param controller The Controller.
	 */
	private void createMenuItems(HiViewerControl controller)
	{
		properties = new JMenuItem(
				controller.getAction(HiViewerControl.PROPERTIES));
		annotate = new JMenuItem(
				controller.getAction(HiViewerControl.ANNOTATE));
		classify = new JMenuItem(
				controller.getAction(HiViewerControl.CLASSIFY));
		declassify = new JMenuItem(
				controller.getAction(HiViewerControl.DECLASSIFY));
		view = new JMenuItem(controller.getAction(HiViewerControl.VIEW));
		remove = new JMenuItem(controller.getAction(HiViewerControl.REMOVE));
		classifyChildren = new JMenuItem(
				controller.getAction(HiViewerControl.CLASSIFY_CHILDREN));
		annotateChildren = new JMenuItem(
				controller.getAction(HiViewerControl.ANNOTATE_CHILDREN));
		pasteRndSettings = new JMenuItem(
				controller.getAction(HiViewerControl.PASTE_RND_SETTINGS));
		copyRndSettings = new JMenuItem(
				controller.getAction(HiViewerControl.COPY_RND_SETTINGS));
		resetRndSettings = new JMenuItem(
				controller.getAction(HiViewerControl.RESET_RND_SETTINGS));
	}

	/** Builds and lays out the GUI. */
	private void buildGUI() 
	{
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		add(view);
		add(new JSeparator(JSeparator.HORIZONTAL));
		add(classify);
		add(declassify);
		add(classifyChildren);
		add(new JSeparator(JSeparator.HORIZONTAL));
		add(annotate);
		add(annotateChildren);
		add(new JSeparator(JSeparator.HORIZONTAL));
		add(copyRndSettings);
		add(pasteRndSettings);
		//add(resetRndSettings);
		add(new JSeparator(JSeparator.HORIZONTAL));
		add(properties);
		add(new JSeparator(JSeparator.HORIZONTAL));
		add(remove);
	}

	/** 
	 * Creates a new instance.
	 *
	 * @param controller The Controller. Mustn't be <code>null</code>.
	 */
	PopupMenu(HiViewerControl controller) 
	{
		if (controller == null) 
			throw new IllegalArgumentException("No control.");
		createMenuItems(controller);
		buildGUI() ;
	}
  
}
