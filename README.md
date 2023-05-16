# MitmWs

MitmWs is an HTTP proxy designed specifically for pentesting applications that use websockets.

# Current work in progress
- analyzer model
- one click replay optimization

# Features
- Replay and modify captured websocket conversations
- Create websocket conversations from scratch
- Define targets and scan conversations
- "Drop-in" to existing websocket connections
- Search traffic
- Scripting support for rules, variables, upgrade scripts etc
- Environment variables applied to manual/automatic tester
- Rule based anomaly detection
- Built-in http server / websocket server ( can be customized with scripts )
- Updates for payloads & scripts
- Protocol level testing
- Fuzzing integration ( zzuf example rule )
- Interactsh integration
- Script based actions for events
- Regex based breakpoints

# Planned features
- Selenium support for upgrade requests
- Selenium "monitor" for XSS detection
- Remote monitoring agent
- WebRTC support

# Installation
 1. Download MitmWs from the [releases](https://github.com/softwaresecured/mitmws/releases/tag/alpha) tab
 2. Extract to convenient location
 3. Run `mitmws`
