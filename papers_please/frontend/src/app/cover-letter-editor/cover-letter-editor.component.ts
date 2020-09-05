import { Component, OnInit } from '@angular/core';
import { CoverLetterService } from '../_service/cover-letter.service';
import { XonomyCoverLetterService } from '../_service/xonomy-cover-letter.service';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

declare const Xonomy: any;

@Component({
  selector: 'app-cover-letter-editor',
  templateUrl: './cover-letter-editor.component.html',
  styleUrls: ['./cover-letter-editor.component.scss']
})
export class CoverLetterEditorComponent implements OnInit {

  coverLetter: string;
  processId: string;

  constructor(private clService: CoverLetterService,
              private xonomyService: XonomyCoverLetterService,
              private router: Router,
              private toastr: ToastrService) { }

  ngOnInit() {
    this.processId = localStorage.getItem('processId');
    localStorage.removeItem('processId');
    this.setXonomy();
  }

  setXonomy() {
    this.coverLetter = '<cover_letter xmlns="https://github.com/ivanmihajlov/papers_please/cover_letter" ' +
                        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"></cover_letter> ';
    const xonomy = document.getElementById('xonomy-letter-editor');
    Xonomy.render(this.coverLetter, xonomy, this.xonomyService.coverLetterElement);
  }

  loadTemplate() {
    this.clService.getTemplate().subscribe(
      (response => {
        const xonomy = document.getElementById('xonomy-letter-editor');
        Xonomy.render(response, xonomy, this.xonomyService.coverLetterElement);
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

  submitLetter() {
    this.clService.addCoverLetter(this.coverLetter, this.processId).subscribe(
      (response => {
        this.toastr.success('Success', 'Your cover letter was submitted');
        this.processId = response.toString();
        setTimeout(() => {
          this.router.navigate(['/my-papers']);
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

  addLetterXonomy() {
    this.coverLetter = Xonomy.harvest() as string;
    this.submitLetter();
  }

}
