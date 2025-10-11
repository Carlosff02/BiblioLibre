import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VisorEpub } from './visor-epub';

describe('VisorEpub', () => {
  let component: VisorEpub;
  let fixture: ComponentFixture<VisorEpub>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VisorEpub]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VisorEpub);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
