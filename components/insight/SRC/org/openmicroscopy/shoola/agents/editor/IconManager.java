/*
 * org.openmicroscopy.shoola.agents.editor.IconManager 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor;



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.AbstractIconManager;

/** 
 * Provides the icons used by the ImViewer.
 * <p>The icons are retrieved by first calling the 
 * {@link #getInstance() getInstance} method and then the 
 * {@link #getIcon(int) getIcon} method passing one of the icon ID's specified
 * by the static constants within this class &#151; icons will be retrieved
 * from the ImViewer's graphics bundle, which implies that its
 * configuration has been read in (this happens during the initialization
 * procedure).</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
public class IconManager     
	extends AbstractIconManager
{

    /** The <code>Status Info</code> icon. */
    public  static final int          STATUS_INFO = 0;
    
    /** The <code>Create</code> icon. */
    public  static final int          CREATE = 1;
    
    /** The <code>Editor</code> icon. */
    public  static final int          EDITOR = 2;

    /** The <code>Text-Line Field</code> icon. */
    public  static final int			TEXT_LINE_ICON = 3;
    
    /** The <code>Add Text-Line Parameter</code> icon. */
    public  static final int			ADD_TEXT_LINE_ICON = 4;
    
    /** The <code>Text Box Field</code> icon. */
    public  static final int			TEXT_BOX_ICON = 5;
    
    /** The <code>Timer Field</code> icon. */
    public  static final int			TIMER_ICON = 6;
   
    /** The <code>Table Field</code> icon. */
    public  static final int			TABLE_ICON = 7;
    
    /** The <code>Image Link Field</code> icon. */
    public  static final int			IMAGE_ICON = 8;
    
    /** The <code>CheckBox Field</code> icon. */
    public  static final int			CHECK_BOX = 9;
    
    /** The <code>Add CheckBox Parameter</code> icon. */
    public  static final int			ADD_CHECK_BOX = 10;
    
    /** The <code>Drop-down Field</code> icon. */
    public  static final int			DROP_DOWN = 11;
    
    /** The <code>Number Field</code> icon. */
    public  static final int			NUMBER = 12;
    
    /** The <code>Add Number Parameter</code> icon. */
    public  static final int			ADD_NUMBER = 13;
    
    /** The <code>Calendar</code> icon. */
    public  static final int			CALENDAR_ICON = 14;
    
    /** The <code>URL (www)</code> icon. */
    public  static final int			WWW_ICON = 15;
    
    /** The <code>Open Image</code> icon. */
    public  static final int			OPEN_IMAGE_ICON = 16;
    
    /** The <code>Zoom Image</code> icon. */
    public  static final int			ZOOM_ICON = 17;
    
    /** The <code>No Image</code> icon. */
    public  static final int			NO_IMAGE_ICON_32 = 18;
    
    /** The <code>Link to local file</code> icon. */
    public  static final int			LINK_LOCAL_ICON = 19;
    
    /** The <code>Relative link to local file</code> icon. */
    public  static final int			LINK_RELATIVE_ICON = 20;
    
    /** The <code>Link to Editor file</code> icon. */
    public  static final int			LINK_SCIENCE_ICON = 21;
    
    /** The <code>Relative link to Editor file</code> icon. */
    public  static final int			LINK_SCIENCE_RELATIVE_ICON = 22;
    
    /** The <code>Close file</code> icon. */
    public  static final int			FILE_CLOSE_ICON = 23;
    
    /** The <code>Wrench / configure</code> icon. */
    public  static final int			WRENCH_ICON = 24;
    
    /** The <code>New table row</code> icon. */
    public  static final int			NEW_ROW_ICON = 25;
    
    /** The <code>Clear table row</code> icon. */
    public  static final int			CLEAR_ROW_ICON = 26;
    
    /** The <code>Start Timer</code> icon. */
    public  static final int			TIMER_START_ICON = 27;
    
    /** The <code>Stop Timer</code> icon. */
    public  static final int			TIMER_STOP_ICON = 28;

    /** The <code>Image Border top left</code> icon. */
    public  static final int			BORDER_TOP_LEFT = 35;
    
    /** The <code>Image Border top</code> icon. */
    public  static final int			BORDER_TOP = 29;
    
    /** The <code>Image Border top right</code> icon. */
    public  static final int			BORDER_TOP_RIGHT = 30;
    
    /** The <code>Image Border left</code> icon. */
    public  static final int			BORDER_LEFT = 31;
    
    /** The <code>Image Border right</code> icon. */
    public  static final int			BORDER_RIGHT= 32;
    
    /** The <code>Image Border bottom left</code> icon. */
    public  static final int			BORDER_BOTTOM_LEFT = 33;
    
    /** The <code>Image Border bottom</code> icon. */
    public  static final int			BORDER_BOTTOM = 34;
    
    /** The <code>Image Border bottom right</code> icon. */
    public  static final int			BORDER_BOTTOM_RIGHT = 36;
    
    /** The <code>Image Border top left</code> icon. */
    public  static final int			BORDER_TOP_LEFT_HLT = 37;
    
    /** The <code>Image Border top</code> icon. */
    public  static final int			BORDER_TOP_HLT = 38;
    
    /** The <code>Image Border top right</code> icon. */
    public  static final int			BORDER_TOP_RIGHT_HLT = 39;
    
    /** The <code>Image Border left</code> icon. */
    public  static final int			BORDER_LEFT_HLT = 40;
    
    /** The <code>Image Border right</code> icon. */
    public  static final int			BORDER_RIGHT_HLT = 41;
    
    /** The <code>Image Border bottom left</code> icon. */
    public  static final int			BORDER_BOTTOM_LEFT_HLT = 42;
    
    /** The <code>Image Border bottom</code> icon. */
    public  static final int			BORDER_BOTTOM_HLT = 43;
    
    /** The <code>Image Border bottom right</code> icon. */
    public  static final int			BORDER_BOTTOM_RIGHT_HLT = 44;

    /** The <code>Info</code> icon. */
    public  static final int			INFO_ICON = 45;
    
    /** The <code>No</code> icon. */
    public  static final int			N0 = 46;
    
    /** The <code>Undo</code> icon. */
    public  static final int			UNDO_ICON = 47;
    
    /** The <code>Redo</code> icon. */
    public  static final int			REDO_ICON = 48;
    
    /** The <code>Add Field</code> icon. */
    public  static final int			ADD_ICON = 49;
    
    /** The <code>Delete Field</code> icon. */
    public  static final int			DELETE_ICON = 50;
    
    /** The <code>Configure</code> icon. */
    public  static final int			CONFIGURE_ICON = 51;
    
    /** The <code>Open Folder</code> icon. */
    public  static final int			OPEN_FOLDER = 52;

    /** The <code>Open Folder</code> icon. */
    public  static final int			INDENT_RIGHT = 53;
    
    /** The <code>Open Folder</code> icon. */
    public  static final int			INDENT_LEFT = 54;
    
    /** The <code>Template locked</code> icon. */
    public  static final int			TEMPLATE_LOCK = 55;
    
    /** The <code>Fully locked</code> icon. */
    public  static final int			FULL_LOCK = 56;
    
    /** The <code>OMERO.editor 16</code> icon. */
    public  static final int			OMERO_EDITOR = 57;
    
    /** The <code>Save</code> icon. */
    public  static final int			SAVE_ICON = 58;
    
    /** The <code>Up-Down 9x12</code> icon. */
    public  static final int			UP_DOWN_9_12 = 59;
    
    /** The <code>Spacer-16</code> icon. */
    public  static final int			SPACER = 60;
    
    /** The <code>New Blank File</code> icon. */
    public  static final int			NEW_FILE_ICON = 61;
    
    /** The <code>Edit XML</code> icon. */
    public  static final int			EDIT_XML_ICON = 62;
    
    /** The <code>Up</code> icon. */
    public  static final int			UP_ICON = 63;
    
    /** The <code>Down</code> icon. */
    public  static final int			DOWN_ICON = 64;
    
    /** The <code>Ontology Parameter</code> icon. */
    public  static final int			ONTOLOGY_ICON = 65;
    
    /** The <code>Add Table</code> icon. */
    public  static final int			ADD_TABLE_ICON = 66;
    
    /** The <code>Copy</code> icon. */
    public  static final int			COPY_ICON = 67;
    
    /** The <code>Paste</code> icon. */
    public  static final int			PASTE_ICON = 68;
    
    /** The <code>Save-As</code> icon. */
    public  static final int			SAVE_AS_ICON = 69;
    
    /** The <code>Remove table</code> icon. */
    public  static final int			REMOVE_TABLE_ICON = 70;
    
    /** The <code>WWW-Folder</code> icon. */
    public  static final int			WWW_FOLDER_ICON = 71;
    
    /** The <code>WWW-Folder-48</code> icon. */
    public  static final int			WWW_FOLDER_ICON_48 = 72;
    
    /** The <code>Save to Server</code> icon. */
    public  static final int			SAVE_SERVER_ICON = 73;
    
    /** The <code>Red Asterisk</code> icon. */
    public  static final int			RED_ASTERISK_ICON_11 = 74;
    
    /** The <code>Info</code> icon, 12 pixels square. */
    public  static final int			INFO_ICON_12 = 75;
    
    /** The <code>Split</code> icon, 12 pixels square. */
    public  static final int			SPLIT_ICON_12 = 76;
    
    /** The <code>Exp</code> icon, 9x11 pixels square. */
    public  static final int			EXP_ICON_9_11 = 77;
    
    /** The <code>Add-Text-Box</code> icon.*/
    public  static final int			ADD_TEXTBOX_ICON = 78;
    
    /** The <code>New Blank File</code> icon, 32 x 32*/
    public  static final int			NEW_FILE_ICON_32 = 79;
    
    /** The <code>Open WWW file</code> icon, 32 x 32.*/
    public  static final int			WWW_FOLDER_ICON_32 = 80;
    
    /** The <code>Open file</code> icon, 32 x 32.*/
    public  static final int			OPEN_FOLDER_ICON_32=  81;
    
    /** The <code>OMERO.editor</code> icon, 48 x 48.*/
    public  static final int			OMERO_EDITOR_48 = 82;
    
    /** The <code>Grey Asterisk</code> icon, 11 x 11.*/
    public  static final int			GREY_ASTERISK_ICON_11 = 83;
    
    /** The <code>Delete 'X'</code> icon, 12 x 12.*/
    public  static final int			DELETE_ICON_12 = 84;
    
    /** The <code>Arrow Right '>'</code> icon, 12 x 12.*/
    public  static final int			ARROW_RIGHT_ICON_12 = 85;
    
    /** The <code>Arrow Left '<'</code> icon, 12 x 12.*/
    public  static final int			ARROW_LEFT_ICON_12 = 86;
    
    /** The <code>Go</code> icon, 12 x 20.*/
    public  static final int			GO_ICON_12_20 = 87;
    
    /** The <code>New Experiment</code> icon */
    public  static final int			EXP_NEW_ICON = 88;
    
    /** The <code>Add Step Note</code> icon */
    public  static final int			ADD_STEP_NOTE_ICON = 89;
    
    /** The <code>Step Note</code> icon */
    public  static final int			STEP_NOTE_ICON = 90;
    
    /** The <code>Clear Values</code> icon */
    public  static final int			CLEAR_VALUES_ICON = 91;
    
    /** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing. 
     */
    private static final int          MAX_ID = 91;
   
    
    /** Paths of the icon files. */
    private static String[]     relPaths = new String[MAX_ID+1];
    
    static {
        relPaths[STATUS_INFO] = "nuvola_messagebox_info16.png";
        relPaths[CREATE] = "nuvola_messagebox_info16.png";
        relPaths[EDITOR] = "omeroEditorLink16.png";
        
        //Icons for the JTree rendering of text-only outline. 
        relPaths[TEXT_LINE_ICON] = "eclipse_textLine16.png";
        relPaths[ADD_TEXT_LINE_ICON] = "eclipse_addTextLine16.png";
        relPaths[TEXT_BOX_ICON] = "eclipse_textBox16.png";
        relPaths[TIMER_ICON] = "nuvola_history16.png";
        relPaths[TABLE_ICON] = "nuvola_view_top_bottom16.png";
        relPaths[IMAGE_ICON] = "nuvola_thumbnail16.png";
        relPaths[CHECK_BOX] = "nuvola_checkBox16.png";
        relPaths[ADD_CHECK_BOX] = "nuvola_addCheckBox16.png";
        relPaths[DROP_DOWN] = "eclipse_dropDown16.png";
        relPaths[NUMBER] = "number16.png";
        relPaths[ADD_NUMBER] = "nuvola_addNumber16.png";
        relPaths[CALENDAR_ICON] = "nuvola_date16.png";
        relPaths[WWW_ICON] = "nuvola_www16.png";
        
        //Icons for image-border
        relPaths[BORDER_TOP_LEFT] = "BorderImages/topLeft.gif";
        relPaths[BORDER_TOP] = "BorderImages/top.gif";
        relPaths[BORDER_TOP_RIGHT] = "BorderImages/topRight.gif";
        relPaths[BORDER_LEFT] = "BorderImages/left.gif";
        relPaths[BORDER_RIGHT] = "BorderImages/right.gif";
        relPaths[BORDER_BOTTOM_LEFT] = "BorderImages/bottomLeft.gif";
        relPaths[BORDER_BOTTOM] = "BorderImages/bottom.gif";
        relPaths[BORDER_BOTTOM_RIGHT] = "BorderImages/bottomRight.gif";
        
        relPaths[BORDER_TOP_LEFT_HLT] = "BorderImages/topLeftHlt.gif";
        relPaths[BORDER_TOP_HLT] = "BorderImages/topHlt.gif";
        relPaths[BORDER_TOP_RIGHT_HLT] = "BorderImages/topRightHlt.gif";
        relPaths[BORDER_LEFT_HLT] = "BorderImages/leftHlt.gif";
        relPaths[BORDER_RIGHT_HLT] = "BorderImages/rightHlt.gif";
        relPaths[BORDER_BOTTOM_LEFT_HLT] = "BorderImages/bottomLeftHlt.gif";
        relPaths[BORDER_BOTTOM_HLT] = "BorderImages/bottomHlt.gif";
        relPaths[BORDER_BOTTOM_RIGHT_HLT] = "BorderImages/bottomRightHlt.gif";
                 
        relPaths[OPEN_IMAGE_ICON] = "nuvola_folder_image16.png";
        relPaths[ZOOM_ICON] = "zoom16.png";
        relPaths[NO_IMAGE_ICON_32] = "nuvola_file_broken32.png";
        relPaths[LINK_LOCAL_ICON] = "nuvola_link_local16.png";
        relPaths[LINK_RELATIVE_ICON] = "nuvola_link_relative16.png";
        relPaths[LINK_SCIENCE_ICON] = "nuvola_link_science16.png";
        relPaths[LINK_SCIENCE_RELATIVE_ICON] = 
        	"nuvola_link_science_relative16.png";
        relPaths[FILE_CLOSE_ICON] = "nuvola_fileclose16.png";
        relPaths[WRENCH_ICON] = "nuvola_configure16.png";
        relPaths[NEW_ROW_ICON] = "nuvola_view_bottom16.png";
        relPaths[CLEAR_ROW_ICON] = "nuvola_view_clear16.png";
        relPaths[TIMER_START_ICON] = "nuvola_timerStart16.png";
        relPaths[TIMER_STOP_ICON] = "nuvola_timerStop16.png";
        relPaths[INFO_ICON] = "nuvola_messagebox_info16.png";
        relPaths[N0] = "nuvola_no16.png";
        relPaths[UNDO_ICON] = "nuvola_undo16.png";
        relPaths[REDO_ICON] = "nuvola_redo16.png";
        relPaths[ADD_ICON] = "nuvola_edit_add16.png";
        relPaths[DELETE_ICON] = "nuvola_cancel16.png";
        relPaths[CONFIGURE_ICON] = "nuvola_package_utilities16.png";
        relPaths[OPEN_FOLDER] = "nuvola_folder_open16.png";
        relPaths[INDENT_RIGHT] = "nuvola_indent_right16.png";
        relPaths[INDENT_LEFT] = "nuvola_indent_left16.png";
        relPaths[TEMPLATE_LOCK] = "nuvola_encrypted16.png";
        relPaths[FULL_LOCK] = "nuvola_encrypted_red16.png";
        relPaths[OMERO_EDITOR] = "omeroEditorLink16.png";
        relPaths[SAVE_ICON] = "nuvola_filesave16.png";
        relPaths[UP_DOWN_9_12] = "upDown.png";
        relPaths[SPACER] = "spacer16.png";
        relPaths[NEW_FILE_ICON] = "nuvola_filenew16.png";
        relPaths[EDIT_XML_ICON] = "nuvola_kwrite16.png";
        relPaths[UP_ICON] = "nuvola_up16.png";
        relPaths[DOWN_ICON] = "nuvola_down16.png";
        relPaths[ONTOLOGY_ICON] = "nuvola_licq16.png";
        relPaths[ADD_TABLE_ICON] = "nuvola_add_table16.png";
        relPaths[COPY_ICON] = "eclipse_copy_edit16.png";
        relPaths[PASTE_ICON] = "eclipse_paste_edit16.png";
        relPaths[SAVE_AS_ICON] = "nuvola_filesaveas16.png";
        relPaths[REMOVE_TABLE_ICON] = "nuvola_remove_table16.png";
        relPaths[WWW_FOLDER_ICON] = "nuvola_folder_http16.png";
        relPaths[WWW_FOLDER_ICON_48] = "nuvola_folder_html48.png";
        relPaths[SAVE_SERVER_ICON] = "nuvola_save_server16.png";
        relPaths[RED_ASTERISK_ICON_11] = "red_asterisk_11.png";
        relPaths[INFO_ICON_12] = "info_12.png";
        relPaths[SPLIT_ICON_12] = "split_12.png";
        relPaths[EXP_ICON_9_11] = "exp_9_11.png";
        relPaths[ADD_TEXTBOX_ICON] = "add_textBox16.png";
        relPaths[NEW_FILE_ICON_32] = "nuvola_filenew32.png";
        relPaths[WWW_FOLDER_ICON_32] = "nuvola_folder_html32.png";
        relPaths[OPEN_FOLDER_ICON_32] = "nuvola_folder32.png";
        relPaths[OMERO_EDITOR_48] = "omeroEditor48.png";
        relPaths[GREY_ASTERISK_ICON_11] = "asterisk_grey_11.png";
        relPaths[DELETE_ICON_12] = "nuvola_edit_delete_12.png";
        relPaths[ARROW_RIGHT_ICON_12] = "arrowRight12.png";
        relPaths[ARROW_LEFT_ICON_12] = "arrowLeft12.png";
        relPaths[GO_ICON_12_20] = "goGreen_12_20.png";
        relPaths[EXP_NEW_ICON] = "expNew.png";
        relPaths[ADD_STEP_NOTE_ICON] = "addStepNote.png";
        relPaths[STEP_NOTE_ICON] = "stepNote.png";
        relPaths[CLEAR_VALUES_ICON] = "nuvola_news_unsubscribe16.png";
    }
    
    
    /** The sole instance. */
    private static IconManager  singleton;
    
    /**
     * Returns the <code>IconManager</code> object. 
     * 
     * @return See above.
     */
    public static IconManager getInstance() 
    { 
        if (singleton == null) 
            singleton = new IconManager(EditorAgent.getRegistry());
        return singleton; 
    }
    
    /**
     * Creates a new instance and configures the parameters.
     * 
     * @param registry  Reference to the registry.
     */
    private IconManager(Registry registry)
    {
        super(registry, "/resources/icons/Factory", relPaths);
    }
    
}
