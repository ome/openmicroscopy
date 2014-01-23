/*
 *   Copyright (C) 2009-2013 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats.importer.cli;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import loci.formats.in.DefaultMetadataOptions;
import loci.formats.in.MetadataLevel;
import loci.formats.meta.MetadataStore;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.transfers.AbstractFileTransfer;
import ome.formats.importer.transfers.CleanupFailure;
import ome.formats.importer.transfers.FileTransfer;
import ome.formats.importer.transfers.UploadFileTransfer;
import omero.model.Annotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.Screen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.apache.log4j.Level; CGB: Needs replacing with logback equivalent.

/**
 * The base entry point for the CLI version of the OMERO importer.
 *
 * @author Chris Allan <callan@glencoesoftware.com>
 * @author Josh Moore josh at glencoesoftware.com
 */
public class CommandLineImporter {
    /** Logger for this class. */
    private static Logger log = LoggerFactory.getLogger(CommandLineImporter.class);

    /** Name that will be used for usage() */
    private static final String APP_NAME = "importer-cli";

    /** Configuration used by all components */
    public final ImportConfig config;

    /** {@link FileTransfer} mechanism to be used for uploading */
    public final FileTransfer transfer;

    /** Base importer library, this is what we actually use to import. */
    public final ImportLibrary library;

    /** ErrorHandler which is also responsible for uploading files */
    public final ErrorHandler handler;

    /** Bio-Formats reader wrapper customized for OMERO. */
    private final OMEROWrapper reader;

    /** Bio-Formats {@link MetadataStore} implementation for OMERO. */
    private final OMEROMetadataStoreClient store;

    /** Candidates for import */
    private final ImportCandidates candidates;

    /** If true, then only a report on used files will be produced */
    private final boolean getUsedFiles;

    /**
     * Legacy constructor which uses a {@link UploadFileTransfer}.
     */
    public CommandLineImporter(final ImportConfig config, String[] paths,
            boolean getUsedFiles) throws Exception {
        this(config, paths, getUsedFiles, new UploadFileTransfer());
    }

    /**
     * Main entry class for the application.
     */
    public CommandLineImporter(final ImportConfig config, String[] paths,
            boolean getUsedFiles, FileTransfer transfer) throws Exception {
        this.config = config;
        config.loadAll();

        this.getUsedFiles = getUsedFiles;
        this.reader = new OMEROWrapper(config);
        this.handler = new ErrorHandler(config);
        this.transfer = transfer;
        candidates = new ImportCandidates(reader, paths, handler);

        if (paths == null || paths.length == 0 || getUsedFiles) {

            store = null;
            library = null;

        } else {

            // Ensure that we have all of our required login arguments
            if (!config.canLogin()) {
                // config.requestFromUser(); // stdin if anything missing.
                usage(); // EXITS TODO this should check for a "quiet" flag
            }

            config.isUpgradeNeeded();
            store = config.createStore();
            store.logVersionInfo(config.getIniVersionNumber());
            reader.setMetadataOptions(
                    new DefaultMetadataOptions(MetadataLevel.ALL));
            library = new ImportLibrary(store, reader, transfer);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                cleanup();
            }
        });
    }

    public int start() {

        boolean successful = true;
        if (getUsedFiles) {
            try {
                candidates.print();
                report();
                return 0;
            } catch (Throwable t) {
                log.error("Error retrieving used files.", t);
                return 1;
            }
        }

        else if (candidates.size() < 1) {
            if (handler.errorCount() > 0) {
                System.err.println("No imports due to errors!");
                report();
            } else {
                System.err.println("No imports found");
                try {
                    cleanup(); // #5426 Preventing close exceptions.
                } finally {
                    usage();
                }
            }
        }

        else {
            library.addObserver(new LoggingImportMonitor());
            // error handler has been configured in constructor from main args
            library.addObserver(this.handler);
            successful = library.importCandidates(config, candidates);
            report();
            try {
                List<String> paths = new ArrayList<String>();
                for (ImportContainer ic : candidates.getContainers()) {
                    paths.addAll(Arrays.asList(ic.getUsedFiles()));
                }
                transfer.afterSuccess(paths);
            } catch (CleanupFailure e) {
                log.error("rcode=3 on failed cleanup");
                return 3;
            }
        }

        return successful? 0 : 2;

    }

    void report() {
        boolean report = config.sendReport.get();
        boolean files = config.sendFiles.get();
        boolean logs = config.sendLogFile.get();
        if (report) {
           handler.update(null, new ImportEvent.DEBUG_SEND(files, logs));
        }
    }

    /**
     * Cleans up after a successful or unsuccessful image import. This method
     * only does the minimum required cleanup, so that it can be called
     * during shutdown.
     */
    public void cleanup() {
        if (store != null) {
            store.logout();
        }
    }

    /**
     * Prints usage to STDERR and exits with return code 1.
     */
    public static void usage() {
        System.err
                .println(String
                        .format(
                                "\n"
                                        + " Usage:  %s [OPTION]... [DIR|FILE]... \n"
                                        + "   or:   %s [OPTION]... - \n"
                                        + "\n"
                                        + "Import any number of files into an OMERO instance.\n"
                                        + "If \"-\" is the only path, a list of files or directories \n"
                                        + "is read from standard in. Directories will be searched for \n"
                                        + "all valid imports.\n"
                                        + "\n"
                                        + "Mandatory arguments:\n"
                                        + "  -s\tOMERO server hostname\n"
                                        + "  -u\tOMERO experimenter name (username)\n"
                                        + "  -w\tOMERO experimenter password\n"
                                        + "  -k\tOMERO session key (can be used in place of -u and -w)\n"
                                        + "  -f\tDisplay the used files (does not require other mandatory arguments)\n"
                                        + "\n"
                                        + "Naming arguments:\n"
                                        + "(All are optional but only image OR plate values should be set)\n"
                                        + "  -n\tImage name to use\n"
                                        + "  -x\tImage description to use\n"
                                        + "  --plate_name\t\tPlate name to use\n"
                                        + "  --plate_description\tPlate description to use\n"
                                        + "\n"
                                        + "Optional arguments:\n"
                                        + "  -c\tContinue importing after errors\n"
                                        + "  -l\tUse the list of readers rather than the default\n"
                                        + "  -d\tOMERO dataset Id to import image into\n"
                                        + "  -r\tOMERO screen Id to import plate into\n"
                                        + "  -p\tOMERO server port [defaults to 4064]\n"
                                        + "  -h\tDisplay this help and exit\n"
                                        + "\n"
                                        + "  --no_thumbnails\tDo not perform thumbnailing after import\n"
                                        + "  --plate_name\t\tPlate name to use\n"
                                        + "  --plate_description\tPlate description to use\n"
                                        + "  --debug[ALL|DEBUG|ERROR|FATAL|INFO|TRACE|WARN]\tTurn debug logging on (optional level)\n"
                                        + "  --report\t\tReport errors to the OME team\n"
                                        + "  --upload\t\tUpload broken files with report\n"
                                        + "  --logs\t\tUpload log file with report\n"
                                        + "  --email\t\tEmail for reported errors\n"
                                        + "  --annotation_ns\tNamespace to use for subsequent annotation\n"
                                        + "  --annotation_text\tContent for a text annotation (requires namespace)\n"
                                        + "  --annotation_link\tComment annotation ID to link all images to\n"
                                        + "\n"
                                        + "ex. %s -s localhost -u bart -w simpson -d 50 foo.tiff\n"
                                        + "\n"
                                        + "Report bugs to <ome-users@lists.openmicroscopy.org.uk>",
                                APP_NAME, APP_NAME, APP_NAME));
        System.exit(1);
    }

    /**
     * Prints advanced usage to STDERR and exits with return code 1.
     */
    public static void advUsage() {
        System.err
                .println(String
                        .format(
                                "\n"
                                        + "Advanced arguments:\n"
                                        + "  --checksum_algorithm=ARG\tE.g. Adler-32, CRC-32, MD5-128\n"
                                        + "                          \t     Murmur3-32, Murmur3-128, SHA1-160\n"
                                        + "  --transfer=ARG          \tFile transfer method\n"
                                        + "      Examples:\n"
                                        + "       -t upload           # Default\n"
                                        + "       -t ln               # Use hard-link. Locally only!\n"
                                        + "       -t ln_rm            # Caution! Hard-link followed by rm. Locally only!\n"
                                        + "       -t ln_s             # Use symlnk. Locally only!\n"
                                        + "       -t some.class.Name  # Use a class on the CLASSPATH\n"
                                        + "\n"
                                        + "ex. %s --transfer=ln_s foo.tiff\n"
                                        + "\n"
                                        + "Report bugs to <ome-users@lists.openmicroscopy.org.uk>",
                                APP_NAME));
        System.exit(1);
    }

    /**
     * Takes pairs of namespaces and string and creates comment annotations
     * from each pair.
     * @param namespaces Namespaces to use.
     * @param strings Strings to use.
     * @return List of comment annotations.
     */
    private static List<Annotation> toTextAnnotations(
                   List<String> namespaces, List<String> strings)
    {
        if (namespaces.size() != strings.size())
        {
            throw new IllegalArgumentException(String.format(
                            "#Namespaces:%d != #Text:%d", namespaces.size(),
                            strings.size()));
        }
        List<Annotation> annotations = new ArrayList<Annotation>();
        for(int i = 0; i < namespaces.size(); i++)
        {
            CommentAnnotationI annotation = new CommentAnnotationI();
            annotation.setNs(omero.rtypes.rstring(namespaces.get(i)));
            annotation.setTextValue(omero.rtypes.rstring(strings.get(i)));
            annotations.add(annotation);
        }
        return annotations;
    }

    /**
     * Command line application entry point which parses CLI arguments and
     * passes them into the importer. Return codes for import are:
     * <ul>
     * <li>0 on success</li>
     * <li>1 on argument parsing failure</li>
     * <li>2 on exception during import</li>
     * </ul>
     *
     * Return codes for the "-f" option (getUsedFiles) are:
     * <ul>
     * <li>0 on success, even if errors exist in the files</li>
     * <li>1 only if an exception propagates up the stack</li>
     * </ul>
     * @param args
     *            Command line arguments.
     */
    public static void main(String[] args) {

        FileTransfer transfer = new UploadFileTransfer();
        ImportConfig config = new ImportConfig();

        // Defaults
        config.email.set("");
        config.sendFiles.set(false);
        config.sendLogFile.set(false);
        config.sendReport.set(false);
        config.contOnError.set(false);
        config.debug.set(false);
        config.encryptedConnection.set(false);

        LongOpt debug = new LongOpt(
                "debug", LongOpt.OPTIONAL_ARGUMENT, null, 1);
        LongOpt report = new LongOpt("report", LongOpt.NO_ARGUMENT, null, 2);
        LongOpt upload = new LongOpt("upload", LongOpt.NO_ARGUMENT, null, 3);
        LongOpt logs = new LongOpt("logs", LongOpt.NO_ARGUMENT, null, 4);
        LongOpt email = new LongOpt(
                "email", LongOpt.REQUIRED_ARGUMENT, null, 5);
        LongOpt plateName = new LongOpt(
                "plate_name", LongOpt.REQUIRED_ARGUMENT, null, 6);
        LongOpt plateDescription = new LongOpt(
                "plate_description", LongOpt.REQUIRED_ARGUMENT, null, 7);
        LongOpt noThumbnails = new LongOpt(
                "no_thumbnails", LongOpt.NO_ARGUMENT, null, 8);
        LongOpt agent = new LongOpt(
                "agent", LongOpt.REQUIRED_ARGUMENT, null, 9);
        LongOpt annotationNamespace =
            new LongOpt("annotation_ns", LongOpt.REQUIRED_ARGUMENT, null, 10);
        LongOpt annotationText =
            new LongOpt("annotation_text", LongOpt.REQUIRED_ARGUMENT,
                        null, 11);
        LongOpt annotationLink =
            new LongOpt("annotation_link", LongOpt.REQUIRED_ARGUMENT,
                        null, 12);
        LongOpt transferOpt =
                new LongOpt("transfer", LongOpt.REQUIRED_ARGUMENT, null, 13);
        LongOpt transferHelp =
                new LongOpt("transfer-help", LongOpt.NO_ARGUMENT, null, 14);
        LongOpt checksumAlgorithm =
                new LongOpt("checksum_algorithm", LongOpt.REQUIRED_ARGUMENT, null, 15);

        Getopt g = new Getopt(APP_NAME, args, "cfl:s:u:w:d:r:k:x:n:p:ht:",
                new LongOpt[] { debug, report, upload, logs, email,
                                plateName, plateDescription, noThumbnails,
                                agent, annotationNamespace, annotationText,
                                annotationLink, transferOpt, transferHelp,
                                checksumAlgorithm});
        int a;

        boolean getUsedFiles = false;
        config.agent.set("importer-cli");

        // Once one of the properties has been set, setting the other
        // is likely a bug and so we'll through an exception.
        boolean userSpecifiedNameAlreadySet = false;
        boolean userSpecifiedDescriptionAlreadySet = false;

        List<String> annotationNamespaces = new ArrayList<String>();
        List<String> textAnnotations = new ArrayList<String>();
        List<Long> annotationIds = new ArrayList<Long>();
        while ((a = g.getopt()) != -1) {
            switch (a) {
            case 1: {
                config.configureDebug(g.getOptarg());
                break;
            }
            case 2: {
                config.sendReport.set(true);
                break;
            }
            case 3: {
                config.sendFiles.set(true);
                break;
            }
            case 4: {
                config.sendLogFile.set(true);
                break;
            }
            case 5: {
                config.email.set(g.getOptarg());
                break;
            }
            case 6: {
                if (userSpecifiedNameAlreadySet) {
                    usage();
                }
                config.userSpecifiedName.set(g.getOptarg());
                userSpecifiedNameAlreadySet = true;
                break;
            }
            case 7: {
                if (userSpecifiedDescriptionAlreadySet) {
                    usage();
                }
                config.userSpecifiedDescription.set(g.getOptarg());
                userSpecifiedDescriptionAlreadySet = true;
                break;
            }
            case 8: {
              config.doThumbnails.set(false);
              break;
            }
            case 9: {
                config.agent.set(g.getOptarg());
                break;
            }
            case 10: {
                annotationNamespaces.add(g.getOptarg());
                break;
            }
            case 11: {
                textAnnotations.add(g.getOptarg());
                break;
            }
            case 12: {
                annotationIds.add(Long.parseLong(g.getOptarg()));
                break;
            }
            case 13:
            case 't': {
                String arg = g.getOptarg();
                log.info("Setting transfer to {}", arg);
                transfer = AbstractFileTransfer.createTransfer(arg);
                break;
            }
            case 14: {
                advUsage();
                break;
            }
            case 15: {
                String arg = g.getOptarg();
                log.info("Setting checksum algorithm to {}", arg);
                config.checksumAlgorithm.set(arg);
                break;
            }
            case 's': {
                config.hostname.set(g.getOptarg());
                break;
            }
            case 'u': {
                config.username.set(g.getOptarg());
                break;
            }
            case 'w': {
                config.password.set(g.getOptarg());
                break;
            }
            case 'k': {
                config.sessionKey.set(g.getOptarg());
                break;
            }
            case 'p': {
                config.port.set(Integer.parseInt(g.getOptarg()));
                break;
            }
            case 'd': {
                config.targetClass.set(Dataset.class.getName());
                config.targetId.set(Long.parseLong(g.getOptarg()));
                break;
            }
            case 'r': {
                config.targetClass.set(Screen.class.getName());
                config.targetId.set(Long.parseLong(g.getOptarg()));
                break;
            }
            case 'n': {
                if (userSpecifiedNameAlreadySet) {
                    usage();
                }
                config.userSpecifiedName.set(g.getOptarg());
                userSpecifiedNameAlreadySet = true;
                break;            }
            case 'x': {
                if (userSpecifiedDescriptionAlreadySet) {
                    usage();
                }
                config.userSpecifiedDescription.set(g.getOptarg());
                userSpecifiedDescriptionAlreadySet = true;
                break;            }
            case 'f': {
                getUsedFiles = true;
                break;
            }
            case 'c': {
                config.contOnError.set(true);
                break;
            }
            case 'l': {
                config.readersPath.set(g.getOptarg());
                break;
            }
            case 'h': {
                usage(); // exits
            }
            default: {
                usage(); // exits
            }
            }
        }

        // CGB: Needs replacing with logback equivalent.
        // Let the user know at what level we're logging
        //log.info(String.format(
        //        "Log levels -- Bio-Formats: %s OMERO.importer: %s",
        //        org.apache.log4j.Logger.getLogger("loci").getLevel(),
        //        org.apache.log4j.Logger.getLogger("ome.formats").getLevel()));

        List<Annotation> annotations =
            toTextAnnotations(annotationNamespaces, textAnnotations);
        for (Long id: annotationIds)
        {
            CommentAnnotationI unloadedAnnotation =
                new CommentAnnotationI(id, false);
            annotations.add(unloadedAnnotation);
        }
        config.annotations.set(annotations);

        // Start the importer and import the image we've been given
        String[] rest = new String[args.length - g.getOptind()];
        System.arraycopy(args, g.getOptind(), rest, 0, args.length
                - g.getOptind());

        CommandLineImporter c = null;
        int rc = 0;
        try {

            if (rest.length == 1 && "-".equals(rest[0])) {
                rest = stdin();
            }
            c = new CommandLineImporter(config, rest, getUsedFiles, transfer);
            rc = c.start();
        } catch (Throwable t) {
            log.error("Error during import process.", t);
            rc = 2;
        } finally {
            if (c != null) {
                c.cleanup();
            }
        }
        System.exit(rc);
    }

    /**
     * Reads a list of paths from stdin.
     * @return
     */
    static String[] stdin() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        List<String> files = new ArrayList<String>();
        while (true) {
            String str = in.readLine();
            if (str == null) {
                break;
            } else {
                str = str.trim();
                if (str.length() > 0) {
                    files.add(str);
                }
            }
        }
        return files.toArray(new String[0]);
    }

}
