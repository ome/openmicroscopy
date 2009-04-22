#
# webgateway/testdb_destroy - test db fixture removal
# 
# Copyright (c) 2008, 2009 Glencoe Software, Inc. All rights reserved.
# 
# This software is distributed under the terms described by the LICENCE file
# you can find at the root of the distribution bundle, which states you are
# free to use it only for non commercial purposes.
# If the file is missing please request a copy by contacting
# jason@glencoesoftware.com.
#
# Author: Carlos Neves <carlos(at)glencoesoftware.com>

from testdb_create import *

global client

if __name__ == '__main__':
    loginAsRoot()
    client = getClient()
    p = getTestProject(client)
    if p is not None:
        update = client.getUpdateService()
        d = getTestDataset(client, p)
        if d is not None:
            delete = client.getDeleteService()
            delete.deleteImagesByDataset(d.getId(), True)
            update.deleteObject(d._obj)
        update.deleteObject(p._obj)
    # TODO do something about the users too
