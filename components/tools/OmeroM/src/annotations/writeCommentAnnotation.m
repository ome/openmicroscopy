function ca = writeCommentAnnotation(session, comment, varargin)
% WRITECOMMENTANNOTATION Create and upload a comment annotation onto OMERO
%
%    ca = writeCommentAnnotation(session, comment) creates and uploads a
%    comment annotation owned by the session user.
%
%    ca = writeCommentAnnotation(session, comment, 'description',
%    description) also specifies the description of the comment annotation.
%
%    ca = writeCommentAnnotation(session, comment, 'namespace', namespace)
%    also sets the namespace of the comment annotation.
%
%    Examples:
%
%        ca = writeCommentAnnotation(session, comment)
%        ca = writeCommentAnnotation(session, comment, 'description', description)
%        ca = writeCommentAnnotation(session, comment, 'namespace', namespace)
%
% See also: WRITEDOUBLEANNOTATION, WRITEFILEANNOTATION,
% WRITELONGANNOTATION, WRITETAGANNOTATION, WRITETEXTANNOTATION,
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
ca = writeTextAnnotation(session, 'comment', comment, varargin{:});