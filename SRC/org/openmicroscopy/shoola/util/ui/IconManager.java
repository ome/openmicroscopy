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
    
    /** ID of <code>information message</code> icon. */
    public static final int	INFORMATION_MESSAGE = 51;

    /** ID of <code>information message</code> icon. */
    public static final int	QUESTION = 52;

    /** ID of <code>Save 48</code> icon. */
    public static final int	SAVE_48 = 53;
    
    /** ID of <code>Load 48</code> icon. */
    public static final int	LOAD_48 = 54;
    
    /** ID of <code>Search 48</code> icon. */
    public static final int	SEARCH_48 = 55;
    
    /** ID of <code>Search</code> icon. */
    public static final int	SEARCH = 56;
    
    /** The <code>Filter menu</code> icon. */
    public static final int	FILTER_MENU = 57;
    
    /** The <code>Clear disabled</code> icon. */
    public static final int	CLEAR_DISABLED = 58;
    
    /** The <code>Search Tag</code> icon. */
    public static final int	SEARCH_TAG = 59;
    
    /** The <code>Search Image</code> icon. */
    public static final int	SEARCH_IMAGE = 60;
    
    /** The <code>Search Annotation</code> icon. */
    public static final int	SEARCH_ANNOTATION = 61;
    
    /** The <code>Search Dataset</code> icon. */
    public static final int	SEARCH_DATASET = 62;
    
    /** The <code>Search Project</code> icon. */
    public static final int	SEARCH_PROJECT = 63;
    
    /** The <code>Tag</code> icon. */
    public static final int	TAG = 64;
    
    /** The <code>Tag group</code> icon. */
    public static final int	TAG_GROUP = 65;
    
    /** The <code>Tag big</code> icon. */
    public static final int	TAG_BIG = 66;
    
    /** The <code>Tag used</code> icon. */
    public static final int	TAG_USED = 67;
    
    /** The <code>Tag used</code> icon. */
    public static final int	BROWSE = 68;
    
    /** The <code>Transparent</code> icon. */
    public static final int	TRANSPARENT = 69;
    
    /** The <code>Owner 48</code> icon. */
    public static final int	OWNER_48 = 70;
    
    /** The <code>Owner 48</code> icon. */
    public static final int	SEARCH_TAG_SET = 71;
    
    /** The <code>Edit remove</code> icon. */
    public static final int	EDIT_REMOVE = 72;
    
    /** The <code>Add 12</code> icon. */
    public static final int	ADD_12 = 73;
    
    /** The <code>Close 8</code> icon. */
    public static final int	CLOSE = 74;
    
    /** The <code>Close over 8</code> icon. */
    public static final int	CLOSE_OVER = 75;

    /** The <code>Help</code> icon. */
    public static final int	HELP = 76;

    /** The <code>Help</code> icon. */
    public static final int	HELP_48 = 77;
    
    /** The <code>Annotation</code> icon. */
    public static final int	ANNOTATION = 78;
    
    /** The <code>File Annotation</code> icon. */
    public static final int	FILE_ANNOTATION = 79;
    
    /** The <code>URL Annotation</code> icon. */
    public static final int	URL_ANNOTATION = 80;
    
    /** The <code>Start selected</code> icon. */
    public static final int START_SELECTED = 81;
    
    /** The <code>Start unselected</code> icon. */
    public static final int	START_UNSELECTED = 82;
    
    /** The <code>Start unselected</code> icon. */
    public static final int	BROKEN_FILE96 = 83;
    
    /** ID of the <code>RightArrow</code> icon for the slider. */
    public static final int DOUBLE_RIGHT_ARROW = 84;
    
    /** ID of the <code>Double</code> icon for the slider. */
    public static final int DOUBLE_LEFT_ARROW = 85;
    
    /** ID of the <code>RightArrow</code> icon for the slider. */
    public static final int RIGHT_ARROW_16 = 86;
    
    /** ID of the <code>Double</code> icon for the slider. */
    public static final int LEFT_ARROW_16 = 87;
    
    /** The <code>Tag</code> icon. */
    public static final int	TAG_OWNER = 88;
    
    /** The <code>Wizard</code> icon. */
    public static final int	WIZARD = 89;
    
    /** The <code>Start selected</code> icon. */
    public static final int START_SELECTED_12 = 90;
    
    /** The <code>Start unselected</code> icon. */
    public static final int	START_UNSELECTED_12 = 91;
    
    /** The <code>Start selected</code> icon. */
    public static final int START_SELECTED_8 = 92;
    
    /** The <code>Start unselected</code> icon. */
    public static final int	START_UNSELECTED_8 = 93;
    
    /** The <code>File</code> icon. */
    public static final int	FILE = 94;
    
    /** The <code>Browser</code> icon. */
    public static final int	BROWSER = 95;
    
    /** ID of the <code>LeftArrowDisabled</code> icon for the slider. */
    public static final int DOWN_ARROW_BLACK = 96;

    /** ID of the <code>RightArrowDisabled</code> icon for the slider. */
    public static final int RIGHT_ARROW_BLACK = 97;
    
    /** ID of the <code>Forward</code> icon. */
    public static final int FORWARD = 98;
    
    /** ID of the <code>Backward</code> icon. */
    public static final int BACKWARD = 99;
    
    /** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing. 
     */
    private static int      MAX_ID = 99;
    
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
        relPaths[RIGHT_ARROW_BLACK] = "nuvola_player_play10_right_black.png";
        relPaths[DOWN_ARROW_BLACK] = "nuvola_player_play10_down_black.png";
        relPaths[QUESTION_ICON] = "nuvola_filetypes48.png";
    	relPaths[ANNOTATION_48] = "nuvola_kwrite48.png";
    	relPaths[INFO] = "nuvola_messagebox_info16.png";
    	relPaths[PROGRESS] = "eclipse_progress_none16";
    	relPaths[CLASSIFICATION_48] = "nuvola_filenew48.png";
    	relPaths[ERROR_ICON_64] = "nuvola_error64.png";
    	relPaths[COMMENT_ICON_64] = "nuvola_mail_send64.png";
    	relPaths[SERVER] = "nuvola_server22.png";
    	relPaths[CONFIG] = "config.png";
    	relPaths[CONFIG_PRESSED] = "config_pressed.png";
    	relPaths[CONFIG_48] = "nuvola_configure48.png";
    	relPaths[REMOVE] = "remove16.png";//nuvola_edit_remove16.png";
    	relPaths[ADD] = "add16.png";//"nuvola_edit_add16.png";
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
        relPaths[INFORMATION_MESSAGE] = "nuvola_messagebox_info48.png";
        relPaths[QUESTION] = "nuvola_filetypes32.png";
        relPaths[SAVE_48] = "nuvola_filesaveas48.png";
        relPaths[LOAD_48] = "nuvola_revert48.png";
        relPaths[SEARCH_48] = "nuvola_find48.png";
        relPaths[SEARCH] = "eclipse_view_menu16.png";
        relPaths[FILTER_MENU] = "eclipse_view_menu16.png"; 
        relPaths[CLEAR_DISABLED] = "nuvola_fileclose_light16.png";
        relPaths[SEARCH_TAG] = "search_knotes16.png";
        relPaths[SEARCH_IMAGE] = "search_image16.png";
        relPaths[SEARCH_ANNOTATION] = "search_annotation16.png";
        relPaths[SEARCH_DATASET] = "search_folder_image16.png";
        relPaths[SEARCH_PROJECT] = "search_document16.png";
        relPaths[TAG] = "nuvola_knotes16.png";
        relPaths[TAG_GROUP] = "tag_folder16.png";
        relPaths[TAG_BIG] = "nuvola_knotes48.png";
        relPaths[TAG_USED] = "tag_used16.png";
        relPaths[BROWSE] = "zoom16.png";
        relPaths[TRANSPARENT] = "zoom16.png";
        relPaths[OWNER_48] ="nuvola_kdmconfig48.png";
        relPaths[SEARCH_TAG_SET] ="search_tag_folder16.png";
        relPaths[EDIT_REMOVE] = "remove12.png";
        relPaths[ADD_12] = "add12.png";
        relPaths[CLOSE] = "nuvola_fileclose16.png";
        relPaths[CLOSE_OVER] = "filecloseRed16.png";
        relPaths[HELP] = "nuvola_help16.png";
        relPaths[HELP_48] ="nuvola_help48.png";
        relPaths[ANNOTATION] ="nuvola_kwrite16.png";
        relPaths[FILE_ANNOTATION] ="nuvola_attach16.png";
        relPaths[URL_ANNOTATION] ="nuvola_browser16.png";
        relPaths[START_SELECTED] = "nuvola_mozilla16.png";
        relPaths[START_UNSELECTED] = "mozilla_grey16.png";
        relPaths[BROKEN_FILE96] = "nuvola_file_broken96.png";
        relPaths[DOUBLE_LEFT_ARROW] = "nuvola_2leftarrow16.png";
        relPaths[DOUBLE_RIGHT_ARROW] = "nuvola_2rightarrow16.png";
        relPaths[LEFT_ARROW_16] = "nuvola_1leftarrow16.png";
        relPaths[RIGHT_ARROW_16] = "nuvola_1rightarrow16.png";
        relPaths[TAG_OWNER] = "nuvola_1rightarrow16.png";
        relPaths[WIZARD] ="nuvola_wizard48.png";
        relPaths[START_SELECTED_12] = "nuvola_mozilla12.png";
        relPaths[START_UNSELECTED_12] = "mozilla_grey12.png";
        relPaths[START_SELECTED_8] = "nuvola_mozilla8.png";
        relPaths[START_UNSELECTED_8] = "mozilla_grey8.png";
        relPaths[FILE] = "nuvola_fileexport16.png";
        relPaths[BROWSER] = "nuvola_browser16.png";
        relPaths[FORWARD] = "eclipse_forward_nav16.png";
        relPaths[BACKWARD] = "eclipse_backward_nav16.png";
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
