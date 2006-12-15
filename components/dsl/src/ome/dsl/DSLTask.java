/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.dsl;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

    private List _fileSets = new ArrayList();

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

        Set<SemanticType> types = new HashSet<SemanticType>();
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

        types.addAll(handler.process());

        for (Iterator it = types.iterator(); it.hasNext();) {
            SemanticType st = (SemanticType) it.next();
            VelocityHelper vh = new VelocityHelper();
            vh.put("type", st);
            try {
                String file = _outputDir + File.separator
                        + st.getId().replaceAll("[.]", "\\" + File.separator)
                        + ".hbm.xml";
                writeToFile(vh, file, "ome/dsl/mapping.vm");
            } catch (Exception e) {
                throw new BuildException("Error while writing type:" + st, e);
            }
        }

        VelocityHelper vh = new VelocityHelper();
        vh.put("types", types);
        try {
            String file = _outputDir + File.separator + File.separator
                    + "data.sql";
            writeToFile(vh, file, "ome/dsl/data.vm");
        } catch (Exception e) {
            throw new BuildException("Error while writing data:", e);
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
}
