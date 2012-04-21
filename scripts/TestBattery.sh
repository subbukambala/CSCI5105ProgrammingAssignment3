#!/bin/bash
PATH=$PATH:/home/kambala/software/jdk1.6.0_25/bin/
rfile=`which $0`
rfiledir=`dirname ${rfile}`
. ${rfiledir}/support.sh
. ${rfiledir}/shflags

DEFINE_integer 'computenodes' "" "Specify the set of compute nodes to iterate over." 'c'
TAG=
DEFINE_string 'tag' "${TAG}" "Specify the tag for the output'${TAG}')" 't'

read -r -d '' FLAGS_HELP <<EOF
USAGE: $0 [flags]

Use RunTest.sh to generate alot of tests.

EOF

# parse the command-line
FLAGS "$@" || exit 1
eval set -- "${FLAGS_ARGV}"

if [[ -z ${FLAGS_computenodes} ]];
then 
    COMPUTENODES="1 2 4 8 16"
else
    COMPUTENODES=${FLAGS_computenodes}
fi
FAILPROB="0 2 4 8 16 32"
THRESHHOLD="100 80 60 50"
NUMBERSP=250
GAUSSIANMEANP=50
GAUSSIANVARP=10
ITERATIONS=3

export CLASSPATH=${CLASSPATH}:${fqsrcdir}

OUT=${rfiledir}/../results/results${FLAGS_tag}.csv

rm -f ${OUT}

test=0;

for computenodes in ${COMPUTENODES};
do
    for failprob in ${FAILPROB};
    do
        for threshhold in ${THRESHHOLD};
        do


            echo "New Parameters: ${computenodes} ${failprob} ${threshhold}"
            for((i=0;i<${ITERATIONS};i++));
            do
                echo "Starting test ${test}"

                execstr="${rfiledir}/RunTest.sh -n ${NUMBERSP} -c ${computenodes} -p test${test}-${FLAGS_tag}"

                for((j=0;j<${computenodes};j++));
                do
                    execstr="${execstr} #p_${failprob}_#o_${threshhold}_#g_${GAUSSIANMEANP}_${GAUSSIANVARP}"
                done

                echo "Executing -> $execstr"

                OFILE=`mktemp /tmp/dwd.XXXXXX`
                /usr/bin/time -f %e ${execstr} 2> ${OFILE}
                TIME=`tail -n 1 ${OFILE} | awk '{print $1;}'`
                goodP=`cat ${rfiledir}/../results/test${test}-${FLAGS_tag}_diff.txt`
                if [[ -z ${goodP} ]];
                then
                    good=1
                else
                    good=0
                fi
                echo "$test,${NUMBERSP},${GAUSSIANMEANP},${GAUSSIANVARP},${failprob},${threshhold},${computenodes},${i},${TIME},${good}" >> ${OUT}
                rm -f ${OFILE}
                test=$((test+1))
            done
            
        done
    done

done