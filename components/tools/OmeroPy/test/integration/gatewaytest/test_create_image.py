
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
    def testCreateImage(self, size_xy, dtype):
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
        if dtype in [np.float32, np.float64]:
            info = np.finfo(dtype)
        else:
            info = np.iinfo(dtype)
        for index, ch in enumerate(new_image.getChannels()):
            assert ch.getWindowMin() == pix_func(0, index, 0)
            assert ch.getWindowMax() == pix_func(size_z - 1, index, size_t - 1)
            assert ch.getWindowMax() <= info.max
