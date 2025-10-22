
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
  page = 1;
  totalElementos=0;
  totalPaginas=0;

  maxVisible = 5; // cantidad de botones numéricos visibles
paginasVisibles: number[] = [];
  constructor(private libroService:LibroService){

  }
  ngOnInit(): void {
    this.buscarLibrosPorIdioma(this.idiomaSeleccionado);
  }



actualizarPaginacion() {
  const half = Math.floor(this.maxVisible / 2);
  let start = Math.max(1, this.page - half);
  let end = Math.min(this.totalPaginas, start + this.maxVisible - 1);

  // Si estamos al final, desplazamos la ventana hacia atrás
  if (end - start + 1 < this.maxVisible && start > 1) {
    start = Math.max(1, end - this.maxVisible + 1);
  }

  this.paginasVisibles = [];
  for (let i = start; i <= end; i++) {
    this.paginasVisibles.push(i);
  }
}

cambiarPagina(nuevaPagina: number) {
  if (nuevaPagina < 1 || nuevaPagina > this.totalPaginas || nuevaPagina === this.page) {
    return;
  }
  this.page = nuevaPagina;
  this.actualizarPaginacion();

  // 👉 Aquí llamas a tu servicio para cargar libros de esa página
  this.buscarLibrosPorIdioma(this.idiomaSeleccionado);
}
seleccionarIdioma(idioma: string) {
  if (this.idiomaSeleccionado === idioma) return;
  this.idiomaSeleccionado = idioma;
  this.page = 1; // 🔹 Reinicia a la primera página al cambiar idioma
  this.buscarLibrosPorIdioma(idioma === 'todos' ? 'english' : idioma);
}
  buscarLibrosPorIdioma(idioma:string){
    this.procesandoDatos=true;
    this.idiomaSeleccionado=idioma;
    this.libroService.buscarPorIdioma(
      {
        idioma:idioma,
        page:this.page
      }
    ).subscribe({
      next:(res)=>{
          this.libros.set(res.content);
      this.totalElementos = res.totalElements;
      this.totalPaginas = res.totalPages;
      this.actualizarPaginacion(); // 🔹 Aquí recién generas las páginas
      this.procesandoDatos = false;
      },
      error:(err)=>{
        this.procesandoDatos=false;
        console.error(err);
        alert("Se ha producido un error listando los libros por idioma");
      }
    })
  }

}
