import { Component, ElementRef, OnInit, signal, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Libro } from '../../domain/models/libro';
import { LibroService } from '../../infraestructure/services/libro.service';
import ePub, { Book, Rendition } from 'epubjs';

@Component({
  selector: 'app-visor-libro.component',
  imports: [],
  templateUrl: './visor-libro.component.html',
  styleUrl: './visor-libro.component.css'
})
export class VisorLibroComponent implements OnInit{
    titulo = '';

    libroVisualizado = signal<Libro|null>(null);

    constructor(private route: ActivatedRoute,
      private libroService:LibroService
    ){

    }

    ngOnInit(): void {
      this.titulo = this.route.snapshot.paramMap.get('titulo')!;
      this.buscarLibroPorTitulo()
  }

  buscarLibroPorTitulo(){
    this.libroService.buscarPorNombre(this.titulo).subscribe({
      next:(res)=>{
        this.libroVisualizado.set(res);
        console.log(res)
      }, error:(err)=>{

        console.error(err)
      }
    })


  }

  visualizarEpub(){
    
  }



}
