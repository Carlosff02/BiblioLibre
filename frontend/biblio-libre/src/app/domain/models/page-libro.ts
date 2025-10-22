import { Libro } from "./libro";

export interface PageLibro{
  content:Libro[];
  totalElements:number;
  totalPages:number;
}
