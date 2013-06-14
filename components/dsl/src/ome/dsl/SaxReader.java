/*
* ome.dsl.SaxReader
*
*   Copyright 2006 University of Dundee. All rights reserved.
*   Use is subject to license terms supplied in LICENSE.txt
*/

package ome.dsl;

// Java imports
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

// Application-internal dependencies

/**
* reads semantic-type-xml and produces a Set of SemanticType objects. Most
* logic is passed off to the {@see ome.dsl.SemanticType ST} and
* {@see ome.dsl.Property Property} classes.
*/
public class SaxReader {

private static Log log = LogFactory.getLog(SaxReader.class);

/** input file */
URL xmlFile;

/** handler which collects all types and properties from the input file */
DSLHandler handler;

/** SAXparser which does the actualy processing */
javax.xml.parsers.SAXParser parser;

public SaxReader(String profile, File file) {
this(file, new DSLHandler(profile));
}

    public SaxReader(File file, DSLHandler dslHandler) {
        handler = dslHandler;

        try {
            xmlFile = file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error determining file's path:" + file
                    + " :\n" + e.getMessage(), e);
        }
        init();
    }

    private void init() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            parser = factory.newSAXParser();

            // XMLReader reader = parser.getXMLReader();
        } catch (Exception e) {
            throw new RuntimeException("Error setting up SaxReader :\n"
                    + e.getMessage(), e);
        }
    }

    /** parses file and returns types */
    public void parse() {
        try {
            parser.parse(xmlFile.getPath(), handler);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing " + xmlFile + " :\n"
                    + e.getMessage(), e);
        }
    }

    public List<SemanticType> process() {
        return handler.process();
    }

}

class DSLHandler extends DefaultHandler {

    private static Log log = LogFactory.getLog(DSLHandler.class);

    // Turns output on/off
    public boolean verbose = false;

    // Indention for output
    private String depth = "";

    // For handling
    private final Map<String, SemanticType> types = new HashMap<String, SemanticType>();

    private SemanticType type;

    private Property property;

    private final String profile;

    DSLHandler(String profile) {
        this.profile = profile;
    }

    /** dispatches to output (printing) and handling (object-creation) routines */
    @Override
    public void startElement(String arg0, String arg1, String element,
            Attributes attrs) throws SAXException {
        if (verbose) {
            outputStart(element, attrs);
        }
        handleEntry(element, attrs);
        super.startElement(arg0, arg1, element, attrs);
    }

    /** dispatches to output (printing) and handling (object-creation) routines */
    @Override
    public void endElement(String arg0, String arg1, String element)
            throws SAXException {
        super.endElement(arg0, arg1, element);
        handleExit(element);
        if (verbose) {
            outputStop(element);
        }
    }

    /** creates a new type or property based on element name */
    private void handleEntry(String element, Attributes attrs) {
        if (Property.FIELDS.contains(element)) {

            if (null != property) {
                throw new IllegalStateException("Trying to enter property "
                        + element + " from within property" + property);
            }

            if (null == type) {
                throw new IllegalStateException("Trying to create property "
                        + element + " without a type!");
            }

            property = Property.makeNew(element, type, attrs2props(attrs));

        } else if ("properties".equals(element)) {
            // ok. these usually contains lots of properties
        } else if (SemanticType.TYPES.contains(element)) {
            if (null != type) {
                throw new IllegalStateException("Trying to enter type "
                        + element + " from within type " + type);
            }

            type = SemanticType.makeNew(profile, element, attrs2props(attrs));

        } else if ("types".equals(element)) {
            // also ok.
        } else {
            log.debug("Deprecated: In the future elements of type " + element
                    + " will be considered an error.");
        }

    }

    /** checks to see that after type creation, the model is in a valid state */
    private void handleExit(String element) {

        if (Property.FIELDS.contains(element)) {

            if (null == property) {
                throw new IllegalStateException(
                        "Exiting non-extant property!\n" + "Element:" + element
                                + "\nType:" + type + "\nProperty:" + property);
            }

            if (null == type) {
                throw new IllegalStateException("Inside of non-extant type!!\n"
                        + "Element:" + element + "\nType:" + type);
            }

            property.validate();
            type.getProperties().add(property);
            property = null;

        } else if (SemanticType.TYPES.contains(element)) {

            if (null == type) {
                throw new IllegalStateException("Exiting non-extent type!\n"
                        + "Element:" + element + "\nType:" + type);
            }

            type.validate();
            types.put(type.getId(), type);
            type = null;

        }
    }

    /**
     * Initial processing.
     * 
     */
    public List<SemanticType> process() {

        /*
         * Handles the various link ups for annotations. (Possibly temporary)
         * This creates new types and therefore should come first.
         */
        Set<SemanticType> additions = new HashSet<SemanticType>();
        for (String id : types.keySet()) {
            SemanticType t = types.get(id);
            if (t.getAnnotated() != null && t.getAnnotated()) {

                String newId = "ome.model.annotations." + t.getShortname()
                        + "AnnotationLink";
                SemanticType ann = types
                        .get("ome.model.annotations.Annotation");

                // Create link
                Properties linkP = new Properties();
                linkP.setProperty("id", newId);
                LinkType l = new LinkType(profile, linkP);

                Properties parentP = new Properties();
                parentP.setProperty("type", t.getId());
                LinkParent lp = new LinkParent(l, parentP);

                lp.validate();
                l.getProperties().add(lp);

                Properties childP = new Properties();
                childP.setProperty("type", ann.getId());
                LinkChild lc = new LinkChild(l, childP);

                lc.validate();
                l.getProperties().add(lc);

                l.validate();
                additions.add(l);

                // And now create the links to the link
                Properties clP = new Properties();
                clP.setProperty("name", "annotationLinks");
                clP.setProperty("type", newId);
                clP.setProperty("target", ann.getId());
                ChildLink cl = new ChildLink(t, clP);
                cl.setBidirectional(false);

                cl.validate();
                t.getProperties().add(cl);
            }
        }
        for (SemanticType semanticType : additions) {
            types.put(semanticType.getId(), semanticType);
        }

        /**
         * Now handling the named and described attributes in the
         * code-generation to free up the templates from the responsibility
         */
        for (SemanticType namedOrDescribed : types.values()) {
            Boolean named = namedOrDescribed.getNamed();
            Boolean descrd = namedOrDescribed.getDescribed();
            if (named != null && named.booleanValue()) {
                Properties p = new Properties();
                p.setProperty("name", "name");
                p.setProperty("type", "string");
                RequiredField r = new RequiredField(namedOrDescribed, p);
                namedOrDescribed.getProperties().add(r);
            }
            if (descrd != null && descrd.booleanValue()) {
                Properties p = new Properties();
                p.setProperty("name", "description");
                p.setProperty("type", "text");
                OptionalField o = new OptionalField(namedOrDescribed, p);
                namedOrDescribed.getProperties().add(o);
            }
        }

        /*
         * Example: Pixels: <zeromany name="thumbnails"
         * type="ome.model.display.Thumbnail" inverse="pixels"/> Thumnail:
         * <required name="pixels" type="ome.model.core.Pixels"/>
         * 
         * We want Thumbnail.pixels to be given the inverse "thumbnails"
         * 
         * This only holds so long as there is only one link from a given type
         * to another given type, which does *not* hold true for
         * AnnotationAnnotationLinks. Therefore here we have a WORKAROUND.
         */
        for (String id : types.keySet()) { // "ome...Pixels"
            SemanticType t = types.get(id); // Pixels
            for (Property p : t.getProperties()) { // thumbnails
                if (!handleLink(p)) { // WORKAROUND
                    String rev = p.getType(); // "ome...Thumbnail"
                    String inv = p.getInverse(); // "pixels"
                    if (inv != null) {
                        if (types.containsKey(rev)) {
                            SemanticType reverse = types.get(rev); // Thumbnail
                            for (Property inverse : reverse.getProperties()) {
                                if (inverse.getType().equals(id)) { // "ome...Pixels"
                                    inverse.setInverse(p.getName());
                                }
                            }
                        }
                    }
                }
            }
        }

        /*
         * Another post-processing step, which checks links for bidirectionality
         */
        for (String id : types.keySet()) {
            SemanticType t = types.get(id);
            for (Property p : t.getProperties()) { // thumbnails
                if (p instanceof AbstractLink) {
                    AbstractLink link = (AbstractLink) p;
                    String targetId = link.getTarget();
                    SemanticType target = types.get(targetId);
                    if (target == null) {
                        throw new RuntimeException("No type " + targetId
                                + " found as target of " + link);
                    }
                    boolean found = false;
                    for (Property p2 : target.getProperties()) {
                        if (id.equals(p2.getTarget())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        link.setBidirectional(Boolean.FALSE);
                    }
                }
            }
        }

        /*
         * Check for all ordered relationships and apply unique constraints
         */
        for (String id : types.keySet()) {
            SemanticType t = types.get(id);
            for (Property property : t.getClassProperties()) {
                if (property instanceof ManyZeroField) {
                    Boolean ord = property.getOrdered();
                    if (ord != null && ord.booleanValue()) {
                        String name = property.getName();
                        t.getUniqueConstraints().add(
                                String
                                        .format("\"%s\",\"%s_index\"", name,
                                                name));
                    }
                }
            }
        }

        /*
         * Similarly apply UNIQUE (parent, child) to all links
         */
        for (String id : types.keySet()) {
            SemanticType t = types.get(id);
            if (t instanceof LinkType) {
                LinkType link = (LinkType) t;
                if (link.getGlobal()) {
                    link.getUniqueConstraints().add("\"parent\",\"child\"");
                } else {
                    link.getUniqueConstraints().add("\"parent\",\"child\",\"owner_id\"");
                }
            }
        }

        /**
         * Each property is given its an actual
         * {@link SemanticType implementation which it belongs to and points to
         */
        for (SemanticType semanticType : types.values()) {
            for (Property property : semanticType.getPropertyClosure()) {
                
                SemanticType target = types.get(property.getType());
                property.setActualTarget(target);
                
                SemanticType currentType = semanticType;
                SemanticType actualType = semanticType;
                
                while (currentType != null) {
                    List<Property> classProperties = currentType.getClassProperties();
                    if (classProperties.contains(property)) {
                        actualType = currentType;
                        break;
                    }
                    String superclass = currentType.getSuperclass();
                    currentType = superclass == null ? null : types.get(currentType.getSuperclass());
                }
                
                property.setActualType(actualType);
            }
        }

        /*
         * Final post-processing step. Each semantic type should be given it's
         * finalized superclass instance as well as its Details property.
         */
        for (String id : types.keySet()) {
            SemanticType t = types.get(id);
            String superclass = t.getSuperclass();
            if (superclass != null) {
                SemanticType s = types.get(superclass);
                t.setActualSuperClass(s);
            } else {
                t.getProperties().add(new DetailsField(t, new Properties()));
            }
        }

        return new ArrayList<SemanticType>(types.values());
    }

    private boolean handleLink(Property p) {
        if (!p.getIsLink()) {
            return false;
        }
        String name = p.getName();
        if (!name.equals("child") && name.equals("parent")) {
            return false;
        }

        // For links, the inverse was already set by the constructor

        return true;
    }

    /** simple outputting routine with indention */
    private void outputStart(String element, Attributes attrs) {
        if (log.isDebugEnabled()) {
            log.debug(depth + element + "(");
        }
        for (int i = 0; i < attrs.getLength(); i++) {
            String attr = attrs.getQName(i);
            String value = attrs.getValue(i);
            if (log.isDebugEnabled()) {
                log.debug(" " + attr + "=\"" + value + "\" ");
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("): ");
        }
        depth += "  ";
    }

    /** reduces indention for output */
    private void outputStop(String element) {
        depth = depth.substring(2);
    }

    /** converts xml attributes to java.util.Properties */
    private Properties attrs2props(Attributes attrs) {
        Properties p = new Properties();
        for (int i = 0; i < attrs.getLength(); i++) {
            String key = attrs.getQName(i);
            String value = attrs.getValue(i);
            p.put(key, value);
        }
        return p;
    }

}
