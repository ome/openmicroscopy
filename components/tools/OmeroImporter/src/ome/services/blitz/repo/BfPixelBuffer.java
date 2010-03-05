/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import java.io.IOException;
import java.io.Serializable;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import loci.formats.ImageReader;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @since Beta4.1
 */
public class BfPixelBuffer implements PixelBuffer, Serializable {

    private final static Log log = LogFactory.getLog(BfPixelBuffer.class);

    private final ImageReader reader = new ImageReader();
    
    public BfPixelBuffer() {
        
    }
    
    public byte[] calculateMessageDigest() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public void checkBounds(Integer y, Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        // TODO Auto-generated method stub
        
    }

    public void close() throws IOException {
        // TODO Auto-generated method stub
        
    }

    public int getByteWidth() {
        // TODO Auto-generated method stub
        return 0;
    }

    public PixelData getCol(Integer x, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException {
        // TODO Auto-generated method stub
        return null;
    }

    public byte[] getColDirect(Integer x, Integer z, Integer c, Integer t,
            byte[] buffer) throws IOException, DimensionsOutOfBoundsException {
        // TODO Auto-generated method stub
        return null;
    }

    public Integer getColSize() {
        // TODO Auto-generated method stub
        return null;
    }

    public long getId() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getPath() {
        // TODO Auto-generated method stub
        return null;
    }

    public PixelData getPlane(Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException {
        // TODO Auto-generated method stub
        return null;
    }

    public byte[] getPlaneDirect(Integer z, Integer c, Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException {
        // TODO Auto-generated method stub
        return null;
    }

    public Long getPlaneOffset(Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        // TODO Auto-generated method stub
        return null;
    }

    public byte[] getPlaneRegionDirect(Integer z, Integer c, Integer t,
            Integer count, Integer offset, byte[] buffer) throws IOException,
            DimensionsOutOfBoundsException {
        // TODO Auto-generated method stub
        return null;
    }

    public Integer getPlaneSize() {
        // TODO Auto-generated method stub
        return null;
    }

    public PixelData getRegion(Integer size, Long offset) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public byte[] getRegionDirect(Integer size, Long offset, byte[] buffer)
            throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public PixelData getRow(Integer y, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException {
        // TODO Auto-generated method stub
        return null;
    }

    public byte[] getRowDirect(Integer y, Integer z, Integer c, Integer t,
            byte[] buffer) throws IOException, DimensionsOutOfBoundsException {
        // TODO Auto-generated method stub
        return null;
    }

    public Long getRowOffset(Integer y, Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        // TODO Auto-generated method stub
        return null;
    }

    public Integer getRowSize() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getSizeC() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getSizeT() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getSizeX() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getSizeY() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getSizeZ() {
        // TODO Auto-generated method stub
        return 0;
    }

    public PixelData getStack(Integer c, Integer t) throws IOException,
            DimensionsOutOfBoundsException {
        // TODO Auto-generated method stub
        return null;
    }

    public byte[] getStackDirect(Integer c, Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException {
        // TODO Auto-generated method stub
        return null;
    }

    public Long getStackOffset(Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        // TODO Auto-generated method stub
        return null;
    }

    public Integer getStackSize() {
        // TODO Auto-generated method stub
        return null;
    }

    public PixelData getTimepoint(Integer t) throws IOException,
            DimensionsOutOfBoundsException {
        // TODO Auto-generated method stub
        return null;
    }

    public byte[] getTimepointDirect(Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException {
        // TODO Auto-generated method stub
        return null;
    }

    public Long getTimepointOffset(Integer t)
            throws DimensionsOutOfBoundsException {
        // TODO Auto-generated method stub
        return null;
    }

    public Integer getTimepointSize() {
        // TODO Auto-generated method stub
        return null;
    }

    public Integer getTotalSize() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isFloat() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isSigned() {
        // TODO Auto-generated method stub
        return false;
    }

    public void setPlane(ByteBuffer buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException {
throw new UnsupportedOperationException("Cannot write to repository");
        
    }

    public void setPlane(byte[] buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException {
throw new UnsupportedOperationException("Cannot write to repository");
        
    }

    public void setRegion(Integer size, Long offset, byte[] buffer)
            throws IOException, BufferOverflowException {
throw new UnsupportedOperationException("Cannot write to repository");
        
    }

    public void setRegion(Integer size, Long offset, ByteBuffer buffer)
            throws IOException, BufferOverflowException {
throw new UnsupportedOperationException("Cannot write to repository");
        
    }

    public void setRow(ByteBuffer buffer, Integer y, Integer z, Integer c,
            Integer t) throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException {
throw new UnsupportedOperationException("Cannot write to repository");
        
    }

    public void setStack(ByteBuffer buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException {
throw new UnsupportedOperationException("Cannot write to repository");
        
    }

    public void setStack(byte[] buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException {
throw new UnsupportedOperationException("Cannot write to repository");
        
    }

    public void setTimepoint(ByteBuffer buffer, Integer t) throws IOException,
            DimensionsOutOfBoundsException, BufferOverflowException {
throw new UnsupportedOperationException("Cannot write to repository");
        
    }

    public void setTimepoint(byte[] buffer, Integer t) throws IOException,
            DimensionsOutOfBoundsException, BufferOverflowException {
throw new UnsupportedOperationException("Cannot write to repository");
        
    }


}