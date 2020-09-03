import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { PaperService } from '../_service/paper.service';
import { Paper } from '../_model/Paper.model';

@Component({
  selector: 'app-quoted-papers',
  templateUrl: './quoted-papers.component.html',
  styleUrls: ['./quoted-papers.component.scss']
})
export class QuotedPapersComponent implements OnInit {

  papers: Paper[];

  constructor(private router: Router,
              private paperService: PaperService) { }

  ngOnInit() {
    const splited = this.router.url.split('/');
    const id = splited[splited.length - 1];
    this.getPapers(id);
  }

  getPapers(paperId: string) {
    this.papers = [];
    this.paperService.getQuotedBy(paperId).subscribe(
      (response) => {
        this.papers = this.paperService.responseToArray(response);
      });
  }

}
