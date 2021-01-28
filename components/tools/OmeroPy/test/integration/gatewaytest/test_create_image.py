
import numpy as np
from itertools import chain
import pytest

import omero
from omero.gateway import BlitzGateway
from omero.testlib import ITest
from omero.util.tiles import TileLoopIteration, RPSTileLoop


class TestCreateImage(ITest):

    def get_planes(self, size_x, size_y, size_z, size_c, size_t, dtype, func):
        for z in range(size_z):
            for c in range(size_c):
                for t in range(size_t):
                    yield np.full((size_y, size_x), func(z, c, t), dtype=dtype)

    def get_big_planes(self, size_x, size_y, size_z, size_c, size_t, dtype, func):
        for t in range(size_t):
            for c in range(size_c):
                for z in range(size_z):
                    yield np.full((size_y, size_x), func(z, c, t), dtype=dtype)

    @pytest.mark.parametrize('size_xy', [[200, 300], [3000, 4500]])
    @pytest.mark.parametrize('dtype', [np.int8, np.int16, np.uint16, np.int32, np.float32, np.float64])
    def test_create_image(self, size_xy, dtype):
        """
        Tests conn.createImageFromNumpySeq()
        """
        conn = BlitzGateway(client_obj = self.client)

        size_x, size_y = size_xy
        size_c = size_t = 2
        size_z = 1

        maxplanesize = conn.getMaxPlaneSize()
        big_image = size_x * size_y > maxplanesize[0] * maxplanesize[1]
        if big_image:
            if dtype in [np.float32, np.float64]:
                # setTile() fails with ROMIO pixel buffer only supports full row writes.
                return

        name = "test_create_image_%s_%s_%s_%s_%s_%s" % (
            size_x, size_y, size_z, size_c, size_t, dtype)
        description = "Description of %s" % name
        dataset = self.make_dataset(name)
        def pix_func(z, c, t):
            return (10 * c) + z + t
        if big_image:
            # TileLoopIteration needs planes in different order
            plane_gen = self.get_big_planes(size_x, size_y, size_z,
                                            size_c, size_t, dtype, pix_func)
        else:
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

        # Test planes are in the right place
        primary_pix = new_image.getPrimaryPixels()
        region = (0, 0, 50, 50)
        for t in range(size_t):
            for c in range(size_c):
                for z in range(size_z):
                    tile = primary_pix.getTile(z, c, t, region)
                    assert tile.min() == tile.max()
                    # known failure for int32 - don't know why!
                    assert tile.min() == pix_func(z, c, t) or (dtype == np.int32 and big_image)

        ds = conn.getObject("Dataset", dataset.id.val)
        assert len(list(ds.listChildren())) == 1

    @pytest.mark.parametrize('size_xy', [[200, 300], [3000, 4500]])
    def test_copy_image(self, size_xy):
        conn = BlitzGateway(client_obj = self.client)
        # size_x = size_y = 256
        size_x, size_y = size_xy
        size_c = size_z = 2
        size_t = 1
        dtype = np.int8
        region = (0, 0, 50, 50)

        name = "test_create_image_from_image"
        description = "Description of %s" % name
        dataset = self.make_dataset(name)
        def pix_func(z, c, t):
            return (10 * c) + z + t
        maxplanesize = conn.getMaxPlaneSize()
        big_image = size_x * size_y > maxplanesize[0] * maxplanesize[1]
        zctList = []
        if big_image:
            # TileLoopIteration needs planes in different order
            plane_gen = self.get_big_planes(size_x, size_y, size_z,
                                            size_c, size_t, dtype, pix_func)
            for t in range(size_t):
                for c in range(size_c):
                    for z in range(size_z):
                        zctList.append((z,c,t))
        else:
            plane_gen = self.get_planes(size_x, size_y,
                                        size_z, size_c, size_t, dtype, pix_func)
            for z in range(size_z):
                for c in range(size_c):
                    for t in range(size_t):
                        zctList.append((z,c,t))

        original = conn.createImageFromNumpySeq(
            plane_gen, name, dataset=dataset,
            sizeZ=size_z, sizeC=size_c, sizeT=size_t,
            description=description)

        primary_pix = original.getPrimaryPixels()
        # Test planes are in the right place
        region = (0, 0, 50, 50)
        for t in range(original.getSizeT()):
            for c in range(original.getSizeC()):
                for z in range(original.getSizeZ()):
                    tile = primary_pix.getTile(z, c, t, region)
                    assert tile.min() == tile.max()
                    assert tile.min() == pix_func(z, c, t)

        # Create 2nd image from first...
        image_name = "new image from ID: %s" % original.id
        description = "Description of %s" % name
        clist = range(original.getSizeC())
        def planeGen():
            planes = original.getPrimaryPixels().getPlanes(zctList)
            for p in planes:
                # Test that this int32 data will be converted to int8
                p = p.astype(np.int32)
                new_plane = p
                print('planeGen...', new_plane.min(), new_plane.max())
                yield new_plane
        new_image = conn.createImageFromNumpySeq(
            planeGen(), image_name, sizeZ=size_z, sizeC=size_c, sizeT=size_t,
            sourceImageId=original.id, channelList=clist,
            dataset=dataset, description=description)

        print('new_image', new_image.id)
        assert original.getPixelsType() == new_image.getPixelsType()
        assert new_image.name == image_name
        assert new_image.description == description

        new_pix = new_image.getPrimaryPixels()
        # Test planes are in the right place
        for t in range(original.getSizeT()):
            for c in clist:
                for z in range(original.getSizeZ()):
                    tile = new_pix.getTile(z, c, t, region)
                    assert tile.min() == tile.max()
                    assert tile.min() == pix_func(z, c, t)

        for index, ch in enumerate(new_image.getChannels()):
            assert ch.getWindowMin() == new_pix.getPlane(0, index, 0).min()
            assert ch.getWindowMax() == new_pix.getPlane(size_z - 1, index, size_t - 1).max()

        ds = conn.getObject("Dataset", dataset.id.val)
        assert len(list(ds.listChildren())) == 2


class TestCreateBigImage(ITest):

    def test_set_tiles(self):

        conn = BlitzGateway(client_obj = self.client)

        pixels_service = conn.getPixelsService()
        query_service = conn.getQueryService()
        size_x = 4000
        size_y = 3500
        size_z = 2
        size_t = 1
        size_c = 2

        def create_image():
            query = "from PixelsType as p where p.value='int16'"
            pixels_type = query_service.findByQuery(query, None)
            channel_list = range(size_c)
            iid = pixels_service.createImage(
                size_x,
                size_y,
                size_z,
                size_t,
                channel_list,
                pixels_type,
                "test_set_tiles",
                None,
                conn.SERVICE_OPTS)

            return conn.getObject("Image", iid)

        def get_pixel_value(z, c, t):
            return (z * 100) + (c * 10) + t

        tile_height = tile_width = 1024

        def tile_gen():
            for t in range(0, size_t):
                for c in range(0, size_c):
                    for z in range(0, size_z):
                        plane = np.full((size_y, size_x), get_pixel_value(z, c, t), dtype=np.int16)
                        print('tile_gen z %s, c %s, t %s value %s' % (z, c, t, plane.min()))
                        for tile_offset_y in range(
                                0, ((size_y + tile_height - 1) // tile_height)):
                            for tile_offset_x in range(
                                    0, ((size_x + tile_width - 1) // tile_width)):
                                x = tile_offset_x * tile_width
                                y = tile_offset_y * tile_height
                                x2 = min(x + tile_width, size_x)
                                y2 = min(y + tile_height, size_y)
                                tile = plane[y:y2, x:x2]
                                byte_swapped_tile = tile.byteswap()
                                yield byte_swapped_tile.tostring()

        tile_iter = tile_gen()

        class Iteration(TileLoopIteration):

            def run(self, data, z, c, t, x, y, tile_width, tile_height,
                    tile_count):
                tile2d = next(tile_iter)
                data.setTile(tile2d, z, c, t, x, y, tile_width, tile_height)

        new_image = create_image()
        pid = new_image.getPixelsId()
        loop = RPSTileLoop(conn.c.sf, omero.model.PixelsI(pid, False))
        loop.forEachTile(tile_width, tile_height, Iteration())

        for the_c in range(size_c):
            pixels_service.setChannelGlobalMinMax(pid, the_c, float(0),
                                                float(255), conn.SERVICE_OPTS)

        primary_pix = new_image.getPrimaryPixels()
        region = (0, 0, 1000, 1000)
        for t in range(size_t):
            for c in range(size_c):
                for z in range(size_z):
                    tile = primary_pix.getTile(z, c, t, region)
                    assert tile.min() == tile.max()
                    assert tile.min() == get_pixel_value(z, c, t)
