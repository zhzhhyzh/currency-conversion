/// <reference types="react-scripts" />

declare module '*.css';

interface Window {
  __ENV__?: {
    REACT_APP_API_URL?: string;
  };
}
