function da = writeDoubleAnnotation(session, value, varargin)
% WRITEDOUBLEANNOTATION Create and upload a double annotation onto OMERO
%
%    da = writeDoubleAnnotation(session, value) creates and uploads an
%    double annotation of input value owned by the session user.
%
%    da = writeDoubleAnnotation(session, value, 'description',
%    description) also set the description of the annotation.
%
%    da = writeDoubleAnnotation(session, value, 'namespace', namespace)
%    also sets the namespace of the annotation.
%
%    Examples:
%
%        da = writeDoubleAnnotation(session, value)
%        da = writeDoubleAnnotation(session, value, 'description',
%        description)
%        da = writeDoubleAnnotation(session, value, 'namespace', namespace)
%
% See also: WRITECOMMENTANNOTATION, WRITEFILEANNOTATION,
% WRITELONGANNOTATION, WRITETAGANNOTATION, WRITETEXTANNOTATION,
% WRITETIMESTAMPANNOTATION, WRITEXMLANNOTATION

% Copyright (C) 2014-2015 University of Dundee & Open Microscopy Environment.
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
ip.addRequired('value', @isscalar);
ip.addParamValue('description', '', @ischar);
ip.addParamValue('namespace', '', @ischar);
ip.parse(session, value, varargin{:});

% Create double annotation
da = omero.model.DoubleAnnotationI;

% Set annotation properties
da.setDoubleValue(rdouble(value));
if ~isempty(ip.Results.description),
    da.setDescription(rstring(ip.Results.description));
end
if ~isempty(ip.Results.namespace),
    da.setNs(rstring(ip.Results.namespace));
end

% Upload and return the annotation
da = session.getUpdateService().saveAndReturnObject(da);
