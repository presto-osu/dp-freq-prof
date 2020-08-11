#!/bin/bash

MODULE=randomized-response

JAR_FILE=./${MODULE}/build/libs/${MODULE}-1.0-SNAPSHOT-all.jar
DATASET_DIR=../dataset
EPSILON=$1
GS=$2
TRIALS=$4
CSVDIR=$3

MAIN_CLASS=presto.MainRRBinomialProjection

./gradlew :${MODULE}:shadowJar

run() {
    APP=$1
    echo "==================================: ${APP}"
    java -cp ${JAR_FILE} ${MAIN_CLASS} ${DATASET_DIR}/${APP} \
        ${DATASET_DIR}/${APP}.json "${EPSILON}" ${TRIALS} "${GS}" "${CSVDIR}"
}

run barometer
run bible
run dpm
run drumpads
run equibase
run localtv
run loctracker
run mitula
run moonphases
run parking
run parrot
run post
run quicknews
run speedlogic
run vidanta
