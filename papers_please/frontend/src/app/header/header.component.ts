import { Component, OnInit, EventEmitter, Output } from '@angular/core';
import { AllowedRoutes } from '../_service/allowed-routes.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  links = [];

  constructor(private routes: AllowedRoutes,
              public router: Router) {

    this.routes.currentRoutes.subscribe(routes => (this.links = routes));
  }

  ngOnInit() {
    this.routes.currentRoutes.subscribe(routes => (this.links = routes));
  }

  goToHome() {
    this.router.navigate(['/']);
  }

}
