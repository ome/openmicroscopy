#!/usr/bin/env python
"""

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import Ice
import omero
if omero.__import_style__ is None:
    omero.__import_style__ = "all"
    import omero.rtypes
    import omero_System_ice
    Ice.updateModules()
    import omero_Collections_ice
    Ice.updateModules()
    import omero_ServicesF_ice
    Ice.updateModules()
    import omero_API_ice
    Ice.updateModules()
    import omero_Repositories_ice
    Ice.updateModules()
    import omero_SharedResources_ice
    Ice.updateModules()
    import omero_Scripts_ice
    Ice.updateModules()
    import omero_Tables_ice
    Ice.updateModules()
    import omero_api_Gateway_ice
    Ice.updateModules()
    import omero_api_IAdmin_ice
    Ice.updateModules()
    import omero_api_IConfig_ice
    Ice.updateModules()
    import omero_api_IContainer_ice
    Ice.updateModules()
    import omero_api_IDelete_ice
    Ice.updateModules()
    import omero_api_ILdap_ice
    Ice.updateModules()
    import omero_api_IMetadata_ice
    Ice.updateModules()
    import omero_api_IPixels_ice
    Ice.updateModules()
    import omero_api_IProjection_ice
    Ice.updateModules()
    import omero_api_IQuery_ice
    Ice.updateModules()
    import omero_api_IRenderingSettings_ice
    Ice.updateModules()
    import omero_api_IRepositoryInfo_ice
    Ice.updateModules()
    import omero_api_IRoi_ice
    Ice.updateModules()
    import omero_api_IScript_ice
    Ice.updateModules()
    import omero_api_ISession_ice
    Ice.updateModules()
    import omero_api_IShare_ice
    Ice.updateModules()
    import omero_api_ITimeline_ice
    Ice.updateModules()
    import omero_api_ITypes_ice
    Ice.updateModules()
    import omero_api_IUpdate_ice
    Ice.updateModules()
    import omero_api_Exporter_ice
    Ice.updateModules()
    import omero_api_JobHandle_ice
    Ice.updateModules()
    import omero_api_MetadataStore_ice
    Ice.updateModules()
    import omero_api_RawFileStore_ice
    Ice.updateModules()
    import omero_api_RawPixelsStore_ice
    Ice.updateModules()
    import omero_api_RenderingEngine_ice
    Ice.updateModules()
    import omero_api_Search_ice
    Ice.updateModules()
    import omero_api_ThumbnailStore_ice
    Ice.updateModules()
    import omero_Constants_ice
    Ice.updateModules()
    import omero_sys_ParametersI
    Ice.updateModules()
    import omero_model_PermissionsI
    Ice.updateModules()
