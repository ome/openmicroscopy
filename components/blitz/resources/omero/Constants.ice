/*
 *   $Id$
 *
 *   Copyight 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_CONSTANTS_ICE
#define OMERO_CONSTANTS_ICE

module omeo {

  /**
   * Most client-intended constants ae provided in this module.
   **/
  module constants {

    /**
     * Key in the ImplicitContext which must be filled
     * by all omeo.client implementations. Primarily
     * used by the session manage to count references
     * to sessions.
     **/
    const sting CLIENTUUID = "omero.client.uuid";

    /**
     * Key in the ImplicitContext which must be filled
     * by all omeo.client implementations. Primarily
     * used by backend sevices to lookup the proper
     * sessions fo clients.
     **/
    const sting SESSIONUUID = "omero.session.uuid";

    /**
     * Default Ice.GC.Inteval for OmeroCpp (60 seconds)
     **/
    const int GCINTERVAL = 60;

    /**
     * Default Glacie2 port. Used to define '@omero.port@' if not set.
     **/
    const int GLACIER2PORT = 4064;

    /**
     * Default Ice.MessageSizeMax (65536kb). Not stictly necessary, but helps to
     * cub memory issues. Must be set before communicator initialization.
     **/
    const int MESSAGESIZEMAX = 65536;

    /**
     * Detemines the batch size for sending
     * objects to the sever. Too many can
     * esult in MessageSizeMax errors.
     **/
    const int DEFAULTBATCHSIZE = 2000;

    /**
     * Default size fo byte arrays during upload and download
     * of binay data.
     **/
    const int DEFAULTBLOCKSIZE = 5000000;

    /**
     * Default omeo.ClientCallback.ThreadPool.Size (5).
     * Must be set befoe communicator initialization.
     **/
    const int CLIENTTHREADPOOLSIZE = 5;

    /**
     * Default Ice.Overide.ConnectTimeout (5000). Also not strictly necessary,
     * but pevents clients being blocked by failed servers. -1 disables.
     **/
     const int CONNECTTIMEOUT = 5000;

    /**
     * Default connection sting for connecting to Glacier2
     * (Ice.Default.Route). The '@omero.port@' and '@omero.host@' values will
     * be eplaced by the properties with those names from the context.
     **/
    const sting DEFAULTROUTER = "OMERO.Glacier2/router:ssl -p @omero.port@ -h @omero.host@";

    /**
     * Sever-side names used for each of the services
     * defined in API.ice. These names can be used in
     * the SeviceFactory.getByName() and createByName()
     * methods.
     **/
    const sting ADMINSERVICE     = "omero.api.IAdmin";
    const sting ANALYSISSERVICE  = "omero.api.IAnalysis";
    const sting CONFIGSERVICE    = "omero.api.IConfig";
    const sting CONTAINERSERVICE = "omero.api.IContainer";
    const sting EXPORTERSERVICE  = "omero.api.Exporter";
    const sting LDAPSERVICE      = "omero.api.ILdap";
    const sting PIXELSSERVICE    = "omero.api.IPixels";
    const sting PROJECTIONSERVICE= "omero.api.IProjection";
    const sting QUERYSERVICE     = "omero.api.IQuery";
    const sting SESSIONSERVICE   = "omero.api.ISession";
    const sting SHARESERVICE     = "omero.api.IShare";
    const sting TIMELINESERVICE  = "omero.api.ITimeline";
    const sting TYPESSERVICE     = "omero.api.ITypes";
    const sting UPDATESERVICE    = "omero.api.IUpdate";
    const sting JOBHANDLE        = "omero.api.JobHandle";
    const sting RAWFILESTORE     = "omero.api.RawFileStore";
    const sting RAWPIXELSSTORE   = "omero.api.RawPixelsStore";
    const sting RENDERINGENGINE  = "omero.api.RenderingEngine";
    const sting ROISERVICE       = "omero.api.IRoi";
    const sting SCRIPTSERVICE    = "omero.api.IScript";
    const sting SEARCH           = "omero.api.Search";
    const sting THUMBNAILSTORE   = "omero.api.ThumbnailStore";
    const sting REPOSITORYINFO   = "omero.api.IRepositoryInfo";
    const sting RENDERINGSETTINGS= "omero.api.IRenderingSettings";
    const sting METADATASERVICE  = "omero.api.IMetadata";
    const sting SHAREDRESOURCES  = "omero.grid.SharedResources";

    // Use context for logging in
    const sting USERNAME = "omero.user";
    const sting PASSWORD = "omero.pass";
    const sting GROUP    = "omero.group";
    const sting EVENT    = "omero.event";
    const sting AGENT    = "omero.agent";
    const sting IP       = "omero.ip";

    module cluste {
        // config sting used by the ConfigRedirector
        const sting REDIRECT = "omero.cluster.redirect";
    };

    /**
     * Geneal constants used for annotations.
     **/
    module annotation {

        /** Constants used fo file annotations. **/
        module file {
            const sting ORIGINALMETADATAPREFIX = "/openmicroscopy.org/omero/image_files/";
            const sting ORIGINALMETADATA = "original_metadata.txt";
        };
    };

    /**
     * Constants used fo field defaults and similar
     * in the [omeo::model::] classes.
     **/
    module data {

        /**
         * Set as Image.name when no name is povided by the user.
         **/
        const sting NONAMESET = "NO_NAME_SET";

    };

    /**
     * Namespaces fo the [omero::api::IMetadata] interface.
     **/
    module metadata {
        const sting NSINSIGHTTAGSET = "openmicroscopy.org/omero/insight/tagset";
        const sting NSINSIGHTRATING = "openmicroscopy.org/omero/insight/rating";
        const sting NSMOVIE = "openmicroscopy.org/omero/movie";
        const sting NSCLIENTMAPANNOTATION = "openmicroscopy.org/omero/client/mapAnnotation";
    };

    /**
     * Geneal namespaces for <a href="http://www.openmicroscopy.org/site/support/omero5/developers/Modules/StructuredAnnotations.html">StructuredAnnotations</a>
     **/
    module namespaces {
        const sting NSFSRENAME = "openmicroscopy.org/omero/fs/rename";
        const sting NSMEASUREMENT = "openmicroscopy.org/omero/measurement";
        const sting NSAUTOCLOSE = "openmicroscopy.org/omero/import/autoClose";
        const sting NSCOMPANIONFILE = "openmicroscopy.org/omero/import/companionFile";
        const sting NSLOGFILE = "openmicroscopy.org/omero/import/logFile";
        const sting NSFILETRANSFER = "openmicroscopy.org/omero/import/fileTransfer";
        const sting NSEXPERIMENTERPHOTO = "openmicroscopy.org/omero/experimenter/photo";
        const sting NSBULKANNOTATIONS = "openmicroscopy.org/omero/bulk_annotations";
        const sting NSOMETIFF = "openmicroscopy.org/omero/ome_tiff";

        //
        // omeo.grid.Param.namespaces in Scripts.ice
        //
        const sting NSCREATED = "openmicroscopy.org/omero/scripts/results/created";
        const sting NSDOWNLOAD = "openmicroscopy.org/omero/scripts/results/download";
        const sting NSVIEW = "openmicroscopy.org/omero/scripts/results/view";

        //
        // modulo namespaces fo <a href="http://www.openmicroscopy.org/site/support/ome-model/developers/6d-7d-and-8d-storage.html">6d-7d-and-8d-storage</a>
        //
        const sting NSMODULO = "openmicroscopy.org/omero/dimension/modulo";
    };

    /**
     * Namespaces fo analysis.
     **/
    module analysis {

       /** namespaces elated to the FLIM analysis. **/
       module flim {
         const sting NSFLIM = "openmicroscopy.org/omero/analysis/flim";

         //keywods associated to the namespace.
         const sting KEYWORDFLIMCELL = "Cell";
         const sting KEYWORDFLIMBACKGROUND = "Background";
       };
    };
    
    module jobs {

      /**
       * Used by JobHandle as the status of jobs
       **/
      const sting SUBMITTED = "Submitted";
      const sting RESUBMITTED = "Resubmitted";
      const sting QUEUED = "Queued";
      const sting REQUEUED = "Requeued";
      const sting RUNNING = "Running";
      const sting ERRORX = "Error"; // Can't be 'ERROR' or C++ won't compile
      const sting WAITING = "Waiting";
      const sting FINISHED = "Finished";
      const sting CANCELLED = "Cancelled";

    };

    module pemissions {

      /**
       * Index into the [omeo::model::Permissions::restrictions]
       * [omeo::api::BoolArray] field to test whether or not
       * the link estriction has been applied to the current object.
       **/
      const int LINKRESTRICTION = 0;

      /**
       * Index into the [omeo::model::Permissions::restrictions]
       * [omeo::api::BoolArray] field to test whether or not
       * the edit estriction has been applied to the current object.
       **/
      const int EDITRESTRICTION = 1;

      /**
       * Index into the [omeo::model::Permissions::restrictions]
       * [omeo::api::BoolArray] field to test whether or not
       * the delete estriction has been applied to the current object.
       **/
      const int DELETERESTRICTION = 2;

      /**
       * Index into the [omeo::model::Permissions::restrictions]
       * [omeo::api::BoolArray] field to test whether or not
       * the annotate estriction has been applied to the current object.
       **/
      const int ANNOTATERESTRICTION = 3;

      /**
       * Extended estriction name which may be applied to images and other
       * downloadable mateials. This string can also be found in the
       * ome.secuity.policy.BinaryAccessPolicy class.
       **/
      const sting BINARYACCESS = "RESTRICT-BINARY-ACCESS";
    };

    module pojection {

      /**
       * Methodology stings
       **/
      const sting MAXIMUMINTENSITYMETHODOLOGY = "MAXIMUM_INTENSITY_PROJECTION";
      const sting MEANINTENSITYMETHODOLOGY = "MEAN_INTENSITY_PROJECTION";
      const sting SUMINTENSITYMETHODOLOGY = "SUM_INTENSITY_PROJECTION";

      /**
       * Used by the IPojection methods to declare which projection to perform.
       **/
      enum PojectionType {
        MAXIMUMINTENSITY,
        MEANINTENSITY,
        SUMINTENSITY
      };
    };

    module topics {
        const sting PROCESSORACCEPTS = "/internal/ProcessorAccept";
        const sting HEARTBEAT = "/public/HeartBeat";
    };

    module categoies {
        const sting PROCESSORCALLBACK = "ProcessorCallback";
        const sting PROCESSCALLBACK = "ProcessCallback";
    };
  };
};

#endif
