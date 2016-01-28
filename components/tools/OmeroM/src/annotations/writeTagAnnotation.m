function ta = writeTagAnnotation(session, tag, varargin)
% WRITETAGANNOTATION Create and upload a tag annotation onto OMERO
%
%    ta = writeTagAnnotation(session, tag) creates and uploads a tag
%    annotation owned by the session user.
%
%    ta = writeTagAnnotation(session, tag, 'description', description) also
%    specifies the description of the tag annotation.
%
%    ta = writeTagAnnotation(session, tag, 'namespace', namespace)
%    also sets the namespace of the tag annotation.
%
%    ta = writeTagAnnotation(session, tag, 'group', groupid)
%    sets the group.
%
%    Examples:
%
%        ta = writeTagAnnotation(session, tag)
%        ta = writeTagAnnotation(session, tag, 'description', description)
%        ta = writeTagAnnotation(session, tag, 'namespace', namespace)
%
% See also: WRITECOMMENTANNOTATION, WRITEDOUBLEANNOTATION,
% WRITEFILEANNOTATION, WRITELONGANNOTATION, WRITETEXTANNOTATION,
% WRITETIMESTAMPANNOTATION, WRITEXMLANNOTATION

% Copyright (C) 2013-2015 University of Dundee & Open Microscopy Environment.
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
ta = writeTextAnnotation(session, 'tag', tag, varargin{:});