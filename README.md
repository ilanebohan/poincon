# Poinçon

An open, searchable reference for the markings stamped on historical firearms.

You read a marking on a piece, Poinçon tells you who made it and which record of the French
[RGA](https://www.data.gouv.fr/datasets/referentiel-general-des-armes) (Référentiel Général des
Armes) it corresponds to.

> **Status: early prototype.** One model is covered, the Mauser Karabiner 98k, with 12
> manufacturer codes. Every code comes from the manufacturer labels of the RGA and has not been
> checked against a printed reference yet.

## Why

Knowledge about proof marks, arsenal codes and inspector stamps is scattered across forums, scanned
books and PDFs. None of it is structured, searchable or reusable.

The French RGA adds a practical problem of its own. It holds 361 records for the Karabiner 98k
alone. In the original 8 x 57 IS caliber, 31 of them share the same barrel length and legal class
and differ essentially by manufacturer. Picking the right one means knowing that `bnz` on a
receiver stands for Steyr-Daimler-Puch.

## What it does today

- Looks up a manufacturer code (`bnz`, `svw`, `337`, `dot`, ...) and returns the maker and its location.
- Returns the matching RGA record for the chosen caliber: reference, legal class, dimensions.
- Warns when the RGA lists the same maker under another, near-identical record.
- Cites the source of every piece of data.

## Run it locally

Requirements: JDK 25 and Node.js 24.

Start the API on port 8080:

```bash
cd backend && ./mvnw spring-boot:run
```

Start the web app on port 4200:

```bash
cd frontend && npm install && npm start
```

Then open <http://localhost:4200>. A code can be linked directly: <http://localhost:4200/?code=bnz>.

## API

| Request | Returns |
| --- | --- |
| `GET /api/models` | The weapon models covered |
| `GET /api/models/{model}/codes` | The codes recorded for a model |
| `GET /api/models/{model}/identify?code=bnz&caliber=8 X 57 IS` | The maker and the matching RGA records. `caliber` defaults to the model's original caliber |
| `GET /api/rga/{reference}` | One RGA record |

Errors follow [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) (`application/problem+json`).

## Repository layout

```
data/markings/   one YAML file per weapon model: codes, makers, sources
data/rga/        the RGA records of each model, extracted from the official export
backend/         Spring Boot API (Java 25)
frontend/        Angular web app
scripts/         tooling to refresh the RGA extracts
```

The datasets in `data/` are the heart of the project. The API reads them at start-up, and the
back-end test suite runs against them, so a broken data file fails the build.

## Contributing data

Add or correct an entry in `data/markings/<model>.yaml`. The header of
[`mauser-k98k.yaml`](data/markings/mauser-k98k.yaml) documents each field. Two rules:

- **Every entry cites a source.** Declare it in the `sources` section and reference its id.
- **Facts only.** Do not copy tables, photos or text from books and websites.

To refresh the RGA extracts, download the latest export from data.gouv.fr and run:

```bash
python scripts/extract_rga.py path/to/export-catalogue.csv
```

## Tests

```bash
cd backend && ./mvnw test
```

```bash
cd frontend && npm test -- --watch=false
```

## Roadmap

- Document the period of use of each code, with sources.
- Inspector and proof marks, with reference photos.
- More models: Mosin-Nagant, Berthier, MAS 36.
- Dating by serial number range.
- French and Polish translations.

## Scope

Poinçon is a historical reference. It does not estimate prices, list items for sale or give legal
advice. The legal class it displays is the one published in the RGA, shown with its date.

## License and attribution

- Code: [MIT](LICENSE).
- Data in `data/markings/`: [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/).
- Data in `data/rga/`: extracted from the Référentiel Général des Armes published by the French
  Ministry of the Interior (SCAE) under the
  [Licence Ouverte 2.0](https://www.etalab.gouv.fr/licence-ouverte-open-licence/), export of
  2026-09-11.
