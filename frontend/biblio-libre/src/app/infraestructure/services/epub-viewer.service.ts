import { ElementRef, Injectable } from '@angular/core';
import ePub, { Book, Rendition } from 'epubjs';
import { constants } from '../../domain/constants/constants';

@Injectable({
  providedIn: 'root'
})
export class EpubViewerService {

  private book?: Book;
  private rendition?: Rendition;

  constructor() {}

  visualizarEpub(epubUrl: string, viewerRef: ElementRef): void {
    if (!epubUrl) return;

    // URL de tu backend proxy
    const proxyUrl = `${constants.apiUrl}/visor-epub/epub?url=${encodeURIComponent(epubUrl)}`;

    console.log('📖 Descargando EPUB desde:', proxyUrl);

    // 🔹 Descarga el EPUB como binario y lo abre desde memoria (evita META-INF)
    fetch(proxyUrl)
      .then(response => response.arrayBuffer())
      .then(buffer => {
        this.book = ePub(buffer);
        this.rendition = this.book.renderTo(viewerRef.nativeElement, {
          width: '100%',
          height: '100%',
          allowScriptedContent: true
        });
        console.log("descargado")
        this.rendition.display();
      })
      .catch(err => console.error('Error cargando EPUB:', err));
  }

  goPrev(): void {
    this.rendition?.prev();
  }

  goNext(): void {
    this.rendition?.next();
  }
}
