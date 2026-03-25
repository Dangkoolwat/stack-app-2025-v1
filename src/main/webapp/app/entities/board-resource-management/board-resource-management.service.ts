import axios from 'axios';

const apiEndpoint = 'api/admin/orphans';

export default class BoardResourceManagementService {
  public retrieve(type: string): Promise<any> {
    return axios.get(`${apiEndpoint}/${type}`);
  }

  public deleteTokens(type: string, ids: number[]): Promise<any> {
    return axios.delete(`${apiEndpoint}/${type}`, { data: ids });
  }
}
