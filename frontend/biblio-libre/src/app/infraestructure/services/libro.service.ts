import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Libro } from '../../domain/models/libro';
import { constants } from '../../domain/constants/constants';
import { PageLibro } from '../../domain/models/page-libro';

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

  buscarPorIdioma(httpParams:{
    idioma:string,
    page:number
  }):Observable<PageLibro>{
    const params = new HttpParams()
    .set('idioma', httpParams.idioma)
    .set('page', httpParams.page)
    return this.http.get<PageLibro>(`${this.libroUrl}/buscar-por-idioma`, {params})
  }

  buscarPorNombre(titulo:string):Observable<Libro>{
    return this.http.get<Libro>(`${this.libroUrl}/${titulo}`)
  }

}
