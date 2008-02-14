/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_SCRIPTS_ICE
#define OMERO_SCRIPTS_ICE

#include <omero/model/OriginalFile.ice>
#include <omero/model/ScriptJob.ice>

/*
 * The Processor API is intended to provide an Ice-implementation
 * of the ome.services.procs.Processor interface.
 */
module omero {
    module grid {

        interface ScriptProcessor;
        interface ScriptProcessCallback;
        interface InteractiveProcess;

        interface ScriptProcess {
            ScriptProcessor* getProcessor();
            bool isActive();
            void finish();
            void cancel();
            void registerCallback(ScriptProcessCallback* cb);
            void unregisterCallback(ScriptProcessCallback* cb);
        };

        interface ScriptProcessCallback {
            void processFinished();
            void processCancelled();
        };

        interface ScriptProcessor {

            /*
             * Starts a process based on the given job id. If
             * this processor cannot handle the given job, a
             * null process will be returned.
             */
            ScriptProcess* processScriptJob(omero::model::ScriptJob job);

            /*
             * Tries to acquire an interactive process on
             * this processor. Waits given number of seconds
             * before giving up.
             */
             InteractiveProcess* processInteractiveJob(omero::model::ScriptJob job, int seconds);

        };

        interface InteractiveProcess extends ScriptProcess {

            long expires();
            omero::model::OriginalFile getScript();
            omero::model::ScriptJob getJob();
            ["ami"] omero::RMap execute(omero::RMap inputs);

        };
    };
};

#endif
