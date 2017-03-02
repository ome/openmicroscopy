
// openwith.js


// This example 'enabledHandler' code is not needed since we configure
// {'supported_objects': ['image']} to only enable the viewer when a
// single image is selected.
// However, it can be used if you need more flexibility to set enable/disable
// status of your Open with option.
// OME.setOpenWithEnabledHandler("Image viewer", function(selected) {
//     // selected is a list of {'id':1, 'name': 'test.tiff', 'type': 'image'}
//     // Only enabled for single objects...
//     if (selected.length !== 1) return false;
//     // Only enable for images
// 	   return (selected[0].type !== 'image') return false;
// });


// We have already configured the base url to be 'webindex' /webclient/ so
// we just need to add 'img_detail/' and the selected image ID
OME.setOpenWithUrlProvider("Image viewer", function(selected, url) {
    url += "img_detail/" + selected[0].id + "/";
    return url;
});
