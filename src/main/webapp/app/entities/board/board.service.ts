import axios from 'axios';
import { type IBoard } from '@/shared/model/board.model';

const baseApiUrl = 'api/boards';

export default class BoardService {
  public retrieve(paginationQuery?: any): Promise<any> {
    return axios.get(baseApiUrl, { params: paginationQuery });
  }

  public get(id: number): Promise<IBoard> {
    return axios.get(`${baseApiUrl}/${id}`).then(res => res.data);
  }

  public delete(id: number): Promise<any> {
    return axios.delete(`${baseApiUrl}/${id}`);
  }

  public create(entity: IBoard): Promise<IBoard> {
    return axios.post(`${baseApiUrl}`, entity).then(res => res.data);
  }

  public update(entity: IBoard): Promise<IBoard> {
    return axios.put(`${baseApiUrl}/${entity.id}`, entity).then(res => res.data);
  }
}
