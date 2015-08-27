/*
 * org.openmicroscopy.shoola.env.data.model.ScriptObject 
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.model;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.RType;
import omero.grid.JobParams;
import omero.grid.Param;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;

/** 
 * Hosts the information about a given script.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ScriptObject
{

    /** 
     * The character used to separate parameter composed of more than
     * one word at the scripting level.
     */
    public static final String PARAMETER_SEPARATOR = "_";

    /** 
     * The character used to separate parameter composed of more than
     * one word at the UI level.
     */
    public static final String PARAMETER_UI_SEPARATOR = " ";

    /** Path to the <code>Figure</code> script. */
    public static final String FIGURE_PATH = "/omero/figure_scripts/";

    /** Path to the <code>Export</code> script. */
    public static final String EXPORT_PATH = "/omero/export_scripts/";

    /** Path to the <code>Region</code> script. */
    public static final String REGION_PATH = "/omero/region_scripts/";

    /** Path to the <code>Import</code> script. */
    public static final String IMPORT_PATH = "/omero/import_scripts/";

    /** Path to the <code>Util</code> script. */
    public static final String UTIL_PATH = "/omero/util_scripts/";

    /** Path to the <code>Setup</code> script. */
    public static final String SETUP_PATH = "/omero/setup_scripts/";

    /** Path to the <code>Setup</code> script. */
    public static final String ANALYSIS_PATH = "/omero/analysis_scripts/";

    /** Indicates that the script is a <code>Export</code> script. */
    public static final int EXPORT = 0;

    /** Indicates that the script is a <code>Figure</code> script. */
    public static final int FIGURE = 1;

    /** Indicates that the script is a <code>Region</code> script. */
    public static final int REGION = 2;

    /** Indicates that the script is a <code>Util</code> script. */
    public static final int UTIL = 3;

    /** Indicates that the script is a <code>Import</code> script. */
    public static final int IMPORT = 4;

    /** Indicates that the script is a <code>Region</code> script. */
    public static final int OTHER = 5;

    /** The default version. */
    private static final String VERSION = "1.0";

    /** Tag identifying the data types.*/
    private static final String DATA_TYPE = "Data_Type";

    /** Identifies the <code>Image</code> type.*/
    private static final String IMAGE_TYPE = "image";

    /** Identifies the <code>Dataset</code> type.*/
    private static final String DATASET_TYPE = "dataset";

    /** Identifies the <code>Project</code> type.*/
    private static final String PROJECT_TYPE = "project";

    /** Identifies the <code>Screen</code> type.*/
    private static final String SCREEN_TYPE = "screen";

    /** Identifies the <code>Plate</code> type.*/
    private static final String PLATE_TYPE = "plate";

    /** The possible keys to identify the object's identifiers.*/
    private static final List<String> IDENTIFIER_KEYS;

    static {
        IDENTIFIER_KEYS = new ArrayList<String>();
        IDENTIFIER_KEYS.add("id");
        IDENTIFIER_KEYS.add("ids");
        IDENTIFIER_KEYS.add("image_id");//ij support.
    }

    /** The id of the script. */
    private long scriptID;

    /** The name of the script. */
    private String name;

    /** The full path to the script when stored in server. */
    private String path;

    /** The description of the script. */
    private String description;

    /** The description of the script. */
    private String journalRef;

    /** The e-mail address of the contact. */
    private String contact;

    /** The version of the script. */
    private String version;

    /** The input parameters. */
    private Map<String, ParamData> inputs;

    /** The output parameters. */
    private Map<String, ParamData> outputs;

    /** The 16x16 icon associated to the script. */
    private Icon icon;

    /** The 48x48 icon associated to the script. */
    private Icon iconLarge;

    /** Hold the parameters related to the script. */
    private JobParams parameters;

    /** The MIME type of the script if set. */ 
    private String mimeType;

    /** The category of the script. */
    private int category;

    /** The folder. */
    private String folder;

    /** 
     * Flag indicating if the script is an official script i.e.
     * release by the team or not.
     */
    private boolean official;

    /** The specified data types if set e.g. image.*/
    private List<Class<?>> dataTypes;

    /** The id of the group the object to the script on belong to.*/
    private long groupID;

    /**
     * Converts the specified values to the corresponding class.
     * 
     * @param values The values to handle.
     */
    private void populateDataTypes(List<Object> values)
    {
        if (CollectionUtils.isEmpty(values)) return;
        dataTypes = new ArrayList<Class<?>>();
        Iterator<Object> i = values.iterator();
        Object object;
        String value;
        while (i.hasNext()) {
            object = i.next();
            if (object instanceof String) {
                value = (String) object;
                value = value.toLowerCase();
                dataTypes.add(convertDataType(value));
            }
        }
    }

    /** Converts the parameters. */
    private void convertJobParameters()
    {
        if (parameters == null) return;
        //Convert authors if 
        String[] authors = parameters.authors;
        if (authors != null && authors.length > 0) {
            ExperimenterData exp;
            for (int i = 0; i < authors.length; i++) {
                exp = new ExperimenterData();
                exp.setLastName(authors[i]);
            }
        }
        Map<String, Param> map = parameters.inputs;
        Entry<String, Param> entry;
        Iterator<Entry<String, Param>> i;
        Param p;
        inputs = new HashMap<String, ParamData>();
        if (map != null) {
            i = map.entrySet().iterator();
            String key;
            ParamData param;
            while (i.hasNext()) {
                entry = i.next();
                p = entry.getValue();
                key = entry.getKey();
                param = new ParamData(p);
                inputs.put(key, param);
                if (DATA_TYPE.equals(key)) {
                    populateDataTypes(param.getValues());
                }
            }
        }
        map = parameters.outputs;
        outputs = new HashMap<String, ParamData>();
        if (map != null) {
            i = map.entrySet().iterator();
            while (i.hasNext()) {
                entry = i.next();
                p = entry.getValue();
                outputs.put(entry.getKey(), new ParamData(p));
            }
        }
    }

    /** Sets the category of the scripts. */
    private void setCategory()
    {
        if (FIGURE_PATH.equals(path))
            category = FIGURE;
        else if (EXPORT_PATH.equals(path))
            category = EXPORT;
        else if (REGION_PATH.equals(path))
            category = REGION;
        else if (UTIL_PATH.equals(path))
            category = UTIL;
        else category = OTHER;
    }

    /**
     * Creates a new instance.
     * 
     * @param scriptID The id of the script if uploaded.
     * @param path The path of the script when stored in server
     */
    public ScriptObject(long scriptID, String path, String name)
    {
        this.scriptID = scriptID;
        this.path = path;
        this.name = name;
        setCategory();
        description = "";
        journalRef = "";
        mimeType = null;
        official = true;
        groupID = -1;
    }

    /**
     * Sets to <code>true</code> if the script is an official script i.e.
     * release by the team, <code>false</code> otherwise.
     * 
     * @param official Pass <code>true</code> if it is an official script,
     * 				   <code>false</code> otherwise.
     */
    public void setOfficial(boolean official) { this.official = official; }

    /**
     * Returns <code>true</code> if it is an official script,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isOfficialScript() { return official; }

    /** 
     * Returns the label associated to the script.
     * 
     * @return See above.
     */
    public String getScriptLabel() { return UIUtilities.toUnix(path+name); }

    /**
     * Returns the absolute path to the script.
     * 
     * @return See above.
     */
    public String getPath() { return path; }

    /** 
     * Returns the folder.
     * 
     * @return See above.
     */
    public String getFolder()
    { 
        if (CommonsLangUtils.isEmpty(folder)) return getPath();
        return File.separator+folder+File.separator+name;
    }

    /**
     * Sets the folder.
     * 
     * @param folder The value to set.
     */
    public void setFolder(String folder) { this.folder = folder; }

    /**
     * Returns the parameters.
     * 
     * @param parameters The parameters to set.
     */
    public void setJobParams(JobParams parameters)
    {
        this.parameters = parameters;
        convertJobParameters();
    }

    /**
     * Sets the description of the script.
     * 
     * @param description The value to set.
     */
    public void setDescription(String description)
    {
        this.description = description; 
    }

    /**
     * Sets the reference to the journal where the script was published
     * if it has been published.
     * 
     * @param journalRef The value to set.
     */
    public void setJournalRef(String journalRef)
    { 
        this.journalRef = journalRef; 
    }

    /** 
     * Returns the authors of the script.
     * 
     * @return See above
     */
    public String[] getAuthors()
    { 
        if (parameters != null) return parameters.authors; 
        return null;
    }

    /**
     * Returns the description of the script.
     * 
     * @return See above.
     */
    public String getDescription()
    { 
        if (parameters != null) return parameters.description;
        return description;
    }

    /**
     * Returns the journal where the script was published if
     * it has been published.
     * 
     * @return See above.
     */
    public String getJournalRef() { return journalRef; }

    /**
     * Returns the id of the script.
     * 
     * @return See above.
     */
    public long getScriptID() { return scriptID; }

    /**
     * Returns the name of the script.
     * 
     * @return See above.
     */
    public String getName() { return name; }

    /**
     * Returns the UI name of the script.
     * 
     * @return See above.
     */
    public String getDisplayedName()
    {
        int lastDotIndex = name.lastIndexOf('.');
        if (lastDotIndex < 0) return name;
        String value = name.substring(0, lastDotIndex);
        if (!value.contains(PARAMETER_SEPARATOR)) return value;
        return value.replace(PARAMETER_SEPARATOR, PARAMETER_UI_SEPARATOR);
    }

    /**
     * Sets the icon.
     * 
     * @param icon The icon associated to the script.
     */
    public void setIcon(Icon icon) { this.icon = icon; }

    /**
     * Sets the 48x48 icon.
     * 
     * @param icon The icon associated to the script.
     */
    public void setIconLarge(Icon icon) { this.iconLarge = icon; }

    /**
     * Returns the icon associated to the script.
     * 
     * @return See above.
     */
    public Icon getIcon() { return icon; }

    /**
     * Returns the 48x48 icon associated to the script.
     * 
     * @return See above.
     */
    public Icon getIconLarge() { return iconLarge; }

    /** 
     * Returns the main contact.
     * 
     * @return See above.
     */
    public String getContact()
    { 
        if (parameters != null) return parameters.contact;
        return contact;
    }

    /**
     * Sets the details of the main contact.
     * 
     * @param contact The details of the contact.
     */
    public void setContact(String contact) { this.contact = contact; }

    /**
     * Returns the version of the script.
     * 
     * @return See above.
     */
    public String getVersion()
    { 
        if (parameters != null) return parameters.version;
        version = VERSION;
        return version; 
    }

    /**
     * Returns the inputs.
     * 
     * @return See above.
     */
    public Map<String, ParamData> getInputs() { return inputs; }

    /**
     * Returns the parameters to pass when running the script.
     * 
     * @return See above.
     */
    public Map<String, RType> getValueToPass()
    {
        Map<String, RType> map = new HashMap<String, RType>();
        if (inputs == null) return map;
        Iterator<Entry<String, ParamData>> i = inputs.entrySet().iterator();
        ParamData p;
        Entry<String, ParamData> entry;
        RType type;
        while (i.hasNext()) {
            entry = i.next();
            p = entry.getValue();
            type = p.getValueToPassAsRType();
            if (type != null)
                map.put(entry.getKey(), type);
        }
        return map;
    }

    /**
     * Returns <code>true</code> if all the required values are populated,
     * <code>false</code> otherwise.
     * 
     * @return See above
     */
    public boolean allRequiredValuesPopulated()
    {
        if (inputs == null) return true;
        Iterator<Entry<String, ParamData>> i = inputs.entrySet().iterator();
        ParamData p;
        Entry<String, ParamData> entry;
        RType type;
        while (i.hasNext()) {
            entry = i.next();
            p = entry.getValue();
            if (!p.isOptional()) {
                type = p.getValueToPassAsRType();
                if (type == null) return false;
            }
        }
        return true;
    }

    /**
     * Returns <code>true</code> if the parameters have been loaded,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isParametersLoaded() { return parameters != null; }

    /**
     * Returns the MIME type.
     * 
     * @return See above.
     */
    public String getMIMEType() { return mimeType; }

    /**
     * Sets the MIME type.
     * 
     * @param mimeType The value to set.
     */
    public void setMIMEType(String mimeType) { this.mimeType = mimeType; }

    /**
     * Returns the category of the script.
     * 
     * @return See above.
     */
    public int getCategory() { return category; }

    /**
     * Returns the parameters.
     * 
     * @return See above.
     */
    public JobParams getParameters() { return parameters; }

    /**
     * Returns the data types supported if specified.
     * 
     * @return See above
     */
    public List<Class<?>> getDataTypes() { return dataTypes; }

    /**
     * Returns the Class corresponding to the passed value.
     * 
     * @param value The value to handle.
     * @return See above.
     */
    public Class<?> convertDataType(String value)
    {
        if (value == null) return null;
        value = value.toLowerCase();
        if (IMAGE_TYPE.equals(value)) {
            return ImageData.class;
        } else if (DATASET_TYPE.equals(value)) {
            return DatasetData.class;
        } else if (PROJECT_TYPE.equals(value)) {
            return ProjectData.class;
        } else if (SCREEN_TYPE.equals(value)) {
            return ScreenData.class;
        } else if (PLATE_TYPE.equals(value)) {
            return PlateData.class;
        }
        return null;
    }

    /**
     * Returns <code>true</code> if the specified key used to collect 
     * object's ID, <code>false</code> otherwise.
     * 
     * @param key The key to handle.
     * @return See above.
     */
    public boolean isIdentifier(String key)
    {
        if (CommonsLangUtils.isBlank(key)) return false;
        key = key.trim().toLowerCase();
        return IDENTIFIER_KEYS.contains(key);
    }

    /**
     * Returns <code>true</code> if the specified key is a data type,
     * <code>false</code> otherwise.
     * 
     * @param key The key to handle.
     * @return See above.
     */
    public boolean isDataType(String key)
    {
        return DATA_TYPE.equals(key);
    }

    /**
     * Sets the id of the group.
     *
     * @param groupID The value to set.
     */
    public void setGroupID(long groupID)
    {
        this.groupID = groupID;
    }

    /**
     * Returns the id of the group.
     *
     * @return See above.
     */
    public long getGroupID() { return groupID; }

    /**
     * Returns <code>true</code> if the specified object is supported,
     * <code>false</code> otherwise.
     *
     * @param data The object to set.
     * @param key The key to determine if the object is supported.
     * @return See above.
     */
    public boolean isSupportedType(pojos.DataObject data, String key)
    {
        if (data == null || CommonsLangUtils.isBlank(key)) return false;
        if (key.contains("_")) {
            String[] values = key.split("_");
            Class<?> type = convertDataType(values[0]);
            return (data.getClass().equals(type));
        }
        return false;
    }
    /**
     * Overridden to return the name of the script.
     * @see java.lang.Object#toString()
     */
    public String toString() { return getScriptLabel(); }

}
