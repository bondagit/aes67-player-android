//
//  Copyright (c) 2024 Andrea Bondavalli. All rights reserved.
//
package com.bondagit.aes67player.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
{
  "http_port": 8080,
  "rtsp_port": 8854,
  "http_base_dir": "../webui/dist",
  "log_severity": 0,
  "playout_delay": 0,
  "tic_frame_size_at_1fs": 48,
  "max_tic_frame_size": 1024,
  "sample_rate": 48000,
  "rtp_mcast_base": "239.1.0.1",
  "rtp_port": 5004,
  "ptp_domain": 0,
  "ptp_dscp": 48,
  "sap_mcast_addr": "239.255.255.255",
  "sap_interval": 30,
  "syslog_proto": "none",
  "syslog_server": "255.255.255.254:1234",
  "status_file": "./status.json",
  "interface_name": "enp2s0",
  "mdns_enabled": true,
  "custom_node_id": "",
  "node_id": "AES67 daemon 000a0d00",
  "ptp_status_script": "./scripts/ptp_status.sh",
  "mac_addr": "84:39:be:6d:dc:74",
  "ip_addr": "10.0.0.13",
  "streamer_channels": 8,
  "streamer_enabled": true,
  "auto_sinks_update": true
}
*/

@Serializable
data class Config(
    @SerialName(value = "http_port") val httpPort: Int,
    @SerialName(value = "ip_addr") val ipAddress: String,
    @SerialName(value = "node_id") var nodeId: String,
    @SerialName(value = "streamer_enabled") val streamerEnabled: Boolean,
    @SerialName(value = "streamer_channels") val streamerChannels: Int
)
