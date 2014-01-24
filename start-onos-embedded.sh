#!/bin/bash

# Set paths
if [ -z "${ONOS_HOME}" ]; then
        ONOS_HOME=`dirname $0`
fi
ONOS_LOGBACK="${ONOS_HOME}/logback.`hostname`.xml"
LOGDIR=${ONOS_HOME}/onos-logs
ONOS_LOG="${LOGDIR}/onos.`hostname`.log"
PCAP_LOG="${LOGDIR}/onos.`hostname`.pcap"
LOGS="$ONOS_LOG $PCAP_LOG"

# Set JVM options
JVM_OPTS=""
JVM_OPTS="$JVM_OPTS -server -d64"
JVM_OPTS="$JVM_OPTS -Xmx2g -Xms2g -Xmn800m"
#JVM_OPTS="$JVM_OPTS -XX:+UseParallelGC -XX:+AggressiveOpts -XX:+UseFastAccessorMethods"
JVM_OPTS="$JVM_OPTS -XX:+UseConcMarkSweepGC -XX:+UseAdaptiveSizePolicy -XX:+AggressiveOpts -XX:+UseFastAccessorMethods"
JVM_OPTS="$JVM_OPTS -XX:MaxInlineSize=8192 -XX:FreqInlineSize=8192"
JVM_OPTS="$JVM_OPTS -javaagent:$ONOS_HOME/lib/jamm-0.2.5.jar"
JVM_OPTS="$JVM_OPTS -XX:CompileThreshold=1500 -XX:PreBlockSpin=8 \
		-XX:+UseThreadPriorities \
		-XX:ThreadPriorityPolicy=42 \
		-XX:+UseCompressedOops \
		-Dcassandra.compaction.priority=1 \
		-Dcom.sun.management.jmxremote.port=7199 \
		-Dcom.sun.management.jmxremote.ssl=false \
		-Dcom.sun.management.jmxremote.authenticate=false"
JVM_OPTS="$JVM_OPTS -Dhazelcast.logging.type=slf4j"

#JVM_OPTS="$JVM_OPTS -Dpython.security.respectJavaAccessibility=false"

# Set Main class to start ONOS core
MAIN_CLASS="net.onrc.onos.ofcontroller.core.Main"

if [ -z "${MVN}" ]; then
    MVN="mvn"
fi

if [ ! -f ${ONOS_HOME}/.javacp ]; then
  ${MVN} -f ${ONOS_HOME}/pom.xml compile
fi
JAVA_CP=`cat ${ONOS_HOME}/.javacp`
JAVA_CP="${JAVA_CP}:${ONOS_HOME}/target/classes"

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
<configuration scan="true" scanPeriod="1 minutes" debug="true">
<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
<encoder>
<pattern>%level [%logger:%thread] %msg%n</pattern>
</encoder>
</appender>

<appender name="FILE" class="ch.qos.logback.core.FileAppender">
<file>${ONOS_LOG}</file>
<encoder>
<pattern>%date %level [%thread] %logger{10} [%file:%line] %msg%n</pattern>
<immediateFlush>true</immediateFlush>
</encoder>
</appender>

<logger name="org" level="WARN"/>
<logger name="LogService" level="WARN"/> <!-- Restlet access logging -->
<logger name="net.floodlightcontroller.logging" level="WARN"/>
<logger name="com.thinkaurelius.titan" level="INFO"/>

<root level="DEBUG">
<appender-ref ref="FILE" />
</root>
</configuration>
EOF_LOGBACK

  # Run ONOS
  echo "Starting ONOS controller ..."
  echo 

  # XXX MVN has to run at the project top dir..
  cd ${ONOS_HOME}
  java ${JVM_OPTS} -Dlogback.configurationFile=${ONOS_LOGBACK} -cp ${JAVA_CP} ${MAIN_CLASS} -cf ${ONOS_HOME}/conf/onos-embedded.properties > ${LOGDIR}/onos.`hostname`.stdout 2>${LOGDIR}/onos.`hostname`.stderr &

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

  sudo -b /usr/sbin/tcpdump -n -i eth0 -s0 -w ${PCAP_LOG} 'tcp port 6633' > /dev/null  2>&1
}

function stop {
  # Kill the existing processes
  flpid=`jps -l |grep ${MAIN_CLASS} | awk '{print $1}'`
  tdpid=`ps -edalf |grep tcpdump |grep ${PCAP_LOG} | awk '{print $4}'`
  pids="$flpid $tdpid"
  for p in ${pids}; do
    if [ x$p != "x" ]; then
      kill -KILL $p
      echo "Killed existing process (pid: $p)"
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
    echo "Usage: $0 {start|stop|status|startifdown}"
    exit 1
esac
