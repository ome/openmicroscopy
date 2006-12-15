/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.dsl;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaSource;

import ome.dsl.VelocityHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * An ant task for generating artifacts from the dsl. Example: <project ...>
 * <taskdef name="api" classname="APITask" /> <property name="dest.dir"
 * value="${basedir}/gen-src/api"/> <target name="generate"> <mkdir
 * dir="${dest.dir}" /> <api outputdir="${dest.dir}"> <fileset dir="${api.dir}">
 * <include name="*.java" /> </fileset> </dsl> </target> </project>
 * 
 * @DEV.TODO refactor out AbstractTask
 */
public class APITask extends Task {

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

        Set interfaces = new HashSet();
        JavaDocBuilder builder = new JavaDocBuilder();

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
                    try {
                        FileReader r = new FileReader(file);
                        builder.addSource(r);
                    } catch (FileNotFoundException fnfe) {
                        log("FileReader " + file + " not created. Skippin.");
                    }
                }
            }
        }

        interfaces.addAll(extract(builder));

        for (Iterator it = interfaces.iterator(); it.hasNext();) {
            JavaClass jclass = (JavaClass) it.next();
            VelocityHelper vh = new VelocityHelper();
            vh.put("api", jclass);
            try {
                FileWriter fw = new FileWriter(_outputDir + File.separator
                        + jclass.getFullyQualifiedName().replaceAll("[.]", "_")
                        + ".ice");
                vh.invoke("ome/dsl/api.vm", fw);
                fw.flush();
                fw.close();
            } catch (Exception e) {
                throw new BuildException("Error while writing type:"
                        + jclass.getFullyQualifiedName(), e);
            }
        }

    }

    List extract(JavaDocBuilder builder) {
        List list = new ArrayList();

        JavaSource[] sources = builder.getSources();

        for (int i = 0; i < sources.length; i++) {
            JavaSource src = sources[i];
            JavaClass[] classes = src.getClasses();

            for (int j = 0; j < classes.length; j++) {
                JavaClass cls = classes[j];
                if (!cls.isInterface()) {
                    continue;
                }
                if (!cls.isPublic()) {
                    continue;
                }

                DocletTag iceTag = cls.getTagByName("ICE");
                // if (null != iceTag)
                list.add(cls);

            }
        }
        return list;

    }

}
