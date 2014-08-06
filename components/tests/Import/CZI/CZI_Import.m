%Init
clear all;close all;

try
    %Params
    timervar={'Projectno','Imageno','Datasetno','createProject','createStore','reader','handler','Candidates1','Candidates2','CreateDataset','ImportLibrary','addObserver','setMetadataOptions','logFactory','ImportCandidates'};
    folder_depth_bioformats=10;%Folder depth for bioformats to search and calculate the number of datasets
    importopt=1; %Inplace import =1;
    outputFolder = [pwd '/matlab_results'];
    mkdir(outputFolder)
    
    %Import Packages
    import loci.formats.in.DefaultMetadataOptions;
    import loci.formats.in.MetadataLevel;
    import loci.common.*;
    import ome.formats.OMEROMetadataStoreClient;
    import ome.formats.importer.*;
    import ome.formats.importer.ImportConfig;
    import ome.formats.importer.cli.ErrorHandler;
    import ome.formats.importer.cli.LoggingImportMonitor;
    import omero.model.Dataset;
    import omero.model.DatasetI;
    import ome.services.blitz.repo.*;
    import ome.formats.importer.transfers.*;
    import ome.formats.importer.transfers.SymlinkFileTransfer;
    import ome.formats.importer.cli.CommandLineImporter;
    import java.util.prefs.*;
    
    
    %Logging (switch on)
    loci.common.DebugTools.enableLogging('DEBUG');
    
    %Load Omero (Please make sure the server properties are updated in the
    %ice.config file within the matlab toolbox
    [client,session]=loadOmero('ice.config');
    client.enableKeepAlive(60);

    %Configuration Object
    config = ImportConfig();
    
    omeroProperties = client.getProperties().getPropertiesForPrefix('omero');
    props.hostname = char(omeroProperties.get('omero.host'));
    props.username = char(omeroProperties.get('omero.user'));
    props.password = char(omeroProperties.get('omero.pass'));
    props.port = str2double(omeroProperties.get('omero.port'));
    props.port = javaObject('java.lang.Integer',props.port);
    
    %Set Config params
    config.email.set('');
    config.sendFiles.set(true);
    config.sendReport.set(false);
    config.contOnError.set(false);
    config.debug.set(false);
    config.hostname.set(props.hostname);
    config.port.set(props.port);
    config.targetClass.set('omero.model.Dataset');
    config.sessionKey.set((session.getAdminService.getEventContext.sessionUuid));
    config.checksumAlgorithm.set('File-Size-64')
    
    %Metadatastore Object
    tic;store = handle(config.createStore());
    store.logVersionInfo(config.getIniVersionNumber());t2=toc;
    
    %CZI job paths (Column 1 : Ticket or PR number);
    czi_job={9398,'/ome/data_repo/from_skyking/zeiss-czi/greg';11204,'/ome/apache_repo/7405';12003,'/ome/data_repo/from_skyking/zeiss-czi/stephane/AO5.czi';11166,'/ome/data_repo/from_skyking/zeiss-czi/zeiss/zen-2012/ApoTome';11166,'/ome/data_repo/from_skyking/zeiss-czi/zeiss/zen-2012/CellObserver SD';11233,'/ome/apache_repo/7601';12189,'/ome/data_repo/from_Biozentrum_Basel/Charles/SPIM/large_timeseries_700GB/'};
    czi_job{10,1}=1123;%This is the PR number
    czi_job{10,2}='/ome/data_repo/from_Dresden/Pavel_Tomancak/Dmel';%Please make sure the .pattern file exists within this directory
    
    %Looped Import
    wierd_folders=[];spw=[];ImportStats=[];nameFolds={};
    for i=1:10
        
        %Projectname = nameFolds{i};
        Projectname = ['CZI_Review_' num2str(czi_job{i,1})];
        tic;reader = OMEROWrapper(config);t3=toc;
        tic;handler = handle(ErrorHandler(config));t4=toc;
        
        paths = [czi_job{i,2}];
        
        tic;candidates = handle(ImportCandidates(folder_depth_bioformats,reader, paths, handler));t5=toc;
        
        %Wierd Folder Errors
        if ((candidates.getContainers().size)==0)
            wierd_folders=[wierd_folders ; nameFolds(i)];
            continue
        end
        
        %     Check point 1 : to check if its a SWP(screen/well/plate format)
        check_spw = candidates.getContainers().get(0).getIsSPW();
        if check_spw.toString.matches('true')
            spw=[spw ; nameFolds(i)];
            continue
        end
        
        datasetno=candidates.getContainers().size;
        timevec=[];errorvec={};
        
        %Create a project
        tic;project = handle(createProject(session, Projectname));
        projectID=project.getId.getValue();t1=toc;
        
        %Close reader object
        reader.close();
        
        %calculate the files for import
        getfiles=[];dataID=[];datasetvec=[];
        for j=1:datasetno
            
            getfiles=[getfiles ; cell(candidates.getContainers().get(j-1).getUsedFiles)]; %#ok<*AGROW>
            getfile=char(candidates.getContainers.get(j-1).getFile);
            idx1 = strfind(getfile,'/');
            DatasetName = strdiff(char(getfile(1:idx1(end))),[paths '/']);%Slash (/) format for mac as of now, can be generalized later.
            
            if isempty(DatasetName)
                DatasetName = Projectname;
            end
            
            if j>1 && ~isempty(intersect(DatasetName,datasetvec(:,1)))
                idx1=strmatch(DatasetName, datasetvec(:,1),'exact');
                dataID= datasetvec{idx1,2};
            else
                tic;dataset = handle(createDataset(session, DatasetName, projectID));t6=toc;
                dataID = javaObject('java.lang.Long',dataset.getId().getValue());
            end
            
            datasetvec=[datasetvec ; {DatasetName} {dataID}];
            nameFolds = [nameFolds ; DatasetName];
            
            %Library Object
            tic;
            if importopt == 1
                library = handle(ImportLibrary(store, reader, SymlinkFileTransfer));
            else
                library = handle(ImportLibrary(store, reader));
            end;t7=toc;
            
            tic;library.addObserver(LoggingImportMonitor());t8=toc;
            tic;reader.setMetadataOptions(DefaultMetadataOptions(MetadataLevel.ALL));t9=toc;
            
            tic;log = org.apache.commons.logging.LogFactory.getLog('ome.formats.importer.ImportLibrary');t10=toc;
            config.targetId.set(dataID);
            
            %reinitialize reader object post setting dataset ID
            tic;reader = OMEROWrapper(config);t3=toc;
            tic;candidates_specific= handle(ImportCandidates(folder_depth_bioformats,reader, char(candidates.getContainers.get(j-1).getFile), handler));t12=toc;
            
            tic;success = library.importCandidates(config, candidates_specific);t11=toc;
            reader.close();
            timevec=[timevec ; i j datasetno t1 t2 t3 t4 t5 t12 t6 t7 t8 t9 t10 t11];
            
        end
        
        ImportStats=[ImportStats ; timevec];
        
        
    end
    store.logout();store.closeServices();
    time1=clock;
    DatasetName=[date '_' num2str(time1(4)) '_' num2str(time1(5)) '_ImportStats.mat'];
    save([outputFolder '/' DatasetName],'ImportStats','nameFolds','spw','wierd_folders','timervar');
    
    %Check a project named Matlab_Result
    projects = getProjects(session);
    for j=1:numel(projects)
        pjname=char(projects(j).getName.getValue());
        if ~isempty(strmatch(pjname,'Import_Statistics_CZI','exact'))
            project=projects(j);
            break
        end
    end
    
    if j==numel(projects)
        project = handle(createProject(session, 'Import_Statistics_CZI'));
    end
    
    %Create a dataset and annotate the matlab mat file to the dataset (The
    %dataset name is the current time).
    dataset = handle(createDataset(session, DatasetName, project.getId.getValue()));
    dataID = dataset.getId().getValue();
    fileAnnotation = writeFileAnnotation(session, [outputFolder '/' DatasetName]);
    link = linkAnnotation(session, fileAnnotation, 'dataset',dataID);
    
    delete([outputFolder '/' DatasetName]);
    %Logout and close session
    store.logout;
    client.closeSession();
    pause(3);
    
    %Load Omero and upload results
    [client,session]=loadOmero('ice.config');
    client.enableKeepAlive(60);
    config.targetId.set(dataID);
    reader = OMEROWrapper(config);
    store = handle(config.createStore());
    store.logVersionInfo(config.getIniVersionNumber());
    handler = handle(ErrorHandler(config));
    library = handle(ImportLibrary(store, reader));
    
    %Plot results and attach to the dataset
    pid=unique(ImportStats(:,1));
    for i=1:length(pid)
        
        idx1=find(ImportStats(:,1)==pid(i));
        plotvec=ImportStats(idx1,:);totval=(plotvec(:,4:end));totval=sum(totval(:));
        bar(plotvec(:,4:end));xlabel('Individual Objects');ylabel('Time(secs)');title(['Total Time Taken : ' num2str(totval) ' Seconds'])
        
        strreplace=findstr('/',nameFolds{i});nameFolds{i}(strreplace)='_';
        saveas(gca,[outputFolder '/' nameFolds{i}(1:end) '.jpeg']) ;
        close;
        
        dataID=javaObject('java.lang.Long',dataset.getId().getValue());
        config.targetId.set(dataID);
        candidates_specific= (ImportCandidates(folder_depth_bioformats,reader, [outputFolder '/' nameFolds{i}(1:end) '.jpeg'], handler));
        success = library.importCandidates(config, candidates_specific);
        delete([outputFolder '/' nameFolds{i}(1:end) '.jpeg']);
    end
    
    store.logout();store.closeServices();reader.close();
    
catch err
    client.closeSession();
    throw(err);
end

client.closeSession();







