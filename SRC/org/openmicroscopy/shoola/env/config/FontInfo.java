package org.openmicroscopy.shoola.env.config;

/** Creates an Object which contains the informations on the font
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */
class FontInfo {
    
    String  family, style;
    Integer size;
    
    static final String FAMILY = "family", SIZE = "size", STYLE = "style";    
    void setValue(String value, String tag) {
        try {
            if (tag.equals(FAMILY)) family = value;
            else if (tag.equals(SIZE)) size = new Integer(value);
            else if (tag.equals(STYLE)) style = value;
        } catch (Exception ex) { throw new RuntimeException(ex); }
    }
    String getFamily() {
        return family;
    }
    Integer getSize() {
        return size;
    }
    String getStyle() {
        return style;
    }
  
    
}
