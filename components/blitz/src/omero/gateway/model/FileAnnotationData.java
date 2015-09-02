/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package omero.gateway.model;


import java.io.File;

import static omero.rtypes.rstring;
import omero.RString;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.OriginalFile;

/**
 * Annotation to upload files to the server.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class FileAnnotationData extends AnnotationData {

    /**  The name space used to identify the experimenter photo. */
    public static final String EXPERIMENTER_PHOTO_NS =
            omero.constants.namespaces.NSEXPERIMENTERPHOTO.value;

    /**
     * The name space used to indicate that the <code>FileAnnotation</code> 
     * is a companion file.
     */
    public static final String COMPANION_FILE_NS =
            omero.constants.namespaces.NSCOMPANIONFILE.value;

    /**
     * The name space used to indicate that the <code>FileAnnotation</code>
     * is an import log file.
     */
    public static final String LOG_FILE_NS =
            omero.constants.namespaces.NSLOGFILE.value;

    /** 
     * The name space used to indicate that the <code>FileAnnotation</code> 
     * is a movie.
     */
    public static final String MOVIE_NS =
            omero.constants.metadata.NSMOVIE.value;

    /** 
     * The name space used to indicate that the <code>FileAnnotation</code> 
     * is a <code>Measurement</code> file.
     */
    public static final String MEASUREMENT_NS =
            omero.constants.namespaces.NSMEASUREMENT.value;

    /** 
     * The name space used to indicate that the <code>FileAnnotation</code> 
     * is a <code>Bulk Annotations</code> file.
     */
    public static final String BULK_ANNOTATIONS_NS =
            omero.constants.namespaces.NSBULKANNOTATIONS.value;

    /** Identifies the FLIM namespace. */
    public static final String FLIM_NS =
            omero.constants.analysis.flim.NSFLIM.value;

    /** The default name for the original metadata file.*/
    public static final String ORIGINAL_METADATA_NAME =
            omero.constants.annotation.file.ORIGINALMETADATA.value;

    /** Identifies the <code>PDF</code> file formats. */
    public static final String PDF = "pdf";

    /** Identifies the <code>Text</code> file formats. */
    public static final String TEXT = "txt";

    /** Identifies the <code>CSV</code> file formats. */
    public static final String CSV = "csv";

    /** Identifies the <code>XML</code> file formats. */
    public static final String XML = "xml";

    /** Identifies the <code>HTML</code> file formats. */
    public static final String HTML = "html";

    /** Identifies the <code>HTM</code> file formats. */
    public static final String HTM = "htm";

    /** Identifies the <code>Word</code> file formats. */
    public static final String MS_WORD = "doc";

    /** Identifies the <code>Word</code> file formats. */
    public static final String MS_WORD_X = "docx";

    /** Identifies the <code>Excel</code> file formats. */
    public static final String MS_EXCEL = "xls";

    /** Identifies the <code>Power point</code> file formats. */
    public static final String MS_POWER_POINT = "ppt";

    /** Identifies the <code>Power point</code> file formats. */
    public static final String MS_POWER_POINT_X = "pptx";

    /** Identifies the <code>Power point</code> file formats. */
    public static final String MS_POWER_POINT_SHOW = "pps";

    /** Identifies the <code>RTF</code> file formats. */
    public static final String RTF = "rtf";

    /** Indicates that the format is not recognized. */
    public static final String UNKNOWN = "UNKNOWN OR NULL FORMAT";

    /**
     * The <code>PDF</code> file format as defined by specification
     * corresponding to the extension.
     */
    private static final String SERVER_PDF = "application/pdf";

    /**
     * The <code>HTML</code> file format as defined by specification
     * corresponding to the extension.
     */
    private static final String SERVER_HTML = "text/html";

    /**
     * The <code>XML</code> file format as defined by specification
     * corresponding to the extension.
     */
    private static final String SERVER_XML = "text/xml";

    /**
     * The <code>TEXT</code> file format as defined by specification
     * corresponding to the extension.
     */
    private static final String SERVER_TEXT = "text/plain";

    /**
     * The <code>CSV</code> file format as defined by specification
     * corresponding to the extension.
     */
    private static final String SERVER_CSV = "text/csv";

    /**
     * The <code>RTF</code> file format as defined by specification
     * corresponding to the extension.
     */
    private static final String SERVER_RTF = "text/rtf";

    /**
     * The <code>Microsoft Word</code> file format as defined by specification
     * corresponding to the extension.
     */
    private static final String SERVER_MS_WORD = "application/msword";

    /**
     * The <code>Microsoft PowerPoint</code> file format as defined by
     * specification corresponding to the extension.
     */
    private static final String SERVER_MS_POWERPOINT = "application/vnd.ms-powerpoint";

    /**
     * The <code>Microsoft Excel</code> file format as defined by
     * specification corresponding to the extension.
     */
    private static final String SERVER_MS_EXCEL = "application/vnd.ms-excel";

    /**
     * The file format as defined by specification corresponding to the extension.
     */
    private static final String SERVER_OCTET_STREAM = "application/octet-stream";

    /** The file to upload to the server. */
    private File attachedFile;

    /** The format of the file. */
    private String format;

    /**
     * Returns the original file if loaded, <code>null</code> otherwise.
     *
     * @return See above.
     */
    private OriginalFile getFile()
    {
        OriginalFile f = ((FileAnnotation) asAnnotation()).getFile();
        if (f != null && f.isLoaded()) return f;
        return null;
    }

    /**
     * Controls if the file format is supported.
     *
     * @param path
     *            The absolute path.
     */
    private void validateFormat(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null.");
        }
        if (path.endsWith(PDF)) {
            format = PDF;
        } else if (path.endsWith(TEXT)) {
            format = TEXT;
        } else if (path.endsWith(CSV)) {
            format = CSV;
        } else if (path.endsWith(XML)) {
            format = XML;
        } else if (path.endsWith(HTML) || path.endsWith(HTM)) {
            format = HTML;
        } else if (path.endsWith(MS_WORD) ||
                path.endsWith(MS_WORD_X)) {
            format = MS_WORD;
        } else if (path.endsWith(MS_EXCEL)) {
            format = MS_EXCEL;
        } else if (path.endsWith(MS_POWER_POINT)
                || path.endsWith(MS_POWER_POINT_SHOW) ||
                path.endsWith(MS_POWER_POINT_X)) {
            format = MS_POWER_POINT;
        } else if (path.endsWith(RTF)) {
            format = RTF;
        } else {
            format = "";
        }
    }

    /**
     * Creates a new instance.
     * 
     * @param file
     *            The file to attach.
     */
    public FileAnnotationData(File file) {
        super(FileAnnotationI.class);
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null.");
        }
        validateFormat(file.getAbsolutePath());
        attachedFile = file;
    }

    /**
     * Creates a new instance.
     *
     * @param annotation
     *            The annotation to wrap.
     */
    public FileAnnotationData(FileAnnotation annotation)
    {
        super(annotation);
        format = null;
    }

    /**
     * Returns the format of the original file.
     *
     * @return See above.
     */
    public String getOriginalMimetype()
    {
        OriginalFile f = getFile();
        String unknown = UNKNOWN;
        String format = f == null ? unknown : (f.getMimetype() == null ? unknown
                : (f.getMimetype().getValue()));
        return format;
    }

    /**
     * Sets the description of the annotation.
     *
     * @param description The value to set.
     */
    public void setDescription(String description)
    {
        if (description == null || description.trim().length() == 0) return;
        setDirty(true);
        asAnnotation().setDescription(rstring(description));
    }

    /**
     * Returns the description of the annotation.
     *
     * @return See above.
     */
    public String getDescription()
    {
        RString value = asAnnotation().getDescription();
        if (value == null) return "";
        return value.getValue();
    }

    /**
     * Returns the file format as defined by the specification, corresponding to
     * the file extension.
     *
     * @return See above.
     */
    public String getServerFileMimetype() {
        if (format == null) {
            return SERVER_TEXT;
        }
        if (format.equals(PDF)) {
            return SERVER_PDF;
        }
        if (format.equals(XML)) {
            return SERVER_XML;
        }
        if (format.equals(HTML) || format.equals(HTM)) {
            return SERVER_HTML;
        }
        if (format.equals(CSV)) {
            return SERVER_CSV;
        }
        if (format.equals(TEXT)) {
            return SERVER_TEXT;
        }
        if (format.equals(RTF)) {
            return SERVER_RTF;
        }
        if (format.equals(MS_WORD) || format.equals(MS_WORD_X)) {
            return SERVER_MS_WORD;
        }
        if (format.equals(MS_EXCEL)) {
            return SERVER_MS_EXCEL;
        }
        if (format.equals(MS_POWER_POINT) ||
                format.equals(MS_POWER_POINT_SHOW) ||
                format.equals(MS_POWER_POINT_X)) {
            return SERVER_MS_POWERPOINT;
        }
        return SERVER_OCTET_STREAM;
    }

    /**
     * Returns the format of the uploaded file.
     *
     * @return See above.
     */
    public String getFileFormat() {
        if (attachedFile != null) {
            return format;
        }
        String format = getOriginalMimetype();
        if (SERVER_PDF.equals(format)) {
            return PDF;
        } else if (SERVER_CSV.equals(format)) {
            return CSV;
        } else if (SERVER_TEXT.equals(format)) {
            return TEXT;
        } else if (SERVER_XML.equals(format)) {
            return XML;
        } else if (SERVER_HTML.equals(format)) {
            return HTML;
        } else if (SERVER_RTF.equals(format)) {
            return RTF;
        } else if (SERVER_MS_EXCEL.equals(format)) {
            return MS_EXCEL;
        } else if (SERVER_MS_WORD.equals(format)) {
            return MS_WORD;
        } else if (SERVER_MS_POWERPOINT.equals(format)) {
            return MS_POWER_POINT;
        }
        return UNKNOWN;
    }

    /**
     * Returns a user readable description of the file.
     *
     * @return See above.
     */
    public String getFileKind() {
        String format = getFileFormat();
        if (PDF.equals(format)) {
            return "PDF Document";
        } else if (XML.equals(format)) {
            return "XML Document";
        } else if (MS_WORD.equals(format) || MS_WORD_X.equals(format)) {
            return "Microsoft Word Document";
        } else if (MS_EXCEL.equals(format)) {
            return "Microsoft Excel Document";
        } else if (MS_POWER_POINT.equals(format)
                || MS_POWER_POINT_SHOW.equals(format) ||
                MS_POWER_POINT_X.equals(format)) {
            return "Microsoft Powerpoint Document";
        } else if (TEXT.equals(format)) {
            return "Plain Text Document";
        } else if (HTML.equals(format) || HTM.equals(format)) {
            return "HTML Document";
        } else if (CSV.equals(format)) {
            return "Comma Separated Value Document";
        } else if (RTF.equals(format)) {
            return "Rich Text Format Document";
        }
        return "";
    }

    /**
     * Returns the file to upload to the server.
     *
     * @return See above.
     */
    public File getAttachedFile() {
        return attachedFile;
    }

    /**
     * Returns the name of the file.
     *
     * @return See above.
     */
    public String getFileName() 
    {
        if (attachedFile != null) {
            return attachedFile.getName();
        }
        OriginalFile f = getFile();
        String name = "";
        if (f != null) {
            if (f.getName() != null) {
                name = f.getName().getValue();
            }
            if (name != null && name.trim().length() != 0)
                return name;
            if (f.getPath() != null) {
                name = f.getPath().getValue();
            }
            if (name != null && name.trim().length() != 0)
                return name;
        }
        return ""+getFileID();
    }

    /**
     * Returns the absolute path to the file.
     *
     * @return See above.
     */
    public String getFilePath()
    {
        if (attachedFile != null) {
            return attachedFile.getAbsolutePath();
        }
        OriginalFile f = getFile();
        if (f != null) {
            if (f.getPath() != null) {
                return f.getPath().getValue();
            }
        }
        return "";
    }

    /**
     * Returns the size of the file.
     *
     * @return See above.
     */
    public long getFileSize()
    {
        if (getId() < 0)  return -1;
        OriginalFile f = getFile();
        if (f == null || f.getSize() == null)  return -1;
        return f.getSize().getValue();
    }

    /**
     * Returns the id of the file.
     *
     * @return See above.
     */
    public long getFileID()
    {
        if (getId() < 0)  return -1;
        OriginalFile f = getFile();
        if (f == null || f.getId() == null)  return -1;
        return f.getId().getValue();
    }

    /**
     * Returns the original file.
     *
     * @see AnnotationData#getContent()
     */
    @Override
    public Object getContent() {
        return ((FileAnnotation) asAnnotation()).getFile();
    }

    /**
     * Returns the absolute path to the file
     *
     * @see AnnotationData#getContentAsString()
     */
    @Override
    public String getContentAsString() {
        return getFilePath();
    }

    /**
     * Returns <code>true</code> if it is a movie file.
     * <code>false</code> otherwise.
     *
     * @return See above.
     */
    public boolean isMovieFile() 
    {
        String ns = getNameSpace();
        if (MOVIE_NS.equals(ns)) 
            return true;
        String format = getOriginalMimetype();
        return (format.contains("video"));
    }

    /**
     * Sets the text annotation.
     *
     * @see AnnotationData#setContent(Object)
     */
    @Override
    public void setContent(Object content) {
        if (content == null) {
            throw new IllegalArgumentException("Content must be an "
                    + "Original file");
        }
        if (content instanceof OriginalFile) {
            setDirty(true);
            ((FileAnnotation) asAnnotation()).setFile((OriginalFile) content);
        } else {
            throw new IllegalArgumentException("Content must be an "
                    + "Original file");
        }
    }

}
