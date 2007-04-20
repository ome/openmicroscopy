/*
 *   $Id$
 * 
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_CONSTANTS_ICE
#define OMERO_CONSTANTS_ICE

module omero { 
  module constants {     

    // Spring names
    const string SERVERCONTEXT = "OMERO.server";
    const string CLIENTCONTEXT = "OMERO.client";

    // Service names
    const string ADMINSERVICE     = "ome.api.IAdmin";
    const string ANALYSISSERVICE  = "ome.api.IAnalysis";
    const string CONFIGSERVICE    = "ome.api.IConfig";
    const string DELETESERVICE    = "ome.api.IDelete";
    const string PIXELSSERVICE    = "ome.api.IPixels";    
    const string POJOSSERVICE     = "ome.api.IPojos";    
    const string QUERYSERVICE     = "ome.api.IQuery";
    const string TYPESSERVICE     = "ome.api.ITypes";
    const string UPDATESERVICE    = "ome.api.IUpdate";    
    const string RAWFILESTORE     = "ome.api.RawFileStore";
    const string RAWPIXELSSTORE   = "ome.api.RawPixelsStore";
    const string RENDERINGENGINE  = "omeis.providers.re.RenderingEngine";
    const string THUMBNAILSTORE   = "ome.api.ThumbnailStore";
    const string REPOSITORYINFO   = "ome.api.IRepositoryInfo";
	
    // User context
    const string USERNAME = "omero.sys.username";
    const string PASSWORD = "omero.sys.password";
    const string GROUP    = "omero.sys.group";
    const string EVENT    = "omero.sys.event";
    const string UMASK    = "omero.sys.umask"; 

    // Other
    const string CLASSIFICATIONME =  "CLASSIFICATION_ME";
    const string CLASSIFICATIONNME = "CLASSIFICATION_NME";
    const string DECLASSIFICATION = "DECLASSIFICATION";

  };
};

#endif 
