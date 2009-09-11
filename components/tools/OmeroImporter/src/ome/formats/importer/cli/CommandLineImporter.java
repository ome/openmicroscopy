package ome.formats.importer.cli;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.File;
import java.io.IOException;

import loci.formats.FormatException;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import omero.ServerError;
import omero.model.Dataset;
import omero.model.IObject;
import omero.model.Screen;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * The base entry point for the CLI version of the OMERO importer.
 * 
 * @author Chris Allan <callan@glencoesoftware.com>
 *
 */
public class CommandLineImporter
{
    /** Logger for this class. */
    private static Log log = LogFactory.getLog(CommandLineImporter.class);

    /** Default OMERO port */
    private static final int PORT = 4063;
    
    /** Name that will be used for usage() */
    private static final String APP_NAME = "importer-cli";
    
    /** Base importer library, this is what we actually use to import. */
    public ImportLibrary library;
    
    /** Bio-Formats reader wrapper customized for OMERO. */
    private OMEROWrapper reader;

    /** Bio-Formats {@link MetadataStore} implementation for OMERO. */
    private OMEROMetadataStoreClient store;

    /**
     * Main entry class for the application.
     */
    public CommandLineImporter(String username, String password,
                               String host, int port)
        throws Exception
    {
        store = new OMEROMetadataStoreClient();
        store.initialize(username, password, host, port);
        reader = new OMEROWrapper();
        library = new ImportLibrary(store, reader);
    }
    
    /**
     * Secondary, session key based constructor.
     */
    public CommandLineImporter(String sessionKey, String host, int port)
        throws Exception
    {
        store = new OMEROMetadataStoreClient();
        store.initialize(host, port, sessionKey);
        reader = new OMEROWrapper();
        library = new ImportLibrary(store, reader);
    }
    
    /**
     * Adds a monitor to the import process.
     * @param monitor The monitor.
     */
    public void addMonitor(LoggingImportMonitor monitor)
    {
        library.addObserver(monitor);
    }
    
    /**
     * Imports an image into OMERO.
     * @param path The file path to import.
     * @param targetClass The class of the target object to import the image
     * into.
     * @param targetId The Id of the target object to import the image into.
     * @param name Image name to use for import.
     * @param description Image description to use for import.
     * @throws IOException If there is an error reading from <code>path</code>.
     * @throws FormatException If there is an error parsing metadata.
     * @throws ServerError If there is a problem interacting with the server.
     */
    public void importImage(String path, Class<? extends IObject> targetClass,
    						Long targetId, String name, String description)
        throws IOException, FormatException, ServerError
    {
        File f = new File(path);
        IObject target = null;
        if (targetId != null)
        {
            target = store.getTarget(targetClass, targetId);
        }
        library.setTarget(target);
        library.importImage(f, 0, 0, 1, name, description, false, true, null);
    }
    
    /**
     * Cleans up after a successful or unsuccessful image import.
     */
    public void cleanup()
    {
    	store.logout();
    }

    /**
     * Prints usage to STDERR and exits with return code 1.
     */
    public static void usage()
    {
        System.err.println(String.format(
                "Usage: %s [OPTION]... [FILE]\n" +
                "Import single files into an OMERO instance.\n" +
                "\n" +
                "Mandatory arguments:\n" +
                "  -s\tOMERO server hostname\n" +
                "  -u\tOMERO experimenter name (username)\n" +
                "  -w\tOMERO experimenter password\n" +
                "  -k\tOMERO session key (can be used in place of -u and -w)\n" +
                "\n" +
                "Optional arguments:\n" +
                "  -c\tContinue importing after errors\n" +
                "  -l\tUse the list of readers rather than the default\n" +
                "  -f\tDisplay the used files [does not require mandatory arguments]\n" +
                "  -d\tOMERO dataset Id to import image into\n" +
                "  -r\tOMERO screen Id to import plate into\n" +
                "  -n\tImage name to use\n" +
                "  -x\tImage description to use\n" +
                "  -p\tOMERO server port [defaults to 4063]\n" +
                "  -h\tDisplay this help and exit\n" +
                "\n" +
                "ex. %s -s localhost -u bart -w simpson -d 50 foo.tiff\n" +
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
    public static void main(String[] args)
    {
    	LongOpt debug = new LongOpt("debug", LongOpt.NO_ARGUMENT, null, 1);
        Getopt g = new Getopt(APP_NAME, args, "cfl:s:u:w:d:r:k:x:n:p:h",
        		              new LongOpt[] { debug });
        int a;
        String username = null;
        String password = null;
        String sessionKey = null;
        String hostname = null;
        int port = PORT;
        Class<? extends IObject> targetClass = null;
        Long targetId = null;
        String name = null;
        String description = null;
        boolean getUsedFiles = false;
        boolean continueImport = false;
        while ((a = g.getopt()) != -1)
        {
            switch (a)
            {
            	case 1:
            	{
            		// We're modifying the Log4j logging level of everything
            		// under the ome.format package hierarchically. We're using
            		// OMEROMetadataStoreClient as a convenience.
            		Logger l = Logger.getLogger(OMEROMetadataStoreClient.class);
            		l.setLevel(Level.DEBUG);
            		break;
            	}
                case 's':
                {
                    hostname = g.getOptarg();
                    break;
                }
                case 'u':
                {
                    username = g.getOptarg();
                    break;
                }
                case 'w':
                {
                    password = g.getOptarg();
                    break;
                }
                case 'k':
                {
                    sessionKey = g.getOptarg();
                    break;
                }
                case 'p':
                {
                    port = Integer.parseInt(g.getOptarg());
                    break;
                }
                case 'd':
                {
                	targetClass = Dataset.class;
                    targetId = Long.parseLong(g.getOptarg());
                    break;
                }
               	case 'r':
                {
                	targetClass= Screen.class;
                	targetId = Long.parseLong(g.getOptarg());
                	break;
                }
                case 'n':
                {
                    name = g.getOptarg();
                    break;
                }
                case 'x':
                {
                    description = g.getOptarg();
                    break;
                }
                case 'f':
                {
                	getUsedFiles = true;
                	break;
                }
                case 'c':
                {
                	continueImport = true;
                	break;
                }
                case 'l':
                {
                    String readers = g.getOptarg();
                    System.setProperty(OMEROWrapper.READERS_KEY, readers);
                    break;
                }
                default:
                {
                    usage();
                }
            }
        }

        // Parse out our file path
        String[] rest = new String[args.length - g.getOptind()];
        System.arraycopy(args, g.getOptind(), rest, 0, args.length - g.getOptind());
        ImportCandidates candidates = new ImportCandidates(rest);
        if (candidates.size() < 1)
        {
            System.err.println("No imports found");
            usage();
        }
        
        // If we've been asked to display used files, display them and exit.
        if (getUsedFiles)
        {
        	try
        	{
        		candidates.print();
        		System.exit(0);
        	}
        	catch (Throwable t)
        	{
        		log.error("Error retrieving used files.", t);
        		System.exit(2);
        	}
        }
        
        // Ensure that we have all of our required login arguments
        if (((username == null || password == null) && sessionKey == null)
            || hostname == null)
        {
            usage();
        }

        // Start the importer and import the image we've been given
        CommandLineImporter c = null;
        try
        {
            if (sessionKey != null)
            {
                c = new CommandLineImporter(sessionKey, hostname, port);
            }
            else
            {
                c = new CommandLineImporter(username, password, hostname, port);
            }

            for (String _path : candidates.getPaths())
            {
                String _name = name;
                // Ensure that we have an image name
                if (_name == null)
                {
                    _name = _path;
                }
                c.library.addObserver(new LoggingImportMonitor());
                c.importImage(_path, targetClass, targetId, _name, description);
            }
            System.exit(0);  // Exit with specified return code
        }
        catch (Throwable t)
        {
            log.error("Error during import process." , t);
            System.exit(2);
        }
        finally
        {
        	if (c != null)
        	{
        		c.cleanup();
        	}
        }
    }
}
