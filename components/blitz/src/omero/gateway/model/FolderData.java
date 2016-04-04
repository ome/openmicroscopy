/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee. All rights reserved.
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

import static omero.rtypes.rstring;

import java.util.ArrayList;
import java.util.List;

import omero.model.Annotation;
import omero.model.BooleanAnnotation;
import omero.model.CommentAnnotation;
import omero.model.DoubleAnnotation;
import omero.model.FileAnnotation;
import omero.model.Folder;
import omero.model.FolderAnnotationLink;
import omero.model.FolderI;
import omero.model.FolderImageLink;
import omero.model.FolderRoiLink;
import omero.model.LongAnnotation;
import omero.model.MapAnnotation;
import omero.model.TagAnnotation;
import omero.model.TermAnnotation;
import omero.model.TimestampAnnotation;
import omero.model.XmlAnnotation;

/**
 * Pojo wrapper for an <i>OME</i> Folder.
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class FolderData extends DataObject {

    /** Caches the folder path string */
    private String folderPathString = null;

    /** Caches the folder path separator character */
    private char folderPathSeparatorChar = '>';

    /** Creates a new instance. */
    public FolderData() {
        setDirty(true);
        setValue(new FolderI());
    }

    /**
     * Creates a new instance.
     *
     * @param folder
     *            Back pointer to the {@link Folder} model object. Mustn't be
     *            <code>null</code>.
     * @throws IllegalArgumentException
     *             If the object is <code>null</code>.
     */
    public FolderData(Folder folder) {
        if (folder == null) {
            throw new IllegalArgumentException("Object cannot null.");
        }
        setValue(folder);
    }

    /**
     * Get the name of the Folder
     * 
     * @return See above
     */
    public String getName() {
        omero.RString n = asFolder().getName();
        if (n == null || n.getValue() == null) {
            throw new IllegalStateException(
                    "The name should never have been null.");
        }
        return n.getValue();
    }

    /**
     * Set the name of the Folder
     * 
     * @param name
     *            The name
     */
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The name cannot be null.");
        }
        setDirty(true);
        asFolder().setName(rstring(name));
    }

    /**
     * Get the description of the Folder
     * 
     * @return See above
     */
    public String getDescription() {
        return asFolder().getDescription() != null ? asFolder()
                .getDescription().getValue() : "";
    }

    /**
     * Set the description of the Folder
     * 
     * @param desc
     *            The description
     */
    public void setDescription(String desc) {
        setDirty(true);
        asFolder().setDescription(rstring(desc));
    }

    /**
     * Get the the parent folder
     * 
     * @return See above.
     */
    public FolderData getParentFolder() {
        Folder f = asFolder().getParentFolder();
        return f == null ? null : new FolderData(f);
    }

    /**
     * Set the the parent folder
     * 
     * @param parent
     *            The parent folder
     */
    public void setParentFolder(Folder parent) {
        Folder f = asFolder();
        f.setParentFolder(parent);
    }

    /**
     * Set the {@link Folder}
     * 
     * @param f
     *            The folder
     */
    public void setFolder(Folder f) {
        setValue(f);
    }
    
    /**
     * Get the number of sub folders
     * 
     * @return See above.
     */
    public int subfolderCount() {
        return asFolder().sizeOfChildFolders();
    }

    /**
     * Get the number of images linked to this folder
     * 
     * @return See above.
     */
    public int imageCount() {
        return asFolder().sizeOfImageLinks();
    }

    /**
     * Get the number of ROIs linked to this folder
     * 
     * @return See above.
     */
    public int roiCount() {
        return asFolder().sizeOfRoiLinks();
    }
    
    /**
     * Copy the list of child folders, see {@link Folder#copyChildFolders()}
     * 
     * @return See above.
     */
    public List<FolderData> copyChildFolders() {
        Folder f = asFolder();
        List<Folder> children = f.copyChildFolders();
        List<FolderData> result = new ArrayList<FolderData>(children.size());
        for (Folder child : children)
            result.add(new FolderData(child));
        return result;
    }

    /**
     * Copy the list of annotation links, see
     * {@link Folder#copyAnnotationLinks()}
     * 
     * @return See above.
     */
    public List<AnnotationData> copyAnnotationLinks() {
        Folder f = asFolder();

        List<FolderAnnotationLink> links = f.copyAnnotationLinks();
        List<AnnotationData> result = new ArrayList<AnnotationData>(
                links.size());
        for (FolderAnnotationLink l : links) {
            Annotation anno = l.getChild();
            if (anno instanceof BooleanAnnotation) {
                result.add(new BooleanAnnotationData((BooleanAnnotation) anno));
            } else if (anno instanceof DoubleAnnotation) {
                result.add(new DoubleAnnotationData((DoubleAnnotation) anno));
            } else if (anno instanceof FileAnnotation) {
                result.add(new FileAnnotationData((FileAnnotation) anno));
            } else if (anno instanceof LongAnnotation) {
                result.add(new LongAnnotationData((LongAnnotation) anno));
            } else if (anno instanceof MapAnnotation) {
                result.add(new MapAnnotationData((MapAnnotation) anno));
            } else if (anno instanceof TagAnnotation) {
                result.add(new TagAnnotationData((TagAnnotation) anno));
            } else if (anno instanceof TermAnnotation) {
                result.add(new TermAnnotationData((TermAnnotation) anno));
            } else if (anno instanceof CommentAnnotation) {
                result.add(new TextualAnnotationData((CommentAnnotation) anno));
            } else if (anno instanceof TimestampAnnotation) {
                result.add(new TimeAnnotationData((TimestampAnnotation) anno));
            } else if (anno instanceof XmlAnnotation) {
                result.add(new XMLAnnotationData((XmlAnnotation) anno));
            }
        }
        return result;
    }

    /**
     * Copy the list of image links, see {@link Folder#copyImageLinks()}
     * 
     * @return See above.
     */
    public List<ImageData> copyImageLinks() {
        Folder f = asFolder();

        List<FolderImageLink> links = f.copyImageLinks();
        List<ImageData> result = new ArrayList<ImageData>(links.size());
        for (FolderImageLink l : links) {
            result.add(new ImageData(l.getChild()));
        }
        return result;
    }

    /**
     * Copy the list of roi links, see {@link Folder#copyRoiLinks()}
     * 
     * @return See above.
     */
    public List<ROIData> copyROILinks() {
        Folder f = asFolder();

        List<FolderRoiLink> links = f.copyRoiLinks();
        List<ROIData> result = new ArrayList<ROIData>(links.size());
        for (FolderRoiLink l : links) {
            result.add(new ROIData(l.getChild()));
        }
        return result;
    }

    @Override
    public String toString() {
        return getFolderPathString() + " [id=" + getId() + "]";
    }

    /**
     * Returns the folder path as string
     * 
     * @return See above
     */
    public String getFolderPathString() {
        return getFolderPathString(folderPathSeparatorChar);
    }

    /**
     * Returns the folder path as string using a custom path separator
     * 
     * @param pathSeparator
     *            The path separator character
     * @return See above
     */
    public String getFolderPathString(char pathSeparator) {
        if (folderPathString == null
                || folderPathSeparatorChar != pathSeparator) {
            StringBuilder sb = new StringBuilder();
            generateFolderPath(this, sb, pathSeparator);
            folderPathString = sb.toString();
            folderPathSeparatorChar = pathSeparator;
        }
        return folderPathString;
    }

    /**
     * Generates the path string of a {@link FolderData}
     * 
     * @param f
     *            The folder
     * @param sb
     *            The {@link StringBuilder} the path is written to
     * @param pathSeparator
     *            The path separator character
     */
    private void generateFolderPath(FolderData f, StringBuilder sb,
            char pathSeparator) {
        sb.insert(0, f.getName());
        FolderData parent = f.getParentFolder();
        if (parent != null) {
            if (parent.isLoaded()) {
                sb.insert(0, " " + pathSeparator + " ");
                generateFolderPath(f.getParentFolder(), sb, pathSeparator);
            }
        }
    }

}
