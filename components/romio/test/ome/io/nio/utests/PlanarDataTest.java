/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio.utests;

import static org.testng.AssertJUnit.*;

import java.nio.ByteBuffer;

import org.testng.annotations.*;

import ome.io.nio.RomioPixelBuffer;
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;
import ome.util.checksum.ChecksumProviderFactory;
import ome.util.checksum.ChecksumProviderFactoryImpl;
import ome.util.checksum.ChecksumType;

/**
 * This tests planar and sub-planar data retrieval using a known good ROMIO
 * binary repository data file. The data file can be downloaded at:
 * <a href="http://users.openmicroscopy.org.uk/~callan/100.bz2"/> and its 
 * decompressed MD5 digest is <code>4034ae78a03d74fd27c80681cdf6a695</code>.
 * 
 * @author Chris Allan <callan@glencoesoftware.com>
 *
 */
public class PlanarDataTest
{
	/** Imported tinyTest.d3d.dv binary repository file. */
	private static final String PIXELS_PATH = "/OMERO/Pixels/100";

	/** Imported tinyTest.d3d.dv pixels ID. */
	private static final long PIXELS_ID = 100L;

	private final ChecksumProviderFactory cpf = new ChecksumProviderFactoryImpl();

	private RomioPixelBuffer getRomioPixelBuffer()
	{
		PixelsType pType = new PixelsType();
		pType.setId(1L);
		pType.setValue("int16");
		Pixels p = new Pixels();
		p.setId(PIXELS_ID);
		p.setSizeX(20);
		p.setSizeY(20);
		p.setSizeZ(5);
		p.setSizeC(1);
		p.setSizeT(6);
		p.setPixelsType(pType);
		
    	RomioPixelBuffer buffer = new RomioPixelBuffer(PIXELS_PATH, p);
    	return buffer;
	}

	@Test(groups={"manual"})
    public void testFirstPlaneSecondTimepointFirstChannelMd5()
		throws Exception
    {
    	RomioPixelBuffer buffer = getRomioPixelBuffer();
    	ByteBuffer buf = buffer.getPlane(0, 0, 1).getData();
    	String md = cpf.getProvider(ChecksumType.MD5).putBytes(buf).checksumAsString();
    	assertEquals("2d1c16c02bece26920ff04ff08985f5e", md);
    }

	@Test(groups={"manual"})
    public void testFirstPlaneSecondTimepointFirstChannelFirstEightPixelsMd5()
		throws Exception
    {
    	RomioPixelBuffer buffer = getRomioPixelBuffer();
    	byte[] buf = new byte[16];
    	buffer.getPlaneRegionDirect(0, 0, 1, 8, 0, buf);
    	String md = cpf.getProvider(ChecksumType.MD5).putBytes(buf).checksumAsString();
    	assertEquals("505c12f3149129adf250ae96af159ea1", md);
    }

	@Test(groups={"manual"})
    public void testFirstPlaneSecondTimepointFirstChannelSecondEightPixelsMd5()
		throws Exception
    {
    	RomioPixelBuffer buffer = getRomioPixelBuffer();
    	byte[] buf = new byte[16];
    	buffer.getPlaneRegionDirect(0, 0, 1, 8, 8, buf);
    	String md = cpf.getProvider(ChecksumType.MD5).putBytes(buf).checksumAsString();
    	assertEquals("ed6a8ba38c61808d5790419c7a33839c", md);
    }

	@Test(groups={"manual"})
    public void testFirstPlaneSecondTimepointFirstChannelLastEightPixelsMd5()
		throws Exception
    {
    	RomioPixelBuffer buffer = getRomioPixelBuffer();
    	byte[] buf = new byte[16];
    	buffer.getPlaneRegionDirect(0, 0, 1, 8, 392, buf);
    	String md = cpf.getProvider(ChecksumType.MD5).putBytes(buf).checksumAsString();
    	assertEquals("ab1786af4395c09f52de23d710e37a7f", md);
    }
}
