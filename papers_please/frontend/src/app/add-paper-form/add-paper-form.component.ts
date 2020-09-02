import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { PaperService } from '../_service/paper.service';
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

  constructor(private formBuilder: FormBuilder,
              private paperService: PaperService,
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
      this.toastr.error('Error', 'You must choose scientific paper');
      return;
    }

    let postReq: any;
    postReq = this.paperService.addScientificPaper(this.paper);

    postReq.subscribe(
      (response => {
        this.toastr.success('Success', 'Your paper was submitted');
        this.processId = response.toString();
      }), (error => {
        if (error.error.text !== undefined && error.error.text.startsWith('process')) {
          this.toastr.success('Success', 'Your paper was submitted');
          this.processId = error.error.text;
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

  openPaperInput() {
    document.getElementById('scientificPaper').click();
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

}
