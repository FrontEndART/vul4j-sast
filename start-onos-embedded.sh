#!/bin/bash

# Set paths
ONOS_HOME=`dirname $0`
ONOS_JAR="${ONOS_HOME}/target/floodlight.jar"
ONOS_ONLY_JAR="${ONOS_HOME}/target/floodlight-only.jar"
ONOS_LOGBACK="${ONOS_HOME}/logback.xml"
LOGDIR=${ONOS_HOME}/onos-logs
ONOS_LOG="${LOGDIR}/onos.`hostname`.log"
PCAP_LOG="${LOGDIR}/onos.`hostname`.pcap"
LOGS="$ONOS_LOG $PCAP_LOG"

# Set JVM options
JVM_OPTS=""
JVM_OPTS="$JVM_OPTS -server -d64"
JVM_OPTS="$JVM_OPTS -Xmx2g -Xms2g -Xmn800m"
JVM_OPTS="$JVM_OPTS -XX:+UseParallelGC -XX:+AggressiveOpts -XX:+UseFastAccessorMethods"
JVM_OPTS="$JVM_OPTS -XX:MaxInlineSize=8192 -XX:FreqInlineSize=8192"
JVM_OPTS="$JVM_OPTS -XX:CompileThreshold=1500 -XX:PreBlockSpin=8 \
            -Dcom.sun.management.jmxremote.port=7199 \
              -Dcom.sun.management.jmxremote.ssl=false \
              -Dcom.sun.management.jmxremote.authenticate=false"

#JVM_OPTS="$JVM_OPTS -Dpython.security.respectJavaAccessibility=false"

# Set classpath to include titan libs
#CLASSPATH=`echo ${ONOS_HOME}/lib/*.jar ${ONOS_HOME}/lib/titan/*.jar | sed 's/ /:/g'`
CLASSPATH="${ONOS_ONLY_JAR}:${ONOS_HOME}/lib/*:${ONOS_HOME}/lib/titan/*"
MAIN_CLASS="net.onrc.onos.ofcontroller.core.Main"

if [ -z "${MVN}" ]; then
    MVN="mvn"
fi
#<logger name="net.floodlightcontroller.linkdiscovery.internal" level="TRACE"/>
#<appender-ref ref="STDOUT" />

function lotate {
    logfile=$1
    nr_max=${2:-10}
    if [ -f $logfile ]; then
	for i in `seq $(expr $nr_max - 1) -1 1`; do
	    if [ -f ${logfile}.${i} ]; then
		mv -f ${logfile}.${i} ${logfile}.`expr $i + 1`
	    fi
	done
	mv $logfile $logfile.1
    fi
}

function start {
  if [ ! -d ${LOGDIR} ]; then
    mkdir -p ${LOGDIR}
  fi
  # Backup log files
  for log in ${LOGS}; do
    echo "rotate log: $log"
    if [ -f ${log} ]; then
      lotate ${log}
    fi
  done

# Create a logback file if required
  cat <<EOF_LOGBACK >${ONOS_LOGBACK}
<configuration scan="true" debug="true">
<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
<encoder>
<pattern>%level [%logger:%thread] %msg%n</pattern>
</encoder>
</appender>

<appender name="FILE" class="ch.qos.logback.core.FileAppender">
<file>${ONOS_LOG}</file>
<encoder>
<pattern>%date %level [%thread] %logger{10} [%file:%line] %msg%n</pattern>
</encoder>
</appender>

<logger name="org" level="WARN"/>
<logger name="LogService" level="WARN"/> <!-- Restlet access logging -->
<logger name="net.floodlightcontroller.logging" level="WARN"/>

<root level="DEBUG">
<appender-ref ref="FILE" />
</root>
</configuration>
EOF_LOGBACK

  # Run floodlight
  echo "Starting ONOS controller ..."
  echo 
  #java ${JVM_OPTS} -Dlogback.configurationFile=${ONOS_LOGBACK} -jar ${ONOS_JAR} -cf ${ONOS_HOME}/onos.properties > /dev/null 2>&1 &
  #java ${JVM_OPTS} -Dlogback.configurationFile=${ONOS_LOGBACK} -cp ${CLASSPATH} ${MAIN_CLASS} -cf ${ONOS_HOME}/onos.properties > /dev/n

  ${MVN} exec:exec -Dexec.executable="java" -Dexec.args="${JVM_OPTS} -Dlogback.configurationFile=${ONOS_LOGBACK} -cp %classpath ${MAIN_CLASS} -cf ${ONOS_HOME}/conf/onos-embedded.properties" > ${LOGDIR}/onos.stdout 2>${LOGDIR}/onos.stderr &

  echo "Waiting for ONOS to start..."
  COUNT=0
  ESTATE=0
  while [ "$COUNT" != "10" ]; do
    COUNT=$((COUNT + 1))
    n=`jps -l |grep "${MAIN_CLASS}" | wc -l`
    if [ "$n" -ge "1" ]; then
      exit 0
    fi
    sleep $COUNT
  done
  echo "Timed out"
  exit 1

#  echo "java ${JVM_OPTS} -Dlogback.configurationFile=${ONOS_LOGBACK} -jar ${ONOS_JAR} -cf ./onos.properties > /dev/null 2>&1 &"
#  sudo -b /usr/sbin/tcpdump -n -i eth0 -s0 -w ${PCAP_LOG} 'tcp port 6633' > /dev/null  2>&1
}

function stop {
  # Kill the existing processes
  flpid=`jps -l |grep ${MAIN_CLASS} | awk '{print $1}'`
  tdpid=`ps -edalf |grep tcpdump |grep ${PCAP_LOG} | awk '{print $4}'`
  pids="$flpid $tdpid"
  for p in ${pids}; do
    if [ x$p != "x" ]; then
      kill -KILL $p
      echo "Killed existing prosess (pid: $p)"
    fi
  done
}

function deldb {
   # Delete the cassandra data
   if [ -d "/tmp/cassandra" ]; then
      rm -rf /tmp/cassandra/*
   fi
}

case "$1" in
  start)
    stop
    start 
    ;;
  startifdown)
    n=`jps -l |grep "${MAIN_CLASS}" | wc -l`
    if [ $n == 0 ]; then
      start
    else 
      echo "$n instance of onos running"
    fi
    ;;
  stop)
    stop
    ;;
  deldb)
    deldb
    ;;
  status)
    n=`jps -l |grep "${MAIN_CLASS}" | wc -l`
    echo "$n instance of onos running"
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|status|startifdown}"
    exit 1
esac
