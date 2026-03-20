export interface IBoard {
  id?: number;
  title?: string;
  content?: string;
  viewCount?: number;
  notice?: boolean;
  userId?: number;
  boardTypeCode?: string;
  createdDate?: Date;
  createdBy?: string;
  lastModifiedDate?: Date;
  lastModifiedBy?: string;
  deleted?: boolean;
}

export class Board implements IBoard {
  constructor(
    public id?: number,
    public title?: string,
    public content?: string,
    public viewCount?: number,
    public notice?: boolean,
    public userId?: number,
    public boardTypeCode?: string,
    public createdDate?: Date,
    public createdBy?: string,
    public lastModifiedDate?: Date,
    public lastModifiedBy?: string,
    public deleted?: boolean
  ) {
    this.notice = this.notice ?? false;
    this.viewCount = this.viewCount ?? 0;
    this.deleted = this.deleted ?? false;
  }
}
