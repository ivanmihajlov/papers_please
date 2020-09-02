import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import * as jwt_decode from 'jwt-decode';

@Injectable({
  providedIn: 'root'
})
export class AllowedRoutes {
  private routes = new BehaviorSubject([]);
  currentRoutes = this.routes.asObservable();

  constructor() {
    this.updateRoutes();
  }

  updateRoutes() {
    const token = localStorage.getItem('token');
    const components = [];

    if (!token) {
      components.push({path: '/login', label: 'Log In'});
      components.push({path: '/register', label: 'Register'});
      this.routes.next(components);
    } else {
      const decodedToken = jwt_decode(token);

      decodedToken.roles.forEach(role => {
        if (role.authority === 'ROLE_EDITOR') {
          // TODO
        }

        if (role.authority === 'ROLE_AUTHOR') {
          // TODO
          components.push({path: '/add-paper', label: 'Submit paper'});
        }

        if (role.authority === 'ROLE_REVIEWER') {
          // TODO
        }
      });

      components.push({path: '/logout', label: 'Logout'});
      this.routes.next(components);
    }
  }
}
