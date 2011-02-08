package omeis.providers.re.utests;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelData;
import ome.model.enums.PixelsType;

public class TestPixelBuffer implements PixelBuffer {
	
	private byte[] dummyPlane; 
	
	private PixelsType pixelsType;
	
	public TestPixelBuffer(PixelsType pixelsType, byte[] dummyPlane)
	{
		this.pixelsType = pixelsType;
		this.dummyPlane = dummyPlane;
	}

	public byte[] calculateMessageDigest() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	public int getByteWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	public PixelData getCol(Integer arg0, Integer arg1, Integer arg2,
			Integer arg3) throws IOException, DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getColDirect(Integer arg0, Integer arg1, Integer arg2,
			Integer arg3, byte[] arg4) throws IOException,
			DimensionsOutOfBoundsException {
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

	public PixelData getPlane(Integer arg0, Integer arg1, Integer arg2)
			throws IOException, DimensionsOutOfBoundsException
	{
		return new PixelData(pixelsType, ByteBuffer.wrap(dummyPlane));
	}

	public byte[] getPlaneDirect(Integer arg0, Integer arg1, Integer arg2,
			byte[] arg3) throws IOException, DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public Long getPlaneOffset(Integer arg0, Integer arg1, Integer arg2)
			throws DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getPlaneRegionDirect(Integer arg0, Integer arg1,
			Integer arg2, Integer arg3, Integer arg4, byte[] arg5)
			throws IOException, DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getPlaneSize() {
		// TODO Auto-generated method stub
		return null;
	}

	public PixelData getRegion(Integer arg0, Long arg1) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getRegionDirect(Integer arg0, Long arg1, byte[] arg2)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public PixelData getRow(Integer arg0, Integer arg1, Integer arg2,
			Integer arg3) throws IOException, DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getRowDirect(Integer arg0, Integer arg1, Integer arg2,
			Integer arg3, byte[] arg4) throws IOException,
			DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public Long getRowOffset(Integer arg0, Integer arg1, Integer arg2,
			Integer arg3) throws DimensionsOutOfBoundsException {
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

	public PixelData getStack(Integer arg0, Integer arg1) throws IOException,
			DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getStackDirect(Integer arg0, Integer arg1, byte[] arg2)
			throws IOException, DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public Long getStackOffset(Integer arg0, Integer arg1)
			throws DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getStackSize() {
		// TODO Auto-generated method stub
		return null;
	}

	public PixelData getTimepoint(Integer arg0) throws IOException,
			DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getTimepointDirect(Integer arg0, byte[] arg1)
			throws IOException, DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public Long getTimepointOffset(Integer arg0)
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

	public void setPlane(ByteBuffer arg0, Integer arg1, Integer arg2,
			Integer arg3) throws IOException, DimensionsOutOfBoundsException,
			BufferOverflowException {
		// TODO Auto-generated method stub

	}

	public void setPlane(byte[] arg0, Integer arg1, Integer arg2, Integer arg3)
			throws IOException, DimensionsOutOfBoundsException,
			BufferOverflowException {
		// TODO Auto-generated method stub

	}

	public void setRegion(Integer arg0, Long arg1, byte[] arg2)
			throws IOException, BufferOverflowException {
		// TODO Auto-generated method stub

	}

	public void setRegion(Integer arg0, Long arg1, ByteBuffer arg2)
			throws IOException, BufferOverflowException {
		// TODO Auto-generated method stub

	}

	public void setRow(ByteBuffer arg0, Integer arg1, Integer arg2,
			Integer arg3, Integer arg4) throws IOException,
			DimensionsOutOfBoundsException, BufferOverflowException {
		// TODO Auto-generated method stub

	}

	public void setStack(ByteBuffer arg0, Integer arg1, Integer arg2,
			Integer arg3) throws IOException, DimensionsOutOfBoundsException,
			BufferOverflowException {
		// TODO Auto-generated method stub

	}

	public void setStack(byte[] arg0, Integer arg1, Integer arg2, Integer arg3)
			throws IOException, DimensionsOutOfBoundsException,
			BufferOverflowException {
		// TODO Auto-generated method stub

	}

	public void setTimepoint(ByteBuffer arg0, Integer arg1) throws IOException,
			DimensionsOutOfBoundsException, BufferOverflowException {
		// TODO Auto-generated method stub

	}

	public void setTimepoint(byte[] arg0, Integer arg1) throws IOException,
			DimensionsOutOfBoundsException, BufferOverflowException {
		// TODO Auto-generated method stub

	}

	public void checkBounds(Integer arg0, Integer arg1, Integer arg2,
			Integer arg3, Integer arg4) throws DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		
	}

	public PixelData getPlaneRegion(Integer arg0, Integer arg1, Integer arg2,
			Integer arg3, Integer arg4, Integer arg5, Integer arg6, Integer arg7)
			throws IOException, DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

}
