/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_CONSTANTS_ICE
#define OMERO_CONSTANTS_ICE

module omero {
  module constants {

    /*
     * Key in the ImplicitContext which must be filled
     * by all omero.client implementations.
     */
    const string CLIENTUUID = "omero.client.uuid";

    /*
     * Server-side names used for each of the services
     * defined in API.ice
     */
    const string ADMINSERVICE     = "omero.api.IAdmin";
    const string ANALYSISSERVICE  = "omero.api.IAnalysis";
    const string CONFIGSERVICE    = "omero.api.IConfig";
    const string GATEWAYSERVICE   = "omero.api.Gateway";
    const string DELETESERVICE    = "omero.api.IDelete";
    const string LDAPSERVICE      = "omero.api.ILdap";
    const string PIXELSSERVICE    = "omero.api.IPixels";
    const string POJOSSERVICE     = "omero.api.IPojos";
    const string PROJECTIONSERVICE= "omero.api.IProjection";
    const string QUERYSERVICE     = "omero.api.IQuery";
    const string SESSIONSERVICE   = "omero.api.ISession";
    const string SHARESERVICE     = "omero.api.IShare";
    const string TYPESSERVICE     = "omero.api.ITypes";
    const string UPDATESERVICE    = "omero.api.IUpdate";
    const string JOBHANDLE        = "omero.api.JobHandle";
    const string RAWFILESTORE     = "omero.api.RawFileStore";
    const string RAWPIXELSSTORE   = "omero.api.RawPixelsStore";
    const string RENDERINGENGINE  = "omero.api.RenderingEngine";
    const string SCRIPTSERVICE    = "omero.api.IScript";
    const string SEARCH           = "omero.api.Search";
    const string THUMBNAILSTORE   = "omero.api.ThumbnailStore";
    const string REPOSITORYINFO   = "omero.api.IRepositoryInfo";
    const string RENDERINGSETTINGS= "omero.api.IRenderingSettings";

    // User context for logging in
    const string USERNAME = "omero.user";
    const string PASSWORD = "omero.pass";
    const string GROUP    = "omero.group";
    const string EVENT    = "omero.event";
    const string UMASK    = "omero.umask";

    /*
     * Strings used by the Java class ome.util.builders.PojoOptions
     * to create Map options for IPojos methods. omero::api::IPojos
     * takes omero::sys::ParamMaps instead.
     *
     * See System.ice and RTypes.ice.
     */
    const string POJOLEAVES       = "leaves";         // omero::RBool (whether or not Images returned)
    const string POJOEXPERIMENTER = "experimenter";   // omero::RLong
    const string POJOGROUP        = "group";          // omero::RLong
    const string POJOLIMIT        = "limit";          // omero::RInt
    const string POJOOFFSET       = "offset";         // omero::RInt
    const string POJOSTARTTIME    = "startTime";      // omero::RTime
    const string POJOENDTIME      = "endTime";        // omero::RTime
    /*
     * Used by IPojos.findCGCPaths(...,string algo,...)
     */
    const string CLASSIFICATIONME  = "CLASSIFICATION_ME";
    const string CLASSIFICATIONNME = "CLASSIFICATION_NME";
    const string DECLASSIFICATION  = "DECLASSIFICATION";

    module jobs {

      /*
       * Used by JobHandle as the status of jobs
       */
      const string SUBMITTED = "Submitted";
      const string RESUBMITTED = "Resubmitted";
      const string QUEUED = "Queued";
      const string REQUEUED = "Requeued";
      const string RUNNING = "Running";
      const string ERROR = "Error";
      const string WAITING = "Waiting";
      const string FINISHED = "Finished";
      const string CANCELLED = "Cancelled";

    };
    
    module projection {
      /*
       * Methodology strings
       */
      const string MAXIMUMINTENSITYMETHODOLOGY = "MAXIMUM_INTENSITY_PROJECTION";
      const string MEANINTENSITYMETHODOLOGY = "MEAN_INTENSITY_PROJECTION";
      const string SUMINTENSITYMETHODOLOGY = "SUM_INTENSITY_PROJECTION";

      /*
       * Used by the IProjection methods to declare which projection to perform.
       */
      enum ProjectionType {
        MAXIMUMINTENSITY,
        MEANINTENSITY,
        SUMINTENSITY
      };
    };
  };
};

#endif
