#!/bin/bash

MODULE=stats

JAR_FILE=./${MODULE}/build/libs/${MODULE}-1.0-SNAPSHOT-all.jar
DATASET_DIR=../dataset
MAIN_CLASS=$1
EPSILON=$2
GS=$3
TRIALS=30
AFTER=$4
CSVDIR=$5

./gradlew :${MODULE}:shadowJar

run() {
    APP=$1
    echo "==================================: ${APP}"
    java -cp ${JAR_FILE} ${MAIN_CLASS} ${DATASET_DIR}/${APP} \
        ${DATASET_DIR}/${APP}.json "${EPSILON}" ${TRIALS} "${GS}" "${CSVDIR}" "${AFTER}"
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
