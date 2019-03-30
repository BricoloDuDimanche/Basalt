# Basalt
[![Build Status](https://img.shields.io/travis/BricoloDuDimanche/Basalt.svg?branch=master&style=flat-square&logo=travis)](https://travis-ci.org/BricoloDuDimanche/Basalt)
[![Latest GitHub tag](https://img.shields.io/github/tag-date/BricoloDuDimanche/Basalt.svg?style=flat-square)](https://github.com/BricoloDuDimanche/Basalt/releases)
[![License](https://img.shields.io/github/license/BricoloDuDimanche/Basalt.svg?style=flat-square)](https://github.com/BricoloDuDimanche/Basalt/blob/master/LICENSE)
[![Discord](https://img.shields.io/badge/chat-on%20Discord%20(%23basalt)-7289DA.svg?style=flat-square)](https://discord.gg/2CkzJzM)
[![Donate](https://img.shields.io/badge/donate-Patreon-F96854.svg?style=flat-square)](https://www.patreon.com/Bowser65)

[Andesite](https://github.com/natanbc/andesite-node) plugin to provide extra sources like Spotify or Deezer as well as caching

**NOTE**: The plugin is **very unstable** atm and should **not** be used in a production app.

## Supported platforms 
 - Spotify (Track, Playlist and Album)
 - Deezer (Track, Playlist and Album)
 - PornHub (Track)

## How it work
Just fetch /loadtracks with a link from a custom source provider.<br>
*Note* PornHub integration can take a phsearch:<blabla> to search videos

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
You'll need to add some entries to your Andesite config to setup Basalt

| Key                         | Type     | Description                                                          | Default |
|-----------------------------|----------|----------------------------------------------------------------------|---------|
| basalt.source.spotify       | boolean  | Whether or not to enable Spotify integration                         | false   |
| basalt.source.deezer        | boolean  | Whether or not to enable Deezer integration                          | false   |
| basalt.source.pornhub       | boolean  | Whether or not to enable PornHub integration                         | false   |
| basalt.spotify.clientID     | string   | Spotify client ID                                                    | null    |
| basalt.spotify.clientSecret | string   | Spotify client secret                                                | null    |
| basalt.max-heavy-tracks     | int      | Maximum tracks to load from a playlist considered as [heavy](#heavy) | 25      |

### Heavy

Spotify and Deezer implementation not actually stream from the service. Basalt fetched an equivalent on YouTube and
plays that instead.
Problem is that since YouTube is taking action against bots and automated software you might get flagged for this and
get your IP temporarily or permanently banned as Basalt needs to make a heavy amount of requests (1 per track).

To prevent those issues, you should restrict this feature in your bot to prevent heavy loads, and only fetch a small
amount of tracks to prevent being flagged.

## Attribution

PornHub integration mostly taken from [JukeBot](https://github.com/Devoxin/JukeBot) by [Devoxin](https://github.com/Devoxin) (Licensed under [Apache License 2.0](https://github.com/Devoxin/JukeBot/blob/master/LICENSE))
