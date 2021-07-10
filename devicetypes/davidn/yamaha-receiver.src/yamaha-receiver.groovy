
metadata {
  definition (name: "Yamaha Receiver", namespace: "davidn", author: "david@newgas.net") {
    capability "Switch"
    capability "Polling"
  }
}

private getHostAddress() {
    def ip = getDataValue("ip")
    def port = getDataValue("port")

    if (!ip || !port) {
        def parts = device.deviceNetworkId.split(":")
        if (parts.length == 2) {
            ip = parts[0]
            port = parts[1]
        } else {
            log.warn "Can't figure out ip and port for device: ${device.id}"
        }
    }

    log.debug "Using IP: $ip and port: $port for device: ${device.id}"
    return convertHexToIP(ip) + ":" + convertHexToInt(port)
}

private Integer convertHexToInt(hex) {
    return Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    return [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

def yamahaCommand(cmd, var) {
  return new physicalgraph.device.HubAction(
    method: "POST",
    path: "/YamahaRemoteControl/ctrl",
    body: "<YAMAHA_AV cmd=\"$cmd\"><Main_Zone><Power_Control><Power>$var</Power></Power_Control></Main_Zone></YAMAHA_AV>",
    headers: [
      HOST: getHostAddress()
    ]
  )
}

def on() {
  log.debug "on"
  return [
    yamahaCommand("PUT", "On"),
    yamahaCommand("GET", "GetParam")
  ]
}
def off() {
  log.debug "off"
  return [
    yamahaCommand("PUT", "Standby"),
    yamahaCommand("GET", "GetParam")
  ]
}

def poll() {
  log.debug "polling"
  return yamahaCommand("GET", "GetParam")
}

def parse(description) {
  log.debug "parse"
  def msg = parseLanMessage(description)
  log.debug msg
  def power_state = msg.xml.Main_Zone.Power_Control.Power.text()
  if (power_state != "") {
    def switch_state = power_state == 'On'? 'on':'off'
    log.debug "setting switch state to $switch_state"
    return createEvent(name:'switch',value: switch_state)
  }
}
