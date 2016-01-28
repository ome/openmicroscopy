function ma = writeMapAnnotation(session, keys, values, varargin)
% WRITEMAPANNOTATION Create and upload a map annotation onto OMERO
%
%    ma = writeMapAnnotation(session, keys, values) creates and uploads a
%    map annotation owned by the session user containing one or multiple
%    key/value pair(s). The keys and values input must be either strings or
%    cell arrays of strings with the same number of elements.
%
%    ma = writeMapAnnotation(session, keys, values, 'description',
%    description) also specifies the description of the map annotation.
%
%    ma = writeMapAnnotation(session, keys, values, 'namespace', namespace)
%    also sets the namespace of the map annotation.
%
%    ma = writeMapAnnotation(session, keys, values, 'group', groupid)
%    sets the group.
%
%    Examples:
%
%        map = writeMapAnnotation(session, 'key', 'value');
%        map = writeMapAnnotation(session, {'key1', 'key2'}, {'value1', 'value2'});
%        map = writeMapAnnotation(session, 'key', 'value', 'description', description)
%        map = writeMapAnnotation(session, 'key', 'value', 'namespace', namespace)
%
% See also: WRITECOMMENTANNOTATION, WRITEDOUBLEANNOTATION,
% WRITEFILEANNOTATION, WRITELONGANNOTATION, WRITETAGANNOTATION,
% WRITETEXTANNOTATION, WRITETIMESTAMPANNOTATION, WRITEXMLANNOTATION

% Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
% All rights reserved.
%
% This program is free software; you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Foundation; either version 2 of the License, or
% (at your option) any later version.
%
% This program is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU General Public License for more details.
%
% You should have received a copy of the GNU General Public License along
% with this program; if not, write to the Free Software Foundation, Inc.,
% 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

% Input check
ip = inputParser;
ip.addRequired('session');
ip.addRequired('keys', @(x) ischar(x) || iscellstr(x));
ip.addRequired('values', @(x) ischar(x) || iscellstr(x));
ip.addParamValue('namespace', '', @ischar);
ip.addParamValue('description', '', @ischar);
ip.addParamValue('group', [], @(x) isscalar(x) && isnumeric(x));
ip.parse(session, keys, values, varargin{:});

% Convert keys and values into cell arrays
if ischar(keys), keys = {keys}; end
if ischar(values), values = {values}; end
assert(numel(keys) == numel(values),...
    'Keys and values input should have the same number of elements');

% Create java ArrayList of NamedValue objects
nv = java.util.ArrayList();
for i = 1 : numel(keys)
    nv.add(omero.model.NamedValue(keys{i}, values{i}));
end

% Create a map annotation
ma = omero.model.MapAnnotationI();
ma.setMapValue(nv);

if ~isempty(ip.Results.description),
    ma.setDescription(rstring(ip.Results.description));
end

if ~isempty(ip.Results.namespace),
    ma.setNs(rstring(ip.Results.namespace))
end

% Save the map annotation
context = java.util.HashMap;
if ~isempty(ip.Results.group)
    context.put(...
        'omero.group', java.lang.String(num2str(ip.Results.group)));
end
ma = session.getUpdateService().saveAndReturnObject(ma, context);
