import React from 'react';

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
});

export default Well
