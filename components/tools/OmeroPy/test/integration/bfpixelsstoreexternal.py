#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test focused on the BfPixelsStore API
   
   This test compares data got through BFPixelsStore and
   RawPixelsStore on the same image file. It essentially
   compares method for method between the two implementaions
   on the same binary data.
   
   Copyright 2011 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest, time, shutil, hashlib, difflib, binascii
import library as lib
import omero, omero.gateway
from omero.rtypes import *
from path import path

class TestBfPixelsStore(lib.ITest):
    
    def setUpTestFile(self, name):
        sess = self.root.sf
        filename = self.OmeroPy / ".." / ".." / ".." / "components" / "common" / "test" / name
        dataDir = path(sess.getConfigService().getConfigValue("omero.data.dir"));
        self.tmp_dir = dataDir / self.uuid()
        self.repo_filename = self.tmp_dir / name
        # Copy file into repository subdirectory
        path.mkdir(self.tmp_dir)
        shutil.copyfile(filename, self.repo_filename)
        # Import the same file
        self.pix_id = long(self.import_image(filename)[0])
        self.rp = self.client.sf.createRawPixelsStore()
        self.rp.setPixelsId(self.pix_id, True)
        ### This line will deprecated on API unification.
        pixels = self.client.sf.createGateway().getPixels(self.pix_id)
        self.sizeX = pixels.getSizeX().getValue()
        self.sizeY = pixels.getSizeY().getValue()
        self.sizeZ = pixels.getSizeZ().getValue()
        self.sizeC = pixels.getSizeC().getValue()
        self.sizeT = pixels.getSizeT().getValue()
        # Get repository and the bf pixels store on the copied file
        repoMap = sess.sharedResources().repositories()
        for r in range(len(repoMap.descriptions)):
            if repoMap.descriptions[r].name.val == dataDir.parent.name:
                repoIndex = r

        repoPrx = repoMap.proxies[repoIndex]
        self.bf = repoPrx.pixels(self.repo_filename)

    def tidyUp(self):
        path.remove(self.repo_filename)
        path.rmdir(self.tmp_dir)
        #self.bf.close() # close() hangs at present
        self.rp.close()
        
    
    # Rather than have one import per test or do a better workaround
    # for now just lump all the tests together.
    def testBMP(self):
        self.setUpTestFile("test.bmp")
        self.allTests()
        self.tidyUp()
        
    def testDV(self):
        self.setUpTestFile("tinyTest.d3d.dv")
        self.allTests()
        self.tidyUp()
        
    # jpg files now generate pyramids. Could use backOff to
    # get around setId MPE exceptions but other methods in
    # the rps interface are not implemented, offset getters, etc.
    #    def testJPG(self):
    #        self.setUpTestFile("test.jpg")
    #        self.allTests()
    #        self.tidyUp()
        
    def allTests(self):
        self.xtestOtherGetters()
        self.xtestGetRow()
        self.xtestGetCol()
        self.xtestGetPlane()
        self.xtestGetStack()
        self.xtestGetTimepoint()
        self.xtestGetHypercube()
        self.xtestGetHypercubeAgainstRPSGetHypercube()
        
    # In this test below the middlish hypercube is got.
    # This was written when the rps method was not implemented 
    # so extract the cube manually by repeated calls to other rps methods.
    def xtestGetHypercube(self):
        x1 = self.sizeX/3; x2 = (2*self.sizeX)/3 + 1 - x1
        y1 = self.sizeY/3; y2 = (2*self.sizeY)/3 + 1 - y1
        z1 = self.sizeZ/3; z2 = (2*self.sizeZ)/3 + 1 - z1
        c1 = self.sizeC/3; c2 = (2*self.sizeC)/3 + 1 - c1
        t1 = self.sizeT/3; t2 = (2*self.sizeT)/3 + 1 - t1
        
        bf_data = self.bf.getHypercube([x1,y1,z1,c1,t1],[x2,y2,z2,c2,t2],[1,1,1,1,1])
        rp_data = self.getSolidHypercubeFromRPS([x1,y1,z1,c1,t1],[x2,y2,z2,c2,t2],[1,1,1,1,1])
        self.assert_(len(rp_data) == len(bf_data))
        bf_md5 = hashlib.md5(bf_data)
        rp_md5 = hashlib.md5(rp_data)
        self.assert_(bf_md5.digest() == rp_md5.digest())
        
        bf_data = self.bf.getHypercube([x1,y1,z1,c1,t1],[x2,y2,z2,c2,t2],[2,2,1,1,1])
        rp_data = self.getHypercubeFromRPS([x1,y1,z1,c1,t1],[x2,y2,z2,c2,t2],[2,2,1,1,1])
        self.assert_(len(rp_data) == len(bf_data))
        bf_md5 = hashlib.md5(bf_data)
        rp_md5 = hashlib.md5(rp_data)
        self.assert_(bf_md5.digest() == rp_md5.digest())

        bf_data = self.bf.getHypercube([x1,y1,z1,c1,t1],[x2,y2,z2,c2,t2],[6,5,4,3,2])
        rp_data = self.getHypercubeFromRPS([x1,y1,z1,c1,t1],[x2,y2,z2,c2,t2],[6,5,4,3,2])
        self.assert_(len(rp_data) == len(bf_data))
        bf_md5 = hashlib.md5(bf_data)
        rp_md5 = hashlib.md5(rp_data)
        self.assert_(bf_md5.digest() == rp_md5.digest())

    # Again, the middlish hypercube is got.
    def xtestGetHypercubeAgainstRPSGetHypercube(self):
        x1 = self.sizeX/3; x2 = (2*self.sizeX)/3 + 1 - x1
        y1 = self.sizeY/3; y2 = (2*self.sizeY)/3 + 1 - y1
        z1 = self.sizeZ/3; z2 = (2*self.sizeZ)/3 + 1 - z1
        c1 = self.sizeC/3; c2 = (2*self.sizeC)/3 + 1 - c1
        t1 = self.sizeT/3; t2 = (2*self.sizeT)/3 + 1 - t1
        
        bf_data = self.bf.getHypercube([x1,y1,z1,c1,t1],[x2,y2,z2,c2,t2],[1,1,1,1,1])
        rp_data = self.rp.getHypercube([x1,y1,z1,c1,t1],[x2,y2,z2,c2,t2],[1,1,1,1,1])
        self.assert_(len(rp_data) == len(bf_data))
        bf_md5 = hashlib.md5(bf_data)
        rp_md5 = hashlib.md5(rp_data)
        self.assert_(bf_md5.digest() == rp_md5.digest())
        
        bf_data = self.bf.getHypercube([x1,y1,z1,c1,t1],[x2,y2,z2,c2,t2],[2,2,1,1,1])
        rp_data = self.rp.getHypercube([x1,y1,z1,c1,t1],[x2,y2,z2,c2,t2],[2,2,1,1,1])
        self.assert_(len(rp_data) == len(bf_data))
        bf_md5 = hashlib.md5(bf_data)
        rp_md5 = hashlib.md5(rp_data)
        self.assert_(bf_md5.digest() == rp_md5.digest())

        bf_data = self.bf.getHypercube([x1,y1,z1,c1,t1],[x2,y2,z2,c2,t2],[6,5,4,3,2])
        rp_data = self.rp.getHypercube([x1,y1,z1,c1,t1],[x2,y2,z2,c2,t2],[6,5,4,3,2])
        self.assert_(len(rp_data) == len(bf_data))
        bf_md5 = hashlib.md5(bf_data)
        rp_md5 = hashlib.md5(rp_data)
        self.assert_(bf_md5.digest() == rp_md5.digest())

    def getSolidHypercubeFromRPS(self,start,size,step):
        bw = self.rp.getByteWidth()
        data = ""
        for t in range(start[4],start[4]+size[4],step[4]):
            for c in range(start[3],start[3]+size[3],step[3]):
                for z in range(start[2],start[2]+size[2],step[2]):
                    for y in range(start[1],start[1]+size[1],step[1]):
                        offset = self.rp.getRowOffset(y,z,c,t)
                        data += self.rp.getRegion(size[0]*bw,start[0]*bw+offset)
        return data
 
    def getHypercubeFromRPS(self,start,size,step):
        bw = self.rp.getByteWidth()
        data = ""
        for t in range(start[4],start[4]+size[4],step[4]):
            for c in range(start[3],start[3]+size[3],step[3]):
                for z in range(start[2],start[2]+size[2],step[2]):
                    for y in range(start[1],start[1]+size[1],step[1]):
                        offset = self.rp.getRowOffset(y,z,c,t)
                        for x in range(start[0],start[0]+size[0],step[0]):
                            data += self.rp.getRegion(bw,x*bw+offset)
        return data
 
       
    # In the tests below the middling element is got.
    def xtestGetRow(self):
        bf_size = self.bf.getRowSize()
        rp_size = self.rp.getRowSize()
        self.assert_(bf_size == rp_size)

        y = self.sizeY/2
        z = self.sizeZ/2
        c = self.sizeC/2
        t = self.sizeT/2
        bf_data = self.bf.getRow(y,z,c,t)
        rp_data = self.rp.getRow(y,z,c,t)
        self.assert_(bf_size == len(bf_data))
        
        bf_md5 = hashlib.md5(bf_data)
        rp_md5 = hashlib.md5(rp_data)
        self.assert_(bf_md5.digest() == rp_md5.digest())

    def xtestGetCol(self):
        bf_size = self.bf.getByteWidth()*self.bf.getPlaneSize()/self.bf.getRowSize()
        rp_size = self.rp.getByteWidth()*self.rp.getPlaneSize()/self.rp.getRowSize()
        self.assert_(bf_size == rp_size)

        x = self.sizeX/2
        z = self.sizeZ/2
        c = self.sizeC/2
        t = self.sizeT/2
        bf_data = self.bf.getCol(x,z,c,t)
        rp_data = self.rp.getCol(x,z,c,t)
        self.assert_(bf_size == len(bf_data))
        
        bf_md5 = hashlib.md5(bf_data)
        rp_md5 = hashlib.md5(rp_data)
        self.assert_(bf_md5.digest() == rp_md5.digest())
    
    def xtestGetPlane(self):
        bf_size = self.bf.getPlaneSize()
        rp_size = self.rp.getPlaneSize()
        self.assert_(bf_size == rp_size)
        
        z = self.sizeZ/2
        c = self.sizeC/2
        t = self.sizeT/2
        bf_data = self.bf.getPlane(z,c,t)
        rp_data = self.rp.getPlane(z,c,t)
        self.assert_(bf_size == len(bf_data))
        
        bf_md5 = hashlib.md5(bf_data)
        rp_md5 = hashlib.md5(rp_data)
        self.assert_(bf_md5.digest() == rp_md5.digest())
    
    def xtestGetStack(self):
        bf_size = self.bf.getStackSize()
        rp_size = self.rp.getStackSize()
        self.assert_(bf_size == rp_size)
        
        c = self.sizeC/2
        t = self.sizeT/2
        bf_data = self.bf.getStack(c,t)
        rp_data = self.rp.getStack(c,t)
        self.assert_(bf_size == len(bf_data))
        
        bf_md5 = hashlib.md5(bf_data)
        rp_md5 = hashlib.md5(rp_data)
        self.assert_(bf_md5.digest() == rp_md5.digest())
    
    def xtestGetTimepoint(self):
        bf_size = self.bf.getTimepointSize()
        rp_size = self.rp.getTimepointSize()
        self.assert_(bf_size == rp_size)
        
        t = self.sizeT/2
        bf_data = self.bf.getTimepoint(t)
        rp_data = self.rp.getTimepoint(t)
        self.assert_(bf_size == len(bf_data))
        
        bf_md5 = hashlib.md5(bf_data)
        rp_md5 = hashlib.md5(rp_data)
        self.assert_(bf_md5.digest() == rp_md5.digest())
    
    def xtestOtherGetters(self):
        bf_size = self.bf.getTotalSize()
        rp_size = self.rp.getTotalSize()
        self.assert_(bf_size == rp_size)
        
        bf_width = self.bf.getByteWidth()
        rp_width = self.rp.getByteWidth()
        self.assert_(bf_width == rp_width)

        """
        # The offset getters are implemented in the bf classes but not in the pyramid variants.
        # The getters are not really necessary in the bf classes so disabling these tests.
        bf_offset = self.bf.getRowOffset(0,0,0,0)
        self.assert_(bf_offset == 0)
        bf_offset = self.bf.getPlaneOffset(0,0,0)
        self.assert_(bf_offset == 0)
        bf_offset = self.bf.getStackOffset(0,0)
        self.assert_(bf_offset == 0)
        bf_offset = self.bf.getTimepointOffset(0)
        self.assert_(bf_offset == 0)
        
        y = self.sizeY/2
        z = self.sizeZ/2
        c = self.sizeC/2
        t = self.sizeT/2
        bf_offset = self.bf.getRowOffset(y,z,c,t)
        rp_offset = self.rp.getRowOffset(y,z,c,t)
        self.assert_(bf_offset == rp_offset)
        
        bf_offset = self.bf.getPlaneOffset(z,c,t)
        rp_offset = self.rp.getPlaneOffset(z,c,t)
        self.assert_(bf_offset == rp_offset)
        
        bf_offset = self.bf.getStackOffset(c,t)
        rp_offset = self.rp.getStackOffset(c,t)
        self.assert_(bf_offset == rp_offset)
        
        bf_offset = self.bf.getTimepointOffset(t)
        rp_offset = self.rp.getTimepointOffset(t)
        self.assert_(bf_offset == rp_offset)
        """
        
if __name__ == '__main__':
    unittest.main()
