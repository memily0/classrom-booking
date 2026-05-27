import { useState, useEffect } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { api } from '../services/api';

function RoomPage() {
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  
  const selectedDate = searchParams.get('date') || new Date().toISOString().split('T')[0];
  const selectedStartTime = searchParams.get('start') || '';
  const selectedEndTime = searchParams.get('end') || '';
  
  const [room, setRoom] = useState(null);
  const [schedule, setSchedule] = useState(null);
  const [startTime, setStartTime] = useState(selectedStartTime);
  const [endTime, setEndTime] = useState(selectedEndTime);
  const [loading, setLoading] = useState(false);
  const [bookingLoading, setBookingLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  useEffect(() => {
    fetchRoomSchedule();
  }, [id, selectedDate]);

  const fetchRoomSchedule = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const data = await api.getRoomSchedule(id, selectedDate);
      setRoom(data.room);
      setSchedule(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleBooking = async () => {
    if (!startTime || !endTime) {
      setError('Выберите время начала и окончания');
      return;
    }

    if (startTime >= endTime) {
      setError('Время начала должно быть меньше времени окончания');
      return;
    }

    setBookingLoading(true);
    setError(null);
    setSuccess(null);

    try {
      const fullStart = `${selectedDate}T${startTime}:00`;
      const fullEnd = `${selectedDate}T${endTime}:00`;
      
      await api.createBooking(id, fullStart, fullEnd);
      
      setSuccess('Бронирование успешно создано!');
      setTimeout(() => {
        navigate('/');
      }, 1500);
    } catch (err) {
      setError(err.message);
    } finally {
      setBookingLoading(false);
    }
  };

  const formatDate = (dateStr) => {
    return new Date(dateStr).toLocaleDateString('ru-RU', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    });
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '24px', marginBottom: '10px' }}>⏳</div>
          <div>Загрузка расписания...</div>
        </div>
      </div>
    );
  }

  return (
    <div style={{ minHeight: '100vh', backgroundColor: '#f0f2f5', padding: '20px' }}>
      <div style={{ maxWidth: '900px', margin: '0 auto', backgroundColor: 'white', borderRadius: '12px', boxShadow: '0 2px 10px rgba(0,0,0,0.1)', overflow: 'hidden' }}>
        {/* Шапка */}
        <div style={{
          backgroundColor: '#0a1e3c',
          color: 'white',
          padding: '20px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          <div>
            <h1 style={{ margin: 0, fontSize: '24px' }}>📚 Кабинет {room?.number}</h1>
            <p style={{ margin: '5px 0 0', opacity: 0.9 }}>{room?.name}</p>
          </div>
          <button onClick={() => navigate('/')} style={{ padding: '8px 16px', backgroundColor: 'rgba(255,255,255,0.2)', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer' }}>
            ← На главную
          </button>
        </div>

        {/* Информация о кабинете */}
        <div style={{ padding: '20px', borderBottom: '1px solid #eee' }}>
          <div style={{ display: 'grid', gap: '10px' }}>
            <div><strong>Вместимость:</strong> {room?.capacity} человек</div>
            <div><strong>Описание:</strong> {room?.description}</div>
            <div><strong>Дата:</strong> {formatDate(selectedDate)}</div>
          </div>
        </div>

        {/* Расписание */}
        <div style={{ padding: '20px', borderBottom: '1px solid #eee', backgroundColor: '#fafafa' }}>
          <h3 style={{ margin: '0 0 15px 0' }}>📋 Расписание на день:</h3>
          
          <div style={{ display: 'grid', gap: '12px' }}>
            {schedule?.bookings?.map((booking) => (
              <div key={booking.id} style={{ padding: '12px', backgroundColor: '#ffebee', borderRadius: '8px', borderLeft: '3px solid #f44336' }}>
                <div><strong>🔴 Занято:</strong> {new Date(booking.start).toLocaleTimeString()} - {new Date(booking.end).toLocaleTimeString()}</div>
              </div>
            ))}
            
            {schedule?.availableSlots?.map((slot, index) => (
              <div key={index} style={{ padding: '12px', backgroundColor: '#e8f5e9', borderRadius: '8px', borderLeft: '3px solid #4caf50', cursor: 'pointer' }}
                onClick={() => {
                  setStartTime(slot.start.split('T')[1].slice(0, 5));
                  setEndTime(slot.end.split('T')[1].slice(0, 5));
                }}>
                <div><strong>🟢 Свободно:</strong> {new Date(slot.start).toLocaleTimeString()} - {new Date(slot.end).toLocaleTimeString()}</div>
              </div>
            ))}
          </div>
        </div>

        {/* Форма бронирования */}
        <div style={{ padding: '20px' }}>
          <h3 style={{ margin: '0 0 15px 0' }}>📝 Забронировать кабинет</h3>
          
          <div style={{ display: 'grid', gap: '15px', marginBottom: '20px' }}>
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Время начала:</label>
              <input type="time" value={startTime} onChange={(e) => setStartTime(e.target.value)} style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '6px' }} />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Время окончания:</label>
              <input type="time" value={endTime} onChange={(e) => setEndTime(e.target.value)} style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '6px' }} />
            </div>
          </div>

          {error && <div style={{ marginBottom: '15px', padding: '12px', backgroundColor: '#ffebee', borderRadius: '6px', color: '#c62828' }}>❌ {error}</div>}
          {success && <div style={{ marginBottom: '15px', padding: '12px', backgroundColor: '#e8f5e9', borderRadius: '6px', color: '#2e7d32' }}>✅ {success}</div>}

          <button onClick={handleBooking} disabled={bookingLoading} style={{ width: '100%', padding: '12px', backgroundColor: '#4caf50', color: 'white', border: 'none', borderRadius: '6px', fontSize: '16px', cursor: bookingLoading ? 'not-allowed' : 'pointer', opacity: bookingLoading ? 0.7 : 1 }}>
            {bookingLoading ? 'Бронирование...' : 'Забронировать'}
          </button>
        </div>
      </div>
    </div>
  );
}

export default RoomPage;