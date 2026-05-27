import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import RoomTooltip from '../components/RoomTooltip';

function HomePage() {
  const navigate = useNavigate();
  const [selectedBuilding, setSelectedBuilding] = useState('solyanka');
  const [selectedFloor, setSelectedFloor] = useState(1);
  const [selectedDate, setSelectedDate] = useState('');
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [hoveredRoom, setHoveredRoom] = useState(null);
  const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 });
  const [user, setUser] = useState(null);

  const buildings = {
    solyanka: { name: 'Солянка', address: 'ул. Солянка, 14' },
    trifon: { name: 'Трифоновская', address: 'Трифоновская ул., 61' },
    kolobok: { name: 'Колобок', address: 'Колобовский пер., 4' },
    lalya: { name: 'Ляля', address: 'Уланский пер., 6' }
  };

  // Проверяем авторизацию
  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    const userData = localStorage.getItem('user');
    if (token && userData) {
      setUser(JSON.parse(userData));
    }
  }, []);

  // Устанавливаем сегодняшнюю дату
  useEffect(() => {
    const today = new Date();
    const formattedDate = today.toISOString().split('T')[0];
    setSelectedDate(formattedDate);
  }, []);

  // Загружаем доступность кабинетов при изменении параметров
  useEffect(() => {
    if (selectedDate && startTime && endTime) {
      fetchRoomsAvailability();
    }
  }, [selectedDate, startTime, endTime, selectedBuilding, selectedFloor]);

  const fetchRoomsAvailability = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const fullDateTime = `${selectedDate}T${startTime}:00`;
      const data = await api.getRoomsAvailability(fullDateTime);
      
      // Фильтруем по зданию и этажу (если бэкенд это поддерживает)
      // Пока показываем все кабинеты
      setRooms(data);
    } catch (err) {
      setError(err.message);
      setRooms([]);
    } finally {
      setLoading(false);
    }
  };

  const handleMouseMove = (e) => {
    setMousePosition({ x: e.clientX + 15, y: e.clientY + 15 });
  };

  const isTimeValid = () => {
    return startTime && endTime && startTime < endTime;
  };

  const handleRoomClick = (room) => {
    if (room.isAvailable && startTime && endTime) {
      navigate(`/room/${room.id}?date=${selectedDate}&start=${startTime}&end=${endTime}`);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('user');
    setUser(null);
    navigate('/login');
  };

  return (
    <div onMouseMove={handleMouseMove} style={{ minHeight: '100vh', backgroundColor: '#f0f2f5' }}>
      {/* Шапка */}
      <div style={{
        backgroundColor: '#0a1e3c',
        color: 'white',
        padding: '20px',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
      }}>
        <div>
          <h1 style={{ margin: 0, fontSize: '28px' }}>🎓 Лицей НИУ ВШЭ</h1>
          <p style={{ margin: '5px 0 0', opacity: 0.9 }}>Система бронирования аудиторий</p>
        </div>
        <div>
          {user ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
              <span>👋 {user.name} {user.surname}</span>
              <button 
                onClick={handleLogout}
                style={{
                  padding: '8px 16px',
                  backgroundColor: 'rgba(255,255,255,0.2)',
                  color: 'white',
                  border: 'none',
                  borderRadius: '6px',
                  cursor: 'pointer'
                }}
              >
                Выйти
              </button>
            </div>
          ) : (
            <button 
              onClick={() => navigate('/login')}
              style={{
                padding: '8px 16px',
                backgroundColor: 'rgba(255,255,255,0.2)',
                color: 'white',
                border: 'none',
                borderRadius: '6px',
                cursor: 'pointer'
              }}
            >
              Войти
            </button>
          )}
        </div>
      </div>

      <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '20px' }}>
        {/* Ознакомительный текст */}
        <div style={{
          backgroundColor: '#e3f2fd',
          borderLeft: '4px solid #1976d2',
          padding: '15px',
          marginBottom: '25px',
          borderRadius: '4px'
        }}>
          <h3 style={{ margin: '0 0 10px 0', color: '#0a1e3c' }}>📚 Добро пожаловать в систему бронирования!</h3>
          <p style={{ margin: 0, color: '#333' }}>
            Выберите дату и время, чтобы увидеть доступные аудитории.
            Наведите курсор на аудиторию, чтобы увидеть расписание.
          </p>
        </div>

        {/* Фильтры */}
        <div style={{
          backgroundColor: 'white',
          padding: '20px',
          borderRadius: '8px',
          marginBottom: '25px',
          boxShadow: '0 1px 3px rgba(0,0,0,0.1)'
        }}>
          <div style={{ 
            display: 'grid', 
            gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', 
            gap: '15px',
            marginBottom: '20px'
          }}>
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>🏢 Здание:</label>
              <select 
                value={selectedBuilding} 
                onChange={(e) => setSelectedBuilding(e.target.value)}
                style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ddd' }}
              >
                {Object.entries(buildings).map(([key, value]) => (
                  <option key={key} value={key}>{value.name}</option>
                ))}
              </select>
            </div>
            
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>📶 Этаж:</label>
              <select 
                value={selectedFloor} 
                onChange={(e) => setSelectedFloor(Number(e.target.value))}
                style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ddd' }}
              >
                {[1, 2, 3, 4, 5].map(floor => (
                  <option key={floor} value={floor}>{floor} этаж</option>
                ))}
              </select>
            </div>
          </div>

          <div style={{ 
            display: 'grid', 
            gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', 
            gap: '15px',
            paddingTop: '15px',
            borderTop: '1px solid #eee'
          }}>
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>📅 Дата:</label>
              <input 
                type="date" 
                value={selectedDate}
                onChange={(e) => setSelectedDate(e.target.value)}
                min={new Date().toISOString().split('T')[0]}
                style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ddd' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>⏰ Время начала:</label>
              <input 
                type="time"
                value={startTime}
                onChange={(e) => setStartTime(e.target.value)}
                style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ddd' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>⏰ Время конца:</label>
              <input 
                type="time"
                value={endTime}
                onChange={(e) => setEndTime(e.target.value)}
                style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ddd' }}
              />
            </div>
          </div>

          {startTime && endTime && !isTimeValid() && (
            <div style={{ 
              marginTop: '15px', 
              padding: '10px', 
              backgroundColor: '#ffebee', 
              borderRadius: '4px',
              color: '#c62828',
              fontSize: '14px'
            }}>
              ⚠️ Время начала должно быть меньше времени конца
            </div>
          )}
        </div>

        {/* Список кабинетов */}
        <div style={{
          backgroundColor: 'white',
          padding: '20px',
          borderRadius: '8px',
          boxShadow: '0 1px 3px rgba(0,0,0,0.1)'
        }}>
          <h2 style={{ margin: '0 0 20px 0', color: '#0a1e3c' }}>
            Аудитории • {buildings[selectedBuilding].name} • {selectedFloor} этаж
          </h2>
          
          {loading && (
            <div style={{ textAlign: 'center', padding: '40px' }}>
              <div>⏳ Загрузка...</div>
            </div>
          )}
          
          {error && (
            <div style={{ 
              textAlign: 'center', 
              padding: '40px', 
              color: 'red',
              backgroundColor: '#ffebee',
              borderRadius: '8px'
            }}>
              ❌ {error}
            </div>
          )}
          
          {!loading && !error && rooms.length === 0 && startTime && endTime && (
            <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
              📭 Нет доступных кабинетов на выбранное время
            </div>
          )}
          
          <div style={{ 
            display: 'grid', 
            gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', 
            gap: '15px' 
          }}>
            {rooms.map(room => (
              <div
                key={room.id}
                onMouseEnter={() => setHoveredRoom(room)}
                onMouseLeave={() => setHoveredRoom(null)}
                onClick={() => handleRoomClick(room)}
                style={{
                  backgroundColor: room.isAvailable ? '#ffffff' : '#e0e0e0',
                  border: `2px solid ${room.isAvailable ? '#4caf50' : '#9e9e9e'}`,
                  borderRadius: '8px',
                  padding: '15px',
                  cursor: room.isAvailable && startTime && endTime ? 'pointer' : 'default',
                  transition: 'all 0.2s',
                  opacity: room.isAvailable ? 1 : 0.6,
                  position: 'relative'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <h3 style={{ margin: 0, fontSize: '24px' }}>📚 {room.number}</h3>
                </div>
                
                <div style={{ marginTop: '10px', fontSize: '14px' }}>
                  <span style={{ color: room.isAvailable ? '#4caf50' : '#f44336' }}>
                    {room.isAvailable ? '✓ Свободно' : '✗ Занято'}
                  </span>
                </div>

                {room.isAvailable && room.availableUntil && (
                  <div style={{ marginTop: '8px', fontSize: '12px', color: '#666' }}>
                    Свободен до: {new Date(room.availableUntil).toLocaleTimeString()}
                  </div>
                )}
                
                {!room.isAvailable && room.busyUntil && (
                  <div style={{ marginTop: '8px', fontSize: '12px', color: '#666' }}>
                    Занят до: {new Date(room.busyUntil).toLocaleTimeString()}
                  </div>
                )}

                <div style={{ marginTop: '8px', fontSize: '12px', color: '#666' }}>
                  {new Date(selectedDate).toLocaleDateString('ru-RU', {
                    weekday: 'long',
                    day: 'numeric',
                    month: 'long'
                  })}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {hoveredRoom && (
        <RoomTooltip 
          room={hoveredRoom} 
          position={mousePosition}
          selectedDate={selectedDate}
        />
      )}
    </div>
  );
}

export default HomePage;