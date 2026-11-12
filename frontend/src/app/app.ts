import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { Identification, IdentificationApi, MarkingCode } from './identification-api';

const MODEL_ID = 'mauser-k98k';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly api = inject(IdentificationApi);

  protected readonly knownCodes = signal<MarkingCode[]>([]);
  protected readonly code = signal('');
  protected readonly result = signal<Identification | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);

  constructor() {
    this.api.codes(MODEL_ID).subscribe({
      next: (codes) => this.knownCodes.set(codes),
      error: () => this.error.set("Couldn't load the reference data. Check that the API is running."),
    });

    const linkedCode = new URLSearchParams(location.search).get('code');
    if (linkedCode) {
      this.pick(linkedCode);
    }
  }

  protected typeCode(value: string): void {
    this.code.set(value);
    this.error.set(null);
  }

  protected pick(code: string): void {
    this.code.set(code);
    this.identify();
  }

  protected identify(caliber: string | null = null): void {
    const code = this.code().trim();
    if (!code) {
      this.error.set('Enter a code first.');
      return;
    }

    this.error.set(null);
    this.loading.set(true);
    this.api.identify(MODEL_ID, code, caliber).subscribe({
      next: (result) => {
        this.result.set(result);
        this.loading.set(false);
        history.replaceState(null, '', `?code=${encodeURIComponent(result.code.code)}`);
      },
      error: (response: HttpErrorResponse) => {
        this.result.set(null);
        this.loading.set(false);
        this.error.set(
          response.status === 404 && response.error?.detail
            ? response.error.detail
            : "Couldn't reach the API. Try again.",
        );
      },
    });
  }
}
