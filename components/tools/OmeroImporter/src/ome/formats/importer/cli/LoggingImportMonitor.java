package ome.formats.importer.cli;

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Basic import process monitor that writes information to the log.
 * 
 * @author Chris Allan <callan@glencoesoftware.com>
 *
 */
public class LoggingImportMonitor implements IObserver
{
    private static Log log = LogFactory.getLog(LoggingImportMonitor.class);
    
    public void update(IObservable importLibrary, ImportEvent event)
    {
        log.info(event.toLog());
    }
}
