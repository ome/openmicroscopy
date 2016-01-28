/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Command-line (and static) utility for working with the {@link #ROOT
 * omero.prefs} {@link Preferences} node in order to store Java properties-file
 * like values on a user basis. This simplifies configuration and permits
 * quicker re-installs, and less wrangling with configuration files.
 * 
 * A single string value is stored as {@link #DEFAULT} (which by default is the
 * value {@link #DEFAULT}, and points to the name of some node under
 * "omero.prefs". This value can be overridden by the "OMERO_CONFIG" environment
 * variable. Almost all commands work on this default node, referred to here as
 * a "profile".
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 * TODO USE JSCH FOR PUBKEY ENCRYPTION
 * TODO backing store sync
 * TODO test nondispatchable
 */
public class prefs {

    /**
     * Storing the standard in as a {@link Properties} instance as early as
     * possible to prevent lost data.
     */
    public final static Properties STDIN;
    static {
        Properties p = new Properties();
        try {
            if (System.in.available() > 0) {
                p.load(System.in);
            }
        } catch (Exception e) {
            // ignore
        }
        STDIN = p;
    }

    /**
     * Activated by setting DBEUG=true in the environment. Various information
     * is printed to {@link System#err}.
     */
    public final static boolean DEBUG = Boolean.valueOf(System.getenv("DEBUG"))
            || "1".equals(System.getenv("DEBUG"));
    static {
        if (DEBUG) {
            printErr(args("STANDARD IN:"));
            try {
                STDIN.store(System.err, null);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * "omero.prefs", the value of the root {@link Preferences} node used for
     * all configuration work.
     */
    public final static String ROOT = "/omero/prefs";

    /**
     * Key (and default value) of the property under {@link #ROOT} which defines
     * which "profile" (subnode} is in effect.
     */
    public final static String DEFAULT = "default";

    /**
     * Environment variable which can be set to override the current, active
     * profile.
     */
    public final static String ENV = "OMERO_CONFIG";

    /**
     * Static exception created at initialization to prevent our needing to
     * subclass. For internal use only.
     */
    private final static RuntimeException USAGE = new RuntimeException("usage");

    /**
     * Static exception created at initialization to prevent our needing to
     * subclass. For internal use only.
     */
    private final static RuntimeException CONFLICT = new RuntimeException(
            "conflict");

    /**
     * Cache the root node at start up
     */
    private static Preferences prefs = Preferences.userRoot().node(ROOT);

    /**
     * Entry point to the prefs command line too. Uses the
     * {@link #dispatch(String[])} method to invoke a public static method which
     * takes a {@link #pop(String[]) popped} String-argument array.
     * 
     * @param args
     *            Not null. Can be empty.
     */
    public static void main(String[] args) {
        try {
            if (DEBUG) {
                printErr(args("Debugging profile " + def(args())[0]));
            }
            exit(print(dispatch(notNull(args))));
        } catch (Throwable e) {

            if (DEBUG) {
                e.printStackTrace();
            }

            if (e == USAGE) {
                exit(printErr(usage(args)));
            } else if (e == CONFLICT) {
                exit(printErr(args("Conflict found in properties. Use drop or load_nowarn")));
            } else if (e instanceof BackingStoreException) {
                exit(printErr(args("Error accessing preferences:", e
                        .getMessage())));
            } else if (e instanceof IOException) {
                exit(printErr(args("IO Error:", e.getMessage())));
            } else {
                exit(printErr(args("Unknown error:", e.getMessage())));
            }
        }
    }

    /**
     * Returns a usage string array.
     * 
     * @param args
     *            Ignored.
     * @return Never null.
     */
    public static String[] usage(String[] args) {
        return new String[] {
                "                                                                  ",
                " usage: prefs COMMAND [ARGS]                         ",
                "                                                                  ",
                "  all                   :  list all profiles under omero.prefs",
                "  def [NEWDEFAULT]      :  list (or set) current default profile",
                "  drop                  :  deletes current profile",
                "  get [KEY [KEY [...]]] :  get keys from the current profile. All by default",
                "  export [FILE ]        :  export to a file or standard out",
                "  keys                  :  list all keys for the current profile",
                "  load [FILE...]        :  read into current profile from a file or standard in (error on conflict)",
                "  load_nowarn [FILE...] :  read current profile from a file or standard in",
                "  set KEY VALUE         :  set value on current profile",
                "  sys COMMANDS          :  applies commands as above to system preferences",
                "                                                                  ",
                "Note: profiles are created on demand. Later properties override earlier ones." };
    }

    /**
     * Returns a help string array. Currently calls {@link #usage(String[])} but
     * may eventually return a more man page-like statement.
     * 
     * @param args
     *            Ignored.
     * @return Never null.
     */
    public static String[] help(String[] args) {
        return usage(args); // Currently just an alias
    }

    // ~ For Main Only (and testing)
    // =========================================================================

    /**
     * Prints the arg array and returns an empty array (for exit purposes)
     */
    public static String[] print(String[] args) {
        for (String string : args) {
            if (string != null) {
                System.out.println(string);
            }
        }
        return new String[] {};
    }

    /**
     * Prints the arg array and returns the input array (for exit purposes)
     */
    public static String[] printErr(String[] args) {
        for (String string : args) {
            if (string != null) {
                System.err.println(string);
            }
        }
        return args;
    }

    /**
     * Delegates to {@link #printErr(String[])} iff {@link #DEBUG} is true.
     */
    public static String[] printDebug(String[] args) {
        if (DEBUG) {
            printErr(args);
        }
        return args;
    }

    /**
     * Uses the length of the argument array as the exit code.
     */
    public static String[] exit(String[] args) {
        try {
            Thread.sleep(1L);
            System.out.flush();
            Thread.sleep(1L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(args.length);
        return null;
    }

    // ~ General Purpose
    // =========================================================================

    /**
     * Uses the first string in the argument array to reflectively invoke
     * another method with the same signature on {@link prefs}. If the array is
     * null, empty, or begins with an non-extant method, {@link #USAGE} will be
     * thrown. Otherwise, invokes the named method, returning its return value
     * or returning the cause of any {@link InvocationTargetException}.
     */
    public static String[] dispatch(String[] args) throws Throwable {
        if (args == null || args.length == 0) {
            USAGE.fillInStackTrace();
            throw USAGE;
        }
        String method = args[0];
        args = pop(args);

        Method m;
        try {
            m = prefs.class.getMethod(method, String[].class);
        } catch (Exception e) {
            USAGE.fillInStackTrace();
            throw USAGE;
        }
        try {
            return (String[]) m.invoke(null, (Object) args);
        } catch (InvocationTargetException ite) {
            throw ite.getCause();
        }

    }

    /**
     * Replaces the user {@link Preferences} instance with the system
     * {@link Preferences} and continues dispatching.
     */
    public static String[] sys(String[] args) throws Throwable {
        prefs = Preferences.systemRoot().node(ROOT);
        return dispatch(args);
    }

    /**
     * Calls {@link Preferences#childrenNames()} and returns the array.
     * 
     * @param args
     *            Ignored.
     */
    public static String[] all(String[] args) throws BackingStoreException {
        return prefs.childrenNames();
    }

    /**
     * Returns the current default if no argument is given, or sets the default
     * to the given string (or "" if null) otherwise returing "Default set to:
     * ... ".
     * 
     * @param args
     *            String array of length 0 or 1. Otherwise {@link #USAGE} is
     *            thrown.
     */
    public static String[] def(String[] args) {
        args = notNull(args);
        if (args.length == 0) {
            String OMERO = System.getenv(ENV);
            if (OMERO == null) {
                OMERO = prefs.get(DEFAULT, null);
                if (OMERO == null) {
                    prefs.put(DEFAULT, DEFAULT);
                    OMERO = DEFAULT;
                }
            }
            return args(OMERO);
        } else if (args.length == 1) {
            prefs.put(DEFAULT, args[0] == null ? "" : args[0]);
            return new String[] { "Default set to: " + def(args())[0] };
        } else {
            USAGE.fillInStackTrace();
            throw USAGE;
        }
    }

    /**
     * Drops the entire profile ({@link Preferences subnode}) via
     * {@link Preferences#removeNode()}.
     * 
     * @param args
     *            Ignored.
     */
    public static String[] drop(String[] args) throws BackingStoreException {
        _node().removeNode();
        return args();
    }

    /**
     * Returns {@link Preferences#keys()}
     * 
     * @param args
     *            Ignored.
     */
    public static String[] keys(String[] args) throws BackingStoreException {
        return _node().keys();
    }

    /**
     * Returns either the given key=value pairs (or all if no argument is
     * given). When a single key is specified the return format is simply
     * "value".
     */
    public static String[] get(String[] args) throws BackingStoreException {
        args = notNull(args);

        String[] keys = _node().keys();
        if (args.length == 0) {
            if (keys.length == 0) {
                return args;
            } else if (keys.length == 1) {
                return get(args(keys[0], "UNKNOWNKEYJUSTTOFORCELENGTH2"));
            } else {
                return get(keys);
            }
        } else if (args.length == 1) {
            String key = args[0] == null ? "" : args[0];
            String value = _node().get(key, "");
            return args(value);
        }

        Set<String> availableKeys = new HashSet<String>(Arrays.asList(_node()
                .keys()));
        Set<String> askedKeys = new HashSet<String>(Arrays.asList(args));
        availableKeys.retainAll(askedKeys);
        String[] rv = new String[availableKeys.size()];

        for (int i = 0; i < rv.length;) {
            String key = args[i] == null ? "" : args[i];
            if (availableKeys.contains(key)) {
                String value = _node().get(key, "");
                rv[i] = key + "=" + value;
                i++;
            }
        }
        Arrays.sort(rv);
        return rv;

    }

    /**
     * Takes an array of length 2, using args[0] as the key and args[1] as the
     * value to be set. For more advanced usage, see {@link #load(String[])}.
     * 
     * @param args
     * @return a
     * @throws BackingStoreException
     */
    public static String[] set(String[] args) throws BackingStoreException {
        if (args == null || args.length < 1) {
            USAGE.fillInStackTrace();
            throw USAGE;
        }
        String key = args[0] == null ? "" : args[0];
        String val = args.length == 1 ? null : args[1];
        if (val == null) {
            _node().remove(key);
        } else {
            _node().put(key, val);
        }

        return args();
    }

    /**
     * Exports all properties in the current profile to {@link System#out}, or
     * if a single argument is given, that is take to be the name of a target
     * ouput file. Export will fail if the file already exists. This method may
     * be removed in favor of using piping with {@link #get(String[])}.
     * 
     * Properties are in standard Java {@link Properties} format.
     */
    public static String[] export(String[] args) throws BackingStoreException,
            IOException {
        if (args.length == 0) {
            _export(System.out);
            return args();
        } else if (args.length == 1) {
            File f = new File(args[0]);
            if (f.exists()) {
                throw new IOException("File " + f.getAbsolutePath()
                        + " exists!");
            }
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(args[0]));
                _export(bos);
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException ioe) {
                        // must ignore
                    }
                }
            }
            return args();
        } else {
            USAGE.fillInStackTrace();
            throw USAGE;
        }
    }

    /**
     * Loads a profile from {@link Properties} files, or properly formatted
     * {@link System#in} input if no files are given. If a key to be loaded
     * already exists in the configuration, {@link #CONFLICT} will be thrown.
     * Use {@link #load_nowarn(String[])} instead, or {@link #drop(String[])}
     * the profile before loading.
     */
    public static String[] load(String[] args) throws IOException {
        args = notNull(args);

        Properties p;
        if (args.length == 0) {
            p = STDIN;
        } else {
            p = _merge(args);
        }
        Preferences node = _node();
        for (Object obj : p.keySet()) {
            String key = obj.toString();
            String currentValue = node.get(key, "");
            if (currentValue != null && currentValue.length() > 0) {
                printErr(args(key + " already present!"));
                throw CONFLICT;
            }
        }
        _load(p);
        return args();
    }

    /**
     * Performs the same actions as {@link #load(String[])} but does not throw
     * {@link #CONFLICT} if a key already exists.
     */
    public static String[] load_nowarn(String[] args) throws IOException {
        args = notNull(args);

        Properties p;
        if (args.length == 0) {
            p = STDIN;
        } else {
            p = _merge(args);
        }
        _load(p);
        return args();
    }

    // ~ Utilities
    // =========================================================================

    /**
     * Converts varargs to a String-array.
     */
    public static String[] args(String... args) {
        return args;
    }

    /**
     * Joins all the String arguments given into a single String (joined with "
     * "), and returns that String as the first element of a new array.
     */
    public static String[] join(String... args) {
        StringBuilder sb = new StringBuilder();
        for (String string : args) {
            sb.append(string);
            sb.append(" ");
        }
        return new String[] { sb.toString() };
    }

    /**
     * Returns an empty array if the argument is null.
     */
    public static String[] notNull(String[] args) {
        if (args == null) {
            return new String[] {};
        }
        return args;
    }

    /**
     * Creates a new subarray from the argument, effectively popping off the
     * first element.
     */
    public static String[] pop(String[] args) {
        int sz = args.length - 1;
        if (sz < 0) {
            return args;
        }
        String[] newArgs = new String[sz];
        System.arraycopy(args, 1, newArgs, 0, sz);
        return newArgs;
    }

    // ~ Testing
    // =========================================================================

    /**
     * Tests for success by dispatching to the first argument and expecting no
     * exception.
     */
    private static String[] _ok(String... args) throws Throwable {
        testcount++;
        String test = join(args)[0];
        String[] rv = args();
        try {
            rv = dispatch(args);
        } catch (Exception e) {
            printErr(join("fail...", test));
            failures++;
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        printErr(join("ok...", test));
        printDebug(rv);
        return rv;
    }

    /**
     * Tests for failure by dispatching to the first argument, and expecting an
     * exception.
     */
    private static String[] _fail(String... args) throws Throwable {
        testcount++;
        String test = join(args)[0];
        String[] rv = args();
        try {
            rv = dispatch(args);
            printErr(join("fail...", test, "No exception thrown."));
            failures++;
        } catch (Exception e) {
            printErr(join("ok...", test));
        }
        printDebug(rv);
        return rv;
    }

    private static boolean testing = false;
    private static int testcount = 0;
    private static int failures = 0;

    /**
     * Simple test framework, callable from the command line via "java prefs
     * test"
     */
    public static String[] test(String[] args) throws Throwable {

        if (testing) {
            throw new RuntimeException("testing already running");
        }

        testing = true;
        failures = 0;
        String origconf = def(args())[0];
        String testconf = "testconf";
        def(args(testconf));

        try {

            // Can't test main since it exits.

            // Some stuff you probably shouldn't do
            _fail("test"); // Prevent infinite recursion.
            _fail("_node");
            _fail("NOMETHOD");
            _fail("_ok", "printDebug"); // Can't find since private.
            _fail("_fail", "NOMETHOD"); // Ditto.

            // Printing
            _ok("print", "stuff");
            _ok("printErr", "stuff");
            _ok("printDebug", "stuff");
            _ok("print", null);
            _ok("printErr", null);
            _ok("printDebug", null);
            _ok("print");
            _ok("printErr");
            _ok("printDebug");

            // These throw usage
            _fail();
            _fail("dispatch"); // No infinite recursion! (Stop via pop)

            // Basic utilities
            _ok("join", "a", "b", "c");
            _ok("join", "a");
            _ok("join");
            _ok("join", null);
            _ok("pop", "a", "b", "c");
            _ok("pop", "a");
            _ok("pop");
            _ok("pop", null);
            _ok("notNull", "a", "b", "c");
            _ok("notNull", "a");
            _ok("notNull");
            _ok("notNull", null);
            _ok("args", "a", "b", "c");
            _ok("args", "a");
            _ok("args");
            _ok("args", null);

            // User help
            _ok("usage");
            _ok("usage", "stuff");
            _ok("usage", null);
            _ok("help");
            _ok("help", "stuff");
            _ok("help", null);

            // User commands
            // //////////////

            _ok("all");

            _ok("keys");
            _ok("keys", null);
            _ok("keys", "stuff");

            // Def
            _ok("def", "new_test_configuration");
            _ok("def", testconf);
            _ok("def", null);
            _fail("def", "too", "many", "args");

            // Set
            _fail("set");
            _fail("set", null);
            _ok("set", "SHOULDBEFOO", "FOO");

            // Get
            _ok("get");
            _ok("get", null);
            _ok("get", "NONEXTANTVALUE");
            _ok("get", "SHOULDBEFOO");

            // Drop
            _ok("def", "droptest");
            _ok("set", "a", "b");
            _ok("drop");
            _ok("def", testconf);

            print(args("============================================"));
            print(args(failures + " from " + testcount + " failed."));

        } finally {
            def(args(origconf));
        }

        return args();
    }

    // ~ Nondispatchable (signature differs)
    // =========================================================================

    /**
     * Returns the {@link Preferences node} (or profile) designated by the
     * current {@link #def(String[]) default}.
     */
    private static Preferences _node() {
        Preferences p = prefs.node(def(args())[0]);
        return p;
    }

    /**
     * Prints all properties as returned by {@link #get(String[])} to the
     * {@link OutputStream}.
     */
    private static void _export(OutputStream os) throws BackingStoreException,
            IOException {
        String[] values = get(args());
        OutputStreamWriter osw = new OutputStreamWriter(os);
        for (String string : values) {
            osw.write(string);
            osw.write("\n");
        }
        osw.flush();
    }

    /**
     * Loads the {@link Properties} instance into the {@link #_node() current
     * profile}.
     */
    private static void _load(Properties properties) {
        Preferences p = _node();
        for (Object obj : properties.keySet()) {
            Object value = properties.get(obj);
            if (value == null) {
                value = "";
            }
            p.put(obj.toString(), value.toString());
        }
    }

    /**
     * {@link Properties#load(java.io.InputStream) Loads} any number of files
     * into a new {@link Properties} instance. Later values overwrite earlier
     * oens.
     */
    private static Properties _merge(String... args) throws IOException {
        Properties p = new Properties();
        for (String string : args) {
            File f = new File(string);
            FileInputStream fis = new FileInputStream(f);
            try {
                p.load(fis);
            } finally {
                fis.close();
            }
        }
        return p;
    }
}
