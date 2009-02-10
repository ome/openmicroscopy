package ome.formats.importer.cli;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.IOException;

import loci.formats.FormatException;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import omero.ServerError;
import omero.model.Dataset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
     * @param datasetId The Id of the dataset to import the image into.
     * @param name Image name to use for import.
     * @throws IOException If there is an error reading from <code>path</code>.
     * @throws FormatException If there is an error parsing metadata.
     * @throws ServerError If there is a problem interacting with the server.
     */
    public void importImage(String path, Long datasetId, String name)
        throws IOException, FormatException, ServerError
    {
        File f = new File(path);
        Dataset d = null;
        if (datasetId != null)
        {
            d = store.getDataset(datasetId);
        }
        library.setDataset(d);
        library.importImage(f, 0, 0, 1, name, false);
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
                "  -d\tOMERO dataset Id to import image into\n" +
                "  -n\tImage name to use\n" +
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
        Getopt g = new Getopt(APP_NAME, args, "s:u:w:d:k:y:n:p:h");
        int a;
        String username = null;
        String password = null;
        String sessionKey = null;
        String hostname = null;
        int port = PORT;
        Long datasetId = null;
        String name = null;
        while ((a = g.getopt()) != -1)
        {
            switch (a)
            {
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
                    datasetId = Long.parseLong(g.getOptarg());
                    break;
                }
                case 'n':
                {
                    name = g.getOptarg();
                    break;
                }
                case 'h':
                {
                    usage();
                }
                case '?':
                {
                    usage();
                }
            }
        }
        
        // Ensure that we have all of our required login arguments
        if (((username == null || password == null) && sessionKey == null)
            || hostname == null)
        {
            usage();
        }
        if (args.length - g.getOptind() != 1)
        {
            usage();
        }
        String path = args[g.getOptind()];
        
        // Start the importer and import the image we've been given
        try
        {
            // Ensure that we have an image name
            if (name == null)
            {
                name = path;
            }
            CommandLineImporter c;
            if (sessionKey != null)
            {
                c = new CommandLineImporter(sessionKey, hostname, port);
            }
            else
            {
                c = new CommandLineImporter(username, password, hostname, port);
            }
            c.library.addObserver(new LoggingImportMonitor());
            c.importImage(path, datasetId, name);
            System.exit(0);  // Exit with specified return code
        }
        catch (Throwable t)
        {
            log.error("Error during import process." , t);
            System.exit(2);
        }
    }
}
