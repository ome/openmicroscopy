//
// RAUrl.java
//

/*
OME Bio-Formats package for reading and converting biological file formats.
Copyright (C) 2005-@year@ UW-Madison LOCI and Glencoe Software, Inc.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package org.openmicroscopy.shoola.env.data;

import java.io.*;
import java.nio.ByteBuffer;

import loci.common.IRandomAccess;
import monitors.MonitorServerPrx;


/**
 * Provides random access to data over HTTP using the IRandomAccess interface.
 * This is slow, but functional.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/loci/formats/RAUrl.java">Trac</a>,
 * <a href="https://skyking.microscopy.wisc.edu/svn/java/trunk/loci/formats/RAUrl.java">SVN</a></dd></dl>
 *
 * @see IRandomAccess
 * @see java.net.HttpURLConnection
 *
 * @author Melissa Linkert linkert at wisc.edu
 */
public class RAOmeroFs implements IRandomAccess {

	public static final String FS_NAME = FSFileSystemView.FS_NAME;
  // -- Fields --

  /** URI of OMERO-FS file */
  private String uri;
  
  /** OMERO-FS monitor server */
  private MonitorServerPrx server;
  
  /** File path on the OMERO-FS server */
  private String path;

  /** Stream pointer */
  private long fp = 0;

  /** Number of bytes in the stream */
  private long length;

  // -- Constructors --

  public RAOmeroFs(String uri) throws IOException {
	  this.uri = uri;
	  this.server = OMEROGateway.getMonitorServer();
	  this.path = uri.substring(FS_NAME.length());
  }

  // -- IRandomAccess API methods --

  /* @see IRandomAccess#close() */
  public void close() throws IOException {
	  // FIXME: No-op.
	  System.err.println("WARNING: close() is a no-op.");
  }

  /* @see IRandomAccess#getFilePointer() */
  public long getFilePointer() throws IOException {
    return fp;
  }

  /* @see IRandomAccess#length() */
  public long length() throws IOException {
	  try {
		  length = server.getSize(path);
		  return length;//server.getSize(path);
	} catch (Exception e) {
		throw new IOException(e.getMessage());
	}
  }
  
  public ByteBuffer read(int len) {
	  return read(fp, len);
  }

  public ByteBuffer read(long off, int len) {
	  System.err.println("Reading " + len + " bytes from " + off);
	  byte[] block = null;
	  try {
		  block = server.readBlock(path, off, len);
	  } catch (Exception e) {
		  //ignore
	  }
	  if (block == null) return null;
	  fp = off + len;
	  return ByteBuffer.wrap(block);
  }

  /* @see IRandomAccess#read() */
  public int read() throws IOException {
	  return read(4).getInt();
  }

  /* @see IRandomAccess#read(byte[]) */
  public int read(byte[] b) throws IOException {
	  byte[] block;
	  try {
		  block = server.readBlock(path, fp, b.length);
	  } catch (Exception e) {
		  throw new IOException(e.getMessage());
	  }
	  fp += block.length;
	  System.arraycopy(block, 0, b, 0, block.length);
	  return block.length;
  }

  /* @see IRandomAccess#read(byte[], int, int) */
  public int read(byte[] b, int off, int len) throws IOException {
	  byte[] block;
	  try {
		  block = server.readBlock(path, fp, len);
		  System.err.println(off+" "+len+" "+fp+" "+length);
	  } catch (Exception e) {
		  throw new IOException(e.getMessage());
	  }
	  if (fp == length) return -1;
	  fp += block.length;
	  System.arraycopy(block, 0, b, off, block.length);
	  return block.length;
  }
  
  /* @see IRandomAccess#seek(long) */
  public void seek(long pos) throws IOException {
	  fp = pos;
  }

  /* @see IRandomAccess#setLength(long) */
  public void setLength(long newLength) throws IOException {
	  // FIXME: No-op.
	  System.err.println("WARNING: setLength() is a no-op.");
  }

  // -- DataInput API methods --

  /* @see java.io.DataInput#readBoolean() */
  public boolean readBoolean() throws IOException {
	  return read(1).get() == 1? true : false;
  }

  /* @see java.io.DataInput#readByte() */
  public byte readByte() throws IOException {
	  return read(1).get();
  }

  /* @see java.io.DataInput#readChar() */
  public char readChar() throws IOException {
	  return read(1).getChar();
  }

  /* @see java.io.DataInput#readDouble() */
  public double readDouble() throws IOException {
	  return read(8).getDouble();
  }

  /* @see java.io.DataInput#readFloat() */
  public float readFloat() throws IOException {
	  return read(4).getFloat();
  }

  /* @see java.io.DataInput#readFully(byte[]) */
  public void readFully(byte[] b) throws IOException {
	  read(b);
  }

  /* @see java.io.DataInput#readFully(byte[], int, int) */
  public void readFully(byte[] b, int off, int len) throws IOException {
	  read(b, off, len);
  }

  /* @see java.io.DataInput#readInt() */
  public int readInt() throws IOException {
	  return read(4).getInt();
  }

  /* @see java.io.DataInput#readLine() */
  public String readLine() throws IOException {
    throw new IOException("Unimplemented");
  }

  /* @see java.io.DataInput#readLong() */
  public long readLong() throws IOException {
	  return read(8).getLong();
  }

  /* @see java.io.DataInput#readShort() */
  public short readShort() throws IOException {
	  return read(2).getShort();
  }

  /* @see java.io.DataInput#readUnsignedByte() */
  public int readUnsignedByte() throws IOException {
	  return read(1).get() & 0xFF;
  }

  /* @see java.io.DataInput#readUnsignedShort() */
  public int readUnsignedShort() throws IOException {
	  return read(2).getShort() & 0xFFFF;
  }

  /* @see java.io.DataInput#readUTF() */
  public String readUTF() throws IOException {
	  throw new IOException("Unimplemented");
  }

  /* @see java.io.DataInput#skipBytes(int) */
  public int skipBytes(int n) throws IOException {
	  throw new IOException("Unimplemented");
  }

  // -- DataOutput API methods --

  /* @see java.io.DataOutput#write(byte[]) */
  public void write(byte[] b) throws IOException {
	  throw new IOException("Unimplemented");
  }

  /* @see java.io.DataOutput#write(byte[], int, int) */
  public void write(byte[] b, int off, int len) throws IOException {
	  throw new IOException("Unimplemented");
  }

  /* @see java.io.DataOutput#write(int b) */
  public void write(int b) throws IOException {
	  throw new IOException("Unimplemented");
  }

  /* @see java.io.DataOutput#writeBoolean(boolean) */
  public void writeBoolean(boolean v) throws IOException {
	  throw new IOException("Unimplemented");
  }

  /* @see java.io.DataOutput#writeByte(int) */
  public void writeByte(int v) throws IOException {
	  throw new IOException("Unimplemented");
  }

  /* @see java.io.DataOutput#writeBytes(String) */
  public void writeBytes(String s) throws IOException {
	  throw new IOException("Unimplemented");
  }

  /* @see java.io.DataOutput#writeChar(int) */
  public void writeChar(int v) throws IOException {
	  throw new IOException("Unimplemented");
  }

  /* @see java.io.DataOutput#writeChars(String) */
  public void writeChars(String s) throws IOException {
	  throw new IOException("Unimplemented");
  }

  /* @see java.io.DataOutput#writeDouble(double) */
  public void writeDouble(double v) throws IOException {
	  throw new IOException("Unimplemented");
  }

  /* @see java.io.DataOutput#writeFloat(float) */
  public void writeFloat(float v) throws IOException {
	  throw new IOException("Unimplemented");
  }

  /* @see java.io.DataOutput#writeInt(int) */
  public void writeInt(int v) throws IOException {
	  throw new IOException("Unimplemented");
  }

  /* @see java.io.DataOutput#writeLong(long) */
  public void writeLong(long v) throws IOException {
	  throw new IOException("Unimplemented");
  }

  /* @see java.io.DataOutput#writeShort(int) */
  public void writeShort(int v) throws IOException {
	  throw new IOException("Unimplemented");
  }

  /* @see java.io.DataOutput#writeUTF(String) */
  public void writeUTF(String str) throws IOException {
	  throw new IOException("Unimplemented");
  }
}
