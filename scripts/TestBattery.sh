#!/bin/bash
PATH=$PATH:/home/kambala/software/jdk1.6.0_25/bin/
rfile=`which $0`
rfiledir=`dirname ${rfile}`
. ${rfiledir}/support.sh


COMPUTENODES="1 2 4 8 16"
FAILPROB="0 1 2 4 8 16"
THRESHHOLD="100 80 60"
NUMBERSP=50000
GAUSSIANMEANP=50
GAUSSIANVARP=10
ITERATIONS=30


OUT=${rfiledir}/../results/results.csv

rm -f ${OUT}

test=0;

for computenodes in ${COMPUTENODES};
do
    for failprob in ${FAILPROB};
    do
        for threshhold in ${THRESHHOLD};
        do

            execstr="${rfiledir}/RunTest.sh -n ${NUMBERSP} -c ${computenodes} -p test${test}"

            for((i=0;i<${computenodes};i++));
            do
                execstr="${execstr} \"#p_${failprob}_#o_${threshhold}_#g_${GAUSSIANMEANP}_${GAUSSIANVARP}\""
            done

            echo $execstr

            for((i=0;i<${ITERATIONS};i++));
            do
                echo "Starting test ${test}"
                OFILE=`mktemp /tmp/dwd.XXXXXX`
                /usr/bin/time -f %e ${execstr} 2> ${OFILE}
                TIME=`tail -n 1 ${OFILE} | awk '{print $1;}'`
                echo "$test,${NUMBERSP},${GAUSSIANMEANP},${GAUSSIANVARP},${failprob},${threshhold},${computenodes},${i},${TIME}" >> ${OUT}
                rm -f ${OFILE}
                test=$((test+1))
            done
            
        done
    done

done