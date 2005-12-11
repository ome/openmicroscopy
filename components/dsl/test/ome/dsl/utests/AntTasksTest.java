package ome.dsl.utests;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.PatternSet.NameEntry;

import ome.dsl.APITask;
import junit.framework.TestCase;

public class AntTasksTest extends TestCase
{

    private static Log log = LogFactory.getLog(AntTasksTest.class);

    public void testAPITask() {}
    /** disabling; need logic to find common/ 
     * perhaps OMERO_HOME has to be set. FIXME
     */
    public void DISABLEDtestAPITask()
    {

        final File currentDir = new File(System.getProperty("user.dir"));

        final File commonDir = new File(currentDir.getParentFile().getPath()
                + File.separator + "common");

        final class T extends APITask
        {

            public T()
            {

                DefaultLogger logger = new DefaultLogger();
                logger.setMessageOutputLevel(Project.MSG_DEBUG);
                logger.setErrorPrintStream(System.err);
                logger.setOutputPrintStream(System.out);

                setProject(new Project());
                getProject().setBasedir(commonDir.getPath());
                getProject().addBuildListener(logger);
                getProject().init();
                setTaskName("api");
                setTaskType("api");
                setOwningTarget(new Target());
            }
        }

        T task = new T();
        FileSet fileSet = new FileSet();
        fileSet.setDir(commonDir);
        NameEntry entry = fileSet.createInclude();
        entry.setName("**/api/*.java");
        task.addFileset(fileSet);
        task.setDestdir(new File("/tmp"));
        task.execute();

    }

}
