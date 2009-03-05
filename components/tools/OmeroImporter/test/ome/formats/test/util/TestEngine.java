package ome.formats.test.util;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactory;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import omero.ServerError;
import omero.model.Dataset;
import omero.model.Project;

public class TestEngine
{   
	/** Logger for this class */
	private Log log = LogFactory.getLog(TestEngine.class);
	
	/** Our configuration. */
	private TestEngineConfig config;
	
    // usage() name
    private static final String APP_NAME = "import-tester";
    
    private OMEROMetadataStoreClient store;
    private ImportLibrary importLibrary;
    private OMEROWrapper wrapper;
    
    private IniWritingInterceptor interceptor = new IniWritingInterceptor();
    
    public TestEngine(TestEngineConfig config)
    	throws CannotCreateSessionException, PermissionDeniedException, ServerError
    {
    	this.config = config;
        store = new OMEROMetadataStoreClient();
        ProxyFactory pf = new ProxyFactory(store);
        pf.addAdvice(interceptor);
        pf.setProxyTargetClass(true);
        store = (OMEROMetadataStoreClient) pf.getProxy();
    	wrapper = new OMEROWrapper();
    	
        // Login
    	if (config.getSessionKey() != null)
    	{
    		store.initialize(config.getHostname(), config.getPort(),
    				         config.getSessionKey());
    	}
    	else
    	{
    		store.initialize(config.getUsername(), config.getPassword(),
    				         config.getHostname(), config.getPort());
    	}
        importLibrary = new ImportLibrary(store, wrapper);
    }

    public boolean run(String targetDirectory)
    	throws Throwable
    {
        // Create a time stamp and use it for the project name
        String projectName = new Date().toString();
        log.info("Storing project: " + projectName);
        Project project = store.addProject(projectName, "");
               
        // Our root directory
        File projectDirectory = new File(targetDirectory);
        boolean status = false;
        if (!config.getRecurse())
        {
        	// Do not parse sub-directory - only import files in the target.
            String name = projectDirectory.getName(); 
            log.info("Storing dataset: " + name);
            Dataset dataset = store.addDataset(name, "", project);
            importLibrary.setDataset(dataset);
        	status = processDirectory(config.getPopulate(), projectDirectory);
        }
        else
        {
        	// Parse the sub-directories - these will become our datasets 
        	for (File datasetDirectory : projectDirectory.listFiles())
        	{
        		if (datasetDirectory.exists() && datasetDirectory.isDirectory())
        		{
                    String name = datasetDirectory.getName(); 
                    log.info("Storing dataset: " + name);
                    Dataset dataset = store.addDataset(name, "", project);
                    importLibrary.setDataset(dataset);
                    // In each sub-directory/dataset, import the images needed
        			status = processDirectory(config.getPopulate(),
        					                  datasetDirectory);
        		}
        	}
        }
        store.logout();
        return status;
    }
    
    private boolean processDirectory(boolean populate, File directory)
    	throws Throwable
    {
    	String iniFilePath = directory + File.separator + "test_setup.ini";
    	log.info("INI file path: " + iniFilePath);
		// Load up the main ini file
		TestEngineIniFile iniFile = 
			new TestEngineIniFile(new File(iniFilePath));
		interceptor.setIniFile(iniFile);

		String[] fileTypes = iniFile.getFileTypes();
		
		if (populate = true && fileTypes != null)
		{
			// get all files in the directory
			File[] datasetFiles = directory.listFiles();

			for (File datasetFile : datasetFiles)
			{
				for (String fileType : fileTypes)
				{
					if (datasetFile.isFile() 
						&& datasetFile.getName().endsWith("." + fileType))
					{
						iniFile.addFile(datasetFile.getName());
					}
				}
			}
		}
		else if (populate = true && fileTypes == null)
		{
			log.error("No filetypes for " + iniFilePath);                        
		}
		
		// The filtered list of files we're to attempt to import
		String[] fileList = iniFile.getFileList();
		
		// Sanity check
		if (fileList.length < 1)
		{
			log.error("No files available to import.");
		}

		for (int j = 0; j < fileList.length; j++)
		{
			if (fileList[j].equals("populate_options"))
				continue;
			File file = new File(directory + File.separator + fileList[j]);

			// Import and return pixels list
			log.info("------Importing file: " + file + "------");

			// Skip missing files
			if (!file.exists())
			{
				log.warn("Image file " + file.getName() + 
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
				store.logout();
				log.error("Failed on file:" + file.getName());
				throw e;
			}
		}
    	return true;
    }

    /**
     * Prints usage to STDERR and exits with return code 1.
     */
    public static void usage()
    {
        System.err.println(String.format(
                "Usage: %s [OPTION]... [TARGET DIRECTORY]\n" +
                "Imports one or more files into an OMERO instance and tests\n" +
                "metadata. More information about the test engine can be found at:\n" +
                "\n" +
                "http://trac.openmicroscopy.org.uk/wiki/ImporterTestEngine\n" +
                "\n" +
                "Optional arguments:\n" +
                "  -s\tOMERO server hostname\n" +
                "  -u\tOMERO experimenter name (username)\n" +
                "  -w\tOMERO experimenter password\n" +
                "  -k\tOMERO session key (can be used in place of -u and -w)\n" +
                "  -p\tOMERO server port [defaults to 4063]\n" +
                "  -c\tConfiguration file location (instead of any of the above arguments)\n" +
                "  -x\tPopulate initiation files with metadata [defaults to False]\n" +
                "  -h, --help\tDisplay this help and exit\n" +
                "  --no-recurse\tSingle directory test run\n" +
                "\n" +
                "ex. %s -s localhost -u bart -w simpson ShortRunImages\n" +
                "\n" +
                "Report bugs to <ome-users@openmicroscopy.org.uk>",
                APP_NAME, APP_NAME));
        System.exit(1);
    }
    
    /**
     * Command line application entry point which parses CLI arguments and
     * passes them into the importer. Return codes are:
     * <ul>
     *   <li>0 on success</li>
     *   <li>1 on argument parsing failure</li>
     *   <li>2 on exception during import</li>
     * </ul>
     * @param args Command line arguments.
     */
    public static void main(String[] args) throws Throwable
    {
    	LongOpt[] longOptions = new LongOpt[] {
    		new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),	
    		new LongOpt("no-recurse", LongOpt.OPTIONAL_ARGUMENT, null, 'r')
    	};
    	
    	// First find our configuration file, if it has been provided.
        Getopt g = new Getopt(APP_NAME, args, "s:u:w:p:k:c:x", longOptions);
        int a;
        InputStream configFile = new ByteArrayInputStream(new byte[0]);
        while ((a = g.getopt()) != -1)
        {
            switch (a)
            {
                case 'c':
                {
                    configFile = new FileInputStream(g.getOptarg());
                    break;
                }
            }
        }
        // Second check for the configuration file on the CLASSPATH if we
        // haven't been given a configuration file on the command line.
        if (configFile instanceof ByteArrayInputStream)
        {
        	InputStream fromClasspath = 
        		TestEngine.class.getClassLoader().
        		getResourceAsStream("test_engine.ini");
        	configFile = fromClasspath == null? configFile : fromClasspath;
        }

        // Now parse our options.
        g = new Getopt(APP_NAME, args, "s:u:w:p:c:k:x", longOptions);
        TestEngineConfig config = new TestEngineConfig(configFile);
        while ((a = g.getopt()) != -1)
        {
            switch (a)
            {
                case 's':
                {
                    config.setHostname(g.getOptarg());
                    break;
                }
                case 'u':
                {
                    config.setUsername(g.getOptarg());
                    break;
                }
                case 'w':
                {
                    config.setPassword(g.getOptarg());
                    break;
                }
                case 'k':
                {
                	config.setSessionKey(g.getOptarg());
                    break;
                }
                case 'p':
                {
                    config.setPort(Integer.parseInt(g.getOptarg()));
                    break;
                }
                case 'x':
                {
                	config.setPopulate(!config.getPopulate());
                	break;
            	}
                case 'r':
                {
                	config.setRecurse(!config.getRecurse());
                	break;
                }
                case 'c':
                {
                	// Ignore, we've dealt with this already.
                	break;
                }
                default:
                {
                    usage();
                }
            }
        }
        
        // Ensure that we have all of our required login arguments
        if (!config.validateLogin())
        {
            usage();
        }
        
        // Ensure that we have a valid target path.
        String path = config.getTarget();
        if (args.length - g.getOptind() == 1)
        {
        	path = args[g.getOptind()];
        }
        else if (path == null)
        {
        	usage();
        }
        
        TestEngine engine = new TestEngine(config);
        engine.run(path);
    }
}
