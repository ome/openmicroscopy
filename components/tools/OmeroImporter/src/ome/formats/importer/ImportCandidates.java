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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import loci.formats.FormatTools;
import loci.formats.UnknownFormatException;

import ome.formats.importer.util.ErrorHandler;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class which given any {@link File} object will determine the correct
 * number and members of a given import. This facility permits iterating over a
 * directory.
 * 
 * This class is NOT thread-safe.
 * 
 * @since Beta4.1
 */
public class ImportCandidates extends DirectoryWalker {

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
    public static class SCANNING extends ImportEvent {
        public final File file;
        public final int depth;
        public final int numFiles;
        public final int totalFiles;
        private boolean cancel = false;

        public SCANNING(File file, int depth, int numFiles, int totalFiles) {
            this.file = file;
            this.depth = depth;
            this.numFiles = numFiles;
            this.totalFiles = totalFiles;
        }

        /**
         * Can be called to cancel the current action.
         */
        public void cancel() {
            this.cancel = true;
        }

        public String toLog() {
            int l = file.toString().length() - 16;
            if (l < 0) {
                l = 0;
            }
            String f = file.toString().substring(l);
            return super.toLog() + String.format(": Depth:%s Num: %4s Tot: %4s File: %s",
                    depth, numFiles, (totalFiles < 0 ? "n/a" : totalFiles), f);
        }
    }


    public static class SCANNING_FILE_EXCEPTION extends ErrorHandler.FILE_EXCEPTION {
        public final String filename;
        public final String[] usedFiles;
        public final String reader;
        public SCANNING_FILE_EXCEPTION(String filename, Exception exception, String[] usedFiles, String reader) {
            super(filename, exception, usedFiles, reader);
            this.filename = filename;
            this.usedFiles = usedFiles;
            this.reader = reader;
        }
        @Override
        public String toLog() {
            return super.toLog() + ": "+filename;
        }
    }
    
    /**
     * Marker exception raised if the {@link SCANNING#cancel()} method is
     * called by an {@link IObserver} instance.
     */
    public static class CANCEL extends RuntimeException {};
    
    final private static Log log = LogFactory.getLog(ImportCandidates.class);

    final public static int DEPTH = Integer.valueOf(System.getProperty("omero.import.depth","4"));
    final public static boolean METADATA = Boolean.valueOf(System.getProperty("omero.import.metadata","false"));
    
    final private IObserver observer;
    final private OMEROWrapper reader;
    final private Groups groups;
    final private Set<String> allFiles = new HashSet<String>();
    final private Map<String, Set<String>> usedBy = new HashMap<String, Set<String>>();
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
     * Current directory during a walk.
     */
    File dir = null;
    
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
            IObserver observer) {
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
            IObserver observer) {
        super(TrueFileFilter.INSTANCE, depth);
        this.reader = reader;
        this.observer = observer;
	log.debug(String.format("Depth: %s%s", depth,
	                       (METADATA ? " - Metadata collected!" : "")));

        if (paths != null && paths.length == 2 && "".equals(paths[0])
                && "".equals(paths[1])) {

            // Easter egg for testing.
            // groups is not null, therefore usage() won't need to be
            // called.
            groups = Groups.test();
            System.exit(0);
            return;
        }

        if (paths == null || paths.length == 0) {
            groups = null;
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
                    + "%s groups with %s calls to setId in "
                    + "%sms. (%sms total)", this.total, size(), this.setids,
                    readerTime, totalElapsed));
        } catch (CANCEL c) {
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
        groups = g;

    }

    /**
     * Prints the "standard" representation of the groups, which is parsed by
     * other software layers. The format is: 1) any empty lines are ignored, 2)
     * any blocks of comments separate groups, 3) each group is begun by the
     * "key", 4) all other files in a group will also be imported.
     */
    public void print() {
        if (groups != null) {
            groups.print();
        }
    }

    public int size() {
        return containers.size();
    }

    public boolean wasCancelled() {
        return cancelled;
    }

    public List<String> getPaths() {
        List<String> paths = new ArrayList<String>();
        for (ImportContainer i : containers) {
            paths.add(i.file.getAbsolutePath());
        }
        return paths;
    }

    public String getReaderType(String abs) {
        for (ImportContainer i : containers) {
            if (i.file.getAbsolutePath().equals(abs)) {
                return i.reader;
            }
        }
        throw new RuntimeException("Unfound reader for: " + abs);
    }

    public String[] getUsedFiles(String abs) {
        for (ImportContainer i : containers) {
            if (i.file.getAbsolutePath().equals(abs)) {
                return i.usedFiles;
            }
        }
        throw new RuntimeException("Unfound reader for: " + abs);
    }

    @SuppressWarnings("unchecked")
    public List<ImportContainer> getContainers() {
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
    private void execute(String[] paths) {
        for (String string : paths) {
            try {
                File f = new File(string);
                if (f.isDirectory()) {
                    walk(f, null);
                } else {
                    handleFile(f, 0, null);
                }
                // Forcing an event for each path, so that at least one
                // event is raised per file despite the count of handlefile.
                scanWithCancel(f, 0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private ImportContainer singleFile(File file) {

        if (file == null) {
            return null;
        }

        String path = file.getAbsolutePath();
        String format = null;
        String[] usedFiles = new String[] { path };
        long start = System.currentTimeMillis();
        try {

            try {
                setids++;
                reader.close();
                reader.setMetadataCollected(METADATA);
                reader.setId(path);
                format = reader.getFormat();
                usedFiles = reader.getUsedFiles();
                String[] domains = reader.getReader().getDomains(path);
                boolean isSPW = Arrays.asList(domains).contains(FormatTools.HCS_DOMAIN);
    
                return new ImportContainer(file, null, null, null, false, null,
                        format, usedFiles, isSPW);

            } finally {
                readerTime += (System.currentTimeMillis() - start);
            }
            
        } catch (UnknownFormatException ufe) {
            safeUpdate(new ErrorHandler.UNKNOWN_FORMAT(path, ufe));
        } catch (Exception e) {
            safeUpdate(new SCANNING_FILE_EXCEPTION(path, e, usedFiles, format));
        }

        return null;

    }

    private void scanWithCancel(File f, int d) throws CANCEL{
        SCANNING s = new SCANNING(f, d, count, total);
        safeUpdate(s);
        if (s.cancel) {
            throw new CANCEL();
        }
    }
    
    private void safeUpdate(ImportEvent event) {
        try {
            observer.update(null, event);
        } catch (Exception ex) {
            log.error(
                    String.format("Error on %s with %s", observer, event),
                    ex);
        }
    }

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

        ImportContainer info = singleFile(file);
        if (info == null) {
            return;
        }

        containers.add(info);
        allFiles.addAll(Arrays.asList(info.usedFiles));
        for (String string : info.usedFiles) {
            Set<String> users = usedBy.get(string);
            if (users == null) {
                users = new HashSet<String>();
                usedBy.put(string, users);
            }
            users.add(file.getAbsolutePath());
        }
    }

    /**
     * The {@link Groups} class servers as an algorithm for sorting the usedBy
     * map from the {@link ImportCandidates#walk(File, Collection)} method.
     * These objects should never leave the outer class.
     */
    private static class Groups {

        private class Group {
            String key;
            Set<String> theyUseMe;
            Set<String> iUseThem;

            public Group(String key) {
                this.key = key;
                this.theyUseMe = new HashSet(usedBy.get(key));
                this.theyUseMe.remove(key);
                this.iUseThem = new HashSet<String>();
                for (Map.Entry<String, Set<String>> entry : usedBy.entrySet()) {
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

        private final Map<String, Set<String>> usedBy;
        private final Map<String, Group> groups = new HashMap<String, Group>();
        private List<String> ordering;

        Groups(Map<String, Set<String>> usedBy) {
            this.usedBy = usedBy;
            for (String key : usedBy.keySet()) {
                groups.put(key, new Group(key));
            }
        }

        public int size() {
            return ordering.size();
        }

        public List<String> getPaths() {
            size(); // Check.
            return ordering;
        }

        Groups parse(List<ImportContainer> containers) {
            if (ordering != null) {
                throw new RuntimeException("Already ordered");
            }
            for (Group g : new HashSet<Group>(groups.values())) {
                g.removeSelfIfSingular();
            }
            ordering = new ArrayList<String>(groups.keySet());
            // Here we remove all the superfluous import containers.
            List<ImportContainer> copy = new ArrayList<ImportContainer>(
                    containers);
            containers.clear();
            for (String key : ordering) {
                for (ImportContainer importContainer : copy) {
                    if (importContainer.file.getAbsolutePath().equals(key)) {
                        containers.add(importContainer);
                    }
                }
            }
            return this;
        }

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

        static Groups test(int count, Map<String, Set<String>> t) {

            System.out.println("\n\n");
            line("TEST " + count);
            Groups g = new Groups(t);
            System.out.println(g);
            g.parse(new ArrayList<ImportContainer>());
            line("RESULT " + count);
            System.out.println(g);
            return g;

        }

        static Groups test() {
            System.out.println("\n");
            line("NOTICE");
            System.out
                    .println("#  You have entered \"\" \"\" as the path to import.");
            System.out
                    .println("#  This runs the test suite. If you would like to");
            System.out.println("#  import the current directory use \"\".");

            Map<String, Set<String>> t = new HashMap<String, Set<String>>();
            t.put("a.dv.log", new HashSet(Arrays.asList("b.dv")));
            t.put("b.dv", new HashSet(Arrays.asList("b.dv")));
            test(1, t);

            t = new HashMap<String, Set<String>>();
            t.put("a.png", new HashSet(Arrays.asList("a.png")));
            test(2, t);

            t = new HashMap<String, Set<String>>();
            t.put("a.tiff", new HashSet(Arrays.asList("a.tiff", "c.lei")));
            t.put("b.tiff", new HashSet(Arrays.asList("b.tiff", "c.lei")));
            t.put("c.lei", new HashSet(Arrays.asList("c.lei")));
            return test(3, t);

        }

    }

}
