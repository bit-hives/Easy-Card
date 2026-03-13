import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { cardService } from '../services/api';
import './ApplyCard.css';

const ApplyCard = () => {
  const [formData, setFormData] = useState({
    cardHolderName: '',
    creditLimit: 5000,
  });
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await cardService.applyForCard(formData);
      setMessage('Card application submitted successfully!');
      setTimeout(() => navigate('/dashboard'), 2000);
    } catch (error) {
      setMessage('Failed to apply for card');
    }
  };

  return (
    <div className="apply-container">
      <div className="apply-card">
        <h2>Apply for Easy-Card</h2>
        {message && <div className="message">{message}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Card Holder Name</label>
            <input
              type="text"
              value={formData.cardHolderName}
              onChange={(e) => setFormData({ ...formData, cardHolderName: e.target.value })}
              required
            />
          </div>
          <div className="form-group">
            <label>Credit Limit ($)</label>
            <input
              type="number"
              min="100"
              max="50000"
              value={formData.creditLimit}
              onChange={(e) => setFormData({ ...formData, creditLimit: e.target.value })}
              required
            />
          </div>
          <button type="submit">Apply Now</button>
        </form>
      </div>
    </div>
  );
};

export default ApplyCard;
