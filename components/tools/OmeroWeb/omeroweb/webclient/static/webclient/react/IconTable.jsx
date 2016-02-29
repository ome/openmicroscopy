import React from 'react';
import IconTableHeader from './IconTableHeader';
import ImageIcon from './ImageIcon';
import Pagination from './Pagination';

var IconTable = React.createClass({

    getInitialState: function() {
        return {
            layout: 'icon'
        };
    },

    setPage: function(page) {
        var inst = this.props.inst,
            parentNode = this.props.parentNode;
        if (page < 1) return;

        // refreshing when images are selected breaks sync with jsTree
        // Select parent before refreshing...
        if (!inst.is_selected(parentNode)) {
            inst.deselect_all(true);
            inst.select_node(parentNode);
        }

        // Tiny delay to make sure that node is not unloaded (during refresh)
        // when the select_node trigger from above causes re-render of Centre Panel
        setTimeout(function(){
            inst.change_page(parentNode, page);
        }, 10);
    },

    setLayout: function(layout) {
        this.setState({layout: layout});
    },

    setFilterText: function(filterText) {
        console.log("setFilterText", filterText);
        // When filtering we need to begin at first page
        var inst = this.props.inst;
        // Use _set_page to not trigger refresh...
        if (inst._set_page) {
            inst._set_page(this.props.parentNode, 1);
        }
        // ...since we refresh here
        inst.filter(this.props.parentNode, filterText);
    },

    componentDidMount: function() {
        var inst = this.props.inst;
        // shares don't allow multi-selection
        if (this.props.parentNode.type === 'share') {
            return;
        }
        $(this.refs.dataIcons).selectable({
            filter: 'li.row',
            distance: 2,
            stop: function() {
                // Make the same selection in the jstree etc
                inst.deselect_all(true);
                $(".ui-selected").each(function(){
                    var imageId = $(this).attr('data-id');
                    var containerNode = OME.getTreeImageContainerBestGuess(imageId);
                    var selectedNode = inst.locate_node('image-' + imageId, containerNode)[0];
                    inst.select_node(selectedNode, true);
                });
            },
        });
    },

    componentWillUnmount: function() {
        // shares don't allow multi-selection
        if (this.props.parentNode.type === 'share') {
            return;
        }
        // cleanup plugin
        $(this.refs.dataIcons).selectable( "destroy" );
    },

    handleIconClick: function(imageId, event) {
        var inst = this.props.inst;
        var containerNode = OME.getTreeImageContainerBestGuess(imageId);
        var selectedNode = inst.locate_node('image-' + imageId, containerNode)[0];
        var keys = {
            shiftKey: event.shiftKey,
            metaKey: event.metaKey
        };
        $("#" + selectedNode.id + ">a").trigger($.Event('click', keys));
    },

    render: function() {
        var parentNode = this.props.parentNode,
            childCount = parentNode.data.obj.childCount,
            inst = this.props.inst;
        var imgNodes = [];
        var dateFormatOptions = {
            weekday: "short", year: "numeric", month: "short",
            day: "numeric", hour: "2-digit", minute: "2-digit"
        };
        // shares tree doesn't support pagination
        var page, pageSize = 1;
        if (inst.get_page) {
            page = inst.get_page(parentNode);
            pageSize = inst.get_page_size(parentNode);
        }

        parentNode.children.forEach(function(ch){
            var childNode = inst.get_node(ch);
            // Ignore non-images under tags or 'deleted' under shares
            if (childNode.type == "image") {
                imgNodes.push(childNode);
            }
        });

        var imgJson = [],
            selFileSets = [],
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
                    'thumbVersion': node.data.obj.thumbVersion,
                    'date': date,
                };
            // Note fileset IDs for selected images
            if (iData.selected) {
                var fsId = node.data.obj.filesetId;
                if (fsId) {
                    selFileSets.push(fsId);
                }
            }
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
            return (
                <ImageIcon
                    image={image}
                    key={image.id}
                    shareId={image.shareId}
                    iconSize={this.props.iconSize}
                    handleIconClick={this.handleIconClick} />
            );
        }.bind(this));

        var filter = this.props.parentNode.data.obj.filter || "";
        var filterCount = this.props.parentNode.data.obj.filterCount;

        return (
        <div className="centrePanel">
            <IconTableHeader
                    filterText={filter}
                    setFilterText={this.setFilterText}
                    childCount={childCount}
                    filteredCount={filterCount}
                    layout={this.state.layout}
                    setLayout={this.setLayout} />
            <div id="icon_table" className="iconTable">
                <ul id="dataIcons"
                    ref="dataIcons"
                    className={this.state.layout + "Layout"}>
                    <IconTableHeadRow />
                    {icons}
                </ul>
                <Pagination
                    page={page}
                    filteredCount={filterCount}
                    pageSize={pageSize}
                    setPage={this.setPage} />
            </div>
        </div>);
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

export default IconTable
