#!/bin/bash
PATH=$PATH:/home/kambala/software/jdk1.6.0_25/bin/
rfile=`which $0`
rfiledir=`dirname ${rfile}`
. ${rfiledir}/support.sh
. ${rfiledir}/shflags

CNODES=1
DEFINE_integer 'computenodes' ${CNODES} "Specify the number of compute node (Default is ${CNODES})" 'c'
NUMBERS=2000
DEFINE_integer 'numints' ${NUMBERS} "Specify the number of integers to sort (Default is ${NUMBERS})" 'n'
PREFIX=
DEFINE_string 'prefix' "${PREFIX}" "Specify the prefix to use for output files (Default is '${PREFIX}')" 'p'

DEFINE_boolean 'generate' false "Generate new random numbers (defaults to use ../results/unsorted.txt)" 'g'

read -r -d '' FLAGS_HELP <<EOF
USAGE: $0 [flags] compute_node_args

compute_node_args will apply the supply arguments to the compute nodes in
order. For instance, suppose you run $0 -c 2 "#c_50_#u_30" "#g_50_10_#o_100", 
then the first computer node will be called with "-c 50 -u 30" and the 
second compute node will be called with "-g 50 10 -o 100". The funny encoding
of - and ' ' is necessary for getopts.

EOF

# parse the command-line
FLAGS "$@" || exit 1
eval set -- "${FLAGS_ARGV}"

i=0;
while ! [ $# -eq 0 ]; 
do 
    args[i]=`echo $1 | sed "s/#/-/g" | sed "s/_/ /g"`;
    shift;
    i=$((i+1))
done 

if [[ "${FLAGS_generate}" == "${FLAGS_FALSE}" ]];
then
    echo "** Not generating random numbers ignoring -c option! **"
fi

echo "** Test Started **"
echo "** Compute nodes = ${FLAGS_computenodes} **"
echo "** Test size     = ${FLAGS_numints} **"
echo "** Output prefix = ${FLAGS_prefix} **"
for((i=0;i<${#args[*]};i++));
do
    echo "** Parameters for compute node ${i} = ${args[i]} **"
done

OUTPREFIX=${rfiledir}/../results/${FLAGS_prefix}
rm -f ${OUTPREFIX}*

${rfiledir}/RunServer.sh &> ${OUTPREFIX}_server.txt &
serverpid=$!

upP=`grep "Server started." ${OUTPREFIX}_server.txt`
while [[ -z ${upP} ]];
do
    sleep 1;
    upP=`grep "Server started." ${OUTPREFIX}_server.txt`
done;

cnodepids=
for (( i=0; i<FLAGS_computenodes; i++ ));
do
    ${rfiledir}/RunComputeNode.sh ${args[i]} &> ${OUTPREFIX}_cnode_${i}.txt &
    cnodepids[i]=$!;
    upP=`grep "started." ${OUTPREFIX}_cnode_${i}.txt`
    while [[ -z ${upP} ]];
    do
        sleep 1;
        upP=`grep "started." ${OUTPREFIX}_server.txt`
    done;
done


unsorted="${rfiledir}/../results/unsorted.dat"
sorted="${rfiledir}/../results/sorted.dat"


if [[ "${FLAGS_generate}" == "${FLAGS_TRUE}" ]];
then

    echo "** Generating random numbers **"
    rm ${OUTPREFIX}_input.txt
    for (( i=0 ; i<FLAGS_numints; i++ ));
    do
        num=$RANDOM
        echo $RANDOM >> ${OUTPREFIX}_input.txt 
    done

    sort -n ${OUTPREFIX}_rubric.txt
    unsorted=${OUTPREFIX}_input.txt
    sorted=${OUTPREFIX}_rubric.txt
fi

${rfiledir}/RunClient.sh localhost -f ${unsorted} &> ${OUTPREFIX}_client.txt
cat ${OUTPREFIX}_client.txt | grep "^result" | awk '{print $3}' > ${OUTPREFIX}_response.txt


for (( i=0 ; i<FLAGS_computenodes; i++ ));
do
    kill -9 ${cnodepids[i]}
    wait ${cnodepids[i]}
done

kill -9 ${serverpid}
wait ${serverpid}

diff ${sorted} ${OUTPREFIX}_response.txt > ${OUTPREFIX}_diff.txt

killall -9 java

