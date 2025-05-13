import React, { Component } from "react";
import {useNavigate, useLocation} from "react-router-dom";
import "../../css/Logo.css";

export const withNavigation = (WrappedComponent) => {
    return (props) => {
        const navigate = useNavigate();
        const location = useLocation();
        return <WrappedComponent {...props} navigate={navigate} location={location}/>;
    };
};

class Logo extends Component {
    handleClick = () => {
      this.props.navigate("/events");
    };
  
    render() {
      return (
        <svg
          width="150"
          height="60"
          xmlns="http://www.w3.org/2000/svg"
          onClick={this.handleClick}
          style={{ cursor: "pointer" }}
        >
          <defs>
            <linearGradient id="grad1" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stopColor="#1774c5" stopOpacity="1" />
              <stop offset="100%" stopColor="#4dc3ff" stopOpacity="1" />
            </linearGradient>
          </defs>
          <text
            className="logo-text"
            x="0"
            y="50%"
            fill="url(#grad1)"
          >
            eventhub
          </text>
        </svg>
      );
    }
  }
  
  export default withNavigation(Logo);