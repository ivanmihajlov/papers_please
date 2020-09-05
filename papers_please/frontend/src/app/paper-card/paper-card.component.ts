import { Component, OnInit, Input } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Paper } from '../_model/paper.model';
import { PaperService } from '../_service/paper.service';
import { UtilService } from '../_service/util.service';
import { ConfirmationDialogComponent } from '../confirmation-dialog/confirmation-dialog.component';
import { AuthorPapersComponent } from '../author-papers/author-papers.component';
import { ToastrService } from 'ngx-toastr';
import { Router } from '@angular/router';


@Component({
  selector: 'app-paper-card',
  templateUrl: './paper-card.component.html',
  styleUrls: ['./paper-card.component.scss']
})
export class PaperCardComponent implements OnInit {

  @Input()
  paper: Paper;

  @Input()
  authorView: boolean;

  papers = [];

  constructor(public dialog: MatDialog,
              private paperService: PaperService,
              private utilService: UtilService,
              private authorPapersComponent: AuthorPapersComponent,
              private toastr: ToastrService,
              public router: Router) { }

  ngOnInit() {
  }

  addRevision(paperTitle: string, processId: string ) {
    localStorage.setItem('revisionData', JSON.stringify({paperTitle, processId}));
    this.router.navigate(['/add-paper']);
  }

  confirmWithdraw(title: string, paperId: string) {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: 'Withdraw paper \"' + title + '\"?'
    });
    dialogRef.afterClosed().subscribe(result => {
      if ( result === true ) {
        this.withdrawPaper(paperId);
       }
    });
  }

  withdrawPaper(paperId: string) {
    this.paperService.withdrawPaper(paperId).subscribe(
      (response => {
        this.toastr.success('Success', 'Paper withdrawn');
        this.authorPapersComponent.ngOnInit();
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

  metadataRdf() {
    this.paperService.getMetadataRdf(this.paper.id).subscribe(
      (response => {
            this.utilService.openAsXml(response);
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

  metadataJson() {
    this.paperService.getMetadataJson(this.paper.id).subscribe(
      (response => {
        this.utilService.openAsJson(response);
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

  quotedBy() {
    this.paperService.getQuotedBy(this.paper.id).subscribe(
      (response => {
        this.papers = this.paperService.responseToArray(response);
        if (this.papers.length > 0)
          this.router.navigate(['/paper-quoted-by/' + this.paper.id]);
        else
          this.toastr.info('Info', 'This paper isn\'t quoted');
      }));
  }

  viewXml() {
    this.paperService.getXml(this.paper.id);
  }

  viewHtml() {
    this.paperService.getHtml(this.paper.id);
  }

  viewPdf() {
    this.paperService.getPdf(this.paper.id);
  }

  viewLetterHtml() {
    this.paperService.getLetterHtml(this.paper.id);
  }

  viewLetterPdf() {
    this.paperService.getLetterPdf(this.paper.id);
  }

  viewLetterXml() {
    this.paperService.getLetterXml(this.paper.id);
  }

}
