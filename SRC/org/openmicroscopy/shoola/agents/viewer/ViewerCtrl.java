/*
 * org.openmicroscopy.shoola.agents.viewer.ViewerCtrl
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

package org.openmicroscopy.shoola.agents.viewer;

//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.AbstractButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.controls.ToolBarManager;
import org.openmicroscopy.shoola.agents.viewer.util.ImageSaver;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

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
public class ViewerCtrl
	implements ActionListener, InternalFrameListener, ChangeListener
{
	
	/** Action Command ID. */
	static final int			V_VISIBLE = 0;
	static final int			RENDERING = 1;
	static final int			SAVE_AS = 2;
	static final int			MOVIE_PLAY = 3;
	static final int			MOVIE_STOP = 4;
	static final int			MOVIE_REWIND = 5;
	
	private JSlider				tSlider, zSlider;
	
	private Viewer				abstraction;
	
	private ViewerUIF			presentation;
	
	ViewerCtrl(Viewer abstraction)
	{
		this.abstraction = abstraction;
	}
	
	void setPresentation(ViewerUIF presentation)
	{
		this.presentation = presentation;
	}
	
	/** 
	 * Attach an InternalFrameListener to the 
	 * {@link ViewerUIF presentaion}.
	 */
	void attachListener() 
	{
		tSlider = presentation.getTSlider(); 
		zSlider = presentation.getZSlider();
		tSlider.addChangeListener(this);
		zSlider.addChangeListener(this);
		presentation.addInternalFrameListener(this);
	}

	/** Return the {@link Viewer abstraction}. */
	Viewer getAbstraction() { return abstraction; }
	
	/** Attach listener to a menu Item. */
	void attachItemListener(AbstractButton item, int id)
	{
		item.setActionCommand(""+id);
		item.addActionListener(this);
	}
	
	/** Forward event to {@link Viewer abstraction}. */
	public JFrame getReferenceFrame()
	{
		return abstraction.getRegistry().getTopFrame().getFrame();
	}
	
	/** Forward event to {@link Viewer abstraction}. */
	public Registry getRegistry()
	{
		return abstraction.getRegistry();
	}
	
	/** Forward event to {@link Viewer abstraction}. */
	public BufferedImage getBufferedImage()
	{
		return abstraction.getCurImage();
	}
	
	/** Forward event to {@link Viewer abstraction}. */
	public PixelsDimensions getPixelsDims()
	{
		return abstraction.getPixelsDims();
	}
	
	/** Forward event to {@link Viewer abstraction}. */
	public int getDefaultT()
	{
		return abstraction.getDefaultT();
	}
	
	/** Forward event to {@link Viewer abstraction}. */
	public int getDefaultZ()
	{
		return abstraction.getDefaultZ();
	}
	
	/** Forward event to {@link Viewer abstraction}. */
	public void onPlaneSelected(int z, int t)
	{
		abstraction.onPlaneSelected(z, t);
	}
	
	/** Update t-slider. */
	public void onTChange(int z, int t)
	{
		//remove listener otherwise an event is fired.
		tSlider.removeChangeListener(this);
		tSlider.setValue(t);
		tSlider.addChangeListener(this);
		abstraction.onPlaneSelected(z, t);
	}
	
	/** Update z-slider. */
	public void onZChange(int z, int t)
	{
		//remove listener otherwise an event is fired.
		zSlider.removeChangeListener(this);
		zSlider.setValue(z);
		zSlider.addChangeListener(this);
		abstraction.onPlaneSelected(z, t);
	}
	
	/** Bring up the rendering widget. */
	public void showRendering()
	{
		abstraction.showRendering();
	}
	
	/** Handles events. */
	public void actionPerformed(ActionEvent e) 
	{
		String s = (String) e.getActionCommand();
		try {
		   int index = Integer.parseInt(s);
		   switch (index) { 
				case V_VISIBLE:
					showPresentation();
					break;
				case RENDERING:
					showRendering();
					break; 	
				case SAVE_AS:
					showImageSaver();
					break;
				/*
				case MOVIE_PLAY:
				case MOVIE_STOP:
				case MOVIE_REWIND:
					break;
				*/
		   }
		} catch(NumberFormatException nfe) {   
			   throw nfe;  //just to be on the safe side...
		} 
	}
	
	/** Handle events fired by the Slider. */
	public void stateChanged(ChangeEvent e)
	{
		Object src = e.getSource();
		int valT, valZ;
		JTextField field;
		valT = tSlider.getValue();
		valZ = zSlider.getValue();
		ToolBarManager tbm = presentation.getToolBar().getManager();
		if (src == tSlider) tbm.onTChange(valT);
		else  tbm.onZChange(valZ);
		abstraction.onPlaneSelected(valZ, valT);
	}
	
	/** Display or not the presentation. */
	private void showPresentation()
	{
		if (presentation != null) {
			if (presentation.isClosed()) abstraction.showPresentation();  
			if (presentation.isIcon()) abstraction.deiconifyPresentation();
			abstraction.setMenuSelection(true);
			//Activate the Frame.
			try {
				presentation.setSelected(true);
			} catch (Exception e) {}	
		}			
	}
	
	/** Forward event to {@link ViewerUIF presentation}. */
	public void showDialog(JDialog dialog)
	{
		presentation.showDialog(dialog);
	}
	
	/** Bring the file chooser. */
	private void showImageSaver()
	{
		if (abstraction.getCurImage() == null) {
			UserNotifier un = getRegistry().getUserNotifier();
			un.notifyError("Save image", "No current image displayed");
		} else	new ImageSaver(this);
	}

	/** Select the checkBox in menu. */
	public void internalFrameOpened(InternalFrameEvent e)
	{
		abstraction.setMenuSelection(true);
	}
	
	/** De-select the checkBox in menu. */
	public void internalFrameClosing(InternalFrameEvent e)
	{
		abstraction.setMenuSelection(false);
	}

	/** De-select the checkBox in menu. */
	public void internalFrameClosed(InternalFrameEvent e) 
	{
		abstraction.setMenuSelection(false);
	}
	
	/** 
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */
	public void internalFrameDeactivated(InternalFrameEvent e) {}

	/** 
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */
	public void internalFrameDeiconified(InternalFrameEvent e) {}

	/** 
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */
	public void internalFrameIconified(InternalFrameEvent e) {}

	/** 
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */
	public void internalFrameActivated(InternalFrameEvent e) {}
	
}
