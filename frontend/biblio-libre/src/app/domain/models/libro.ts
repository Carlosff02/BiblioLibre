import { Autor } from "./autor";

export interface Libro{
  idlibro:number|null;
  titulo:string;
  autor:Autor | null;
  descargas:number;
  imgSrc:string;
  textHtml:string;
  epub:string;
}
