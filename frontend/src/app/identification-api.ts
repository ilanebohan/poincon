import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface WeaponModel {
  id: string;
  name: string;
  defaultCaliber: string;
  markingLocation: string | null;
}

export interface MarkingCode {
  code: string;
  manufacturer: string;
  location: string | null;
  country: string | null;
  period: string | null;
}

export interface RgaRecord {
  reference: string;
  type: string;
  manufacturer: string;
  country: string;
  action: string;
  overallLengthMm: number | null;
  barrelLengthMm: number | null;
  magazineCapacity: number | null;
  caliber: string;
  frenchClass: string;
}

export interface Source {
  id: string;
  title: string;
  publisher: string | null;
  url: string | null;
  license: string | null;
}

export interface Identification {
  model: WeaponModel;
  code: MarkingCode;
  caliber: string;
  matches: RgaRecord[];
  similar: RgaRecord[];
  availableCalibers: string[];
  sources: Source[];
}

@Injectable({ providedIn: 'root' })
export class IdentificationApi {
  private readonly http = inject(HttpClient);

  codes(modelId: string): Observable<MarkingCode[]> {
    return this.http.get<MarkingCode[]>(`/api/models/${modelId}/codes`);
  }

  identify(modelId: string, code: string, caliber: string | null): Observable<Identification> {
    let params = new HttpParams().set('code', code);
    if (caliber) {
      params = params.set('caliber', caliber);
    }
    return this.http.get<Identification>(`/api/models/${modelId}/identify`, { params });
  }
}
