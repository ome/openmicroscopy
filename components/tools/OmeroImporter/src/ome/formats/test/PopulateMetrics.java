package ome.formats.test;

import gnu.getopt.Getopt;

import java.io.File;
import java.util.Date;
import java.util.List;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import omero.model.Dataset;
import omero.model.Project;
import omero.model.Pixels;
import omero.model.DimensionOrder;
import omero.model.PixelsType;

public class PopulateMetrics
{   
    // usage() name
    private static final String APP_NAME = "import-tester";
    
    // Command options
    public static boolean populateIniFiles = false;
    public static String  rootDirectory;
    
    private OMEROMetadataStoreClient store;
    private ImportLibrary importLibrary;
    private OMEROWrapper wrapper;

    IniFileLoader   ini;
    
    private PopulateMetrics() throws Exception
    {
        wrapper = new OMEROWrapper();
        
        // Login
        store = new OMEROMetadataStoreClient();
        store.initialize("root", "ome", "mage.openmicroscopy.org.uk", 4063);
        importLibrary = new ImportLibrary(store, wrapper);
        
        // Create a time stamp and use it for the project name
        String projectName = new Date().toString();
        System.err.println("Storing project: " + projectName);
        Project project = store.addProject(projectName, "");
               
        // Parse the sub-directories - these will become our datasets
        File projectDirectory = new File("/users/TheBrain/metric_files/");
        if (projectDirectory.exists() && projectDirectory.isDirectory())
        {
            File[] files = projectDirectory.listFiles();
            for (int i = 0; i < files.length; i++)
            {               
                // In each sub-directory/dataset, import the image needed
                File datasetDirectory = files[i];
                if (datasetDirectory.exists() && datasetDirectory.isDirectory())
                {
                    String datasetName = datasetDirectory.getName();
                    System.err.println("Storing dataset: " + datasetName);
                    Dataset dataset = store.addDataset(datasetName, "", project);
                    importLibrary.setDataset(dataset);
                    
                    String dirstring = datasetDirectory + File.separator + "test_setup.ini";
                    System.err.println("dataset directory: " + dirstring);
                    
                    // Load up the main ini file
                    ini = IniFileLoader.getNewIniFileLoader(dirstring);
                    
                    String[] fileList = ini.getFileList();
                    String[] fileTypes = ini.getFileTypes();
                    
                    if (populateIniFiles = true && fileTypes != null)
                    {
                        // get all files in the directory
                        File[] datasetFiles = datasetDirectory.listFiles();
                        
                        for (int k = 0; k < datasetFiles.length; k++)
                        {
                            for (int m = 0; m < fileTypes.length; m++)
                            {
                                if (datasetFiles[k].isFile() 
                                        && datasetFiles[k].getName().endsWith("." + fileTypes[m]))
                                {
                                    ini.addFile(datasetFiles[k].getName());
                                }
                            }
                        }
                    } else if (populateIniFiles = true && fileTypes == null)
                    {
                        System.err.println("No filetypes for " + dirstring);                        
                    }                  
                    
                    for (int j = 0; j < fileList.length; j++)
                    {
                        if (fileList[j].equals("populate_options"))
                            continue;
                        //System.out.println(fileList[j]);
                        //System.out.println(" - " + ini.getNote(fileList[j]));
                        //importImage(datasetDirectory + File.separator + fileList[j], dataset);
                        File file = new File(datasetDirectory + File.separator + fileList[j]);
                        
                        // Import and return pixels list
                        List<Pixels> pixList = importLibrary.importImage(file, 0, 0, 1, fileList[j], false);

                    }
                }
                
            }
        }
    }

    // Test a string value, setting new values if existing one in the ini file is empty
    /**
     * @param section
     * @param key
     * @param value - can be string or common primative that accepts toString() method
     */
    public void testValue(String section, String key, Object value)
    {
        
        if (!Object.class.getName().equals("java.lang.String") && value !=null)
            value = value.toString();
        
        String storedValue = ini.getStringValue(section, key);
        if ((storedValue == null || populateIniFiles == true) && value != null)
        {
            System.err.println("Storing value for " + section + ": key=" + key + ", value=: " + value);
            ini.setStringValue(section, key, (String) value);    
        } else if ((value!= null && !storedValue.equals(value)) || (value == null && storedValue != null))
        {
            System.err.println("Value mismatch in " + section + ": key=" + key + ", stored value=" + storedValue + " new value=" + value);
        } else if (value == null)
        {
            System.err.println("Skipping null value: " + section + ": key=" + key);
        }
    }   
    
    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception
    {
        
        int a;
                
        Getopt g = new Getopt(APP_NAME, args, ":pfh");
        
        while ((a = g.getopt()) != -1)
        {
            switch(a)
            {
                case 'p':
                {
                    populateIniFiles = true;
                }
                case 'f':
                {
                    rootDirectory = g.getOptarg();
                    break;
                }
                case 'h':
                {
                    usage();
                    break;
                }
                case '?':
                {
                    usage();
                    break;
                }
            }
        }               
        System.err.println("Populate Ini Files: " + populateIniFiles);
        System.err.println("Root Test Folder: " + rootDirectory);
        
        new PopulateMetrics();
    }
        

    private static void usage()
    {
        System.err.println("Optional variables:\n" +
                " - p: populate ini files with any images found\n" +
                " - f: point to a new root test directory. Example: -f \"/tests/test1/\"\n"
                );
    }
}
