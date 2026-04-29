import React from 'react';
import * as Flags from 'country-flag-icons/react/3x2';

interface FlagIconProps {
  countryCode: string;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

const CURRENCY_COUNTRY_MAP: { [key: string]: string } = {
  USD: 'US',
  EUR: 'EU', // European Union
  GBP: 'GB',
  JPY: 'JP',
  AUD: 'AU',
  CAD: 'CA',
  CHF: 'CH',
  CNY: 'CN',
  INR: 'IN',
  MXN: 'MX',
  BRL: 'BR',
  SGD: 'SG',
  HKD: 'HK',
  SEK: 'SE',
  NZD: 'NZ',
  MYR: 'MY',
  ZAR: 'ZA',
  THB: 'TH',
  KRW: 'KR',
  NOK: 'NO',
  DKK: 'DK',
  AED: 'AE',
  SAR: 'SA',
  QAR: 'QA',
  TWD: 'TW',
};

const sizeClasses = {
  sm: 'flag-size-sm',
  md: 'flag-size-md',
  lg: 'flag-size-lg',
};

export const FlagIcon: React.FC<FlagIconProps> = ({ 
  countryCode, 
  size = 'md', 
  className = '' 
}) => {
  const code = (CURRENCY_COUNTRY_MAP[countryCode.toUpperCase()] || countryCode).toUpperCase();
  
  const Flag = Flags[code as keyof typeof Flags] as React.ComponentType<{ className?: string }> | undefined;

  if (!Flag) {
    return (
      <div 
        className={`${sizeClasses[size]} flag-fallback ${className}`}
      >
        {countryCode.substring(0, 2).toUpperCase()}
      </div>
    );
  }

  return (
    <Flag 
      className={`${sizeClasses[size]} rounded shadow-sm ${className}`}
    />
  );
};

export default FlagIcon;
