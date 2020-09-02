import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/internal/Observable';
import { Paper } from '../_model/paper.model';

declare var require: any;
const convert = require('xml-js');

const URL = 'http://localhost:8088/api/scientificPapers';

@Injectable({
  providedIn: 'root'
})
export class PaperService {

  constructor(private http: HttpClient) { }

  httpOptions = {
    headers: new HttpHeaders({
      'Content-Type':  'application/xml',
      'Response-Type': 'text'
    })
  };

  addScientificPaper(paperXml: string) {
    return this.http.post(URL, paperXml, this.httpOptions);
  }

  responseToArray(response: any): Paper[] {
    const returnPapers: Paper[] = [];
    if (response != null) {

      const jsonResponse = JSON.parse(convert.xml2json(response, { compact: true, spaces: 4 } ));
      let papers = jsonResponse.search.paper;
      if (!papers)
        return returnPapers;

      // if not an array, there is only one result
      if (!Array.isArray(papers))
        papers = [papers];

      for ( const paper of papers ) {
        const authorsList = [];
        for (const author of paper.author) {
          authorsList.push(author._text);
        }

        const keywordsList = [];
        for (const keyword of paper.keyword) {
          keywordsList.push(keyword._text);
        }

        const scientificPaper: Paper = {
            id : paper.id._text,
            processId: paper.process_id._text,
            paperStatus: paper.paper_status._text,
            title : paper.title._text,
            recievedDate: paper.recieved_date._text,
            acceptedDate : paper.accepted_date._text,
            authors : authorsList,
            keywords: keywordsList
        };

        returnPapers.push(scientificPaper);
      }
    }
    return returnPapers;
  }
}
