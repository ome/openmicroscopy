

(function(){
    var CentrePanel = React.createClass({

        parentTypes: ["dataset", "orphaned", "tag", "share", "plate", "acquisition"],

        getInitialState: function() {
            return {
                iconSize: 65,
            };
        },

        setIconSize: function(size) {
            this.setState({iconSize: size});
        },

        renderNothing: function(selected) {
            if (selected.length === 0) {
                if (this.previousParent) {
                    return false;
                }
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

        componentWillReceiveProps: function(nextProps) {
            // When props change...
            // If nothing is selected AND the previous node is valid
            // We continue to render that node (Dataset)
            if (nextProps.selected.length !== 0) {
                delete(this.previousParent);
            }
        },

        getParentNode: function(selected, inst) {
            if (this.renderNothing(selected)) {
                return;
            }
            if (selected.length === 0 && this.previousParent) {
                return this.previousParent;
            }
            var dtype = selected[0].type;
            if (this.parentTypes.indexOf(dtype) > -1) {
                return selected[0];
            }
            if (dtype === "image") {
                return inst.get_node(inst.get_parent(selected[0]));
            }
        },

        // Most render nothing unless we've selected a Dataset or Image(s)
        render: function() {
            var selected = this.props.selected,
                inst = this.props.jstree,
                imgNodes = [],
                dtype;

            var iconTable;

            var parentNode = this.getParentNode(selected, inst);

            if (parentNode) {

                dtype = parentNode.type;

                if (dtype === "plate" || dtype === "acquisition") {
                    var plateId = parentNode.data.id;
                    if (dtype === "acquisition") {
                        plateId = inst.get_node(inst.get_parent(parentNode)).data.id;
                    }
                    iconTable = (
                        <ReactPlate
                            plateId={plateId}
                            iconSize={this.state.iconSize}
                            parentNode={parentNode} />
                    )
                } else {
                    // handles tag, orphaned, dataset, share
                    // Cache this parentNode. If next selection == 0, still show this
                    // E.g. if image in Dataset is de-selected
                    this.previousParent = parentNode;
                    iconTable = (
                        <IconTable
                            parentNode={parentNode}
                            inst={inst}
                            filterText={this.state.filterText}
                            setThumbsToDeselect={this.setThumbsToDeselect}
                            iconSize={this.state.iconSize}
                            layout={this.state.layout} />
                    )
                }
            }

            return (
                <div>
                    
                    {iconTable}
                    <IconTableFooter
                        iconSize={this.state.iconSize}
                        setIconSize={this.setIconSize} />
                </div>
            );
        }
    });


    var IconTableHeader = React.createClass({

        handleLayoutClick: function(event) {
            var layout = event.target.getAttribute('data-layout');
            this.props.setLayout(layout);
        },

        handleFilterChange: function(event) {
            var filterText = event.target.value;
            this.props.setFilterText(filterText);
        },

        render: function() {
            var layout = this.props.layout,
                filterText = this.props.filterText;
            var iconBtnClass = layout === "icon" ? "checked" : "",
                tableBtnClass = layout === "table" ? "checked" : "";
            return (
                <div className="toolbar iconTableHeader">
                    <div id="layout_chooser">
                        <button
                            onClick={this.handleLayoutClick}
                            id="icon_layout"
                            title="View as Thumbnails"
                            data-layout="icon"
                            className={iconBtnClass} />
                        <button
                            onClick={this.handleLayoutClick}
                            id="table_layout"
                            title="View as List"
                            data-layout="table"
                            className={tableBtnClass} />
                    </div>
                    <form className="search filtersearch" id="filtersearch" action="#" style={{top: 4}}>
                        <div>
                            <input
                                id="id_search"
                                type="text"
                                placeholder="Filter Images"
                                onKeyUp={this.handleFilterChange}
                                size={25} />
                        </div>
                        <span className="loading" style={{display: 'none'}} />
                    </form>
                </div>
            );
        }
    });

    var IconTableFooter = React.createClass({

        componentDidMount: function() {
            var setIconSize = this.props.setIconSize,
                iconSize = this.props.iconSize;
            $(this.refs.thumbSlider).slider({
                max: 200,
                min: 30,
                value: iconSize,
                slide: function(event, ui) {
                    setIconSize(ui.value);
                }
            });
        },

        componentWillUnmount: function() {
            // cleanup plugin
            $(this.refs.thumbSlider).slider( "destroy" );
        },

        render: function() {
            return (
                <div className="toolbar iconTableFooter">
                    <div
                        id="thumb_size_slider"
                        ref="thumbSlider"
                        title="Zoom Thumbnails" />
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

        getInitialState: function() {
            return {
                layout: 'icon',
                filterText: "",
            }
        },

        setLayout: function(layout) {
            this.setState({layout: layout});
        },

        setFilterText: function(filterText) {
            console.log("setFilterText", filterText);
            this.setState({filterText: filterText});
            setTimeout(this.deselectHiddenThumbs, 50);
        },

        deselectHiddenThumbs: function() {
            var imageIds = this._thumbsToDeselect;
            console.log("deselectHiddenThumbs", imageIds);

            if (imageIds.length === 0) {
                return;
            }
            var inst = this.props.inst;
            var containerNode = OME.getTreeImageContainerBestGuess(imageIds[0]);
            if (containerNode) {
                imageIds.forEach(function(iid){
                    var selectedNode = inst.locate_node('image-' + iid, containerNode)[0];
                    inst.deselect_node(selectedNode, true);
                });
            }
        },

        setThumbsToDeselect: function(imageIds) {
            this._thumbsToDeselect = imageIds;
        },

        componentDidMount: function() {
            var inst = this.props.inst;
            $(this.refs.dataIcons).selectable({
                filter: 'li.row',
                distance: 2,
                stop: function() {
                    // Make the same selection in the jstree etc
                    $(".ui-selected").each(function(){
                        var imageId = $(this).attr('data-id');
                        var containerNode = OME.getTreeImageContainerBestGuess(imageId);
                        var selectedNode = inst.locate_node('image-' + imageId, containerNode)[0];
                        inst.select_node(selectedNode, true);
                    });
                },
                start: function() {
                    inst.deselect_all();
                }
            });
        },

        componentWillUnmount: function() {
            // cleanup plugin
            $(this.refs.dataIcons).selectable( "destroy" );
        },

        handleIconClick: function(imageId, event) {
            var inst = this.props.inst;
            var containerNode = OME.getTreeImageContainerBestGuess(imageId);
            var selectedNode = inst.locate_node('image-' + imageId, containerNode)[0];

            // Deselect all to begin (supress jstree event)
            // inst.deselect_all(true);
            // inst.select_node(selectedNode, true);

            // Simply allow jstree to handle selection ranges etc by delegating
            // the event.
            // TODO: this fails when we have some thumbnails hidden (still get selected in range)
            var keys = {
                shiftKey: event.shiftKey,
                metaKey: event.metaKey
            }
            $("#" + selectedNode.id + ">a").trigger($.Event('click', keys));
        },

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
                selFileSets = [],
                thumbsToDeselect = [],
                fltr = this.state.filterText;
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

                if (fltr.length === 0 || iData.name.indexOf(fltr) > -1) {
                    imgJson.push(iData);
                } else if (iData.selected) {
                    thumbsToDeselect.push(iData.id);
                }
            });

            // Let parent know that some aren't shown
            this.setThumbsToDeselect(thumbsToDeselect);

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
                return (
                    <ImageIcon
                        image={image}
                        key={image.id}
                        iconSize={this.props.iconSize}
                        handleIconClick={this.handleIconClick} />
                );
            }.bind(this));

            return (
            <div className="centrePanel">
                <IconTableHeader
                        filterText={this.state.filterText}
                        setFilterText={this.setFilterText}
                        layout={this.state.layout}
                        setLayout={this.setLayout} />
                <div id="icon_table" className="iconTable">
                    <ul id="dataIcons"
                        ref="dataIcons"
                        className={this.state.layout + "Layout"}>
                        <IconTableHeadRow />
                        {icons}
                    </ul>
                </div>
            </div>);
        }
    });

    var ImageIcon = React.createClass({

        handleIconClick: function(event) {
            // this.setState ({selected: true});
            this.props.handleIconClick(this.props.image.id, event);
        },

        // getInitialState: function() {
        //     return {selected: this.props.image.selected};
        // },

        getIconSizes: function() {
            var image = this.props.image,
                width = this.props.iconSize,
                height = this.props.iconSize,
                wh = image.data.obj.sizeX / image.data.obj.sizeY;
            if (wh < 1) {
                width = width * wh;
            } else if (wh > 1) {
                height = height / wh;
            }
            return {'width': width, 'height': height}
        },

        // After rendering, scroll selectd icon into view
        // NB: scrollIntoViewIfNeeded() is provided by polyfill
        componentDidUpdate: function() {
            if (this.props.image.selected && this.refs.icon) {
                this.refs.icon.scrollIntoViewIfNeeded();
            }
        },

        render: function() {

            var image = this.props.image,
                iconSizes = this.getIconSizes(),
                cls = [];

            if (image.selected) {cls.push('ui-selected')};
            if (image.fsSelected) {cls.push('fs-selected')};

            return (
                <li className={"row " + cls.join(" ")}
                    id={"image_icon-" + image.id}
                    ref="icon"
                    data-fileset={image.data.obj.filesetId}
                    data-type="image"
                    data-id={image.id}
                    data-perms={image.data.obj.permsCss}
                    tabIndex={0}
                    onClick={this.handleIconClick}
                >
                    <div className="image">
                        <img alt="image"
                            width={iconSizes.width + "px"}
                            height={iconSizes.height + "px"}
                            src={"/webgateway/render_thumbnail/" + image.id + "/?version=" + image.thumbVersion}
                            title={image.name} />
                    </div>
                    <div className="desc" valign="middle">
                        {image.name}
                        <span className="hidden_sort_text">{image.name}</span>
                    </div>
                    <div className="date" valign="middle">{image.date}</div>
                    <div className="sizeX" valign="middle">{image.data.obj.sizeX}</div>
                    <div className="sizeY" valign="middle">{image.data.obj.sizeY}</div>
                    <div className="sizeZ" valign="middle">{image.data.obj.sizeZ}</div>
                </li>
            )
        }
    });

    window.OME.renderCentrePanel = function(jstree, selected) {
        console.log("renderCentrePanel...");
        ReactDOM.render(
            <CentrePanel
                jstree={jstree}
                selected={selected}/>,
            document.getElementById('content_details')
        );
    };

})();
