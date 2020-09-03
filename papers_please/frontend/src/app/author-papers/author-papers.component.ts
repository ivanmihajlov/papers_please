import { Component, OnInit } from '@angular/core';
import { UtilService } from '../_service/util.service';
import { Paper } from '../_model/Paper.model';
import { PaperService } from '../_service/paper.service';

@Component({
  selector: 'app-author-papers',
  templateUrl: './author-papers.component.html',
  styleUrls: ['./author-papers.component.scss']
})
export class AuthorPapersComponent implements OnInit {

  papers: Paper[] = [];
  allPapers: Paper[] = [];

  constructor(private utilService: UtilService,
              private paperService: PaperService) { }

  ngOnInit() {
    this.getPapers('?loggedAuthor=' + this.utilService.getLoggedUser());
  }

  getPapers(params: string) {
    this.papers = [];

    this.paperService.getScientificPapers(params).subscribe(
      (response) => {
        this.papers = this.paperService.responseToArray(response);
        this.allPapers = this.papers;
      });
  }

  sendSearchData(searchText: string) {
    this.getPapers('?searchText=' + searchText + '&loggedAuthor=' + this.utilService.getLoggedUser());
  }

  sendSearchParams(params: string) {
    if (params === '')
      params = 'loggedAuthor=' + this.utilService.getLoggedUser();
    else
      params = params + '&loggedAuthor=' + this.utilService.getLoggedUser();

    this.getPapers('?' + params);
  }

  filterByStatus(status: string) {
    if (status !== 'ALL') {
      this.papers = this.allPapers.filter( (event) => {
        return event.paperStatus === status;
      });
    } else
      this.papers = this.allPapers;
  }

}
