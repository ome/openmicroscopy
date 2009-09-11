/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import loci.formats.FormatException;
import loci.formats.ImageReader;

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

    final private Groups groups;
    final private OMEROWrapper reader = new OMEROWrapper();
    final private Set<String> allFiles = new HashSet<String>();
    final private Map<String, Set<String>> usedBy = new HashMap<String, Set<String>>();

    /**
     * Main constructor which iterates over all the paths calling
     * {@link #walk(File, Collection)}.
     * 
     * @param paths
     * @param verbose
     * @throws IOException
     */
    public ImportCandidates(String[] paths) {
        super(TrueFileFilter.INSTANCE, 4);

        if (paths != null && paths.length == 2 && "".equals(paths[0])
                && "".equals(paths[1])) {

            // Easter egg for testing.
            // groups is not null, therefore usage() won't need to be
            // called.
            groups = Groups.test();
            System.exit(0);

        } else if (paths == null || paths.length == 0) {

            groups = null;

        } else {

            for (String string : paths) {
                try {
                    walk(new File(string), null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            groups = new Groups(usedBy);
            groups.parse();
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
        if (groups == null) {
            return -1;
        }
        return groups.groups.size();
    }

    public Set<String> getPaths() {
        if (groups != null) {
            return new HashSet<String>(groups.groups.keySet());
        }
        return new HashSet<String>();
    }

    public String[] singleFile(File file) throws FormatException, IOException {
        reader.setId(file.getAbsolutePath());
        String[] rv = reader.getUsedFiles();
        return rv;
    }

    @Override
    public void handleFile(File file, int depth, Collection collection) {

        // Optimization.
        if (allFiles.contains(file.getAbsolutePath())) {
            return;
        }

        try {
            String[] used = singleFile(file);
            allFiles.addAll(Arrays.asList(used));
            for (String string : used) {
                Set<String> users = usedBy.get(string);
                if (users == null) {
                    users = new HashSet<String>();
                    usedBy.put(string, users);
                }
                users.add(file.getAbsolutePath());
            }
        } catch (FormatException e) {
            log.info("FormatException: " + file.getAbsolutePath());
        } catch (IOException e) {
            log.warn("IOException: " + file.getAbsolutePath());
        }
    }

}

class Groups {

    class Group {
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
            sb.append("# Used by: ");
            for (String key : theyUseMe) {
                sb.append(" " + key + " ");
            }
            sb.append("\n");
            sb.append(key);
            sb.append("\n");
            for (String val : iUseThem) {
                sb.append(val);
                sb.append("\n");
            }
            return sb.toString();
        }

    }

    final Map<String, Set<String>> usedBy;
    final Map<String, Group> groups = new HashMap<String, Group>();

    Groups(Map<String, Set<String>> usedBy) {
        this.usedBy = usedBy;
        for (String key : usedBy.keySet()) {
            groups.put(key, new Group(key));
        }
    }

    Groups parse() {
        for (Group g : new HashSet<Group>(groups.values())) {
            g.removeSelfIfSingular();
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
        g.parse();
        line("RESULT " + count);
        System.out.println(g);
        return g;

    }

    static Groups test() {
        System.out.println("\n");
        line("NOTICE");
        System.out.println("#  You have entered \"\" \"\" as the path to import.");
        System.out.println("#  This runs the test suite. If you would like to");
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
