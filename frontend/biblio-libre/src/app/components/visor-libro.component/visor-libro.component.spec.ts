import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VisorLibroComponent } from './visor-libro.component';

describe('VisorLibroComponent', () => {
  let component: VisorLibroComponent;
  let fixture: ComponentFixture<VisorLibroComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VisorLibroComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VisorLibroComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
