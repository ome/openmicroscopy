#!/usr/bin/env python
# -*- coding: utf-8 -*-
import IceImport
IceImport.load("omero_FS_ice")

#
# Copied from:
# blitz/src/ome/formats/importer/transfers/AbstractFileTransfer.java
#
TRANSFERS = {
    "ome.formats.importer.transfers.CopyFileTransfer2": "cp",
    "ome.formats.importer.transfers.CopyMoveFileTransfer": "cp_rm",
    "ome.formats.importer.transfers.HardlinkFileTransfer2": "ln",
    "ome.formats.importer.transfers.MoveFileTransfer": "ln_rm",
    "ome.formats.importer.transfers.SymlinkFileTransfer2": "ln_s",
    "ome.formats.importer.transfers.UploadRmFileTransfer": "upload_rm",
    "ome.formats.importer.transfers.UploadFileTransfer": "",
    }
