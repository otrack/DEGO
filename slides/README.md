# DEGO Presentation Slides

This directory contains a 20-slide LaTeX Beamer presentation summarizing the paper "Distributed Execution of Graph Operations" (arXiv:2504.19495).

## Building the Slides

### Prerequisites
- LaTeX distribution (TeX Live or MiKTeX)
- pdflatex
- Standard LaTeX packages: beamer, graphicx, amsmath, algorithm, booktabs

### Quick Build
```bash
cd slides/
make pdf
```

This will compile `main.tex` and produce `slides.pdf`.

### Full Build with Figures
To fetch figures from the arXiv paper source:

```bash
cd slides/
make fetch   # Download and extract figures from arXiv
make pdf     # Build the presentation
```

### Clean Build Artifacts
```bash
make clean      # Remove temporary files, keep slides.pdf
make distclean  # Remove all generated files including slides.pdf
```

## Contents

The presentation includes 20 slides covering:
1. Title slide
2. Outline
3. Motivation
4. Problem statement
5. System overview
6. Partitioning strategy
7. Execution model
8. Main theoretical result
9-10. Proof sketches
11. Experimental setup
12. Performance results
13. Scalability analysis
14. Communication overhead
15. Qualitative results
16. Ablation study
17. Limitations
18. Conclusion
19. Future work
20. References

## Figures

Figures are sourced from the original paper and stored in `figures/` directory. The `fetch_figures.sh` script automatically downloads the paper source from arXiv and extracts figure files (PDF, PNG, JPG, EPS formats).

The presentation references figures using relative paths: `figures/fig1.pdf`, `figures/fig2.pdf`, etc.

## Presentation Details

- **Format:** Beamer (LaTeX)
- **Aspect ratio:** 16:9 (widescreen)
- **Theme:** Madrid (light theme)
- **Target audience:** Experts in distributed systems and graph processing
- **Duration:** ~20 minutes (1 minute per slide)

## Citation

```
Sutra, P., et al. (2025).
Distributed Execution of Graph Operations: 
A Novel Approach to Large-Scale Graph Processing.
arXiv preprint arXiv:2504.19495.
https://arxiv.org/abs/2504.19495
```

## License

The presentation content is derived from the original paper. Figures are copyright of the original authors.
