import React, { Component } from "react";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import leaflet from "leaflet";
import { motion } from "framer-motion";
import onlineIconImg from "../../img/online-marker.png";
import offlineIconImg from "../../img/offline-marker.png";
import iconShadow from "leaflet/dist/images/marker-shadow.png";
import "leaflet/dist/leaflet.css";
import "../../css/EventsPage.css";
import UserContext from "../../UserContext";
import { useNavigate } from "react-router-dom";
import EventHubLogo from "../../img/eventhub.png"

// HOC для добавления navigate в класс-компонент
export const withNavigation = (WrappedComponent) => {
  return (props) => <WrappedComponent {...props} navigate={useNavigate()} />;
};

// Иконки
const onlineIcon = new leaflet.Icon({
  iconUrl: onlineIconImg,
  shadowUrl: iconShadow,
  iconSize: [41, 41],
  iconAnchor: [12, 41],
});

const offlineIcon = new leaflet.Icon({
  iconUrl: offlineIconImg,
  shadowUrl: iconShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
});


// Центрирование карты по всем точкам
function FitToAllMarkers({ events }) {
  const map = useMap();
  React.useEffect(() => {
    if (events.length > 0) {
      const bounds = leaflet.latLngBounds(events.map((e) => e.position));
      map.fitBounds(bounds, { padding: [30, 30] });
    }
  }, [events, map]);
  return null;
}


// Перелет к точке
function FlyToLocation({ position }) {
  const map = useMap();
  React.useEffect(() => {
    if (position) {
      map.flyTo(position, 13, { duration: 1.5 });
    }
  }, [position, map]);
  return null;
}

// Формат даты
const formatDateRange = (start, end) => {
  const options = { day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit" };
  const startStr = start.toLocaleString("ru-RU", options).replace(",", "").replaceAll("/", ".");
  const endStr = end.toLocaleString("ru-RU", options).replace(",", "").replaceAll("/", ".");
  return `${startStr} - ${endStr}`;
};

class EventsPage extends Component {
  static contextType = UserContext;


  constructor(props) {
    super(props);
        this.markerRefs = React.createRef();
        this.markerRefs.current = {};
    this.state = {
      events: [],
      search: "",
      focusedEvent: null,
    };
  }

  componentDidMount() {
    fetch("http://localhost:9500/api/v1/events")
      .then((res) => res.json())
      .then((data) => {
        const loadedEvents = data.list.map((e) => ({
          id: e.id,
          title: e.title,
          description: e.description,
          format: e.format,
          date: formatDateRange(e.startDateTime, e.endDateTime),
          tags: e.tags?.map((tag) => tag.name) || [],
          position: [e.latitude, e.longitude],
          location: e.location,
        }));
        this.setState({ events: loadedEvents });
      })
      .catch((err) => console.error("Ошибка при загрузке точек:", err));
  }

  render() {
    const { user } = this.context;
    const { navigate } = this.props;


    return (
      <div className="events-container">
        {/* Верхняя панель */}
        <div className="header-bar">
          <div className="top-logo">
            <img src={EventHubLogo} alt="Logo" className="logo" />
          </div>
          <div className="login-button-container">
            <button
              onClick={() => navigate("/login")}
              className="login-button"
            >
              Войти
            </button>
          </div>
        </div>

        {/* Основной контент: список и карта */}
        <div className="content-panels">
          <motion.div
            className="left-panel"
            initial={{ opacity: 0, x: -50 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.5 }}
          >
            <input
              type="text"
              placeholder="Поиск мероприятий"
              className="search-input"
              value={this.state.search}
              onChange={(e) => this.setState({ search: e.target.value })}
            />

            {this.state.events
              .filter(
                (event) =>
                  event.title.toLowerCase().includes(this.state.search.toLowerCase()) ||
                  event.tags.some((tag) =>
                    tag.toLowerCase().includes(this.state.search.toLowerCase())
                  )
              )
              .map((event) => (
                <motion.div
                  key={event.id}
                  className="event-card"
                  whileHover={{ scale: 1.02 }}
                >
                  <div className="event-date">{event.date}</div>
                  <h3 className="event-title">{event.title}</h3>
                  <p className="event-description">{event.description}</p>
                  <p className="event-location">{event.location}</p>
                  <div className="event-tags">
                    {event.tags.map((tag, idx) => (
                      <span key={idx} className="event-tag">
                        {tag}
                      </span>
                    ))}
                  </div>
                  <div className="card-buttons">
                    <div className="button-group">
                      <button
                        onClick={() => navigate(`/event/${event.id}`)}
                        className="event-button details"
                      >
                        Подробнее
                      </button>
                      <button
                        onClick={() => this.setState({ focusedEvent: event })}
                        className="event-button map"
                      >
                        Показать на карте
                      </button>
                    </div>
                    <div className="event-format">
                      {event.format === "ONLINE" ? "Онлайн" : "Оффлайн"}
                    </div>
                  </div>
                </motion.div>
              ))}
          </motion.div>

          <motion.div
            className="right-panel"
            initial={{ opacity: 0, x: 50 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.5 }}
          >
            <MapContainer center={[55.75, 37.61]} zoom={11} style={{ height: "100%" }}>
              <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution="&copy; OpenStreetMap contributors"
                maxZoom={18}
              />
              {this.state.events.length > 0 && <FitToAllMarkers events={this.state.events} />}
              {this.state.focusedEvent &&
                        <FlyToLocation
                            position={this.state.focusedEvent.position}
                            markerId={this.state.focusedEvent.id}
                            markerRefs={this.markerRefs}
                        />}
              {this.state.events.map((event) => (
                <Marker
                  key={event.id}
                  position={event.position}
                  icon={event.format === "ONLINE" ? onlineIcon : offlineIcon}
                        ref={ref => {
                            if (ref) this.markerRefs.current[event.id] = ref;
                        }}
                >
                  <Popup>
                    <strong>{event.title}</strong>
                    <p>{event.description}</p>
                    <p>{event.date}</p>
                  </Popup>
                </Marker>
              ))}
            </MapContainer>
          </motion.div>
        </div>
      </div>
    );
  }
}

export default withNavigation(EventsPage);
