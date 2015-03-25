/*
 *   Copyright (C) 2009-2014 University of Dundee & Open Microscopy Environment.
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
import java.util.Map;
import java.util.HashMap;

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
import ome.formats.importer.exclusions.AbstractFileExclusion;
import ome.formats.importer.exclusions.FileExclusion;
import ome.formats.importer.transfers.AbstractFileTransfer;
import ome.formats.importer.transfers.CleanupFailure;
import ome.formats.importer.transfers.FileTransfer;
import ome.formats.importer.transfers.UploadFileTransfer;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceInterfacePrx;
import omero.cmd.HandlePrx;
import omero.cmd.Response;
import omero.grid.ImportProcessPrx;
import omero.grid.ImportProcessPrxHelper;
import omero.model.Annotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.Screen;

import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base entry point for the CLI version of the OMERO importer.
 *
 * @author Chris Allan <callan@glencoesoftware.com>
 * @author Josh Moore josh at glencoesoftware.com
 */
public class CommandLineImporter {

    public static final int DEFAULT_WAIT = -1;

    /** Logger for this class. */
    private static Logger log = LoggerFactory.getLogger(CommandLineImporter.class);

    /** StopWatch instance **/
    private final StopWatch sw = new StopWatch();

    /** Name that will be used for usage() */
    private static final String APP_NAME = "importer-cli";

    /** Configuration used by all components */
    public final ImportConfig config;

    /** {@link FileTransfer} mechanism to be used for uploading */
    public final FileTransfer transfer;

    /** {@link FileExclusion} mechanisms for skipping candidates */
    public final List<FileExclusion> exclusions = new ArrayList<FileExclusion>();

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
        this(config, paths, getUsedFiles, new UploadFileTransfer(), DEFAULT_WAIT);
    }

    /**
     * Legacy constructor without any file exclusions.
     */
    public CommandLineImporter(final ImportConfig config, String[] paths,
            boolean getUsedFiles, FileTransfer transfer, int minutesToWait)
                    throws Exception {
        this(config, paths, getUsedFiles, new UploadFileTransfer(), null, DEFAULT_WAIT);
    }

    /**
     * Main entry class for the application.
     */
    public CommandLineImporter(final ImportConfig config, String[] paths,
            boolean getUsedFiles, FileTransfer transfer,
            List<FileExclusion> exclusions, int minutesToWait)
                    throws Exception {

        this.config = config;
        config.loadAll();

        this.getUsedFiles = getUsedFiles;
        this.reader = new OMEROWrapper(config);
        this.handler = new ErrorHandler(config);
        this.transfer = transfer;
        if (exclusions != null) {
            this.exclusions.addAll(exclusions);
        }
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

            if (config.checkUpgrade.get()) {
                config.isUpgradeNeeded();
            }
            else
            {
                log.debug("UpgradeCheck disabled.");
            }
            store = config.createStore();
            store.logVersionInfo(config.getIniVersionNumber());
            reader.setMetadataOptions(
                    new DefaultMetadataOptions(MetadataLevel.ALL));

            library = new ImportLibrary(store, reader,
                    transfer, exclusions, minutesToWait);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                cleanup();
            }
        });
    }

    /**
     * Look for all {@link ImportProcessPrx} in the current session and close
     * them if they return a non-null {@link Response} (i.e. they are done).
     */
    public static int closeCompleted(ImportConfig config) throws Exception {
        config.loadAll();
        OMEROMetadataStoreClient client = config.createStore();
        ImportCloser closer = new ImportCloser(client);
        closer.closeCompleted();
        log.info("{} service(s) processed", closer.getProcessed());
        return closer.getErrors();
    }

    /**
     * Look for all {@link ImportProcessPrx} in the current session and close
     * them if they return a non-null {@link Response} (i.e. they are done).
     */
    public static int waitCompleted(ImportConfig config) throws Exception {
        long wait = 5000L;
        config.loadAll();
        OMEROMetadataStoreClient client = config.createStore();
        while (true) {
            ImportCloser closer = new ImportCloser(client);
            closer.closeCompleted();
            if (closer.getProcessed() == 0) {
                // In this case, there's nothing to do. Exit successfully.
                return 0;
            }
            int closed = closer.getClosed();
            int open = closer.getProcessed() - closed;
            int errs = closer.getErrors();
            if (errs > 0) {
                log.warn("{} open. {} closed. {} errors", open, closed, errs);
            } else {
                log.info("{} open. {} closed.", open, closed);
            }
            try {
                log.debug("Sleeping {} ms", wait);
                Thread.sleep(wait);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    public int start() {
        boolean successful = true;

        if (getUsedFiles) {
            try {
                candidates.print();
                return 0;
            } catch (Throwable t) {
                log.error("Error retrieving used files.", t);
                return 1;
            }
        } else if (candidates.size() < 1) {
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
        } else {
            sw.start();
            library.addObserver(new LoggingImportMonitor());
            // error handler has been configured in constructor from main args
            library.addObserver(this.handler);

            successful = library.importCandidates(config, candidates);

            try {
                List<String> paths = new ArrayList<String>();
                for (ImportContainer ic : candidates.getContainers()) {
                    paths.addAll(Arrays.asList(ic.getUsedFiles()));
                }

                // No exceptions are thrown from importCandidates and therefore
                // we must manually check for the number of errors. If there
                // are **ANY** then we refuse to post-process these paths,
                // which primarily only means that MoveFileTransfer will not
                // get a chance to delete the files.
                transfer.afterTransfer(handler.errorCount(), paths);

            } catch (CleanupFailure e) {
                log.error("Failed to cleanup {} files", e.getFailedFiles()
                        .size());
                return 3;
            } finally {
                sw.stop();
                report();
            }
        }

        return successful ? 0 : 2;
    }

    void report() {
        boolean report = config.sendReport.get();
        boolean files = config.sendFiles.get();
        boolean logs = config.sendLogFile.get();
        if (report) {
            handler.update(null, new ImportEvent.DEBUG_SEND(files, logs));
        }
        library.notifyObservers(new ImportEvent.IMPORT_SUMMARY(sw.getTime(),
                handler.errorCount()));
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
        System.err.println(String.format("\n"
            + " Usage:  %s [OPTION]... [path [path ...]]... \n"
            + "   or:   %s [OPTION]... - \n"
            + "\n"
            + "Import any number of files into an OMERO instance.\n"
            + "If \"-\" is the only path, a list of files or directories \n"
            + "is read from standard in. Directories will be searched for \n"
            + "all valid imports.\n"
            + "\n"
            + "Session arguments:\n"
            + "  Mandatory arguments for creating a session are 1- either the OMERO server hostname,\n"
            + "username and password or 2- the OMERO server hostname and a valid session key.\n"
            + "  -s SERVER\tOMERO server hostname\n"
            + "  -u USER\tOMERO username\n"
            + "  -w PASSWORD\tOMERO password\n"
            + "  -k KEY\tOMERO session key (UUID of an active session)\n"
            + "  -p PORT\tOMERO server port (default: 4064)\n"
            + "\n"
            + "Naming arguments:\n"
            + "All naming arguments are optional\n"
            + "  -n NAME\t\t\t\tImage or plate name to use\n"
            + "  -x DESCRIPTION\t\t\tImage or plate description to use\n"
            + "  --name NAME\t\t\t\tImage or plate name to use\n"
            + "  --description DESCRIPTION\t\tImage or plate description to use\n"
            + "\n"
            + "Optional arguments:\n"
            + "  -h\t\t\t\t\tDisplay this help and exit\n"
            + "  -f\t\t\t\t\tDisplay the used files and exit\n"
            + "  -c\t\t\t\t\tContinue importing after errors\n"
            + "  -l READER_FILE\t\t\tUse the list of readers rather than the default\n"
            + "  -d DATASET_ID\t\t\t\tOMERO dataset ID to import image into\n"
            + "  -r SCREEN_ID\t\t\t\tOMERO screen ID to import plate into\n"

            + "  --report\t\t\t\tReport errors to the OME team\n"
            + "  --upload\t\t\t\tUpload broken files and log file (if any) with report. Required --report\n"
            + "  --logs\t\t\t\tUpload log file (if any) with report. Required --report\n"
            + "  --email EMAIL\t\t\t\tEmail for reported errors. Required --report\n"
            + "  --debug LEVEL\t\t\t\tTurn debug logging on (optional level)\n"
            + "  --annotation-ns ANNOTATION_NS\t\tNamespace to use for subsequent annotation\n"
            + "  --annotation-text ANNOTATION_TEXT\tContent for a text annotation (requires namespace)\n"
            + "  --annotation-link ANNOTATION_LINK\tComment annotation ID to link all images to\n"
            + "\n"
            + "Examples:\n"
            + "\n"
            + "  $ %s -s localhost -u user -w password -d 50 foo.tiff\n"
            + "  $ %s -s localhost -u user -w password -d Dataset:50 foo.tiff\n"
            + "  $ %s -f foo.tiff\n"
            + "  $ %s -s localhost -u username -w password -d 50 --debug ALL foo.tiff\n"
            + "\n"
            + "For additional information, see:\n"
            + "http://www.openmicroscopy.org/site/support/omero5.1/users/cli/import.html\n"
            + "Report bugs to <ome-users@lists.openmicroscopy.org.uk>",
            APP_NAME, APP_NAME, APP_NAME, APP_NAME, APP_NAME, APP_NAME));
        System.exit(1);
    }

    /**
     * Prints advanced usage to STDERR and exits with return code 1.
     */
    public static void advUsage() {
        System.err.println("\n"
            + "ADVANCED OPTIONS:\n\n"
            + "  These options are not intended for general use. Make sure you have read the\n"
            + "  documentation regarding them. They may change in future releases.\n\n"
            + "  In-place imports:\n"
            + "  -----------------\n\n"
            + "    --transfer=ARG          \tFile transfer method\n\n"
            + "        General options:    \t\n"
            + "          upload          \t# Default\n"
            + "          upload_rm       \t# Caution! File upload followed by source deletion.\n"
            + "          some.class.Name \t# Use a class on the CLASSPATH.\n\n"
            + "        Server-side options:\t\n"
            + "          ln              \t# Use hard-link.\n"
            + "          ln_s            \t# Use soft-link.\n"
            + "          ln_rm           \t# Caution! Hard-link followed by source deletion.\n"
            + "          cp              \t# Use local copy command.\n"
            + "          cp_rm           \t# Caution! Copy followed by source deletion.\n\n"
            + "\n"
            + "  e.g. $ bin/omero import -- --transfer=ln_s foo.tiff\n"
            + "       $ ./importer-cli --transfer=ln bar.tiff\n"
            + "       $ CLASSPATH=mycode.jar ./importer-cli --transfer=com.example.MyTransfer baz.tiff\n"
            + "\n"
            + "  Background imports:\n"
            + "  -------------------\n\n"
            + "    --auto_close            \tClose completed imports immediately.\n\n"
            + "    --minutes_wait=ARG      \tChoose how long the importer will wait on server-side processing.\n"
            + "                            \tARG > 0 implies the number of minutes to wait.\n"
            + "                            \tARG = 0 exits immediately. Use a *_completed option to clean up.\n"
            + "                            \tARG < 0 waits indefinitely. This is the default.\n\n"
            + "    --close_completed       \tClose completed imports.\n\n"
            + "    --wait_completed        \tWait for all background imports to complete.\n\n"
            + "\n"
            + "  e.g. $ bin/omero import -- --minutes_wait=0 file1.tiff file2.tiff file3.tiff\n"
            + "       $ ./importer-cli --minutes_wait=0 some_directory/\n"
            + "       $ ./importer-cli --wait_completed # Waits on all 3 imports.\n"
            + "\n"
            + "  File exclusion:\n"
            + "  ---------------\n\n"
            + "    --exclude=filename      \tExclude files based on filename.\n\n"
            + "\n"
            + "  e.g. $ bin/omero import -- --exclude=filename foo.tiff # First-time imports\n"
            + "       $ bin/omero import -- --exclude=filename foo.tiff # Second-time skips\n"
            + "\n"
            + "  Import speed:\n"
            + "  -------------\n\n"
            + "    --checksum-algorithm=ARG\tChoose a possibly faster algorithm for detecting file corruption,\n"
            + "                            \te.g. Adler-32 (fast), CRC-32 (fast), File-Size-64 (fast),\n"
            + "                            \t     MD5-128, Murmur3-32, Murmur3-128,\n"
            + "                            \t     SHA1-160 (slow, default)\n\n"
            + "  e.g. $ bin/omero import -- --checksum-algorithm=CRC-32 foo.tiff\n"
            + "       $ ./importer-cli --checksum-algorithm=Murmur3-128 bar.tiff\n\n"
            + "    --no-stats-info\t\tDisable calculation of minima and maxima"
            + " when as part of the Bio-Formats reader metadata\n\n"
            + "  e.g. $ bin/omero import -- --no-stats-info foo.tiff\n"
            + "       $ ./importer-cli --no-stats-info bar.tiff\n\n"
            + "  --no-thumbnails\t\tDo not perform thumbnailing after import\n\n"
            + "  e.g. $ bin/omero import -- --no-thumbnails foo.tiff\n"
            + "       $ ./importer-cli --no-thumbnails bar.tiff\n\n"
            + "    --no-upgrade-check\t\tDisable upgrade check for each import\n"
            + "  e.g. $ bin/omero import -- --no-upgrade-check foo.tiff\n"
            + "       $ ./importer-cli --no-upgrade-check bar.tiff\n\n"
            + "\n"
            + "  Feedback:\n"
            + "  ---------\n\n"
            + "    --qa-baseurl=ARG\tSpecify the base URL for reporting feedback\n"
            + "  e.g. $ bin/omero import broken_image.tif"
            + " -- --email EMAIL --report --upload --logs"
            + " --qa-baseurl=https://qa.staging.openmicroscopy.org/qa\n"
            + "       $ ./importer-cli broken_image.tif"
            + " --email EMAIL --report --upload --logs"
            + " --qa-baseurl=https://qa.staging.openmicroscopy.org/qa\n"
            + "\n"
            + "Report bugs to <ome-users@lists.openmicroscopy.org.uk>");
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
    public static void main(String[] args) throws Exception {

        int minutesToWait = DEFAULT_WAIT;
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
        LongOpt name = new LongOpt(
                "name", LongOpt.REQUIRED_ARGUMENT, null, 6);
        LongOpt description = new LongOpt(
                "description", LongOpt.REQUIRED_ARGUMENT, null, 7);
        LongOpt noThumbnails = new LongOpt(
                "no-thumbnails", LongOpt.NO_ARGUMENT, null, 8);
        LongOpt agent = new LongOpt(
                "agent", LongOpt.REQUIRED_ARGUMENT, null, 9);
        LongOpt annotationNamespace =
            new LongOpt("annotation-ns", LongOpt.REQUIRED_ARGUMENT, null, 10);
        LongOpt annotationText =
            new LongOpt("annotation-text", LongOpt.REQUIRED_ARGUMENT,
                        null, 11);
        LongOpt annotationLink =
            new LongOpt("annotation-link", LongOpt.REQUIRED_ARGUMENT,
                        null, 12);

        // ADVANCED OPTIONS
        LongOpt advancedHelp =
                new LongOpt("advanced-help", LongOpt.NO_ARGUMENT, null, 13);
        LongOpt transferOpt =
                new LongOpt("transfer", LongOpt.REQUIRED_ARGUMENT, null, 14);
        LongOpt checksumAlgorithm =
                new LongOpt("checksum-algorithm", LongOpt.REQUIRED_ARGUMENT, null, 15);
        LongOpt minutesWait =
                new LongOpt("minutes_wait", LongOpt.REQUIRED_ARGUMENT, null, 16);
        LongOpt closeCompleted =
                new LongOpt("close_completed", LongOpt.NO_ARGUMENT, null, 17);
        LongOpt waitCompleted =
                new LongOpt("wait_completed", LongOpt.NO_ARGUMENT, null, 18);
        LongOpt autoClose =
                new LongOpt("auto_close", LongOpt.NO_ARGUMENT, null, 19);
        LongOpt exclude =
                new LongOpt("exclude", LongOpt.REQUIRED_ARGUMENT, null, 20);

        LongOpt qaBaseURL = new LongOpt(
                "qa-baseurl", LongOpt.REQUIRED_ARGUMENT, null, 21);

        LongOpt noStatsInfo =
                new LongOpt("no-stats-info", LongOpt.NO_ARGUMENT, null, 22);
        LongOpt noUpgradeCheck =
                new LongOpt("no-upgrade-check", LongOpt.NO_ARGUMENT, null, 23);

        // DEPRECATED OPTIONS
        LongOpt plateName = new LongOpt(
                "plate_name", LongOpt.REQUIRED_ARGUMENT, null, 90);
        LongOpt plateDescription = new LongOpt(
                "plate_description", LongOpt.REQUIRED_ARGUMENT, null, 91);
        LongOpt noThumbnailsDeprecated = new LongOpt(
                "no_thumbnails", LongOpt.NO_ARGUMENT, null, 92);
        LongOpt checksumAlgorithmDeprecated = new LongOpt(
                "checksum_algorithm", LongOpt.REQUIRED_ARGUMENT, null, 93);
        LongOpt annotationNamespaceDeprecated =
            new LongOpt("annotation_ns", LongOpt.REQUIRED_ARGUMENT, null, 94);
        LongOpt annotationTextDeprecated =
            new LongOpt("annotation_text", LongOpt.REQUIRED_ARGUMENT, null, 95);
        LongOpt annotationLinkDeprecated =
            new LongOpt("annotation_link", LongOpt.REQUIRED_ARGUMENT, null, 96);

        Getopt g = new Getopt(APP_NAME, args, "cfl:s:u:w:d:r:k:x:n:p:h",
                new LongOpt[] { debug, report, upload, logs, email,
                                name, description, noThumbnails,
                                agent, annotationNamespace, annotationText,
                                annotationLink, transferOpt, advancedHelp,
                                checksumAlgorithm, minutesWait,
                                closeCompleted, waitCompleted, autoClose,
                                exclude, noStatsInfo,
                                noUpgradeCheck, qaBaseURL,
                                plateName, plateDescription,
                                noThumbnailsDeprecated,
                                checksumAlgorithmDeprecated,
                                annotationNamespaceDeprecated,
                                annotationTextDeprecated,
                                annotationLinkDeprecated
                                });
        int a;

        boolean doCloseCompleted = false;
        boolean doWaitCompleted = false;
        boolean getUsedFiles = false;
        config.agent.set("importer-cli");

        // Create a map for handling conflicting option strings
        Map<String, Boolean> conflictingArguments = new HashMap<String, Boolean>();
        conflictingArguments.put("userSpecifiedName", false);
        conflictingArguments.put("userSpecifiedDescription", false);
        conflictingArguments.put("checksumAlgorithm", false);

        List<String> annotationNamespaces = new ArrayList<String>();
        List<String> textAnnotations = new ArrayList<String>();
        List<Long> annotationIds = new ArrayList<Long>();
        List<FileExclusion> exclusions = new ArrayList<FileExclusion>();
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
                setArgument(conflictingArguments, "userSpecifiedName");
                config.userSpecifiedName.set(g.getOptarg());
                break;
            }
            case 7: {
                setArgument(conflictingArguments, "userSpecifiedDescription");
                config.userSpecifiedDescription.set(g.getOptarg());
                break;
            }
            case 8: {
                log.info("Skipping thumbnails creation");
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
            case 13: {
                advUsage();
                break;
            }

            // ADVANCED START -------------------------------------------------
            case 14: {
                String arg = g.getOptarg();
                log.info("Setting transfer to {}", arg);
                transfer = AbstractFileTransfer.createTransfer(arg);
                break;
            }
            case 15: {
                setArgument(conflictingArguments, "checksumAlgorithm");
                String arg = g.getOptarg();
                log.info("Setting checksum algorithm to {}", arg);
                config.checksumAlgorithm.set(arg);
                break;
            }
            case 16: {
                minutesToWait = Integer.parseInt(g.getOptarg());
                log.info("Setting minutes to wait to {}", minutesToWait);
                break;
            }
            case 17: {
                doCloseCompleted = true;
                break;
            }
            case 18: {
                doWaitCompleted = true;
                break;
            }
            case 19: {
                minutesToWait = 0;
                config.autoClose.set(true);
                break;
            }
            case 20: {
                String arg = g.getOptarg();
                log.info("Adding exclusion: {}", arg);
                FileExclusion exclusion = AbstractFileExclusion.createExclusion(arg);
                if (exclusion != null) {
                    exclusions.add(exclusion);
                }
                break;
            }
            case 21: {
                config.qaBaseURL.set(g.getOptarg());
                break;
            }
            case 22: {
                log.info("Skipping minimum/maximum computation");
                config.noStatsInfo.set(true);
                break;
            }
            case 23: {
                log.info("Disabling upgrade check");
                config.checkUpgrade.set(false);
                break;
            }
            // ADVANCED END ---------------------------------------------------
            // DEPRECATED OPTIONS
            case 90: {
                setArgument(conflictingArguments, "userSpecifiedName");
                config.userSpecifiedName.set(g.getOptarg());
                break;
            }
            case 91: {
                setArgument(conflictingArguments, "userSpecifiedDescription");
                config.userSpecifiedDescription.set(g.getOptarg());
                break;
            }
            case 92: {
                log.info("Skipping thumbnails creation");
                config.doThumbnails.set(false);
                break;
            }
            case 93: {
                setArgument(conflictingArguments, "checksumAlgorithm");
                String arg = g.getOptarg();
                log.info("Setting checksum algorithm to {}", arg);
                config.checksumAlgorithm.set(arg);
                break;
            }
            case 94: {
                annotationNamespaces.add(g.getOptarg());
                break;
            }
            case 95: {
                textAnnotations.add(g.getOptarg());
                break;
            }
            case 96: {
                annotationIds.add(Long.parseLong(g.getOptarg()));
                break;
            }
            // END OF DEPRECATED OPTIONS
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
                String datasetString = g.getOptarg();
                if (datasetString.startsWith("Dataset:")) {
                    datasetString = datasetString.substring(
                            "Dataset:".length());
                }
                config.targetId.set(Long.parseLong(datasetString));
                config.targetClass.set(Dataset.class.getName());
                break;
            }
            case 'r': {
                String screenString = g.getOptarg();
                if (screenString.startsWith("Screen:")) {
                    screenString = screenString.substring(
                            "Screen:".length());
                }
                config.targetId.set(Long.parseLong(screenString));
                config.targetClass.set(Screen.class.getName());
                break;
            }
            case 'n': {
                setArgument(conflictingArguments, "userSpecifiedName");
                config.userSpecifiedName.set(g.getOptarg());
                break;
            }
            case 'x': {
                setArgument(conflictingArguments, "userSpecifiedDescription");
                config.userSpecifiedDescription.set(g.getOptarg());
                break;
            }
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

        // Let the user know at what level we're logging
        log.info(String.format(
                "Log levels -- Bio-Formats: %s OMERO.importer: %s",
                ((ch.qos.logback.classic.Logger)LoggerFactory
                    .getLogger("loci")).getLevel(),
                ((ch.qos.logback.classic.Logger)LoggerFactory
                    .getLogger("ome.formats")).getLevel()));

        // Start the importer and import the image we've been given
        String[] rest = new String[args.length - g.getOptind()];
        System.arraycopy(args, g.getOptind(), rest, 0, args.length
                - g.getOptind());

        if (doCloseCompleted || doWaitCompleted) {
            if (rest.length > 0) {
                log.error("Files found with completed option: "+
                        Arrays.toString(rest));
                System.exit(-2); // EARLY EXIT!
            } else if (doCloseCompleted) {
                System.exit(closeCompleted(config)); // EARLY EXIT!
            } else if (doWaitCompleted) {
                System.exit(waitCompleted(config)); // EARLY EXIT!
            }
        }

        List<Annotation> annotations =
            toTextAnnotations(annotationNamespaces, textAnnotations);
        for (Long id: annotationIds)
        {
            CommentAnnotationI unloadedAnnotation =
                new CommentAnnotationI(id, false);
            annotations.add(unloadedAnnotation);
        }
        config.annotations.set(annotations);

        CommandLineImporter c = null;
        int rc = 0;
        try {

            if (rest.length == 1 && "-".equals(rest[0])) {
                rest = stdin();
            }
            c = new CommandLineImporter(config, rest, getUsedFiles,
                    transfer, exclusions, minutesToWait);
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
     * Set a conflicting argument and return the usage if the key is already
     * set
     * @param map map of conflicting properties
     * @param key configuration property to be set
     */
    public static void setArgument(Map <String, Boolean> map, String key) {
        if (map.get(key)) {
            // The property has already been set
            log.error("Conflicting arguments setting {}.", key);
            usage();
        }
        map.put(key, true);
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


class ImportCloser {

    private final static Logger log = LoggerFactory.getLogger(ImportCloser.class);

    List<ImportProcessPrx> imports;
    int closed = 0;
    int errors = 0;
    int processed = 0;

    ImportCloser(OMEROMetadataStoreClient client) throws Exception {
        this.imports = getImports(client);
    }

    void closeCompleted() {
        for (ImportProcessPrx imPrx : imports) {
            try {
                processed++;
                String logName = imPrx.toString().split("\\s")[0];
                HandlePrx handle = imPrx.getHandle();
                if (handle != null) {
                    Response rsp = handle.getResponse();
                    if (rsp != null) {
                        log.info("Done: {}", logName);
                        imPrx.close();
                        closed++;
                        continue;
                    }
                }
                log.info("Running: {}", logName);
            } catch (Exception e) {
                errors++;
                log.warn("Failure accessing service", e);
            }
        }
    }

    int getClosed() {
        return closed;
    }

    int getErrors() {
        return errors;
    }

    int getProcessed() {
        return processed;
    }

    private static List<ImportProcessPrx> getImports(OMEROMetadataStoreClient client) throws Exception {
        final List<ImportProcessPrx> rv = new ArrayList<ImportProcessPrx>();
        final ServiceFactoryPrx sf = client.getServiceFactory();
        final List<String> active = sf.activeServices();
        for (String service : active) {
            try {
                final ServiceInterfacePrx prx = sf.getByName(service);
                final ImportProcessPrx imPrx = ImportProcessPrxHelper.checkedCast(prx);
                if (imPrx != null) {
                    try {
                        imPrx.ice_ping();
                        rv.add(imPrx);
                    } catch (Ice.ObjectNotExistException onee) {
                        // ignore
                    }
                }
            } catch (Exception e) {
                log.warn("Failure accessing active service", e);
            }
        }
        return rv;
    }
}
