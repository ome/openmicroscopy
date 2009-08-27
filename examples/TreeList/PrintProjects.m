function PrintProjects(projects)
if (projects.size()==0)
    return;
end;
for i=0:projects.size()-1,
    project = projects.get(i);
    disp(project.getName().getValue());
    links = project.copyDatasetLinks();
    if (links.size()==0)
        return
    end
    for j=0:links.size()-1,
        pdl = links.get(j);
        dataset = pdl.getChild();
        disp(sprintf('  %s', char(dataset.getName().getValue())));
    end
end
