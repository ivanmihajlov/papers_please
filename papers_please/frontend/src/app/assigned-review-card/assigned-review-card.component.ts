import { Component, OnInit, Input } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ReviewRequest } from '../_model/reviewRequest.model';
import { ReviewService } from '../_service/review.service';
import { ToastrService } from 'ngx-toastr';
import { Router } from '@angular/router';

@Component({
  selector: 'app-assigned-review-card',
  templateUrl: './assigned-review-card.component.html',
  styleUrls: ['./assigned-review-card.component.scss']
})
export class AssignedReviewCardComponent implements OnInit {

  @Input()
  reviewInfo: ReviewRequest;
  form: FormGroup;
  reviewXml: string;
  
  constructor(private formBuilder: FormBuilder,
              private reviewService: ReviewService,
              private toastr: ToastrService,
              private router: Router) { }

  ngOnInit() {
    this.createForm();
  }

  createForm() {
    this.form = this.formBuilder.group({
      review: ['', Validators.required]
    });
  }

  reviewFileChange(files: any) {
    const reader = new FileReader();
    reader.readAsText(files[0], 'UTF-8');
    reader.onload = () => {
        this.reviewXml = reader.result.toString();
        this.toastr.success('Success', 'File uploaded');
    };
    reader.onerror = () => {
      this.toastr.error('Error', 'Cannot read the file');
    };
  }

  submitReview() {
    if (this.reviewXml === '' || this.reviewXml === undefined || this.reviewXml === null ) {
      this.toastr.error('Error', 'No review selected');
      return;
    }
    this.reviewService.submitEvaluationForm(this.reviewXml, this.reviewInfo.processId).subscribe(
      (response => {
        this.toastr.success('Success', 'Your review has been submitted');
        setTimeout(function() {
          location.reload();
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

  openReviewInput() {
    document.getElementById('review').click();
  }

  viewHtml() {
    this.reviewService.getPaperForReviewerHtml(this.reviewInfo.processId);
  }

  viewPdf() {
    this.reviewService.getPaperForReviewerPdf(this.reviewInfo.processId);
  }

}
