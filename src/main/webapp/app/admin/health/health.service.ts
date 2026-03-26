import axios, { type AxiosPromise } from 'axios';

export default class HealthService {
  separator: string;

  constructor() {
    this.separator = '.';
  }

  checkHealth(): AxiosPromise<any> {
    return axios.get('management/health');
  }

  transformHealthData(data: any): any {
    const response = [];
    this.flattenHealthData(response, null, data.components);
    return response;
  }

  getBaseName(name: string): string {
    if (name) {
      const split = name.split('.');
      return split[0];
    }
  }

  getSubSystemName(name: string): string {
    if (name) {
      const split = name.split('.');
      split.splice(0, 1);
      const remainder = split.join('.');
      return remainder ? ` - ${remainder}` : '';
    }
  }

  addHealthObject(result: any, isLeaf: boolean, healthObject: any, name: string) {
    const healthData = {
      name,
      details: undefined,
      error: undefined,
    };

    const details = {};
    let hasDetails = false;

    for (const key in healthObject) {
      if (Object.hasOwn(healthObject, key)) {
        const value = healthObject[key];
        if (key === 'error') {
          healthData[key] = value;
        } else if (key === 'status') {
          healthData[key] = value;
          // status 는 별도로 처리
        } else {
          if (!this.isHealthObject(value)) {
            details[key] = value;
            hasDetails = true;
          }
        }
      }
    }

    // status 만 있는 경우 (livenessState, readinessState 등) details 에 추가하여 눈 아이콘 표시
    // error 가 있을 때는 status 를 details 에 추가하지 않음
    if (!hasDetails && healthData.status && !healthData.error) {
      details['status'] = healthData.status;
      hasDetails = true;
    }

    // Add the details
    if (hasDetails) {
      healthData.details = details;
    }

    // Only add nodes if they provide additional information
    if (isLeaf || hasDetails || healthData.error) {
      result.push(healthData);
    }
    return healthData;
  }

  flattenHealthData(result: any, path: any, data: any): any {
    for (const key in data) {
      if (Object.hasOwn(data, key)) {
        const value = data[key];
        if (this.isHealthObject(value)) {
          // details 가 있으면 리프 노드로 처리 (서브시스템이 아닌 최종 항목)
          if (value.details) {
            this.addHealthObject(result, true, value, this.getModuleName(path, key));
          } else if (this.hasSubSystem(value)) {
            // 서브시스템이 있으면 중첩 처리
            // error 가 있더라도 서브시스템이 있으면 함께 처리
            this.addHealthObject(result, false, value, this.getModuleName(path, key));
            this.flattenHealthData(result, this.getModuleName(path, key), value);
          } else {
            this.addHealthObject(result, true, value, this.getModuleName(path, key));
          }
        }
      }
    }
    return result;
  }

  getModuleName(path: any, name: string) {
    if (path && name) {
      return path + this.separator + name;
    } else if (path) {
      return path;
    } else if (name) {
      return name;
    }
    return '';
  }

  hasSubSystem(healthObject: any): any {
    let result = false;

    for (const key in healthObject) {
      if (Object.hasOwn(healthObject, key)) {
        const value = healthObject[key];
        if (value?.status) {
          result = true;
        }
      }
    }
    return result;
  }

  isHealthObject(healthObject: any): any {
    let result = false;

    for (const key in healthObject) {
      if (Object.hasOwn(healthObject, key)) {
        if (key === 'status') {
          result = true;
        }
      }
    }
    return result;
  }
}
