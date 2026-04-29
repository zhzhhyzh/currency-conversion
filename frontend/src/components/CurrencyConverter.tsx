import React, { useEffect, useRef, useState } from 'react';
import FlagIcon from './FlagIcon';
import { ConversionResponse } from '../types';
import api from '../services/api';
import './CurrencyConverter.css';

const POPULAR_CURRENCIES = [
  'AED', 'AUD', 'BDT', 'BND', 'CAD', 'CHF', 'CNY', 'DKK',
  'EUR', 'GBP', 'HKD', 'IDR', 'INR', 'JPY', 'LKR', 'NOK',
  'NZD', 'PHP', 'PKR', 'SAR', 'SEK', 'SGD', 'THB', 'USD',
  'ZAR'
];

export const CurrencyConverter: React.FC = () => {
  const [fromCurrency, setFromCurrency] = useState('USD');
  const [toCurrency, setToCurrency] = useState('EUR');
  const [amount, setAmount] = useState('100');
  const [result, setResult] = useState<ConversionResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [exchangeRate, setExchangeRate] = useState<number | null>(null);
  const latestRequestId = useRef(0);

  useEffect(() => {
    const parsedAmount = Number(amount);

    if (!amount.trim()) {
      setResult(null);
      setExchangeRate(null);
      setError(null);
      return;
    }

    if (!fromCurrency || !toCurrency || !Number.isFinite(parsedAmount) || parsedAmount < 0) {
      setResult(null);
      setExchangeRate(null);
      setError('Please enter a valid non-negative amount');
      return;
    }

    const requestId = latestRequestId.current + 1;
    latestRequestId.current = requestId;
    setLoading(true);
    setError(null);

    const timeoutId = window.setTimeout(async () => {
      try {
        const response = await api.convert(fromCurrency, toCurrency, parsedAmount);
        if (latestRequestId.current === requestId) {
          setResult(response);
          setExchangeRate(response.rate);
        }
      } catch (err) {
        if (latestRequestId.current === requestId) {
          setError(err instanceof Error ? err.message : 'An error occurred during conversion');
          setResult(null);
          setExchangeRate(null);
        }
      } finally {
        if (latestRequestId.current === requestId) {
          setLoading(false);
        }
      }
    }, 350);

    return () => {
      window.clearTimeout(timeoutId);
    };
  }, [amount, fromCurrency, toCurrency]);

  const handleSwapCurrencies = () => {
    setFromCurrency(toCurrency);
    setToCurrency(fromCurrency);
  };

  return (
    <div className="converter-container">
      <div className="converter-card">
        <h1 className="converter-title">Currency Converter</h1>
        
        {error && <div className="error-message">{error}</div>}

        <div className="converter-form">
          {/* From Currency */}
          <div className="currency-input-group">
            <label htmlFor="from-currency" className="input-label">
              From
            </label>
            <div className="currency-selector">
              <FlagIcon countryCode={fromCurrency} size="md" className="flag-icon" />
              <select
                id="from-currency"
                value={fromCurrency}
                onChange={(e) => setFromCurrency(e.target.value)}
                className="currency-select"
              >
                {POPULAR_CURRENCIES.map((curr) => (
                  <option key={curr} value={curr}>
                    {curr}
                  </option>
                ))}
              </select>
            </div>
            <input
              type="number"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              className="amount-input"
              placeholder="Enter amount"
              min="0"
              step="0.01"
            />
          </div>

          {/* Swap Button */}
          <button
            onClick={handleSwapCurrencies}
            className="swap-button"
            title="Swap currencies"
            disabled={loading}
          >
            Swap
          </button>

          {/* To Currency */}
          <div className="currency-input-group">
            <label htmlFor="to-currency" className="input-label">
              To
            </label>
            <div className="currency-selector">
              <FlagIcon countryCode={toCurrency} size="md" className="flag-icon" />
              <select
                id="to-currency"
                value={toCurrency}
                onChange={(e) => setToCurrency(e.target.value)}
                className="currency-select"
              >
                {POPULAR_CURRENCIES.map((curr) => (
                  <option key={curr} value={curr}>
                    {curr}
                  </option>
                ))}
              </select>
            </div>
            {result && (
              <div className="result-display">
                {result.convertedAmount.toFixed(2)}
              </div>
            )}
          </div>
        </div>

        {loading && <div className="loading-message">Updating conversion...</div>}

        {/* Result */}
        {result && !error && (
          <div className="result-section">
            <div className="result-info">
              <p className="result-rate">
                1 {fromCurrency} = {exchangeRate?.toFixed(6)} {toCurrency}
              </p>
              <p className="result-conversion">
                <span className="amount">{result.amount}</span>
                <span className="currency">{result.from}</span>
                <span className="equals">=</span>
                <span className="amount">{result.convertedAmount.toFixed(2)}</span>
                <span className="currency">{result.to}</span>
              </p>
              <p className="result-timestamp">
                Updated: {new Date(result.timestamp).toLocaleTimeString()}
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default CurrencyConverter;
