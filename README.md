# Basalt
[![Build Status](https://img.shields.io/travis/BricoloDuDimanche/Basalt.svg?branch=master&style=flat-square&logo=travis)](https://travis-ci.org/BricoloDuDimanche/Basalt)
[![Latest GitHub tag](https://img.shields.io/github/tag-date/BricoloDuDimanche/Basalt.svg?style=flat-square)](https://github.com/BricoloDuDimanche/Basalt/releases)
[![License](https://img.shields.io/github/license/BricoloDuDimanche/Basalt.svg?style=flat-square)](https://github.com/BricoloDuDimanche/Basalt/blob/master/LICENSE)
[![Discord](https://img.shields.io/badge/chat-on%20Discord%20(%23basalt)-7289DA.svg?style=flat-square)](https://discord.gg/V82UXC5)
[![Donate](https://img.shields.io/badge/donate-Patreon-F96854.svg?style=flat-square)](https://www.patreon.com/Bowser65)

[Andesite](https://github.com/natanbc/andesite-node) plugin to provide extra features such as caching and sources like
Spotify.

## Supported platforms 
 - Spotify (Track, Playlist and Album)
 - Deezer (Track, Playlist and Album)
 - Tidal (Track, Playlist and Album)
 - PornHub (Track)
 - Mixcloud

### Coming soon
 - Napster

## How it work
### Custom sources

Just fetch /loadtracks with a link from a custom source provider.<br>
*Note* You can search PornHub videos with the `phsearch:` prefix.

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

### Cache
When enabled, Basalt will save loadtrack results to the cache for faster lookup on highly queried tracks.

Basalt will add a `cacheStatus` key in the json result, that will either be `HIT` or `MISS` depending on if the track
have been pulled from cache. You can bypass cache by adding `nocache=1` in the query parameters. Then `cacheStatus` will
always be `MISS`

## Configuration
All keys must be prefixed with `basalt.`. When using a HOCON file, they can be put inside a block named `basalt`.

| Key                  | Type     | Description                                                          | Default   |
|----------------------|----------|----------------------------------------------------------------------|-----------|
| cache.enabled        | boolean  | Whether or not to enable Redis caching                               | false     |
| cache.host           | string   | Redis host                                                           | 127.0.0.1 |
| cache.port           | int      | Redis port                                                           | 6379      |
| cache.ssl            | boolean  | Whether or not to connect using ssl                                  | false     |
| cache.password       | string   | Password used to authenticate                                        | null      |
| cache.ttl            | int      | TTL for cache entries                                                | 300       |
| source.spotify       | boolean  | Whether or not to enable Spotify integration                         | false     |
| source.deezer        | boolean  | Whether or not to enable Deezer integration                          | false     |
| source.tidal         | boolean  | Whether or not to enable Tidal integration                           | false     |
| source.mixcloud      | boolean  | Whether or not to enable Mixcloud integration                        | false     |
| source.pornhub       | boolean  | Whether or not to enable PornHub integration                         | false     |
| spotify.clientID     | string   | Spotify client ID                                                    | null      |
| spotify.clientSecret | string   | Spotify client secret                                                | null      |
| tidal-countryCode    | string   | Contry code used to perform requests to Tidal                        | US        |
| max-heavy-tracks     | int      | Maximum tracks to load from a playlist considered as [heavy](#heavy) | 10        |

### Heavy

Spotify and Deezer implementation not actually stream from the service. Basalt fetched an equivalent on YouTube and
plays that instead.
Problem is that since YouTube is taking action against bots and automated software you might get flagged for this and
get your IP temporarily or permanently banned as Basalt needs to make a heavy amount of requests (1 per track).

To prevent those issues, you should restrict this feature in your bot to prevent heavy loads, and only fetch a small
amount of tracks to prevent being flagged.

## Attribution

PornHub and Mixcloud integrations taken from [JukeBot](https://github.com/Devoxin/JukeBot) by [Devoxin](https://github.com/Devoxin) (Licensed under [Apache License 2.0](https://github.com/Devoxin/JukeBot/blob/master/LICENSE))
