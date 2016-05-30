
    // Example openwith script
    // TEMP - for evaluation of https://github.com/openmicroscopy/openmicroscopy/pull/4630
    // TODO: remove this before merging PR!
    var isEnabled = function(selected) {
        console.log('GenBank Protein, isEnabled()', selected);
        if (selected.length !== 1) return false;

        // return True if name is a number (E.g. Gene ID)
        var name = selected[0].data.obj.name;
        return (parseInt(name, 10) == name);
    };

    var handleAction = function(selected, url) {
        console.log('GenBank Protein, handleAction()', selected, url);
        var name = selected[0].data.obj.name;
        window.open(url + name, 'new');
    };


    OME.setOpenWithEnabledHandler("GenBank Protein", isEnabled);
    OME.setOpenWithActionHandler("GenBank Protein", handleAction);

