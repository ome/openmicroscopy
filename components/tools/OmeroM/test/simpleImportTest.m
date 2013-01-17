function success=simpleImportTest(hostname,port,user,password)
% A simple example using the ImportLibrary
%
% Adapted from https://gist.github.com/1710116

% Input check
ip=inputParser;
ip.addRequired('hostname',@ischar)
ip.addRequired('port',@isscalar)
ip.addRequired('user',@ischar)
ip.addRequired('password',@ischar)
ip.parse(hostname,port,user,password);

% Add required jars
javaaddpath(which('slf4j-api.jar'));
javaaddpath(which('slf4j-log4j12.jar'));
javaaddpath(which('scifio.jar'));
javaaddpath(which('blitz.jar'));
javaaddpath(which('ini4j.jar'));
javaaddpath(which('omero_client.jar'));

% Add jar-fix for Matlab
if ismac
    java.lang.System.setProperty('java.util.prefs.PreferencesFactory','java.util.prefs.MacOSXPreferencesFactory');
end

% Load Omero and create ImportConfi object
loadOmero();
config = ome.formats.importer.ImportConfig();

% Set configuraiton
config.hostname.set(hostname);
config.username.set(user);
config.password.set(password);
config.group.set(java.lang.Long(1));

store = config.createStore();
reader = ome.formats.importer.OMEROWrapper(config);

library = ome.formats.importer.ImportLibrary(store, reader);
handler = ome.formats.importer.cli.ErrorHandler(config);
library.addObserver(ome.formats.importer.cli.LoggingImportMonitor());
candidates = ome.formats.importer.ImportCandidates(reader, [], handler);
success =library.importCandidates(config, candidates);

end