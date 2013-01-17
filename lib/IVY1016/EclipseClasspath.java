/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package IVY1016;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.ivy.ant.*;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.util.Message;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.DOMElementWriter;
import org.apache.tools.ant.util.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * An Ant task that updates a project's Eclipse .classpath file according to the result of
 * <code>ivy:resolve</code>. Supports attaching sources jars to entries if any are available.
 */
public class EclipseClasspath extends IvyCacheTask {

    private static List<String> IGNORE = Arrays.asList(
        "blitz-test",
        "blitz",
        "server",
        "common",
        "model-",
        "dsl",
        "romio",
        "rendering",
        "common-test",
        "dsl-test",
        "omero_client"
        //"postgresql", These are needed.
        //"ice",
        //"ice-db",
    );

    private static final String ATTR_IVYGEN = "ivygen";
    private static final String TAG_CLASSPATH_ENTRY = "classpathentry";

    private String sourceType = "sources";
    private String classpathFile = ".classpath";
    private boolean filter = false;

    public void doExecute() throws BuildException {
        prepareAndCheck();
        FileWriter output = null;
        try {
            Map binMap = new HashMap();
            Map sourceMap = new HashMap();
            OUTER:
            for (Iterator iter = getArtifactReports().iterator(); iter.hasNext();) {
                ArtifactDownloadReport a = (ArtifactDownloadReport) iter.next();
                org.apache.ivy.core.module.id.ArtifactRevisionId arid = a.getArtifact().getId();
                org.apache.ivy.core.module.id.ArtifactId aid = arid.getArtifactId();
                org.apache.ivy.core.module.id.ModuleId mid = aid.getModuleId();
                String modOrg = mid.getOrganisation();
                String modName = mid.getName();
                if (filter && "omero".equals(modOrg)) {
                    for (String prefix : IGNORE) {
                        if (modName.startsWith(prefix)) {
                            continue OUTER;
                        }
                    }
                }

                String artifactName = a.getArtifact().getName();
                if (a.getType().equals(sourceType)) {
                    sourceMap.put(artifactName, a.getLocalFile());
                } else {
                    binMap.put(artifactName, a.getLocalFile());
                }
                Message.verbose("Artifact " + artifactName + ": " + a.getLocalFile());
            }

            File template = new File(classpathFile+"-template"); // Changed by OME
            if (!template.exists()) {
                log("No .classpath-template file found.");
                return; // EARLY EXIT!
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(template);
            Element root = doc.getDocumentElement();
            // go through the classpath entries
            Node node = root.getFirstChild();
            while (node != null) {
                if (node instanceof Element
                    && ((Element)node).getTagName().equals(TAG_CLASSPATH_ENTRY)
                    && Boolean.parseBoolean(((Element)node).getAttribute(ATTR_IVYGEN))) {
                    // remove an entry if it has ivygen="true"
                    Node prev = node;
                    node = node.getNextSibling();
                    root.removeChild(prev);
                } else if (node instanceof Text && Pattern.matches("\\s+", node.getTextContent())) {
                    // remove the whitespaces between classpathentry elements. This helps keeping
                    // the changes minimal when writing the dom, and makes it easier to diff and merge.
                    Node prev = node;
                    node = node.getNextSibling();
                    root.removeChild(prev);
                } else {
                    node = node.getNextSibling();
                }
            }

            // insert entries, marking them with ivygen="true"
            for (Iterator iEntry = binMap.entrySet().iterator(); iEntry.hasNext();) {
                Map.Entry entry = (Map.Entry)iEntry.next();
                String artifactName = (String)entry.getKey();
                Element newEntry = doc.createElement(TAG_CLASSPATH_ENTRY);
                newEntry.setAttribute(ATTR_IVYGEN, "true");
                newEntry.setAttribute("kind", "lib");
                newEntry.setAttribute("path", ((File)entry.getValue()).getAbsolutePath());
                File sourcePath = (File)sourceMap.get(artifactName);
                if (sourcePath != null) {
                    newEntry.setAttribute("sourcepath", sourcePath.getAbsolutePath());
                }
                root.appendChild(newEntry);
            }

            // write the result DOM to a temp file.
            final FileUtils fileUtils = FileUtils.newFileUtils();
            File newClasspath = fileUtils.createTempFile("ivygen-", ".classpath", getProject().getBaseDir());
            output = new FileWriter(newClasspath);
            DOMElementWriter writer = new DOMElementWriter();
            output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write(root, output, 0, "\t");

            // overwrite the original .classpath if all went well.
            fileUtils.copyFile(newClasspath, new File(classpathFile));
            newClasspath.delete();
        } catch (Exception ex) {
            throw new BuildException("Unable to generate Eclipse classpath:", ex);
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * @see org.apache.ivy.ant.IvyPostResolveTask#prepareAndCheck()
     */
    protected void prepareAndCheck() {
        super.prepareAndCheck();
        classpathFile = getProject().getBaseDir() + "/" + classpathFile;
    }

    /**
     * @return the sourceType
     */
    public String getSourceType() {
        return sourceType;
    }

    /**
     * @param sourceType the sourceType to set
     */
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    /**
     * @return the classpathFile
     */
    public String getClasspathFile() {
        return classpathFile;
    }

    /**
     * @param classpathFile the classpathFile to set
     */
    public void setClasspathFile(String classpathFile) {
        this.classpathFile = classpathFile;
    }

    /**
     * @param whether or not to filter out OMERO items.
     */
    public void setFilter(boolean filter) {
        this.filter = filter;
    }
}
