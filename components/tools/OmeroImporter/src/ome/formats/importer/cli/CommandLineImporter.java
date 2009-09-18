package ome.formats.importer.cli;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import loci.formats.meta.MetadataStore;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import omero.ServerError;
import omero.model.Dataset;
import omero.model.Screen;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * The base entry point for the CLI version of the OMERO importer.
 * 
 * @author Chris Allan <callan@glencoesoftware.com>
 * @author Josh Moore josh at glencoesoftware.com
 */
public class CommandLineImporter
{
    /** Logger for this class. */
    private static Log log = LogFactory.getLog(CommandLineImporter.class);

    /** Name that will be used for usage() */
    private static final String APP_NAME = "importer-cli";
    
    /** Configuration used by all components */
    public final ImportConfig config;
    
    /** Base importer library, this is what we actually use to import. */
    public final ImportLibrary library;
    
    /** Bio-Formats reader wrapper customized for OMERO. */
    private final OMEROWrapper reader;

    /** Bio-Formats {@link MetadataStore} implementation for OMERO. */
    private final OMEROMetadataStoreClient store;

    /** Candidates for import */
    private final ImportCandidates candidates;
    
    /** If true, then only a report on used files will be produced */
    private final boolean getUsedFiles;
    
    /**
     * Main entry class for the application.
     */
    public CommandLineImporter(ImportConfig config, String[] paths, boolean getUsedFiles)
        throws Exception
    {
        this.config = config;
        this.getUsedFiles = getUsedFiles;
        reader = new OMEROWrapper(config);
        candidates = new ImportCandidates(reader, paths);
        if (!getUsedFiles) {
            store = new OMEROMetadataStoreClient(config);
            library = new ImportLibrary(store, reader);
        } else {
            store = null;
            library = null;
        }
    }

    public int run() {
        
        if (candidates.size() < 1) {
            System.err.println("No imports found");
            usage();
        }
        
        if (getUsedFiles)
        {
            try
            {
                candidates.print();
                return 0;
            }
            catch (Throwable t)
            {
                log.error("Error retrieving used files.", t);
                return 2;
            }
        }
        
        else
        {
            
            // Ensure that we have all of our required login arguments
            if (!config.canLogin()) {
                usage(); // EXITS
            }
            
            library.addObserver(new LoggingImportMonitor());
            library.addObserver(new ErrorHandler(config));
            library.importCandidates(config, candidates);
        
        }
        
        return 0;

    }
    
    /**
     * Cleans up after a successful or unsuccessful image import.
     */
    public void cleanup()
    {
        if (store != null) {
            store.logout();
        }
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
                "  --debug\tTurn debug logging on\n" +
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
        ImportConfig config = new ImportConfig(args);
    	LongOpt debug = new LongOpt("debug", LongOpt.NO_ARGUMENT, null, 1);
        Getopt g = new Getopt(APP_NAME, args, "cfl:s:u:w:d:r:k:x:n:p:h",
        		              new LongOpt[] { debug });
        int a;

        boolean getUsedFiles = false;
        
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
                    config.setSessionkey(g.getOptarg());
                    break;
                }
                case 'p':
                {
                    config.setPort(Integer.parseInt(g.getOptarg()));
                    break;
                }
                case 'd':
                {
                    config.setTargetClass(Dataset.class);
                    config.setTargetId(Long.parseLong(g.getOptarg()));
                    break;
                }
               	case 'r':
                {
                    config.setTargetClass(Screen.class);
                    config.setTargetId(Long.parseLong(g.getOptarg()));
                	break;
                }
                case 'n':
                {
                    config.setName(g.getOptarg());
                    break;
                }
                case 'x':
                {
                    config.setDescription(g.getOptarg());
                    break;
                }
                case 'f':
                {
                	getUsedFiles = true;
                	break;
                }
                case 'c':
                {
                    config.setContinueOnErrors(true);
                	break;
                }
                case 'l':
                {
                    config.setReadersPath(g.getOptarg());
                    break;
                }
                default:
                {
                    usage();
                }
            }
        }
        

        // Start the importer and import the image we've been given
        String[] rest = new String[args.length - g.getOptind()];
        System.arraycopy(args, g.getOptind(), rest, 0, args.length - g.getOptind());
        CommandLineImporter c = null;
        int rc = 0;
        try
        {
            c = new CommandLineImporter(config, rest, getUsedFiles);
            rc = c.run();
        }
        catch (Throwable t)
        {
            log.error("Error during import process." , t);
            rc = 2;
        }
        finally
        {
            if (c != null)
            {
                c.cleanup();
            }
        }
        System.exit(rc);
    }
}
