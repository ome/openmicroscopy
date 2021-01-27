
import numpy as np
from itertools import chain
import pytest

import omero
from omero.gateway import BlitzGateway
from omero.testlib import ITest


class TestCreateImage(ITest):

    def get_planes(self, size_x, size_y, size_z, size_c, size_t, dtype, func):
        for z in range(size_z):
            for c in range(size_c):
                for t in range(size_t):
                    yield np.full((size_y, size_x), func(z, c, t), dtype=dtype)


    @pytest.mark.parametrize('size_xy', [[200, 300], [3000, 4500]])
    @pytest.mark.parametrize('dtype', [np.int8, np.int16, np.uint16, np.int32, np.float32, np.float64])
    def test_create_image(self, size_xy, dtype):
        """
        Tests conn.createImageFromNumpySeq()
        """
        conn = BlitzGateway(client_obj = self.client)

        size_x, size_y = size_xy
        size_z = size_c = size_t = 2

        maxplanesize = conn.getMaxPlaneSize()
        if size_x * size_y > maxplanesize[0] * maxplanesize[1]:
            if dtype in [np.float32, np.float64]:
                # setTile() fails with ROMIO pixel buffer only supports full row writes.
                return

        name = "test_create_image_%s_%s_%s_%s_%s_%s" % (
            size_x, size_y, size_z, size_c, size_t, dtype)
        description = "Description of %s" % name
        dataset = self.make_dataset(name)
        def pix_func(z, c, t):
            return (10 * c) + z + t
        plane_gen = self.get_planes(size_x, size_y,
                                    size_z, size_c, size_t, dtype, pix_func)
        new_image = conn.createImageFromNumpySeq(
            plane_gen, name, dataset=dataset,
            sizeZ=size_z, sizeC=size_c, sizeT=size_t,
            description=description)
        assert new_image is not None
        print('new_image', new_image.id)
        assert new_image.id is not None
        assert new_image.name == name
        assert new_image.description == description
        assert new_image.getSizeZ() == size_z
        assert new_image.getSizeC() == size_c
        assert new_image.getSizeT() == size_t
        assert new_image.getSizeX() == size_x
        assert new_image.getSizeY() == size_y
        for index, ch in enumerate(new_image.getChannels()):
            assert ch.getWindowMin() == pix_func(0, index, 0)
            assert ch.getWindowMax() == pix_func(size_z - 1, index, size_t - 1)

        ds = conn.getObject("Dataset", dataset.id.val)
        assert len(list(ds.listChildren())) == 1

    def test_create_image_from_image(self):
        conn = BlitzGateway(client_obj = self.client)
        size_x = size_y = 256
        size_c = size_z = 2
        size_t = 1
        dtype = np.int8

        name = "test_create_image_from_image"
        description = "Description of %s" % name
        dataset = self.make_dataset(name)
        def pix_func(z, c, t):
            return (10 * c) + z + t
        plane_gen = self.get_planes(size_x, size_y,
                                    size_z, size_c, size_t, dtype, pix_func)
        original = conn.createImageFromNumpySeq(
            plane_gen, name, dataset=dataset,
            sizeZ=size_z, sizeC=size_c, sizeT=size_t,
            description=description)

        # Create 2nd image from first...
        image_name = "new image from ID: %s" % original.id
        description = "Description of %s" % name
        clist = range(original.getSizeC())
        zctList = []
        for z in range(original.getSizeZ()):
            for c in clist:
                for t in range(original.getSizeT()):
                    zctList.append((z,c,t))
        def planeGen():
            planes = original.getPrimaryPixels().getPlanes(zctList)
            for p in planes:
                # Test that this int32 data will be converted to int8
                p = p.astype(np.int32)
                new_plane = p * 10
                yield new_plane
        new_image = conn.createImageFromNumpySeq(
            planeGen(), image_name, sizeZ=size_z, sizeC=size_c, sizeT=size_t,
            sourceImageId=original.id, channelList=clist,
            dataset=dataset, description=description)

        assert original.getPixelsType() == new_image.getPixelsType()
        assert new_image.name == image_name
        assert new_image.description == description

        new_pix = new_image.getPrimaryPixels()
        for index, ch in enumerate(new_image.getChannels()):
            assert ch.getWindowMin() == new_pix.getPlane(0, index, 0).min()
            assert ch.getWindowMax() == new_pix.getPlane(size_z - 1, index, size_t - 1).max()

        ds = conn.getObject("Dataset", dataset.id.val)
        assert len(list(ds.listChildren())) == 2
