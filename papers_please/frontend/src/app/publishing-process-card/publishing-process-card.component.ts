import { Component, OnInit, Input } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { AssignReviewerDialogComponent } from '../assign-reviewer-dialog/assign-reviewer-dialog.component';
import { PublishingProcess } from '../_model/publishingProcess.model';
import { PublishingProcessService } from '../_service/publishing-process.service';
import { PaperService } from '../_service/paper.service';
import { ReviewService } from '../_service/review.service';
import { UtilService } from '../_service/util.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-publishing-process-card',
  templateUrl: './publishing-process-card.component.html',
  styleUrls: ['./publishing-process-card.component.scss']
})
export class PublishingProcessCardComponent implements OnInit {

  @Input()
  publishingProcess: PublishingProcess;
  showAssignEditor = false;
  showAssignReviewer = false;
  showDecisionButtons = false;
  showReviews = false;

  constructor(private dialog: MatDialog,
              private publishingProcessService: PublishingProcessService,
              private paperService: PaperService,
              private reviewService: ReviewService,
              private utilService: UtilService,
              private toastr: ToastrService) { }

  ngOnInit() {
    this.showButtons();
  }

  showButtons() {
    if (this.publishingProcess.editorUsername == null)
      this.showAssignEditor = true;
    else if (this.publishingProcess.editorUsername === this.utilService.getLoggedUser()) {
      if (this.publishingProcess.status === 'NEW_SUBMISSION' || this.publishingProcess.status === 'NEW_REVIEWER_NEEDED' || this.publishingProcess.status === 'NEW_REVISION') {
        this.showAssignEditor = false;
        this.showAssignReviewer = true;
      } else if (this.publishingProcess.status === 'REVIEWS_DONE') {
        this.showDecisionButtons = true;
      } else {
        this.showDecisionButtons = false;
        this.showAssignReviewer = false;
        this.showAssignEditor = false;
      }
    }
    if (this.publishingProcess.finishedReviewsIds.length !== 0) {
      this.showReviews = true;
    }
  }

  assignMyselfAsEditor() {
    this.publishingProcessService.assignEditor(this.publishingProcess.processId).subscribe(
      ((response: PublishingProcess) => {
        this.toastr.success('Success', 'Assigned yourself as the editor');
        this.publishingProcess = response;
        this.showButtons();
      }), (error: any) => {
        this.toastr.error('Error', 'Unknown error');
        console.log(JSON.stringify(error));
      }
    );
  }

  openDialog() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.disableClose = true;
    dialogConfig.autoFocus = true;
    dialogConfig.data = {
      assignedReviwersIds: this.publishingProcess.reviewersIds
    }

    const dialogReference = this.dialog.open(AssignReviewerDialogComponent, dialogConfig);
    dialogReference.afterClosed().subscribe(
      reviewerId => {
        if (reviewerId !== undefined) {
          this.publishingProcessService.assignReviewer(this.publishingProcess.processId, reviewerId).subscribe(
            ((response: PublishingProcess) => {
              this.toastr.success('Success', 'Reviewer assigned');
              this.publishingProcess = response;
              this.showButtons();
            }), ((error: any) => {
              this.toastr.error('Error', 'Unknown error');
              console.log(JSON.stringify(error));
            })
          );
        }
      }
    );
  }

  accept() {
    this.publishingProcessService.updatePaperStatus(this.publishingProcess.processId, 'ACCEPTED').subscribe(
      ((response: PublishingProcess) => {
        this.toastr.success('Success', 'The paper has been accepted');
        this.publishingProcess = response;
        this.showButtons();
        setTimeout(function() {
          location.reload();
        }, 2000);
      }), (error: any) => {
        this.toastr.error('Error', 'Unknown error');
        console.log(JSON.stringify(error));
      }
    );
  }

  revise() {
    this.publishingProcessService.updatePaperStatus(this.publishingProcess.processId, 'NEW_REVISION').subscribe(
      ((response: PublishingProcess) => {
        this.toastr.success('Success', 'The paper has been sent for revision');
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
    this.publishingProcessService.updatePaperStatus(this.publishingProcess.processId, 'REJECTED').subscribe(
      ((response: PublishingProcess) => {
        this.toastr.success('Success', 'The paper has been rejected');
        this.publishingProcess = response;
        this.showButtons();
        setTimeout(function() {
          location.reload();
        }, 2000);
      }), (error: any) => {
        this.toastr.error('Error', 'Unknown error');
        console.log(JSON.stringify(error));
      }
    );
  }

  viewHtmlPaper() {
    this.paperService.getHtml(this.publishingProcess.latestPaperId);
  }

  viewPdfPaper() {
    this.paperService.getPdf(this.publishingProcess.latestPaperId);
  }

  viewXmlPaper() {
    this.paperService.getXml(this.publishingProcess.latestPaperId);
  }

  viewHtmlLetter() {
    this.paperService.getLetterHtml(this.publishingProcess.latestPaperId);
  }

  viewPdfLetter() {
    this.paperService.getLetterPdf(this.publishingProcess.latestPaperId);
  }

  viewXmlLetter() {
    this.paperService.getLetterXml(this.publishingProcess.latestPaperId);
  }

  viewHtmlReviews() {
    this.reviewService.getHtml(this.publishingProcess.finishedReviewsIds);
  }

  viewMergedReviews() {
    this.reviewService.getMergedHtml(this.publishingProcess.finishedReviewsIds);
  }

  viewPdfReviews() {
    this.reviewService.getPdf(this.publishingProcess.finishedReviewsIds);
  }

  viewXmlReviews() {
    this.reviewService.getXml(this.publishingProcess.finishedReviewsIds);
  }

}
