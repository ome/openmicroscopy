/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.blitz.repo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ome.io.nio.PixelsService;
import ome.io.nio.TileSizes;
import ome.services.blitz.fire.Registry;
import ome.services.blitz.fire.Ring;
import ome.system.OmeroContext;

import ome.formats.importer.ImportConfig;
import ome.formats.importer.OMEROWrapper;

import omero.util.Resources;


/**
 * Requests which are handled by the repository servants.
 */
public class RequestObjectFactoryRegistry extends
        omero.util.ObjectFactoryRegistry implements ApplicationContextAware {

    private final Registry reg;

    private final TileSizes sizes;

    private final RepositoryDao dao;

    private final Ring ring;

    private final PixelsService pixels;

    private final Resources resources;

    private/* final */OmeroContext ctx;

    public RequestObjectFactoryRegistry(Registry reg, TileSizes sizes,
            RepositoryDao repositoryDao, Ring ring,
            PixelsService pixels) {
        this(reg, sizes, repositoryDao, ring, pixels, null);
    }
    public RequestObjectFactoryRegistry(Registry reg, TileSizes sizes,
            RepositoryDao repositoryDao, Ring ring,
            PixelsService pixels, Resources resources) {
        this.reg = reg;
        this.sizes = sizes;
        this.dao = repositoryDao;
        this.ring = ring;
        this.pixels = pixels;
        this.resources = resources;
    }

    public void setApplicationContext(ApplicationContext ctx)
            throws BeansException {
        this.ctx = (OmeroContext) ctx;
    }

    public Map<String, ObjectFactory> createFactories(Ice.Communicator ic) {
        Map<String, ObjectFactory> factories = new HashMap<String, ObjectFactory>();
        factories.put(ManagedImportRequestI.ice_staticId(), new ObjectFactory(
                ManagedImportRequestI.ice_staticId()) {
            @Override
            public Ice.Object create(String name) {
                ManagedImportRequestI mir = new ManagedImportRequestI(reg, sizes, dao,
                        new OMEROWrapper(
                                new ImportConfig(),
                                pixels.getMemoizerWait(),
                                pixels.getMemoizerDirectory()),
                        ring.uuid);
                mir.setResources(resources);
                return mir;
            }

        });
        factories.put(RawAccessRequestI.ice_staticId(), new ObjectFactory(
                RawAccessRequestI.ice_staticId()) {
            @Override
            public Ice.Object create(String name) {
                return new RawAccessRequestI(reg);
            }

        });
        return factories;
    }

}
