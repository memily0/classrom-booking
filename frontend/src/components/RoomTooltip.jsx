import { useEffect, useState, useRef } from 'react';

function RoomTooltip({ room, position, selectedDate }) {
  const [adjustedPosition, setAdjustedPosition] = useState(position);
  const [tooltipSize, setTooltipSize] = useState({ width: 0, height: 0 });
  const scrollContainerRef = useRef(null);
  const [scrollDirection, setScrollDirection] = useState('down');

  const formattedDate = selectedDate 
    ? new Date(selectedDate).toLocaleDateString('ru-RU', {
        weekday: 'long',
        day: 'numeric',
        month: 'long',
        year: 'numeric'
      })
    : 'Не выбрана';

  // Автоматическая прокрутка
  useEffect(() => {
    if (!scrollContainerRef.current) return;

    const scrollInterval = setInterval(() => {
      const container = scrollContainerRef.current;
      if (!container) return;

      const maxScroll = container.scrollHeight - container.clientHeight;
      
      if (scrollDirection === 'down') {
        container.scrollTop += 1;
        if (container.scrollTop >= maxScroll) {
          setScrollDirection('up');
        }
      } else {
        container.scrollTop -= 1;
        if (container.scrollTop <= 0) {
          setScrollDirection('down');
        }
      }
    }, 30);

    return () => clearInterval(scrollInterval);
  }, [scrollDirection]);

  useEffect(() => {
    const tooltipElement = document.querySelector('.room-tooltip');
    if (tooltipElement) {
      const rect = tooltipElement.getBoundingClientRect();
      setTooltipSize({ width: rect.width, height: rect.height });
    }
  }, [room]);

  useEffect(() => {
    if (!position) return;

    let newX = position.x;
    let newY = position.y;

    if (position.x + tooltipSize.width > window.innerWidth) {
      newX = position.x - tooltipSize.width - 15;
    }
    if (newX < 10) {
      newX = 10;
    }
    if (position.y + tooltipSize.height > window.innerHeight) {
      newY = position.y - tooltipSize.height - 15;
    }
    if (newY < 10) {
      newY = 10;
    }

    setAdjustedPosition({ x: newX, y: newY });
  }, [position, tooltipSize]);

  return (
    <div
      className="room-tooltip"
      style={{
        position: 'fixed',
        top: adjustedPosition.y,
        left: adjustedPosition.x,
        backgroundColor: 'white',
        border: '2px solid #0a1e3c',
        borderRadius: '12px',
        padding: '20px',
        boxShadow: '0 4px 20px rgba(0,0,0,0.15)',
        zIndex: 1000,
        minWidth: '320px',
        maxWidth: '380px',
        pointerEvents: 'none',
        animation: 'fadeIn 0.2s ease'
      }}
    >
      <h3 style={{ margin: '0 0 10px 0', color: '#0a1e3c', fontSize: '20px' }}>
        📚 Аудитория {room.number}
      </h3>
      
      <div style={{ marginBottom: '15px', paddingBottom: '10px', borderBottom: '1px solid #eee' }}>
        <p style={{ margin: '5px 0', fontSize: '14px', color: '#666' }}>
          📅 {formattedDate}
        </p>
        <p style={{ margin: '5px 0', fontSize: '13px', color: '#999' }}>
          🏢 {room.buildingName} • {room.floor} этаж
        </p>
      </div>

      <h4 style={{ margin: '0 0 12px 0', fontSize: '16px', color: '#333' }}>
        📋 Расписание на сегодня:
      </h4>
      
      {/* Автоматическая прокрутка */}
      <div 
        ref={scrollContainerRef}
        style={{ 
          fontSize: '14px', 
          maxHeight: '300px', 
          overflowY: 'auto',
          overflowX: 'hidden',
          paddingRight: '5px'
        }}
      >
        {Object.entries(room.schedule).map(([time, event]) => (
          <div key={time} style={{ 
            marginBottom: '12px',
            padding: '10px',
            backgroundColor: event.type === 'lesson' ? '#e3f2fd' : '#fff3e0',
            borderRadius: '8px',
            borderLeft: `3px solid ${event.type === 'lesson' ? '#1976d2' : '#ff9800'}`
          }}>
            <div style={{ fontWeight: 'bold', marginBottom: '6px', fontSize: '14px' }}>
              🕐 {time}
            </div>
            <div style={{ color: '#333', marginBottom: '4px' }}>
              <strong>{event.name}</strong>
            </div>
            <div style={{ fontSize: '12px', color: '#666' }}>
              👤 Организатор: {event.organizer}
            </div>
          </div>
        ))}
      </div>

      {room.hasWarning && (
        <div style={{
          marginTop: '15px',
          padding: '12px',
          backgroundColor: '#fff3e0',
          borderRadius: '8px',
          borderLeft: '3px solid #ff9800'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <span style={{ fontSize: '20px' }}>⚠️</span>
            <div>
              <div style={{ fontWeight: 'bold', fontSize: '13px', color: '#e65100', marginBottom: '4px' }}>
                Внимание!
              </div>
              <div style={{ fontSize: '12px', color: '#666' }}>
                Для бронирования этой аудитории требуется ответственный на мероприятии
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default RoomTooltip;