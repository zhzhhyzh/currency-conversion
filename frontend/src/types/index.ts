export interface ConversionResponse {
  from: string;
  to: string;
  amount: number;
  convertedAmount: number;
  rate: number;
  timestamp: string;
}

export interface ExchangeRatesResponse {
  base: string;
  rates: { [key: string]: number };
  timestamp: number;
}

export interface ErrorResponse {
  status: number;
  message: string;
  details: string;
  timestamp: number;
}

export interface Currency {
  code: string;
  name: string;
  symbol: string;
}
