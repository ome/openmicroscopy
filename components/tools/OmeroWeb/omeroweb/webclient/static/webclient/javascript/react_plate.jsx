

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
                <div className="iconTable">
                    <Plate
                        parentNode={parentNode}
                        key={key}/>
                </div>
            )
        }
    });

    
    var Plate = React.createClass({

        componentDidMount: function() {
            var parentNode = this.props.parentNode;
            console.log("componentDidMount", parentNode.type, parentNode.children);
            if (parentNode.type === "acquisition") {
                // select 'run', load plate...
                console.log("load RUN");
            } else if (parentNode.type == "plate") {
                // select 'plate', load if single 'run'
                if (parentNode.children.length === 1) {
                    console.log("load RUN");
                } else if (parentNode.children.length === 0) {
                    console.log("load PLATE");
                }
            }
            // var data = {'id': this.props.dataset.id};
            // var url = REACTOMERO.WEBGATEWAY_INDEX + "api/images/";
            // $.ajax({
            //     url: url,
            //     jsonp: "callback",
            //     data: data,
            //     dataType: 'jsonp',
            //     cache: false,
            //     success: function(data) {
            //         if (this.isMounted()) {
            //             this.setState({images: data.images});
            //         }
            //     }.bind(this),
            //     error: function(xhr, status, err) {
            //     }.bind(this)
            // });
        },

        render: function() {
            return (
                <h1>Plate grid {this.props.parentNode.id}</h1>
            )
        }
    });

    // Only export ReactPlate
    window.ReactPlate = ReactPlate;
})();
