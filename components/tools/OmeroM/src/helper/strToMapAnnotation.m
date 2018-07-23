function ma = strToMapAnnotation(session, str, varargin)
% strToMapAnnotation returns MapAnnotation object of OMERO from
% string array or cell array of strings
%
% SYNTAX
% ma = strToMapAnnotation(session,str)
% ma = strToMapAnnotation(session,str,iseditable)
%
% REQUIREMENTS
%
%   OMERO.matlab toolbox
%   https://docs.openmicroscopy.org/latest/omero/developers/Matlab.html
%
%   Before using this function, you need to run an equivalent of the
%   following command.
%
%     client = loadOmero('demo.openmicroscopy.org', 4064)
%
% INPUT ARGUMENTS
% session     omero.api.ServiceFactoryPrxHelper object
%
% str         string array | cell array of strings
%             Number of columns must be 2.
%
% iseditable  false (default) | true | 0 | 1 
%             (Optional) If true or 1, MapAnnotation (Key-Value Pairs) will
%             be editable via GUI (OMERO.web or OMERO.insight)
%
% OPTIONAL PARA<ETER/VALUE PAIRS
% 'description'
%             char
%             Description for the MapAnnotation
%
%
% OUTPUT ARGUMENTS
% ma          MapAnnotationI object
%             To link ma to an image in OMERO, identify image ID from OMERO
%             GUI and execute the following command
%     
%               client = loadOmero('demo.openmicroscopy.org', 4064)
%               session = client.createSession(username, password)
%
%               link1 = linkAnnotation(session, ma, 'image', imageID);
%
%               clear
%               unloadOmero
%
% Written by Kouichi C. Nakamura Ph.D.
% MRC Brain Network Dynamics Unit
% University of Oxford
% kouichi.c.nakamura@gmail.com
% 09-Jun-2018 15:20:17
%
% See also
% writeMapAnnotation, omero_xlsIHC2MapAnnotation, linkAnnotation

p = inputParser;
p.addRequired('session',@(x) isscalar(x));

if verLessThan('matlab','9.1.0')
    p.addRequired('str',@(x) (size(str,2) ==2 || size(str,2) == 3) ...
        && iscellstr(x));  %#ok<ISCLSTR>
else
    p.addRequired('str',@(x) (size(str,2) ==2 || size(str,2) == 3) ...
        && iscellstr(x) || isstring(x) );
end
p.addOptional('iseditable',false,@(x) isscalar(x) && x == 1 || x == 0);
p.addParameter('description', '', @ischar);

p.parse(session,str,varargin{:});

iseditable  = p.Results.iseditable;
description = p.Results.description;


%% Job

if ~verLessThan('matlab','9.1.0') && isstring(str) 
    
   str = cellstr(str);
    
end


if iseditable
    %NOTE this is required to make it editable from GUI
    eval('import omero.constants.metadata.NSCLIENTMAPANNOTATION')
    namespace = char(NSCLIENTMAPANNOTATION.value);
else
    namespace = '';
end


ma = writeMapAnnotation(session,...
    str(:,1),str(:,2),...
    'namespace',namespace,'description',description);


end

