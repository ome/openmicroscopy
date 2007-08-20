/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.service.utests;

import ome.api.ThumbnailStore;
import ome.services.blitz.util.IceMethodInvoker;
import ome.system.EventContext;
import ome.system.OmeroContext;
import omero.util.IceMapper;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

@Test
public class IceMethodInvokerUnitTest extends MockObjectTestCase {

	IceMethodInvoker invoker;
	Mock tbMock;
	Destroyable tb;
	IceMapper mapper;
	Ice.Current current;

	@Override
	@Configuration(beforeTestMethod = true)
	protected void setUp() throws Exception {
		tb = new Destroyable();
		invoker = new IceMethodInvoker(ThumbnailStore.class, 
				new OmeroContext("classpath:ome/testing/empty.xml"));
		mapper = new IceMapper();
		current = new Ice.Current();
		current.operation = "close";
		current.id = Ice.Util.stringToIdentity("test");
	}

	@Test
	void testAllCallsOnCloseAlsoCallDestroy() throws Exception {
		invoker.invoke(tb, current, mapper);
		assertTrue(tb.toString(), tb.closed == 1);
		assertTrue(tb.toString(), tb.destroyed == 1);
	}

	public static class Destroyable implements ThumbnailStore {

		@Override
		public String toString() {
			return String
					.format("%d closes and %d destroys", closed, destroyed);
		}

		int destroyed = 0;
		int closed = 0;

		public void destroy() {
			destroyed++;
		}

		public void close() {
			closed++;
		}

		public void createThumbnail(Integer sizeX, Integer sizeY) {
		}

		public void createThumbnails() {
		}

		public byte[] getThumbnail(Integer sizeX, Integer sizeY) {
			return null;
		}

		public byte[] getThumbnailByLongestSide(Integer size) {
			return null;
		}

		public byte[] getThumbnailByLongestSideDirect(Integer size) {
			return null;
		}

		public byte[] getThumbnailDirect(Integer sizeX, Integer sizeY) {
			return null;
		}

		public void resetDefaults() {
		}

		public boolean setPixelsId(long pixelsId) {
			return false;
		}

		public void setRenderingDefId(Long renderingDefId) {
		}

		public boolean thumbnailExists(Integer sizeX, Integer sizeY) {
			return false;
		}

		public EventContext getCurrentEventContext() {
			return null;
		}

	}

}