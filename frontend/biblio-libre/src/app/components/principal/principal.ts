import { Component, OnInit } from '@angular/core';
import { LibroService } from '../../infraestructure/services/libro.service';
import { Libro } from '../../domain/models/libro';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-principal',
  imports: [RouterLink],
  templateUrl: './principal.html',
  styleUrl: './principal.css'
})
export class Principal implements OnInit{

  libros:Libro[] = [];

  constructor(private libroService:LibroService){

  }
  ngOnInit(): void {
    this.listarLibrosPopulares();
  }

  listarLibrosPopulares(){
    this.libroService.listarLibrosPopulares().subscribe({
      next:(res)=> {
        this.libros = res;
      },error:(err)=> {
        console.error(err);
        alert("Se ha producido un error listando los libros")
      }
    })
  }

}
