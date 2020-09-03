import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { AllowedRoutes } from './_service/allowed-routes.service';
import { AuthenticationService } from './_service/authentication.service';
import { PaperService } from './_service/paper.service';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { MaterialModule } from './material/material.module';
import { ToastrModule } from 'ngx-toastr';
import { TokenInterceptorService } from './_service/token-interceptor.service';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HeaderComponent } from './header/header.component';
import { RegisterComponent } from './register/register.component';
import { LoginComponent } from './login/login.component';
import { LogoutComponent } from './logout/logout.component';
import { HomeComponent } from './home/home.component';
import { AddPaperFormComponent } from './add-paper-form/add-paper-form.component';
import { PaperCardComponent } from './paper-card/paper-card.component';
import { AuthorPapersComponent } from './author-papers/author-papers.component';
import { QuotedPapersComponent } from './quoted-papers/quoted-papers.component';
import { SearchFormComponent } from './search-form/search-form.component';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    RegisterComponent,
    LoginComponent,
    LogoutComponent,
    HomeComponent,
    AddPaperFormComponent,
    PaperCardComponent,
    AuthorPapersComponent,
    QuotedPapersComponent,
    SearchFormComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MaterialModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    ToastrModule.forRoot({
      progressBar: true,
      timeOut: 4000,
      closeButton: true,
      positionClass: 'toast-bottom-right',
      preventDuplicates: true
    })
  ],
  providers: [
    AllowedRoutes,
    AuthenticationService,
    PaperService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TokenInterceptorService,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})

export class AppModule { }
