import { Component, OnInit } from '@angular/core';
import { FormGroup, Validators, FormControl } from '@angular/forms';
import { AuthenticationService } from '../_service/authentication.service';
import { Router } from '@angular/router';
import { CustomErorStateMatcher } from '../_util/custom-eror-state-matcher';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {

  form: FormGroup;
  matcher = new CustomErorStateMatcher();

  constructor(
    private authService: AuthenticationService,
    private router: Router,
    private toastr: ToastrService
  ) {
    this.form = new FormGroup(
      {
        name: new FormControl('', Validators.required),
        surname: new FormControl('', Validators.required),
        username: new FormControl('', Validators.required),
        email: new FormControl('', [Validators.required, Validators.email]),
        password: new FormControl('', [Validators.required, Validators.minLength(6)]),
        confirmPassword: new FormControl('', [Validators.required, Validators.minLength(6)])
      },
      {
        validators: this.checkPasswords
      }
    );
  }

  ngOnInit() {
  }

  checkPasswords(group: FormGroup) {
    const password = group.controls.password.value;
    const confirmPassword = group.controls.confirmPassword.value;
    return password === confirmPassword ? null : {notSame: true};
  }

  register() {
    const params = this.form.value;

    if (this.form.valid) {
      this.authService.register(
        params.name,
        params.surname,
        params.username,
        params.email,
        params.password,
        params.confirmPassword
      )
      .subscribe(
        response => {
          this.toastr.success('Success', 'Registration successful, please log in');
          this.router.navigate(['/login']);
        },
        response => {
          try {
            const errorResponse = JSON.parse(response.error);
            let errorAlert = '';
            errorResponse.errors.array.forEach(e => {
              errorAlert += `${e.defaultMessage}\n`;
            });
            this.toastr.error('Error', errorAlert);
          } catch (e) {
            this.toastr.error('Error', response.error);
          }
        }
      );
    }
  }
}
