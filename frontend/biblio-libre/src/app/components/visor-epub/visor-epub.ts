import { Component, ElementRef, OnInit, signal, ViewChild } from '@angular/core';
import ePub, { Book, Rendition } from 'epubjs';

@Component({
  selector: 'app-visor-epub',
  imports: [],
  templateUrl: './visor-epub.html',
  styleUrl: './visor-epub.css'
})
export class VisorEpub  implements OnInit{
  ngOnInit(): void {
    this.visualizarEpub();
  }

   @ViewChild('viewer', { static: true}) viewerRef!: ElementRef<HTMLDivElement>;
    book = signal<Book|null>(null);
    private rendition!:Rendition;

  visualizarEpub(){
    const url = '/pg2000-images-3.epub';
    
    if(url){
      console.log(url)
    this.book.set(ePub(url))
    this.rendition = this.book()?.renderTo(this.viewerRef.nativeElement, {
      width:'100%',
      height:'100%',
  allowScriptedContent: true
    }) as Rendition;
    this.rendition.display();
    }
  }
  goPrev(){
      this.rendition.prev();
    }
    goNext(){
      this.rendition.next();
    }

}
