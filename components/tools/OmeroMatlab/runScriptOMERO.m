function [scriptsReturn] = runScriptOMERO(serviceFactory, scriptId, scriptParams)

scriptsReturn = serviceFactory.runScript(scriptId, scriptParams);
%scripts = toMatlabMap(map);