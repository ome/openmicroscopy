/*
 * org.openmicroscopy.shoola.util.ui.lens.LensAction 
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
package org.openmicroscopy.shoola.util.ui.lens;

//Java imports
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies

/** 
* Lens Action is called to change the size of the lens based on the index, this
* is called from the popupmenu in the lensUI and menubar in the zoomWindowUI. 
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $Date: $)
* </small>
* @since OME3.0
*/
class LensAction 
	extends AbstractAction
{

	/** Number of options in action. */
	final static int 			MAX = 11;
	
	/** Default size of lens i.e. 20x20. */
	final static int			LENSDEFAULTSIZE = 0;
	
	/** Set lens to 40x40. */
	final static int			LENS40x40 = 1;
	
	/** Set lens to 50x50. */
	final static int			LENS50x50 = 2;
	
	/** Set lens to 60x60. */
	final static int			LENS60x60 = 3;
	
	/** Set lens to 70x70. */
	final static int			LENS70x70 = 4;
	
	/** Set lens to 80x80. */
	final static int			LENS80x80 = 5;
	
	/** Set lens to 90x90. */
	final static int			LENS90x90 = 6;
	
	/** Set lens to 100x100. */
	final static int			LENS100x100 = 7;
	
	/** Set lens to 120x120. */
	final static int			LENS120x120 = 8;
	
	/** Set lens to 150x150. */
	final static int			LENS150x150 = 9;
	
	/** Set lens to manual. */
	final static int			LENSMANUAL = 10;
	
	/** The width corresponding to {@link #LENSDEFAULTSIZE}. */
	private static final int	DEFAULT_SIZE = LensComponent.LENS_DEFAULT_WIDTH;
	
	/** The width corresponding to {@link #LENS40x40}. */
	private static final int	SIZE_40 = 40;
	
	/** The width corresponding to {@link #LENS50x50}. */
	private static final int	SIZE_50 = 50;
	
	/** The width corresponding to {@link #LENS60x60}. */
	private static final int	SIZE_60 = 60;
	
	/** The width corresponding to {@link #LENS70x70}. */
	private static final int	SIZE_70 = 70;
	
	/** The width corresponding to {@link #LENS80x80}. */
	private static final int	SIZE_80 = 80;
	
	/** The width corresponding to {@link #LENS90x90}. */
	private static final int	SIZE_90 = 90;
	
	/** The width corresponding to {@link #LENS100x100}. */
	private static final int	SIZE_100 = 100;
	
	/** The width corresponding to {@link #LENS120x120}. */
	private static final int	SIZE_120 = 120;
	
	/** The width corresponding to {@link #LENS150x150}. */
	private static final int	SIZE_150 = 150;
	
	/** the parent component of the magnifying lens. */
	private LensComponent	lens;
	
	/** The index which refers to the change in the lens size.*/
	private int				index;
	
	/** Names for each action associated with the change in lens size. */
	private static String[]     names;
	   
	static {
		names = new String[MAX];
		names[LENSDEFAULTSIZE] = "Default size";
		names[LENS40x40] = "40x40";
		names[LENS50x50] = "50x50";
		names[LENS60x60] = "60x60";
		names[LENS70x70] = "70x70";
		names[LENS80x80] = "80x80";
		names[LENS90x90] = "90x90";
		names[LENS100x100] = "100x100";
		names[LENS120x120] = "120x120";
		names[LENS150x150] = "150x150";
		names[LENSMANUAL] = "Manual";
	}

  /** 
   * Controls if the specified index is valid.
   * 
   * @param i The index to check.
   */
  private void checkIndex(int i)
  {
      switch (i) {
          case LENSDEFAULTSIZE:
          case LENS40x40:
          case LENS50x50:
          case LENS60x60:
          case LENS70x70:
          case LENS80x80:
          case LENS90x90:
          case LENS100x100:
          case LENS120x120:
          case LENS150x150:
          case LENSMANUAL:
              return;
          default:
              throw new IllegalArgumentException("Index not supported.");
      }
  }
  
  /**
   * Controls if the passed dimension corresponds to one of the
   * predefined index. Returns the index or <code>-1</code>.
   * 
   * @param w	The width of the lens.
   * @param h The height of the lens.
   * @return See above.
   */
  static int sizeToIndex(int w, int h)
  {
	  if (w != h) return LENSMANUAL;
	  switch (w) {
		  case SIZE_50:
			  return LENS50x50;
		  case SIZE_40:
			  return LENS40x40;
		  case SIZE_60:
			  return LENS60x60;
		  case SIZE_70:
			  return LENS70x70;
		  case SIZE_80:
			  return LENS80x80;
		  case SIZE_90:
			  return LENS90x90;
		  case SIZE_100:
			  return LENS100x100;
		  case SIZE_120:
			  return LENS120x120;
		  case SIZE_150:
			  return LENS150x150;
		  default:
			  return LENSMANUAL;
	  }
  }
  
	/**
	 * Lens action changes the size of the lens based on the parameter 
	 * lensIndex. 
	 * 
	 * @param lens      The parent component. Mustn't be <code>null</code>.
	 * @param lensIndex The action index. One of the constants
	 * 					defined by this class.
	 */
	LensAction(LensComponent lens, int lensIndex)
	{
		if (lens == null)
			throw new IllegalArgumentException("No parent.");
		this.lens = lens;
		checkIndex(lensIndex);
		index = lensIndex;
        putValue(Action.NAME, names[index]);
	}
	
	/**
	 * Returns the index of the action.
	 * 
	 * @return See above.
	 */
	int getIndex() { return index; }
	
	/** 
   * Sets the size of the lens.
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{ 
		switch (index) {
	        case LENSDEFAULTSIZE:
	        	lens.setLensSize(DEFAULT_SIZE, DEFAULT_SIZE);
	       	 	break;
	        case LENS40x40:
	        	lens.setLensSize(SIZE_40, SIZE_40);
	        	break;
	        case LensAction.LENS50x50:
	        	lens.setLensSize(SIZE_50, SIZE_50);;
	        	break;
	        case LensAction.LENS60x60:
	        	lens.setLensSize(SIZE_60, SIZE_60);
	        	break;
	        case LensAction.LENS70x70:
	        	lens.setLensSize(SIZE_70, SIZE_70);
	        	break;
	        case LensAction.LENS80x80:
	        	lens.setLensSize(SIZE_80, SIZE_80);
	        	break;
	        case LensAction.LENS90x90:
	        	lens.setLensSize(SIZE_90, SIZE_90);
	        	break;
	        case LensAction.LENS100x100:
	        	lens.setLensSize(SIZE_100, SIZE_100);
	        	break;
	        case LensAction.LENS120x120:
	        	lens.setLensSize(SIZE_120, SIZE_120);
	        	break;
	        case LensAction.LENS150x150:
	        	lens.setLensSize(SIZE_150, SIZE_150);
		}
	}

}