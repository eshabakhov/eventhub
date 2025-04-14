import React, { Component } from 'react'

const UserContext = React.createContext()

class UserProvider extends Component {
  // Context state
  //state = {
  //  user: {},
  //}
  // Подбираем контекст юзера из локального хранилища
  state = {
    user: JSON.parse(localStorage.getItem('user')) || {}
  }

  // Method to update state
  //setUser = (user) => {
  //  this.setState((prevState) => ({ user }))
  //}

  // задаем контекст
  setUser = (user) => {
    localStorage.setItem('user', JSON.stringify(user));
    this.setState({ user });
  }

  render() {
    const { children } = this.props
    const { user } = this.state
    const { setUser } = this

    return (
      <UserContext.Provider
        value={{
          user,
          setUser,
        }}
      >
        {children}
      </UserContext.Provider>
    )
  }
}

export default UserContext

export { UserProvider }