#!/bin/bash

MODULE=laplace
JAR_FILE=./${MODULE}/build/libs/${MODULE}-1.0-SNAPSHOT-all.jar
./gradlew :${MODULE}:shadowJar

run_individual() {
  DATASET_DIR=../dataset
  EPSILON=$1
  PERCENTILE=$2
  TRIALS=$5
  CSVDIR=$3
  OPTIN=$4
  ALPHA=$6
  H=$7

  for APP in barometer bible dpm drumpads equibase localtv loctracker mitula moonphases parking parrot post quicknews speedlogic vidanta
  do
    echo "==================================: ${APP}"
    MAIN_CLASS=presto.MainLaplacePhdHybridRawTauHotness
    java -cp ${JAR_FILE} ${MAIN_CLASS} ${DATASET_DIR}/${APP} \
      ${DATASET_DIR}/${APP}.json "${EPSILON}" ${TRIALS} "-1" "${CSVDIR}" \
      "./log/lp/${APP}.csv" "${PERCENTILE}" "${OPTIN}" "${ALPHA}" "${H}"
  done
}

run_phd() {
  v=$1
  ep=$(python -c "import math; print(math.pow(2, $v))")

  for h in 100 75 50 25
  do

    csvdir=../intermediate-results/csv-lap-hybrid-raw-tau-2pow$v-simplegraph-h$h
    mkdir $csvdir

    for alpha in 0 1
    do
      echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
      echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
      echo "epsilon=$ep    h=$h   alpha=$alpha"
      echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
      echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
      for optin in 0.1
      do
        logprefix=log/lap.hybrid.$optin.2pow$v.h${h}.aplha${alpha}
        trials=30
        run_individual $ep $alpha $csvdir $optin $trials $alpha $h | tee $logprefix
        bash runall_stats.sh presto.MainStatsPercentile $ep $alpha qp $csvdir $trials | tee $logprefix.qp.stats
      done
    done
  done
}

for v in 0 -1 1; do
  run_phd $v
done
