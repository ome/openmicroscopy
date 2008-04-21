function [project] = mapProject(blitzProject)
if(~isempty(blitzProject.id))
	project.id = blitzProject.id.val;
end
if(~isempty(blitzProject.description))
	project.description = char(blitzProject.description.val);
end
if(~isempty(blitzProject.loaded))
	project.loaded = blitzProject.loaded;
end
if(~isempty(blitzProject.name))
	project.name = char(blitzProject.name.val);
end