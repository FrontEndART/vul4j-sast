#java -Dlogback.configurationFile=logback.xml -cp target/floodlight-only.jar:lib/*:lib/titan/* net.onrc.onos.registry.controller.RegistryRunner $1
java -cp target/floodlight-only.jar:lib/*:lib/titan/* net.floodlightcontroller.core.Main -cf onos.properties
