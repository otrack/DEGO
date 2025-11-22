.PHONY: all pdf fetch clean

# Default target
all: pdf

# Build the presentation PDF
pdf: slides.pdf

slides.pdf: main.tex
	@echo "Building slides.pdf..."
	pdflatex -interaction=nonstopmode -halt-on-error main.tex
	pdflatex -interaction=nonstopmode -halt-on-error main.tex
	@mv main.pdf slides.pdf
	@echo "Build complete: slides.pdf"

# Fetch figures from arXiv source
fetch:
	@echo "Fetching figures from arXiv..."
	./fetch_figures.sh

# Clean build artifacts
clean:
	@echo "Cleaning build artifacts..."
	rm -f *.aux *.log *.nav *.out *.snm *.toc *.vrb
	rm -f *.bbl *.blg *.synctex.gz
	rm -f main.pdf
	@echo "Clean complete"

# Clean everything including the final PDF
distclean: clean
	rm -f slides.pdf
