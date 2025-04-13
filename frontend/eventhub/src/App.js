import React from "react"
import {BrowserRouter as Router, Route, Routes} from 'react-router';
import LoginForm from "./login/LoginForm"
import { UserProvider } from "./UserContext";
import './App.css';
import MainPage from "./MainPage";

const user = { name: 'Tania', loggedIn: true }
class App extends React.Component {

  constructor(props) {
    super(props)

    this.state = {
        
    }

  }
  
  render() {
    return (
      <div>
        <Router>
          <UserProvider value={{ user: user}}>
            <Routes>
              <Route path='/login' exact={true} element={<LoginForm/>}/>
              <Route path='/main' exact={true} element={<MainPage/>}/>
            </Routes>
          </UserProvider>
        </Router>
      </div>
    )
  }

  deleteUser(id) {
    this.setState({
      users: this.state.users.filter((el) => el.id !== id)
    })
  }

  editUser(user) {
    let allUsers = this.state.users
    allUsers[user.id - 1] = user

    this.setState({ users: [] }, () => {
      this.setState({ users: [...allUsers] })
    })
  }

  addUser(user) {
    const id = this.state.users.length + 1
    this.setState({ 
      users: [...this.state.users, {id, ...user}]
    })
  }

  mapStateToProps(state) {
    const {userReducer} = state;
    return {
        users: userReducer.users
    }
  }
}

export default App;
