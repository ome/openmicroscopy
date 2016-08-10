/*
 * org.openmicroscopy.shoola.util.ui.IconManager
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
import java.awt.Image;
import java.net.URL;
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
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME2.2
 */
public class IconManager
{

    /** The pathname, relative to this class, of the login splash screen. */
    private static final String LOGIN_BACKGROUND =
            "graphx/login_background.png";

    /** The pathname, relative to this class, of the login splash screen. */
    private static final String IMAGEJ_SPLASHSCREEN =
            "graphx/omeroImageJSplashscreen.png";

    /** The pathname, relative to this class, of the OME screen. */
    private static final String OME_ICON = "graphx/omero16.png";

    /** ID of the <code>Colour slider</code> icon. */
    public static final int COLOUR_SLIDER_24 = 0;

    /** ID of the <code>Colour watch</code> icon. */
    public static final int COLOUR_SWATCH_24 = 1;

    /** ID of the <code>Colour wheel</code> icon. */
    public static final int COLOUR_WHEEL_24 = 2;

    /** ID of the <code>Cancel</code> icon. */
    public static final int CANCEL_22 = 3;

    /** ID of the <code>OK</code> icon. */
    public static final int OK_22 = 4;

    /** ID of the <code>Undo</code> icon. */
    public static final int UNDO_22 = 5;

    /** ID of the <code>Thumb</code> icon for the slider. */
    public static final int THUMB = 6;

    /** ID of the <code>ThumbDisabled</code> icon for the slider. */
    public static final int THUMB_DISABLED = 7;

    /** ID of the <code>UpArrow</code> icon for the slider. */
    public static final int UP_ARROW_10 = 8;

    /** ID of the <code>DownArrow</code> icon for the slider. */
    public static final int DOWN_ARROW_10 = 9;

    /** ID of the <code>LeftArrow</code> icon for the slider. */
    public static final int LEFT_ARROW_10 = 10;

    /** ID of the <code>RightArrow</code> icon for the slider. */
    public static final int RIGHT_ARROW_10 = 11;

    /** ID of the <code>UpArrowDisabled</code> icon for the slider. */
    public static final int UP_ARROW_DISABLED_10 = 12;

    /** ID of the <code>DownArrowDisabled</code> icon for the slider. */
    public static final int DOWN_ARROW_DISABLED_10 = 13;

    /** ID of the <code>LeftArrowDisabled</code> icon for the slider. */
    public static final int LEFT_ARROW_DISABLED_10 = 14;

    /** ID of the <code>RightArrowDisabled</code> icon for the slider. */
    public static final int RIGHT_ARROW_DISABLED_10 = 15;

    /** ID of the <code>QuestionIcon</code> icon for the slider. */
    public static final int QUESTION_ICON_48 = 16;

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
    public static final int SERVER_22 = 23;

    /** ID of the <code>Remove</code> icon. */
    public static final int REMOVE = 24;

    /** ID of the <code>Config</code> icon. */
    public static final int CONFIG_24 = 25;

    /** ID of the <code>Config pressed</code> icon. */
    public static final int CONFIG_PRESSED_24 = 26;

    /** ID of the <code>Config 48</code> icon. */
    public static final int CONFIG_48= 27;

    /** ID of the <code>Add</code> icon. */
    public static final int ADD = 28;

    /** ID of the <code>Info 32</code> icon. */
    public static final int INFO_32 = 29;

    /** ID of the <code>Download 48</code> icon. */
    public static final int DOWNLOAD_48 = 30;

    /** ID of the <code>Minus</code> icon. */
    public static final int MINUS_9 = 31;

    /** ID of the <code>Minus over</code> icon. */
    public static final int MINUS_OVER_9 = 32;

    /** ID of the <code>Plus</code> icon. */
    public static final int PLUS_9 = 33;

    /** ID of the <code>Plus over</code> icon. */
    public static final int PLUS_OVER_9 = 34;

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
    public static final int SINGLE_VIEW_MODE_9 = 46;

    /** ID of the single-view over icon in the browser's internal frame. */
    public static final int SINGLE_VIEW_MODE_OVER_9 = 47;

    /** ID of the multi-view icon in the browser's internal frame. */
    public static final int MULTI_VIEW_MODE = 48;

    /** ID of the views list icon in the browser's internal frame. */
    public static final int VIEWS_LIST_9 = 49;

    /** ID of the views list over icon in the browser's internal frame. */
    public static final int VIEWS_LIST_OVER_9 = 50;

    /** ID of <code>information message</code> icon. */
    public static final int INFORMATION_MESSAGE_48 = 51;

    /** ID of <code>information message</code> icon. */
    public static final int QUESTION_32 = 52;

    /** ID of <code>Save 48</code> icon. */
    public static final int SAVE_48 = 53;

    /** ID of <code>Load 48</code> icon. */
    public static final int LOAD_48 = 54;

    /** ID of <code>Search 48</code> icon. */
    public static final int SEARCH_48 = 55;

    /** ID of <code>Search</code> icon. */
    public static final int SEARCH = 56;

    /** The <code>Filter menu</code> icon. */
    public static final int FILTER_MENU = 57;

    /** The <code>Clear disabled</code> icon. */
    public static final int CLEAR_DISABLED = 58;

    /** The <code>Search Tag</code> icon. */
    public static final int SEARCH_TAG = 59;

    /** The <code>Search Image</code> icon. */
    public static final int SEARCH_IMAGE = 60;

    /** The <code>Search Annotation</code> icon. */
    public static final int SEARCH_ANNOTATION = 61;

    /** The <code>Search Dataset</code> icon. */
    public static final int SEARCH_DATASET = 62;

    /** The <code>Search Project</code> icon. */
    public static final int SEARCH_PROJECT = 63;

    /** The <code>Tag</code> icon. */
    public static final int TAG = 64;

    /** The <code>Tag Set</code> icon. */
    public static final int TAG_SET = 65;

    /** The <code>Tag big</code> icon. */
    public static final int TAG_48 = 66;

    /** The <code>Tag used</code> icon. */
    public static final int TAG_USED = 67;

    /** The <code>Tag used</code> icon. */
    public static final int BROWSE = 68;

    /** The <code>Transparent</code> icon. */
    public static final int TRANSPARENT = 69;

    /** The <code>Owner 48</code> icon. */
    public static final int OWNER_48 = 70;

    /** The <code>Owner 48</code> icon. */
    public static final int SEARCH_TAG_SET = 71;

    /** The <code>Edit remove</code> icon. */
    public static final int EDIT_REMOVE_12 = 72;

    /** The <code>Add 12</code> icon. */
    public static final int ADD_12 = 73;

    /** The <code>Close 8</code> icon. */
    public static final int CLOSE = 74;

    /** The <code>Close over 8</code> icon. */
    public static final int CLOSE_OVER = 75;

    /** The <code>Help</code> icon. */
    public static final int HELP = 76;

    /** The <code>Help</code> icon. */
    public static final int HELP_48 = 77;

    /** The <code>Annotation</code> icon. */
    public static final int ANNOTATION = 78;

    /** The <code>File Annotation</code> icon. */
    public static final int FILE_ANNOTATION = 79;

    /** The <code>URL Annotation</code> icon. */
    public static final int URL_ANNOTATION = 80;

    /** The <code>Start selected</code> icon. */
    public static final int START_SELECTED = 81;

    /** The <code>Start unselected</code> icon. */
    public static final int START_UNSELECTED = 82;

    /** The <code>Start unselected</code> icon. */
    public static final int BROKEN_FILE_96 = 83;

    /** ID of the <code>RightArrow</code> icon for the slider. */
    public static final int DOUBLE_RIGHT_ARROW = 84;

    /** ID of the <code>Double</code> icon for the slider. */
    public static final int DOUBLE_LEFT_ARROW = 85;

    /** ID of the <code>RightArrow</code> icon for the slider. */
    public static final int RIGHT_ARROW = 86;

    /** ID of the <code>Double</code> icon for the slider. */
    public static final int LEFT_ARROW = 87;

    /** The <code>Tag</code> icon. */
    public static final int TAG_OTHER_OWNER = 88;

    /** The <code>Wizard</code> icon. */
    public static final int WIZARD_48 = 89;

    /** The <code>Start selected</code> icon. */
    public static final int START_SELECTED_12 = 90;

    /** The <code>Start unselected</code> icon. */
    public static final int START_UNSELECTED_12 = 91;

    /** The <code>Start selected</code> icon. */
    public static final int START_SELECTED_8 = 92;

    /** The <code>Start unselected</code> icon. */
    public static final int START_UNSELECTED_8 = 93;

    /** The <code>File</code> icon. */
    public static final int FILE = 94;

    /** The <code>Browser</code> icon. */
    public static final int BROWSER = 95;

    /** ID of the <code>LeftArrowDisabled</code> icon for the slider. */
    public static final int DOWN_ARROW_BLACK_10 = 96;

    /** ID of the <code>RightArrowDisabled</code> icon for the slider. */
    public static final int RIGHT_ARROW_BLACK_10 = 97;

    /** ID of the <code>Forward</code> icon. */
    public static final int FORWARD = 98;

    /** ID of the <code>Backward</code> icon. */
    public static final int BACKWARD = 99;

    /** ID of the <code>Backward</code> icon. */
    public static final int ACQUISITION_48 = 100;

    /** The <code>PDF File</code> icon. */
    public static final int FILE_PDF = 101;

    /** The <code>Text File</code> icon. */
    public static final int FILE_TEXT = 102;

    /** The <code>Editor File</code> icon. */
    public static final int FILE_EDITOR = 103;

    /** The <code>Word File</code> icon. */
    public static final int FILE_WORD = 104;

    /** The <code>Excel File</code> icon. */
    public static final int FILE_EXCEL = 105;

    /** The <code>PPT file</code> icon. */
    public static final int FILE_PPT = 106;

    /** The <code>XML File</code> icon. */
    public static final int FILE_XML = 107;

    /** The <code>HTML File</code> icon. */
    public static final int FILE_HTML = 108;

    /** The <code>Hyperlink</code> icon. */
    public static final int HYPERLINK = 109;

    /** The <code>Hyperlink</code> icon. */
    public static final int UP_DOWN_9_12 = 110;

    /** A 48x48 version of the <code>Project</code> icon. */
    public static final int PROJECT_48 = 111;

    /** A 48x48 version of the <code>Dataset</code> icon. */
    public static final int DATASET_48 = 112;

    /** A 48x48 version of the <code>Tag Set</code> icon. */
    public static final int TAG_SET_48 = 113;

    /** A 48x48 version of the <code>Screen</code> icon. */
    public static final int SCREEN_48 = 114;

    /** A 48x48 version of the <code>Create</code> icon. */
    public static final int CREATE_48 = 115;

    /** A 16x16 version of the <code>Edit</code> icon. */
    public static final int EDIT = 116;

    /** A 16x16 version of the <code>Experiment</code> icon. */
    public static final int EDITOR_EXPERIMENT = 117;

    /** A 16x16 version of the <code>Collapse</code> icon. */
    public static final int COLLAPSE = 118;

    /** A 16x16 version of the <code>sort alphabetically</code> icon. */
    public static final int SORT_ALPHABETICALLY = 119;

    /** A 16x16 version of the <code>sort by date</code> icon. */
    public static final int SORT_BY_DATE = 120;

    /** A 16x16 version of the <code>words</code> icon. */
    public static final int MESSED_WORDS = 121;

    /** A 16x16 version of the <code>project</code> icon. */
    public static final int PROJECT = 122;

    /** A 16x16 version of the <code>quit</code> icon. */
    public static final int QUIT = 123;

    /** A 16x16 version of the <code>refresh</code> icon. */
    public static final int REFRESH = 124;

    /** A 16x16 version of the <code>empty trash can</code> icon. */
    public static final int TRASH_CAN_EMPTY = 125;

    /** A 22x22 version of the <code>quit</code> icon. */
    public static final int QUIT_22 = 126;

    /** A 22x22 version of the <code>refresh</code> icon. */
    public static final int REFRESH_22 = 127;

    /** A 22x22 version of the <code>image</code> icon. */
    public static final int IMAGE_22 = 128;

    /** A 48x48 version of the <code>image</code> icon. */
    public static final int MOVIE_48 = 129;

    /** The <code>Color Picker</code> icon. */
    public static final int COLOR_PICKER = 130;

    /** A 48x48 version of the <code>Import</code> icon. */
    public static final int IMPORT_48 = 131;

    /** A 64x64 version of the <code>Submit</code> icon. */
    public static final int SUBMIT_ICON_64 = 132;

    /** ID of the <code>Actual Size</code> icon. */
    public static final int ACTUAL_SIZE = 133;

    /** ID of the <code>Zoom In</code> icon. */
    public static final int ZOOM_IN = 134;

    /** ID of the <code>Zoom Out</code> icon. */
    public static final int ZOOM_OUT = 135;

    /** The <code>Tag</code> icon. */
    public static final int	TAG_SET_OTHER_OWNER = 136;

    /** ID of the <code>Group</code> icon. */
    public static final int GROUP = 137;

    /** ID of the <code>encrypted</code> icon. */
    public static final int ENCRYPTED_24 = 138;

    /** ID of the <code>decrypted</code> icon. */
    public static final int DECRYPTED_24 = 139;

    /** ID of the 96x96 <code>Personal</code> icon. */
    public static final int PERSONAL_96 = 140;

    /** The <code>Private Group</code> icon. */
    public static final int PRIVATE_GROUP = 141;

    /** The <code>Read Group</code> icon. */
    public static final int READ_GROUP = 142;

    /** The <code>Read Link Group</code> icon. */
    public static final int READ_LINK_GROUP = 143;

    /** The <code>Public Group</code> icon. */
    public static final int PUBLIC_GROUP = 144;

    /** The 12x12 <code>Private Group Drop Down</code> icon. */
    public static final int PRIVATE_GROUP_DD_12 = 145;

    /** The 12x12 <code>Read Group Drop Down</code> icon. */
    public static final int  READ_GROUP_DD_12 = 146;

    /** The 12x12 <code>Read Link Group Drop Down</code> icon. */
    public static final int READ_LINK_GROUP_DD_12 = 147;

    /** The 12x12 <code>Public Group Drop Down</code> icon. */
    public static final int PUBLIC_GROUP_DD_12 = 148;

    /** The <code>Ontology</code> icon. */
    public static final int ONTOLOGY = 149;

    /** The 32x32 <code>Error</code> icon. */
    public static final int ERROR_32 = 150;

    /** The 48x48 <code>Upload Script</code> icon. */
    public static final int UPLOAD_SCRIPT_48 = 151;

    /** The <code>Upload Script</code> icon. */
    public static final int UPLOAD_SCRIPT = 152;

    /** The <code>No entry</code> icon. */
    public static final int NO_ENTRY = 153;

    /** The <code>Read Link Group</code> icon. */
    public static final int READ_WRITE_GROUP = 154;

    /** The <code>Read Link Group</code> icon. */
    public static final int NOT_OWNED_8 = 155;

    /** The <code>Selected</code> icon. */
    public static final int SELECTED = 156;

    /** The <code>Not Selected</code> icon. */
    public static final int NOT_SELECTED = 157;

    /** The <code>Public Group</code> icon. */
    public static final int PUBLIC_GROUP_READ_WRITE = 158;
    
    /** The <code>Folder</code> icon. */
    public static final int ROI_FOLDER = 159;
    
    /** Reference to the <code>Folder owned by other user</code> icon. */
    public static final int ROI_FOLDER_NOT_OWNER = 160;
    
    /** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing.
     */
    private static int MAX_ID = 160;

    /** Paths of the icon files. */
    private static String[] relPaths = new String[MAX_ID+1];
    static {
        relPaths[COLOUR_SLIDER_24] = "coloursliders24.png";
        relPaths[COLOUR_SWATCH_24] = "colourswatch24.png";
        relPaths[COLOUR_WHEEL_24] = "colourwheel24.png";
        relPaths[CANCEL_22] = "nuvola_cancel22.png";
        relPaths[OK_22] = "nuvola_button_accept22.png";
        relPaths[UNDO_22] = "nuvola_undo22.png";
        relPaths[THUMB] = "sliderthumb.png";
        relPaths[THUMB_DISABLED] = "sliderthumb_disabled.png";
        relPaths[UP_ARROW_10] = "nuvola_player_play10_up.png";
        relPaths[DOWN_ARROW_10] = "nuvola_player_play10_down.png";
        relPaths[LEFT_ARROW_10] = "nuvola_player_play10_left.png"; 
        relPaths[RIGHT_ARROW_10] = "nuvola_player_play10_right.png";
        relPaths[UP_ARROW_DISABLED_10] = "nuvola_player_play10_up_disabled.png";
        relPaths[DOWN_ARROW_DISABLED_10] =
                "nuvola_player_play10_down_disabled.png";
        relPaths[LEFT_ARROW_DISABLED_10] =
                "nuvola_player_play10_left_disabled.png";
        relPaths[RIGHT_ARROW_DISABLED_10] =
                "nuvola_player_play10_right_disabled.png";
        relPaths[RIGHT_ARROW_BLACK_10] = "nuvola_player_play10_right_black.png";
        relPaths[DOWN_ARROW_BLACK_10] = "nuvola_player_play10_down_black.png";
        relPaths[QUESTION_ICON_48] = "nuvola_filetypes48.png";
        relPaths[ANNOTATION_48] = "nuvola_kwrite48.png";
        relPaths[INFO] = "nuvola_messagebox_info16.png";
        relPaths[PROGRESS] = "eclipse_progress_none16";
        relPaths[CLASSIFICATION_48] = "nuvola_filenew48.png";
        relPaths[ERROR_ICON_64] = "nuvola_important64.png";
        relPaths[COMMENT_ICON_64] = "nuvola_mail_send64.png";
        relPaths[SERVER_22] = "nuvola_server22.png";
        relPaths[CONFIG_24] = "nuvola_configure24.png";
        relPaths[CONFIG_PRESSED_24] = "config_pressed22.png";
        relPaths[CONFIG_48] = "nuvola_configure48.png";
        relPaths[REMOVE] = "remove16.png";
        relPaths[ADD] = "add16.png";
        relPaths[INFO_32] = "nuvola_messagebox_info32.png";
        relPaths[DOWNLOAD_48] = "nuvola_download_manager48.png";
        relPaths[MINUS_9] = "minus.png";
        relPaths[MINUS_OVER_9] = "minus_over.png";
        relPaths[PLUS_9] = "plus.png";
        relPaths[PLUS_OVER_9] = "plus_over.png";
        relPaths[CROSS] = "cross.png";
        relPaths[CROSS_OVER] = "cross_over.png";
        relPaths[ERROR] = "eclipse_error_tsk16.png";
        relPaths[HISTORY] = "eclipse_history_list16.png";
        relPaths[BACKWARD_NAV] = "eclipse_backward_nav16.png";
        relPaths[TRASH_CAN] = "nuvola_trashcan_full16.png";
        relPaths[SAVE] = "nuvola_filesaveas16.png";
        relPaths[OWNER] = "nuvola_kdmconfig_modified16.png";
        relPaths[CALENDAR] = "nuvola_date16.png";
        relPaths[DATASET] = "nuvola_folder_image16.png";
        relPaths[IMAGE] = "nuvola_image16.png";
        relPaths[SINGLE_VIEW_MODE_9] = "sinlge_view_mode.png";
        relPaths[SINGLE_VIEW_MODE_OVER_9] = "sinlge_view_mode_over.png";
        relPaths[MULTI_VIEW_MODE] = "nuvola_view_multicolumn16.png";
        relPaths[VIEWS_LIST_9] = "frame_list.png";
        relPaths[VIEWS_LIST_OVER_9] = "frame_list_over.png";
        relPaths[INFORMATION_MESSAGE_48] = "nuvola_messagebox_info48.png";
        relPaths[QUESTION_32] = "nuvola_filetypes32.png";
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
        relPaths[TAG_SET] = "nuvola_knotesRedRed16.png";
        relPaths[TAG_48] = "nuvola_knotes48.png";
        relPaths[TAG_USED] = "tag_used16.png";
        relPaths[BROWSE] = "zoom16.png";
        relPaths[TRANSPARENT] = "eclipse_transparent16.png";
        relPaths[OWNER_48] = "nuvola_kdmconfig_modified48.png";
        relPaths[SEARCH_TAG_SET] ="search_tag_folder16.png";
        relPaths[EDIT_REMOVE_12] = "remove12.png";
        relPaths[ADD_12] = "add12.png";
        relPaths[CLOSE] = "nuvola_fileclose16.png";
        relPaths[CLOSE_OVER] = "filecloseRed16.png";
        relPaths[HELP] = "nuvola_help16.png";
        relPaths[HELP_48] = "nuvola_help48.png";
        relPaths[ANNOTATION] = "nuvola_kwrite16.png";
        relPaths[FILE_ANNOTATION] = "nuvola_attach16.png";
        relPaths[URL_ANNOTATION] = "nuvola_browser16.png";
        relPaths[START_SELECTED] = "nuvola_mozilla16.png";
        relPaths[START_UNSELECTED] = "mozilla_grey16.png";
        relPaths[BROKEN_FILE_96] = "nuvola_image_grey96.png";
        relPaths[DOUBLE_LEFT_ARROW] = "nuvola_2leftarrow16.png";
        relPaths[DOUBLE_RIGHT_ARROW] = "nuvola_2rightarrow16.png";
        relPaths[LEFT_ARROW] = "nuvola_1leftarrow16.png";
        relPaths[RIGHT_ARROW] = "nuvola_1rightarrow16.png";
        relPaths[TAG_OTHER_OWNER] = "nuvola_knotes_group16.png";
        relPaths[WIZARD_48] ="nuvola_wizard48.png";
        relPaths[START_SELECTED_12] = "nuvola_mozilla12.png";
        relPaths[START_UNSELECTED_12] = "mozilla_grey12.png";
        relPaths[START_SELECTED_8] = "nuvola_mozilla8.png";
        relPaths[START_UNSELECTED_8] = "mozilla_grey8.png";
        relPaths[FILE] = "nuvola_attach16.png";
        relPaths[BROWSER] = "nuvola_browser16.png";
        relPaths[FORWARD] = "eclipse_forward_nav16.png";
        relPaths[BACKWARD] = "eclipse_backward_nav16.png";
        relPaths[ACQUISITION_48] = "nuvola_help48.png";
        relPaths[FILE_PDF] = "nuvola_acroread16.png";
        relPaths[FILE_TEXT] = "nuvola_txt16.png";
        relPaths[FILE_WORD] = "alienOSX_Microsoft_Word16.png";
        relPaths[FILE_PPT] = "alienOSX_Microsoft_PowerPoint16.png";
        relPaths[FILE_EXCEL] = "alienOSX_Microsoft_Excel16.png";
        relPaths[FILE_XML] = "txt_xml16.png";
        relPaths[FILE_HTML] = "txt_html16.png";
        relPaths[FILE_EDITOR] = "omeroEditor16.png";
        relPaths[HYPERLINK] = "nuvola_browser16.png";
        relPaths[UP_DOWN_9_12] = "upDown9_12.png";
        relPaths[TAG_SET_48] = "nuvola_knotes_red48.png";
        relPaths[PROJECT_48] = "nuvola_folder_blue_open48.png";
        relPaths[DATASET_48] = "nuvola_folder_image48.png";
        relPaths[SCREEN_48] = "nuvola_folder_blue_open_modified_screen48.png";
        relPaths[CREATE_48] = "nuvola_filenew48.png";
        relPaths[EDIT] = "nuvola_ksig16.png";
        relPaths[EDITOR_EXPERIMENT] = "expNew.png";
        relPaths[COLLAPSE] = "eclipse_collapseall16.png";
        relPaths[SORT_ALPHABETICALLY] = "eclipse_alphab_sort_co16.png";
        relPaths[SORT_BY_DATE] = "eclipse_trace_persp16.png";
        relPaths[MESSED_WORDS] = "nuvola_kmessedwords16.png";
        relPaths[PROJECT] = "nuvola_folder_darkblue_open16.png";
        relPaths[QUIT] = "nuvola_exit16.png";
        relPaths[REFRESH] = "nuvola_reload16.png";
        relPaths[TRASH_CAN_EMPTY] = "nuvola_reload16.png";
        relPaths[QUIT_22] = "nuvola_exit22.png";
        relPaths[REFRESH_22] = "nuvola_reload22.png";
        relPaths[IMAGE_22] = "nuvola_indeximg22.png";
        relPaths[MOVIE_48] = "crystal_video48.png";
        relPaths[COLOR_PICKER] = "nuvola_colorpicker16.png";
        relPaths[IMPORT_48] = "omeroImporter48.png";
        relPaths[SUBMIT_ICON_64] = "nuvola_important64.png";
        relPaths[ACTUAL_SIZE] = "nuvola_viewmagfit16.png";
        relPaths[ZOOM_IN] = "nuvola_viewmag+16.png";
        relPaths[ZOOM_OUT] = "nuvola_viewmag-16.png";
        relPaths[TAG_SET_OTHER_OWNER] = "nuvola_knotesRed_group16.png";
        relPaths[GROUP] = "group_generic16.png";
        relPaths[ENCRYPTED_24] = "nuvola_encrypted_grey24.png";
        relPaths[DECRYPTED_24] = "nuvola_decrypted_grey24.png";
        relPaths[PERSONAL_96] ="nuvola_personal96.png";
        relPaths[PRIVATE_GROUP] = "private16.png";
        relPaths[READ_GROUP] = "group_read16.png";
        relPaths[READ_LINK_GROUP] = "group_read_annotate16.png";
        relPaths[PUBLIC_GROUP] = "public_read16.png";
        relPaths[READ_WRITE_GROUP] = "group_read_write16.png";
        relPaths[PRIVATE_GROUP_DD_12] = "nuvola_permission_private_dd12.png";
        relPaths[READ_GROUP_DD_12] = "nuvola_permission_readOnly_dd12.png";
        relPaths[READ_LINK_GROUP_DD_12] = "nuvola_permission_read_dd12.png";
        relPaths[PUBLIC_GROUP_DD_12] = "nuvola_permission_public_dd12.png";
        relPaths[ONTOLOGY] = "nuvola_ledorange_readOnly16.png";
        relPaths[ERROR_32] = "nuvola_no32.png";
        relPaths[UPLOAD_SCRIPT_48] = "nuvola_script_add48.png";
        relPaths[UPLOAD_SCRIPT] = "nuvola_script_add16.png";
        relPaths[NO_ENTRY] = "crystal_agt_action_fail16.png";
        relPaths[NOT_OWNED_8] = "red_dot8.png";
        relPaths[SELECTED] = "tick_check16.png";
        relPaths[NOT_SELECTED] = "empty_check16.png";
        relPaths[PUBLIC_GROUP_READ_WRITE] = "public_read16.png";
        relPaths[ROI_FOLDER] = "roi_folder_icon.png";
        relPaths[ROI_FOLDER_NOT_OWNER] = "roi_folder_user_icon.png";
    }

    /**
     * Returns the image of the login button within the splash screen.
     * 
     * @return See above.
     */
    public static Icon getLoginBackground()
    {
        return createIcon(LOGIN_BACKGROUND);
    }

    /**
     * Returns the splash screen for ImageJ.
     * 
     * @return See above.
     */
    public static Icon getImageJSplashscreen()
    {
        return createIcon(IMAGEJ_SPLASHSCREEN);
    }

    /**
     * Returns the <i>OME</i> logo to be used for title-bars.
     * 
     * @return See above.
     */
    public static Image getOMEImageIcon()
    {
        //This type cast is OK, see implementation of createIcon.
        return ((ImageIcon) getOMEIcon()).getImage();
    }

    /**
     * Returns the <i>OME</i> logo.
     * 
     * @return See above.
     */
    public static Icon getOMEIcon() { return createIcon(OME_ICON); }

    /** 
     * Retrieves the icon specified by <code>id</code>.
     * If the icon can't be retrieved, then this method will log the error and
     * return <code>null</code>.
     *
     * @param id The index of the file name in the array of file names 
     *           specified to this class' constructor.
     * @return An {@link Icon} object created from the image file.  The return
     *         value will be <code>null</code> if the file couldn't be found
     *         or an image icon couldn't be created from that file.
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
     * @param name Must be one a valid icon file name within the directory
     *             used by the {@link IconFactory} instance specified via
     *             this class' constructor.
     * @return An {@link Icon} object created from the image file.  The return
     *         value will be <code>null</code> if the file couldn't be found
     *         or an image icon couldn't be created from that file.
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
     * @param id The index of the file name in the array of file names 
     *           specified to this class' constructor.
     * @return An {@link Icon} object created from the image file.  The return
     *         value will be <code>null</code> if the file couldn't be found
     *         or an image icon couldn't be created from that file.
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
     * @param name Must be one a valid icon file name within the directory
     *             used by the {@link IconFactory} instance specified via
     *            this class' constructor.
     * @return An {@link Icon} object created from the image file. The return
     *         value will be <code>null</code> if the file couldn't be found
     *         or an image icon couldn't be created from that file.
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
    private static IconManager singleton;

    /** The factory. */
    private IconFactory factory;

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

    /** 
     * Utility factory method to create an icon from a file.
     *
     * @param path    The path of the icon file relative to this class.
     * @return  An instance of {@link javax.swing.Icon Icon} or
     * 			<code>null</code> if the path was invalid.
     */
    private static Icon createIcon(String path)
    {
        URL location = IconManager.class.getResource(path);
        ImageIcon icon = null;
        if (location != null)
            icon = new ImageIcon(location);
        return icon;
    }

}
