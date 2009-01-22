package ome.formats.importer.cli;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.util.Actions;
import omero.model.Dataset;

/**
 * Basic import process monitor that writes information to the log.
 * 
 * @author Chris Allan <callan@glencoesoftware.com>
 *
 */
public class LoggingImportMonitor implements IObserver
{
    private static Log log = LogFactory.getLog(LoggingImportMonitor.class);
    
    public void update(IObservable importLibrary, Object message, Object[] args)
    {

        if (message.equals(Actions.IMPORT_STEP))
        {
            /*
            args2[] = {series, step, reader.getSeriesCount()};
             */
            String s = String.format(
                    "Message: %s Image: %d Series: %d Total Series: %d",
                    message,
                    (Integer) args[1],
                    (Integer) args[0],
                    (Integer) args[2]);
            log.info(s);
        }
        else
        {
            /*
            (String)  args[0] = shortName;   (Dataset) args[4] = getDataset();
            (Integer) args[1] = index;       (Long) args[5] = pixId;
            (Integer) args[2] = numDone;     (Integer) args[6] = count;
            (Integer) args[3] = total;       (Integer) args[7] = series;
             */
            String s = String.format(
                    "Message: %s Name: %s Index: %d nDone: %d Total: %d " +
                    "Dataset: %s Pixels Id: %d Image Count: %d Series: %d",
                    message,
                    (String) args[0],
                    (Integer) args[1],
                    (Integer) args[2],
                    (Integer) args[3],
                    (Dataset) args[4],
                    (Long) args[5],
                    (Integer) args[6],
                    (Integer) args[7]);
            log.info(s);
        }
    }
}
