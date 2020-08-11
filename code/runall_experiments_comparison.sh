#!/bin/bash

mkdir log

run_comparison() {
  v=$1
  ep=$(python -c "import math; print(math.pow(2, $v))")

  for gs in -1 1 10 100
  do
    echo "------------ $ep $gs $v ---------------"
    bash runall_rr_projection.sh $ep $gs ../csv-rr-eln$v-comparison
    bash runall_stats.sh presto.MainStats $ep $gs qp ../csv-rr-eln$v-comparison | tee log/comparison.rr.eln$v.gs${gs}.qp.stats

    bash runall_lap_projection.sh $ep $gs ../csv-lap-eln$v-comparison
    bash runall_stats.sh presto.MainStats $ep $gs qp ../csv-lap-eln$v-comparison | tee log/comparison.lap.eln$v.gs${gs}.qp.stats
  done
}

for v in 0
do
  run_comparison $v
done
