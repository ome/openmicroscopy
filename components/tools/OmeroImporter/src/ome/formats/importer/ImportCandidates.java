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

/**
 * 
 * @since Beta4.1
 */
public class ImportCandidates extends DirectoryWalker {

    final File __file;
    final ImageReader reader = new ImageReader();;
    final Map<String, Set<String>> usedBy = new HashMap<String, Set<String>>();

    public ImportCandidates(String path) {
        super(TrueFileFilter.INSTANCE, 4);
        this.__file = new File(path);
    }

    public int run() throws FormatException, IOException {
        if ("".equals(__file.getName())) {
            Groups.test();
        } else if (__file.isDirectory()) {
            walk(__file, null);
            new Groups(usedBy).parse().print();
        } else {
            singleFile(__file);
        }
        return 0;
    }

    public String[] singleFile(File file) throws FormatException, IOException {
        reader.setId(file.getAbsolutePath());
        return reader.getUsedFiles();
    }

    @Override
    public void handleFile(File file, int depth, Collection collection) {
        try {
            String[] used = singleFile(file);
            for (String string : used) {
                Set<String> users = usedBy.get(string);
                if (users == null) {
                    users = new HashSet<String>();
                    usedBy.put(string, users);
                }
                users.add(file.getAbsolutePath());
            }
        } catch (FormatException e) {
            System.err.println("FormatException: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("IOException: " + file.getAbsolutePath());
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
            if (used <=1 && users > 0) {
                groups.remove(key);
            }
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

    final boolean verbose;
    final Map<String, Set<String>> usedBy;
    final Map<String, Group> groups = new HashMap<String, Group>();

    Groups(Map<String, Set<String>> usedBy) {
        this(usedBy, false);
    }

    Groups(Map<String, Set<String>> usedBy, boolean verbose) {
        this.verbose = verbose;
        this.usedBy = usedBy;
        for (String key : usedBy.keySet()) {
            groups.put(key, new Group(key));
        }
        if (verbose) {
            for (Group g : groups.values()) {
                System.out.println(g);
            }
        }
    }

    Groups parse() {
        for (Group g : new HashSet<Group>(groups.values())) {
            g.removeSelfIfSingular();
        }
        return this;
    }

    Groups print() {
        for (Group g : groups.values()) {
            System.out.println(g);
        }
        return this;
    }

    static void line(String s) {
        System.out.println("\n# ************ " + s + " ************ \n");
    }

    static void test(int count, Map<String, Set<String>> t) {

        System.out.println("\n\n");
        line("TEST " + count);
        Groups g = new Groups(t, true).parse();
        line("RESULT " + count);
        g.print();

    }

    static void test() {
        System.out.println("\n");
        line("NOTICE");
        System.out.println("#  You have entered \"\" as the path to import.");
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
        test(3, t);

    }

    // TODO
    // Filter during walk
    // "." for current directory.
    // -k keep going
    // pass ImportCandidates to ImportLibrary(even for single path)
    // 

}
