/*
 * ome.services.blitz.repo.FileSetI
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
 *
 * 
 */
package ome.services.blitz.repo;

import java.util.List;

import omero.model.IObject;
import omero.model.Image;
import omero.grid.FileSet;

/**
 *  A container for returned file sets
 *
 * @author Colin Blackburn <cblackburn at dundee dot ac dot uk>
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

