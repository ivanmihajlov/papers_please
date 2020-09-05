import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

const URL = 'http://localhost:8088/api/evaluationForms';
const URL2 = 'http://localhost:8088/api/reviewers';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {
  constructor(private http: HttpClient) { }

  httpOptions = {
    headers: new HttpHeaders({
      'Content-Type':  'application/xml'
    })
  };

  getPaperForReviewerHtml(processId: string) {
    window.open(URL2 + '/html/paper/' + processId, '_blank');
  }

  getPaperForReviewerPdf(processId: string) {
    window.open(URL2 + '/pdf/paper/' + processId, '_blank');
  }

  getPdf(reviewIds: string[]) {
    reviewIds.forEach(id => {
      window.open(URL + '/pdf/' + id, '_blank');
    });
  }

  getXml(reviewIds: string[]) {
    reviewIds.forEach(id => {
      window.open(URL + '/xml/' + id, '_blank');
    });
  }

  getHtml(reviewIds: string[]) {
    reviewIds.forEach(id => {
      window.open(URL + '/html/' + id, '_blank');
    });
  }

  getReviewRequests(): Observable<any> {
    return this.http.get('http://localhost:8088/api/reviewers/reviewRequests');
  }

  acceptReview(processId: string): Observable<any>  {
    return this.http.post(`http://localhost:8088/api/reviewers/accept/${processId}`, null);
  }

  rejectReview(processId: string): Observable<any>  {
    return this.http.post(`http://localhost:8088/api/reviewers/reject/${processId}`, null);
  }

  getAssignedReviews(): Observable<any>  {
    return this.http.get('http://localhost:8088/api/reviewers/assignedReviews');
  }

  getTemplate(): Observable<string> {
    return this.http.get('http://localhost:8088/api/evaluationForms/template', { responseType: 'text' });
  }

  submitEvaluationForm(reviewXml: string, processId: string): Observable<any> {
    return this.http.post(`http://localhost:8088/api/evaluationForms/${processId}`, reviewXml, this.httpOptions);
  }

}
