
metadata {
  definition (name: "Yamaha Receiver", namespace: "davidn", author: "david@newgas.net") {
    capability "Switch"
    capability "Polling"
    capability "Audio Mute"
    capability "Audio Volume"
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

def yamahaCommand(cmd, body) {
  return new physicalgraph.device.HubAction(
    method: "POST",
    path: "/YamahaRemoteControl/ctrl",
    body: "<YAMAHA_AV cmd=\"$cmd\"><Main_Zone>$body</Main_Zone></YAMAHA_AV>",
    headers: [
      HOST: getHostAddress()
    ]
  )
}

def on() {
  log.debug "on()"
  return [
    yamahaCommand("PUT", "<Power_Control><Power>On</Power></Power_Control>"),
    yamahaCommand("GET", "<Basic_Status>GetParam</Basic_Status>")
  ]
}
def off() {
  log.debug "off()"
  return [
    yamahaCommand("PUT", "<Power_Control><Power>Standby</Power></Power_Control>"),
    yamahaCommand("GET", "<Basic_Status>GetParam</Basic_Status>")
  ]
}

def setMute(state) {
  log.debug "setMute($state)"
  def set = state == "muted" ? "On" : "Off"
  return [
    yamahaCommand("PUT", "<Volume><Mute>$set</Mute></Volume>"),
    yamahaCommand("GET", "<Basic_Status>GetParam</Basic_Status>")
  ]
}

def mute() {
  log.debug "mute()"
  return setMute("mute")
}

def unmute() {
  log.debug "unmute()"
  return setMute("unmute")
}

def setVolume(volume) {
  log.debug "setVolume($volume)"
  def vol_db = volume/2 - 50
  return [
    yamahaCommand("PUT", "<Volume><Lvl>$vol_db</Lvl><Exp>1</Exp><Unit>dB</Unit></Volume>"),
    yamahaCommand("GET", "<Basic_Status>GetParam</Basic_Status>")
  ]
}

def volumeUp() {
  log.debug "volumeUp()"
  return setMute("mute")
}

def volumeDown() {
  log.debug "volumeDown()"
  return setMute("unmute")
}

def poll() {
  log.debug "poll()"
  return yamahaCommand("GET", "<Basic_Status>GetParam</Basic_Status>")
}

def parse(description) {
  log.debug "parse()"
  def msg = parseLanMessage(description)
  log.debug msg
  
  def events = []
  
  def power_state = msg.xml.Main_Zone.Basic_Status.Power_Control.Power.text()
  if (power_state != "") {
    def switch_attr = power_state == 'On'? 'on':'off'
    log.debug "setting switch state to $switch_attr"
    events.add(createEvent(name:'switch',value: switch_attr))
  }
  
  def mute_state = msg.xml.Main_Zone.Basic_Status.Volume.Mute.text()
  if (mute_state != "") {
    def mute_attr = mute_state == 'On'? 'muted':'unmuted'
    log.debug "setting mute state to $mute_attr"
    events.add(createEvent(name:'mute',value: mute_attr))
  }
  
  def vol_val = msg.xml.Main_Zone.Basic_Status.Volume.Lvl.Val.text()
  def vol_exp = msg.xml.Main_Zone.Basic_Status.Volume.Lvl.Exp.text()
  if (vol_val != "" && vol_exp != "") {
    def vol_db = vol_val.toInteger()/Math.pow(10, vol_exp.toInteger())
    // Treat -50dB as 0% and 0dB as 100%. Lock to that that range. dB fixed as 2% to follow perception
    def vol_percent = Math.min(100, Math.max(0, (vol_db+50)*2))
    log.debug "setting volume to $vol_percent"
    events.add(createEvent(name:'volume',value: vol_percent))
  }
  
  return events
}
