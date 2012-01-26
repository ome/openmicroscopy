function [client,session,gateway]=loadOmero(varargin)
% Add OMERO to the MATLAB path and javaclasspath, and optionally login.
% loadOmero specifies the directory of the current method as an
% OmeroMatlab toolbox installation, and adds it to the path and the
% dynamic javaclasspath. (If you have already specified an OMERO jar on
% your static classpath via classpath.txt, it will take priority. Please
% remove it to use loadOmero)
%
% VARARGIN USAGE: any arguments passed to loadOmero will be passed
% to the omero.client constructor IF CALLED. (See VARARGOUT section
% below). Therefore parameters should be of the right type and in
% the right order for the existing Java constructors. Alternatively,
% omero.client can be used after the call to loadOmero.
%
%   % Use ICE_CONFIG or ice.config in local directory
%   c = loadOmero;
%
%   -- If any parameters are speficed, the ice.config in the local
%   directory will not be used, and will have to be passed as below.
%
%   % Just the host string
%   c = loadOmero('localhost');
%
%   % Host string and port
%   c = loadOmero('localhost,'14064');
%
%   % Via a Properties object
%   p = java.util.Properties();
%   p.setProperty('omero.host', 'example.com');
%   p.setProperty('omero.user', 'me');
%   p.setProperty('omero.pass', 'super_secret!');
%   c = loadOmero(p);
%
%   % Via File array
%   fs = javaArray('java.io.File', 2)
%   fs(1) = java.io.File('ice.config');
%   fs(2) = java.io.File('personal.config'); % later files win.
%   c = loadOmero(fs);
%
%   % Or using omero.client directly
%   loadOmero;                                 % No constructor called
%   c = omero.client('localhost');             % Like examples above
%   s = c.createSession('user','password');
%   g = s.createGateway();
%
%
% VARARGOUT USAGE: If return values are specified, then loadOmero will
% perform some initialization steps, possibly logging the user in to
% an OMERO Server. See the 'Configuration' section of the OmeroMatlab
% documentation for more information.
%
%   % No omero.client created.
%   loadOmero;
%
%   % Call omero.client constructor and return. No session created.
%   client = loadOmero;
%
%   % Call omero.client and then createSession and return both. No gateway created.
%   [client, session] = loadOmero;
%
%   % Call omero.client, createSession, and createGateway and return.
%   [client, session, gateway] = loadOmero;
%

disp('');
disp('--------------------------');
disp('OmeroMatlab Toolbox ');
disp(omeroVersion);
disp('--------------------------');
disp('');


% Check if "omero.client" is already on the classpath, if not
% then add the omero_client.jar to the javaclasspath.
if exist('omero.client','class') == 0

    % Add the omero_client jar to the Java dynamic classpath
    % This will allow the import omero.* statement to pass
    % successfully.
    OmeroClient_Jar = fullfile(findOmero, 'omero_client.jar');
    javaaddpath(OmeroClient_Jar);
    import omero.*;

    % Also add the OmeroM directory and its subdirectories to the path
    % so that functions and demos are available even if the user changes
    % directories. See the unloadOmero function for how to remove these
    % values.
    addpath(genpath(findOmero)); % OmeroM and subdirectories

% If it does exist, then check that there aren't more than one
% version active.
else

    w = which('omeroVersion','-ALL');
    sz = size(w);
    sz = sz(1);
    if sz > 1
        warning('OMERO:loadOmero','More than one OMERO version found!');
        disp(char(w));
    end

end

%
% Try to find a valid configuration file and use it to create an initial
% omero_client object.
%
% Either first in the ICE_CONFIG environment variable
ice_config = getenv('ICE_CONFIG');
if strcmp(ice_config, '')
  % Then in the current directory.
  if exist('ice.config','file')
      ice_config = fullfile('.', 'ice.config');
  elseif exist(fullfile(findOmero, 'ice.config'), 'file')
      ice_config = fullfile(findOmero, 'ice.config');
  end
else
    % Clearing the ice_config now, since it is available
    % in the environment, and Ice will pick it up
    % (assuming it was set before MATLAB started)
    ice_config = '';
end

% If one or more return values are specified, then load some useful
% objects and return them.
if (nargout >=1 )
    props = java.util.Properties();
    for n = 1:length(varargin)
        if strcmp(varargin{n}.class, 'java.util.Properties')
            props = varargin{n};
        end
    end

    % If we've found ice_config and there's no other
    % Ice.Config property set, then set it.
    if 0==strcmp(ice_config, '')
        if strcmp(char(props.getProperty('Ice.Config')), '')
            props.setProperty('Ice.Config', ice_config);
        end
    end

    % If no properties in varargins but ice_config set
    % then ice_config is not set. This is difficult to
    % handle because we don't know what's in the varargin
    % in order to pick the proper constructor (ticket:6892)

    if nargin > 0
        client = javaObject('omero.client', varargin{:});
    else
        client = javaObject('omero.client', p);
    end
end

if (nargout >= 2)
    session = client.createSession();
end

if (nargout >= 3)
    gateway = session.createGateway();
end
