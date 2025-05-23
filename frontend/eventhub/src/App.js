import React from "react"
import {BrowserRouter as Router, Route, Routes, Navigate} from 'react-router';
import { UserProvider } from "./UserContext";
import './App.css';
import LoginForm from "./components/login/LoginForm"
import EventsPage from "./components/events/EventsPage";
import ProfilePage from "./components/profile/ProfilePage"
import LogoutPage from "./components/logout/LogoutPage"
import EventForm from "./components/eventForm/EventForm";
import MyEventsList from "./components/events/MyEventsList";
import FriendsPage from './components/friends/FriendsPage';
import AccreditationPage from "./components/accreditation/AccreditationPage";
import EventDetailsPage from "./components/events/EventDetailsPage";
import EventEditForm from "./components/eventForm/EventEdit";
import ModeratorsPage from "./components/ModeratorManagement/ModeratorManagement";
import ModeratorCreate from "./components/ModeratorManagement/ModeratorCreate";
import UserProfile from "./components/profile/UserProfile";
import UserEventsList from "./components/events/UserEventsList";
import EventStatsPage from "./components/stats/EventStatsPage";
import FavoritesPage from "./components/favorites/FavoritesPage";
import OrgStats from "./components/stats/OrgStats";

class App extends React.Component {
  
  render() {
    return (
      <div>
        <Router>
          <UserProvider>
            <Routes>
              <Route path="/" element={<Navigate to="/events" />} />
              <Route path='/login' exact={true} element={<LoginForm/>}/>
              <Route path='/events' exact={true} element={<EventsPage/>}/>
              <Route path="/profile" element={<ProfilePage />} />
              <Route path="/logout" element={<LogoutPage />} />
              <Route path="/my-events" element={<MyEventsList />} />
              <Route path='/create-event' exact={true} element={<EventEditForm/>}/>
              <Route path="/accreditation" element={<AccreditationPage />} />
              <Route path="/friends" element={<FriendsPage />} />
              <Route path="/events/:id" element={<EventDetailsPage />} />
              <Route path="/event-edit/:eventId" element={<EventEditForm />} />
              <Route path="/moderator-management" element={<ModeratorsPage />} />
              <Route path="/moderators/create" element={<ModeratorCreate/>}/>
              <Route path="/moderators/edit/:moderatorId" element={<ModeratorCreate/>}/>
              <Route path="/users/:id" element={<UserProfile/>}/>
              <Route path="/users/:id/events" element={<UserEventsList/>}/>
              <Route path="/organizers/:id" element={<UserProfile/>}/>
              <Route path="/organizers/:id/events" element={<UserEventsList/>}/>
              <Route path="/stats" element={<EventStatsPage />} />
              <Route path="/favorites" element={<FavoritesPage />} />
              <Route path="/org-stats/:id" element={<OrgStats />} />
            </Routes>
          </UserProvider>
        </Router>
      </div>
    )
  }
}

export default App;
