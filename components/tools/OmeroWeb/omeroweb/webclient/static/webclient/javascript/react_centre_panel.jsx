

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

            var txt = selected.length + " " + dtype + "s selected";
            return (
                <div>
                    <IconTableHeader />
                    <IconTable />
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

    var IconTable = React.createClass({
        render: function() {
            return (
              <div id="icon_table" className="iconTable" />
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
