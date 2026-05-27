// Базовый URL для API (измените порт если нужно)
const API_BASE_URL = 'http://localhost:8080/api';

// Вспомогательная функция для запросов с авторизацией
const getAuthHeaders = () => {
  const token = localStorage.getItem('accessToken');
  return {
    'Content-Type': 'application/json',
    'Authorization': token ? `Bearer ${token}` : '',
  };
};

// API вызовы
export const api = {
  // Авторизация
  login: async (email, password) => {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    });
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Ошибка входа');
    }
    
    return response.json();
  },
  
  getCurrentUser: async () => {
    const response = await fetch(`${API_BASE_URL}/auth/me`, {
      headers: getAuthHeaders(),
    });
    
    if (!response.ok) {
      throw new Error('Ошибка получения пользователя');
    }
    
    return response.json();
  },
  
  // Кабинеты
  getRoomsAvailability: async (datetime) => {
    const response = await fetch(`${API_BASE_URL}/rooms/availability?datetime=${datetime}`, {
      headers: getAuthHeaders(),
    });
    
    if (!response.ok) {
      throw new Error('Ошибка загрузки доступности кабинетов');
    }
    
    return response.json();
  },
  
  getRoomSchedule: async (roomId, date) => {
    const response = await fetch(`${API_BASE_URL}/rooms/${roomId}/schedule?date=${date}`, {
      headers: getAuthHeaders(),
    });
    
    if (!response.ok) {
      throw new Error('Ошибка загрузки расписания');
    }
    
    return response.json();
  },
  
  // Бронирования
  createBooking: async (roomId, start, end) => {
    const response = await fetch(`${API_BASE_URL}/bookings`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ roomId, start, end }),
    });
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Ошибка при бронировании');
    }
    
    return response.json();
  },
};