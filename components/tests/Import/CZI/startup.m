clear all;close all;

addpath(genpath(pwd));

inputdir=[pwd '/libs'];
jars1=dir(inputdir);

for i=3:length(jars1)
javaaddpath([inputdir '/' jars1(i).name]);
end




