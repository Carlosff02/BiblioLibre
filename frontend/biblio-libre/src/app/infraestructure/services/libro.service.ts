import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Libro } from '../../domain/models/libro';
import { constants } from '../../domain/constants/constants';

@Injectable({
  providedIn: 'root'
})
export class LibroService {

  libroUrl = `${constants.apiUrl}/libros`

  constructor(private http:HttpClient){

  }

  listarLibrosPopulares():Observable<Libro[]>{
    return this.http.get<Libro[]>(`${this.libroUrl}/populares`)
  }

  buscarPorIdioma(idioma:string):Observable<Libro[]>{
    return this.http.get<Libro[]>(`${this.libroUrl}/buscar-por-idioma/${idioma}`)
  }

  buscarPorNombre(titulo:string):Observable<Libro>{
    return this.http.get<Libro>(`${this.libroUrl}/${titulo}`)
  }

}
