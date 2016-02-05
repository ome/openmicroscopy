
import React from 'react';

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

export default IconTableFooter
