import { Component,ViewChild,ElementRef } from '@angular/core';

@Component({
  selector: 'app-login.component',
  imports: [],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  
@ViewChild('container') container!: ElementRef;

singUpClick(){
    console.log(this.container)
  if (this.container){
    this.container.nativeElement.classList.add("right-panel-active");  }
}

singInClick(){

if (this.container)
{
    this.container.nativeElement.classList.remove("right-panel-active");  }
}
}



