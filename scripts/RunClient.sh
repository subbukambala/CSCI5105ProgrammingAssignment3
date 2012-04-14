#!/bin/bash
PATH=$PATH:/home/kambala/software/jdk1.6.0_25/bin/
rfile=`which $0`
rfiledir=`dirname ${rfile}`
. ${rfiledir}/support.sh
(cd ${fqsrcdir} && java -cp .:${fqsrcdir}/:${fqjardir}commons-cli-1.2.jar Client $*)
