import { Component } from '@angular/core';

import { RouterOutlet } from '@angular/router';
import { Nav } from '../../components/nav/nav';

@Component({
  selector: 'app-home.component',
  imports: [Nav, RouterOutlet],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {

}
