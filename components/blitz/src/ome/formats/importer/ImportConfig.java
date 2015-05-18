/*
 *   $Id$
 *
 *   Copyright 2009-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer;

import static omero.rtypes.rstring;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import loci.formats.FormatTools;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.targets.ImportTarget;
import ome.formats.importer.targets.TargetBuilder;
import ome.formats.importer.util.IniFileLoader;
import ome.system.PreferenceContext;
import ome.system.UpgradeCheck;
import omero.model.Annotation;
import omero.model.NamedValue;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

/**
 * Utility class which configures the Import.
 *
 * @since Beta4.1
 */
public class ImportConfig {

    private final static Logger log = LoggerFactory.getLogger(ImportConfig.class);

    /** The class used to identify the dataset target.*/
    private static final String DATASET_CLASS = "omero.model.Dataset";

    /** The class used to identify the screen target.*/
    private static final String SCREEN_CLASS = "omero.model.Screen";

    /**
     * Delimiter used to encode multiple servers in one preferences value.
     */
    public final static String SERVER_NAME_SEPARATOR = ",";

    /**
     * Lookup key for {@link System#getProperty(String)}. Should be the path of
     * a readers.txt file.
     */
    public final static String READERS_KEY = "omero.import.readers";

    //
    // CONFIGURATION SOURCES: several configuration sources are defined below.
    // Each may or may not be used for a given {@link value}.
    //

    /**
     * Preferences node which will be used for all Preferences in the
     * ome.formats package. This must work in tandem with other sources such as
     * {@link IniFileLoader}
     */
    private final Preferences prefs;

    /**
     * Ini-file based configuration source which loads both a static
     * configuration file and a user-defined configuration file.
     */
    private final IniFileLoader ini;

    /**
     * {@link Properties} instance which will also be used for lookups. In the
     * default case, this is from {@link System#getProperties()}
     */
    private final Properties props;

    /**
     * Stores the omeroVersion from omero.properties
     */
    private String omeroVersion = "Unknown";

    //
    // MUTABLE STATE : To prevent every class from having it's own
    // username/password/port/etc field, all are available here. On save, these
    // are committed to disk.
    //

    public final StrValue agent;
    public final StrValue hostname;
    public final StrValue username;
    public final StrValue password;
    public final IntValue port;
    public final LongValue savedProject;
    public final LongValue savedDataset;
    public final LongValue savedScreen;

    public final StrValue sessionKey;
    public final LongValue group;
    public final BoolValue doThumbnails;
    public final BoolValue noStatsInfo;
    public final StrValue email;
    public final StrValue userSpecifiedName;
    public final StrValue userSpecifiedDescription;

    @Deprecated
    public final StrValue targetClass;

    @Deprecated
    public final LongValue targetId;

    public final StrValue target;

    public final BoolValue debug;
    public final BoolValue contOnError;
    public final BoolValue sendReport;
    public final BoolValue sendFiles;
    public final BoolValue sendLogFile;
    public final StrValue qaBaseURL;
    public final BoolValue checkUpgrade;

    public final BoolValue useCustomImageNaming;
    public final BoolValue useFullPath;
    public final IntValue numOfDirectories;

    public final FileValue savedDirectory;
    public final StrValue readersPath;
    public final StrValue checksumAlgorithm;

    public final BoolValue encryptedConnection;
    public final BoolValue autoClose;

    public final AnnotationListValue annotations;
    public final DoubleArrayValue userPixels;

    public final static String DEFAULT_QABASEURL = "http://qa.openmicroscopy.org.uk/qa";
    /**
     * Keys for fileset version information entries.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.1
     */
    public static enum VersionInfo {
        BIO_FORMATS_READER("bioformats.reader"),
        BIO_FORMATS_VERSION("bioformats.version"),
        CLIENT_LANGUAGE_NAME("client.language.name"),
        CLIENT_LANGUAGE_VENDOR("client.language.vendor"),
        CLIENT_LANGUAGE_COMPILER("client.language.compiler"),
        CLIENT_LANGUAGE_VERSION("client.language.version"),
        OMERO_VERSION("omero.version"),
        OS_NAME("os.name"),
        OS_VERSION("os.version"),
        OS_ARCHITECTURE("os.architecture"),
        LOCALE("locale");

        /** the map key corresponding to this instance */
        public final String key;

        private VersionInfo(String key) {
            this.key = key;
        }
    }

    /**
     * Static method for creating {@link Preferences} during construction if
     * necessary.
     */
    private static Preferences prefs() {
        Preferences prefs = Preferences.userNodeForPackage(ImportConfig.class);
        try {
            prefs.flush();
        } catch (Exception e) {
            log.error("Error flushing preferences");
        }
        return prefs;
    }

    /**
     * Simplest constructor which use calls
     * {@link ImportConfig#ImportConfig(File)} with null.
     */
    public ImportConfig() {
        this(null);
    }

    /**
     * Calls
     * {@link ImportConfig#ImportConfig(Preferences, IniFileLoader, Properties)}
     * with user preferences, a local {@link PreferenceContext}, an
     * {@link IniFileLoader} initialized with the given argument, and
     * {@link System#getProperties()}.
     *
     * @param configFile
     *            Can be null.
     */
    public ImportConfig(final File configFile) {
        this(prefs(), new IniFileLoader(configFile),
                System.getProperties());
    }

    /**
     * Complete constructor. All values can be null.
     *
     * @param prefs
     * @param ctx
     * @param ini
     * @param props
     */
    public ImportConfig(final Preferences prefs,
            IniFileLoader ini, Properties props) {

        this.prefs = prefs;
        this.props = props;
        this.ini = ini;

        // Various startup requirements

        ResourceBundle bundle = ResourceBundle.getBundle("omero");
        omeroVersion = bundle.getString("omero.version");
        log.info("OMERO Version: " + omeroVersion);

        if (ini != null) {
            ini.updateFlexReaderServerMaps();
        }

        log.info("Bioformats " + getBioFormatsVersion());

        agent        = new StrValue("agent", this, "importer");
        hostname     = new StrValue("hostname", this, "omero.host");
        username     = new StrValue("username", this, "omero.name");
        password     = new StrValue("password", this, "omero.pass");
        port         = new IntValue("port", this, 4064, "omero.port") {
            @Override
            public synchronized void load() {
                super.load();
                // Handle previous versions in which a null/"" got stored
                // to preferences.
                if (_current.compareAndSet(null, _default)) {
                    log.debug("Replacing port load value with default");
                }
            }
        };

        sessionKey   = new StrValue("session", this);
        group		 = new LongValue("group", this, null);
        doThumbnails = new BoolValue("doThumbnails", this, true);
        noStatsInfo  = new BoolValue("noStatsInfo", this, false);
        email        = new StrValue("email", this);
        qaBaseURL    = new StrValue("qaBaseURL", this, DEFAULT_QABASEURL);
        checkUpgrade  = new BoolValue("checkUpgrade", this, true);
        userSpecifiedName = new StrValue("userSpecifiedName", this);
        userSpecifiedDescription = new StrValue("userSpecifiedDescription", this);
        targetClass  = new StrValue("targetClass", this);
        targetId     = new LongValue("targetId", this, 0L);
        target       = new StrValue("target", this);

        savedProject = new LongValue("savedProject", this, 0L);
        savedDataset = new LongValue("savedDataset", this, 0L);
        savedScreen  = new LongValue("savedScreen", this, 0L);

        debug        = new BoolValue("debug", this, false);
        contOnError  = new BoolValue("contOnError", this, false);
        sendReport   = new BoolValue("sendReport", this, false);
        sendFiles    = new BoolValue("sendFiles", this, true);
        sendLogFile  = new BoolValue("sendLogFile", this, true);

        useFullPath  = new BoolValue("useFullPath", this, true);
        useCustomImageNaming = new BoolValue("overrideImageName", this, true);
        numOfDirectories = new IntValue("numOfDirectories", this, 0);
        savedDirectory = new FileValue("savedDirectory", this);

        encryptedConnection = new BoolValue("ecryptedConnection", this, true);
        autoClose = new BoolValue("autoClose", this, false);

        annotations = new AnnotationListValue(
                "annotations", this, new ArrayList<Annotation>());
        userPixels = new DoubleArrayValue(
                "userPixels", this, null);

        readersPath = new StrValue("readersPath", this);
        checksumAlgorithm = new StrValue("checksumAlgorithm", this);
    }

    public String getBioFormatsVersion() {
        return String.format("version: %s revision: %s date: %s",
                FormatTools.VERSION, FormatTools.VCS_REVISION, FormatTools.DATE);
    }

    public String getOmeroVersion() {
        return omeroVersion;
    }

    /**
     * Note useful version information that can be extracted from this system,
     * as provenance that is useful for debugging.
     * @param versionInfo the map into which version information is to be added
     */
    public void fillVersionInfo(List<NamedValue> versionInfo) {
        final Map<VersionInfo, String> properties = new HashMap<VersionInfo, String>();
        properties.put(VersionInfo.BIO_FORMATS_VERSION, getBioFormatsVersion());
        properties.put(VersionInfo.CLIENT_LANGUAGE_NAME, "Java");
        properties.put(VersionInfo.CLIENT_LANGUAGE_VENDOR, System.getProperty("java.vendor"));
        properties.put(VersionInfo.CLIENT_LANGUAGE_COMPILER, System.getProperty("java.compiler"));
        properties.put(VersionInfo.CLIENT_LANGUAGE_VERSION, System.getProperty("java.version"));
        properties.put(VersionInfo.LOCALE, Locale.getDefault().toString());
        properties.put(VersionInfo.OMERO_VERSION, getOmeroVersion());
        properties.put(VersionInfo.OS_NAME, System.getProperty("os.name"));
        properties.put(VersionInfo.OS_VERSION, System.getProperty("os.version"));
        properties.put(VersionInfo.OS_ARCHITECTURE, System.getProperty("os.arch"));
        /* fill any useful information for Ice to serialize */
        for (final Map.Entry<VersionInfo, String> property : properties.entrySet()) {
            if (StringUtils.isNotEmpty(property.getValue())) {
                versionInfo.add(new NamedValue(property.getKey().key, property.getValue()));
            }
        }
    }

    /**
     * Modifies the logging level of everything under the
     * <code>ome.formats</code>, <code>ome.services.blitz</code>,
     * <code>ome.system</code> and <code>loci</code> packages hierarchically.
     * @param levelString if null, then {@link #ini#getDebugLevel()} will be
     * used.
     */
     public void configureDebug(String levelString) {
         Level level;
         if (levelString == null) {
             level = Level.toLevel(ini.getDebugLevel());
         } else {
             level = Level.toLevel(levelString);
         }
         setLevel("ome.formats", level);
         setLevel("ome.services.blitz", level);
         setLevel("ome.system", level);
         setLevel("loci", level);
     }

     private void setLevel(String loggerName, Level level) {
         Logger logger = LoggerFactory.getLogger(loggerName);
         if (!(logger instanceof ch.qos.logback.classic.Logger)) return;
         ((ch.qos.logback.classic.Logger) logger).setLevel(level);
     }

    //
    // Login methods
    //

    /**
     * Create and return a new OMEROMetadataStoreClient
     * @return - OMEORMetadataStoreClient
     * @throws Exception
     */
    public OMEROMetadataStoreClient createStore() throws Exception {
        if (!canLogin()) {
            throw new RuntimeException("Can't create store. See canLogin()");
        }
        OMEROMetadataStoreClient client = new OMEROMetadataStoreClient();
        if (sessionKey.empty()) {
            client.initialize(username.get(), password.get(), hostname.get(),
                    port.get(), group.get(), encryptedConnection.get());

        } else {
            client.initialize(hostname.get(), port.get(), sessionKey.get(), encryptedConnection.get());
        }
        return client;
    }


    /**
     * Check online to see if this is the current version
     */
    public boolean isUpgradeNeeded() {

        ResourceBundle bundle = ResourceBundle.getBundle("omero");
        String url = bundle.getString("omero.upgrades.url");
        UpgradeCheck check = new UpgradeCheck(url, getVersionNumber(), agent.get());
        check.run();
        return check.isUpgradeNeeded();
    }

    /**
     * Confirm all information for login is supplied
     *
     * @return true if all is ok
     */
    public boolean canLogin() {
        if (((username.empty() || password.empty())
                && sessionKey.empty()) || hostname.empty()) {
            return false;
        }
        return true;
    }

    //
    // GUI related. Delegates to IniFileLoader
    //

    /**
     * @return ini log file
     */
    public String getLogFile() {
        return ini.getLogFile();
    }

    /**
     * @return ini home URL
     */
    public String getHomeUrl() {
        return ini.getHomeUrl();
    }

    /**
     * @return ini forum URL
     */
    public String getForumUrl() {
	return ini.getForumUrl();
    }

    /**
     * @return ini application title
     */
    public String getAppTitle() {
        return ini.getAppTitle();
    }

    /**
     * @return ini getForceFileArchiveOn
     */
    public boolean getForceFileArchiveOn() {
        return ini.getForceFileArchiveOn();
    }

    /**
     * @return ini getStaticDisableHistory
     */
    public boolean getStaticDisableHistory() {
        return ini.getStaticDisableHistory();
    }

    /**
     * @return ini getUserDisableHistory
     */
    public boolean getUserDisableHistory() {
        return ini.getUserDisableHistory();
    }

    /**
     * @param b - true if Quaqua should be used
      */
    public void setUserDisableHistory(boolean b) {
        ini.setUserDisableHistory(b);
    }

    /**
     * @return ini version note
     */
    public String getVersionNumber() {
        return this.omeroVersion; // + " " + ini.getVersionNote();
    }

    public void setVersionNumber(String s) {
	this.omeroVersion = s;
    }

    /**
     * @return ini version number
     */
    public String getIniVersionNumber() {
	return ini.getVersionNumber();
    }

    /**
     * @return ini user settings directory
     */
    public String getUserSettingsDirectory() {
        return ini.getUserSettingsDirectory();
    }

    /**
     * @return ini option for if Qquaqua should be use for Macs
     */
    public boolean getUseQuaqua() {
        return ini.getUseQuaqua();
    }

    /**
     * @param b - true if Quaqua should be used
      */
    public void setUseQuaqua(boolean b) {
        ini.setUseQuaqua(b);
    }


    /**
     * @param level - default debug level
     */
    public void setDebugLevel(int level)
    {
        ini.setDebugLevel(level);
    }

    /**
     * @return current debug level
     */
    public int getDebugLevel()
    {
        return ini.getDebugLevel();
    }

    /**
     * @return UI bounds for application window
     */
    public Rectangle getUIBounds() {
        return ini.getUIBounds();
    }

    /**
     * @param bounds - set UI bounds for application window
     */
    public void setUIBounds(Rectangle bounds) {
        ini.setUIBounds(bounds);
    }

    /**
     * @return feedback URL for QA system
     */
    public String getFeedbackUrl() {
        return qaBaseURL + "/upload_processing/";
    }

    /**
     * @return token URL for QA system
     */
    public String getTokenUrl() {
        return qaBaseURL + "/initial/";
    }

    /**
     * @return upload URL for QA system
     */
    public String getUploaderUrl() {
        return qaBaseURL + "/upload_processing/";
    }

    /**
     * @return ini user full path
     */
    public boolean getUserFullPath() {
        return ini.getUserFullPath();
    }

    /**
     * @return ini user full path
     */
    public void setUserFullPath(boolean b) {
        ini.setUserFullPath(b);
    }

    /**
     * @return ini user full path
     */
    public boolean getCustomImageNaming() {
        return ini.getCustomImageNaming();
    }

    /**
     * @return ini user full path
     */
    public void setCustomImageNaming(boolean b) {
        ini.setCustomImageNaming(b);
    }

    /**
     * @return ini user full path
     */
    public int getNumOfDirectories() {
        return ini.getNumOfDirectories();
    }

    /**
     * @return ini user full path
     */
    public void setNumOfDirectories(int i) {
        ini.setNumOfDirectories(i);
    }

    //
    // HELPERS
    //

    /**
     * Build prompt
     *
     * @param value
     * @param prompt
     * @param hide - use *s for characters
     */
    protected void prompt(Value value, String prompt, boolean hide) {

        String v = value.toString();
        if (hide) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < v.length(); i++) {
                sb.append("*");
            }
            v = sb.toString();
        }
        System.out.print(String.format("%s[%s]:", prompt, v));
        String input;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                input = br.readLine();
                if (input == null || input.trim().equals("")) {
                    continue;
                }
                value.set(value.fromString(input));
            } catch (IOException e) {
		log.error("IGNORING: ", e);
                continue;
            }
        }
    }

    /**
     *  if can't log in request needed information
     */
    public void requestFromUser() {
        if (!canLogin()) {
            loadAll();
            prompt(hostname, " Enter server name: ", false);
            prompt(username, " Enter user name: ", false);
            prompt(password, " Enter password: ", true);
        }
    }

    protected List<Value<?>> values() {
        List<Value<?>> rv = new ArrayList<Value<?>>();
        for (Field f : getClass().getFields()) {
            try {
                Object o = f.get(this);
                if (o instanceof Value) {
                    Value<?> cv = (Value<?>) o;
                    rv.add(cv);
                }
            } catch (Exception e) {
                log.debug("Error during field lookup: " + e);
            }
        }
        return rv;
    }

    public Map<String, String> map() {
        Map<String, String> rv = new HashMap<String, String>();
        for (Value<?> cv : values()) {
            rv.put(cv.key, cv.toString());
        }
        return rv;
    }


    /**
     * Loads gui specific values for which it makes sense to have a preferences values.
     *
     * @see #saveAll()
     */
    public void loadGui() {
         email.load();
    }

     /**
      * Saves gui specific values for which it makes sense to have a preferences values.
      *
      * @see #saveAll()
      */
     public void saveGui() {
          email.store();
     }

    /**
     * Loads all the values for which it makes sense to have a preferences values.
     *
     * @see #saveAll()
     */
    public void loadAll() {

        // Moving to expliti calls.
        // for (Value<?> cv : values()) {
        //    cv.load();
        // }
        savedProject.load();
        savedDataset.load();
        savedScreen.load();

        useCustomImageNaming.load();
        useFullPath.load();
        numOfDirectories.load();
        savedDirectory.load();


        sendLogFile.load();
        sendFiles.load();
        sendReport.load();

        port.load();
    }


    /**
     * @see #loadAll()
     */
    public void saveAll() {

        // Moving to explicit calls
        // for (Value<?> cv : values()) {
        //    cv.store();
        // }

        savedProject.store();
        savedDataset.store();
        savedScreen.store();

        useCustomImageNaming.store();
        useFullPath.store();
        numOfDirectories.store();
        savedDirectory.store();

        sendLogFile.store();
        sendFiles.store();
        sendReport.store();

        try {
            prefs.flush();
            ini.flushPreferences();
        } catch (BackingStoreException e) {
            log.error(e.toString()); // slf4j migration: toString()
            throw new RuntimeException(e);
        }
    }

    /**
     * Container which thread-safely makes a generic configuration value
     * available, without requiring getters and setters.
     *
     * @param <T>
     */
    public static abstract class Value<T> {

        final AtomicReference<T> _current = new AtomicReference<T>();

        final String key, omeroKey;
        final Preferences prefs;
        final IniFileLoader ini;
        final Properties props;
        final T _default;

        /**
         * Records the load location
         */
        Object which = null;

        /**
         * Ctor taking an {@link ImportConfig} instance meaning that all the
         * context values are used.
         */
        Value(String key, ImportConfig config) {
            this(key, config, null, null);
        }

        Value(String key, ImportConfig config, T defValue) {
            this(key, config, defValue, null);
        }

        Value(String key, ImportConfig config, T defValue, String omeroKey) {
            this.key = key;
            this.omeroKey = omeroKey;
            this.ini = config.ini;
            this.prefs = config.prefs;
            this.props = config.props;
            _default = defValue;
            _current.set(null);
        }

        /**
         * Returns the generic type contained by this holder. This does not
         * touch the persistent stores, but only accesses the value in-memory.
         */
        public T get() {
            if (_current.get() == null)
                return _default;
            else return _current.get();
        }

        /**
         * Sets the in-memory value, which will get persisted on
         * {@link #store()} when {@link ImportConfig#saveAll()} is called.
         */
        public void set(T t) {
            _current.set(t);
        }

        @Override
        public String toString() {
            T t = get();
            if (t == null) {
                return "";
            } else {
                return t.toString();
            }
        }

        /**
         * Stores the current value back to some medium. The decision of which
         * medium is based on the current value of {@link #which}. In each case,
         * the type-matching source is used <em>except</em> when the
         * {@link Properties} are used, since this is most likely not a
         * persistent store.
         */
        public synchronized void store() {
            if (which instanceof Properties || which instanceof Preferences) {
                prefs.put(key, toString());
                log.debug("Saved " + key + " to " + prefs);
            } else if (which instanceof IniFileLoader) {
                // FIXME ((IniFileLoader)which).set
                log.debug("Saved " + key + " to " + ini);
            } else if (which == null && prefs != null) { // Loaded from defaults
                prefs.put(key, toString());
                log.debug("Freshly saved " + key + " to " + prefs);
            } else {
                log.debug("WHICH:" + which); // Unknown state
            }

        }

        /**
         * Loads properties from various locations. In order, the
         * {@link Properties} argument, the {@link PreferenceContext}, the
         * {@link Preferences}, the {@link IniFileLoader}, and finally the
         * default value.
         */
        public synchronized void load() {

            if (empty() && props != null) {
                set(fromString(props.getProperty(key)));
                if (!empty()) {
                    which = props;
                    log.debug("Loaded " + key + " from " + props);
                    return;
                }
            }

            if (empty() && prefs != null) {
                set(fromString(prefs.get(key, "")));
                if (!empty()) {
                    which = prefs;
                    log.debug("Loaded " + key + " from " + prefs);
                    return;
                }
            }

            if (empty() && ini != null) {
                // set(fromString((ini.getProperty(key));
                log.debug("Loaded " + key + " from " + ini);
                // break; FIXME
            }

            if (empty()) {
                set(_default);
                log.debug("Loaded " + key + " from default");
                which = null;
            }
        }

        public boolean empty() {
            return get() == null;
        }

        protected abstract T fromString(String string);
    }

    public static class StrValue extends Value<String> {

        public StrValue(String key, ImportConfig config) {
            super(key, config);
        }

        public StrValue(String key, ImportConfig config, String defValue) {
            super(key, config, defValue);
        }

        public StrValue(String key, ImportConfig config, String defValue,
                String omeroKey) {
            super(key, config, defValue, omeroKey);
        }

        @Override
        protected String fromString(String arg0) {
            return arg0;
        }

        public boolean empty() {
            String s = get();
            return s == null || s.length() == 0;
        }
    }

    public static class AnnotationListValue extends Value<List<Annotation>> {

        public AnnotationListValue(String key, ImportConfig config,
                                   List<Annotation> defValue) {
            super(key, config, defValue);
        }

        @Override
        protected List<Annotation> fromString(String string) {
            throw new RuntimeException("Not implemented.");
        }
    }

    public static class DoubleArrayValue extends Value<Double[]> {

        public DoubleArrayValue(String key, ImportConfig config,
                                Double[] defValue) {
            super(key, config, defValue);
        }

        @Override
        protected Double[] fromString(String string) {
            throw new RuntimeException("Not implemented.");
        }
    }

    public static class PassValue extends StrValue {
        public PassValue(String key, ImportConfig config) {
            super(key, config);
        }

        @Override
        public synchronized void store() {
            log.trace("Skipping password storage");
        }
    }

    public static class BoolValue extends Value<Boolean> {

        public BoolValue(String key, ImportConfig config, boolean defValue) {
            super(key, config, defValue);
        }

        @Override
        protected Boolean fromString(String arg0) {
            if (arg0 == null) {
                return null;
            }
            return Boolean.parseBoolean(arg0);
        }
    }

    public static class IntValue extends Value<Integer> {
        public IntValue(String key, ImportConfig config, int defValue) {
            super(key, config, Integer.valueOf(defValue));
        }

        public IntValue(String key, ImportConfig config, int defValue,
                String omeroKey) {
            super(key, config, Integer.valueOf(defValue), omeroKey);
        }

        @Override
        protected Integer fromString(String arg0) {
            try {
                return Integer.valueOf(arg0);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }

    }

    public static class LongValue extends Value<Long> {
        public LongValue(String key, ImportConfig config, Long defValue) {
            super(key, config, defValue);
        }

        @Override
        protected Long fromString(String arg0) {
            try {
                return Long.valueOf(arg0);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
    }

    public static class FileValue extends Value<File> {
        public FileValue(String key, ImportConfig config) {
            super(key, config);
        }

        @Override
        protected File fromString(String arg0) {
            if (arg0 == null) {
                return null;
            }
            return new File(arg0);
        }

        @Override
        public File get() {
            File f = super.get();
            if (f != null && f.exists()) {
                return f;
            } else {
                set(null);
                return null;
            }
        }
    }

    public ImportTarget getTarget() {
        TargetBuilder builder = new TargetBuilder();
        if (target.get() != null)
        {
            return builder.parse(target.get()).build();
        }
        if (DATASET_CLASS.equals(targetClass.get()))
        {
            return builder.parse(
                    String.format("%s:%s", DATASET_CLASS, targetId.get()))
                    .build();
        }
        else if (SCREEN_CLASS.equals(targetClass.get()))
        {
            return builder.parse(
                    String.format("%s:%s", SCREEN_CLASS, targetId.get()))
                    .build();
        }
        return null;
    }

}
