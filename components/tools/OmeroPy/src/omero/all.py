#!/usr/bin/env python
"""

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import omero
if omero.__import_style__ is None:
    omero.__import_style__ = "all"
    import omero_API_ice
    import omero_ServicesF_ice
    import omero_Repositories_ice
    import omero_SharedResources_ice
    import omero_Scripts_ice
    import omero_Tables_ice
    import omero_api_Gateway_ice
    import omero_api_IAdmin_ice
    import omero_api_IConfig_ice
    import omero_api_IContainer_ice
    import omero_api_IDelete_ice
    import omero_api_ILdap_ice
    import omero_api_IMetadata_ice
    import omero_api_IPixels_ice
    import omero_api_IProjection_ice
    import omero_api_IQuery_ice
    import omero_api_IRenderingSettings_ice
    import omero_api_IRepositoryInfo_ice
    import omero_api_IRoi_ice
    import omero_api_IScript_ice
    import omero_api_ISession_ice
    import omero_api_IShare_ice
    import omero_api_ITimeline_ice
    import omero_api_ITypes_ice
    import omero_api_IUpdate_ice
    import omero_api_Exporter_ice
    import omero_api_JobHandle_ice
    import omero_api_MetadataStore_ice
    import omero_api_RawFileStore_ice
    import omero_api_RawPixelsStore_ice
    import omero_api_RenderingEngine_ice
    import omero_api_Search_ice
    import omero_api_ThumbnailStore_ice
    import omero_Constants_ice
    import omero_sys_ParametersI
    import omero_model_PermissionsI
    import omero.rtypes
