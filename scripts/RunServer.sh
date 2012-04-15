#!/bin/bash
rfile=`which $0`
PATH=$PATH:/home/kambala/software/jdk1.6.0_25/bin/
rfiledir=`dirname ${rfile}`
. ${rfiledir}/support.sh
(cd ${fqsrcdir} && startRMIRegistry && java -cp .:${fqsrcdir}/:${fqjardir}commons-cli-1.2.jar Server $*)
