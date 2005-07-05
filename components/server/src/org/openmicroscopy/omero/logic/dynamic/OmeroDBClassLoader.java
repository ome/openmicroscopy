package org.openmicroscopy.omero.logic.dynamic;


// Single ClassLoader with two different Daos!
// -client accesses BinaryService
// -server accesses BinaryDao
class OmeroDBClassLoader extends ClassLoader {
	
	public Class doIt(String name, byte[] code){
		return super.defineClass(null,code,0,code.length);
	}
	
}
