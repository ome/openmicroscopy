import React from 'react';

var Well = React.createClass({

    handleClick: function(event) {
        this.props.handleWellClick(event, this.props.id);
    },

    handleDoubleClick: function(event) {
        var imageId = this.props.imageId;
        var url = WEBCLIENT.URLS.webindex + 'img_detail/' + imageId + '/';
        OME.openPopup(url);
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
            <td className={"well " + cls}
                data-wellid={this.props.id}
                title={""+this.props.row+this.props.col}>
                <img
                    src={this.props.thumbUrl}
                    onDoubleClick={this.handleDoubleClick}
                    onClick={this.handleClick}
                    style={imgStyle} />
            </td>
        )
    }
});

export default Well
