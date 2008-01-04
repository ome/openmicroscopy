/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.dsl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.types.FileSet;
import org.springframework.util.ResourceUtils;

/**
 * An ant task for generating artifacts from the dsl.
 * 
 * Example:
 * 
 * <project ...> <taskdef name="dsl" classname="DSLTask" /> <property
 * name="dest.dir" value="${basedir}/gen-src/mappings"/> <target
 * name="generate"> <mkdir dir="${dest.dir}" /> <dsl outputdir="${dest.dir}">
 * <fileset dir="${mapping.dir}"> <include name="*.xml" /> </fileset> </dsl>
 * </target> </project>
 */
public class DSLTask extends Task {

    public final static String PKG_PLACEHOLDER = "{package-dir}";

    public final static String CLS_PLACEHOLDER = "{class-name}";

    private final List<FileSet> _fileSets = new ArrayList<FileSet>();

    private String _filepattern;

    private String _template;

    private boolean singleType = false;

    public void setFilepattern(String filepattern) {
        _filepattern = filepattern;
        if (_filepattern.contains(CLS_PLACEHOLDER)) {
            singleType = true;
        }
    }

    public void setTemplate(String template) {
        _template = template;
        if (_template == null) {
            throw new BuildException("Template cannot be null");
        }
    }

    public void addFileset(FileSet fileSet) {
        _fileSets.add(fileSet);
    }

    @Override
    public void execute() throws BuildException {
        if (_fileSets.isEmpty()) {
            throw new BuildException("No fileset specified");
        }

        List<SemanticType> types;
        DSLHandler handler = new DSLHandler();

        java.util.Iterator<FileSet> p = _fileSets.iterator();
        while (p.hasNext()) {
            FileSet fileset = p.next();
            DirectoryScanner scanner = fileset
                    .getDirectoryScanner(getProject());
            scanner.scan();
            String[] files = scanner.getIncludedFiles();
            for (int i = 0; i < files.length; i++) {
                String filename = fileset.getDir(getProject()) + File.separator
                        + files[i];
                File file = new File(filename);
                if (!file.exists()) {
                    log("File " + file + " not found.");
                } else {
                    SaxReader sr = new SaxReader(file, handler);
                    sr.parse();
                }
            }
        }

        types = handler.process();

        if (singleType) {
            for (Iterator<SemanticType> it = types.iterator(); it.hasNext();) {
                SemanticType st = it.next();
                try {
                    VelocityHelper vh = new VelocityHelper();
                    vh.put("type", st);
                    String className = st.getShortname();
                    String packageName = st.getId();
                    packageName = packageName.substring(0, packageName
                            .lastIndexOf("."));
                    packageName = packageName.replaceAll("[.]", File.separator);

                    String target = _filepattern;
                    target = target.replace(CLS_PLACEHOLDER, className);
                    target = target.replace(PKG_PLACEHOLDER, packageName);

                    writeToFile(vh, new File(target), _template);
                } catch (Exception e) {
                    throw new BuildException("Error while writing type:" + st,
                            e);
                }
            }
        } else {
            VelocityHelper vhData = new VelocityHelper();
            vhData.put("types", types);
            try {
                writeToFile(vhData, new File(_filepattern), _template);
            } catch (Exception e) {
                throw new BuildException("Error while generating for template:"
                        + _template, e);
            }
        }

    }

    public static InputStream getStream(String str) {
        InputStream in = null;
        try {
            in = DSLTask.class.getClassLoader().getResourceAsStream(str);
        } catch (Exception e) {
            // ok
        }

        if (in == null) {

            try {
                File file = new File(str);
                in = new FileInputStream(file);
            } catch (Exception e) {
                // ignore
            }
        }

        if (in == null) {
            try {
                URL url = ResourceUtils.getURL(str);
                in = url.openStream();
            } catch (Exception e) {
                // ok
            }
        }

        // That didn't work. Then let's try to call ourself again with a
        // "classpath prefix
        if (in == null && !str.startsWith("classpath:")) {
            in = getStream("classpath:" + str);
        }

        return in;
    }

    private void writeToFile(VelocityHelper vh, File file, String template)
            throws IOException {

        InputStream in;
        in = getStream(template);
        if (in == null) {
            throw new BuildException("Cannot resolve template:" + template);
        }

        FileWriter fw = null;

        try {
            mkdir(file);
            fw = new FileWriter(file);
            vh.invoke(in, fw);
        } finally {
            try {
                if (fw != null) {
                    fw.flush();
                    fw.close();
                }
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
    }

    void mkdir(File file) {
        Mkdir mkdir = new Mkdir();
        mkdir.setProject(new Project());
        mkdir.setOwningTarget(new Target());
        mkdir.setDir(new File(file.getParent()));
        mkdir.execute();

    }

    static String fileAsString(File file) {
        StringBuffer contents = new StringBuffer();
        BufferedReader input = null;
        try {
            input = new BufferedReader(new FileReader(file));
            String line = null;

            while ((line = input.readLine()) != null) {
                contents.append(line);
                contents.append(System.getProperty("line.separator"));
            }
        } catch (Exception ex) {
            throw new BuildException("Failed to get file contents", ex);
        } finally {
            try {
                if (input != null) {

                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return contents.toString();
    }
}
