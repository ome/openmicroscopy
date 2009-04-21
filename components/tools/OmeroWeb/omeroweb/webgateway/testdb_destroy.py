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
