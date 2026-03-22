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
      (config.headers as any) = {};
    }
    (config.headers as any).Authorization = `Bearer ${token}`;
  }
  config.timeout = TIMEOUT;

  // SERVER_API_URL이 설정되어 있으면 해당 주사용, 없으면 상대 경로('/') 사용
  const serverUrl = (!SERVER_API_URL || SERVER_API_URL === '/') ? '/' : (SERVER_API_URL.endsWith('/') ? SERVER_API_URL : `${SERVER_API_URL}/`);
  
  const requestUrl = config.url?.startsWith('/') ? config.url.substring(1) : config.url;
  config.url = `${serverUrl}${requestUrl ?? ''}`;

  return config;
};

const setupAxiosInterceptors = (onUnauthenticated: (error: AxiosError) => void, onServerError: (error: AxiosError) => void) => {
  const onResponseError = (error: AxiosError<ApiError>) => {
    const status = error.status || error.response?.status;

    if (status === 401) {
      const url = error.config?.url ?? '';
      // url might be 'api/authenticate' or '/api/authenticate'
      if (!url.includes('api/authenticate') && !url.includes('api/account')) {
        console.warn('Unauthorized access detected for URL:', url);
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
