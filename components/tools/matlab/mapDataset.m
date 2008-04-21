function [dataset] = mapDataset(blitzDataset)
if(~isempty(blitzDataset.id))
	dataset.id = blitzDataset.id.val;
end
if(~isempty(blitzDataset.description))
	dataset.description = char(blitzDataset.description.val);
end
if(~isempty(blitzDataset.loaded))
	dataset.loaded = blitzDataset.loaded;
end
if(~isempty(blitzDataset.name))
	dataset.name = char(blitzDataset.name.val);
end