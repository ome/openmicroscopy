/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import java.util.List;

import omero.model.IObject;
import omero.model.Image;
import omero.grid.FileSet;

/**
 *  A config class used to control listing
 *
 * @since Beta4.1
 */
public class FileSetI extends FileSet {
    
    FileSetI() {
        importableImage = false;
        fileName = "";
	    imageName = "";
	    reader = "";
        imageCount = 0;
        usedFiles = null;
        imageList = null;
    }
    
    public void setImportableImage(boolean importableImage) {
        importableImage = importableImage;
    }
    
    public boolean getImportableImage() {
        return importableImage;
    }

    public void setImageName(String imageName) {
        imageName = imageName;
    }
    
    public String getImageName() {
        return imageName;
    }

    public void setFileName(String fileName) {
        fileName = fileName;
    }
    
    public String getReader() {
        return reader;
    }

    public void setReader(String reader) {
        reader = reader;
    }
    
    public String getFileName() {
        return fileName;
    }

    public void setUsedFiles(List<IObject> usedFiles) {
        usedFiles = usedFiles;
    }
    
    public List<IObject> getUsedFiles() {
        return usedFiles;
    }

    public void setImageCount(int imageCount) {
        imageCount = imageCount;
    }
    
    public int getImageCount() {
        return imageCount;
    }

    public void setImageList(List<Image> imageList) {
        imageList = imageList;
    }
    
    public List<Image> getImageList() {
        return imageList;
    }


}

