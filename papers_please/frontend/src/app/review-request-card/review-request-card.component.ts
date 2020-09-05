import { Component, OnInit, Input } from '@angular/core';
import { ReviewService } from '../_service/review.service';
import { ReviewRequest } from '../_model/reviewRequest.model';
import { ToastrService } from 'ngx-toastr';
import { Router } from '@angular/router';


@Component({
  selector: 'app-review-request-card',
  templateUrl: './review-request-card.component.html',
  styleUrls: ['./review-request-card.component.scss']
})
export class ReviewRequestCardComponent implements OnInit {

  @Input()
  reviewRequest: ReviewRequest;

  constructor(private reviewService: ReviewService,
              private toastr: ToastrService,
              private router: Router) { }

  ngOnInit() {
  }

  viewHtml() {
    this.reviewService.getPaperForReviewerHtml(this.reviewRequest.processId);
  }

  viewPdf() {
    this.reviewService.getPaperForReviewerPdf(this.reviewRequest.processId);
  }

  accept() {
    this.reviewService.acceptReview(this.reviewRequest.processId).subscribe(
      ((response: any) => {
        this.toastr.success('Success', 'Review request accepted');
        setTimeout(function() {
          location.reload();
        }, 2000);
      }), (error: any) => {
        this.toastr.error('Error', 'Unknown error');
        console.log(JSON.stringify(error));
      }
    );
  }

  reject() {
    this.reviewService.rejectReview(this.reviewRequest.processId).subscribe(
      ((response: any) => {
        this.toastr.success('Success', 'Review request rejected');
        location.reload();
      }), (error: any) => {
        this.toastr.error('Error', 'Unknown error');
        console.log(JSON.stringify(error));
      }
    );
  }

}
