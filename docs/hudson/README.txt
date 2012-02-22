This directory contains scripts which are used by
hudson.openmicroscopy.org.uk to build the OMERO
products. Execution is as follows:

 * The OMERO-<BRANCHNAME> job polls subversion
   (usually every 5 minutes) to detect new commits
   to either trunk if <BRANCHNAME> is trunk or
   branches/<BRANCHNAME> otherwise.

 * If there is one or more new commits, then the job executes
   by checking out subversion, changing to the docs/hudson
   directory and running: sh OMERO.sh

 * If the OMERO-<BRANCHNAME> execution is successful, then
   OMERO-<BRACHNAME>-components is launched.

 * First, a controlling job is launched on the hudson master.
   This job is otherwise uninteresting.

 * Second, the OMERO-<BRANCHNAME>-start jobs are launched.
   These start a server and create the <BRANCHNAME>.config
   file which all other component jobs download. (The web
   server is not started. That is handled by OMERO-web.*)

 * If all OMERO-<BRANCHNAME>-start jobs completed successfully,
   then all other component jobs are started which connect
   to the server of the same label. This server is found by
   downloading a configuration file from the start job of the
   form "<BRANCHNAME>.config" (This is done by launcher.py).
   The ICE_CONFIG environment variable is set to point at the
   absolute location of this file. Other variables which are
   set for jobs include:

    o OMERO_HOST   - the host where the OMERO server is running,
                     not necessarily localhost)

    o OMERO_PREFIX - the prefix of the ports to use
                     (e.g. 1 for 14064)
