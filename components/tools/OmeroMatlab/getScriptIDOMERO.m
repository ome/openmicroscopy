function [scripts] = getScriptIDOMERO(serviceFactory, scriptName)

scripts = serviceFactory.getScriptsID(scriptName);
%scripts = toMatlabMap(map);