package org.openmicroscopy.shoola.util.ui.colourpicker;

/**
 * Item for the Lookup Table list, which shows a nicely formatted name for the
 * lookup table, based on the file name.
 */
public class LookupTableItem implements Comparable<LookupTableItem> {

    /** Item for representing "None" selection */
    public static final LookupTableItem NONE = new LookupTableItem("None",
            false);

    /** Item for being used as Separator */
    public static final LookupTableItem SEPARATOR = new LookupTableItem("---",
            false);

    /** The file name **/
    private String filename;

    /** More readable name generated from the filename */
    private String readableName;

    /**
     * Create new instance
     * 
     * @param filename
     *            The lut file name
     */
    public LookupTableItem(String filename) {
        this(filename, true);
    }

    /**
     * Creates a new instance
     * 
     * @param filename
     *            The file name
     * @param generateReadableName
     *            Pass <code>true</code> if a readable name should be generated
     */
    private LookupTableItem(String filename, boolean generateReadableName) {
        this.filename = filename;
        if (generateReadableName)
            this.readableName = generateReadableName(filename);
        else
            this.readableName = filename;
    }

    /**
     * Generates a more readable name for the given lut filename by removing
     * '*.lut' extension, underscores and using upper case at the beginning of
     * words.
     * 
     * @param filename
     *            The filename
     * @return See above
     */
    private String generateReadableName(String filename) {
        filename = filename.replace(".lut", "");
        String[] parts = filename.replace(".lut", "").split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            sb.append(part.substring(0, 1).toUpperCase());
            if (part.length() > 1) {
                sb.append(part.substring(1));
            }

            if (i < parts.length - 1)
                sb.append(' ');
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return readableName;
    }

    /**
     * Get the lut file name
     * 
     * @return See above
     */
    public String getFilename() {
        return this.filename;
    }

    /**
     * @param filename
     *            The file name
     * @return <code>true</code> if the given filename matches the filename of
     *         this {@link LookupTableItem}
     */
    public boolean matchesFilename(String filename) {
        return this.filename.equals(filename);
    }

    @Override
    public int compareTo(LookupTableItem o) {
        return this.readableName.compareTo(o.readableName);
    }
}
