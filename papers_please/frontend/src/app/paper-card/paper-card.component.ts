import { Component, OnInit, Input } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Paper } from '../_model/paper.model';
import { PaperService } from '../_service/paper.service';
import { UtilService } from '../_service/util.service';
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

  constructor(public dialog: MatDialog,
              private paperService: PaperService,
              private utilService: UtilService,
              private toastr: ToastrService,
              private router: Router) { }

  ngOnInit() {
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
    this.router.navigate(['/paper-quoted-by/' + this.paper.id]);
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

}
