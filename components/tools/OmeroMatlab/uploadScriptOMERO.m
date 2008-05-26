function [scriptID] = uploadScriptOMERO(serviceFactory, script)

scriptID = serviceFactory.uploadScript(script);
%scripts = toMatlabMap(map);