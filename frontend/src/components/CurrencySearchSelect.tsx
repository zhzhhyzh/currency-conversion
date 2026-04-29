import React, { useMemo, useState } from 'react';
import FlagIcon from './FlagIcon';
import { CurrencyOption, CURRENCY_OPTIONS } from '../data/currencies';

interface CurrencySearchSelectProps {
  id: string;
  selectedId: string;
  onChange: (optionId: string) => void;
}

const getSearchText = (option: CurrencyOption) => (
  `${option.currencyCode} ${option.countryName} ${option.currencyName}`.toLowerCase()
);

export const CurrencySearchSelect: React.FC<CurrencySearchSelectProps> = ({
  id,
  selectedId,
  onChange,
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const [query, setQuery] = useState('');
  const selectedOption = CURRENCY_OPTIONS.find((option) => option.id === selectedId) || CURRENCY_OPTIONS[0];

  const filteredOptions = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();
    if (!normalizedQuery) {
      return CURRENCY_OPTIONS;
    }

    return CURRENCY_OPTIONS.filter((option) => getSearchText(option).includes(normalizedQuery));
  }, [query]);

  const selectOption = (option: CurrencyOption) => {
    onChange(option.id);
    setQuery('');
    setIsOpen(false);
  };

  return (
    <div className="currency-search">
      <button
        type="button"
        className="currency-trigger"
        onClick={() => setIsOpen((value) => !value)}
        aria-expanded={isOpen}
        aria-controls={`${id}-listbox`}
      >
        <span className="currency-trigger-main">
          <FlagIcon countryCode={selectedOption.countryCode} size="md" className="flag-icon" />
          <span className="currency-trigger-text">
            <strong>{selectedOption.currencyCode}</strong>
          </span>
        </span>
        <span className="currency-chevron">v</span>
      </button>

      {isOpen && (
        <div className="currency-menu">
          <input
            id={id}
            type="search"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            className="currency-search-input"
            placeholder="Search currency or country"
            autoFocus
          />

          <div id={`${id}-listbox`} className="currency-options" role="listbox">
            {filteredOptions.length === 0 ? (
              <div className="currency-empty">No currency found</div>
            ) : (
              filteredOptions.map((option) => (
                <button
                  key={option.id}
                  type="button"
                  className={`currency-option ${option.id === selectedId ? 'currency-option-selected' : ''}`}
                  onClick={() => selectOption(option)}
                  role="option"
                  aria-selected={option.id === selectedId}
                >
                  <FlagIcon countryCode={option.countryCode} size="sm" className="flag-icon" />
                  <span className="currency-option-text">
                    <strong>{option.currencyCode}</strong>
                    <span>- {option.countryName}</span>
                  </span>
                </button>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default CurrencySearchSelect;
