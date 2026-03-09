import axios, { type AxiosPromise } from 'axios';

export default class LoginService {
  logout(): AxiosPromise<any> {
    return axios.post('api/logout');
  }
}
