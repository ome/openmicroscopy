#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test focused on the BfPixelsStore API
   
   This test compares data got through BFPixelsStore using
   different methods. No file needs to be imported for these
   tests. bfpixelsstoreexternal.py tests that the methods
   return the same data as the equivalent rps methods would
   for imported data.
   
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
        # Get repository and the bf pixels store on the copied file
        repoMap = sess.sharedResources().repositories()
        for r in range(len(repoMap.descriptions)):
            if repoMap.descriptions[r].name.val == dataDir.parent.name:
                repoIndex = r

        repoPrx = repoMap.proxies[repoIndex]
        self.bf = repoPrx.pixels(self.repo_filename)

        byteWidth = self.bf.getByteWidth()
        rowSize = self.bf.getRowSize()
        planeSize = self.bf.getPlaneSize()
        stackSize = self.bf.getStackSize()
        timepointSize = self.bf.getTimepointSize()
        totalSize = self.bf.getTotalSize()
        
        self.sizeX = rowSize/byteWidth
        self.sizeY = planeSize/rowSize
        self.sizeZ = stackSize/planeSize
        self.sizeC = timepointSize/stackSize
        self.sizeT = totalSize/timepointSize


    def tidyUp(self):
        path.remove(self.repo_filename)
        path.rmdir(self.tmp_dir)
        #self.bf.close() # close() hangs at present
        
    
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
        
    def testJPG(self):
        self.setUpTestFile("test.jpg")
        self.allTests()
        self.tidyUp()
        
    def allTests(self):
        self.xtestGetRowFromHypercube()
        self.xtestGetColFromHypercube()
        self.xtestGetPlaneFromHypercube()
        self.xtestGetStackFromHypercube()
        self.xtestGetTimepointFromHypercube()
        self.xtestGetPlaneFromRows()
        self.xtestGetStackFromPlanes()
        self.xtestGetTimepointFromStacks()
    
    # In the tests below the middling element is got.
    def xtestGetRowFromHypercube(self):
        y = self.sizeY/2
        z = self.sizeZ/2
        c = self.sizeC/2
        t = self.sizeT/2
        rowDirect = self.bf.getRow(y,z,c,t)
        rowHypercube = self.bf.getHypercube([0,y,z,c,t],[self.sizeX,1,1,1,1],[1,1,1,1,1])
        rowDirect_md5 = hashlib.md5(rowDirect)
        rowHypercube_md5 = hashlib.md5(rowHypercube)
        self.assert_(rowDirect_md5.digest() == rowHypercube_md5.digest())

    def xtestGetColFromHypercube(self):
        x = self.sizeX/2
        z = self.sizeZ/2
        c = self.sizeC/2
        t = self.sizeT/2
        colDirect = self.bf.getCol(x,z,c,t)
        colHypercube = self.bf.getHypercube([x,0,z,c,t],[1,self.sizeY,1,1,1],[1,1,1,1,1])
        colDirect_md5 = hashlib.md5(colDirect)
        colHypercube_md5 = hashlib.md5(colHypercube)
        self.assert_(colDirect_md5.digest() == colHypercube_md5.digest())

    def xtestGetPlaneFromHypercube(self):
        z = self.sizeZ/2
        c = self.sizeC/2
        t = self.sizeT/2
        planeDirect = self.bf.getPlane(z,c,t)
        planeHypercube = self.bf.getHypercube([0,0,z,c,t],[self.sizeX,self.sizeY,1,1,1],[1,1,1,1,1])
        planeDirect_md5 = hashlib.md5(planeDirect)
        planeHypercube_md5 = hashlib.md5(planeHypercube)
        self.assert_(planeDirect_md5.digest() == planeHypercube_md5.digest())

    def xtestGetStackFromHypercube(self):
        c = self.sizeC/2
        t = self.sizeT/2
        stackDirect = self.bf.getStack(c,t)
        stackHypercube = self.bf.getHypercube([0,0,0,c,t],[self.sizeX,self.sizeY,self.sizeZ,1,1],[1,1,1,1,1])
        stackDirect_md5 = hashlib.md5(stackDirect)
        stackHypercube_md5 = hashlib.md5(stackHypercube)
        self.assert_(stackDirect_md5.digest() == stackHypercube_md5.digest())

    def xtestGetTimepointFromHypercube(self):
        t = self.sizeT/2
        timepointDirect = self.bf.getTimepoint(t)
        timepointHypercube = self.bf.getHypercube([0,0,0,0,t],[self.sizeX,self.sizeY,self.sizeZ,self.sizeC,1],[1,1,1,1,1])
        timepointDirect_md5 = hashlib.md5(timepointDirect)
        timepointHypercube_md5 = hashlib.md5(timepointHypercube)
        self.assert_(timepointDirect_md5.digest() == timepointHypercube_md5.digest())

    def xtestGetPlaneFromRows(self):
        z = self.sizeZ/2
        c = self.sizeC/2
        t = self.sizeT/2
        plane = self.bf.getPlane(z,c,t)
        rows =''
        for y in range(self.sizeY):
            rows += self.bf.getRow(y,z,c,t)

        rows_md5 = hashlib.md5(rows)
        plane_md5 = hashlib.md5(plane)
        self.assert_(rows_md5.digest() == plane_md5.digest())

    def xtestGetStackFromPlanes(self):
        c = self.sizeC/2
        t = self.sizeT/2
        stack = self.bf.getStack(c,t)
        planes = ''
        for z in range(self.sizeZ):
            planes += self.bf.getPlane(z,c,t)

        planes_md5 = hashlib.md5(planes)
        stack_md5 = hashlib.md5(stack)
        self.assert_(planes_md5.digest() == stack_md5.digest())

    def xtestGetTimepointFromStacks(self):
        t = self.sizeT/2
        timepoint = self.bf.getTimepoint(t)
        stacks = ''
        for c in range(self.sizeC):
            stacks += self.bf.getStack(c,t)

        stacks_md5 = hashlib.md5(stacks)
        timepoint_md5 = hashlib.md5(timepoint)
        self.assert_(stacks_md5.digest() == timepoint_md5.digest())

if __name__ == '__main__':
    unittest.main()
