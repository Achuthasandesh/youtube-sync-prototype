# YouTubeSyncPlayer – **Seamless YouTube Sync Across Your Local Network**

## 🚀 Project Overview

**YouTubeSyncPlayer** is a modern Kotlin/Jetpack Compose app that delivers a true "*Watch Together*" YouTube experience right on your local WiFi network—_**no manual IP entry, no cloud broker, just instant discovery and sync!**_

- **Create a Server:** Any user can become the “host” with a single tap—launching an embedded WebSocket **server**.
- **Zero Configuration Connection:** Other users simply tap to *Join*. Thanks to **Android NSD (Network Service Discovery)**, all servers are automatically advertised and discoverable on the LAN—*no IPs, QR codes, or manual setup*.
- **Smart Service Discovery:** Clients see a live list of available servers on the network. Just tap any server to instantly connect via a WebSocket client—*direct device-to-device!*
- **Synchronized YouTube Playback:** The host pastes a YouTube video link and clicks **"Play"**. Both server and client navigate to the YouTube player screen. The client cues the video and signals it’s **ready**.
- **Frame-Accurate Start:** The server, upon seeing all clients ready, broadcasts a `"play"` command including a **start time set 5 seconds in the future**. All devices then calculate the exact delay to start—using Google’s **Trusted Time API** for **millisecond-accurate clock sync**.
- **Simultaneous Launch:** When the delay hits zero, every device begins playback at precisely the same instant—for a perfectly shared viewing experience.

## ✨ How It Works – In-Depth

1. **Server Creation**  
   User clicks **"Create Server"** → A WebSocket server spins up and registers itself via NSD (`_ws._tcp.` service type).

2. **Network Service Discovery**  
   Every client app running on the same WiFi scans for services using NSD. All available servers are listed for quick selection—_no network gymnastics!_

3. **Connection Establishment**  
   Client taps a discovered server → Instantly connects using WebSocket.

4. **Video Selection & Sync**  
   Host pastes a YouTube link and clicks **"Play"**. All connected clients cue the same video and send a **"ready"** signal as soon as their player is primed.

5. **Coordinated Playback**  
   Host waits for all clients to be ready, then broadcasts a `"play_at"` timestamp (current trusted time + 5sec buffer). Each device schedules local playback for that exact future moment, based on its Trusted Time. The result: every screen starts the YouTube video in **absolute, frame-accurate sync**.

## 🚧 **Scope of Improvements**

- **Allowing client to also play the video.**  
- **Enabling sync for pause, play, and seek operations.**

---

**Build once, sync everywhere—no IPs, no drama, just YouTube joy together!** 🎉
