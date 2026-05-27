// Моковые данные для разработки
export const mockApi = {
  // Получить доступность комнат
  getRoomsAvailability: async (building, floor, datetime) => {
    await new Promise(resolve => setTimeout(resolve, 500));
    return { success: true, data: [] };
  },
  
  // Создать бронирование
  createBooking: async (roomId, startTime, endTime) => {
    await new Promise(resolve => setTimeout(resolve, 1000));
    const user = localStorage.getItem('user');
    if (!user) throw new Error('Не авторизован');
    return { success: true, message: 'Бронирование создано!' };
  },
  
  // Войти
  login: async (email, password) => {
    await new Promise(resolve => setTimeout(resolve, 1000));
    if (email && password) {
      return {
        accessToken: 'mock-token',
        user: { id: 1, email, name: 'Студент', surname: 'Лицея' }
      };
    }
    throw new Error('Неверные данные');
  }
};