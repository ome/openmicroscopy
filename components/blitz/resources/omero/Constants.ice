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
     * The names of the OmeroContext instances 
     * defined in beanRefContext.xml files. Only useful
     * in the Java mappings.
     */
    const string SERVERCONTEXT = "OMERO.server";
    const string CLIENTCONTEXT = "OMERO.client";

    /*
     * Server-side names used for each of the services
     * defined in API.ice
     */
    const string ADMINSERVICE     = "omero.api.IAdmin";
    const string ANALYSISSERVICE  = "omero.api.IAnalysis";
    const string CONFIGSERVICE    = "omero.api.IConfig";
    const string DELETESERVICE    = "omero.api.IDelete";
    const string LDAPSERVICE      = "omero.api.ILdap";
    const string PIXELSSERVICE    = "omero.api.IPixels";
    const string POJOSSERVICE     = "omero.api.IPojos";
    const string QUERYSERVICE     = "omero.api.IQuery";
    const string TYPESSERVICE     = "omero.api.ITypes";
    const string UPDATESERVICE    = "omero.api.IUpdate";
    const string RAWFILESTORE     = "omero.api.RawFileStore";
    const string RAWPIXELSSTORE   = "omero.api.RawPixelsStore";
    const string RENDERINGENGINE  = "omero.api.RenderingEngine";
    const string THUMBNAILSTORE   = "omero.api.ThumbnailStore";
    const string REPOSITORYINFO   = "omero.api.IRepositoryInfo";

    // User context for logging in
    const string USERNAME = "OMERO.user";
    const string PASSWORD = "OMERO.pass";
    const string GROUP    = "OMERO.group";
    const string EVENT    = "OMERO.event";
    const string UMASK    = "OMERO.umask"; 

    /*
     * Strings used by the Java class ome.util.builders.PojoOptions
     * to create Map options for IPojos methods. omero::api::IPojos
     * takes omero::sys::ParamMaps instead. 
     * 
     * See System.ice and RTypes.ice.
     */
    const string POJOFIELDS       = "fields";         // omero::RList<omero::RString> (names of fields) UNSUPPORTED
    const string POJOCOUNTS       = "counts";         // omero::RLong (user id)
    const string POJOLEAVES       = "leaves";         // omero::RBool (whether or not Images returned)
    const string POJOEXPERIMENTER = "experimenter";   // omero::RLong
    const string POJOGROUP        = "group";          // omero::RLong

    /*
     * Used by IPojos.findCGCPaths(...,string algo,...) 
     */
    const string CLASSIFICATIONME  =  "CLASSIFICATION_ME";
    const string CLASSIFICATIONNME = "CLASSIFICATION_NME";
    const string DECLASSIFICATION  = "DECLASSIFICATION";

  };
};

#endif 
