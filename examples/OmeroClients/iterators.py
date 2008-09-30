from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_DatasetImageLinkI import DatasetImageLinkI

image = ImageI()
dataset = DatasetI()
link = dataset.linkImage(image)

for dataset in image.iteratorDatasetLinks():
    dataset.getName();
