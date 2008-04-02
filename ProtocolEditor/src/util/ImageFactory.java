/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package util;

import javax.swing.Icon;
import javax.swing.ImageIcon;

// for returning icons.
// Icons come from Nuvola. http://icon-king.com/?p=15

public class ImageFactory {
	
	// singleton
	private static ImageFactory uniqueInstance = new ImageFactory();
	// private constructor
	private ImageFactory() {};
	// return uniqueInstance
	public static ImageFactory getInstance() {
		return uniqueInstance;
	}
	
	public static final String ICONS_FILE = "/graphx/";
	public static final String ACTION_ICONS_FILE = ICONS_FILE + "";
	
	public static final String OPEN_FILE_ICON = ICONS_FILE + "folder_open.png";
	public static final String SAVE_ICON = ACTION_ICONS_FILE + "filesave.png";
	public static final String PRINT_ICON = ACTION_ICONS_FILE + "fileExportPrint.png";
	public static final String LOAD_DEFAULTS_ICON = ACTION_ICONS_FILE + "bookmarks_list_add.png";
	public static final String NEW_FILE_ICON = ACTION_ICONS_FILE + "filenew.png";
	public static final String SAVE_FILE_AS_ICON = ACTION_ICONS_FILE + "filesaveas.png";
	public static final String EDIT_ICON = ICONS_FILE + "package_editors.png";
	public static final String ADD_ICON = ACTION_ICONS_FILE + "edit_add.png";
	public static final String DELETE_ICON = ACTION_ICONS_FILE + "cancel.png";
	public static final String MOVE_UP_ICON = ACTION_ICONS_FILE + "up.png";
	public static final String MOVE_DOWN_ICON = ACTION_ICONS_FILE + "down.png";
	public static final String DEMOTE_ICON = ACTION_ICONS_FILE + "demote.png";
	public static final String PROMOTE_ICON = ACTION_ICONS_FILE + "promote.png";
	public static final String DUPLICATE_ICON = ICONS_FILE + "window_list.png";
	public static final String COPY_ICON = ACTION_ICONS_FILE + "eclipse_copy_edit.png";
	public static final String PASTE_ICON = ACTION_ICONS_FILE + "eclipse_paste_edit.png";
	public static final String IMPORT_ICON = ACTION_ICONS_FILE + "importXml.png";
	public static final String IMPORT_TEXT_ICON = ACTION_ICONS_FILE + "eclipse_copy_import.png";
	public static final String BIG_PROTOCOL_ICON = ACTION_ICONS_FILE + "edu_science.png";
	public static final String PROTOCOL_ICON = ICONS_FILE + "edu_science.png";
	public static final String COLLAPSED_ICON = ACTION_ICONS_FILE + "1rightarrow.png";
	public static final String NOT_COLLAPSED_ICON = ACTION_ICONS_FILE + "1downarrow.png";
	public static final String WWW_ICON = ACTION_ICONS_FILE + "www.png";
	public static final String MORE_LIKE_THIS_ICON = ACTION_ICONS_FILE + "moreLikeThis.png";
	public static final String INFO_ICON = ACTION_ICONS_FILE + "messagebox_info.png";
	public static final String SEARCH_ICON = ICONS_FILE + "xmag.png";
	public static final String TWO_LEFT_ARROW = ACTION_ICONS_FILE + "2leftarrow.png";
	public static final String N0 = ACTION_ICONS_FILE + "no.png";
	public static final String EDU_MATHS = ICONS_FILE + "edu_mathematics.png";
	public static final String NOTE_PAD = ICONS_FILE + "kwrite.png";
	public static final String VALIDATION_ICON = ICONS_FILE + "spellcheck.png";
	public static final String RED_BALL_ICON = ACTION_ICONS_FILE + "krec_record.png";
	public static final String UNDO_ICON = ACTION_ICONS_FILE + "undo.png";
	public static final String REDO_ICON = ACTION_ICONS_FILE + "redo.png";
	public static final String CLEAR_FIELDS_ICON = ACTION_ICONS_FILE + "news_unsubscribe.png";
	public static final String WWW_FILE_ICON = ICONS_FILE + "folder_http.png";
	public static final String FIND_ICON = ACTION_ICONS_FILE + "find.png";
	public static final String FILE_CLOSE_ICON = ACTION_ICONS_FILE + "fileclose.png";
	public static final String PREVIOUS_UP_ICON = ACTION_ICONS_FILE + "previousUp.png";
	public static final String NEXT_DOWN_ICON = ACTION_ICONS_FILE + "nextDown.png";
	public static final String OLS_LOGO_SMALL = ACTION_ICONS_FILE + "ols-logo-small.jpg";
	public static final String NEW_ROW_ICON = ACTION_ICONS_FILE + "view_bottom.png";
	public static final String CLEAR_ROW_ICON = ACTION_ICONS_FILE + "view_clear.png";
	public static final String COLOUR_SELECTION_ICON = ACTION_ICONS_FILE + "colorize.png";
	public static final String BOLD_ICON = ACTION_ICONS_FILE + "bold.png";
	public static final String BULLET_POINTS_ICON = ACTION_ICONS_FILE + "bulletPoints.png";
	public static final String UNDERLINE_ICON = ACTION_ICONS_FILE + "underline.png";
	public static final String ENTER_ICON = ACTION_ICONS_FILE + "enter.png";
	public static final String ONTOLOGY_METADATA_ICON = ICONS_FILE + "kdict.png";
	public static final String CONFIGURE_ICON = ICONS_FILE + "package_utilities.png";
	public static final String OPEN_IMAGE_ICON = ICONS_FILE + "folder_image.png";
	public static final String SEND_COMMENT_ICON = ACTION_ICONS_FILE + "mail_send.png";
	public static final String ZOOM_ICON = ACTION_ICONS_FILE + "zoom.png";
	public static final String INDEX_FILES_ICON = ICONS_FILE + "bookcase.png";
	public static final String ROTATE_HORIZONTAL_ICON = ACTION_ICONS_FILE + "rotate_right_up.png";
	public static final String ROTATE_VERTICAL_ICON = ACTION_ICONS_FILE + "rotate_down_left.png";
	public static final String CALENDAR_ICON = ACTION_ICONS_FILE + "date.png";
	public static final String ALARM_ICON_64 = ACTION_ICONS_FILE + "kalarm64.png";
	public static final String ALARM_GIF_64 = ACTION_ICONS_FILE + "kalarmAnimated64.gif";
	public static final String RELOAD_ICON = ACTION_ICONS_FILE + "reload.png";
	public static final String LOCKED_ICON = ACTION_ICONS_FILE + "encrypted.png";
	public static final String LOCKED_ICON_48 = ACTION_ICONS_FILE + "encrypted48.png";
	public static final String UNLOCKED_ICON = ACTION_ICONS_FILE + "decrypted.png";
	public static final String NO_IMAGE_ICON = ACTION_ICONS_FILE + "file_broken32.png";
	public static final String KORGANIZER_ICON = ACTION_ICONS_FILE + "korganizer64.png";
	public static final String NETWORK_LOCAL_ICON = ACTION_ICONS_FILE + "network_local.png";
	public static final String LINK_LOCAL_ICON = ACTION_ICONS_FILE + "link_local.png";
	public static final String LINK_SCIENCE_ICON = ACTION_ICONS_FILE + "link_science.png";
	public static final String WRENCH_ICON = ACTION_ICONS_FILE + "configure.png";
	
	
	
	public Icon getIcon(String iconPathName) {
		try {
			return new ImageIcon(ImageFactory.class.getResource(iconPathName));
		} catch (NullPointerException ex) {
			System.out.println("Could not find Icon at " + iconPathName);
			return null;
		}
	}

	public ImageIcon getImageIcon(String iconPathName) {
		return (ImageIcon)getIcon(iconPathName);
	}
}
