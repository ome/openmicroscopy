import React from 'react';
import Plate from './Plate';

var PlateManager = React.createClass({

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

export default PlateManager
