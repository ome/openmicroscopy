function upload_image(path,session,username,password,ProjectName,DatasetName,host,importopt)


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

%Choose a dataset name, will be assigned to your imported dataset under the root user.
DataForImport = path;%Source Directory

%Logging (switch on)
loci.common.DebugTools.enableLogging('DEBUG');

%Configuration Object
config = ImportConfig();

%Set Config params
config.email.set('');
config.sendFiles.set(true);
config.sendReport.set(false);
config.contOnError.set(false);
config.debug.set(false);
config.hostname.set(host);


port = javaObject('java.lang.Integer',4064);
config.port.set(port);
config.username.set(username);
config.password.set(password);
config.targetClass.set('omero.model.Dataset');

%Check a project named ProjectName
projects = getProjects(session);
for j=1:numel(projects)
    pjname=char(projects(j).getName.getValue());
    if ~isempty(strmatch(pjname,ProjectName,'exact'))
        project=projects(j);
        break
    end
end

if j==numel(projects) 
    project = handle(createProject(session, ProjectName));
elseif isempty(projects)
    project = handle(createProject(session, ProjectName));
end

%Check datasetList under the Project to see if there are datasets with
%similar name
datasetsList = project.linkedDatasetList;
for i = 0:datasetsList.size()-1,
    d = datasetsList.get(i);
    dname = char(d.getName.getValue());
    
    if ~isempty(strmatch(dname,DatasetName,'exact'))
        dataset=d;
        break
    end
end

if isempty(datasetsList) || datasetsList.size()==0
    dataset = handle(createDataset(session, DatasetName, project.getId.getValue()));
end

dataID = javaObject('java.lang.Long',dataset.getId().getValue());
config.targetId.set(dataID);

%Metadatastore Object
store = config.createStore();
store.logVersionInfo(config.getIniVersionNumber());
reader = OMEROWrapper(config);

%Library Object
if importopt == 1
    library = handle(ImportLibrary(store, reader, SymlinkFileTransfer));
else
    library = handle(ImportLibrary(store, reader));
end

handler = ErrorHandler(config);
library.addObserver(LoggingImportMonitor());

%Import
paths = DataForImport;
candidates = ImportCandidates(reader, paths, handler);
reader.setMetadataOptions(DefaultMetadataOptions(MetadataLevel.ALL));
success = library.importCandidates(config, candidates);
if success == 0
    log = org.apache.commons.logging.LogFactory.getLog('ome.formats.importer.ImportLibrary');
    templog=log.setLevel(0);
end

%Logout and close session
store.logout();
