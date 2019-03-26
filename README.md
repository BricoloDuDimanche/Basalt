# Basalt
![Latest GitHub tag](https://img.shields.io/github/tag-date/BricoloDuDimanche/Basalt.svg?style=flat-square)
[![License](https://img.shields.io/github/license/BricoloDuDimanche/Basalt.svg?style=flat-square)](https://github.com/BricoloDuDimanche/Basalt/blob/master/LICENSE)
[![Discord](https://img.shields.io/badge/chat-on%20Discord%20(%23basalt)-7289DA.svg?style=flat-square)](https://discord.gg/2CkzJzM)
[![Donate](https://img.shields.io/badge/donate-Patreon-F96854.svg?style=flat-square)](https://www.patreon.com/Bowser65)

[Andesite](https://github.com/natanbc/andesite-node) plugin to provide extra sources like Spotify or Deezer 

**NOTE**: The plugin is **very unstable** atm and should **not** be used in a production app.

## Supported platforms 
 - Spotify
 - Deezer (soon)
 - PornHub (soon)

## How it work
Just fetch /loadtracks with a link from a custom source provider. It works for tracks, playlist and albums.

Example:
``` 
GET /loadtracks?identifier=https://open.spotify.com/track/1HDApabtZoWpGEcWAMMyNM

{
    "loadType": "TRACK_LOADED",
    "playlistInfo": {},
    "tracks": [
        {
            "track": "QAAAkAIAKUNhcmF2YW4gUGFsYWNlIC0gTWlyYWNsZSAob2ZmaWNpYWwgYXVkaW8pAA1DYXJhdmFuUGFsYWNlAAAAAAADW2AAC1hSUDlrOW5sQWZFAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9WFJQOWs5bmxBZkUAB3lvdXR1YmUAAAAAAAAAAA==",
            "info": {
                "class": "com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack",
                "title": "Caravan Palace - Miracle (official audio)",
                "author": "CaravanPalace",
                "length": 220000,
                "identifier": "XRP9k9nlAfE",
                "uri": "https://www.youtube.com/watch?v=XRP9k9nlAfE",
                "isStream": false,
                "isSeekable": true,
                "position": 0
            }
        }
    ]
}
```

## Configuration
By default, all custom sources are disabled. You'll need to add some entries to your Andesite config.

Note that some services like Spotify requires you to use an access token, so you'll need a Spotify app

| Key                         | Type     | Description                                                           |
|-----------------------------|----------|-----------------------------------------------------------------------|
| basalt.proxy                | String[] | Proxy IPs used to bypass ratelimits from some services (PornHub, ...) |
| basalt.youtube-keys         | String[] | Youtube API keys used to fetch tracks                                 |
| basalt.spotify.enabled      | boolean  | Whether or not to enable Spotify integration                          |
| basalt.spotify.clientID     | string   | Spotify client ID                                                     |
| basalt.spotify.clientSecret | string   | Spotify client secret                                                 |

### Youtube keys
You may have noticed that we take an array of keys in `basalt.youtube-keys`. This is because Basalt will use key
rotation to handle heavy loads, to ensure you have enough quota

## Attribution

PornHub integration mostly taken from [JukeBot](https://github.com/Devoxin/JukeBot) by [Devoxin](https://github.com/Devoxin) (Licensed under [Apache License 2.0](https://github.com/Devoxin/JukeBot/blob/master/LICENSE))
