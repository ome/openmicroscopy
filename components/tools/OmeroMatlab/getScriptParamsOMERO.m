function [scriptParams] = getScriptParams(serviceFactory, scriptId)

scriptParams = serviceFactory.getScriptParams(scriptId);
%scripts = toMatlabMap(map);