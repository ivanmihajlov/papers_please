import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { RegisterComponent } from './register/register.component';
import { LoginComponent } from './login/login.component';
import { LogoutComponent } from './logout/logout.component';
import { HomeComponent } from './home/home.component';
import { AddPaperFormComponent } from './add-paper-form/add-paper-form.component';
import { AuthorPapersComponent } from './author-papers/author-papers.component';
import { QuotedPapersComponent } from './quoted-papers/quoted-papers.component';
import { EditorPageComponent } from './editor-page/editor-page.component';
import { ReviewRequestsComponent } from './review-requests/review-requests.component';
import { ReviewsComponent } from './reviews/reviews.component';
import { PaperEditorComponent } from './paper-editor/paper-editor.component';

const routes: Routes = [
  { path: '', component: HomeComponent},
  { path: 'register', component: RegisterComponent},
  { path: 'login', component: LoginComponent},
  { path: 'logout', component: LogoutComponent},
  { path: 'add-paper', component: AddPaperFormComponent},
  { path: 'my-papers', component: AuthorPapersComponent},
  { path: 'paper-quoted-by/:id', component: QuotedPapersComponent},
  { path: 'submissions-in-process', component: EditorPageComponent},
  { path: 'review-requests', component: ReviewRequestsComponent},
  { path: 'assigned-reviews', component: ReviewsComponent },
  { path: 'add-paper-editor', component:  PaperEditorComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
