"""Extract the RGA rows for the weapon models covered by this project.

The full RGA export (Référentiel Général des Armes, French Ministry of the
Interior, Licence Ouverte 2.0) is about 14 MB and changes over time, so the
repository only keeps the rows it needs, re-encoded as UTF-8.

Usage:
    python scripts/extract_rga.py path/to/export-catalogue-YYYYMMDD.csv

Download the export from:
    https://www.data.gouv.fr/datasets/referentiel-general-des-armes
"""

import csv
import sys
from pathlib import Path

# model id -> (output file, predicate on an RGA row)
MODELS = {
    "mauser-k98k": lambda row: row["marque"] == "MAUSER" and row["modele"] == "KARABINER 98 K",
}

OUTPUT_DIR = Path(__file__).resolve().parent.parent / "data" / "rga"


def main() -> int:
    if len(sys.argv) != 2:
        print(__doc__)
        return 2

    source = Path(sys.argv[1])
    with source.open(encoding="latin-1", newline="") as handle:
        reader = csv.DictReader(handle, delimiter=";")
        columns = reader.fieldnames
        rows = list(reader)

    for model_id, matches in MODELS.items():
        selected = sorted((row for row in rows if matches(row)), key=lambda row: row["referenceRGA"])
        target = OUTPUT_DIR / f"{model_id}.csv"
        with target.open("w", encoding="utf-8", newline="") as handle:
            writer = csv.DictWriter(handle, fieldnames=columns, delimiter=";", lineterminator="\n")
            writer.writeheader()
            writer.writerows(selected)
        print(f"{model_id}: {len(selected)} rows -> {target}")

    return 0


if __name__ == "__main__":
    sys.exit(main())
