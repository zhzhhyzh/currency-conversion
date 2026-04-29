import axios, { AxiosInstance } from 'axios';
import { ConversionResponse, ExchangeRatesResponse } from '../types';

const API_BASE_URL = window.__ENV__?.REACT_APP_API_URL || process.env.REACT_APP_API_URL || 'http://localhost:8081/api';

class CurrencyApi {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    });
  }

  async convert(from: string, to: string, amount: number): Promise<ConversionResponse> {
    try {
      const response = await this.api.get<ConversionResponse>('/convert', {
        params: {
          from: from.toUpperCase(),
          to: to.toUpperCase(),
          amount,
        },
      });
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  async getExchangeRates(): Promise<ExchangeRatesResponse> {
    try {
      const response = await this.api.get<ExchangeRatesResponse>('/rates');
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  async healthCheck(): Promise<string> {
    try {
      const response = await this.api.get<string>('/health');
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  private handleError(error: any): Error {
    if (axios.isAxiosError(error)) {
      if (error.response) {
        return new Error(
          error.response.data?.message || `Error: ${error.response.status} ${error.response.statusText}`
        );
      } else if (error.request) {
        return new Error('No response from server. Please check your connection.');
      }
    }
    return new Error('An unexpected error occurred');
  }
}

const currencyApi = new CurrencyApi();

export default currencyApi;
