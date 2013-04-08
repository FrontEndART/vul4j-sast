#! /usr/bin/env python
# -*- Mode: python; py-indent-offset: 4; tab-width: 8; indent-tabs-mode: t; -*-

import pprint
import os
import sys
import subprocess
import json
import argparse
import io
import time

from flask import Flask, json, Response, render_template, make_response, request

#
# curl http://127.0.0.1:8080/wm/topology/route/00:00:00:00:00:00:0a:01/1/00:00:00:00:00:00:0a:04/1/json
#

## Global Var ##
ControllerIP="127.0.0.1"
ControllerPort=8080

DEBUG=1
pp = pprint.PrettyPrinter(indent=4)

app = Flask(__name__)

## Worker Functions ##
def log_error(txt):
  print '%s' % (txt)

def debug(txt):
  if DEBUG:
    print '%s' % (txt)

# @app.route("/wm/topology/route/<srcdpid>/<srcport>/<destdpid>/<destport>/json")
#
# Sample output:
# {'dstPort': {'port': {'value': 0}, 'dpid': {'value': '00:00:00:00:00:00:00:02'}}, 'srcPort': {'port': {'value': 0}, 'dpid': {'value': '00:00:00:00:00:00:00:01'}}, 'flowEntries': [{'outPort': {'value': 1}, 'flowEntryErrorState': None, 'flowEntryMatch': None, 'flowEntryActions': None, 'inPort': {'value': 0}, 'flowEntryId': None, 'flowEntryUserState': 'FE_USER_UNKNOWN', 'dpid': {'value': '00:00:00:00:00:00:00:01'}, 'flowEntrySwitchState': 'FE_SWITCH_UNKNOWN'}, {'outPort': {'value': 0}, 'flowEntryErrorState': None, 'flowEntryMatch': None, 'flowEntryActions': None, 'inPort': {'value': 9}, 'flowEntryId': None, 'flowEntryUserState': 'FE_USER_UNKNOWN', 'dpid': {'value': '00:00:00:00:00:00:00:02'}, 'flowEntrySwitchState': 'FE_SWITCH_UNKNOWN'}]}
#
def shortest_path(v1, p1, v2, p2):
  try:
    command = "curl -s http://%s:%s/wm/topology/route/%s/%s/%s/%s/json" % (ControllerIP, ControllerPort, v1, p1, v2, p2)
    debug("shortest_path %s" % command)

    result = os.popen(command).read()
    debug("result %s" % result)

    if len(result) == 0:
	print "No Path found"
	return;

    parsedResult = json.loads(result)
    debug("parsed %s" % parsedResult)
  except:
    log_error("Controller IF has issue")
    exit(1)

  srcSwitch = parsedResult['srcPort']['dpid']['value'];
  srcPort = parsedResult['srcPort']['port']['value'];
  dstSwitch = parsedResult['dstPort']['dpid']['value'];
  dstPort = parsedResult['dstPort']['port']['value'];

  print "DataPath: (src = %s/%s dst = %s/%s)" % (srcSwitch, srcPort, dstSwitch, dstPort);

  for f in parsedResult['flowEntries']:
    inPort = f['inPort']['value'];
    outPort = f['outPort']['value'];
    dpid = f['dpid']['value']
    print "  FlowEntry: (%s, %s, %s)" % (inPort, dpid, outPort)


if __name__ == "__main__":
  usage_msg = "Compute the shortest path between two switch ports in the Network MAP\n"
  usage_msg = usage_msg + "Usage: %s <src-dpid> <src-port> <dest-dpid> <dest-port>" % (sys.argv[0])

  # app.debug = False;

  # Usage info
  if len(sys.argv) > 1 and (sys.argv[1] == "-h" or sys.argv[1] == "--help"):
    print(usage_msg)
    exit(0)

  # Check arguments
  if len(sys.argv) < 5:
    log_error(usage_msg)
    exit(1)

  # Do the work
  shortest_path(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4]);
