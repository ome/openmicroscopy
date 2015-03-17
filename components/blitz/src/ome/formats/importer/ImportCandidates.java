/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static omero.rtypes.rint;
import static omero.rtypes.rstring;

import loci.formats.FileInfo;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.MissingLibraryException;
import loci.formats.UnknownFormatException;
import loci.formats.UnsupportedCompressionException;
import loci.formats.in.DefaultMetadataOptions;
import loci.formats.in.MetadataLevel;
import ome.formats.ImageNameMetadataStore;
import ome.formats.importer.util.ErrorHandler;
import omero.model.Pixels;
import omero.model.PixelsI;
import omero.model.PixelsType;
import omero.model.PixelsTypeI;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class which given any {@link File} object will determine the correct
 * number and members of a given import. This facility permits iterating over a
 * directory.
 *
 * This class is NOT thread-safe.
 *
 * @since Beta4.1
 */
public class ImportCandidates extends DirectoryWalker
{

    /**
     * Event raised during a pass through the directory structure given to
     * {@link ImportCandidates}. A {@link Scanning} event will not necessarily
     * be raised for every file or directory, but the values will be valid for
     * each event.
     *
     * If {@link #totalFiles} is less than 0, then the directory is currently be
     * scanned and the count is unknown. Once {@link #totalFiles} is positive,
     * it will remain constant.
     *
     * If {@link #cancel()} is called, then directory searching will cease. The
     * {@link ImportCandidates} instance will be left with <em>no</em>
     * {@link ImportContainer}s.
     */
    public static class SCANNING extends ImportEvent
    {
        public final File file;
        public final int depth;
        public final int numFiles;
        public final int totalFiles;
        private boolean cancel = false;

        public SCANNING(File file, int depth, int numFiles, int totalFiles)
        {
            this.file = file;
            this.depth = depth;
            this.numFiles = numFiles;
            this.totalFiles = totalFiles;
        }

        /**
         * Can be called to cancel the current action.
         */
        public void cancel()
        {
            this.cancel = true;
        }

        public String toLog()
        {
            int l = file.toString().length() - 16;
            if (l < 0)
            {
                l = 0;
            }
            String f = file.toString().substring(l);
            return super.toLog() + String.format(": Depth:%s Num: %4s Tot: %4s File: %s",
                    depth, numFiles, (totalFiles < 0 ? "n/a" : totalFiles), f);
        }
    }

    /**
     * Marker exception raised if the {@link SCANNING#cancel()} method is
     * called by an {@link IObserver} instance.
     */
    public static class CANCEL extends RuntimeException {

	private static final long serialVersionUID = 1L;};

    final private static Logger log = LoggerFactory.getLogger(ImportCandidates.class);

    final public static int DEPTH = Integer.valueOf(
            System.getProperty("omero.import.depth","4"));
    final public static MetadataLevel METADATA_LEVEL =
        MetadataLevel.valueOf(System.getProperty(
                "omero.import.metadata.level","MINIMUM"));

    final private IObserver observer;
    final private OMEROWrapper reader;
    final private Set<String> allFiles = new HashSet<String>();
    final private Map<String, List<String>> usedBy = new LinkedHashMap<String, List<String>>();
    final private List<ImportContainer> containers = new ArrayList<ImportContainer>();
    final private long start = System.currentTimeMillis();

    /**
     * Time take for {@link IFormatReader#setId()}
     */
    long readerTime = 0;

    /**
     * Current count of calls to {@link IFormatReader#setId()}.
     */
    int setids = 0;

    /**
     * Number of times UNKNOWN_EVENT was raised
     */
    int unknown = 0;

    /**
     * Current count of files processed. This will be incremented in two phases:
     * once during directory counting, and once during parsing.
     */
    int count = 0;

    /**
     * Total number of files which have been / will be examined. During the
     * first pass, this value is negative.
     */
    int total = -1;

    /**
     * Whether or not one of the {@link SCANNING} events had {@link SCANNING#cancel()}
     * called.
     */
    boolean cancelled = false;

    /**
     * Calls {@link #ImportCandidates(int, OMEROWrapper, String[], IObserver)}
     * with {@link #DEPTH} as the first argument.
     *
     * @param reader
     *            instance used for parsing each of the paths. Not used once the
     *            constructor completes.
     * @param paths
     *            file paths which are searched. May be directories.
     * @param observer
     *            {@link IObserver} which will monitor any exceptions during
     *            {@link OMEROWrapper#setId(String)}. Otherwise no error
     *            reporting takes place.
     */
    public ImportCandidates(OMEROWrapper reader, String[] paths,
            IObserver observer)
    {
        this(DEPTH, reader, paths, observer);
    }

    /**
     * Main constructor which iterates over all the paths calling
     * {@link #walk(File, Collection)} and permitting a descent to the given
     * depth.
     *
     * @param depth
     *            number of directory levels to search down.
     * @param reader
     *            instance used for parsing each of the paths. Not used once the
     *            constructor completes.
     * @param paths
     *            file paths which are searched. May be directories.
     * @param observer
     *            {@link IObserver} which will monitor any exceptions during
     *            {@link OMEROWrapper#setId(String)}. Otherwise no error
     *            reporting takes place.
     */
    public ImportCandidates(int depth, OMEROWrapper reader, String[] paths,
            IObserver observer)
    {
        super(TrueFileFilter.INSTANCE, depth);
        this.reader = reader;
        this.observer = observer;
        log.info(String.format("Depth: %s Metadata Level: %s", depth,
                METADATA_LEVEL));

        if (paths != null && paths.length == 2 && "".equals(paths[0])
                && "".equals(paths[1]))
        {
            // Easter egg for testing.
            // groups is not null, therefore usage() won't need to be
            // called.
            System.exit(0);
            return;
        }

        if (paths == null || paths.length == 0)
        {
            return;
        }

        Groups g;
        try {
            execute(paths);
            total = count;
            count = 0;
            execute(paths);
            g = new Groups(usedBy);
            g.parse(containers);
            long totalElapsed = System.currentTimeMillis() - start;
            log.info(String.format("%s file(s) parsed into "
                    + "%s group(s) with %s call(s) to setId in "
                    + "%sms. (%sms total) [%s unknowns]", this.total, size(), this.setids,
                    readerTime, totalElapsed, unknown));
        } catch (CANCEL c)
        {
            log.info(String.format("Cancelling search after %sms "
                    + "with %s containers found (%sms in %s calls to setIds)",
                    (System.currentTimeMillis() - start), containers.size(),
                    readerTime, setids));
            containers.clear();
            cancelled = true;
            g = null;
            total = -1;
            count = -1;
        }

    }

    /**
     * Prints the "standard" representation of the groups, which is parsed by
     * other software layers. The format is: 1) any empty lines are ignored, 2)
     * any blocks of comments separate groups, 3) each group is begun by the
     * "key", 4) all other files in a group will also be imported.
     *
     * Similar logic is contained in {@link Groups#print()} but it does not
     * take the ordering of the used files into account.
     */
    public void print()
    {
        if (containers == null)
        {
            return;
        }
        for (ImportContainer container : containers)
        {
            System.out.println("#======================================");
            System.out.println(String.format(
                    "# Group: %s SPW: %s Reader: %s", container.getFile(),
                    container.getIsSPW(), container.getReader()));
            for (String file : container.getUsedFiles())
            {
                System.out.println(file);
            }
        }
    }

    /**
     * @return containers size
     */
    public int size()
    {
        return containers.size();
    }

    /**
     * @return if import was cancelled
     */
    public boolean wasCancelled()
    {
        return cancelled;
    }

    /**
     * @return array of string paths for files in containers
     */
    public List<String> getPaths()
    {
        List<String> paths = new ArrayList<String>();
        for (ImportContainer i : containers)
        {
            paths.add(i.getFile().getAbsolutePath());
        }
        return paths;
    }

    /**
     * Retrieve reader type for file specified in path
     *
     * @param path - absolute path for container
     * @return reader type
     */
    public String getReaderType(String path)
    {
        for (ImportContainer i : containers) {
            if (i.getFile().getAbsolutePath().equals(path)) {
                return i.getReader();
            }
        }
        throw new RuntimeException("Unfound reader for: " + path);
    }

    /**
     * Return string of files used by container item at path
     *
     * @param path - absolute path for container
     * @return string array of used files
     */
    public String[] getUsedFiles(String path)
    {
        for (ImportContainer i : containers)
        {
            if (i.getFile().getAbsolutePath().equals(path))
            {
                return i.getUsedFiles();
            }
        }
        throw new RuntimeException("Unfound reader for: " + path);
    }

    /**
     * @return all containers as a array list
     */
    public List<ImportContainer> getContainers()
    {
        return new ArrayList<ImportContainer>(containers);
    }

    /**
     * Method called during
     * {@link ImportCandidates#ImportCandidates(OMEROWrapper, String[], IObserver)}
     * to operate on all the given paths. This will be called twice: once
     * without reading the files, and once (with the known total) using
     * {@link #reader}
     *
     * @param paths
     */
    protected void execute(String[] paths)
    {
        for (String string : paths)
        {
            try {
                File f = new File(string);
                if (f.isDirectory())
                {
                    walk(f, null);
                } else
                {
                    handleFile(f, 0, null);
                }
                // Forcing an event for each path, so that at least one
                // event is raised per file despite the count of handlefile.
                scanWithCancel(f, 0);
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Return an import container for a single file
     * @param file - single file
     * @return importer container
     */
    protected ImportContainer singleFile(File file, ImportConfig config)
    {

        if (file == null) {
            // Can't do anything about it.
            return null;
        }

        final String path = file.getAbsolutePath();
        if (!file.exists() || !file.canRead()) {
            safeUpdate(new ErrorHandler.UNREADABLE_FILE(path,
                new java.io.FileNotFoundException(path), this));
            return null;
        }

        String format = null;
        String[] usedFiles = new String[] { path };
        long start = System.currentTimeMillis();
        try {

            try {
                setids++;
                reader.close();
                reader.setMetadataStore(new ImageNameMetadataStore());
                reader.setMetadataOptions(
                        new DefaultMetadataOptions(METADATA_LEVEL));
                reader.setId(path);
                format = reader.getFormat();
                usedFiles = getOrderedFiles();
                String[] domains = reader.getReader().getDomains();
                boolean isSPW = Arrays.asList(domains).contains(FormatTools.HCS_DOMAIN);

                final String readerClassName = reader.unwrap().getClass().getCanonicalName();
                ImportContainer ic = new ImportContainer(config,
                        file, null, null,
                        readerClassName, usedFiles, isSPW);
                ic.setDoThumbnails(config.doThumbnails.get());
                ic.setNoStatsInfo(config.noStatsInfo.get());
                ic.setNoPixelsChecksum(config.noPixelsChecksum.get());
                String configImageName = config.userSpecifiedName.get();
                if (configImageName == null)
                {
                    ic.setUserSpecifiedName(file.getName());
                }
                else
                {
                    ic.setUserSpecifiedName(configImageName);
                }
                ic.setUserSpecifiedDescription(config.userSpecifiedDescription.get());
                ic.setCustomAnnotationList(config.annotations.get());
                return ic;
            } finally
            {
                readerTime += (System.currentTimeMillis() - start);
                reader.close();
            }

        } catch (UnsupportedCompressionException uce)
        {
            unknown++;
            // Handling as UNKNOWN_FORMAT for 4.3.0
            safeUpdate(new ErrorHandler.UNKNOWN_FORMAT(path, uce, this));
        } catch (UnknownFormatException ufe)
        {
            unknown++;
            safeUpdate(new ErrorHandler.UNKNOWN_FORMAT(path, ufe, this));
        } catch (MissingLibraryException mle)
        {
            safeUpdate(new ErrorHandler.MISSING_LIBRARY(path, mle, usedFiles, format));
        } catch (Throwable t)
        {
            Exception e = null;
            if (t instanceof Exception) {
                e = (Exception) t;
            }
            else {
                e = new Exception(t);
            }
            safeUpdate(new ErrorHandler.FILE_EXCEPTION(path, e, usedFiles, format));
        }

        return null;

    }

    /**
     * Retrieves Image names for each image that Bio-Formats has detected.
     * @return See A list of Image names, in the order of <i>series</i>.
     */
    private List<String> getImageNames() {
        List<String> toReturn = new ArrayList<String>();
        Map<Integer, String> imageNames = ((ImageNameMetadataStore)
                reader.getMetadataStore()).getImageNames();
        for (int i = 0; i < reader.getSeriesCount(); i++) {
            toReturn.add(imageNames.get(i));
        }
        return toReturn;
    }

    /**
     * This method uses the {@link FileInfo#usedToInitialize} flag to re-order
     * used files. All files which can be used to initialize a fileset are
     * returned first.
     */
    private String[] getOrderedFiles() {

        FileInfo[] infos = reader.getAdvancedUsedFiles(false);
        String[] usedFiles = new String[infos.length];

        int count = 0;
        for (int i = 0; i < usedFiles.length; i++) {
            if (infos[i].usedToInitialize) {
                usedFiles[count++] = infos[i].filename;
            }
        }
        for (int i = 0; i < usedFiles.length; i++) {
            if (!infos[i].usedToInitialize) {
                usedFiles[count++] = infos[i].filename;
            }
        }
        return usedFiles;
    }

    /**
     * @param f
     * @param d
     * @throws CANCEL
     */
    private void scanWithCancel(File f, int d) throws CANCEL{
        SCANNING s = new SCANNING(f, d, count, total);
        safeUpdate(s);
        if (s.cancel) {
            throw new CANCEL();
        }
    }

    /**
     * Update observers with event
     *
     * @param event
     */
    private void safeUpdate(ImportEvent event) {
        try {
            observer.update(null, event);
        } catch (Exception ex) {
            log.error(
                    String.format("Error on %s with %s", observer, event),
                    ex);
        }
    }

    /**
     * Handle a file import
     *
     * @param file - file selected
     * @param depth - depth of scan
     * @param collection
     */
    @Override
    public void handleFile(File file, int depth, Collection collection) {

        count++;

        // Our own filtering
        if (file.getName().startsWith(".")) {
            return; // Omitting dot files.
        }

        // If this is the 100th file, publish an event
        if (count%100 == 0) {
            scanWithCancel(file, depth);
        }

        // If this is just a count, return
        if (total < 0) {
            return;
        }

        // Optimization.
        if (allFiles.contains(file.getAbsolutePath())) {
            return;
        }

        ImportContainer info = singleFile(file, reader.getConfig());
        if (info == null) {
            return;
        }

        containers.add(info);
        allFiles.addAll(Arrays.asList(info.getUsedFiles()));
        for (String string : info.getUsedFiles()) {
            List<String> users = usedBy.get(string);
            if (users == null) {
                users = new ArrayList<String>();
                usedBy.put(string, users);
            }
            users.add(file.getAbsolutePath());
        }
    }

    /**
     * The {@link Groups} class servers as an algorithm for sorting the usedBy
     * map from the {@link ImportCandidates#walk(File, Collection)} method.
     * These objects should never leave the outer class.
     *
     * It is important that the Groups keep their used files ordered.
     * @see ImportCandidates#getOrderedFiles()
     */
    private static class Groups {

        private class Group {
            String key;
            List<String> theyUseMe;
            List<String> iUseThem;

            public Group(String key) {
                this.key = key;
                this.theyUseMe = new ArrayList<String>(usedBy.get(key));
                this.theyUseMe.remove(key);
                this.iUseThem = new ArrayList<String>();
                for (Map.Entry<String, List<String>> entry : usedBy.entrySet()) {
                    if (entry.getValue().contains(key)) {
                        iUseThem.add(entry.getKey());
                    }
                }
                iUseThem.remove(key);
            }

            public void removeSelfIfSingular() {
                int users = theyUseMe.size();
                int used = iUseThem.size();
                if (used <= 1 && users > 0) {
                    groups.remove(key);
                }
            }

            public String toShortString() {
                StringBuilder sb = new StringBuilder();
                sb.append(key);
                sb.append("\n");
                for (String val : iUseThem) {
                    sb.append(val);
                    sb.append("\n");
                }
                return sb.toString();
            }

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append("#======================================\n");
                sb.append("# Group: " + key);
                sb.append("\n");
                // sb.append("# Used by: ");
                // for (String key : theyUseMe) {
                // sb.append(" " + key + " ");
                // }
                // sb.append("\n");
                sb.append(key);
                sb.append("\n");
                for (String val : iUseThem) {
                    sb.append(val);
                    sb.append("\n");
                }
                return sb.toString();
            }

        }

        private final Map<String, List<String>> usedBy;
        private final Map<String, Group> groups = new LinkedHashMap<String, Group>();
        private List<String> ordering;

        Groups(Map<String, List<String>> usedBy) {
            this.usedBy = usedBy;
            for (String key : usedBy.keySet()) {
                groups.put(key, new Group(key));
            }
        }

        public int size() {
            return ordering.size();
        }

        @SuppressWarnings("unused")
		public List<String> getPaths() {
            size(); // Check.
            return ordering;
        }

        Groups parse(List<ImportContainer> containers) {
            if (ordering != null) {
                throw new RuntimeException("Already ordered");
            }
            for (Group g : new ArrayList<Group>(groups.values())) {
                g.removeSelfIfSingular();
            }
            ordering = new ArrayList<String>(groups.keySet());
            // Here we remove all the superfluous import containers.
            List<ImportContainer> copy = new ArrayList<ImportContainer>(
                    containers);
            containers.clear();
            for (String key : ordering) {
                for (ImportContainer importContainer : copy) {
                    if (importContainer.getFile().getAbsolutePath().equals(key)) {
                        containers.add(importContainer);
                    }
                }
            }
            // Now rewrite the filename chosen based on the first file in the
            // getUsedFiles.
            for (ImportContainer c : containers) {
                c.setFile(new File(c.getUsedFiles()[0]));
                c.updateUsedFilesTotalSize();
            }
            return this;
        }

        @SuppressWarnings("unused")
		void print() {
            Collection<Group> values = groups.values();
            if (values.size() == 1) {
                System.out.println(values.iterator().next().toShortString());
            } else {
                for (Group g : values) {
                    System.out.println(g);
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Group g : groups.values()) {
                sb.append(g.toString());
                sb.append("\n");
            }
            return sb.toString();
        }

        static void line(String s) {
            System.out.println("\n# ************ " + s + " ************ \n");
        }

        static Groups test(int count, Map<String, List<String>> t) {

            System.out.println("\n\n");
            line("TEST " + count);
            Groups g = new Groups(t);
            System.out.println(g);
            g.parse(new ArrayList<ImportContainer>());
            line("RESULT " + count);
            System.out.println(g);
            return g;

        }

        @SuppressWarnings("unused")
		static Groups test() {
            System.out.println("\n");
            line("NOTICE");
            System.out
                    .println("#  You have entered \"\" \"\" as the path to import.");
            System.out
                    .println("#  This runs the test suite. If you would like to");
            System.out.println("#  import the current directory use \"\".");

            Map<String, List<String>> t = new LinkedHashMap<String, List<String>>();
            t.put("a.dv.log", Arrays.asList("b.dv"));
            t.put("b.dv", Arrays.asList("b.dv"));
            test(1, t);

            t = new LinkedHashMap<String, List<String>>();
            t.put("a.png", Arrays.asList("a.png"));
            test(2, t);

            t = new LinkedHashMap<String, List<String>>();
            t.put("a.tiff", Arrays.asList("a.tiff", "c.lei"));
            t.put("b.tiff", Arrays.asList("b.tiff", "c.lei"));
            t.put("c.lei", Arrays.asList("c.lei"));
            test(3, t);

            t = new LinkedHashMap<String, List<String>>();
            t.put("overlay.tiff", Arrays.asList("overlay.tiff"));
            t.put("b.tiff", Arrays.asList("b.tiff", "overlay.tiff"));
            return test(4, t);

        }

    }

}
