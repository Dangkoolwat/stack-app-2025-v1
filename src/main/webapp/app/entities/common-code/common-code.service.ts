import axios from 'axios';

const baseApiUrl = 'api/common';

export default class CommonCodeService {
  public retrieveGroups(): Promise<any> {
    return axios.get(`${baseApiUrl}/groups`);
  }

  public retrieveDetailsByGroup(groupCode: string): Promise<any> {
    return axios.get(`${baseApiUrl}/details/group/${groupCode}`);
  }

  public getDetail(id: number): Promise<any> {
    return axios.get(`${baseApiUrl}/details/${id}`).then(res => res.data);
  }

  // CRUD for Group

  public createGroup(entity: any): Promise<any> {
    return axios.post(`${baseApiUrl}/groups`, entity);
  }

  public updateGroup(entity: any): Promise<any> {
    return axios.put(`${baseApiUrl}/groups`, entity);
  }

  public deleteGroup(groupCode: string): Promise<any> {
    return axios.delete(`${baseApiUrl}/groups/${groupCode}`);
  }

  // CRUD for Detail
  public createDetail(entity: any): Promise<any> {
    return axios.post(`${baseApiUrl}/details`, entity);
  }

  public updateDetail(entity: any): Promise<any> {
    return axios.put(`${baseApiUrl}/details`, entity);
  }

  public deleteDetail(id: number): Promise<any> {
    return axios.delete(`${baseApiUrl}/details/${id}`);
  }
}
