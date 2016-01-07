

(function(){
    var ThumbTable = React.createClass({

        render: function() {
            var selected = this.props.selected;

            if (selected.length === 0) {
                return (<h1>None Selected</h1>);
            }
            var dtype = selected[0].type;
            if (selected.length > 1 && dtype !== "image") {
                return (<h1>Multiple non-images selected</h1>);
            }
            if (dtype === "plate" || dtype === "acquisition") {
                return (<h1>Plate not supported yet</h1>);
            }
            if (dtype !== "image") {
                return (<h1>Nothing to see!</h1>);
            }

            var txt = selected.length + " " + dtype + "s selected";
            return (
                <h1>{txt}</h1>
            );
        }
    });


    window.OME.renderCentrePanel = function(selected) {
        ReactDOM.render(
            <ThumbTable 
                selected={selected}/>,
            document.getElementById('content_details')
        );
    };

})();
