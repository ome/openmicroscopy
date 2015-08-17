/*
 * ome.testing.MockServiceFactory
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.testing;

import ome.api.IAdmin;
import ome.api.IAnalysis;
import ome.api.IConfig;
import ome.api.IContainer;
import ome.api.IPixels;
import ome.api.IQuery;
import ome.api.ITypes;
import ome.api.IUpdate;
import ome.system.ServiceFactory;
import omeis.providers.re.RenderingEngine;

import org.jmock.Mock;

/**
 * <a href="http://jmock.org">JMock'ed</a> ServiceFactory whose public fields
 * can be set like:
 * 
 * <code>
 *   mockServiceFactory.mockUpdate = mock(IUpdate.class);
 * </code>
 * 
 * and later used like:
 * 
 * <code>
 *   mockServiceFactory.mockUpdate.expects( once() ).method( "saveObject" );
 * </code>
 * 
 * when the test-case subclasses {@link org.jmock.MockObjectTestCase}.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0
 * @since 3.0
 * @see org.jmock.MockObjectTestCase
 */
public class MockServiceFactory extends ServiceFactory {

    @Override
    protected String getDefaultContext() {
        return null;
    }

    public Mock mockAdmin = new Mock(IAdmin.class);

    @Override
    public IAdmin getAdminService() {
        return (IAdmin) mockAdmin.proxy();
    }

    public Mock mockAnalysis = new Mock(IAnalysis.class);

    @Override
    public IAnalysis getAnalysisService() {
        return (IAnalysis) mockAnalysis.proxy();
    }

    public Mock mockPixels = new Mock(IPixels.class);

    @Override
    public IPixels getPixelsService() {
        return (IPixels) mockPixels.proxy();
    }

    public Mock mockConfig = new Mock(IConfig.class);

    @Override
    public IConfig getConfigService() {
        return (IConfig) mockConfig.proxy();
    }
    
    public Mock mockContainer = new Mock(IContainer.class);

    @Override
    public IContainer getContainerService() {
        return (IContainer) mockContainer.proxy();
    }

    public Mock mockQuery = new Mock(IQuery.class);

    @Override
    public IQuery getQueryService() {
        return (IQuery) mockQuery.proxy();
    }

    public Mock mockRendering = new Mock(RenderingEngine.class);

    public RenderingEngine getRenderingService() {
        return (RenderingEngine) mockRendering.proxy();
    }

    public Mock mockUpdate = new Mock(IUpdate.class);

    @Override
    public IUpdate getUpdateService() {
        return (IUpdate) mockUpdate.proxy();
    }

    public Mock mockTypes = new Mock(ITypes.class);

    @Override
    public ITypes getTypesService() {
        return (ITypes) mockTypes.proxy();
    }

}
