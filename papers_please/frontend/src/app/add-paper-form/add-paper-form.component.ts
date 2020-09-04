import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { PaperService } from '../_service/paper.service';
import { CoverLetterService } from '../_service/cover-letter.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-add-paper-form',
  templateUrl: './add-paper-form.component.html',
  styleUrls: ['./add-paper-form.component.scss']
})
export class AddPaperFormComponent implements OnInit {
  form: FormGroup;
  paper: string;
  processId: string;
  letter: string;
  isPaperSubmitted = false;

  constructor(private formBuilder: FormBuilder,
              private paperService: PaperService,
              private letterService: CoverLetterService,
              private toastr: ToastrService) { }

  ngOnInit() {
    this.createForm();
  }

  createForm() {
    this.form = this.formBuilder.group({
      scientificPaper: ['', Validators.required],
      coverLetter: ['', Validators.required]
    });
  }

  submitPaper() {
    if (this.paper === '' || this.paper === undefined || this.paper === null ) {
      this.toastr.error('Error', 'You must upload a paper');
      return;
    }

    let postReq: any;
    postReq = this.paperService.addScientificPaper(this.paper);

    postReq.subscribe(
      (response => {
        this.toastr.success('Success', 'Your paper was submitted');
        this.processId = response.toString();
        this.isPaperSubmitted = true;
      }), (error => {
        if (error.error.text !== undefined && error.error.text.startsWith('process')) {
          this.toastr.success('Success', 'Your paper was submitted');
          this.processId = error.error.text;
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

  submitLetter() {
    if (this.processId === '' || this.processId === undefined || this.processId === null ) {
      this.toastr.info('Info', 'You have to submit the paper first');
      return;
    }
    if (this.letter === '' || this.letter === undefined || this.letter === null ) {
      this.toastr.error('Error', 'You must upload a cover letter');
      return;
    }

    this.letterService.addCoverLetter(this.letter, this.processId).subscribe(
      (response => {
        this.toastr.success('Success', 'Your cover letter was submitted');
        this.processId = response.toString();
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

  openPaperInput() {
    document.getElementById('scientificPaper').click();
  }

  openLetterInput() {
    document.getElementById('coverLetter').click();
  }

  paperFileChange(files: any) {
    const reader = new FileReader();
    reader.readAsText(files[0], 'UTF-8');
    reader.onload = () => {
        this.paper = reader.result.toString();
        this.toastr.success('Success', 'File uploaded');
    };
    reader.onerror = () => {
      this.toastr.error('Error', 'Failed to read the file');
    };
  }

  letterFileChange(files: any, change: any) {
    const reader = new FileReader();
    reader.readAsText(files[0], 'UTF-8');
    reader.onload  = () => {
        this.letter = reader.result.toString();
        this.toastr.success('Success', 'File uploaded');
    };
    reader.onerror = () => {
      this.toastr.error('Error', 'Failed to read the file');
    };
  }

}
