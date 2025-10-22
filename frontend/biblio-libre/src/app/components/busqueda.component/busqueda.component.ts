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
  procesandoDatos = false;
  
  
  terminoBusqueda: string = '';
  tipoBusqueda: 'titulo' | 'autor' = 'titulo'; // Valor por defecto
  private busquedaActiva: 'idioma' | 'termino' = 'idioma'; // Controla el modo de listado

  // Variables existentes
  idiomaSeleccionado = 'español';
  libros = signal<Libro[]>([]);
  page = 1;
  totalElementos = 0;
  totalPaginas = 0;

  maxVisible = 5; 
  paginasVisibles: number[] = [];
  
  constructor(private libroService: LibroService) {
  }
  
  ngOnInit(): void {
    
    this.buscarLibrosPorIdioma(this.idiomaSeleccionado);
  }

  
  ejecutarBusquedaPorTermino(): void {
    if (!this.terminoBusqueda.trim()) {
      
      this.busquedaActiva = 'idioma';
      this.page = 1;
      this.buscarLibrosPorIdioma(this.idiomaSeleccionado);
      return;
    }

    this.busquedaActiva = 'termino';
    this.procesandoDatos = true;
    this.page = 1; // Reinicia la página
    
    // Asume que LibroService.buscarLibros ya fue implementado
    this.libroService.buscarLibros(this.terminoBusqueda, this.tipoBusqueda)
      .subscribe({
        next: (libros: Libro[]) => {
          this.libros.set(libros);
          
          // Actualiza la paginación para reflejar solo los resultados de la búsqueda por término
          this.totalElementos = libros.length;
          this.totalPaginas = libros.length > 0 ? 1 : 0; 
          this.paginasVisibles = [1];
          this.procesandoDatos = false;
        },
        error: (err) => {
          this.procesandoDatos = false;
          this.libros.set([]);
          this.totalElementos = 0;
          this.totalPaginas = 0;
          console.error('Error en la búsqueda por término:', err);
          alert("Se ha producido un error en la búsqueda.");
        }
      });
  }

 
  cambiarPagina(nuevaPagina: number): void {
    if (nuevaPagina < 1 || nuevaPagina > this.totalPaginas || nuevaPagina === this.page) {
      return;
    }
    
    this.page = nuevaPagina;
    this.actualizarPaginacion();

    // Solo llama a la búsqueda por idioma si es el modo activo
    if (this.busquedaActiva === 'idioma') {
      this.buscarLibrosPorIdioma(this.idiomaSeleccionado);
    } 
  }

  
  seleccionarIdioma(idioma: string): void {
    if (this.idiomaSeleccionado === idioma && this.busquedaActiva === 'idioma') return;
    
    this.idiomaSeleccionado = idioma;
    this.page = 1; 
    this.busquedaActiva = 'idioma';
    this.terminoBusqueda = ''; // Limpia el término
    this.buscarLibrosPorIdioma(idioma === 'todos' ? 'english' : idioma);
  }

  // Función original de búsqueda por idioma
  buscarLibrosPorIdioma(idioma: string): void {
    this.procesandoDatos = true;
    this.idiomaSeleccionado = idioma;
    this.libroService.buscarPorIdioma(
      {
        idioma: idioma,
        page: this.page
      }
    ).subscribe({
      next: (res) => {
        this.libros.set(res.content);
        this.totalElementos = res.totalElements;
        this.totalPaginas = res.totalPages;
        this.actualizarPaginacion(); 
        this.procesandoDatos = false;
      },
      error: (err) => {
        this.procesandoDatos = false;
        console.error(err);
        alert("Se ha producido un error listando los libros por idioma");
      }
    })
  }

  // Función original de paginación
  actualizarPaginacion(): void {
    const half = Math.floor(this.maxVisible / 2);
    let start = Math.max(1, this.page - half);
    let end = Math.min(this.totalPaginas, start + this.maxVisible - 1);

    if (end - start + 1 < this.maxVisible && start > 1) {
      start = Math.max(1, end - this.maxVisible + 1);
    }

    this.paginasVisibles = [];
    for (let i = start; i <= end; i++) {
      this.paginasVisibles.push(i);
    }
  }
}