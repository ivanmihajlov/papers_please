import { Component, OnInit } from '@angular/core';
import { ReviewService } from '../_service/review.service';
import { XonomyReviewService } from '../_service/xonomy-review.service';
import { ToastrService } from 'ngx-toastr';
import { Router } from '@angular/router';

declare const Xonomy: any;

@Component({
  selector: 'app-review-editor',
  templateUrl: './review-editor.component.html',
  styleUrls: ['./review-editor.component.scss']
})
export class ReviewEditorComponent implements OnInit {

  review: string;
  processId: string;

  constructor(private reviewService: ReviewService,
              private xonomyService: XonomyReviewService,
              private toastr: ToastrService,
              private router: Router) { }

  ngOnInit() {
    this.processId = localStorage.getItem('processId');
    localStorage.removeItem('processId');
    this.setXonomy();
  }

  setXonomy() {
    this.review = '<evaluation_form xmlns="https://github.com/ivanmihajlov/papers_please/evaluation_form" ' +
                  'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"></evaluation_form>';
    const xonomy = document.getElementById('xonomy-ef-editor');
    Xonomy.render(this.review, xonomy, this.xonomyService.reviewElement);
  }

  loadEFTemplate() {
    this.reviewService.getTemplate().subscribe(
      (response => {
        const xonomy = document.getElementById('xonomy-ef-editor');
        Xonomy.render(response, xonomy, this.xonomyService.reviewElement);
      }), (error => {
        if (error.error.exception) {
          this.toastr.error('Error', error.error.exception);
        } else {
          this.toastr.error('Error', 'Unknown error');
          console.log(JSON.stringify(error));
        }
      })
    );
  }

  submitReview() {
    this.reviewService.submitEvaluationForm(this.review, this.processId).subscribe(
      (response => {
        this.toastr.success('Success', 'Your review has been submitted');
        setTimeout(() => {
          this.router.navigate(['/assigned-reviews']);
        }, 2000);
      }), (error => {
        if (error.error.exception) {
          this.toastr.error('Error', error.error.exception);
        } else {
          this.toastr.error('Error', 'Unknown error');
          console.log(JSON.stringify(error));
        }
      })
    );
  }

  addEFXonomy() {
    this.review = Xonomy.harvest() as string;
    this.submitReview();
  }

}
