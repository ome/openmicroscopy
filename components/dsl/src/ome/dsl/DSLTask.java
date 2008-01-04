/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.dsl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private final List _fileSets = new ArrayList();

    private File _outputDir;

    public void setDestdir(File dir) {
        _outputDir = dir;
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

        java.util.Iterator p = _fileSets.iterator();
        while (p.hasNext()) {
            FileSet fileset = (FileSet) p.next();
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

        for (Iterator it = types.iterator(); it.hasNext();) {
            SemanticType st = (SemanticType) it.next();
            VelocityHelper vh = new VelocityHelper();
            vh.put("type", st);
            List<String> extraLines = new ArrayList<String>();
            Log log = LogFactory.getLog(this.getClass());
            try {
                try {
                    log.info(System.getProperties().getProperty(
                            "java.classpath"));
                    File extra = ResourceUtils.getFile("classpath:ome/extra/"
                            + st.getId());
                    log.warn(extra.toString());
                    if (extra.canRead()) {
                        vh.put("extra", fileAsString(extra));
                        log.warn(fileAsString(extra));
                    }
                    log.warn("XXX");
                } catch (FileNotFoundException fnfe) {
                    // ok
                    log.warn(fnfe.toString());
                }
            } catch (Exception e) {
                throw new BuildException("Error while loading extra code", e);
            }
            try {
                String file = _outputDir + File.separator + "src"
                        + File.separator
                        + st.getId().replaceAll("[.]", "\\" + File.separator)
                        + ".java";
                writeToFile(vh, file, "ome/dsl/object.vm");
            } catch (Exception e) {
                throw new BuildException("Error while writing type:" + st, e);
            }
        }

        VelocityHelper vhData = new VelocityHelper();
        vhData.put("types", types);
        try {
            String file = _outputDir + File.separator + "resources"
                    + File.separator + "data.sql";
            writeToFile(vhData, file, "ome/dsl/data.vm");
        } catch (Exception e) {
            throw new BuildException("Error while writing data:", e);
        }

        VelocityHelper vhCfg = new VelocityHelper();
        vhCfg.put("types", types);
        try {
            String file = _outputDir + File.separator + "resources"
                    + File.separator + "hibernate.cfg.xml";
            writeToFile(vhCfg, file, "ome/dsl/cfg.vm");
        } catch (Exception e) {
            throw new BuildException("Error while writing cfg:", e);
        }

    }

    private void writeToFile(VelocityHelper vh, String file, String template)
            throws IOException {
        mkdir(file);
        FileWriter fw = new FileWriter(file);
        vh.invoke(template, fw);
        fw.flush();
        fw.close();
    }

    void mkdir(String file) {
        Mkdir mkdir = new Mkdir();
        mkdir.setProject(new Project());
        mkdir.setOwningTarget(new Target());
        mkdir.setDir(new File(file.substring(0, file
                .lastIndexOf(File.separator))));
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
