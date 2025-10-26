import { Component, ElementRef, OnInit, signal, ViewChild } from '@angular/core';
import { Libro } from '../../domain/models/libro';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { LibroService } from '../../infraestructure/services/libro.service';
import { EpubViewerService } from '../../infraestructure/services/epub-viewer.service';

@Component({
  selector: 'app-visor-epub',
  imports: [RouterLink],
  templateUrl: './visor-epub.html',
  styleUrl: './visor-epub.css'
})
export class VisorEpub implements OnInit {
  titulo = '';
  libroVisualizado = signal<Libro | null>(null);

  @ViewChild('viewer', { static: true }) viewerRef!: ElementRef<HTMLDivElement>;

  constructor(
    private route: ActivatedRoute,
    private libroService: LibroService,
    private epubViewer: EpubViewerService
  ) {}

  ngOnInit(): void {
    this.titulo = this.route.snapshot.paramMap.get('titulo')!;
    this.buscarLibroPorTitulo();
  }

  buscarLibroPorTitulo() {
    this.libroService.buscarPorNombre(this.titulo).subscribe({
      next: (res) => {
        this.libroVisualizado.set(res);

        setTimeout(() => {
        this.epubViewer.visualizarEpub(res.epub, this.viewerRef);
      }, 200);
      },
      error: (err) => console.error(err)
    });
  }

  visualizarEpub() {
    const url = this.libroVisualizado()?.epub;
    if (url) {
      // ✅ Ahora usamos el servicio
      this.epubViewer.visualizarEpub(url, this.viewerRef);
    }
  }

  goPrev() {
    this.epubViewer.goPrev();
  }

  goNext() {
    this.epubViewer.goNext();
  }
}
