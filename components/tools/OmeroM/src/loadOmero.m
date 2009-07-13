function client=loadOmero(varargin)

disp('');
disp('--------------------------');
disp('OmeroMatlab Toolbox ');
disp(omeroVersion);
disp('--------------------------');
disp('');

%
% Add the omero_client jar to the Java dynamic classpath
% This will allow the import omero.* statement to pass
% successfully.
%
OmeroClient_Jar = fullfile(findOmero, 'omero_client.jar');
javaaddpath(OmeroClient_Jar);
import omero.*;

% Also add the OmeroM directory and its subdirectories to the path
% so that functions and demos are available even if the user changes
% directories. See the unloadOmero function for how to remove these
% values.
addpath(genpath(findOmero)); % OmeroM and subdirectories


%
% Try to find a valid configuration file and use it to create an initial
% omero_client object.
%
% Either first in the ICE_CONFIG environment variable
client = 'Missing configuration; no omero.client created.';
ice_config = getenv('ICE_CONFIG');
if strcmp(ice_config, '')
  % Then in the current directory.
  if exist('ice.config','file')
      client = omero.client('ice.config');
  end
else
  % Using ICE_CONFIG variable
  client = omero.client();
end