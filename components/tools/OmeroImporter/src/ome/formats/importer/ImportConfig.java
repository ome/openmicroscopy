/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import loci.formats.ClassList;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;

import ome.formats.importer.ImportReader;
import ome.formats.importer.util.IniFileLoader;
import omero.model.Dataset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class which configures the Import.
 * 
 * @since Beta4.1
 */
public class ImportConfig {

    public final static String READERS_KEY = "omero.import.readers";

    private final static Log log = LogFactory.getLog(ImportConfig.class);

    private static boolean configured = false;

    /**
     * Main constructor which iterates over all the paths calling
     * {@link #walk(File, Collection)}.
     * 
     * @param paths
     * @param verbose
     * @throws IOException
     */
    public ImportConfig() {

        // Load up the main ini file
        // ini = IniFileLoader.getIniFileLoader(args);
        // ini.updateFlexReaderServerMaps();

        configured = true;
    }

    public String getReadersPath() {
        String readers = System.getProperty(READERS_KEY);
        if (readers == null) {
            String readersDirectory = System.getProperty("user.dir")
                    + File.separator + "config";
            String readersFile = readersDirectory + File.separator
                    + "importer_readers.txt";
            File rFile = new File(readersFile);
            if (rFile.exists()) {
                readers = rFile.getAbsolutePath();
            } else {
                readers = "importer_readers.txt";
            }
        }
        return readers;
    }

    public boolean cancelOnError() {
        return false;
    }

    public void save() {
        throw new UnsupportedOperationException("NYI");
    }

    public void setHostname(String optarg) {
        // TODO Auto-generated method stub

    }

    public void setUsername(String optarg) {
        // TODO Auto-generated method stub

    }

    public void setPassword(String optarg) {
        // TODO Auto-generated method stub

    }

    public void setSessionkey(String optarg) {
        // TODO Auto-generated method stub

    }

    public void setPort(int parseInt) {
        // TODO Auto-generated method stub

    }

    public void setTargetClass(Class class1) {
        // TODO Auto-generated method stub

    }

    public void setTargetId(long parseLong) {
        // TODO Auto-generated method stub

    }

    public void setName(String optarg) {
        // TODO Auto-generated method stub

    }

    public void setDescription(String optarg) {
        // TODO Auto-generated method stub

    }

    public void setContinueOnErrors(boolean b) {
        // TODO Auto-generated method stub

    }

    public void setReadersPath(String optarg) {
        // TODO Auto-generated method stub

    }

    public boolean canLogin() {
        if (((username == null || password == null) && sessionKey == null)
                || hostname == null) {
            return false;
        }
    }

}
