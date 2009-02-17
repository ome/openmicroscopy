package ome.formats.test.util;

import gnu.getopt.Getopt;

import java.io.File;
import java.util.Date;

import org.springframework.aop.framework.ProxyFactory;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.formats.utests.TestServiceFactory;
import omero.model.Dataset;
import omero.model.Project;

public class TestEngine
{   
    // Directory to use for test files
    private static final String TEST_FOLDER = "/Users/TheBrain/test_images_shortrun/";
        //"/Volumes/lmf/mporter/InCell/Aurora_Inhibitors/2009.01.26.17.45.32/";
        //
    
    // Display verbose output on command line
    private static final Boolean VERBOSE = false;
       
    // usage() name
    private static final String APP_NAME = "import-tester";
    
    // Command options
    public static boolean populateIniFiles = false;
    public static String  rootDirectory;
    
    private OMEROMetadataStoreClient store;
    private ImportLibrary importLibrary;
    private OMEROWrapper wrapper;
    
    private IniWritingInterceptor interceptor = new IniWritingInterceptor();

    private TestEngine() throws Throwable
    {
        wrapper = new OMEROWrapper();
        store = new OMEROMetadataStoreClient();
        store.initialize(new TestServiceFactory());
        
        ProxyFactory pf = new ProxyFactory(store);
        pf.addAdvice(interceptor);
        pf.setProxyTargetClass(true);
        store = (OMEROMetadataStoreClient) pf.getProxy();
        
        // Login
        store.initialize("root", "ome", "warlock.openmicroscopy.org.uk", 4063);
        importLibrary = new ImportLibrary(store, wrapper);
        
        // Create a time stamp and use it for the project name
        String projectName = new Date().toString();
        CPrint("Storing project: " + projectName);
        Project project = store.addProject(projectName, "");
               
        // Parse the sub-directories - these will become our datasets
        File projectDirectory = new File(TEST_FOLDER);
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
                    CPrint("Storing dataset: " + datasetName);
                    Dataset dataset = store.addDataset(datasetName, "", project);
                    importLibrary.setDataset(dataset);
                    
                    String dirstring = datasetDirectory + File.separator + "test_setup.ini";
                    CPrint("dataset directory: " + dirstring);
                    
                    // Load up the main ini file
                    TestEngineIniFile iniFile = 
                        new TestEngineIniFile(new File(dirstring));
                    interceptor.setIniFile(iniFile);
                    
                    String[] fileList = iniFile.getFileList();
                    String[] fileTypes = iniFile.getFileTypes();
                    
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
                                    iniFile.addFile(datasetFiles[k].getName());
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
                        File file = new File(datasetDirectory + File.separator + fileList[j]);
                        
                        // Import and return pixels list
                        System.err.println("------Importing file: " + file + "------");
                        
                        // Skip missing files
                        if (!file.exists())
                        {
                            System.err.println("Image file " + file.getName() + 
                                    " missing but referenced in test_setup.ini");
                            continue;
                        }
                        
                        try
                        {
                            interceptor.setSourceFile(file);
                            importLibrary.importImage(file, 0, 0, 1, fileList[j], false);
                            iniFile.flush();
                        }
                        catch (Throwable e)
                        {
                            // Flush our file log to disk
                            iniFile.flush();
                            throw e;
                        }
                    }
                    
                }
            }
        }
        System.err.println("All done");
        store.logout();
        System.exit(0);
    }


    
    private void CPrint(final String string)
    {
        if (VERBOSE)
            System.err.println(string);
    }
    
    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Throwable
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
