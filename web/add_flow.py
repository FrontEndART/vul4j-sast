#! /usr/bin/env python
# -*- Mode: python; py-indent-offset: 4; tab-width: 8; indent-tabs-mode: t; -*-

import copy
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
ControllerIP = "127.0.0.1"
ControllerPort = 8080
MonitoringEnabled = False

DEBUG=0
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
	log_error("No Path found")
	exit(1);

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

  return parsedResult

def add_flow_path(flow_path):
  flow_path_json = json.dumps(flow_path)

  try:
    command = "curl -s -H 'Content-Type: application/json' -d '%s' http://%s:%s/wm/flow/add/json" % (flow_path_json, ControllerIP, ControllerPort)
    debug("add_flow_path %s" % command)
    result = os.popen(command).read()
    debug("result %s" % result)
    # parsedResult = json.loads(result)
    # debug("parsed %s" % parsedResult)
  except:
    log_error("Controller IF has issue")
    exit(1)

def delete_flow_path(flow_id):
  command = "curl -s \"http://%s:%s/wm/flow/delete/%s/json\"" % (ControllerIP, ControllerPort, flow_id)
  debug("delete_flow_path %s" % command)
  result = os.popen(command).read()
  debug("result %s" % result)
  # parsedResult = json.loads(result)
  # debug("parsed %s" % parsedResult)

def extract_flow_args(my_args):
  # Check the arguments
  if len(my_args) < 6:
    log_error(usage_msg)
    exit(1)

  # Extract the mandatory arguments
  my_flow_id = my_args[0]
  my_installer_id = my_args[1]
  my_src_dpid = my_args[2]
  my_src_port = my_args[3]
  my_dst_dpid = my_args[4]
  my_dst_port = my_args[5]

  #
  # Extract the "match" and "action" arguments
  #
  match = {}
  matchInPortEnabled = True		# NOTE: Enabled by default
  actions = []
  actionOutputEnabled = True		# NOTE: Enabled by default
  idx = 6
  while idx < len(my_args):
    action = {}
    arg1 = my_args[idx]
    idx = idx + 1
    # Extract the second argument
    if idx >= len(my_args):
      error_arg = "ERROR: Missing or invalid '" + arg1 + "' argument"
      log_error(error_arg)
      log_error(usage_msg)
      exit(1)
    arg2 = my_args[idx]
    idx = idx + 1

    if arg1 == "matchInPort":
      # Just mark whether inPort matching is enabled
      matchInPortEnabled = arg2 in ['True', 'true']
      # inPort = {}
      # inPort['value'] = int(arg2, 0)
      # match['inPort'] = inPort
      ## match['matchInPort'] = True
    elif arg1 == "matchSrcMac":
      srcMac = {}
      srcMac['value'] = arg2
      match['srcMac'] = srcMac
      # match['matchSrcMac'] = True
    elif arg1 == "matchDstMac":
      dstMac = {}
      dstMac['value'] = arg2
      match['dstMac'] = dstMac
      # match['matchDstMac'] = True
    elif arg1 == "matchVlanId":
      match['vlanId'] = int(arg2, 0)
      # match['matchVlanId'] = True
    elif arg1 == "matchVlanPriority":
      match['vlanPriority'] = int(arg2, 0)
      # match['matchVlanPriority'] = True
    elif arg1 == "matchEthernetFrameType":
      match['ethernetFrameType'] = int(arg2, 0)
      # match['matchEthernetFrameType'] = True
    elif arg1 == "matchIpToS":
      match['ipToS'] = int(arg2, 0)
      # match['matchIpToS'] = True
    elif arg1 == "matchIpProto":
      match['ipProto'] = int(arg2, 0)
      # match['matchIpProto'] = True
    elif arg1 == "matchSrcIPv4Net":
      srcIPv4Net = {}
      srcIPv4Net['value'] = arg2
      match['srcIPv4Net'] = srcIPv4Net
      # match['matchSrcIPv4Net'] = True
    elif arg1 == "matchDstIPv4Net":
      dstIPv4Net = {}
      dstIPv4Net['value'] = arg2
      match['dstIPv4Net'] = dstIPv4Net
      # match['matchDstIPv4Net'] = True
    elif arg1 == "matchSrcTcpUdpPort":
      match['srcTcpUdpPort'] = int(arg2, 0)
      # match['matchSrcTcpUdpPort'] = True
    elif arg1 == "matchDstTcpUdpPort":
      match['dstTcpUdpPort'] = int(arg2, 0)
      # match['matchDstTcpUdpPort'] = True
    elif arg1 == "actionOutput":
      # Just mark whether ACTION_OUTPUT action is enabled
      actionOutputEnabled = arg2 in ['True', 'true']
      #
      # TODO: Complete the implementation for ACTION_OUTPUT
      #   actionOutput = {}
      #   outPort = {}
      #   outPort['value'] = int(arg2, 0)
      #   actionOutput['port'] = outPort
      #   actionOutput['maxLen'] = int(arg3, 0)
      #   action['actionOutput'] = actionOutput
      #   # action['actionType'] = 'ACTION_OUTPUT'
      #   actions.append(action)
      #
    elif arg1 == "actionSetVlanId":
      vlanId = {}
      vlanId['vlanId'] = int(arg2, 0)
      action['actionSetVlanId'] = vlanId
      # action['actionType'] = 'ACTION_SET_VLAN_VID'
      actions.append(copy.deepcopy(action))
    elif arg1 == "actionSetVlanPriority":
      vlanPriority = {}
      vlanPriority['vlanPriority'] = int(arg2, 0)
      action['actionSetVlanPriority'] = vlanPriority
      # action['actionType'] = 'ACTION_SET_VLAN_PCP'
      actions.append(copy.deepcopy(action))
    elif arg1 == "actionSetIpToS":
      ipToS = {}
      ipToS['ipToS'] = int(arg2, 0)
      action['actionSetIpToS'] = ipToS
      # action['actionType'] = 'ACTION_SET_NW_TOS'
      actions.append(copy.deepcopy(action))
    elif arg1 == "actionSetTcpUdpSrcPort":
      tcpUdpSrcPort = {}
      tcpUdpSrcPort['port'] = int(arg2, 0)
      action['actionSetTcpUdpSrcPort'] = tcpUdpSrcPort
      # action['actionType'] = 'ACTION_SET_TP_SRC'
      actions.append(copy.deepcopy(action))
    elif arg1 == "actionSetTcpUdpDstPort":
      tcpUdpDstPort = {}
      tcpUdpDstPort['port'] = int(arg2, 0)
      action['actionSetTcpUdpDstPort'] = tcpUdpDstPort
      # action['actionType'] = 'ACTION_SET_TP_DST'
      actions.append(copy.deepcopy(action))
    elif arg1 == "actionStripVlan":
      stripVlan = {}
      stripVlan['stripVlan'] = arg2 in ['True', 'true']
      action['actionStripVlan'] = stripVlan
      # action['actionType'] = 'ACTION_STRIP_VLAN'
      actions.append(copy.deepcopy(action))
    elif arg1 == "actionSetEthernetSrcAddr":
      ethernetSrcAddr = {}
      ethernetSrcAddr['value'] = arg2
      setEthernetSrcAddr = {}
      setEthernetSrcAddr['addr'] = ethernetSrcAddr
      action['actionSetEthernetSrcAddr'] = setEthernetSrcAddr
      # action['actionType'] = 'ACTION_SET_DL_SRC'
      actions.append(copy.deepcopy(action))
    elif arg1 == "actionSetEthernetDstAddr":
      ethernetDstAddr = {}
      ethernetDstAddr['value'] = arg2
      setEthernetDstAddr = {}
      setEthernetDstAddr['addr'] = ethernetDstAddr
      action['actionSetEthernetDstAddr'] = setEthernetDstAddr
      # action['actionType'] = 'ACTION_SET_DL_DST'
      actions.append(copy.deepcopy(action))
    elif arg1 == "actionSetIPv4SrcAddr":
      IPv4SrcAddr = {}
      IPv4SrcAddr['value'] = arg2
      setIPv4SrcAddr = {}
      setIPv4SrcAddr['addr'] = IPv4SrcAddr
      action['actionSetIPv4SrcAddr'] = setIPv4SrcAddr
      # action['actionType'] = 'ACTION_SET_NW_SRC'
      actions.append(copy.deepcopy(action))
    elif arg1 == "actionSetIPv4DstAddr":
      IPv4DstAddr = {}
      IPv4DstAddr['value'] = arg2
      setIPv4DstAddr = {}
      setIPv4DstAddr['addr'] = IPv4DstAddr
      action['actionSetIPv4DstAddr'] = setIPv4DstAddr
      # action['actionType'] = 'ACTION_SET_NW_DST'
      actions.append(copy.deepcopy(action))
    elif arg1 == "actionEnqueue":
      # TODO: Implement ACTION_ENQUEUE
      actionEnqueue = {}
      #   actionEnqueue['queueId'] = int(arg2, 0)
      #   enqueuePort = {}
      #   enqueuePort['value'] = int(arg3, 0)
      #   actionEnqueue['port'] = enqueuePort
      #   action['actionEnqueue'] = actionEnqueue
      #   # action['actionType'] = 'ACTION_ENQUEUE'
      #   actions.append(copy.deepcopy(action))
      #
    else:
      log_error("ERROR: Unknown argument '%s'" % (arg1))
      log_error(usage_msg)
      exit(1)

  return {
    'my_flow_id' : my_flow_id,
    'my_installer_id' : my_installer_id,
    'my_src_dpid' : my_src_dpid,
    'my_src_port' : my_src_port,
    'my_dst_dpid' : my_dst_dpid,
    'my_dst_port' : my_dst_port,
    'match' : match,
    'matchInPortEnabled' : matchInPortEnabled,
    'actions' : actions,
    'actionOutputEnabled' : actionOutputEnabled
    }

def compute_data_path(parsed_args):

  my_src_dpid = parsed_args['my_src_dpid']
  my_src_port = parsed_args['my_src_port']
  my_dst_dpid = parsed_args['my_dst_dpid']
  my_dst_port = parsed_args['my_dst_port']

  # Compute the shortest path
  data_path = shortest_path(my_src_dpid, my_src_port, my_dst_dpid, my_dst_port)

  debug("Data Path: %s" % data_path)
  return data_path

def compute_flow_path(parsed_args, data_path):

  my_flow_id = parsed_args['my_flow_id']
  my_installer_id = parsed_args['my_installer_id']
  match = parsed_args['match']
  matchInPortEnabled = parsed_args['matchInPortEnabled']
  actions = parsed_args['actions']
  actionOutputEnabled = parsed_args['actionOutputEnabled']
  my_data_path = copy.deepcopy(data_path)

  flow_id = {}
  flow_id['value'] = my_flow_id
  installer_id = {}
  installer_id['value'] = my_installer_id

  flow_path = {}
  flow_path['flowId'] = flow_id
  flow_path['installerId'] = installer_id

  #
  # Add the match conditions to each flow entry
  #
  if (len(match) > 0) or matchInPortEnabled:
    idx = 0
    while idx < len(my_data_path['flowEntries']):
      if matchInPortEnabled:
	inPort = my_data_path['flowEntries'][idx]['inPort']
	match['inPort'] = copy.deepcopy(inPort)
	# match['matchInPort'] = True
      my_data_path['flowEntries'][idx]['flowEntryMatch'] = copy.deepcopy(match)
      idx = idx + 1

  #
  # Set the actions for each flow entry
  # NOTE: The actions from the command line are aplied
  # ONLY to the first flow entry.
  #
  # If ACTION_OUTPUT action is enabled, then apply it
  # to each flow entry.
  #
  if (len(actions) > 0) or actionOutputEnabled:
    idx = 0
    while idx < len(my_data_path['flowEntries']):
      if idx > 0:
	actions = []	# Reset the actions for all but first entry
      action = {}
      outPort = my_data_path['flowEntries'][idx]['outPort']
      actionOutput = {}
      actionOutput['port'] = copy.deepcopy(outPort)
      # actionOutput['maxLen'] = 0	# TODO: not used for now
      action['actionOutput'] = copy.deepcopy(actionOutput)
      # action['actionType'] = 'ACTION_OUTPUT'
      actions.append(copy.deepcopy(action))

      my_data_path['flowEntries'][idx]['flowEntryActions'] = copy.deepcopy(actions)
      idx = idx + 1


  flow_path['dataPath'] = my_data_path
  debug("Flow Path: %s" % flow_path)

  add_flow_path(flow_path)


if __name__ == "__main__":
  usage_msg = "Usage: %s [Flags] <flow-id> <installer-id> <src-dpid> <src-port> <dest-dpid> <dest-port> [Match Conditions] [Actions]\n" % (sys.argv[0])
  usage_msg = usage_msg + "    Flags:\n"
  usage_msg = usage_msg + "        -m        Monitor and maintain the installed shortest path\n"
  usage_msg = usage_msg + "    Match Conditions:\n"
  usage_msg = usage_msg + "        matchInPort <True|False> (default to True)\n"
  usage_msg = usage_msg + "        matchSrcMac <source MAC address>\n"
  usage_msg = usage_msg + "        matchDstMac <destination MAC address>\n"
  usage_msg = usage_msg + "        matchSrcIPv4Net <source IPv4 network address>\n"
  usage_msg = usage_msg + "        matchDstIPv4Net <destination IPv4 network address>\n"
  usage_msg = usage_msg + "        matchEthernetFrameType <Ethernet frame type>\n"

  usage_msg = usage_msg + "    Match Conditions (not implemented yet):\n"
  usage_msg = usage_msg + "        matchVlanId <VLAN ID>\n"
  usage_msg = usage_msg + "        matchVlanPriority <VLAN priority>\n"
  usage_msg = usage_msg + "        matchIpToS <IP ToS (DSCP field, 6 bits)>\n"
  usage_msg = usage_msg + "        matchIpProto <IP protocol>\n"
  usage_msg = usage_msg + "        matchSrcTcpUdpPort <source TCP/UDP port>\n"
  usage_msg = usage_msg + "        matchDstTcpUdpPort <destination TCP/UDP port>\n"
  usage_msg = usage_msg + "    Actions:\n"
  usage_msg = usage_msg + "        actionOutput <True|False> (default to True)\n"
  usage_msg = usage_msg + "        actionSetEthernetSrcAddr <source MAC address>\n"
  usage_msg = usage_msg + "        actionSetEthernetDstAddr <destination MAC address>\n"
  usage_msg = usage_msg + "        actionSetIPv4SrcAddr <source IPv4 address>\n"
  usage_msg = usage_msg + "        actionSetIPv4DstAddr <destination IPv4 address>\n"
  usage_msg = usage_msg + "    Actions (not implemented yet):\n"
  usage_msg = usage_msg + "        actionSetVlanId <VLAN ID>\n"
  usage_msg = usage_msg + "        actionSetVlanPriority <VLAN priority>\n"
  usage_msg = usage_msg + "        actionSetIpToS <IP ToS (DSCP field, 6 bits)>\n"
  usage_msg = usage_msg + "        actionSetTcpUdpSrcPort <source TCP/UDP port>\n"
  usage_msg = usage_msg + "        actionSetTcpUdpDstPort <destination TCP/UDP port>\n"
  usage_msg = usage_msg + "        actionStripVlan <True|False>\n"
  usage_msg = usage_msg + "        actionEnqueue <dummy argument>\n"

  # app.debug = False;

  # Usage info
  if len(sys.argv) > 1 and (sys.argv[1] == "-h" or sys.argv[1] == "--help"):
    print(usage_msg)
    exit(0)

  #
  # Check the flags
  #
  start_argv_index = 1
  if len(sys.argv) > 1 and sys.argv[1] == "-m":
    MonitoringEnabled = True
    start_argv_index = start_argv_index + 1

  #
  # Parse the remaining arguments
  #
  my_args = sys.argv[start_argv_index:]
  parsed_args = copy.deepcopy(extract_flow_args(my_args))

  last_data_path = []
  my_flow_id = parsed_args['my_flow_id']
  # Cleanup leftover state
  delete_flow_path(my_flow_id)

  while True:
    data_path = compute_data_path(parsed_args)
    if data_path != last_data_path:
      if len(last_data_path) > 0:
	delete_flow_path(my_flow_id)
      flow_path = compute_flow_path(parsed_args, data_path)
      add_flow_path(flow_path)
      last_data_path = data_path

    if MonitoringEnabled != True:
      break
    time.sleep(1)
