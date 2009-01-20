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
    public  static int          STATUS_INFO = 0;
    
    /** The <code>Create</code> icon. */
    public  static int          CREATE = 1;
    
    /** The <code>Create</code> icon. */
    public  static int          EDITOR = 2;
    
    /*
     * Icons for the Browser JTree
     */
    /** The <code>Text-Line Field</code> icon. */
    public  static int			TEXT_LINE_ICON = 3;
    
    /** The <code>Add Text-Line Parameter</code> icon. */
    public  static int			ADD_TEXT_LINE_ICON = 4;
    
    /** The <code>Text Box Field</code> icon. */
    public  static int			TEXT_BOX_ICON = 5;
    
    /** The <code>Timer Field</code> icon. */
    public  static int			TIMER_ICON = 6;
   
    /** The <code>Table Field</code> icon. */
    public  static int			TABLE_ICON = 7;
    
    /** The <code>Image Link Field</code> icon. */
    public  static int			IMAGE_ICON = 8;
    
    /** The <code>CheckBox Field</code> icon. */
    public  static int			CHECK_BOX = 9;
    
    /** The <code>Add CheckBox Parameter</code> icon. */
    public  static int			ADD_CHECK_BOX = 10;
    
    /** The <code>Drop-down Field</code> icon. */
    public  static int			DROP_DOWN = 11;
    
    /** The <code>Number Field</code> icon. */
    public  static int			NUMBER = 12;
    
    /** The <code>Add Number Parameter</code> icon. */
    public  static int			ADD_NUMBER = 13;
    
    /** The <code>Calendar</code> icon. */
    public  static int			CALENDAR_ICON = 14;
    
    /** The <code>URL (www)</code> icon. */
    public  static int			WWW_ICON = 15;
    
    /** The <code>Open Image</code> icon. */
    public  static int			OPEN_IMAGE_ICON = 16;
    
    /** The <code>Zoom Image</code> icon. */
    public  static int			ZOOM_ICON = 17;
    
    /** The <code>No Image</code> icon. */
    public  static int			NO_IMAGE_ICON_32 = 18;
    
    /** The <code>Link to local file</code> icon. */
    public  static int			LINK_LOCAL_ICON = 19;
    
    /** The <code>Relative link to local file</code> icon. */
    public  static int			LINK_RELATIVE_ICON = 20;
    
    /** The <code>Link to Editor file</code> icon. */
    public  static int			LINK_SCIENCE_ICON = 21;
    
    /** The <code>Relative link to Editor file</code> icon. */
    public  static int			LINK_SCIENCE_RELATIVE_ICON = 22;
    
    /** The <code>Close file</code> icon. */
    public  static int			FILE_CLOSE_ICON = 23;
    
    /** The <code>Wrench / configure</code> icon. */
    public  static int			WRENCH_ICON = 24;
    
    /** The <code>New table row</code> icon. */
    public  static int			NEW_ROW_ICON = 25;
    
    /** The <code>Clear table row</code> icon. */
    public  static int			CLEAR_ROW_ICON = 26;
    
    /** The <code>Start Timer</code> icon. */
    public  static int			TIMER_START_ICON = 27;
    
    /** The <code>Stop Timer</code> icon. */
    public  static int			TIMER_STOP_ICON = 28;
    
    /*
     * Image Border Icons. 
     */
    
    /** The <code>Image Border top left</code> icon. */
    public  static int			BORDER_TOP_LEFT = 35;
    
    /** The <code>Image Border top</code> icon. */
    public  static int			BORDER_TOP = 29;
    
    /** The <code>Image Border top right</code> icon. */
    public  static int			BORDER_TOP_RIGHT = 30;
    
    /** The <code>Image Border left</code> icon. */
    public  static int			BORDER_LEFT = 31;
    
    /** The <code>Image Border right</code> icon. */
    public  static int			BORDER_RIGHT= 32;
    
    /** The <code>Image Border bottom left</code> icon. */
    public  static int			BORDER_BOTTOM_LEFT = 33;
    
    /** The <code>Image Border bottom</code> icon. */
    public  static int			BORDER_BOTTOM = 34;
    
    /** The <code>Image Border bottom right</code> icon. */
    public  static int			BORDER_BOTTOM_RIGHT = 36;
    
    /** The <code>Image Border top left</code> icon. */
    public  static int			BORDER_TOP_LEFT_HLT = 37;
    
    /** The <code>Image Border top</code> icon. */
    public  static int			BORDER_TOP_HLT = 38;
    
    /** The <code>Image Border top right</code> icon. */
    public  static int			BORDER_TOP_RIGHT_HLT = 39;
    
    /** The <code>Image Border left</code> icon. */
    public  static int			BORDER_LEFT_HLT = 40;
    
    /** The <code>Image Border right</code> icon. */
    public  static int			BORDER_RIGHT_HLT = 41;
    
    /** The <code>Image Border bottom left</code> icon. */
    public  static int			BORDER_BOTTOM_LEFT_HLT = 42;
    
    /** The <code>Image Border bottom</code> icon. */
    public  static int			BORDER_BOTTOM_HLT = 43;
    
    /** The <code>Image Border bottom right</code> icon. */
    public  static int			BORDER_BOTTOM_RIGHT_HLT = 44;
    
    
    /** The <code>Info</code> icon. */
    public  static int			INFO_ICON = 45;
    
    /** The <code>Close</code> icon. */
    public  static int			N0 = 46;
    
    /** The <code>Undo</code> icon. */
    public  static int			UNDO_ICON = 47;
    
    /** The <code>Redo</code> icon. */
    public  static int			REDO_ICON = 48;
    
    /** The <code>Add Field</code> icon. */
    public  static int			ADD_ICON = 49;
    
    /** The <code>Delete Field</code> icon. */
    public  static int			DELETE_ICON = 50;
    
    /** The <code>Configure</code> icon. */
    public  static int			CONFIGURE_ICON = 51;
    
    /** The <code>Open Folder</code> icon. */
    public  static int			OPEN_FOLDER = 52;

    /** The <code>Open Folder</code> icon. */
    public  static int			INDENT_RIGHT = 53;
    
    /** The <code>Open Folder</code> icon. */
    public  static int			INDENT_LEFT = 54;
    
    /** The <code>Template locked</code> icon. */
    public  static int			TEMPLATE_LOCK = 55;
    
    /** The <code>Fully locked</code> icon. */
    public  static int			FULL_LOCK = 56;
    
    /** The <code>OMERO.editor 16</code> icon. */
    public  static int			OMERO_EDITOR = 57;
    
    /** The <code>Save</code> icon. */
    public  static int			SAVE_ICON = 58;
    
    /** The <code>Up-Down</code> icon. */
    public  static int			UP_DOWN_9_12 = 59;
    
    /** The <code>Spacer-16</code> icon. */
    public  static int			SPACER = 60;
    
    /** The <code>New Blank File</code> icon. */
    public  static int			NEW_FILE_ICON = 61;
    
    /** The <code>Edit XML</code> icon. */
    public  static int			EDIT_XML_ICON = 62;
    
    /** The <code>Up</code> icon. */
    public  static int			UP_ICON = 63;
    
    /** The <code>Down</code> icon. */
    public  static int			DOWN_ICON = 64;
    
    /** The <code>Ontology Parameter</code> icon. */
    public  static int			ONTOLOGY_ICON = 65;
    
    /** The <code>Add Table</code> icon. */
    public  static int			ADD_TABLE_ICON = 66;
    
    /** The <code>Copy</code> icon. */
    public  static int			COPY_ICON = 67;
    
    /** The <code>Paste</code> icon. */
    public  static int			PASTE_ICON = 68;
    
    /** The <code>Save-As</code> icon. */
    public  static int			SAVE_AS_ICON = 69;
    
    /** The <code>Remove table</code> icon. */
    public  static int			REMOVE_TABLE_ICON = 70;
    
    /** The <code>WWW-Folder</code> icon. */
    public  static int			WWW_FOLDER_ICON = 71;
    
    /** The <code>WWW-Folder-48</code> icon. */
    public  static int			WWW_FOLDER_ICON_48 = 72;
    
    /** The <code>Save to Server</code> icon. */
    public  static int			SAVE_SERVER_ICON = 73;
    
    /** The <code>Red Asterisk</code> icon. */
    public  static int			RED_ASTERISK_ICON_11 = 74;
    
    /** The <code>Info</code> icon, 12 pixels square. */
    public  static int			INFO_12_ICON = 75;
    
    /** The <code>Split</code> icon, 12 pixels square. */
    public  static int			SPLIT_12_ICON = 76;
    
    /** The <code>Split</code> icon, 12 pixels square. */
    public  static int			EXP_9_11_ICON = 77;
    
    /** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing. 
     */
    private static int          MAX_ID = 77;
   
    
    /** Paths of the icon files. */
    private static String[]     relPaths = new String[MAX_ID+1];
    
    static {
        relPaths[STATUS_INFO] = "nuvola_messagebox_info16.png";
        relPaths[CREATE] = "nuvola_messagebox_info16.png";
        relPaths[EDITOR] = "nuvola_messagebox_info16.png";
        
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
        relPaths[INFO_12_ICON] = "info_12.png";
        relPaths[SPLIT_12_ICON] = "split_12.png";
        relPaths[EXP_9_11_ICON] = "exp_9_11.png";
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
