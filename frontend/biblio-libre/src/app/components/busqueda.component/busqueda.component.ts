
import { Component, OnInit, Signal, signal } from '@angular/core';
import { LibroService } from '../../infraestructure/services/libro.service';
import { Libro } from '../../domain/models/libro';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-busqueda.component',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './busqueda.component.html',
  styleUrl: './busqueda.component.css'
})
export class BusquedaComponent implements OnInit {
  procesandoDatos=false;
  idiomaSeleccionado='español';
  libros = signal<Libro[]>([]);
  constructor(private libroService:LibroService){

  }
  ngOnInit(): void {
    this.buscarLibrosPorIdioma(this.idiomaSeleccionado);
  }

  buscarLibrosPorIdioma(idioma:string){
    this.procesandoDatos=true;
    this.libroService.buscarPorIdioma(idioma).subscribe({
      next:(res)=>{
        this.libros.set(res);
        this.procesandoDatos=false;
      },
      error:(err)=>{
        this.procesandoDatos=false;
        console.error(err);
        alert("Se ha producido un error listando los libros por idioma");
      }
    })
  }

}
