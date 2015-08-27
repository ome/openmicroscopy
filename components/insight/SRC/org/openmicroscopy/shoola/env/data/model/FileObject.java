/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.model;


import ij.IJ;
import ij.ImagePlus;
import ij.io.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import loci.formats.codec.CompressionType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Object hosting the information about the "file" to import.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class FileObject
{

    /** The field identifying the image id.*/
    public static final String OMERO_ID = "Omero_iid";

    /** The field identifying the group id.*/
    public static final String OMERO_GROUP = "Omero_group";

    /** The file to import.
     * This could be a file on disk or an ImageJ object for example.
     */
    private Object file;

    /**
     * Flag indicating if the file is generated or not.
     */
    private boolean generated;

    /** 
     * List of associated files. Mainly for imageJ.
     */
    private List<FileObject> associatedFiles;

    /** The trueFile if available.*/
    private File trueFile;


    /**
     * Returns the Pixels node matching the index.
     *
     * @param doc The document to handle.
     * @return See above.
     */
    private Node getPixelsNode(Document doc)
    {
        
        NodeList l = doc.getElementsByTagName("Image");
        if (l == null || l.getLength() == 0) return null;
        NamedNodeMap attributes;
        String value;
        Node node;
        NodeList nodeList;
        int series = getIndex();
        for (int i = 0; i < l.getLength(); i++) {
            node = l.item(i);
            if (node.hasAttributes()) {
                attributes = node.getAttributes();
                value = attributes.getNamedItem("ID").getNodeValue();
                if (value.equals("Image:"+series)) {
                    nodeList = node.getChildNodes();
                    if (nodeList != null && nodeList.getLength() > 0) {
                        for (int j = 0; j < nodeList.getLength(); j++) {
                            Node n = nodeList.item(j);
                            if ("Pixels".equals(n.getNodeName())) {
                                return n;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Parses the image's description.
     *
     * @param xmlStr The string to parse.
     * @return See above.
     */
    private Document xmlParser(String xmlStr) throws SAXException
    {
        InputSource stream = new InputSource();
        stream.setCharacterStream(new StringReader(xmlStr));
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        DocumentBuilder builder;
        Document doc = null;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = builder.parse(stream);
        } catch (ParserConfigurationException e) {
            e.printStackTrace(pw);
            IJ.log(sw.toString());
        } catch (IOException e) {
            e.printStackTrace(pw);
            IJ.log(sw.toString());
        } finally {
            try {
                sw.close();
            } catch (IOException e) {
                IJ.log("I/O Exception:" + e.getMessage());
            }
            pw.close();
        }
        return doc;
    }

    /**
     * Creates a new instance.
     *
     * @param file The file to import.
     */
    public FileObject(Object file)
    {
        if (file == null)
            throw new IllegalArgumentException("No object to import");
        this.file = file;
    }

    /**
     * Sets the omero image Id after saving the image.
     *
     * @param id The value to set.
     */
    public void setImageID(long id)
    {
        if (!isImagePlus()) return;
        ImagePlus image = (ImagePlus) file;
        image.setProperty(OMERO_ID, id);
    }

    /**
     * Add the associated file if any.
     * 
     * @param file The file to add.
     */
    public void addAssociatedFile(FileObject file)
    {
        if (associatedFiles == null) {
            associatedFiles = new ArrayList<FileObject>();
        }
        if (file != null) {
            associatedFiles.add(file);
        }
    }

    /**
     * Returns the associated files if any or <code>null</code>.
     * 
     * @return See above.
     */
    public List<FileObject> getAssociatedFiles()
    {
        return associatedFiles;
    }

    /**
     * Returns the object to import.
     *
     * @return See above
     */
    public Object getFile() { return file; }

    /**
     * Returns the name of the object to import.
     *
     * @return See above.
     */
    public String getName()
    {
        if (file instanceof File) {
            return ((File) file).getName();
        } else if (file instanceof ImagePlus) {
            ImagePlus img = (ImagePlus) file;
            return img.getTitle();
        }
        return null;
    }

    /**
     * Returns the absolute path to the file.
     *
     * @return See above.
     */
    public String getAbsolutePath()
    {
        if (file instanceof File) {
            return ((File) file).getAbsolutePath();
        } else if (file instanceof ImagePlus) {
            File f = getTrueFile();
            if (f != null) return f.getAbsolutePath();
            return ((ImagePlus) file).getTitle();
        }
        return "";
    }

    /**
     * Returns the file to import.
     * @return See above.
     */
    public File getFileToImport()
    {
        File f = getTrueFile();
        if (f != null) return f;
        if (file instanceof ImagePlus) {
            //prepare command
            ImagePlus img = (ImagePlus) file;
            generated = true;
            try {
                //name w/o extension
                String baseName = FilenameUtils.getBaseName(
                        FilenameUtils.removeExtension(img.getTitle()));
                baseName = CommonsLangUtils.deleteWhitespace(baseName);
                String n = baseName+".ome.tif";
                f = File.createTempFile(img.getTitle(), ".ome.tif");
                File p = f.getParentFile();
                File[] list = p.listFiles();
                if (list != null) {
                    File toDelete = null;
                    for (int i = 0; i < list.length; i++) {
                        if (list[i].getName().equals(n)) {
                            toDelete = list[i];
                            break;
                        }
                    }
                    if (toDelete != null) {
                        toDelete.delete();
                    }
                }
                f = new File(p, n);
                f.deleteOnExit();
            } catch (Exception e) {
                return null;
            }
            StringBuffer buffer = new StringBuffer();
            buffer.append("outfile="+f.getAbsolutePath());
            buffer.append(" splitz=false");
            buffer.append(" splitc=false");
            buffer.append(" splitt=false");
            buffer.append(" saveroi=false");
            buffer.append(" compression="+CompressionType.UNCOMPRESSED.getCompression());
            buffer.append(" imageid="+img.getID()+" ");
            IJ.runPlugIn("loci.plugins.LociExporter", buffer.toString());
            return f;
        }
        return null;
    }

    /**
     * Returns <code>true</code> if the file has been generated,
     * <code>false</code> otherwise.
     *
     * @return See above.
     */
    public boolean isGenerated() { return generated; }

    /**
     * Returns <code>true</code> if it is a new image from ImageJ,
     * <code>false</code> otherwise.
     *
     * @return See above.
     */
    public boolean isNewImage()
    {
        if (file instanceof ImagePlus) {
            ImagePlus img = (ImagePlus) file;
            if (img.changes) return true;
            FileInfo info = img.getOriginalFileInfo();
            if (info == null) {
                info = img.getFileInfo();
                String name = info.fileName;
                if (CommonsLangUtils.isBlank(name) || "Untitled".equals(name))
                    return true;
            }
            String xmlStr = info.description;
            if (CommonsLangUtils.isBlank(xmlStr)) return false;
          //Get Current Dimensions
            int sizeC_cur = img.getNChannels();
            int sizeT_cur = img.getNFrames();
            int sizeZ_cur = img.getNSlices();
            int sizeC_org = sizeC_cur;
            int sizeT_org = sizeT_cur;
            int sizeZ_org = sizeZ_cur;
            Document doc = null;
            boolean xml = false;
            try {
                if (xmlStr.startsWith("<")) {
                    doc = xmlParser(xmlStr);
                }
            } catch (SAXException e) { //not XML or not possible to read it correctly
                xml = false;
            }
            if (!xml) {
              //try to parse the string
                String[] values = xmlStr.split("\n");
                String v;
                for (int i = 0; i < values.length; i++) {
                    v = values[i];
                    if (v.startsWith("slices")) {
                        String[] keys = v.split("=");
                        if (keys.length > 1) {
                            sizeZ_org = Integer.valueOf(keys[1]);
                        }
                    } else if (v.startsWith("channels")) {
                        String[] keys = v.split("=");
                        if (keys.length > 1) {
                            sizeC_org = Integer.valueOf(keys[1]);
                        }
                    } else if (v.startsWith("frames")) {
                        String[] keys = v.split("=");
                        if (keys.length > 1) {
                            sizeT_org = Integer.valueOf(keys[1]);
                        }
                    }
                }
            }
            if (doc != null) { 
                Node node = getPixelsNode(doc);
                if (node == null) return false;
                NamedNodeMap nnm = node.getAttributes();
                sizeC_org = Integer.valueOf(nnm.getNamedItem("SizeC").getNodeValue());
                sizeT_org = Integer.valueOf(nnm.getNamedItem("SizeT").getNodeValue());
                sizeZ_org = Integer.valueOf(nnm.getNamedItem("SizeZ").getNodeValue());
            }
            if (sizeC_cur != sizeC_org || sizeT_cur != sizeT_org ||
                    sizeZ_cur != sizeZ_org) {
                return true;
            }
        }
        return false;
    }

   

    /**
     * Returns the file to import.
     *
     * @return See above.
     */
    public File getTrueFile()
    {
        if (file instanceof File) {
            return (File) file;
        } else if (file instanceof ImagePlus) {
            if (trueFile != null) return trueFile;
            ImagePlus img = (ImagePlus) file;
            if (!img.changes) {
                FileInfo info = img.getOriginalFileInfo();
                if (info != null) {
                    if (CommonsLangUtils.isNotEmpty(info.url)) {
                        //create a tmp file and copy the URL
                        String fname = img.getTitle();
                        String extension = FilenameUtils.getExtension(fname);
                        String baseName = FilenameUtils.getBaseName(
                                FilenameUtils.removeExtension(fname));
                        try {
                            trueFile = File.createTempFile(baseName,
                                    "."+extension);
                            trueFile.deleteOnExit();
                            FileUtils.copyURLToFile(new URL(info.url), trueFile);
                        } catch (Exception e) {
                            //ignore.
                        }
                        return trueFile;
                    }
                    if (info.directory != null && info.fileName != null) {
                        trueFile = new File(info.directory, info.fileName);
                        return trueFile;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the name of the parent file if it exists.
     *
     * @return See above.
     */
    public String getParentName()
    {
        File f = getTrueFile();
        if (f == null || f.getParentFile() == null) return null;
        return f.getParentFile().getName();
    }

    /**
     * Returns the size of the file
     * 
     * @return See above.
     */
    public long getLength()
    {
        File f;
        if (file instanceof File) {
            f = (File) file;
            if (f.isFile()) return f.length();
            return FileUtils.sizeOfDirectory(f);
        } else if (file instanceof ImagePlus) {
            f = getTrueFile();
            if (f != null) return f.length();
        }
        return 0;
    }

    /**
     * Returns <code>true</code> if it is a directory, <code>false</code>
     * otherwise.
     *
     * @return See above.
     */
    public boolean isDirectory()
    {
        if (file instanceof File) {
            File f = (File) file;
            return f.isDirectory();
        }
        return false;
    }

    /**
     * Returns <code>true</code> if it is a file, <code>false</code>
     * otherwise.
     *
     * @return See above.
     */
    public boolean isFile()
    {
        if (file instanceof File) {
            File f = (File) file;
            return f.isFile();
        }
        return true;
    }

    /**
     * Returns <code>true</code> if it is an image from ImageJ,
     * <code>false</code> otherwise.
     *
     * @return See above.
     */
    public boolean isImagePlus() { return file instanceof ImagePlus; }

    /**
     * Returns the index of the image if it is an image plus.
     *
     * @return
     */
    public int getIndex()
    {
        if (!isImagePlus()) return -1;
        ImagePlus image = (ImagePlus) file;
        Object value = image.getProperty("Series");
        if (value != null && value instanceof Integer)
            return ((Integer) value).intValue();
        return -1;
    }

    /**
     * Returns the <code>OMERO</code> id or <code>-1</code> if not set.
     *
     * @return See above.
     */
    public long getOMEROID()
    {
        if (!isImagePlus()) return -1;
        ImagePlus image = (ImagePlus) file;
        Object value = image.getProperty(OMERO_ID);
        if (value != null && value instanceof Long)
            return ((Long) value).longValue();
        return -1;
    }

    /**
     * Returns the <code>OMERO</code> group id or <code>-1</code> if not set.
     *
     * @return See above.
     */
    public long getGroupID()
    {
        if (!isImagePlus()) return -1;
        ImagePlus image = (ImagePlus) file;
        Object value = image.getProperty(OMERO_GROUP);
        if (value != null && value instanceof Long)
            return ((Long) value).longValue();
        return -1;
    }

    /**
     * Returns the name as container if option is on.
     *
     * @return See above.
     */
    public String getFolderAsContainerName()
    {
        File parentFile;
        if (file instanceof File) {
            File f = (File) file;
            if (f.isFile()) {
                parentFile = f.getParentFile();
                if (parentFile == null)
                    return null;
                return parentFile.getName();
            }
            return f.getName();
        } else {
            File f = getTrueFile();//image plus
            if (f != null && f.getParentFile() != null)
                return f.getParentFile().getName();
            return getName();
        }
    }
}
