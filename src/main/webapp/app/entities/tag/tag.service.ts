import axios from 'axios';

const baseApiUrl = 'api/tags';
const adminApiUrl = 'api/admin/tags';

export default class TagService {
  public get(id: number): Promise<any> {
    return axios.get(`${baseApiUrl}/${id}`).then(res => res.data);
  }

  public retrieve(): Promise<any> {
    return axios.get(baseApiUrl);
  }

  public search(prefix: string, limit = 10): Promise<any> {
    return axios.get(`${baseApiUrl}/search`, { params: { prefix, limit } });
  }

  public create(entity: any): Promise<any> {
    return axios.post(baseApiUrl, entity).then(res => res.data);
  }

  // Tags usually don't have separate update in this project's current API,
  // but if needed, we might use POST to existing name or similar concepts.
  // For now, only create and delete (soft) are explicitly provided.

  public delete(id: number): Promise<any> {
    return axios.delete(`${adminApiUrl}/${id}`);
  }

  public undelete(id: number): Promise<any> {
    return axios.patch(`${adminApiUrl}/${id}/undelete`);
  }

  public clearAllTagCaches(): Promise<any> {
    // Note: This matches the PatchMapping in TagAdminResource which calls clearAllTagCaches
    return Promise.resolve(); // Client side cache clear if any
  }
}
