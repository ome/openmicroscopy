import React from 'react';
import PlateGrid from './PlateGrid';

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
        };
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
                        fieldIdx={this.state.selectedField} />
                </div>
            </div>
        )
    }
});

export default Plate;
