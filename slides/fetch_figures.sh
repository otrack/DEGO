#!/bin/bash
# Script to download and extract figures from arXiv paper 2504.19495

set -e

ARXIV_ID="2504.19495"
WORK_DIR=$(mktemp -d)
FIGURES_DIR="$(dirname "$0")/figures"

echo "Downloading arXiv source for paper ${ARXIV_ID}..."

# Try /src endpoint first
if curl -L "https://arxiv.org/src/${ARXIV_ID}" -o "${WORK_DIR}/source.tar.gz" 2>/dev/null; then
    echo "Downloaded from /src endpoint"
elif curl -L "https://arxiv.org/e-print/${ARXIV_ID}" -o "${WORK_DIR}/source.tar.gz" 2>/dev/null; then
    echo "Downloaded from /e-print endpoint"
else
    echo "Error: Could not download paper source from arXiv"
    rm -rf "${WORK_DIR}"
    exit 1
fi

echo "Extracting source..."
cd "${WORK_DIR}"
tar -xzf source.tar.gz 2>/dev/null || gunzip -c source.tar.gz | tar -x 2>/dev/null || {
    echo "Error: Could not extract source archive"
    rm -rf "${WORK_DIR}"
    exit 1
}

echo "Copying figure files to ${FIGURES_DIR}..."
mkdir -p "${FIGURES_DIR}"

# Find and copy figure files (PDF, PNG, JPG, EPS)
figure_count=0
for ext in pdf PDF png PNG jpg JPG jpeg JPEG eps EPS; do
    for fig in $(find . -name "*.${ext}" 2>/dev/null); do
        figure_count=$((figure_count + 1))
        base_name=$(basename "$fig")
        cp "$fig" "${FIGURES_DIR}/${base_name}"
        echo "  Copied: ${base_name}"
    done
done

# Create numbered copies for easy reference
counter=1
cd "${FIGURES_DIR}"
for fig in *.pdf *.PDF 2>/dev/null; do
    [ -f "$fig" ] || continue
    if [ ! -f "fig${counter}.pdf" ]; then
        ln -s "$fig" "fig${counter}.pdf" 2>/dev/null || cp "$fig" "fig${counter}.pdf"
        counter=$((counter + 1))
    fi
done

# Cleanup
rm -rf "${WORK_DIR}"

if [ ${figure_count} -eq 0 ]; then
    echo "Warning: No figure files found in the source"
    exit 1
fi

echo "Successfully extracted ${figure_count} figure(s)"
echo "Figures are ready in: ${FIGURES_DIR}"
