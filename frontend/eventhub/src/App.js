import React from "react"
import {BrowserRouter as Router, Route, Routes, Navigate} from 'react-router';
import { UserProvider } from "./UserContext";
import './App.css';
import LoginForm from "./components/login/LoginForm"
import EventsPage from "./components/events/EventsPage";
import ProfilePage from "./components/profile/ProfilePage"
import LogoutPage from "./components/logout/LogoutPage"
import MyEventsList from "./components/events/MyEventsList";
import FriendsPage from './components/friends/FriendsPage';

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
              <Route path="/" element={<Navigate to="/events" />} />
              <Route path='/login' exact={true} element={<LoginForm/>}/>
              <Route path='/events' exact={true} element={<EventsPage/>}/>
              <Route path="/profile" element={<ProfilePage />} />
              <Route path="/logout" element={<LogoutPage />} />
              <Route path="/my-events" element={<MyEventsList />} />
              <Route path="/friends" element={<FriendsPage />} />
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
