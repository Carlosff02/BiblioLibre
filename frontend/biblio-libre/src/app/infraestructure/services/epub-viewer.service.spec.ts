import { TestBed } from '@angular/core/testing';

import { EpubViewerService } from './epub-viewer.service';

describe('EpubViewerService', () => {
  let service: EpubViewerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EpubViewerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
