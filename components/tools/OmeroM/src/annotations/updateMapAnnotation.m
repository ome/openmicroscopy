function ma = updateMapAnnotation(session, ma, keyvalue, varargin)
% updateMapAnnotation will update the content (key-value pairs) of
% MapAnnotation ma while maintaining its ID.
%
% SYNTAX
% ma = updateMapAnnotation(session, ma, keyvalue)
% ma = updateMapAnnotation(____,'Param',value)
%
% INPUT ARGUMENTS
% session     omero.api.ServiceFactoryPrxHelper object
%
% ma          MapAnnotationI object
%
% keyvalue    cell array of characters | string array
%             The number of columns must be 2. The first colum is for keys
%             and the second column is for values.
%
% OPTIONAL PARA<ETER/VALUE PAIRS
% 'namespace' char
%             Namespace for the MapAnnotation. If you specify this, the
%             value of 'iseditable' will be ignored.
%
% 'description'
%             char
%             Description for the MapAnnotation. 
%
% 'group'     a positive integer
%             group ID
%
% 'iseditable' 
%             false (default) | true | 0 | 1 
%             If true or 1, MapAnnotation (Key-Value Pairs) will
%             be editable via GUI (OMERO.web or OMERO.insight). If you
%             specify 'namespace', 'iseditable' will be ignored.
%
%
% OUTPUT ARGUMENTS
% ma          MapAnnotationI object
%
%
% Written by Kouichi C. Nakamura Ph.D.
% MRC Brain Network Dynamics Unit
% University of Oxford
% kouichi.c.nakamura@gmail.com
% 05-Dec-2018 18:23:13
%
% See also
% writeMapAnnotation, strToMapAnnotation, mapAnnotationToCellstr

ip = inputParser;
ip.addRequired('session',@(x) isscalar(x));
ip.addRequired('ma',@(x) isa(x,'omero.model.MapAnnotationI'));
ip.addRequired('keyvalue',@(x) isempty(x) || size(x,2) == 2 && (iscellstr(x) || isstring (x)));
ip.addParameter('namespace', '', @ischar);
ip.addParameter('description', '', @ischar);
ip.addParameter('iseditable', false, @(x) isscalar(x) && x == 1 || x == 0);
ip.addParameter('group', [], @(x) isscalar(x) && isnumeric(x));
ip.parse(session, ma, keyvalue,varargin{:});


context = java.util.HashMap;
% Check if the Annotation exists on the server
try
    group = ma.getDetails().getGroup().getId().getValue();
    context.put('omero.group', java.lang.String(num2str(group)));
    if isempty(ip.Results.group)
         index = length(varargin);
         varargin{index + 1} = 'group';
         varargin{index + 2} = group;
    end
catch
end

% In case the MapAnnotation does not yet exist on the server:
if ~context.containsKey('omero.group') && ~isempty(ip.Results.group)
    context.put(...
        'omero.group', java.lang.String(num2str(ip.Results.group)));
end


if ~isempty(ip.Results.description)
    ma.setDescription(rstring(ip.Results.description));
end


if isempty(ip.Results.namespace)
    
    if ip.Results.iseditable
        %NOTE this is required to make it editable from GUI
        eval('import omero.constants.metadata.NSCLIENTMAPANNOTATION')
        ma.setNs(rstring(char(NSCLIENTMAPANNOTATION.value)));
    else
        ma.setNs(rstring(char('')));
    end
    
else
    ma.setNs(rstring(ip.Results.namespace))
end



%update the keys and values of the object

keys = cellstr(keyvalue(:,1));
values = cellstr(keyvalue(:,2));

nv = java.util.ArrayList();
for i = 1 : numel(keys)
    nv.add(omero.model.NamedValue(keys{i}, values{i}));
end
ma.setMapValue(nv);

ma = session.getUpdateService().saveAndReturnObject(...
    ma,context);
