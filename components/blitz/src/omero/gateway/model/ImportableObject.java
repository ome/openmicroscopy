/*
 * org.openmicroscopy.shoola.env.data.model.ImportableObject 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.in.OMEXMLReader;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import omero.gateway.SecurityContext;
import omero.gateway.util.TIFFFilter;
import omero.gateway.util.Utils;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;

/**
 * Helper class where parameters required for the imports are stored.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ImportableObject
{
    public static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd-MM-yyyy");;

    /** The default name for the dataset. */
    public static final String DEFAULT_DATASET_NAME;

    /** 
     * The collection of HCS files extensions to check before importing.
     */
    public static final Set<String> HCS_FILES_EXTENSION;

    /** 
     * The collection of arbitrary files extensions to check
     * before importing. If a file has one of the extensions, we need
     * to check the import candidates.
     */
    private static final List<String> ARBITRARY_FILES_EXTENSION;

    /** 
     * The collection of HCS format.
     */
    public static final List<String> HCS_DOMAIN;

    /** 
     * The collection of OME suffices.
     */
    public static final List<String> OME_SUFFIXES;

    /** The filter used to exclude extensions.*/
    public static final TIFFFilter FILTER;

    /** The <code>dat</code> extension.*/
    public static final String DAT_EXTENSION = "dat";

    static {
        FILTER = new TIFFFilter();
        DEFAULT_DATASET_NAME = DATE_FORMAT.format(new Date());
        HCS_FILES_EXTENSION = new HashSet<String>();
        HCS_DOMAIN = new ArrayList<String>();

        ImageReader r = new ImageReader();
        IFormatReader[] allReaders = r.getReaders();
        try {
            for (IFormatReader reader : allReaders) {
                if (Arrays.asList(reader.getPossibleDomains("")).contains(
                        FormatTools.HCS_DOMAIN)) {
                    populateExtensions(reader.getSuffixes());
                    HCS_DOMAIN.add(reader.getFormat());
                }
                reader.close();
            }
        } catch (Exception e) {}
        finally {
            try {
                r.close();
            } catch (Exception ex) {}
        }


        IFormatReader reader = new OMEXMLReader();
        OME_SUFFIXES = (List<String>) Arrays.asList(reader.getSuffixes());

        ARBITRARY_FILES_EXTENSION = new ArrayList<String>();
        ARBITRARY_FILES_EXTENSION.add("text");
        ARBITRARY_FILES_EXTENSION.add("txt");
        ARBITRARY_FILES_EXTENSION.add("xml");
        ARBITRARY_FILES_EXTENSION.add("exp");
        ARBITRARY_FILES_EXTENSION.add("log");
        ARBITRARY_FILES_EXTENSION.add("ini");
        ARBITRARY_FILES_EXTENSION.add("dat");
        ARBITRARY_FILES_EXTENSION.add(TIFFFilter.TIFF);
        ARBITRARY_FILES_EXTENSION.add(TIFFFilter.TIF);
        try {
            reader.close();
        } catch (Exception e) {//do not record
        }
    }

    /**
     * Adds the specified suffixes to the list.
     * 
     * @param suffixes The values to handle.
     */
    private static void populateExtensions(String[] suffixes)
    {
        if (suffixes != null) {
            String s;
            for (int i = 0; i < suffixes.length; i++) {
                s = suffixes[i];
                if (s != null && s.trim().length() > 0)
                    HCS_FILES_EXTENSION.add(s.toLowerCase());
            }
        }
    }

    /**
     * Returns <code>true</code> if the extension of the specified file
     * is arbitrary and so requires to use the import candidates,
     * <code>false</code> otherwise.
     *
     * @param f The file to handle.
     * @return See above.
     */
    public static boolean isArbitraryFile(File f)
    {
        if (f == null) return false;
        String name = f.getName();
        if (!name.contains(".")) return false; 	
        String ext = FilenameUtils.getExtension(name);
        return ARBITRARY_FILES_EXTENSION.contains(ext);
    }

    /** The collection of files to import. */
    private List<ImportableFile> files;

    /** The depth when the name is overridden. */
    private int depthForName;

    /** The depth used when scanning a folder. */
    private int scanningDepth;

    /**
     * Flag indicating to override the name set by B-F when importing the data. 
     */
    private boolean overrideName;

    /** The collection of tags. */
    private Collection<TagAnnotationData> tags;

    /** The array containing pixels size.*/
    private double[] pixelsSize;

    /** The type to create if the folder has to be saved as a container. */
    private Class type;

    /** Flag indicating to load the thumbnails. */ 
    private boolean loadThumbnail;

    /** The nodes of reference. */
    private List<Object> refNodes;

    /** The collection of new objects. */
    private List<DataObject> newObjects;

    /** The collection of new object. */
    private Map<Long, List<DatasetData>> projectDatasetMap;

    /**
     * Returns the name of the object.
     *
     * @param object The object to handle.
     * @return See above.
     */
    private String getObjectName(DataObject object)
    {
        if (object instanceof DatasetData) {
            return ((DatasetData) object).getName();
        }
        if (object instanceof ProjectData) {
            return ((ProjectData) object).getName();
        }
        if (object instanceof ScreenData) {
            return ((ScreenData) object).getName();
        }
        return "";
    }

    /**
     * Creates a new instance.
     *
     * @param files The collection of files to import.
     * @param overrideName Pass <code>true</code> to override the name of the 
     *                     file set while importing the data,
     *                     <code>false</code> otherwise.
     */
    public ImportableObject(List<ImportableFile> files, boolean overrideName)
    {
        this.files = files;
        this.overrideName = overrideName;
        type = DatasetData.class;
        depthForName = -1;
        loadThumbnail = true;
        newObjects = new ArrayList<DataObject>();
        projectDatasetMap = new HashMap<Long, List<DatasetData>>();
    }

    /**
     * Sets to <code>true</code> if the thumbnail has to be loaded when 
     * the image is imported, <code>false</code> otherwise.
     *
     * @param loadThumbnail Pass <code>true</code> to load the thumbnail when 
     *                      the image is imported, <code>false</code> otherwise.
     */
    public void setLoadThumbnail(boolean loadThumbnail)
    {
        this.loadThumbnail = loadThumbnail;
    }

    /**
     * Returns <code>true</code> if the thumbnail has to be loaded when 
     * the image is imported, <code>false</code> otherwise.
     * @return See above.
     */
    public boolean isLoadThumbnail() { return loadThumbnail; }

    /**
     * Sets the type to use when creating a folder as container.
     *
     * @param type The type to use.
     */
    public void setType(Class type) { this.type = type; }

    /**
     * Sets the default size of the pixels if the value is not found.
     *
     * @param pixelsSize The value to set.
     */
    public void setPixelsSize(double[] pixelsSize)
    { 
        this.pixelsSize = pixelsSize;
    }

    /** 
     * Sets the collection of tags.
     *
     * @param tags The tags to use.
     */
    public void setTags(Collection<TagAnnotationData> tags)
    {
        this.tags = tags;
    }

    /**
     * Sets the depth used scanning a folder.
     * 
     * @param scanningDepth The value to set.
     */
    public void setScanningDepth(int scanningDepth)
    {
        this.scanningDepth = scanningDepth;
    }

    /**
     * Returns the depth used scanning a folder.
     *
     * @return See above.
     */
    public int getScanningDepth() { return scanningDepth; }

    /**
     * Sets the depth used when the name is overridden.
     *
     * @param depthForName The value to set.
     */
    public void setDepthForName(int depthForName)
    {
        this.depthForName = depthForName;
    }

    /**
     * Returns the depth used when the name is overridden.
     *
     * @return See above.
     */
    public int getDepthForName() { return depthForName; }

    /**
     * Returns the collection of files to import.
     *
     * @return See above.
     */
    public List<ImportableFile> getFiles() { return files; }

    /**
     * Returns the <code>DataObject</code> corresponding to the folder 
     * be saved as a container.
     *
     * @param file The file to handle.
     * @return See above.
     */
    public DataObject createFolderAsContainer(ImportableFile file)
    {
        return createFolderAsContainer(file, false);
    }

    /**
     * Returns the <code>DataObject</code> corresponding to the folder 
     * be saved as a container.
     *
     * @param file The file to handle.
     * @param hcs Pass <code>true</code> to indicate that the folder 
     * to create is for HCS data, <code>false</code> otherwise.
     * @return See above.
     */
    public DataObject createFolderAsContainer(ImportableFile file, boolean hcs)
    {
        if (file == null) return null;
        Class<?> klass = type;
        if (hcs) klass = ScreenData.class;
        File f = file.getFile();
        boolean b = file.isFolderAsContainer();
        if (!b) return null;
        String name = Utils.getFolderAsContainerName(f);
        if (StringUtils.isBlank(name)) return null;
        if (DatasetData.class.equals(klass)) {
            DatasetData dataset = new DatasetData();
            dataset.setName(name);
            return dataset;
        } else if (ScreenData.class.equals(klass)) {
            ScreenData screen = new ScreenData();
            screen.setName(name);
            return screen;
        }
        return null;
    }

    /**
     * Returns the root type used when creating the object.
     *
     * @return See above.
     */
    public Class getRootType()
    { 
        if (ScreenData.class.equals(type))
            return type;
        return ProjectData.class;
    }

    /** 
     * Returns <code>true</code> if the name set while importing the data
     * has to be overridden, <code>false</code> otherwise.
     *
     * @return See above.
     */
    public boolean isOverrideName() { return overrideName; }

    /** 
     * Returns the pixels size to use of the value is not found in the 
     * file.
     *
     * @return See above.
     */
    public Double[] getPixelsSize()
    { 
        if (pixelsSize != null && pixelsSize.length > 0) {
            Double[] array = new Double[pixelsSize.length];
            for (int i = 0; i < pixelsSize.length; i++)
                array[i] = new Double(pixelsSize[i]);
            return array;
        }
        return null; 
    }

    /**
     * Returns the collection of tags.
     *
     * @return See above.
     */
    public Collection<TagAnnotationData> getTags() { return tags; }

    /**
     * Returns <code>true</code> if new tags were created, <code>false</code>
     * otherwise.
     *s
     * @return See above.
     */
    public boolean hasNewTags()
    {
        if (CollectionUtils.isEmpty(tags)) {
            return false;
        }
        Iterator<TagAnnotationData> i = tags.iterator();
        TagAnnotationData tag;
        while (i.hasNext()) {
            tag = i.next();
            if (tag.getId() <= 0) return true;
        }
        return false;
    }

    /**
     * Returns the nodes of reference.
     *
     * @return See above.
     */
    public List<Object> getRefNodes() { return refNodes; }

    /**
     * Returns the nodes of reference.
     *
     * @param refNodes The value to set.
     */
    public void setRefNodes(List<Object> refNodes) { this.refNodes = refNodes; }

    /**
     * Returns <code>true</code> if the extension of the specified file
     * is a HCS files, <code>false</code> otherwise.
     *
     * @param f The file to handle.
     * @return See above.
     */
    public static boolean isHCSFile(File f)
    {
        if (f == null) return false;
        String path = f.getAbsolutePath();
        if (FILTER.accept(path)) return false;
        String name = path;
        if (!name.contains(".")) return false; 
        String ext = name.substring(name.lastIndexOf('.')+1, name.length());
        if (ext == null) return false;
        return HCS_FILES_EXTENSION.contains(ext.toLowerCase());
    }

    /**
     * Returns <code>true</code> if the passed format is a HCS format,
     * <code>false</code> otherwise.
     * 
     * @param format The format to handle.
     * @return See above.
     */
    public static boolean isHCSFormat(String format)
    {
        Iterator<String> i = HCS_DOMAIN.iterator();
        while (i.hasNext()) {
            if (format.contains(i.next()))
                return true;
        }
        return false;
    }

    /**
     * Returns code <code>true</code> if the specified file is an
     * <code>OME</code> file, <code>false</code> otherwise.
     *
     * @param file The file to handle.
     * @return See above.
     */
    public static boolean isOMEFile(File file)
    {
        if (file == null) return false;
        String name = file.getName();
        if (name == null) return false;
        Iterator<String> i = OME_SUFFIXES.iterator();
        name = name.toLowerCase();
        String s;
        while (i.hasNext()) {
            s = i.next().toLowerCase();
            if (name.endsWith(s))
                return true;
        }
        return false;
    }

    /**
     * Returns <code>true</code> if new objects have to be created,
     * <code>false</code> otherwise.
     *
     * @return See above.
     */
    public boolean hasNewObjects()
    {
        int size = newObjects.size();
        if (size > 0) return true;
        return projectDatasetMap.size() > 0;
    }

    /**
     * Adds a new object.
     *
     * @param object The object to add.
     */
    public void addNewDataObject(DataObject object)
    {
        if (object != null) newObjects.add(object);
    }

    /**
     * Returns the object if it has already been created in the specified
     * context, <code>null</code> otherwise.
     *
     * @param object The object to check.
     * @param ctx The security context.
     * @return See above.
     */
    public DataObject hasObjectBeenCreated(DataObject object,
            SecurityContext ctx)
    {
        if (object == null) return null;
        Iterator<DataObject> i = newObjects.iterator();
        DataObject data;
        String name = getObjectName(object);
        String n;
        while (i.hasNext()) {
            data = i.next();
            n = getObjectName(data);
            if (data.getClass().equals(object.getClass()) && n.equals(name)) {
                if (data.getGroupId() == ctx.getGroupID())
                    return data;
            }
        }
        return null;
    }

    /**
     * Returns the dataset if already created.
     *
     * @param projectID The id of the project.
     * @param dataset The dataset to register.
     * @return See above.s
     */
    public DatasetData isDatasetCreated(long projectID, DatasetData dataset)
    {
        List<DatasetData> datasets = projectDatasetMap.get(projectID);
        if (CollectionUtils.isEmpty(datasets)) return null;
        Iterator<DatasetData> i = datasets.iterator();
        DatasetData data;
        String name = dataset.getName();
        while (i.hasNext()) {
            data = i.next();
            if (data.getName().equals(name))
                return data;
        }
        return null;
    }

    /**
     * Registers the dataset.
     * 
     * @param projectID The id of the project.
     * @param dataset The dataset to register.
     */
    public void registerDataset(long projectID, DatasetData dataset)
    {
        if (dataset == null) return;
        List<DatasetData> datasets = projectDatasetMap.get(projectID);
        if (datasets == null) {
            datasets = new ArrayList<DatasetData>();
            projectDatasetMap.put(projectID, datasets);
        }
        datasets.add(dataset);
    }

    /**
     * Returns <code>true</code> if the file is already in the list of files
     * to import e.g. the file and the companion file are selected, 
     * <code></code> otherwise.
     * @param value The path to the file.
     * @return See above.
     */
    public boolean isFileinQueue(String value)
    {
        Iterator<ImportableFile> i = files.iterator();
        ImportableFile f;
        while (i.hasNext()) {
            f = i.next();
            if (f.getFile().getAbsolutePath().equals(value))
                return true;
        }
        return false;
    }

    /**
     * Resets the files to re-upload
     *
     * @param files The files to reupload.
     */
    public void reUpload(List<ImportableFile> files)
    {
        if (CollectionUtils.isEmpty(files)) return;
        this.files = files;
    }

}
