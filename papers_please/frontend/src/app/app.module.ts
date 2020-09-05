import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { AllowedRoutes } from './_service/allowed-routes.service';
import { AuthenticationService } from './_service/authentication.service';
import { PaperService } from './_service/paper.service';
import { CoverLetterService } from './_service/cover-letter.service';
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
import { ConfirmationDialogComponent } from './confirmation-dialog/confirmation-dialog.component';
import { EditorPageComponent } from './editor-page/editor-page.component';
import { PublishingProcessCardComponent } from './publishing-process-card/publishing-process-card.component';
import { AssignReviewerDialogComponent } from './assign-reviewer-dialog/assign-reviewer-dialog.component';
import { ReviewsComponent } from './reviews/reviews.component';
import { ReviewRequestsComponent } from './review-requests/review-requests.component';
import { ReviewRequestCardComponent } from './review-request-card/review-request-card.component';
import { AssignedReviewCardComponent } from './assigned-review-card/assigned-review-card.component';

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
    SearchFormComponent,
    ConfirmationDialogComponent,
    EditorPageComponent,
    PublishingProcessCardComponent,
    AssignReviewerDialogComponent,
    ReviewsComponent,
    ReviewRequestsComponent,
    ReviewRequestCardComponent,
    AssignedReviewCardComponent
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
      timeOut: 2000,
      closeButton: true,
      positionClass: 'toast-bottom-right',
      preventDuplicates: false
    })
  ],
  providers: [
    AllowedRoutes,
    AuthenticationService,
    PaperService,
    AuthorPapersComponent,
    CoverLetterService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TokenInterceptorService,
      multi: true
    }
  ],
  entryComponents: [ConfirmationDialogComponent, AssignReviewerDialogComponent],
  bootstrap: [AppComponent]
})

export class AppModule { }
