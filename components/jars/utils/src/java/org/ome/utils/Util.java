/*
 * Created on Feb 8, 2005
 */
package org.ome.utils;

/**
 * maps XSD types to Java classes
*/
public class Util {

	/** returns a first-letter capitalized version of
	 * a string for getters and setters
	 * @param str
	 * @return upper case string
	 */
	public static String uc(String str){
		return str.substring(0,1).toUpperCase()+str.substring(1);
	}

}