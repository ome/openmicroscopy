

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
                    key={key}/>
            )
        }
    });

    
    var Plate = React.createClass({

        componentDidMount: function() {
            var parentNode = this.props.parentNode,
                plateId = this.props.plateId,
                objId = parentNode.data.id;
            console.log("Plate componentDidMount");
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
            return (
                <div className="iconTable">
                    <div>
                        <select>
                            {fieldSelect}
                        </select>
                    </div>
                    <PlateGrid
                        plateId={this.props.plateId}
                        fieldId={this.state.selectedField} />
                </div>
            )
        }
    });

    var PlateGrid = React.createClass({

        componentDidMount: function() {
            var plateId = this.props.plateId,
                fieldId = this.props.fieldId;
            console.log("PlateGrid componentDidMount");

            var url = "/webgateway/plate/" + plateId + "/" + fieldId + "/";
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

        render: function() {
            var data = this.state.data;
            console.log(data);

            return (
                <table>

                </table>
            );
        }
    });

    // Only export ReactPlate
    window.ReactPlate = ReactPlate;
})();
