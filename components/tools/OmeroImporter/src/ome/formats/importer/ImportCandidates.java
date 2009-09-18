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
 * @since Beta4.1
 */
public class ImportCandidates extends DirectoryWalker {

    private final static Log log = LogFactory.getLog(ImportCandidates.class);

    final private IObserver observer;
    final private OMEROWrapper reader;
    final private Groups groups;
    final private Set<String> allFiles = new HashSet<String>();
    final private Map<String, Set<String>> usedBy = new HashMap<String, Set<String>>();
    final private List<ImportContainer> containers = new ArrayList<ImportContainer>();

    /**
     * Main constructor which iterates over all the paths calling
     * {@link #walk(File, Collection)}.
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
        super(TrueFileFilter.INSTANCE, 4);
        this.reader = reader;
        this.observer = observer;

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

        for (String string : paths) {
            try {
                File f = new File(string);
                if (f.isDirectory()) {
                    walk(f, null);
                } else {
                    handleFile(f, 0, null);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        groups = new Groups(usedBy);
        groups.parse(containers);

    }

    /**
     * Takes {@link ImportContainer} array to support explicit candidates. The
     * {@link Groups} strategy is not currently enforced.
     */
    public ImportCandidates(OMEROWrapper reader, ImportContainer[] containers) {
        this.reader = reader;
        this.observer = null;
        this.groups = null;
        if (containers != null) {
            this.containers.addAll(Arrays.asList(containers));
        }
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

    private ImportContainer singleFile(File file) {

        if (file == null) {
            return null;
        }

        String path = file.getAbsolutePath();
        String format = null;
        String[] usedFiles = null;
        try {
            reader.setId(path);
            format = reader.getFormat();
            usedFiles = reader.getUsedFiles();
            
            return new ImportContainer(file, null, null, null, false, null,
                    reader.getFormat(), usedFiles , reader
                            .isSPWReader(path));
        } catch (Exception e) {
            
            if (usedFiles == null || usedFiles.length == 0) {
                if (new File(path).exists()) {
                    usedFiles = new String[]{path};
                }
            }
            
            ImportEvent event = new ErrorHandler.FILE_EXCEPTION(path, e, usedFiles, format);
            try {
                observer.update(null, event);
            } catch (Exception ex) {
                log.error(
                        String.format("Error on %s with %s", observer, event),
                        ex);
            }
        }

        return null;
    }

    @Override
    public void handleFile(File file, int depth, Collection collection) {

        // Optimization.
        if (allFiles.contains(file.getAbsolutePath())) {
            return;
        }

        if (file.getName().startsWith(".")) {
            return; // Omitting dot files.
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