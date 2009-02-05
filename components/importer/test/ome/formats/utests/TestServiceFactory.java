package ome.formats.utests;

import ome.api.IAdmin;
import ome.api.IAnalysis;
import ome.api.IConfig;
import ome.api.IDelete;
import ome.api.ILdap;
import ome.api.IPixels;
import ome.api.IContainer;
import ome.api.IProjection;
import ome.api.IQuery;
import ome.api.IRenderingSettings;
import ome.api.IRepositoryInfo;
import ome.api.IShare;
import ome.api.ITypes;
import ome.api.IUpdate;
import ome.api.RawFileStore;
import ome.api.RawPixelsStore;
import ome.api.Search;
import ome.api.ThumbnailStore;
import ome.system.OmeroContext;
import ome.system.ServiceFactory;
import omeis.providers.re.RenderingEngine;


public class TestServiceFactory extends ServiceFactory
{
    public TestServiceFactory()
    {
        super(new OmeroContext("haxxor.xml"));
        return;
    }

    @Override
    public RawFileStore createRawFileStore()
    {
        return null;
    }

    @Override
    public RawPixelsStore createRawPixelsStore()
    {
        return null;
    }

    @Override
    public RenderingEngine createRenderingEngine()
    {
        return null;
    }

    @Override
    public Search createSearchService()
    {
        return null;
    }

    @Override
    public ThumbnailStore createThumbnailService()
    {
        return null;
    }

    @Override
    public IAdmin getAdminService()
    {
        return null;
    }

    @Override
    public IAnalysis getAnalysisService()
    {
        return null;
    }

    @Override
    public IConfig getConfigService()
    {
        return null;
    }

    @Override
    public IDelete getDeleteService()
    {
        return null;
    }

    @Override
    public ILdap getLdapService()
    {
        return null;
    }

    @Override
    public IPixels getPixelsService()
    {
        return null;
    }

    @Override
    public IContainer getContainerService()
    {
        return null;
    }

    @Override
    public IProjection getProjectionService()
    {
        return null;
    }

    @Override
    public IQuery getQueryService()
    {
        return null;
    }

    @Override
    public IRenderingSettings getRenderingSettingsService()
    {
        return null;
    }

    @Override
    public IRepositoryInfo getRepositoryInfoService()
    {
        return null;
    }

    @Override
    public IShare getShareService()
    {
        return null;
    }

    @Override
    public ITypes getTypesService()
    {
        return null;
    }

    @Override
    public IUpdate getUpdateService()
    {
        return null;
    }
    
}
