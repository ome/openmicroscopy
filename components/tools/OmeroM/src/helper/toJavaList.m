function [javaList] = toJavaList(matlabList, varargin)
% TOJAVALIST Convert a MATLAB array or cell array into a Java ArrayList
%
%    javaList = toJavaList(matlabList) converts a Matlab array or cell
%    array into a java.util.ArrayList.
%
%    javaList = toJavaList(matlabList, castFun) converts a Matlab array or
%    cell array into a java.util.ArrayList and casts each element using the
%    input casting function/classname.
%
%    Examples:
%
%        javaList = toJavaList(matlabList)
%        javaList = toJavaList(matlabList, @java.lang.Long)
%        javaList = toJavaList(matlabList, 'java.lang.Long')
%
% See also: TOMATLABLIST

% Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
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

% Check input
ip = inputParser;
ip.addRequired('matlabList', @(x) isvector(x) || iscellstr(x) || isempty(x) || ischar(x));
ip.addOptional('castFun', @(x) x, @(x) ischar(x) || isa(x, 'function_handle'));
ip.parse(matlabList, varargin{:})

if ischar(matlabList), matlabList = {matlabList}; end

% Read casting function
castFun = ip.Results.castFun;
if ischar(castFun), castFun = str2func(castFun); end

% Create Java list
javaList = java.util.ArrayList;
for i=1:length(matlabList)
    if iscell(matlabList),
        javaList.add(castFun(matlabList{i}));
    else
        javaList.add(castFun(matlabList(i)));
    end
end
