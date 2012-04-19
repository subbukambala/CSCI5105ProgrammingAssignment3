#!/bin/bash
PATH=$PATH:/home/kambala/software/jdk1.6.0_25/bin/
rfile=`which $0`
rfiledir=`dirname ${rfile}`
. ${rfiledir}/support.sh



CNODES=$1
NUMBERS=$2
PREFIX=$3

OUTPREFIX=${rfiledir}/../results/${PREFIX}

${rfiledir}/RunServer.sh &> ${OUTPREFIX}_server.txt &
serverpid=$!


echo "Allows three seconds for server startup";
sleep 3;

cnodepids=
for (( i=0; i<CNODES; i++ ));
do
    ${rfiledir}/RunComputeNode.sh &> ${OUTPREFIX}_cnode_${i}.txt &
    cnodepids[i]=$!;
done


rm ${OUTPREFIX}_input.txt
for (( i=0 ; i<NUMBERS; i++ ));
do
    num=$RANDOM
    echo $RANDOM >> ${OUTPREFIX}_input.txt 
done

cat ${OUTPREFIX}_input.txt | sort -n > ${OUTPREFIX}_rubric.txt


echo "Countdown until client blast off ..."
for (( i=0 ; i<10 ; i++ ));
do
    echo $((10-$i));
    sleep 1;
done
echo "... countdown, initiating client!"

${rfiledir}/RunClient.sh localhost -f ${OUTPREFIX}_input.txt | grep "^result" \
| awk '{print $3}' > ${OUTPREFIX}_response.txt


for (( i=0 ; i<CNODES; i++ ));
do
    kill -9 ${cnodepids[i]}
    wait ${cnodepids[i]}
done

kill -9 ${serverpid}
wait ${serverpid}

diff ${OUTPREFIX}_rubric.txt ${OUTPREFIX}_response.txt > ${OUTPREFIX}_diff.txt


