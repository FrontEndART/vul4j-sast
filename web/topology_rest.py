#! /usr/bin/env python
import pprint
import os
import sys
import subprocess
import json
import argparse
import io
import time

from flask import Flask, json, Response, render_template, make_response, request

## Global Var ##
RestIP="onos1vpc"
RestPort=8080
#DBName="onos-network-map"

DEBUG=1
pp = pprint.PrettyPrinter(indent=4)

app = Flask(__name__)

## Worker Functions ##
def log_error(txt):
  print '%s' % (txt)

def debug(txt):
  if DEBUG:
    print '%s' % (txt)

## Rest APIs ##
### File Fetch ###
@app.route('/ui/img/<filename>', methods=['GET'])
@app.route('/img/<filename>', methods=['GET'])
@app.route('/css/<filename>', methods=['GET'])
@app.route('/js/models/<filename>', methods=['GET'])
@app.route('/js/views/<filename>', methods=['GET'])
@app.route('/js/<filename>', methods=['GET'])
@app.route('/lib/<filename>', methods=['GET'])
@app.route('/', methods=['GET'])
@app.route('/<filename>', methods=['GET'])
@app.route('/tpl/<filename>', methods=['GET'])
def return_file(filename="index.html"):
  if request.path == "/":
    fullpath = "./index.html"
  else:
    fullpath = str(request.path)[1:]

  response = make_response(open(fullpath).read())
  suffix = fullpath.split(".")[-1]

  if suffix == "html" or suffix == "htm":
    response.headers["Content-type"] = "text/html"
  elif suffix == "js":
    response.headers["Content-type"] = "application/javascript"
  elif suffix == "css":
    response.headers["Content-type"] = "text/css"
  elif suffix == "png":
    response.headers["Content-type"] = "image/png"

  return response

init_topo1 = { 
  "nodes" : [ 
    {"name" : "sw0", "group" : 0},
    {"name" : "sw1", "group" : 0},
    {"name" : "sw2", "group" : 0},
    {"name" : "sw3", "group" : 0},
    {"name" : "sw4", "group" : 0},
    {"name" : "sw5", "group" : 0},
    {"name" : "host0", "group" : 1}
    ],
  "links" : [
    {"source" :0, "target": 1},
    {"source" :1, "target": 0},
    {"source" :0, "target": 2},
    {"source" :2, "target": 0},
    {"source" :1, "target": 3},
    {"source" :3, "target": 1},
    {"source" :2, "target": 3},
    {"source" :3, "target": 2},
    {"source" :2, "target": 4},
    {"source" :4, "target": 2},
    {"source" :3, "target": 5},
    {"source" :5, "target": 3},
    {"source" :4, "target": 5},
    {"source" :5, "target": 4},
    {"source" :6, "target": 0},
    {"source" :0, "target": 6}
    ]
}

def node_id(switch_array, dpid):
  id = -1
  for i, val in enumerate(switch_array):
    if val['name'] == dpid:
      id = i
      break

  return id

@app.route("/topology")
def topology_for_gui():
  try:
    command = "curl -s \'http://%s:%s/wm/core/topology/switches/all/json\'" % (RestIP, RestPort)
    result = os.popen(command).read()
    parsedResult = json.loads(result)
  except:
    log_error("REST IF has issue: %s" % command)
    log_error("%s" % result)
    sys.exit(0)

  topo = {}
  switches = []
  links = []

  for v in parsedResult:
    if v.has_key('dpid'):
#      if v.has_key('dpid') and str(v['state']) == "ACTIVE":#;if you want only ACTIVE nodes
      dpid = str(v['dpid'])
      state = str(v['state'])
      sw = {}
      sw['name']=dpid
      if str(v['state']) == "ACTIVE":
         sw['group']=0
      if str(v['state']) == "INACTIVE":
         sw['group']=1

      switches.append(sw)
  
  try:
    command = "curl -s \'http://%s:%s/wm/core/topology/links/json\'" % (RestIP, RestPort)
    result = os.popen(command).read()
    parsedResult = json.loads(result)
  except:
    log_error("REST IF has issue: %s" % command)
    log_error("%s" % result)
    sys.exit(0)

  for v in parsedResult:
    link = {}
    if v.has_key('dst-switch'):
      dst_dpid = str(v['dst-switch'])
      dst_id = node_id(switches, dst_dpid)
    if v.has_key('src-switch'):
      src_dpid = str(v['src-switch'])
      src_id = node_id(switches, src_dpid)
    link['source'] = src_id
    link['target'] = dst_id
    links.append(link)

  topo['nodes'] = switches
  topo['links'] = links

  pp.pprint(topo)
  js = json.dumps(topo)
  resp = Response(js, status=200, mimetype='application/json')
  return resp


@app.route("/wm/core/controller/switches/json")
def query_switch():
  try:
    command = "curl -s \'http://%s:%s/wm/core/topology/switches/all/json\'" % (RestIP, RestPort)
#    http://localhost:8080/wm/core/topology/switches/active/json
    result = os.popen(command).read()
    parsedResult = json.loads(result)
  except:
    log_error("REST IF has issue: %s" % command)
    log_error("%s" % result)
    sys.exit(0)

#  print command
#  print result
  switches_ = []
  for v in parsedResult:
    if v.has_key('dpid'):
      if v.has_key('dpid') and str(v['state']) == "ACTIVE":#;if you want only ACTIVE nodes
        dpid = str(v['dpid'])
        state = str(v['state'])
        sw = {}
        sw['dpid']=dpid
        sw['active']=state
        switches_.append(sw)

  pp.pprint(switches_)
  js = json.dumps(switches_)
  resp = Response(js, status=200, mimetype='application/json')
  return resp

@app.route("/wm/device/")
def devices():
  try:
    command = "curl -s http://%s:%s/graphs/%s/vertices\?key=type\&value=device" % (RestIP, RestPort, DBName)
    result = os.popen(command).read()
    parsedResult = json.loads(result)['results']
  except:
    log_error("REST IF has issue: %s" % command)
    log_error("%s" % result)
    sys.exit(0)

  devices = []
  for v in parsedResult:
    dl_addr = v['dl_addr']
    nw_addr = v['nw_addr']
    vertex = v['_id']
    mac = []
    mac.append(dl_addr)
    ip = []
    ip.append(nw_addr)
    device = {}
    device['entryClass']="DefaultEntryClass"
    device['mac']=mac
    device['ipv4']=ip
    device['vlan']=[]
    device['lastSeen']=0
    attachpoints =[]

    port, dpid = deviceV_to_attachpoint(vertex)
    attachpoint = {}
    attachpoint['port']=port
    attachpoint['switchDPID']=dpid
    attachpoints.append(attachpoint)
    device['attachmentPoint']=attachpoints
    devices.append(device)

  print devices
  js = json.dumps(devices)
  resp = Response(js, status=200, mimetype='application/json')
  return resp

#{"entityClass":"DefaultEntityClass","mac":["7c:d1:c3:e0:8c:a3"],"ipv4":["192.168.2.102","10.1.10.35"],"vlan":[],"attachmentPoint":[{"port":13,"switchDPID":"00:01:00:12:e2:78:32:44","errorStatus":null}],"lastSeen":1357333593496}


## return fake stat for now
@app.route("/wm/core/switch/<switchId>/<statType>/json")
def switch_stat(switchId, statType):
    if statType == "desc":
        desc=[{"length":1056,"serialNumber":"None","manufacturerDescription":"Nicira Networks, Inc.","hardwareDescription":"Open vSwitch","softwareDescription":"1.4.0+build0","datapathDescription":"None"}]
        ret = {}
        ret[switchId]=desc
    elif statType == "aggregate":
        aggr = {"packetCount":0,"byteCount":0,"flowCount":0}
        ret = {}
        ret[switchId]=aggr
    else:
        ret = {} 

    js = json.dumps(ret)
    resp = Response(js, status=200, mimetype='application/json')
    return resp


@app.route("/wm/topology/links/json")
def query_links():
  try:
    command = 'curl -s http://%s:%s/graphs/%s/vertices?key=type\&value=port' % (RestIP, RestPort, DBName)
    result = os.popen(command).read()
    parsedResult = json.loads(result)['results']
  except:
    log_error("REST IF has issue: %s" % command)
    log_error("%s" % result)
    sys.exit(0)

  debug("query_links %s" % command)
  pp.pprint(parsedResult)
  sport = []
  links = []
  for v in parsedResult:
    srcport = v['_id']
    try:
      command = "curl -s http://%s:%s/graphs/%s/vertices/%d/out?_label=link" % (RestIP, RestPort, DBName, srcport)
      print command
      result = os.popen(command).read()
      linkResults = json.loads(result)['results']
    except:
      log_error("REST IF has issue: %s" % command)
      log_error("%s" % result)
      sys.exit(0)

    for p in linkResults:
      if p.has_key('type') and p['type'] == "port":
        dstport = p['_id']
        (sport, sdpid) = portV_to_port_dpid(srcport)
        (dport, ddpid) = portV_to_port_dpid(dstport)
        link = {}
        link["src-switch"]=sdpid
        link["src-port"]=sport
        link["src-port-state"]=0
        link["dst-switch"]=ddpid
        link["dst-port"]=dport
        link["dst-port-state"]=0
        link["type"]="internal"
        links.append(link)

  pp.pprint(links)
  js = json.dumps(links)
  resp = Response(js, status=200, mimetype='application/json')
  return resp

if __name__ == "__main__":
  if len(sys.argv) > 1 and sys.argv[1] == "-d":
    print "-- query all switches --"
    query_switch()
    print "-- query topo --"
    topology_for_gui()
#    print "-- query all links --"
#    query_links()
#    print "-- query all devices --"
#    devices()
  else:
    app.debug = True
    app.run(host="0.0.0.0", port=9000)
