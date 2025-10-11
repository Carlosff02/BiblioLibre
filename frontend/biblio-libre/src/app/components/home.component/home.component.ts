import { Component } from '@angular/core';
import { Nav } from '../nav/nav';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-home.component',
  imports: [Nav, RouterOutlet],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {

}
