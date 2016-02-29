

(function(){

    var ReactPlate = React.createClass({

        render: function() {
            var parentNode = this.props.parentNode;

            // If not loaded, show nothing (don't know how many children plate will have)
            if (!parentNode.state.loaded) {
                return (<h2 className="iconTable">Loading...</h2>);
            }
            // If plate has > 1 run, show nothing
            if (parentNode.type === "plate" && parentNode.children.length > 1) {
                return (<h2 className="iconTable">Select Run</h2>);
            }
            // key identifies the content of center panel. Plate or Run
            var key = parentNode.id;
            if (parentNode.type === "plate" && parentNode.children.length === 1) {
                // Children is list of node-ids
                key = parentNode.children[0];
            }
            // We pass key to <Plate> so that if key doesn't change,
            // Plate won't mount (load data) again
            return (
                <Plate
                    plateId={this.props.plateId}
                    parentNode={parentNode}
                    iconSize={this.props.iconSize}
                    key={key}/>
            )
        }
    });

    
    var Plate = React.createClass({

        componentDidMount: function() {
            var parentNode = this.props.parentNode,
                plateId = this.props.plateId,
                objId = parentNode.data.id;
            var data;
            if (parentNode.type === "acquisition") {
                // select 'run', load plate...
                data = {'run': objId};
            } else if (parentNode.type == "plate") {
                // select 'plate', load if single 'run'
                if (parentNode.children.length < 2) {
                    data = {'plate': objId};
                }
            } else {
                return;
            }

            var url = "/webclient/api/fields/";
            $.ajax({
                url: url,
                data: data,
                dataType: 'json',
                cache: false,
                success: function(data) {
                    if (this.isMounted()) {
                        this.setState({
                            fields: data.fields,
                            selectedField: data.fields[0]
                        });
                    }
                }.bind(this),
                    error: function(xhr, status, err) {
                }.bind(this)
            });
        },

        getInitialState: function() {
            return {
                fields: [],
                selectedField: undefined
            }
        },

        handleFieldSelect: function(event) {
            this.setState({selectedField: event.target.value});
        },

        render: function() {
            var fieldSelect,
                fields = this.state.fields;
            if (fields.length == 0) {
                return (<div className="iconTable">Loading...</div>);
            }
            fieldSelect = [];
            for (var f=fields[0], idx=1; f<=fields[1]; f++) {
                fieldSelect.push(
                    <option
                        key={f}
                        value={f}>
                        {"Field " + idx}
                    </option>);
                idx++;
            }
            // #spw id is just for css
            // Use key: selectedField to force PlateGrid to mount on field change
            return (
                <div className="plateContainer">
                    <div>
                        <select onChange={this.handleFieldSelect} >
                            {fieldSelect}
                        </select>
                    </div>
                    <div id="spw">
                        <PlateGrid
                            key={this.state.selectedField}
                            iconSize={this.props.iconSize}
                            plateId={this.props.plateId}
                            fieldId={this.state.selectedField} />
                    </div>
                </div>
            )
        }
    });

    var PlateGrid = React.createClass({

        componentDidMount: function() {
            var plateId = this.props.plateId,
                fieldId = this.props.fieldId;

            var url = "/webgateway/plate/" + plateId + "/" + fieldId + "/";
            $.ajax({
                url: url,
                dataType: 'json',
                cache: false,
                success: function(data) {
                    if (this.isMounted()) {
                        var wellIds = this.getWellIdsFromUrlQuery(data);
                        this.setState({
                            data: data,
                            selectedWellIds: wellIds
                        });
                    }
                }.bind(this),
                    error: function(xhr, status, err) {
                }.bind(this)
            });
        },

        // Uses the url ?show=well-123 or image-123 to get well IDs from data
        getWellIdsFromUrlQuery: function(data) {
            var param = OME.getURLParameter('show'),
                wellIds = [];
            if (param) {
                param.split("|").forEach(function(p) {
                    var wellId, imgId;
                    if (p.split("-")[0] === "well") {
                        wellId = parseInt(p.split("-")[1], 10);
                    } else if (p.split("-")[0] === "image") {
                        imgId = parseInt(p.split("-")[1], 10);
                    }
                    // Validate well Id is in this plate
                    wellId = this.getWellId(data, wellId, imgId);
                    if (wellId) {
                        wellIds.push(wellId);
                    }
                }.bind(this));
            }
            return wellIds;
        },

        // Find well in data using wellId OR imageId, return wellId
        getWellId: function (data, wellId, imageId) {
            var wellId;
            data.grid.forEach(function(row){
                row.forEach(function(well) {
                    if (well && (well.id === imageId || well.wellId === wellId)) {
                        wellId = well.wellId;
                    }
                });
            });
            return wellId;
        },

        getInitialState: function() {
            return {
                data: undefined,
                selectedWellIds: [],
            }
        },

        handleWellClick: function(event, wellId) {
            // update selected state for range of wells etc...
            var isWellSelected = function(wellId) {
                return (this.state.selectedWellIds.indexOf(wellId) > -1);
            }.bind(this);

            if (event.shiftKey) {
                // select range
                var wellIds = [],
                    selectedIdxs = [];
                // make a list of all well IDs, and index of selected wells...
                this.state.data.grid.forEach(function(row){
                    row.forEach(function(w){
                        if (w) {
                            wellIds.push(w.wellId);
                            if (isWellSelected(w.wellId)) {
                                selectedIdxs.push(wellIds.length-1);
                            }
                        }
                    });
                });
                // extend the range of selected wells with index of clicked well...
                var clickedIdx = wellIds.indexOf(wellId),
                    newSel = [],
                    startIdx = Math.min(clickedIdx, selectedIdxs[0]),
                    endIdx = Math.max(clickedIdx, selectedIdxs[selectedIdxs.length-1]);
                //...and select all wells within that range
                wellIds.forEach(function(wellId, idx){
                    if (startIdx <= idx && idx <= endIdx) {
                        newSel.push(wellId);
                    }
                });
                this.setState({selectedWellIds: newSel});

            } else if (event.metaKey) {
                // toggle selection of well
                var found = false;
                // make a new list from old, removing clicked well
                var s = this.state.selectedWellIds.map(function(id){
                    if (wellId !== id) {
                        return id;
                    } else {
                        found = true;
                    }
                });
                // if well wasn't already seleced, then select it
                if (!found) {
                    s.push(wellId);
                }
                this.setState({selectedWellIds: s});
            } else {
                // Select only this well
                this.setState({selectedWellIds: [wellId]});
            }
            // Calls to ome.webclient.actions.js
            //OME.well_selection_changed(selected, idx, perms);
        },

        render: function() {
            var data = this.state.data,
                iconSize = this.props.iconSize,
                placeholderStyle = {
                    width: iconSize + 'px',
                    height: iconSize + 'px',
                },
                selectedWellIds = this.state.selectedWellIds,
                handleWellClick = this.handleWellClick;
            if (!data) {
                return (<table />)
            }
            var columnNames = data.collabels.map(function(l){
                return (<th key={l}>{l}</th>);
            });
            var grid = data.grid;
            var rows = data.rowlabels.map(function(r, rowIndex){
                var wells = data.collabels.map(function(c, colIndex){
                    var well = grid[rowIndex][colIndex];
                    if (well) {
                        var selected = selectedWellIds.indexOf(well.wellId) > -1;
                        return (
                            <Well
                                key={well.wellId}
                                id={well.wellId}
                                iid={well.id}
                                selected={selected}
                                iconSize={iconSize}
                                handleWellClick={handleWellClick}
                                row={r}
                                col={c} />
                        )
                    } else {
                        return (
                            <td className="placeholder" key={r + "_" + c}>
                                <div style={placeholderStyle} />
                            </td>);
                    }
                });
                return (
                    <tr key={r}>
                        <th>{r}</th>
                        {wells}
                    </tr>
                );
            });

            return (
                <table>
                    <tbody>
                        <tr>
                            <th> </th>
                            {columnNames}
                        </tr>
                        {rows}
                    </tbody>
                </table>
            );
        }
    });


    var Well = React.createClass({

        handleClick: function(event) {
            this.props.handleWellClick(event, this.props.id);
        },

        render: function() {
            var imgStyle = {
                    width: this.props.iconSize + 'px',
                    maxHeight: this.props.iconSize + 'px',
                },
                cls = "";
            if (this.props.selected) {
                cls = "ui-selected";
            }
            return (
                <td className={"well " + cls} title={""+this.props.row+this.props.col}>
                    <img

                        src={"/webgateway/render_thumbnail/" + this.props.iid + "/96/"}
                        onClick={this.handleClick}
                        style={imgStyle} />
                </td>
            )
        }
    })

    // Only export ReactPlate
    window.ReactPlate = ReactPlate;
})();
