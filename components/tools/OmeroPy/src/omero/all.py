#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""

   Copyright 2010-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

# This file is an import-only file providing a mechanism for other files to
# import a range of modules in a controlled way. It could be made to pass
# flake8 but given its simplicity it is being marked as noqa for now.
#
# flake8: noqa

import Ice
import IceImport
import omero
if omero.__import_style__ is None:
    omero.__import_style__ = "all"
    import omero.min
    import omero.callbacks
    import omero.ObjectFactoryRegistrar
    IceImport.load("omero_FS_ice")
    IceImport.load("omero_System_ice")
    IceImport.load("omero_Collections_ice")
    IceImport.load("omero_Repositories_ice")
    IceImport.load("omero_SharedResources_ice")
    IceImport.load("omero_Scripts_ice")
    IceImport.load("omero_Tables_ice")
    IceImport.load("omero_api_IAdmin_ice")
    IceImport.load("omero_api_IConfig_ice")
    IceImport.load("omero_api_IContainer_ice")
    IceImport.load("omero_api_ILdap_ice")
    IceImport.load("omero_api_IMetadata_ice")
    IceImport.load("omero_api_IPixels_ice")
    IceImport.load("omero_api_IProjection_ice")
    IceImport.load("omero_api_IQuery_ice")
    IceImport.load("omero_api_IRenderingSettings_ice")
    IceImport.load("omero_api_IRepositoryInfo_ice")
    IceImport.load("omero_api_IRoi_ice")
    IceImport.load("omero_api_IScript_ice")
    IceImport.load("omero_api_ISession_ice")
    IceImport.load("omero_api_IShare_ice")
    IceImport.load("omero_api_ITimeline_ice")
    IceImport.load("omero_api_ITypes_ice")
    IceImport.load("omero_api_IUpdate_ice")
    IceImport.load("omero_api_Exporter_ice")
    IceImport.load("omero_api_JobHandle_ice")
    IceImport.load("omero_api_MetadataStore_ice")
    IceImport.load("omero_api_RawFileStore_ice")
    IceImport.load("omero_api_RawPixelsStore_ice")
    IceImport.load("omero_api_RenderingEngine_ice")
    IceImport.load("omero_api_Search_ice")
    IceImport.load("omero_api_ThumbnailStore_ice")
    IceImport.load("omero_cmd_Admin_ice")
    IceImport.load("omero_cmd_API_ice")
    IceImport.load("omero_cmd_Basic_ice")
    IceImport.load("omero_cmd_FS_ice")
    IceImport.load("omero_cmd_Graphs_ice")
    IceImport.load("omero_cmd_Mail_ice")
    IceImport.load("omero_model_Units_ice")
    import omero_sys_ParametersI
    import omero_model_PermissionsI
