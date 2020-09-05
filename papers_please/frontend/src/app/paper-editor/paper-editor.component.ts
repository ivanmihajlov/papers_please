import { Component, OnInit } from '@angular/core';
import { PaperService } from '../_service/paper.service';
import { XonomyPaperService } from '../_service/xonomy-paper.service';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

declare const Xonomy: any;

@Component({
  selector: 'app-paper-editor',
  templateUrl: './paper-editor.component.html',
  styleUrls: ['./paper-editor.component.scss']
})
export class PaperEditorComponent implements OnInit {

  paper: string;
  processId: string;
  revisionProcessId: string;
  revisionPaperTitle: string;
  isPaperSubmitted = false;

  constructor(private xonomyService: XonomyPaperService,
              private paperService: PaperService,
              private router: Router,
              private toastr: ToastrService) { }

  ngOnInit() {
    const revisionData = JSON.parse(localStorage.getItem('revisionData'));
    if (revisionData) {
      this.revisionPaperTitle = revisionData.paperTitle;
      this.revisionProcessId = revisionData.processId;
      localStorage.removeItem('revisionData');
    }
    this.setXonomy();
  }

  setXonomy() {
    this.paper = '<scientific_paper xmlns:jxb="http://java.sun.com/xml/ns/jaxb" ' +
                  'xmlns:xjc="http://java.sun.com/xml/ns/jaxb/xjc" ' +
                  'xmlns:pred="https://github.com/ivanmihajlov/papers_please/predicate/" ' +
                  'xmlns:rdfa="http://www.w3.org/ns/rdfa#" ' +
                  'xmlns="https://github.com/ivanmihajlov/papers_please" ' +
                  'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ' +
                  'xmlns:xs="http://www.w3.org/2001/XMLSchema#" id="" status="" version=""></scientific_paper>';
    const xonomy = document.getElementById('xonomy-editor');
    Xonomy.render(this.paper, xonomy, this.xonomyService.scientificPaperElement);
  }

  loadTemplate() {
    this.paperService.getTemplate().subscribe(
      (response => {
        const xonomy = document.getElementById('xonomy-editor');
        Xonomy.render(response, xonomy, this.xonomyService.scientificPaperElement);
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

  submitPaper() {
    let postReq: any;
    if (this.revisionPaperTitle )
      postReq = this.paperService.addPaperRevision(this.paper, this.revisionProcessId);
    else
      postReq = this.paperService.addScientificPaper(this.paper);

    postReq.subscribe(
      (response => {
        this.toastr.success('Success', 'Your paper was submitted');
        this.processId = response.toString();
        localStorage.setItem('processId', this.processId);
        this.isPaperSubmitted = true;
      }), (error => {
        if (error.error.text !== undefined && error.error.text.startsWith('process')) {
          this.toastr.success('Success', 'Your paper was submitted');
          this.processId = error.error.text;
          localStorage.setItem('processId', this.processId);
          this.isPaperSubmitted = true;
        } else {
          if (error.error.exception) {
            this.toastr.error('Error', error.error.exception);
          } else {
            this.toastr.error('Error', 'Unknown error');
            console.log(JSON.stringify(error));
          }
        }
      })
    );
  }

  addPaperXonomy() {
    this.paper = Xonomy.harvest() as string;
    this.submitPaper();
  }

  openClEditor() {
    if (this.processId === '' || this.processId === undefined || this.processId === null ) {
      this.toastr.info('Info', 'You have to submit the paper first');
      return;
    }
    this.router.navigate(['/add-cover-letter-editor']);
  }

}
