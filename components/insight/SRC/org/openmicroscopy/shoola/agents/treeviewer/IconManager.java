/*
 * org.openmicroscopy.shoola.agents.treeviewer.IconManager
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

package org.openmicroscopy.shoola.agents.treeviewer;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.AbstractIconManager;

/** 
 * Provides the icons used by the TreeViewer.
 * <p>The icons are retrieved by first calling the 
 * {@link #getInstance() getInstance} method and then the 
 * {@link #getIcon(int) getIcon} method passing one of the icon ID's specified
 * by the static constants within this class &#151; icons will be retrieved
 * from the TreeViewer's graphics bundle, which implies that its
 * configuration has been read in (this happens during the initialization
 * procedure).</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class IconManager
    extends AbstractIconManager
{

    /** The <code>Hierarchy Explorer</code> icon. */
    public static final int           HIERARCHY_EXPLORER = 0;
    
    /** The <code>Category Explorer</code> icon. */
    public static final int           CATEGORY_EXPLORER = 1;
    
    /** The <code>Images Explorer</code> icon. */
    public static final int           IMAGES_EXPLORER = 2;
    
    /** The <code>Manager</code> icon. */
    public static final int           MANAGER = 3;
    
    /** The <code>Project</code> icon. */
    public static final int           PROJECT = 4;
    
    /** The <code>Dataset</code> icon. */
    public static final int           DATASET = 5;
    
    /** The <code>Image</code> icon. */
    public static final int           IMAGE = 6;
    
    /** The <code>Root</code> icon. */
    public static final int           ROOT = 7;
    
    /** The <code>Properties</code> icon. */
    public static final int           PROPERTIES = 8;
    
    /** The <code>Viewer</code> icon. */
    public static final int           VIEWER = 9;
    
    /** The <code>Refresh</code> icon. */
    public static final int           REFRESH = 10;
    
    /** The <code>Create</code> icon. */
    public static final int           CREATE = 11;
    
    /** The <code>Close</code> icon. */
    public static final int           CLOSE = 12;
    
    /** The <code>Collapse</code> icon. */
    public static final int           COLLAPSE = 13;
    
    /** The <code>Sort</code> icon. */
    public static final int           SORT = 14;
    
    /** The <code>Sort by Date</code> icon. */
    public static final int           SORT_DATE = 15;
    
    /** The <code>Filter</code> icon. */
    public static final int           FILTER = 16;
    
    /** The <code>Filter Menu</code> icon. */
    public static final int           FILTER_MENU = 17;
    
    /** The <code>Annotated Dataset</code> icon. */
    public static final int           DATASET_ANNOTATED = 18;
    
    /** The <code>Annotated Image</code> icon. */
    public static final int           IMAGE_ANNOTATED = 19;
    
    /** A 48x48 version of the <code>Create</code> icon. */
    public static final int           CREATE_48 = 20;
    
    /** The <code>Error</code> icon. */
    public static final int           ERROR = 21;
    
    /** A 48x48 version of the <code>Properties</code> icon. */
    public static final int           PROPERTIES_48 = 22;
    
    /** The <code>Owner</code> icon. */
    public static final int           OWNER = 23;
    
    /** The <code>Copy</code> icon. */
    public static final int           COPY = 24;
    
    /** The <code>Paste</code> icon. */
    public static final int           PASTE = 25;
    
    /** The <code>Delete</code> icon. */
    public static final int           DELETE = 26;
    
    /** The <code>Find Next</code> icon. */
    public static final int           FIND_NEXT = 27;
    
    /** The <code>Find Previous</code> icon. */
    public static final int           FIND_PREVIOUS = 28;
    
    /** The <code>Highlight</code> icon. */
    public static final int           HIGHLIGHT = 29;
    
    /** The <code>Finder</code> icon. */
    public static final int           FINDER = 30;
    
    /** The <code>Warning</code> icon. */
    public static final int           WARNING = 31;
    
    /** The <code>Blank</code> icon. */
    public static final int           TRANSPARENT = 32;
    
    /** The <code>Annotation</code> icon. */
    public static final int           ANNOTATION = 33;
    
    /** The <code>Classification</code> icon. */
    public static final int           CLASSIFY = 34;
    
    /** The <code>Classification</code> icon. */
    public static final int           CATEGORY_48 = 35;
    
    /** The <code>Classified Image</code> icon. */
    public static final int           CLASSIFIED_IMAGE = 36;
    
    /** The <code>Classified and Annotated Image</code> icon. */
    public static final int           ANNOTATED_CLASSIFIED_IMAGE = 37;
    
    /** The <code>Exit Application</code> icon. */
    public static final int           EXIT_APPLICATION = 38;
    
    /** The <code>Filter 48</code> icon. */
    public static final int           FILTER_48 = 39;
    
    /** The <code>Navigation Forward</code> icon. */
    public static final int           FORWARD_NAV = 40;
    
    /** The <code>Navigation Forward</code> icon. */
    public static final int           BACKWARD_NAV = 41;
    
    /** The <code>Status Info</code> icon. */
    public static final int           STATUS_INFO = 42;
    
    /** The <code>Cancel</code> icon. */
    public static final int           CANCEL = 43;
    
    /** The <code>Data Manager</code> icon. */
    public static final int           DATA_MANAGER = 44;
    
    /** The <code>Classifier</code> icon. */
    public static final int           CLASSIFIER = 45;
    
    /** The <code>Add top container</code> icon. */
    public static final int           ADD_CONTAINER = 46;
    
    /** The <code>Add existing</code> icon. */
    public static final int           ADD_EXISTING = 47;
    
    /** The <code>Progress</code> icon. */
    public static final int           PROGRESS = 48;
    
    /** The <code>Declassify</code> icon. */
    public static final int           DECLASSIFY = 49;
    
    /** The <code>Declassify</code> icon. */
    public static final int           CUT = 50;
    
    /** The <code>Question</code> icon. */
    public static final int           QUESTION = 51;
    
    /** The <code>Manager 48</code> icon. */
    public static final int           MANAGER_48 = 52;
        
    /** The <code>Partial name</code> icon. */
    public static final int           PARTIAL_NAME = 53;
    
    /** The <code>Partial name</code> icon. */
    public static final int           IMAGE_48 = 54;
    
    /** The <code>User Group</code> icon. */
    public static final int           USER_GROUP = 55;
    
    /** The <code>Owner 48</code> icon. */
    public static final int           OWNER_48 = 56;
    
    /** The <code>Server</code> icon. */
    public static final int           SERVER = 57;
    
    /** The <code>history</code> icon. */
    public static final int           HISTORY = 58;
    
    /** The <code>roll over</code> icon. */
    public static final int           ROLL_OVER = 59;
    
    /** The <code>browser</code> icon. */
    public static final int           BROWSER = 60;
    
    /** The <code>disk space</code> icon. */
    public static final int           DISK_SPACE = 61;
    
    /** The <code>date</code> icon. */
    public static final int           DATE = 62;
    
    /** The <code>Redo</code> icon. */
    public static final int           REDO = 63;
    
    /** The <code>Search</code> icon. */
    public static final int           SEARCH = 64;
    
    /** The <code>remove edit</code> icon. */
    public static final int           EDIT_REMOVE = 65;
    
    /** The <code>add_12</code> icon. */
    public static final int           ADD_12 = 66;
    
    /** The <code>Project annotated</code> icon. */
    public static final int           PROJECT_ANNOTATED = 67;
    
    /** The <code>Tags explorer</code> icon. */
    public static final int           TAGS_EXPLORER = 68;
    
    /** The <code>Tag</code> icon. */
    public static final int           TAG = 69;
    
    /** The <code>Set original Rendering settings</code> icon. */
    public static final int           SET_RND_SETTINGS = 70;
    
    /** The <code>Tag Set</code> icon. */
    public static final int           TAG_SET = 71;
    
    /** The <code>Add metadata</code> icon. */
    public static final int           ADD_METADATA = 72;
    
    /** The <code>Add metadata 48</code> icon. */
    public static final int           ADD_METADATA_48 = 73;
    
    /** The <code>Screens Explorer</code> icon. */
    public static final int           SCREENS_EXPLORER = 74;
    
    /** The <code>Screen</code> icon. */
    public static final int           SCREEN = 75;
    
    /** The <code>Screen Annotated</code> icon. */
    public static final int           SCREEN_ANNOTATED = 76;
    
    /** The <code>Plate</code> icon. */
    public static final int           PLATE = 77;
    
    /** The <code>Plate Annotated</code> icon. */
    public static final int           PLATE_ANNOTATED = 78;
    
    /** The <code>Editor</code> icon. */
    public static final int           EDITOR = 79;
    
    /** The <code>Files Explorer</code> icon. */
    public static final int           FILES_EXPLORER = 80;
    
    /** The <code>File</code> icon. */
    public static final int           FILE = 81;
    
    /** The <code>File PDF</code> icon. */
    public static final int           FILE_PDF = 82;
    
    /** The <code>File text</code> icon. */
    public static final int           FILE_TEXT = 83;
    
    /** The <code>File editor</code> icon. */
    public static final int           FILE_EDITOR = 84;
    
    /** The <code>File Word</code> icon. */
    public static final int           FILE_WORD = 85;
    
    /** The <code>File Excel</code> icon. */
    public static final int           FILE_EXCEL = 86;
    
    /** The <code>File PPT</code> icon. */
    public static final int           FILE_PPT = 87;
    
    /** The <code>File XML</code> icon. */
    public static final int           FILE_XML = 88;
    
    /** The <code>File HTML</code> icon. */
    public static final int           FILE_HTML = 89;
    
    /** The <code>Importer</code> icon. */
    public static final int           IMPORTER = 90;
    
    /** The <code>Remove 48</code> icon. */
    public static final int           REMOVE_48 = 91;
    
    /** The <code>Tag 48</code> icon. */
    public static final int           TAG_48 = 92;
    
    /** A 48x48 version of the <code>Project</code> icon. */
    public static final int           PROJECT_48 = 93;
    
    /** A 48x48 version of the <code>Dataset</code> icon. */
    public static final int           DATASET_48 = 94;
    
    /** A 48x48 version of the <code>Tag Set</code> icon. */
    public static final int           TAG_SET_48 = 95;
    
    /** A 48x48 version of the <code>Screen</code> icon. */
    public static final int           SCREEN_48 = 96;
    
    /** A version of the <code>Editor experiment</code> icon. */
    public static final int           EDITOR_EXPERIMENT = 97;
    
    /** A version of the <code>Editor protocol</code> icon. */
    public static final int           EDITOR_PROTOCOL = 98;
    
    /** A version of the <code>Editor experiment</code> icon. */
    public static final int           FILE_PROTOCOL_EXPERIMENT = 99;
    
    /** A version of the <code>Inspector</code> icon. */
    public static final int           INSPECTOR = 100;
    
    /** A version of the <code>Movie</code> icon. */
    public static final int           MOVIE = 101;
    
    /** The <code>Directory</code> icon. */
    public static final int           DIRECTORY = 102;
    
    /** The <code>Import</code> icon. */
    public static final int           IMPORTED_FILE = 103;
    
    /** The <code>File System</code> icon. */
    public static final int           FILE_SYSTEM_EXPLORER = 104;
    
    /** The <code>Info</code> icon. */
    public static final int           INFO = 105;
    
    /** The <code>Movie folder</code> icon. */
    public static final int           MOVIE_FOLDER = 106;
    
    /** The 22x22 <code>Split view figure</code> icon. */
    public static final int           SPLIT_VIEW_FIGURE_22 = 107;
    
    /** The <code>Download</code> icon. */
    public static final int           DOWNLOAD = 108;
    
    /** The 48x48 <code>Download</code> icon. */
    public static final int           DOWNLOAD_48 = 109;
    
    /** The 22x22 <code>Download</code> icon. */
    public static final int           DOWNLOAD_22 = 110;
    
    /** The 48x48 <code>Application</code> icon. */
    public static final int           APPLICATION_48 = 111;
    
    /** The 12x12 <code>Personal</code> icon. */
    public static final int          PERSONAL = 112;
    
    /** The 48x48 <code>Personal</code> icon. */
    public static final int          PERSONAL_48 = 113;
    
    /** The <code>Plate Acquisition</code> icon. */
    public static final int           PLATE_ACQUISITION = 114;

    /** The <code>Data Browser</code> icon. */
    public static final int           DATA_BROWSER = 115;

    /** The <code>Upload</code> icon. */
    public static final int           UPLOAD = 116;
    
    /** The 48x48 <code>Upload</code> icon. */
    public static final int           UPLOAD_48 = 117;

    /** The <code>Admin</code> icon. */
    public static final int           ADMIN = 118;
    
    /** The <code>Registered Directory</code> icon. */
    public static final int           DIRECTORY_REGISTERED = 119;
    
    /** The <code>Registered File</code> icon. */
    public static final int           FILE_REGISTERED = 120;
    
    /** The <code>Unregistered Image</code> icon. */
    public static final int           IMAGE_UNREGISTERED = 121;
    
    /** The <code>Password</code> icon. */
    public static final int           PASSWORD = 122;
    
    /** The <code>48x48 Password</code> icon. */
    public static final int           PASSWORD_48 = 123;
    
    /** The <code>Owner not active</code> icon. */
    public static final int           OWNER_NOT_ACTIVE = 124;
    
    /** The <code>Up down</code> icon. */
    public static final int           UP_DOWN_9_12 = 125;
    
    /** The <code>Image Directory</code> icon. */
    public static final int           IMAGE_DIRECTORY = 126;
    
    /** The <code>Unregistered Image Directory</code> icon. */
    public static final int           IMAGE_DIRECTORY_UNREGISTERED = 127;
    
    /** The <code>Upload Script</code> icon. */
    public static final int           UPLOAD_SCRIPT = 128;
    
    /** The 48x48 <code>Upload Script</code> icon. */
    public static final int           UPLOAD_SCRIPT_48 = 129;
    
    /** The <code>Private Group</code> icon. */
    public static final int           PRIVATE_GROUP = 130;
    
    /** The <code>Read Group</code> icon. */
    public static final int           READ_GROUP = 131;
    
    /** The <code>Read Link Group</code> icon. */
    public static final int           READ_LINK_GROUP = 132;
    
    /** The <code>Public Group</code> icon. */
    public static final int           PUBLIC_GROUP = 133;
    
    /** The 12x12 <code>Private Group Drop Down</code> icon. */
    public static final int           PRIVATE_GROUP_DD_12 = 134;
    
    /** The 12x12 <code>Read Group Drop Down</code> icon. */
    public static final int           READ_GROUP_DD_12 = 135;
    
    /** The 12x12 <code>Read Link Group Drop Down</code> icon. */
    public static final int           READ_LINK_GROUP_DD_12 = 136;
    
    /** The 12x12 <code>Public Group Drop Down</code> icon. */
    public static final int           PUBLIC_GROUP_DD_12 = 137;
    
    /** The <code>Rendering Settings redo</code> icon. */
    public static final int           RND_UNDO = 138;
    
    /** The <code>Rendering Settings Min-Max</code> icon. */
    public static final int           RND_MIN_MAX = 139;
    
    /** The <code>Rendering Settings Owner</code> icon. */
    public static final int           RND_OWNER = 140;
    
    /** The 22x22 <code>Delete</code> icon. */
    public static final int           DELETE_22 = 141;
    
    /** The 22x22 <code>Apply</code> icon. */
    public static final int           APPLY_22 = 142;
    
    /** The <code>Send comment</code> icon. */
    public static final int			  SEND_COMMENT = 143;
    
    /** The 48x48 <code>Add existing</code> icon. */
    public static final int           ADD_EXISTING_48 = 144;
    
    /** The <code>Annotated Dataset</code> to refresh icon. */
    public static final int           DATASET_ANNOTATED_TO_REFRESH = 145;
    
    /** The <code>Dataset</code> to refresh icon. */
    public static final int           DATASET_TO_REFRESH = 146;
    
    /** The <code>Annotated Project</code> to refresh icon. */
    public static final int           PROJECT_ANNOTATED_TO_REFRESH = 147;
    
    /** The <code>Project</code> to refresh icon. */
    public static final int           PROJECT_TO_REFRESH = 148;
    
    /** The <code>Annotated Screen</code> to refresh icon. */
    public static final int           SCREEN_ANNOTATED_TO_REFRESH = 149;
    
    /** The <code>Screen</code> to refresh icon. */
    public static final int           SCREEN_TO_REFRESH = 150;
    
    /** The <code>Owner</code> to Refresh icon. */
    public static final int           OWNER_TO_REFRESH = 151;
    
    /** The <code>Owner</code> Group icon. */
    public static final int           OWNER_GROUP = 152;
    
    /** The 48x48<code>Group</code> icon. */
    public static final int           OWNER_GROUP_48 = 153;
    
    /** The <code>Plate Acquisition</code> icon. */
    public static final int           PLATE_ACQUISITION_ANNOTATED = 154;
    
    /** The <code>Tag</code> icon. */
    public static final int			TAG_OTHER_OWNER = 155;
    
    /** The <code>Login</code> icon. */
    public static final int			LOGIN = 156;
    
    /** The <code>Analysis</code> icon. */
    public static final int         ANALYSIS_RUN = 157;
    
    /** The <code>Analysis</code> icon. */
    public static final int         ANALYSIS = 158;
    
    /** The 48x48 <code>Analysis</code> icon. */
    public static final int         ANALYSIS_48 = 159;

    /** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing. 
     */
    private static final int          MAX_ID = 159;
    
    /** Paths of the icon files. */
    private static String[]     relPaths = new String[MAX_ID+1];
    
    static {
        relPaths[HIERARCHY_EXPLORER] = "eclipse_hierarchy_co16.png";
        relPaths[CATEGORY_EXPLORER] = "eclipse_hierarchicalLayout16.png";
        relPaths[IMAGES_EXPLORER] = "eclipse_outline_co16.png";
        relPaths[MANAGER] = "nuvola_file-manager16.png";
        relPaths[PROJECT] = "nuvola_folder_darkblue_open16.png";//"nuvola_document16.png";
        relPaths[DATASET] = "nuvola_folder_image16.png";
        relPaths[IMAGE] = "nuvola_image16.png";
        relPaths[ROOT] = "nuvola_trashcan_empty16.png";
        relPaths[PROPERTIES] = "nuvola_kate16.png";
        relPaths[VIEWER] = "viewer16.png";
        relPaths[REFRESH] = "nuvola_reload16.png";
        relPaths[CREATE] = "nuvola_filenew16.png"; 
        relPaths[CLOSE] = "nuvola_editdelete16.png";
        relPaths[COLLAPSE] = "eclipse_collapseall16.png";
        relPaths[SORT] = "eclipse_alphab_sort_co16.png";
        relPaths[SORT_DATE] = "eclipse_trace_persp16.png";
        relPaths[FILTER] = "nuvola_find16.png";
        relPaths[FILTER_MENU] = "eclipse_view_menu16.png"; 
        relPaths[DATASET_ANNOTATED] = "tagged_dataset16.png";
        relPaths[IMAGE_ANNOTATED] = "tagged_image16.png";
        relPaths[CREATE_48] = "nuvola_filenew48.png"; 
        relPaths[ERROR] = "eclipse_error_tsk16.png";
        relPaths[PROPERTIES_48] = "nuvola_filenew48.png";
        relPaths[OWNER] = "nuvola_kdmconfig_modified16.png";
        relPaths[COPY] = "eclipse_copy_edit16.png";
        relPaths[PASTE] = "eclipse_paste_edit16.png";
        relPaths[DELETE] = "eclipse_delete_edit16.png";
        relPaths[FIND_NEXT] = "eclipse_SelectNextBottomMappedObject16.png";
        relPaths[FIND_PREVIOUS] = 
            		"eclipse_SelectPreviousBottomMappedObject16.png";
        relPaths[HIGHLIGHT] = "eclipse_default_log_co16.png";
        relPaths[FINDER] = "eclipse_searchrecord16.png";
        relPaths[WARNING] = "eclipse_showwarn_tsk16.png";
        relPaths[TRANSPARENT] = "eclipse_transparent16.png";
        relPaths[ANNOTATION] = "nuvola_kwrite16.png";
        relPaths[CLASSIFY] = "nuvola_knotes16.png";
        relPaths[CATEGORY_48] = "nuvola_filenew48.png";
        relPaths[ANNOTATED_CLASSIFIED_IMAGE] = 
                                    "annotated_tagged_image16.png";
        relPaths[CLASSIFIED_IMAGE] = "tagged_image16.png";
        relPaths[EXIT_APPLICATION] = "nuvola_exit16.png";
        relPaths[FILTER_48] = "nuvola_find48.png";
        relPaths[FORWARD_NAV] = "eclipse_forward_nav16.png";
        relPaths[BACKWARD_NAV] = "eclipse_backward_nav16.png";
        relPaths[STATUS_INFO] = "nuvola_messagebox_info16.png";
        relPaths[CANCEL] = "nuvola_cancel16.png";
        relPaths[DATA_MANAGER] = "eclipse_external_tools16.png";
        relPaths[CLASSIFIER] = "tag_folder_open16.png";//"eclipse_sroot_obj16.png";
        relPaths[ADD_CONTAINER] = "eclipse_newpack_wiz16.png";
        relPaths[ADD_EXISTING] = "nuvola_window_new16.png";//"eclipse_newdatapool_wiz16.png";
        relPaths[PROGRESS] = "eclipse_progress_none16.png";
        relPaths[DECLASSIFY] = "delete_knotes16.png";
        relPaths[CUT] = "nuvola_editcut16.png";
        relPaths[QUESTION] = "nuvola_filetypes32.png";
        relPaths[MANAGER_48] = "nuvola_file-manager48.png";
        relPaths[PARTIAL_NAME] = "nuvola_kmessedwords16.png";
        relPaths[IMAGE_48] = "nuvola_thumbnail48.png";
        relPaths[USER_GROUP] = "nuvola_kgpg_photo16.png";
        relPaths[OWNER_48] = "nuvola_kdmconfig_modified48.png";
        relPaths[SERVER] = "nuvola_server16.png";
        relPaths[HISTORY] = "eclipse_history_list16.png";
        relPaths[ROLL_OVER] = "nuvola_mouse16.png";
        relPaths[BROWSER] = "thumbnail_view16.png";
        relPaths[DISK_SPACE] = "nuvola_kcmpartitions16.png";
        relPaths[DATE] = "nuvola_date16.png";
        relPaths[REDO] = "nuvola_undo16.png";
        relPaths[SEARCH] = "nuvola_find16.png";
        relPaths[EDIT_REMOVE] = "remove12.png";
        relPaths[ADD_12] = "add12.png";
        relPaths[PROJECT_ANNOTATED] = "tagged_project_darkblue16.png";
        relPaths[TAGS_EXPLORER] = "eclipse_hierarchicalLayout16.png";
        relPaths[TAG] = "nuvola_knotes16.png";
        relPaths[SET_RND_SETTINGS] = "nuvola_redo16.png";
        relPaths[TAG_SET] = "nuvola_knotesRedRed16.png";
        relPaths[ADD_METADATA] = "nuvola_knotesOrange16.png";
        relPaths[ADD_METADATA_48] = "nuvola_kdmconfig48.png";
        relPaths[SCREENS_EXPLORER] ="eclipse_hierarchicalLayout16.png";
        relPaths[SCREEN] = "nuvola_folder_blue_open_modified_screen16.png";
        relPaths[SCREEN_ANNOTATED] =
        	"nuvola_folder_blue_open_modified_screen_annotated16.png";
        relPaths[PLATE_ANNOTATED] = "plate_annotated16.png";
        relPaths[PLATE] = "plate16.png";
        relPaths[EDITOR] = "omeroEditorLink16.png";
        relPaths[FILES_EXPLORER] = "nuvola_attach16.png";
        relPaths[FILE] = "nuvola_attach16.png";
        relPaths[FILE_PDF] = "nuvola_acroread16.png";
        relPaths[FILE_TEXT] = "nuvola_txt16.png";
        relPaths[FILE_WORD] = "alienOSX_Microsoft_Word16.png";
        relPaths[FILE_PPT] = "alienOSX_Microsoft_PowerPoint16.png";
        relPaths[FILE_EXCEL] = "alienOSX_Microsoft_Excel16.png";
        relPaths[FILE_XML] = "txt_xml16.png";
        relPaths[FILE_HTML] = "txt_html16.png";
        relPaths[FILE_EDITOR] = "omeroEditorLink16.png";
        relPaths[IMPORTER] = "omeroImporterLink16.png";
        relPaths[REMOVE_48] = "nuvola_trashcan_full48.png";
        relPaths[TAG_48] = "nuvola_knotes48.png";
        relPaths[TAG_SET_48] = "nuvola_knotes_red48.png";
        relPaths[PROJECT_48] = "nuvola_folder_blue_open48.png";
        relPaths[DATASET_48] = "nuvola_folder_image48.png";
        relPaths[SCREEN_48] = "nuvola_knotes48.png";
        relPaths[EDITOR_PROTOCOL] = "nuvola_folder_cyan_open16.png";
        relPaths[EDITOR_EXPERIMENT] = "nuvola_folder_cyan_open16.png";
        relPaths[FILE_PROTOCOL_EXPERIMENT] = "experimentNew16.png";
        relPaths[INSPECTOR] = "nuvola_view_tree16.png";
        relPaths[MOVIE] = "openOffice_stock_insert-video-plugin-16.png";
        relPaths[DIRECTORY] = "nuvola_folder_grey16.png";
        relPaths[IMPORTED_FILE] = "openOffice_stock_insert-video-plugin-16.png";
        relPaths[FILE_SYSTEM_EXPLORER] = "nuvola_fsview16.png";
        relPaths[INFO] = "nuvola_messagebox_info16.png";
        relPaths[MOVIE_FOLDER] = "nuvola_folder_video16.png";
        relPaths[SPLIT_VIEW_FIGURE_22] = "splitViewFigure22.png";
        relPaths[DOWNLOAD] = "nuvola_download_manager16.png";
        relPaths[DOWNLOAD_48] = "nuvola_download_manager48.png";
        relPaths[DOWNLOAD_22] = "nuvola_download_manager22.png";
        relPaths[APPLICATION_48] = "nuvola_applixware48.png";//"nuvola_download_manager48.png";
        relPaths[PERSONAL] = "nuvola_personal16.png";
        relPaths[PERSONAL_48] = "nuvola_personal48.png";
        relPaths[PLATE_ACQUISITION] = "plateAcquisition16.png";
        relPaths[DATA_BROWSER] = "nuvola_kwrite16.png";//"plateAcquisition16.png";
        relPaths[UPLOAD] = "nuvola_download_manager_rotated16.png";
        relPaths[UPLOAD_48] = "nuvola_download_manager_rotated48.png";
        relPaths[ADMIN] = "nuvola_personal16.png";
        relPaths[DIRECTORY_REGISTERED] = "nuvola_folder_grey_annotated16.png";;//"nuvola_folder_grey16.png";
        relPaths[FILE_REGISTERED] = "nuvola_txt_annotated16.png";//"nuvola_folder_grey16.png";
        relPaths[IMAGE_UNREGISTERED] = "nuvola_image_grey16.png";
        relPaths[PASSWORD] = "nuvola_download_manager_rotated16.png";
        relPaths[PASSWORD_48] = "nuvola_download_manager_rotated48.png";
        relPaths[OWNER_NOT_ACTIVE] = "nuvola_kdmconfig_modified_grey16.png";
        relPaths[UP_DOWN_9_12] = "upDown.png";
        relPaths[IMAGE_DIRECTORY] = "nuvola_folder_image16.png";
        relPaths[IMAGE_DIRECTORY_UNREGISTERED] = "nuvola_folder_image16.png";
        relPaths[UPLOAD_SCRIPT] = "nuvola_script_add16.png";
        relPaths[UPLOAD_SCRIPT_48] = "nuvola_script_add48.png";
        relPaths[PRIVATE_GROUP] = "nuvola_ledred16.png";
        relPaths[READ_GROUP] = "nuvola_ledorange_readOnly16.png";
        relPaths[READ_LINK_GROUP] = "nuvola_ledorange16.png";
        relPaths[PUBLIC_GROUP] = "nuvola_ledgreen16.png";
        
        relPaths[PRIVATE_GROUP_DD_12] = "nuvola_permission_private_dd12.png";
        relPaths[READ_GROUP_DD_12] = "nuvola_permission_readOnly_dd12.png";
        relPaths[READ_LINK_GROUP_DD_12] = "nuvola_permission_read_dd12.png";//"nuvola_permission_readLink_dd12.png";
        relPaths[PUBLIC_GROUP_DD_12] = "nuvola_permission_public_dd12.png";
        relPaths[RND_UNDO] = "nuvola_undo16.png";
        relPaths[RND_MIN_MAX] = "nuvola_rendering_minmax16.png";
        relPaths[RND_OWNER] = "rendering_owner16.png";
        relPaths[DELETE_22] = "nuvola_cancel22.png";
        relPaths[APPLY_22] = "nuvola_apply22.png";
        relPaths[SEND_COMMENT] = "nuvola_mail_send16.png";
        relPaths[ADD_EXISTING_48] = "nuvola_window_new48.png";
        relPaths[PROJECT_TO_REFRESH] = "nuvola_folder_darkblue_open_reload16.png";
        	//"nuvola_folder_darkblue_open_ledgreen16.png";
        relPaths[DATASET_TO_REFRESH] = "nuvola_folder_image_reload16.png";//"nuvola_folder_image_ledgreen16.png";
        relPaths[SCREEN_TO_REFRESH] = 
        	"nuvola_folder_blue_open_modified_screen_reload16.png";
        	//"nuvola_folder_blue_open_modified_screen_ledgreen16.png";
        relPaths[SCREEN_ANNOTATED_TO_REFRESH] =
        	"nuvola_folder_blue_open_modified_screen_annotated_reload16.png";
        	//"nuvola_folder_blue_open_modified_screen_annotated_ledgreen16.png";
        relPaths[PROJECT_ANNOTATED_TO_REFRESH] = 
        	"tagged_project_darkblue_reload16.png";
        	//"tagged_project_darkblue_ledgreen16.png";
        relPaths[DATASET_ANNOTATED_TO_REFRESH] = "tagged_dataset_reload16.png";//"tagged_dataset_ledgreen16.png";
        relPaths[OWNER_TO_REFRESH] = "nuvola_kdmconfig_reload16.png";//"nuvola_kdmconfig_modified_ledgreen16.png";
        relPaths[OWNER_GROUP] = "nuvola_kdmconfig16.png";
        relPaths[OWNER_GROUP_48] = "nuvola_kdmconfig48.png";
        relPaths[PLATE_ACQUISITION_ANNOTATED] = "plateAcquisition_annotated16.png";
        relPaths[TAG_OTHER_OWNER] = "nuvola_knotes_group16.png";
        relPaths[LOGIN] = "nuvola_login16.png";
        relPaths[ANALYSIS_RUN] = "nuvola_script_run16.png";
        relPaths[ANALYSIS_48] = "nuvola_kcmsystem48.png";
        relPaths[ANALYSIS] = "nuvola_kcmsystem16.png";
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
            singleton = new IconManager(TreeViewerAgent.getRegistry());
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
