import { Routes } from '@angular/router';
import { Principal } from './components/principal/principal';
import { BusquedaComponent } from './components/busqueda.component/busqueda.component';
import { VisorLibroComponent } from './components/visor-libro.component/visor-libro.component';
import { VisorEpub } from './components/visor-epub/visor-epub';
import { HomeComponent } from './ui/pages/home-page/home.component';

export const routes: Routes = [
  {path:'', component:HomeComponent, children:
    [
      {path:'', component:Principal,

      },
      {path:'busqueda', component:BusquedaComponent},
         {path:'busqueda/visor/:titulo', component:VisorLibroComponent},

    ]
  },
  {path:'busqueda/visor-epub/:titulo', component:VisorEpub}
];
