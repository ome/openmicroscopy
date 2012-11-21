package ome.formats.test.util;

import java.io.File;
import java.util.prefs.BackingStoreException;

import org.ini4j.IniFile;


class TestEngineIniFile extends IniFile
{
    private boolean populate = true;

    public TestEngineIniFile(File file)
        throws BackingStoreException
    {
        super(file, Mode.RW);
    }

    public String[] getFileList()
    {
        try
        {
            return childrenNames();
        } catch (BackingStoreException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
    }

    public void addFile(String fileName)
    {
        node(fileName);
    }

    public String getNote(String file)
    {
        return node(file).get("note", null);
    }

    public String[] getFileTypes()
    {
        String[] types = null;
        String fileTypes = node("populate_options").get("filetypes", null);
        if (fileTypes != null)
        {
            types = fileTypes.split(",");
            for (int i = 0; i<types.length; i++)
            {
                types[i] = types[i].trim();
            }
        }
        return types;
    }

    // Test a string value, setting new values if existing one in the ini file is empty
    /**
     * @param section
     * @param key
     * @param value - can be string or common primitive that accepts toString() method
     */
    public void testValue(String section, String key, Object value)
    {
        if (value != null && !(value instanceof String))
        {
            value = value.toString();
        }

        if (value != null && value.toString().length() > 255)
        {
            value = value.toString().substring(0, 252) + "...";
        }

        String storedValue = node(section).get(key, null);
        if ((storedValue == null || populate == true) && value != null)
        {
            System.err.println("Storing value for " + section + ": key=" + key + ", value=: " + value);
            node(section).put(key, (String) value);
        } else if ((value!= null && !storedValue.equals(value)) || (value == null && storedValue != null))
        {
            System.err.println("Value mismatch in " + section + ": key=" + key + ", stored value=" + storedValue + " new value=" + value);
        } else if (value == null)
        {
            System.err.println("Skipping null value: " + section + ": key=" + key);
        }
    }
}
