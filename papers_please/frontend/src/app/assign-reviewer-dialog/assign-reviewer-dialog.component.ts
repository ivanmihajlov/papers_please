import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { BasicUserInfo } from '../_model/basicUserInfo.model';
import { PublishingProcessCardComponent } from '../publishing-process-card/publishing-process-card.component';
import { PublishingProcessService } from '../_service/publishing-process.service';

@Component({
  selector: 'app-assign-reviewer-dialog',
  templateUrl: './assign-reviewer-dialog.component.html',
  styleUrls: ['./assign-reviewer-dialog.component.scss']
})
export class AssignReviewerDialogComponent implements OnInit {

  allReviewers: BasicUserInfo[];
  assignedReviwersIds: string[];
  choosenReviewerId: string;

  constructor(private dialogRef: MatDialogRef<PublishingProcessCardComponent>,
              private publishingProcessService: PublishingProcessService,
              @Inject(MAT_DIALOG_DATA) data) {
    this.assignedReviwersIds = data.assignedReviwersIds;
  }

  ngOnInit() {
    this.allReviewers = [];

    this.publishingProcessService.getAllReviewers().subscribe(
      ((response: BasicUserInfo[]) => {
        const reviewers = response;
        reviewers.forEach(reviewer => {
          if (!this.assignedReviwersIds.includes(reviewer.userId))
            this.allReviewers.push(reviewer);
        });
      }), ((error: any) => {
        console.log(JSON.stringify(error));
      })
    );
  }

  assign() {
    if (this.choosenReviewerId != null) {
      this.dialogRef.close(this.choosenReviewerId);
      return;
    }
    this.dialogRef.close();
  }

  cancel() {
    this.dialogRef.close();
  }

}
