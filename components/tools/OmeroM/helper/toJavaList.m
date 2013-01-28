function [javaList] = toJavaList(matlabList, varargin)
% Convert a MATLAB vector into a Java ArrayList

% Check input
ip = inputParser;
ip.addRequired('matlabList', @(x) isvector(x) || isempty(x));
ip.addOptional('castFun', @(x) x, @(x) ischar(x) || isa(x, 'function_handle'));
ip.parse(matlabList, varargin{:})

% Read casting function
castFun = ip.Results.castFun;
if ischar(castFun), castFun = str2func(castFun); end

% Create Java list
javaList = java.util.ArrayList;
for i=1:length(matlabList)
    javaList.add(castFun(matlabList(i)));
end
