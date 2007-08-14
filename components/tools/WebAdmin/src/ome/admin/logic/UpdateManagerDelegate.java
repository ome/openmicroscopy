package ome.admin.logic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ome.admin.validator.FileValidator;

public class UpdateManagerDelegate {

	private String path = null;
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public List<String> getFiles() {
		File f = new File(this.path);
		File [] files = null;
		List<String> list = new ArrayList<String>();
		
		if(f.isDirectory()) {
			files = f.listFiles();
			for ( int i=0; i<files.length; i++ ) {
				if( files[i].isFile() && FileValidator.validFileName(files[i].getName())) 
					list.add(files[i].getName());
			}
		}
		return list;
	}
	
	public List<String> getDirs() {
		File f = new File(this.path);
		File [] dirs = null;
		List<String> list = new ArrayList<String>();
		
		if(f.isDirectory()) {
			dirs = f.listFiles();
			for ( int i=0; i<dirs.length; i++ ) {
				if(dirs[i].isDirectory()) 
					list.add(dirs[i].getName());
			}
		}
		return list;	
	}

}
