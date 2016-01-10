

(function(){
    var ThumbTable = React.createClass({

        parentTypes: ["dataset", "orphaned", "tag", "share"],

        renderNothing: function(selected) {
            if (selected.length === 0) {
                return true;
            }
            var dtype = selected[0].type;
            if (dtype === "image") {
                return false;
            }
            if (selected.length > 1 && dtype !== "image") {
                return true;
            }
            if (this.parentTypes.indexOf(dtype) === -1) {
                return true;
            }
        },

        getParentNode: function(dtype, selected, inst) {
            if (this.parentTypes.indexOf(dtype) > -1) {
                return selected[0];
            }
            if (dtype === "image") {
                return inst.get_node(inst.get_parent(selected[0]));
            }
        },

        // getNodeData: function(inst, parentNode) {
        //     imgNodes = [];
        //     parentNode.children.forEach(function(ch){
        //         var childNode = inst.get_node(ch);
        //         // Ignore non-images under tags or 'deleted' under shares
        //         if (childNode.type == "image") {
        //             imgNodes.push(childNode);
        //         }
        //     });


        // },

        // Most render nothing unless we've selected a Dataset or Image(s)
        render: function() {
            var selected = this.props.selected,
                inst = this.props.jstree,
                imgNodes = [],
                parentNode,
                dtype;

            if (this.renderNothing(selected)) {
                return (<span></span>);
            }

            dtype = selected[0].type;
            parentNode = this.getParentNode(dtype, selected, inst);

            if (dtype === "plate" || dtype === "acquisition") {
                return (<h1>Plate not supported yet</h1>);
            }

            return (
                <div>
                    <IconTableHeader />
                    <IconTable parentNode={parentNode} inst={inst} />
                    <IconTableFooter />
                </div>
            );
        }
    });


    var IconTableHeader = React.createClass({

        render: function() {
            return (
                <div className="toolbar iconTableHeader">
                    <div id="layout_chooser">
                        <button id="icon_layout" title="View as Thumbnails" className="checked"></button>
                        <button id="table_layout" title="View as List" className></button>
                    </div>
                    <form className="search filtersearch" id="filtersearch" action="#" style={{top: 4}}>
                        <div>
                            <label htmlFor="id_search"> Filter Images </label>
                            <input id="id_search" type="text" size={25} />
                        </div>
                        <span className="loading" style={{display: 'none'}} />
                    </form>
                </div>
            );
        }
    });

    var IconTableFooter = React.createClass({
        render: function() {
            return (
                <div className="toolbar iconTableFooter">
                    <div id="thumb_size_slider" title="Zoom Thumbnails" />
                </div>
            );
        }
    });


    var IconTableHeadRow = React.createClass({
        render: function() {
            return (
                <li className="thead"> 
                <div /> 
                <div className="sort-alpha">Name</div>
                <div className="sort-date">Date</div> 
                <div className="sort-numeric">Size X</div> 
                <div className="sort-numeric">Size Y</div> 
                <div className="sort-numeric">Size Z</div>
                </li>
            );
        }
    });


    var IconTable = React.createClass({

        render: function() {
            var parentNode = this.props.parentNode,
                inst = this.props.inst;
            var imgNodes = [];
            var dateFormatOptions = {
                weekday: "short", year: "numeric", month: "short",
                day: "numeric", hour: "2-digit", minute: "2-digit"
            };

            parentNode.children.forEach(function(ch){
                var childNode = inst.get_node(ch);
                // Ignore non-images under tags or 'deleted' under shares
                if (childNode.type == "image") {
                    imgNodes.push(childNode);
                }
            });

            var imgJson = [],
                selFileSets = [];
            // Convert jsTree nodes into json for template
            imgNodes.forEach(function(node){
                var d = node.data.obj.date || node.data.obj.acqDate;
                var date = new Date(d);
                date = date.toLocaleTimeString(undefined, dateFormatOptions);
                var iData = {'id': node.data.obj.id,
                        'name': node.text,
                        'data': JSON.parse(JSON.stringify(node.data)),
                        'selected': node.state.selected,
                        'date': date,
                    };
                // Note fileset IDs for selected images
                if (iData.selected) {
                    var fsId = node.data.obj.filesetId;
                    if (fsId) {
                        selFileSets.push(fsId);
                    }
                }
                // Thumb version: random to break cache if thumbnails are -1 'in progress'
                // or we're refresing 1 or all thumbnails
                // if (node.data.obj.thumbVersion != undefined ||
                //         event.type === "refreshThumbnails" ||
                //         event.type === "refreshThumb") {
                //     var thumbVersion = node.data.obj.thumbVersion;
                //     if (thumbVersion === -1 || event.type === "refreshThumbnails" || (
                //             event.type === "refreshThumb" && data.imageId === iData.id)) {
                //         thumbVersion = getRandom();
                //         // We cache this to prevent new thumbnails requested on every
                //         // selection change. Refreshing of tree will reset thumbVersion.
                //         node.data.obj.thumbVersion = thumbVersion;
                //     }
                //     iData.thumbVersion = thumbVersion;
                // }
                // If image is in share and share is not owned by user...
                if (node.data.obj.shareId && !parentNode.data.obj.isOwned) {
                    // share ID will be needed to open image viewer
                    iData.shareId = node.data.obj.shareId;
                }
                imgJson.push(iData);
            });

            // Now we know which filesets are selected, we can
            // go through all images, adding fs-selection flag if in
            if (selFileSets.length > 0) {
                imgJson.forEach(function(img){
                    if (selFileSets.indexOf(img.data.obj.filesetId) > -1) {
                        img.fsSelected = true;
                    }
                });
            }

            var icons = imgJson.map(function(image){
                var cls = [];
                if (image.selected) {cls.push('ui-selected')};
                if (image.fsSelected) {cls.push('fs-selected')};
                return (
                    <li className={"row " + cls.join(" ")}
                        key={image.id}
                        id={"image_icon-" + image.id}
                        data-fileset={image.data.obj.filesetId}
                        data-type="image"
                        data-id={image.id}
                        data-perms={image.data.obj.permsCss}
                        tabIndex={0}
                    >
                        <div className="image">
                            <img alt="image"
                                src={"render_thumbnail/size/96/" + image.id + "/?version=" + image.thumbVersion}
                                title={image.name}
                            />
                        </div>
                    </li>
                );
            })

            return (
                <div id="icon_table" className="iconTable">
                    <ul id="dataIcons" className="element_sorter iconLayout">
                        <IconTableHeadRow />
                        {icons}
                    </ul>
                </div>
            );
        }
    });


    window.OME.renderCentrePanel = function(jstree, selected) {
        ReactDOM.render(
            <ThumbTable 
                jstree={jstree}
                selected={selected}/>,
            document.getElementById('content_details')
        );
    };

})();
