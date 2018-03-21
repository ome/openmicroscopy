
# From http://www.openmicroscopy.org/community/viewtopic.php?f=6&t=8407
# Uses code from https://github.com/openmicroscopy/openmicroscopy/blob/develop/components/tools/OmeroPy/src/omero/testlib/__init__.py

import omero
import platform
import os
from omero.model import ChecksumAlgorithmI
# from omero.model import NamedValue
from omero.model.enums import ChecksumAlgorithmSHA1160
from omero.rtypes import rstring, rbool
from omero_version import omero_version
from omero.callbacks import CmdCallbackI
from omero.gateway import BlitzGateway


def create_fileset(folder_path):
    fileset = omero.model.FilesetI()
    # for f in folder.files():
    for f in os.listdir(folder_path):
        if f.startswith('.'):
            continue
        entry = omero.model.FilesetEntryI()
        # entry.setClientPath(rstring(str(f.abspath())))
        entry.setClientPath(rstring(os.path.join(folder_path, f)))
        fileset.addFilesetEntry(entry)

    # Fill version info
    system, node, release, version, machine, processor = platform.uname()

    # client_version_info = [
    #     NamedValue('omero.version', omero_version),
    #     NamedValue('os.name', system),
    #     NamedValue('os.version', release),
    #     NamedValue('os.architecture', machine)
    # ]
    # try:
    #     client_version_info.append(
    #         NamedValue('locale', locale.getdefaultlocale()[0]))
    # except:
    #     pass

    upload = omero.model.UploadJobI()
    # upload.setVersionInfo(client_version_info)
    fileset.linkJob(upload)
    return fileset


def create_settings():
    settings = omero.grid.ImportSettings()
    settings.doThumbnails = rbool(True)
    settings.noStatsInfo = rbool(False)
    settings.userSpecifiedTarget = None
    settings.userSpecifiedName = None
    settings.userSpecifiedDescription = None
    settings.userSpecifiedAnnotationList = None
    settings.userSpecifiedPixels = None
    settings.checksumAlgorithm = ChecksumAlgorithmI()
    s = rstring(ChecksumAlgorithmSHA1160)
    settings.checksumAlgorithm.value = s
    return settings


def upload_folder(proc, folder_path, client):
    ret_val = []
    # for i, fobj in enumerate(folder.files()):  # Assuming same order
    i = 0
    for f in os.listdir(folder_path):
        if f.startswith('.'):
            continue
        rfs = proc.getUploader(i)
        i += 1
        try:
            # f = fobj.open()
            abspath = os.path.join(folder_path, f)
            f = open(abspath)
            try:
                offset = 0
                block = []
                rfs.write(block, offset, len(block))  # Touch
                while True:
                    block = f.read(1000 * 1000)
                    if not block:
                        break
                    rfs.write(block, offset, len(block))
                    offset += len(block)
                ret_val.append(client.sha1(abspath))
            finally:
                f.close()
        finally:
            rfs.close()
    return ret_val


def assert_passes(cb, loops=10, wait=500):
    cb.loop(loops, wait)
    rsp = cb.getResponse()
    if isinstance(rsp, omero.cmd.ERR):
        raise Exception(rsp)
    return rsp


def assert_import(client, proc, folder):
    hashes = upload_folder(proc, folder, client)
    handle = proc.verifyUpload(hashes)
    cb = CmdCallbackI(client, handle)
    rsp = assert_passes(cb)
    assert len(rsp.pixels) > 0
    return rsp


def full_import(client, folder_path):
    """
    Re-usable method for a basic import
    """
    mrepo = client.getManagedRepository()
    fileset = create_fileset(folder_path)
    settings = create_settings()

    proc = mrepo.importFileset(fileset, settings)
    try:
        return assert_import(client, proc, folder_path)
    finally:
        proc.close()
