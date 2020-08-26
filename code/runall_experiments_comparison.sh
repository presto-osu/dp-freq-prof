#!/bin/bash

mkdir log

run_comparison() {
  v=$1
  ep=$(python -c "import math; print(math.pow(2, $v))")

  for gs in -1 #1 10 100
  do
    echo "------------ $ep $gs $v ---------------"
    CSV="../intermediate-results/comparison$PARTITION-rr-eln$v"
    mkdir -p $CSV
    bash runall_rr_projection.sh $ep $gs $CSV 30
    bash runall_stats.sh presto.MainStats $ep $gs qp $CSV | tee log/comparison.rr.eln$v.gs${gs}.qp.stats$PARTITION

    CSV="../intermediate-results/comparison$PARTITION-lap-eln$v"
    mkdir -p $CSV
    bash runall_lap_projection.sh $ep $gs $CSV 30
    bash runall_stats.sh presto.MainStats $ep $gs qp $CSV | tee log/comparison.lap.eln$v.gs${gs}.qp.stats$PARTITION
  done
}

for v in 0
do
  run_comparison $v
done
