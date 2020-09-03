import { Injectable } from '@angular/core';
import * as jwt_decode from 'jwt-decode';

@Injectable({
  providedIn: 'root'
})
export class UtilService {

  constructor() { }

  getLoggedUser() {
    const token = localStorage.getItem('token');
    if (!token)
      return '';
    else {
      const decodedToken = jwt_decode(token);
      return decodedToken.sub;
    }
  }

  openAsXml(data: any) {
    const blob = new Blob([data], { type: 'text/xml' });
    const url = URL.createObjectURL(blob);
    window.open(url, '_blank');
  }

  openAsJson(data: any) {
    const blob = new Blob([data], { type: 'text/json' });
    const url = URL.createObjectURL(blob);
    window.open(url, '_blank');
  }

  generateParams(data: any): string {
    return Object.keys(data).map(k => {
      if (data[k]) {
        if (data[k] instanceof Date)
          return encodeURIComponent(k) + '=' + encodeURIComponent( + data[k]);
        else
          return encodeURIComponent(k) + '=' + encodeURIComponent(data[k]);
      }
    }).filter(Boolean).join('&');
  }

}
