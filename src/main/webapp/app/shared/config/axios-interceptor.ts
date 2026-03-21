import axios, { type AxiosError, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios';

const TIMEOUT = 1000000;

export interface ApiError {
  title?: string;
  detail?: string;
  status: number;
  type?: string;
  violations?: Array<{
    field: string;
    message: string;
  }>;
}

const onRequestSuccess = (config: InternalAxiosRequestConfig): InternalAxiosRequestConfig => {
  const token = localStorage.getItem('jhi-authenticationToken') ?? sessionStorage.getItem('jhi-authenticationToken');
  if (token) {
    if (!config.headers) {
      config.headers = new axios.AxiosHeaders();
    }
    config.headers.set('Authorization', `Bearer ${token}`);
  }
  config.timeout = TIMEOUT;
  config.url = `${SERVER_API_URL}${config.url}`;
  return config;
};

const setupAxiosInterceptors = (onUnauthenticated: (error: AxiosError) => void, onServerError: (error: AxiosError) => void) => {
  const onResponseError = (error: AxiosError<ApiError>) => {
    const status = error.status || error.response?.status;

    if (status === 401) {
      const url = error.config?.url ?? '';
      if (!url.includes('/api/authenticate') && !url.includes('/api/account')) {
        console.warn('Unauthorized access detected');
        return onUnauthenticated(error);
      }
    } else if (status === 403) {
      console.warn('Forbidden:', error.response?.data);
      window.location.href = '/forbidden';
    } else if (status === 400) {
      const data = error.response?.data;
      if (data?.violations && data.violations.length > 0) {
        console.error('Validation errors:', data.violations);
        if (import.meta.env.DEV) {
          const messages = data.violations.map(v => `${v.field}: ${v.message}`).join('\n');
          console.warn(`Validation Error:\n${messages}`);
        }
      }
    } else if (status && status >= 500) {
      console.error('Server error:', error.response?.data);
      return onServerError(error);
    }

    return Promise.reject(error);
  };

  if (axios.interceptors) {
    axios.interceptors.request.use(onRequestSuccess);
    axios.interceptors.response.use((res: AxiosResponse) => res, onResponseError);
  }
};

export { onRequestSuccess, setupAxiosInterceptors };
