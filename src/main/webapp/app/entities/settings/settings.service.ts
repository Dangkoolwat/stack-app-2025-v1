import axios from 'axios';

const baseApiUrl = 'api/settings';

export default class SettingsService {
  public get(): Promise<any> {
    return axios.get(baseApiUrl).then(res => res.data);
  }

  public update(settings: any): Promise<any> {
    return axios.put(baseApiUrl, settings);
  }
}
