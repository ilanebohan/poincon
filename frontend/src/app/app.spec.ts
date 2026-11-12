import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { App } from './app';
import { Identification, MarkingCode } from './identification-api';

const CODES: MarkingCode[] = [
  { code: 'bnz', manufacturer: 'Steyr-Daimler-Puch', location: 'Steyr', country: 'AT', period: null },
  { code: 'svw', manufacturer: 'Mauser-Werke AG', location: 'Oberndorf am Neckar', country: 'DE', period: null },
];

const BNZ: Identification = {
  model: {
    id: 'mauser-k98k',
    name: 'Mauser Karabiner 98k',
    defaultCaliber: '8 X 57 IS',
    markingLocation: 'Top of the receiver ring.',
  },
  code: CODES[0],
  caliber: '8 X 57 IS',
  matches: [
    {
      reference: 'BK887',
      type: 'CARABINE',
      manufacturer: 'WAFFENFABRIK STEYR DAIMLER PUCH (BNZ)',
      country: 'AUTRICHE',
      action: 'A REPETITION MANUELLE',
      overallLengthMm: 1110,
      barrelLengthMm: 600,
      magazineCapacity: 5,
      caliber: '8 X 57 IS',
      frenchClass: 'C 1° b)',
    },
  ],
  similar: [],
  availableCalibers: ['8 X 57 IS', '8 X 60 S'],
  sources: [],
};

describe('App', () => {
  let fixture: ComponentFixture<App>;
  let http: HttpTestingController;
  let page: HTMLElement;

  async function open(url: string): Promise<void> {
    history.replaceState(null, '', url);
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(App);
    http = TestBed.inject(HttpTestingController);
    page = fixture.nativeElement;
    http.expectOne('/api/models/mauser-k98k/codes').flush(CODES);
    await fixture.whenStable();
  }

  beforeEach(() => open('/'));

  afterEach(() => http.verify());

  it('identifies the code carried by the address, and keeps the address shareable', async () => {
    TestBed.resetTestingModule();
    await open('/?code=bnz');
    http.expectOne('/api/models/mauser-k98k/identify?code=bnz').flush(BNZ);
    await fixture.whenStable();

    expect(page.querySelector('.reference strong')?.textContent).toBe('BK887');
    expect(location.search).toBe('?code=bnz');
  });

  function submit(code: string): void {
    const input = page.querySelector('input')!;
    input.value = code;
    input.dispatchEvent(new Event('input'));
    page.querySelector('form')!.dispatchEvent(new Event('submit'));
  }

  it('offers the known codes as shortcuts', () => {
    const chips = [...page.querySelectorAll('.chip')].map((chip) => chip.textContent?.trim());

    expect(chips).toEqual(['bnz', 'svw']);
  });

  it('shows the maker and the RGA record of a code', async () => {
    submit('bnz');
    http.expectOne('/api/models/mauser-k98k/identify?code=bnz').flush(BNZ);
    await fixture.whenStable();

    expect(page.querySelector('h2')?.textContent).toContain('Steyr-Daimler-Puch');
    expect(page.querySelector('.reference strong')?.textContent).toBe('BK887');
  });

  it('identifies a known code when its shortcut is clicked', async () => {
    page.querySelector<HTMLButtonElement>('.chip')!.click();
    http.expectOne('/api/models/mauser-k98k/identify?code=bnz').flush(BNZ);
    await fixture.whenStable();

    expect(page.querySelector('.reference strong')?.textContent).toBe('BK887');
  });

  it('asks again with the caliber picked in the list', async () => {
    submit('bnz');
    http.expectOne('/api/models/mauser-k98k/identify?code=bnz').flush(BNZ);
    await fixture.whenStable();

    const select = page.querySelector('select')!;
    select.value = '8 X 60 S';
    select.dispatchEvent(new Event('change'));

    const request = http.expectOne((req) => req.url === '/api/models/mauser-k98k/identify');
    expect(request.request.params.get('caliber')).toBe('8 X 60 S');
    request.flush({ ...BNZ, caliber: '8 X 60 S', matches: [] });
    await fixture.whenStable();

    expect(page.querySelector('.empty')?.textContent).toContain('8 X 60 S');
  });

  it('asks for a code before calling the API', async () => {
    submit('   ');
    await fixture.whenStable();

    expect(page.querySelector('.error')?.textContent).toContain('Enter a code first.');
  });

  it('shows the reason given by the API when the code is unknown', async () => {
    submit('zzz');
    http
      .expectOne('/api/models/mauser-k98k/identify?code=zzz')
      .flush({ detail: "No code 'zzz' recorded for the Mauser Karabiner 98k." }, { status: 404, statusText: 'Not Found' });
    await fixture.whenStable();

    expect(page.querySelector('.error')?.textContent).toContain("No code 'zzz' recorded");
    expect(page.querySelector('.result')).toBeNull();
  });
});
