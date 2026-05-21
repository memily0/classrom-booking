import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./styles.css";

const rooms = [
  { name: "Кабинет 204", subject: "Физика", status: "Свободен", time: "12:00-13:30" },
  { name: "Кабинет 312", subject: "Информатика", status: "Занят", time: "11:15-12:45" },
  { name: "Актовый зал", subject: "Мероприятие", status: "Свободен", time: "14:00-16:00" }
];

function App() {
  return (
    <main className="app-shell">
      <section className="workspace">
        <div className="header">
          <div>
            <p className="eyebrow">Лицей</p>
            <h1>Classroom Booking</h1>
          </div>
          <button type="button">Новая бронь</button>
        </div>

        <div className="room-grid" aria-label="Доступность кабинетов">
          {rooms.map((room) => (
            <article className="room-card" key={room.name}>
              <div>
                <h2>{room.name}</h2>
                <p>{room.subject}</p>
              </div>
              <div>
                <span className={room.status === "Свободен" ? "status available" : "status busy"}>
                  {room.status}
                </span>
                <strong>{room.time}</strong>
              </div>
            </article>
          ))}
        </div>
      </section>
    </main>
  );
}

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <App />
  </StrictMode>
);
