/*
 * org.openmicroscopy.shoola.agents.zoombrowser.ui.ThumbnailPopupMenu
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

package org.openmicroscopy.shoola.agents.zoombrowser.ui;

//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;


//Third-party libraries

//Application-internal dependencies
import 
   org.openmicroscopy.shoola.agents.zoombrowser.piccolo.DatasetBrowserCanvas;
import org.openmicroscopy.shoola.agents.zoombrowser.piccolo.Thumbnail;
import org.openmicroscopy.shoola.env.config.Registry;
/** 
 * A popup window for a datasetbrowser canvas. 
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </smalbl>
 * @since OME2.2
 */
public class ThumbnailPopupMenu extends JPopupMenu implements ActionListener
{

	private DatasetBrowserCanvas canvas;
	private Registry registry;
	private JMenuItem zoomItem;
	private JMenuItem viewItem;
	
	private Thumbnail thumbnail;
	
	public ThumbnailPopupMenu(DatasetBrowserCanvas canvas,Registry registry) {
		super();
		this.canvas = canvas;
		this.registry=registry;
		
		zoomItem = new JMenuItem("Zoom out");
		add(zoomItem);
		zoomItem.addActionListener(this);
		viewItem = new JMenuItem("View Image");
		add(viewItem);
		viewItem.addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (thumbnail == null)
			return;
		if (e.getSource() == zoomItem)  {
			thumbnail.zoomOutOfHalo();
		}
		else if (e.getSource() == viewItem) {
			thumbnail.viewImage(registry);
		}
	}
	
	public void popup(Thumbnail thumb,boolean canZoomOut,Point2D pt) {
		thumbnail = thumb;
		if (canZoomOut)
			zoomItem.setEnabled(true);
		else
			zoomItem.setEnabled(false);
		show(canvas,(int) pt.getX(),(int)pt.getY());
	}
}