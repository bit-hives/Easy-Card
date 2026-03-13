import { useState, useEffect } from 'react';
import { cardService, whatsAppService } from '../services/api';
import { useAuth } from '../context/AuthContext';
import './Dashboard.css';

const Dashboard = () => {
  const [cards, setCards] = useState([]);
  const [loading, setLoading] = useState(true);
  const { user, isAdmin, logout } = useAuth();

  useEffect(() => {
    loadCards();
  }, []);

  const loadCards = async () => {
    try {
      const response = isAdmin() 
        ? await cardService.getAllCards()
        : await cardService.getMyCards();
      setCards(response.data);
    } catch (error) {
      console.error('Failed to load cards', error);
    } finally {
      setLoading(false);
    }
  };

  const handleActivate = async (id) => {
    try {
      await cardService.activateCard(id);
      loadCards();
    } catch (error) {
      alert('Failed to activate card');
    }
  };

  const handleBlock = async (id) => {
    try {
      await cardService.blockCard(id);
      loadCards();
    } catch (error) {
      alert('Failed to block card');
    }
  };

  const handleCancelViaWhatsApp = async (cardId) => {
    try {
      const response = await whatsAppService.getCancelLink(cardId);
      window.open(response.data.link, '_blank');
    } catch (error) {
      alert('Failed to generate WhatsApp link');
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'ACTIVE': return 'status-active';
      case 'PENDING': return 'status-pending';
      case 'BLOCKED': return 'status-blocked';
      default: return '';
    }
  };

  return (
    <div className="dashboard">
      <header className="header">
        <h1>Easy-Card</h1>
        <div className="user-info">
          <span>{user?.email}</span>
          <button onClick={logout}>Logout</button>
        </div>
      </header>

      <main className="main-content">
        <h2>{isAdmin() ? 'All Cards' : 'My Cards'}</h2>
        
        {loading ? (
          <p>Loading...</p>
        ) : cards.length === 0 ? (
          <p>No cards found</p>
        ) : (
          <div className="cards-grid">
            {cards.map((card) => (
              <div key={card.id} className="card-item">
                <div className="card-number">{card.maskedCardNumber}</div>
                <div className="card-holder">{card.cardHolderName}</div>
                <div className="card-details">
                  <span>Exp: {card.expiryDate}</span>
                  <span className={`status ${getStatusColor(card.status)}`}>
                    {card.status}
                  </span>
                </div>
                <div className="card-balance">
                  <div>Limit: ${card.creditLimit}</div>
                  <div>Available: ${card.availableBalance}</div>
                </div>
                {isAdmin() && (
                  <div className="card-actions">
                    {card.status === 'PENDING' && (
                      <button 
                        className="btn-activate"
                        onClick={() => handleActivate(card.id)}
                      >
                        Activate
                      </button>
                    )}
                    {card.status === 'ACTIVE' && (
                      <button 
                        className="btn-block"
                        onClick={() => handleBlock(card.id)}
                      >
                        Block
                      </button>
                    )}
                  </div>
                )}
                {!isAdmin() && card.status === 'ACTIVE' && (
                  <div className="card-actions">
                    <button 
                      className="btn-whatsapp"
                      onClick={() => handleCancelViaWhatsApp(card.id)}
                    >
                      Cancel via WhatsApp
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
};

export default Dashboard;
