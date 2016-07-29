package org.openmicroscopy.shoola.util.ui.colourpicker;

import java.awt.Color;

/**
 * Item for the Lookup Table list
 */
public class LookupTableItem implements Comparable<LookupTableItem> {

    /** Item for being used as Separator */
    public static final LookupTableItem SEPARATOR = new LookupTableItem("---");

    /** The file name */
    private String filename;

    /** The color */
    private Color color;

    /**
     * More readable name (in case of lookup table it's generated from the
     * filename)
     */
    private String label;

    /**
     * Create new instance for a lookup table
     * 
     * @param filename
     *            The lut file name
     */
    public LookupTableItem(String filename) {
        this.filename = filename;
        this.label = generateLabel(filename);
    }

    /**
     * Create new instance for defined color
     * 
     * @param color
     *            The color
     * @param label
     *            The label
     */
    public LookupTableItem(Color color, String label) {
        this.color = color;
        this.label = label;
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
    private String generateLabel(String filename) {
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

    /**
     * Checks if this {@link LookupTableItem} represents a
     * lookup table file
     * @return See above
     */
    public boolean hasLookupTable() {
        return this.filename != null;
    }

    /**
     * Get the label text 
     * @return See above
     */
    public String getLabel() {
        return label;
    }
    
    @Override
    public String toString() {
        return getLabel();
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
     * Get the color
     * 
     * @return See above
     */
    public Color getColor() {
        return this.color;
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
        return this.label.compareTo(o.label);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((color == null) ? 0 : color.hashCode());
        result = prime * result
                + ((filename == null) ? 0 : filename.hashCode());
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LookupTableItem other = (LookupTableItem) obj;
        if (color == null) {
            if (other.color != null)
                return false;
        } else if (!color.equals(other.color))
            return false;
        if (filename == null) {
            if (other.filename != null)
                return false;
        } else if (!filename.equals(other.filename))
            return false;
        if (label == null) {
            if (other.label != null)
                return false;
        } else if (!label.equals(other.label))
            return false;
        return true;
    }
    
    
}
