

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
                console.log("load RUN", objId);
                data = {'run': objId};
            } else if (parentNode.type == "plate") {
                // select 'plate', load if single 'run'
                if (parentNode.children.length < 2) {
                    console.log("load RUN", objId);
                    data = {'plate': objId};
                }
            } else {
                return;
            }

            var url = "/webclient/api/fields/";
            console.log("Plate componentDidMount LOADING: ", url);
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
                <div className="iconTable">
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
            console.log("PlateGrid componentDidMount. LOADING:", url);
            $.ajax({
                url: url,
                dataType: 'json',
                cache: false,
                success: function(data) {
                    if (this.isMounted()) {
                        this.setState({data: data});
                    }
                }.bind(this),
                    error: function(xhr, status, err) {
                }.bind(this)
            });
        },

        getInitialState: function() {
            return {data: undefined};
        },

        handleWellClick: function(wellId) {
            // update selected state for range of wells etc...
            console.log("handleWellClick", wellId);
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
                    var well = grid[rowIndex][colIndex]
                    if (well) {
                        return (
                            <Well
                                key={well.id}
                                id={well.id}
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

        handleClick: function() {
            this.setState({selected: !this.state.selected});
            this.props.handleWellClick(this.props.id);
        },

        getInitialState: function() {
            return {selected: false};
        },

        render: function() {
            var imgStyle = {
                    width: this.props.iconSize + 'px',
                    maxHeight: this.props.iconSize + 'px',
                },
                cls = "";
            if (this.state.selected) {
                cls = "ui-selected";
            }
            return (
                <td className={"well " + cls} title={""+this.props.row+this.props.col}>
                    <img

                        src={"/webgateway/render_thumbnail/" + this.props.id + "/96/"}
                        onClick={this.handleClick}
                        style={imgStyle} />
                </td>
            )
        }
    })

    // Only export ReactPlate
    window.ReactPlate = ReactPlate;
})();
