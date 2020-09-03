import { Component, OnInit } from '@angular/core';
import { PaperService } from '../_service/paper.service';
import { Paper } from '../_model/Paper.model';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  papers: Paper[] = [];
  allPapers: Paper[] = [];

  constructor(private paperService: PaperService) { }

  ngOnInit() {
    this.getPapers('');
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
    this.getPapers('?searchText=' + searchText);
  }

  sendSearchParams(searchText: string) {
    this.getPapers('?' + searchText);
  }

}
