/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.file.modulo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parses the modulo annotation.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class ModuloParser
{

    /** The tags that we handle. */
    private static String[] tagsModulo = {ModuloInfo.MODULO_C,
        ModuloInfo.MODULO_Z, ModuloInfo.MODULO_T};

    /** The file to parse.*/
    private String file;

    /** Hosts the contents of the modulo tags.*/
    private List<ModuloInfo> modulos;

    /** Collects all tags that we have to handle from the file. */
    private List<Node> moduloTags;
    
    /** The configuration file. */
    private Document document;

    /** 
     * Retrieves the content of the tags that we handle.
     * Stores their DOM representation (DOM node) into a list.
     */
    private void readModuloEntries()
    {
        NodeList list;
        Node n;
        for (int k = 0; k < tagsModulo.length; ++k) {
            list = document.getElementsByTagName(tagsModulo[k]);
            for (int i = 0; i < list.getLength(); ++i) {
                n = list.item(i);
                if (n.hasAttributes()) moduloTags.add(n);
            }
        }
    }

    /** 
     * Creates a concrete <code>ModuloInfo</code> object to handle the
     * conversion of the content of the passed tag into an object. 
     *
     * @param tag DOM node representing either a <i>modulo</i> tag.
     * @return See above.
     * @throws Exception If the tag couldn't be handled.
     */  
    ModuloInfo createModuloFor(Node tag)
        throws Exception
    {
        if (!tag.hasAttributes())
            throw new Exception("Missing tag's attributes.");
        ModuloInfo info = new ModuloInfo(tag.getNodeName());
        NamedNodeMap attributes = tag.getAttributes();
        Node attribute;
        for (int i = 0; i < attributes.getLength(); ++i) {
            attribute = attributes.item(i);
            if (ModuloInfo.START.equals(attribute.getNodeName()))
                info.setStart(Double.parseDouble(attribute.getNodeValue()));
            else if (ModuloInfo.END.equals(attribute.getNodeName()))
                info.setEnd(Double.parseDouble(attribute.getNodeValue()));
            else if (ModuloInfo.STEP.equals(attribute.getNodeName()))
                info.setStep(Double.parseDouble(attribute.getNodeValue()));
            else if (ModuloInfo.TYPE.equals(attribute.getNodeName()))
                info.setType(attribute.getNodeValue());
            else if (ModuloInfo.TYPE_DESCRIPTION.equals(
                    attribute.getNodeName()))
                info.setTypeDescription(attribute.getNodeValue());
            else if (ModuloInfo.UNIT.equals(attribute.getNodeName()))
                info.setUnit(attribute.getNodeValue());
        }
        NodeList nodes = tag.getChildNodes();
        Double min = Double.MAX_VALUE;
        Double max = Double.MIN_VALUE;
        Double v;
        List<Double> labels = new ArrayList<Double>();
        for (int i = 0; i < nodes.getLength(); i++) {
            attribute = nodes.item(i);
            if (attribute.getNodeType() == Node.ELEMENT_NODE) {
                if (ModuloInfo.LABEL.equals(attribute.getNodeName())) {
                    v = Double.parseDouble(
                            attribute.getFirstChild().getNodeValue());
                    if (v < min) min = v;
                    if (v > max) max = v;
                    labels.add(v);
                }
            }
        }
        if (!CollectionUtils.isEmpty(labels)) {
            info.setStart(min);
            info.setEnd(max);
            info.setLabels(labels);
        }
        
        return info;
    }

    /**
     * Creates a new instance.
     * 
     * @param file The file to parse
     */
    public ModuloParser(String file)
    {
        if (CommonsLangUtils.isEmpty(file))
            throw new IllegalArgumentException("No file to parse.");
        this.file = file;
        modulos = new ArrayList<ModuloInfo>();
        moduloTags = new ArrayList<Node>();
    }

    /**
     * Parses the file.
     *
     *  @throws Exception If an error occurs when the annotation cannot be
     *  parsed.
     */
    public void parse()
        throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        BufferedWriter out = null;
        File f = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            f = File.createTempFile("modulo", ".xml");
            out = new BufferedWriter(new FileWriter(f));
            out.write(file);
            out.close();
            out = null;
            document = builder.parse(f);
            readModuloEntries();
            Iterator<Node> i = moduloTags.iterator();
            Node node;
            ModuloInfo entry;
            while (i.hasNext()) {
               node = i.next();
               entry = createModuloFor(node);
               if (entry != null) modulos.add(entry);
            }
        } catch (Exception e) {
            throw new Exception("Cannot read the file", e);
        } finally {
            if (out != null) out.close();
            if (f != null) f.delete();
        }
    }

    /**
     * Returns the <code>ModuloInfo</code>s.
     *
     * @return See above.
     */
    public List<ModuloInfo> getModulos() { return modulos; }
}
