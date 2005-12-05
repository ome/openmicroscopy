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

    public void testAPITask()
    {

        final class T extends APITask
        {

            public T()
            {

                DefaultLogger logger = new DefaultLogger();
                logger.setMessageOutputLevel(Project.MSG_DEBUG);
                logger.setErrorPrintStream(System.err);
                logger.setOutputPrintStream(System.out);

                setProject(new Project());
                getProject().setBasedir(
                        "/home/josh/code/omero/components/common/");
                getProject().addBuildListener(logger);
                getProject().init();
                setTaskName("api");
                setTaskType("api");
                setOwningTarget(new Target());
            }
        }
        
        T task = new T();
        FileSet fileSet = new FileSet();
        fileSet.setDir(new File("/home/josh/code/omero/components/common/"));
        NameEntry entry = fileSet.createInclude();
        entry.setName("**/api/*.java");
        task.addFileset(fileSet);
        task.setDestdir(new File("/tmp"));
        task.execute();
    
    }

}
