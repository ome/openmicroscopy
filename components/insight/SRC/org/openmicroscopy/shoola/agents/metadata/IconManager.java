/*
 * org.openmicroscopy.shoola.agents.metadata.IconManager 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.AbstractIconManager;

/** 
 * Provides the icons used by the MetadataViewer.
 * <p>The icons are retrieved by first calling the 
 * {@link #getInstance() getInstance} method and then the 
 * {@link #getIcon(int) getIcon} method passing one of the icon ID's specified
 * by the static constants within this class &#151; icons will be retrieved
 * from the MetadataViewer's graphics bundle, which implies that its
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
 * @since OME3.0
 */
public class IconManager 
	extends AbstractIconManager
{

	/** The <code>Refresh</code> icon. */
    public static final int           REFRESH = 0;
    
    /** The <code>Collapse</code> icon. */
    public static final int           COLLAPSE = 1;
    
    /** The <code>Annotation</code> icon. */
    public static final int           ANNOTATION = 2;
    
    /** The <code>Tag</code> icon. */
    public static final int           TAG = 3;
    
    /** The <code>Attachment</code> icon. */
    public static final int           ATTACHMENT = 4;
    
    /** The <code>Properties</code> icon. */
    public static final int           PROPERTIES = 5;
    
    /** The <code>URL</code> icon. */
    public static final int           URL = 6;
    
    /** The <code>Image</code> icon. */
    public static final int           IMAGE = 7;
    
    /** The <code>Project</code> icon. */
    public static final int           PROJECT = 8;
    
    /** The <code>Dataset</code> icon. */
    public static final int           DATASET = 9;
    
    /** The <code>Add</code> icon. */
    public static final int           ADD = 10;
    
    /** The <code>Remove</code> icon. */
    public static final int           REMOVE = 11;
    
    /** The <code>Browse</code> icon. */
    public static final int           BROWSE = 12;
    
    /** The <code>View</code> icon. */
    public static final int           VIEW = 13;
    
    /** The <code>Owner</code> icon. */
    public static final int           OWNER = 14;
    
    /** The <code>Root</code> icon. */
    public static final int           ROOT = 15;
    
    /** The <code>List view</code> icon. */
    public static final int           LIST_VIEW = 16;
    
    /** The <code>Grid view</code> icon. */
    public static final int           GRID_VIEW = 17;
    
    /** The <code>Download</code> icon. */
    public static final int           DOWNLOAD = 18;
    
    /** The <code>Info</code> icon. */
    public static final int           INFO = 19;
    
    /** The <code>Right arrow</code> icon. */
    public static final int           RIGHT_ARROW = 20;
    
    /** The <code>Left arrow</code> icon. */
    public static final int           LEFT_ARROW = 21;
    
    /** The <code>Double right arrow</code> icon. */
    public static final int           DOUBLE_RIGHT_ARROW = 22;
    
    /** The <code>Double left arrow</code> icon. */
    public static final int           DOUBLE_LEFT_ARROW = 23;
    
    /** The <code>Tags 48x48</code> icon. */
    public static final int           TAGS_48 = 24;
    
    /** The <code>Order by date</code> icon. */
    public static final int           ORDER_BY_DATE = 25;
    
    /** The <code>Order by user</code> icon. */
    public static final int           ORDER_BY_USER = 26;
    
    /** The <code>Close</code> icon. */
    public static final int           CLOSE = 27;
    
    /** The <code>Edit</code> icon. */
    public static final int           EDIT = 28;
    
    /** The <code>Info 48x48</code> icon. */
    public static final int           INFO_48 = 29;
    
    /** The <code>PDF 22x22</code> icon. */
    public static final int           PDF_DOC_22 = 30;
    
    /** The <code>Text 22x22</code> icon. */
    public static final int           TEXT_DOC_22 = 32;
    
    /** The <code>Word 22x22</code> icon. */
    public static final int           WORD_DOC_22 = 33;
    
    /** The <code>Excel 22x22</code> icon. */
    public static final int           EXCEL_DOC_22 = 34;
    
    /** The <code>Power point 22x22</code> icon. */
    public static final int           PPT_DOC_22 = 35;
    
    /** The <code>XML 22x22</code> icon. */
    public static final int           XML_DOC_22 = 36;
    
    /** The <code>Save</code> icon. */
    public static final int           SAVE = 37;
    
    /** The <code>Columns view</code> icon. */
    public static final int           COLUMNS_VIEW = 38;
     
    /** The <code>Upload</code> icon. */
    public static final int           UPLOAD = 39;
    
    /** The <code>Sort</code> icon. */
    public static final int           SORT = 40;
    
    /** The <code>URL</code> 48x48 icon. */
    public static final int           URL_48 = 41;
     
    /** The <code>Attachment 48x48</code> icon. */
    public static final int           ATTACHMENT_48 = 42;
    
    /** The <code>Metadata 48x48</code> icon. */
    public static final int           METADATA_48 = 43;
    
    /** The <code>Doc</code> icon. */
    public static final int           DOC = 44;
    
    /** The <code>Tag 12x12</code> icon. */
    public static final int           TAG_12 = 45;
    
    /** The <code>HTML Document</code> icon. */
    public static final int           HTML_DOC_22 = 46;
    
    /** The <code>Screen</code> icon. */
    public static final int           SCREEN = 47;
    
    /** The <code>Plate</code> icon. */
    public static final int           PLATE = 48;
    
    /** The <code>Attach</code> icon. */
    public static final int           ATTACH = 49;
    
    /** The <code>Acquisition</code> icon. */
    public static final int           ACQUISITION = 50;
    
    /** The <code>Plus</code> icon. */
    public static final int           PLUS_9 = 51;
    
    /** The <code>Minus</code> icon. */
    public static final int           MINUS_9 = 52;
    
    /** The <code>Edit 8x8</code> icon. */
    public static final int           EDIT_8 = 53;
    
    /** The <code>Edit 12x12</code> icon. */
    public static final int           EDIT_12 = 54;
    
    /** The <code>Edit 48x48</code> icon. */
    public static final int           EDIT_48 = 55;
    
    /** The <code>Plus 12x12</code> icon. */
    public static final int           PLUS_12 = 56;
    
    /** The <code>Minus 12x12</code> icon. */
    public static final int           MINUS_12 = 57;
    
    /** The <code>Tag set</code> icon. */
    public static final int           TAG_SET = 58;
    
    /** The <code>Download 12x12</code> icon. */
    public static final int           DOWNLOAD_12 = 59;
    
    /** The <code>Create Movie</code> icon. */
    public static final int           MOVIE = 60;
    
    /** The <code>Histogram</code> image. */
    public static final int 		  TEMPORARY_HISTOGRAM = 61;
    
    /** The <code>Histogram</code> icon. */
    public static  final int          HISTOGRAM = 62;
   
    /** The <code>Contrast Stretching</code> icon. */
    public static  final int          CONTRAST_STRETCHING = 63;
    
    /** The <code>Plane slicing</code> icon. */
    public static  final int          PLANE_SLICING = 64;
    
    /** The  <code>codomain</code> icon. */
    public  static final int          CODOMAIN = 65;
    
    /** The  <code>domain</code> icon. */
    public  static final int          DOMAIN = 66;
    
    /** The <code>Renderer</code> icon. */
    public  static final int          RENDERER = 67;
    
    /** The <code>GreyScale</code> icon. */
    public static  final int          GRAYSCALE = 68;
    
    /** The <code>RGB</code> icon. */
    public static  final int          RGB = 69;
    
    /** The <code>Analyze</code> icon. */
    public static  final int          ANALYSE = 70;
    
    /** The <code>Analyze 48x48</code> icon. */
    public static  final int          ANALYSE_48 = 71;
    
    /** The <code>Export</code> icon. */
    public static final int           EXPORT_AS_OMETIFF = 72;
    
    /** The <code>Export 48x48</code> icon. */
    public static final int           EXPORT_AS_OMETIFF_48 = 73;
    
    /** A 22x22 version of the <code>movie</code> icon. */
    public static final int 		  MOVIE_22 = 74;
    
    /** A 22x22 version of the <code>Export</code> icon. */
    public static final int 		  EXPORT_22 = 75;
    
    /** A 22x22 version of the <code>Download</code> icon. */
    public static final int           DOWNLOAD_22 = 76;
    
    /** A 48x48 version of the <code>Download</code> icon. */
    public static final int           DOWNLOAD_48 = 77;
    
    /** The <code>publishing</code> icon. */
    public static final int           PUBLISHING = 78;
    
    /** The <code>Split View</code> icon. */
    public static final int           SPLIT_VIEW = 79;
    
    /** The 48x48 <code>Split View</code> icon. */
    public static final int           SPLIT_VIEW_48 = 80;
    
    /** The <code>Analysis</code> icon. */
    public static final int           ANALYSIS = 81;
    
    /** The 12x12 <code>Delete</code> icon. */
    public static final int           DELETE_12 = 82;
    
    /** The <code>FRAP</code> icon. */
    public static final int           ANALYSE_FRAP = 83;
    
    /** The 22x22 <code>FRAP</code> icon. */
    public static final int           ANALYSE_FRAP_22 = 84;
    
    /** The 48x48 <code>FRAP</code> icon. */
    public static final int           ANALYSE_FRAP_48 = 85;
    
    /** The <code>Python</code> icon. */
    public static final int           PYTHON = 86;
    
    /** The <code>Java</code> icon. */
    public static final int           JAVA = 87;
    
    /** The <code>Matlab</code> icon. */
    public static final int           MATLAB = 88;

    /** The <code>C++</code> icon. */
    public static final int           CPP = 89;
    
    /** The 48x48 <code>Python</code> icon. */
    public static final int           PYTHON_48 = 90;
    
    /** The 48x48 <code>Java</code> icon. */
    public static final int           JAVA_48 = 91;
    
    /** The 48x48 <code>Matlab</code> icon. */
    public static final int           MATLAB_48 = 92;

    /** The 48x48 <code>C++</code> icon. */
    public static final int           CPP_48 = 93;
    
    /** The 48x48 <code>Analysis</code> icon. */
    public static final int           ANALYSIS_48 = 94;
    
    /** The <code>Reset</code> icon. */
    public static final int           RND_RESET = 95;
    
    /** The <code>Up down</code> icon. */
    public static final int           UP_DOWN_9_12 = 96;
    
    /** The <code>Upload Script</code> icon. */
    public static final int           UPLOAD_SCRIPT = 97;
    
    /** The 48x48 <code>Upload Script</code> icon. */
    public static final int           UPLOAD_SCRIPT_48 = 98;
    
    /** The 22x22 <code>User photo</code> icon. */
    public static final int           USER_PHOTO_22 = 99;
    
    /** The 48x48 <code>User photo</code> icon. */
    public static final int           USER_PHOTO_48 = 100;
    
    /** The 32x32 <code>User photo</code> icon. */
    public static final int           USER_PHOTO_32 = 101;
    
    /** The <code>Rendering Settings redo</code> icon. */
    public static final int          RND_REDO = 102;
    
    /** The <code>Rendering Settings Min-Max</code> icon. */
    public static final int          RND_MIN_MAX = 103;
    
    /** The <code>Rendering Settings Owner</code> icon. */
    public static final int          RND_OWNER = 104;
    
    /** The <code>Rendering Settings Undo</code> icon. */
    public static final int          RND_UNDO = 105;
    
    /** The <code>Rendering Settings Apply to all</code> icon. */
    public static final int          RND_APPLY_TO_ALL = 106;
    
    /** The <code>Analysis</code> icon. */
    public static final int           ANALYSIS_RUN = 107;

    /** The <code>Black arrow down</code> icon. */
    public static final int         BLACK_ARROW_DOWN = 108;
    
    /** The <code>Transparent</code> icon. */
    public static final int         TRANSPARENT_32 = 109;
    
    /** The <code>Script with UI</code> icon. */
    public static final int         SCRIPT_WITH_UI = 110;
    
    /** The 22x22 <code>Script with UI</code> icon. */
    public static final int         SCRIPT_WITH_UI_22 = 111;
    
    /** The 32x32 <code>Script with UI</code> icon. */
    public static final int         PREVIEW_THUMBNAILS_32 = 112;
    
    /** The <code>Plot 12x12</code> icon. */
    public static final int         PLOT_12 = 113;
    
    /** The 22x22 <code>Delete</code> icon. */
    public static final int         DELETE_22 = 114;
    
    /** The 22x22 <code>Apply</code> icon. */
    public static final int         APPLY_22 = 115;
    
    /** The 48x48 <code>FLIM</code> icon. */
    public static final int         FLIM_48 = 116;
    
    /** The <code>Save As</code> icon. */
    public static final int         SAVE_AS = 117;
    
    /** The 48x48 <code>Save As</code> icon. */
    public static final int         SAVE_AS_48 = 118;
    
    /** The 22x22 <code>Save As</code> icon. */
    public static final int         SAVE_AS_22 = 119;
    
    /** The 16x16 <code>Viewer</code> icon. */
    public static final int         VIEWER = 120;

    /** The <code>Edit 12x12</code> icon. */
    public static final int         VIEW_DOC_12 = 121;

    /** The <code>View</code> icon. */
    public static final int         VIEWER_IJ = 122;
    
    /** The <code>File Path</code> icon. */
    public static final int         FILE_PATH = 123;

    /** The <code>Link</code> icon. */
    public static final int LINK = 124;
    
    /** The <code>Copy</code> icon. */
    public static final int COPY = 125;
    
    /** The <code>Paste</code> icon. */
    public static final int PASTE = 126;
    
    /** The <code>Plus</code> icon. */
    public static final int PLUS = 127;
    
    /** 
	 * The maximum ID used for the icon IDs.
	 * Allows to correctly build arrays for direct indexing.
	 */
	private static final int          MAX_ID = 127;

	/** Paths of the icon files. */
	private static String[]     relPaths = new String[MAX_ID+1];

	static {
		relPaths[REFRESH] = "nuvola_reload16.png";
		relPaths[COLLAPSE] = "eclipse_collapseall16.png";
		relPaths[ANNOTATION] = "nuvola_kwrite16.png";
		relPaths[TAG] = "nuvola_message16.png";//"nuvola_knotes16.png";
		relPaths[ATTACHMENT] = "nuvola_attach16.png";
		relPaths[PROPERTIES] = "nuvola_kate16.png";
		relPaths[URL] = "nuvola_browser16.png";
		relPaths[PROJECT] = "nuvola_folder_darkblue_open16.png"; 
        relPaths[DATASET] = "nuvola_folder_image16.png";
        relPaths[IMAGE] = "nuvola_image16.png";
        relPaths[ADD] = "nuvola_image16.png";
        relPaths[REMOVE] = "nuvola_fileclose16.png";
        relPaths[VIEW] = "viewer16.png";
        relPaths[BROWSE] = "zoom16.png";
        relPaths[OWNER] = "nuvola_kdmconfig16.png";
        relPaths[ROOT] = "nuvola_trashcan_empty16.png";
        relPaths[LIST_VIEW] = "nuvola_view_text16.png";
        relPaths[GRID_VIEW] = "nuvola_view_multicolumn16.png";
        relPaths[DOWNLOAD] = "nuvola_download_manager16.png";
        relPaths[LEFT_ARROW] = "nuvola_1leftarrow16.png";
        relPaths[RIGHT_ARROW] = "nuvola_1rightarrow16.png";
        relPaths[DOUBLE_LEFT_ARROW] = "nuvola_2leftarrow16.png";
        relPaths[DOUBLE_RIGHT_ARROW] = "nuvola_2rightarrow16.png";
        relPaths[ORDER_BY_DATE] = "eclipse_trace_persp16.png";
        relPaths[ORDER_BY_USER] = "nuvola_kdmconfig16.png";
        relPaths[CLOSE] = "eclipse_alphab_sort_co16.png";
        relPaths[EDIT] = "nuvola_editpaste16.png";
        relPaths[INFO] = "nuvola_messagebox_info16.png";
        relPaths[INFO_48] = "nuvola_messagebox_info48.png";
        relPaths[PDF_DOC_22] = "nuvola_acroread22.png";
        relPaths[TEXT_DOC_22] = "nuvola_txt22.png";
        relPaths[WORD_DOC_22] = "alienOSX_Microsoft_Word22.png";
        relPaths[PPT_DOC_22] = "alienOSX_Microsoft_PowerPoint22.png";
        relPaths[EXCEL_DOC_22] = "alienOSX_Microsoft_Excel22.png";
        relPaths[XML_DOC_22] = "txt_xml22.png";
        relPaths[HTML_DOC_22] = "txt_html22.png";
        relPaths[SAVE] = "nuvola_filesave16.png";
        relPaths[COLUMNS_VIEW] = "nuvola_view_left_right16.png";
        relPaths[UPLOAD] = "nuvola_download_manager_green_flipped16.png";
        relPaths[SORT] = "nuvola_player_play_gray16.png";
        relPaths[ATTACHMENT_48] = "nuvola_attach48.png";
		relPaths[URL_48] = "nuvola_browser48.png";
		relPaths[METADATA_48] = "nuvola_browser48.png";
		relPaths[DOC] = "nuvola_kword16.png";
		relPaths[TAG_12] = "nuvola_message12.png";
		relPaths[SCREEN] = "nuvola_folder_blue_open_modified_screen16.png";
        relPaths[PLATE] = "plate16.png";
        relPaths[ATTACH] = "nuvola_attach16.png";
        relPaths[ACQUISITION] = "nuvola_attach16.png";
        relPaths[PLUS_9] = "plus.png";
        relPaths[MINUS_9] = "minus.png";
        relPaths[EDIT_8] = "nuvola_ksig8.png";
        relPaths[EDIT_12] = "color_line-thin12.png";//nuvola_ksig11.png";
        relPaths[EDIT_48] = "nuvola_ksig48.png";
        relPaths[PLUS_12] = "plus11.png";
        relPaths[MINUS_12] = "minus11.png";
        relPaths[TAG_SET] = "nuvola_knotesRedRed16.png";
        relPaths[DOWNLOAD_12] = "nuvola_download_manager11.png";
        relPaths[MOVIE] = "crystal_video16.png";
        relPaths[TEMPORARY_HISTOGRAM] = "histogram_temporary.png";
        relPaths[HISTOGRAM] = "histogram16.png";
        relPaths[CONTRAST_STRETCHING] = "openOffice_stock_new-drawing-16.png";
        relPaths[PLANE_SLICING] = "openOffice_stock_new-labels-16.png";
        relPaths[DOMAIN] = "nuvola_kmplot16.png";
        relPaths[CODOMAIN] = "codomain16.png";
        relPaths[RENDERER] = "render16.png";
        relPaths[GRAYSCALE] = "grayscale16.png";
        relPaths[RGB] = "rgb16.png";
        relPaths[ANALYSE] = "nuvola_kchart16.png";
        relPaths[ANALYSE_48] = "nuvola_kchart48.png";
        relPaths[EXPORT_AS_OMETIFF] = "export16.png";
        relPaths[EXPORT_AS_OMETIFF_48] = "export48.png";
        relPaths[MOVIE_22] = "crystal_video22.png";
        relPaths[EXPORT_22] = "export22.png";
        relPaths[DOWNLOAD_22] = "nuvola_download_manager22.png";
        relPaths[DOWNLOAD_48] = "nuvola_download_manager48.png";
        relPaths[PUBLISHING] = "splitViewFigure16.png";
        relPaths[SPLIT_VIEW] = "splitViewFigure16.png";
        relPaths[SPLIT_VIEW_48] = "splitViewFigure48.png";
        relPaths[ANALYSIS] = "nuvola_kcmsystem16.png";
        relPaths[DELETE_12] = "nuvola_cancel11.png";
        relPaths[ANALYSE_FRAP_22] = "FRAP22.png";
        relPaths[ANALYSE_FRAP] = "FRAP16.png";
        relPaths[ANALYSE_FRAP_48] = "FRAP48.png";
        relPaths[PYTHON] = "tango_text-x-python16.png";
        relPaths[MATLAB] = "crystal_source_cpp16.png";
        relPaths[JAVA] = "tango_text-x-java-source16.png";
        relPaths[CPP] = "crystal_source_cpp16.png";
        relPaths[PYTHON_48] = "tango_text-x-python48.png";
        relPaths[MATLAB_48] = "crystal_source_java48.png";
        relPaths[JAVA_48] = "tango_text-x-java-source48.png";
        relPaths[CPP_48] = "crystal_source_cpp48.png";
        relPaths[ANALYSIS_48] = "nuvola_kcmsystem48.png";
        relPaths[RND_RESET] = "nuvola_undo16.png";
        relPaths[UP_DOWN_9_12] = "upDown.png";
        relPaths[UPLOAD_SCRIPT] = "nuvola_script_add16.png";
        relPaths[UPLOAD_SCRIPT_48] = "nuvola_script_add48.png";
        relPaths[USER_PHOTO_22] = "nuvola_personal22.png"; //"nuvola_licq22.png";
        relPaths[USER_PHOTO_48] = "nuvola_personal48.png";//"nuvola_licq48.png";
        relPaths[USER_PHOTO_32] = "nuvola_personal32.png";
        relPaths[RND_REDO] = "nuvola_redo16.png"; 
        relPaths[RND_MIN_MAX] = "nuvola_rendering_minmax16.png";
        relPaths[RND_OWNER] = "nuvola_rendering_owner16.png";
        relPaths[RND_UNDO] = "nuvola_reset16.png";
        relPaths[RND_APPLY_TO_ALL] = "rendering_applyAll16.png";
        relPaths[ANALYSIS_RUN] = "nuvola_script_run16.png";
        relPaths[BLACK_ARROW_DOWN] = "eclipse_view_menu16.png";
        relPaths[TRANSPARENT_32] = "eclipse_transparent32.png";
        relPaths[SCRIPT_WITH_UI] = "nuvola_script_ui16.png";
        relPaths[SCRIPT_WITH_UI_22] = "nuvola_script_ui22.png";
        relPaths[PREVIEW_THUMBNAILS_32] = "preview_thumbs32.png";
        relPaths[TAGS_48] = "nuvola_knotes48.png";
        relPaths[PLOT_12] = "nuvola_kmplot12.png";
        relPaths[APPLY_22] = "nuvola_apply22.png";
        relPaths[DELETE_22] = "nuvola_cancel22.png";
        relPaths[FLIM_48] = "nuvola_messagebox_info48.png";
        relPaths[SAVE_AS] = "download_image16.png";
        relPaths[SAVE_AS_48] = "download_image48.png";
        relPaths[SAVE_AS_22] = "download_image22.png";
        relPaths[VIEWER] = "viewer16.png";
        relPaths[VIEW_DOC_12] = "nuvola_editcopy12.png";
        relPaths[VIEWER_IJ] = "imageJ16.png";
        relPaths[FILE_PATH] = "icon_fs_path16.png";
        relPaths[LINK] = "nuvola_enhanced_browsing16.png";
        relPaths[COPY] = "eclipse_copy_edit16.png";
        relPaths[PASTE] = "eclipse_paste_edit16.png";
        relPaths[PLUS] = "nuvola_edit_add16.png";
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
            singleton = new IconManager(MetadataViewerAgent.getRegistry());
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
