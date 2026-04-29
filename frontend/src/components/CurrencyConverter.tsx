import React, { useEffect, useRef, useState } from 'react';
import CurrencySearchSelect from './CurrencySearchSelect';
import { ConversionResponse } from '../types';
import { CURRENCY_OPTIONS, DEFAULT_FROM_CURRENCY_ID, DEFAULT_TO_CURRENCY_ID } from '../data/currencies';
import api from '../services/api';
import './CurrencyConverter.css';

const getCurrencyOption = (optionId: string) => (
  CURRENCY_OPTIONS.find((option) => option.id === optionId) || CURRENCY_OPTIONS[0]
);

const formatNumber = (value: number, maximumFractionDigits = 6) => (
  new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 0,
    maximumFractionDigits,
  }).format(value)
);

const formatInputAmount = (value: number) => value.toFixed(2);

const currentYear = new Date().getFullYear();

export const CurrencyConverter: React.FC = () => {
  const [fromCurrencyId, setFromCurrencyId] = useState(DEFAULT_FROM_CURRENCY_ID);
  const [toCurrencyId, setToCurrencyId] = useState(DEFAULT_TO_CURRENCY_ID);
  const [amount, setAmount] = useState('100');
  const [result, setResult] = useState<ConversionResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const latestRequestId = useRef(0);
  const fromCurrency = getCurrencyOption(fromCurrencyId);
  const toCurrency = getCurrencyOption(toCurrencyId);

  useEffect(() => {
    const parsedAmount = Number(amount);

    if (!amount.trim()) {
      setResult(null);
      setError(null);
      return;
    }

    if (!fromCurrency.currencyCode || !toCurrency.currencyCode || !Number.isFinite(parsedAmount) || parsedAmount < 0) {
      setResult(null);
      setError('Please enter a valid non-negative amount');
      return;
    }

    const requestId = latestRequestId.current + 1;
    latestRequestId.current = requestId;
    setLoading(true);
    setError(null);

    const timeoutId = window.setTimeout(async () => {
      try {
        const response = await api.convert(fromCurrency.currencyCode, toCurrency.currencyCode, parsedAmount);
        if (latestRequestId.current === requestId) {
          setResult(response);
        }
      } catch (err) {
        if (latestRequestId.current === requestId) {
          setError(err instanceof Error ? err.message : 'An error occurred during conversion');
          setResult(null);
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
  }, [amount, fromCurrency.currencyCode, toCurrency.currencyCode]);

  const handleSwapCurrencies = () => {
    if (result) {
      setAmount(formatInputAmount(result.convertedAmount));
    }
    setFromCurrencyId(toCurrencyId);
    setToCurrencyId(fromCurrencyId);
  };

  const handleFromCurrencyChange = (optionId: string) => {
    setFromCurrencyId(optionId);
  };

  const handleToCurrencyChange = (optionId: string) => {
    setToCurrencyId(optionId);
  };

  const handleAmountChange = (value: string) => {
    setAmount(value);
  };

  const indicativeText = result
    ? `1 ${result.from} = ${formatNumber(result.rate, 4)} ${result.to}`
    : `1 ${fromCurrency.currencyCode} = 0 ${toCurrency.currencyCode}`;

  return (
    <div className="converter-container">
      <header className="converter-header">
        <h1 className="converter-title">Currency Converter</h1>
        <p className="converter-subtitle">
          Check live rates, set rate alerts, receive notifications and more.
        </p>
      </header>

      <div className="converter-card">
        {error && <div className="error-message">{error}</div>}

        <div className="converter-panel">
          <div className="conversion-row">
            <p className="row-label">Amount</p>
            <div className="row-body">
              <CurrencySearchSelect
                id="from-currency"
                selectedId={fromCurrencyId}
                onChange={handleFromCurrencyChange}
              />
              <input
                type="number"
                value={amount}
                onChange={(e) => handleAmountChange(e.target.value)}
                className="amount-input"
                placeholder="0.00"
                min="0"
                step="0.01"
              />
            </div>
          </div>

          <div className="swap-divider">
            <span className="divider-line" />
            <button
              onClick={handleSwapCurrencies}
              className="swap-button"
              title="Swap currencies"
              disabled={loading}
              aria-label="Swap currencies"
            >
              <span className="swap-icon" aria-hidden="true" />
            </button>
            <span className="divider-line" />
          </div>

          <div className="conversion-row">
            <p className="row-label">Converted Amount</p>
            <div className="row-body">
              <CurrencySearchSelect
                id="to-currency"
                selectedId={toCurrencyId}
                onChange={handleToCurrencyChange}
              />
              <div className="result-display">
                {result ? result.convertedAmount.toFixed(2) : '0.00'}
              </div>
            </div>
          </div>
        </div>

        {loading && <div className="loading-message">Updating conversion...</div>}
      </div>

      <section className="indicative-rate">
        <p className="indicative-label">Indicative Exchange Rate</p>
        <p className="indicative-value">{indicativeText}</p>
      </section>

      <footer className="converter-footer">
        Zhe heng Y. @ {currentYear}
      </footer>
    </div>
  );
};

export default CurrencyConverter;
