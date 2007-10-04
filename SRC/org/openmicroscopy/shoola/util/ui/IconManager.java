/*
 * org.openmicroscopy.shoola.util.ui.IconManager
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

package org.openmicroscopy.shoola.util.ui;


//Java imports
import javax.swing.Icon;
import javax.swing.ImageIcon;


//Third-party libraries

//Application-internal dependencies

/** 
 * Provides the icons used by the util.ui package.
 * <p>The icons are retrieved by first calling the 
 * {@link #getInstance() getInstance} method and then the 
 * {@link #getIcon(int) getIcon} method passing one of the icon ID's specified
 * by the static constants within this class.</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class IconManager
{

    /** ID of the <code>Colour slider</code> icon. */
    public static final int COLOUR_SLIDER = 0;
    
    /** ID of the <code>Colour watch</code> icon. */
    public static final int COLOUR_SWATCH = 1;
    
    /** ID of the <code>Colour wheel</code> icon. */
    public static final int COLOUR_WHEEL = 2;
    
    /** ID of the <code>Cancel</code> icon. */
    public static final int CANCEL = 3;
    
    /** ID of the <code>OK</code> icon. */
    public static final int OK = 4;
    
    /** ID of the <code>Undo</code> icon. */
    public static final int UNDO = 5;
    
    /** ID of the <code>Thumb</code> icon for the slider. */
    public static final int THUMB = 6;
    
    /** ID of the <code>ThumbDisabled</code> icon for the slider. */
    public static final int THUMB_DISABLED = 7;

    /** ID of the <code>UpArrow</code> icon for the slider. */
    public static final int UP_ARROW = 8;
    
    /** ID of the <code>DownArrow</code> icon for the slider. */
    public static final int DOWN_ARROW = 9;
    
    /** ID of the <code>LeftArrow</code> icon for the slider. */
    public static final int LEFT_ARROW = 10;
    
    /** ID of the <code>RightArrow</code> icon for the slider. */
    public static final int RIGHT_ARROW = 11;
    
    /** ID of the <code>UpArrowDisabled</code> icon for the slider. */
    public static final int UP_ARROW_DISABLED = 12;
    
    /** ID of the <code>DownArrowDisabled</code> icon for the slider. */
    public static final int DOWN_ARROW_DISABLED = 13;

    /** ID of the <code>LeftArrowDisabled</code> icon for the slider. */
    public static final int LEFT_ARROW_DISABLED = 14;

    /** ID of the <code>RightArrowDisabled</code> icon for the slider. */
    public static final int RIGHT_ARROW_DISABLED = 15;

    /** ID of the <code>QuestionIcon</code> icon for the slider. */
    public static final int QUESTION_ICON = 16;
    
    /** ID of the <code>Annotation 48</code> icon. */
    public static final int ANNOTATION_48 = 17;

    /** ID of the <code>Info</code> icon. */
    public static final int INFO = 18;
    
    /** ID of the <code>Progress</code> icon. */
    public static final int PROGRESS = 19;
    
    /** ID of the <code>Classification 48</code> icon. */
    public static final int CLASSIFICATION_48 = 20;
    
    /** ID of the <code>Comment icon 64</code> icon. */
    public static final int COMMENT_ICON_64 = 21;
    
    /** ID of the <code>Error icon 64</code> icon. */
    public static final int ERROR_ICON_64 = 22;
    
    /** ID of the <code>Server</code> icon. */
    public static final int SERVER = 23;
    
    /** ID of the <code>Remove</code> icon. */
    public static final int REMOVE = 24;
    
    /** ID of the <code>Config</code> icon. */
    public static final int CONFIG = 25;
    
    /** ID of the <code>Config pressed</code> icon. */
    public static final int CONFIG_PRESSED = 26;
    
    /** ID of the <code>Config 48</code> icon. */
    public static final int CONFIG_48= 27;
  
    /** ID of the <code>Add</code> icon. */
    public static final int ADD = 28;
    
    /** ID of the <code>Info 32</code> icon. */
    public static final int INFO_32 = 29;
  
    /** ID of the <code>Download 48</code> icon. */
    public static final int DOWNLOAD_48 = 30;
    
    /** ID of the <code>Minus</code> icon. */
    public static final int MINUS = 31;
    
    /** ID of the <code>Minus over</code> icon. */
    public static final int MINUS_OVER = 32;
    
    /** ID of the <code>Plus</code> icon. */
    public static final int PLUS = 33;
    
    /** ID of the <code>Plus over</code> icon. */
    public static final int PLUS_OVER = 34;
    
    /** ID of the <code>Cross</code> icon. */
    public static final int CROSS = 35;
    
    /** ID of the <code>Cross over</code> icon. */
    public static final int CROSS_OVER = 36;
    
    /** ID of the <code>Error</code> icon. */
    public static final int ERROR = 37;
    
    /** ID of the <code>History</code> icon. */
    public static final int HISTORY = 38;
    
    /** ID of the <code>Backward navigation</code> icon. */
    public static final int BACKWARD_NAV = 39;
    
    /** ID of the <code>Trashcan full</code> icon. */
    public static final int TRASH_CAN = 40;
    
    /** ID of the <code>Save</code> icon. */
    public static final int SAVE = 41;
    
    /** ID of the <code>Owner</code> icon. */
    public static final int OWNER = 42;
    
    /** ID of the <code>Calendar</code> icon. */
    public static final int CALENDAR = 43;
    
    /** ID of the <code>Image</code> icon. */
    public static final int IMAGE = 44;
    
    /** ID of the <code>Dataset</code> icon. */
    public static final int DATASET = 45;
 
    /** ID of the single-view icon in the browser's internal frame. */
    public static final int	SINGLE_VIEW_MODE = 46;
    
    /** ID of the single-view over icon in the browser's internal frame. */
    public static final int	SINGLE_VIEW_MODE_OVER = 47;

    /** ID of the multi-view icon in the browser's internal frame. */
    public static final int	MULTI_VIEW_MODE = 48;
    
    /** ID of the views list icon in the browser's internal frame. */
    public static final int	VIEWS_LIST = 49;
    
    /** ID of the views list over icon in the browser's internal frame. */
    public static final int	VIEWS_LIST_OVER = 50;
    
    /** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing. 
     */
    private static int      MAX_ID = 51;
    
    /** Paths of the icon files. */
    private static String[]     relPaths = new String[MAX_ID+1];
    static {  
        relPaths[COLOUR_SLIDER] = "coloursliders24.png";
        relPaths[COLOUR_SWATCH] = "colourswatch24.png";
        relPaths[COLOUR_WHEEL] = "colourwheel24.png";
        relPaths[CANCEL] = "nuvola_cancel22.png";
        relPaths[OK] = "nuvola_button_accept22.png";
        relPaths[UNDO] = "nuvola_undo22.png";
        relPaths[THUMB] = "sliderthumb.png";
        relPaths[THUMB_DISABLED] = "sliderthumb_disabled.png";
        relPaths[UP_ARROW] = "nuvola_player_play10_up.png";
        relPaths[DOWN_ARROW] = "nuvola_player_play10_down.png";
        relPaths[LEFT_ARROW] = "nuvola_player_play10_left.png"; 
        relPaths[RIGHT_ARROW] = "nuvola_player_play10_right.png";
        relPaths[UP_ARROW_DISABLED] = "nuvola_player_play10_up_disabled.png";
        relPaths[DOWN_ARROW_DISABLED] = 
        							"nuvola_player_play10_down_disabled.png";
        relPaths[LEFT_ARROW_DISABLED] = 
        							"nuvola_player_play10_left_disabled.png"; 
        relPaths[RIGHT_ARROW_DISABLED] = 
        						"nuvola_player_play10_right_disabled.png";
        relPaths[QUESTION_ICON] = "nuvola_filetypes48.png";
    	relPaths[ANNOTATION_48] = "nuvola_kwrite48.png";
    	relPaths[INFO] = "nuvola_messagebox_info16.png";
    	relPaths[PROGRESS] = "eclipse_progress_none16";
    	relPaths[CLASSIFICATION_48] = "nuvola_filenew48.png";
    	relPaths[ERROR_ICON_64] = "nuvola_error64.png";
    	relPaths[COMMENT_ICON_64] = "nuvola_knotes64.png";
    	relPaths[SERVER] = "nuvola_server22.png";
    	relPaths[CONFIG] = "config.png";
    	relPaths[CONFIG_PRESSED] = "config_pressed.png";
    	relPaths[CONFIG_48] = "nuvola_configure48.png";
    	relPaths[REMOVE] = "nuvola_edit_remove16.png";
    	relPaths[ADD] = "nuvola_edit_add16.png";
    	relPaths[INFO_32] = "nuvola_messagebox_info32.png";
    	relPaths[DOWNLOAD_48] = "nuvola_download_manager48.png";
    	relPaths[MINUS] = "minus.png";
    	relPaths[MINUS_OVER] = "minus_over.png";
    	relPaths[PLUS] = "plus.png";
    	relPaths[PLUS_OVER] = "plus_over.png";
    	relPaths[CROSS] = "cross.png";
    	relPaths[CROSS_OVER] = "cross_over.png";
    	relPaths[ERROR] = "eclipse_error_tsk16.png";
    	relPaths[HISTORY] = "eclipse_history_list16.png";
        relPaths[BACKWARD_NAV] = "eclipse_backward_nav16.png";
        relPaths[TRASH_CAN] = "nuvola_trashcan_full16.png";
        relPaths[SAVE] = "nuvola_filesaveas16.png";
        relPaths[OWNER] = "nuvola_kdmconfig16.png";
        relPaths[CALENDAR] = "nuvola_date16.png";
        relPaths[DATASET] = "nuvola_folder_image16.png";
        relPaths[IMAGE] = "nuvola_image16.png";
        relPaths[SINGLE_VIEW_MODE] = "sinlge_view_mode.png";
        relPaths[SINGLE_VIEW_MODE_OVER] = "sinlge_view_mode_over.png";
        relPaths[MULTI_VIEW_MODE] = "nuvola_view_multicolumn16.png";
        relPaths[VIEWS_LIST] = "frame_list.png";
        relPaths[VIEWS_LIST_OVER] = "frame_list_over.png";
    }
    
    /** 
     * Retrieves the icon specified by <code>id</code>.
     * If the icon can't be retrieved, then this method will log the error and
     * return <code>null</code>.
     *
     * @param id    The index of the file name in the array of file names 
     *              specified to this class' constructor.
     * @return  An {@link Icon} object created from the image file.  The return
     *          value will be <code>null</code> if the file couldn't be found
     *          or an image icon couldn't be created from that file.
     */ 
    public Icon getIcon(int id)
    {
        if (id < 0 || relPaths.length <= id) return null;
        return getIcon(relPaths[id]);
    }
    
    /** 
     * Retrieves the icon specified by <code>name</code>.
     * If the icon can't be retrieved, then this method will log the error and
     * return <code>null</code>.
     *
     * @param name    Must be one a valid icon file name within the directory
     *                  used by the {@link IconFactory} instance specified via
     *                  this class' constructor.
     * @return  An {@link Icon} object created from the image file.  The return
     *          value will be <code>null</code> if the file couldn't be found
     *          or an image icon couldn't be created from that file.
     */ 
    public Icon getIcon(String name)
    {
        Icon icon = factory.getIcon(name);
        if (icon == null) {
            StringBuffer buf = new StringBuffer("Failed to retrieve icon: ");
            buf.append("<classpath>");
            buf.append(factory.getResourcePathname(name));
            buf.append(".");
        }
        return icon;
    }
    
    /** 
     * Retrieves the icon specified by <code>id</code>.
     * If the icon can't be retrieved, then this method will log the error and
     * return <code>null</code>.
     *
     * @param id    The index of the file name in the array of file names 
     *              specified to this class' constructor.
     * @return  An {@link Icon} object created from the image file.  The return
     *          value will be <code>null</code> if the file couldn't be found
     *          or an image icon couldn't be created from that file.
     */ 
    public ImageIcon getImageIcon(int id)
    {
        if (id < 0 || relPaths.length <= id) return null;
        return getImageIcon(relPaths[id]);
    }
    
    /** 
     * Retrieves the icon specified by <code>name</code>.
     * If the icon can't be retrieved, then this method will log the error and
     * return <code>null</code>.
     *
     * @param name    Must be one a valid icon file name within the directory
     *                  used by the {@link IconFactory} instance specified via
     *                  this class' constructor.
     * @return  An {@link Icon} object created from the image file.  The return
     *          value will be <code>null</code> if the file couldn't be found
     *          or an image icon couldn't be created from that file.
     */ 
    public ImageIcon getImageIcon(String name)
    {
        ImageIcon icon = factory.getImageIcon(name);
        if (icon == null) {
            StringBuffer buf = new StringBuffer("Failed to retrieve icon: ");
            buf.append("<classpath>");
            buf.append(factory.getResourcePathname(name));
            buf.append(".");
        }
        return icon;
    }
    
    /** The sole instance. */
    private static IconManager  singleton;
    
    /** The factory. */
    private IconFactory         factory;
 
    /** 
     * Returns the <code>IconManager</code> object. 
     * 
     * @return See above.
     */
    public static IconManager getInstance()
    {
        if (singleton == null) singleton = new IconManager();
        return singleton;
    }
    
    /** Creates a new instance and configures the parameters. */
    private IconManager()
    {
        factory = new IconFactory();
    }
    
}
