import { Component, OnInit, signal } from '@angular/core';
import { LibroService } from '../../infraestructure/services/libro.service';
import { Libro } from '../../domain/models/libro';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-busqueda',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './busqueda.component.html',
  styleUrls: ['./busqueda.component.css']
})
export class BusquedaComponent implements OnInit {
  procesandoDatos = signal<boolean>(false);

  // 🔹 Campos de búsqueda
  titulo = '';
  autor = '';
  idiomaSeleccionado = 'español';

  // 🔹 Resultados y paginación
  libros = signal<Libro[]>([]);
  page = 1;
  totalElementos = 0;
  totalPaginas = 0;

  maxVisible = 5;
  paginasVisibles: number[] = [];

  constructor(private libroService: LibroService) {}
  // Probando uno dos
  ngOnInit(): void {
    this.buscarLibros();
  }
  // Prueba de Jira
  // Prueba de Jira 5
  // 🔹 Método general de búsqueda
  buscarLibros() {
    this.procesandoDatos.set(true);
    this.libroService.buscarPorFiltros({
      titulo: this.titulo.trim(),
      autor: this.autor.trim(),
      idioma: this.idiomaSeleccionado,
      page: this.page
    }).subscribe({
      next: (res) => {
        this.libros.set(res.content);
        console.log(this.libros())
        this.totalElementos = res.totalElements;
        this.totalPaginas = res.totalPages;
        this.actualizarPaginacion();
        this.procesandoDatos.set(false);
      },
      error: (err) => {
        this.procesandoDatos.set(false);
        console.error(err);
        alert('Error al buscar los libros');
      }
    });
  }

  getClaseIdioma(idioma: string) {
  return {
    'text-indigo-600 font-semibold': this.idiomaSeleccionado === idioma,
    'text-gray-700 hover:text-indigo-600': this.idiomaSeleccionado !== idioma
  };
}


  // 🔹 Paginación
  actualizarPaginacion() {
    const half = Math.floor(this.maxVisible / 2);
    let start = Math.max(1, this.page - half);
    let end = Math.min(this.totalPaginas, start + this.maxVisible - 1);
    if (end - start + 1 < this.maxVisible && start > 1) {
      start = Math.max(1, end - this.maxVisible + 1);
    }
    this.paginasVisibles = [];
    for (let i = start; i <= end; i++) this.paginasVisibles.push(i);
  }
  
  //metodo paginacion
  animacion = '';
  cambiarPagina(nuevaPagina: number) {
    if (nuevaPagina < 1 || nuevaPagina > this.totalPaginas || nuevaPagina === this.page) return;
    // Dirección de movimiento
    this.animacion = nuevaPagina > this.page ? 'slide-right' : 'slide-left';
    this.page = nuevaPagina;

    // Esperar un instante para que la animación se note
    setTimeout(() => this.buscarLibros(), 200);
  }


  seleccionarIdioma(idioma: string) {
    if (this.idiomaSeleccionado === idioma) return;
    this.idiomaSeleccionado = idioma;
    this.page = 1;
    this.buscarLibros();
  }
}
