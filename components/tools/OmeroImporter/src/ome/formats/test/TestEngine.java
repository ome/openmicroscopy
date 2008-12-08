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

public class TestEngine
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
    
    private TestEngine() throws Exception
    {
        wrapper = new OMEROWrapper();
        
        // Login
        store = new OMEROMetadataStoreClient("root", "ome", "mage.openmicroscopy.org.uk", "1099");
        importLibrary = new ImportLibrary(store, wrapper);
        
        // Create a time stamp and use it for the project name
        String projectName = new Date().toString();
        System.err.println("Storing project: " + projectName);
        Project project = store.addProject(projectName, "");
               
        // Parse the sub-directories - these will become our datasets
        File projectDirectory = new File("/users/TheBrain/test_images/");
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
                        
                        // Find how many individual series images there are in the file
                        int seriesCount = wrapper.getSeriesCount();
                        
                        // For each image, get or set important data
                        for (int series = 0; series < seriesCount; series++)
                        {
                            Long pixId = pixList.get(series).getId().getValue();

                            // Get Sha1
                            String sha1 = store.getPixels(series).getSha1().getValue();

                            // Get image dimensions
                            Integer sizeX = store.getPixels(series).getSizeX().getValue();
                            Integer sizeY = store.getPixels(series).getSizeY().getValue();
                            Integer sizeZ = store.getPixels(series).getSizeZ().getValue();
                            Integer sizeC = store.getPixels(series).getSizeC().getValue();
                            Integer sizeT = store.getPixels(series).getSizeT().getValue();
                            PixelsType pixelType = store.getPixels(series).getPixelsType();
                            DimensionOrder dimOrder = store.getPixels(series).getDimensionOrder();
                            Float pixelSizeX = store.getPixels(series).getPhysicalSizeX().getValue();
                            Float pixelSizeY = store.getPixels(series).getPhysicalSizeY().getValue();
                            Float pixelSizeZ = store.getPixels(series).getPhysicalSizeZ().getValue();

                            // Compare
                            testValue(fileList[j], "s" + series + "_SHA1", sha1);
                            testValue(fileList[j], "s" + series + "_sizeX", sizeX);
                            testValue(fileList[j], "s" + series + "_sizeY", sizeY);
                            testValue(fileList[j], "s" + series + "_sizeZ", sizeZ);
                            testValue(fileList[j], "s" + series + "_sizeC", sizeC);
                            testValue(fileList[j], "s" + series + "_sizeT", sizeT);
                            testValue(fileList[j], "s" + series + "_pixelType", pixelType.getValue());
                            testValue(fileList[j], "s" + series + "_dimOrder", dimOrder.getValue());
                            testValue(fileList[j], "s" + series + "_pixelSizeX", pixelSizeX);
                            testValue(fileList[j], "s" + series + "_pixelSizeY", pixelSizeY);
                            testValue(fileList[j], "s" + series + "_pixelSizeZ", pixelSizeZ);

                            
                            
                            for (int channel = 0; channel < sizeC; channel++)
                            {
                                Double globalMin = store.getPixels(series).getChannel(channel).getStatsInfo().getGlobalMin().getValue();
                                Double globalMax = store.getPixels(series).getChannel(channel).getStatsInfo().getGlobalMax().getValue();
                                
                                Integer emWave = store.getPixels(series).getChannel(channel).getLogicalChannel().getEmissionWave().getValue();
                                Integer exWave = store.getPixels(series).getChannel(channel).getLogicalChannel().getExcitationWave().getValue();                                
                                
                                testValue(fileList[j], "s" + series + "_c" + channel + "_globalMin", globalMin);
                                testValue(fileList[j], "s" + series + "_c" + channel + "_globalMax", globalMax);

                                testValue(fileList[j], "s" + series + "_c" + channel + "_emWave", emWave);
                                testValue(fileList[j], "s" + series + "_c" + channel + "_exWave", exWave);
                            }
                            
                        }
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
        
        new TestEngine();
    }
        

    private static void usage()
    {
        System.err.println("Optional variables:\n" +
                " - p: populate ini files with any images found\n" +
                " - f: point to a new root test directory. Example: -f \"/tests/test1/\"\n"
                );
    }
}
