This directory contains the code for randomization described in the paper. Below are the folders that contain essential code and results:

- `common` and `integer-lp` contain the common utility classes and the Matlab code
- `le-pairs-csv` contains the "<=" pairs using the static analysis from [our CC'20 paper](http://web.cse.ohio-state.edu/presto/pubs/cc20.pdf)
- `laplace` contains the Laplace-mechanism-based randomization described in the paper
- `randomized-response` contains the code for the randomized-response-based randomization
- `stats` includes code for printing statistics

## Prerequisites

- Java 1.8+
- MATLAB: Please follow the instructions on [MATLAB's website](https://www.mathworks.com/products/get-matlab.html) for installation and then export `matlab` to `PATH` of executables.

<!-- ## Install Google OR-Tools

```bash
curl -L https://github.com/google/or-tools/releases/download/v7.6/or-tools_ubuntu-18.04_v7.5.7466.tar.gz --output - | tar --directory tools -zxvf -
``` -->

## Run

Clone the repository:

```bash
$ git clone https://github.com/presto-osu/dp-freq-prof.git
$ cd dp-freq-prof
```

Download and extract [dataset.tar.gz](https://github.com/presto-osu/dp-freq-prof/releases/download/dataset/dataset.tar.gz) to `dp-freq-prof` and execute the following commands to reproduce the experimental results:

```bash
$ cd code
$ bash runall_experiments_comparison.sh # comparison of the two randomizers (Sec. 7.1)
$ bash runall_rr_experiments.sh # randomized response (Sec. 7.2 & 7.3)
$ bash runall_lap_experiments.sh # Laplace mechanism (Sec. 7.2 & 7.3)
$ export PARTITION=500  && bash runall_experiments_comparison.sh # high-similarity (Sec. 7.5)
$ export PARTITION=-500 && bash runall_experiments_comparison.sh # low-similarity (Sec. 7.5)
```

The log files are stored in folder `log`.
