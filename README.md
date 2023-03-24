# Mobile app for promoting and licensing artists' works

This is an app that I developed for my undergraduate thesis. Its purpose is to offer the artists the ability to upload and distribute their music in a controlled manner via the other users of this app by implementing a usage request, evaluation and license generating mechanism to offer the applicants the permission of complete or partially use of the desired audio content.

| Home in Light Mode | Home in Dark Mode |
| - | - |
| <img src="art/home_light.png" width="300px"/> | <img src="art/home_dark.png" width="300px"/> |

## ✨ Features

- Authentication system
- User/artist profiles
- Music catalog sorted by the newest songs, most played etc.
- Song search engine
- Song upload/update with art, related data, genre etc.
- Dynamic music player and mini player
- Background playing even with the app closed and notification controls
- Song analytics
- Mechanism for requesting permission to reuse songs
- Mechanism for viewing and evaluating song usage requests
- Song usage license generator
- In-app licences viewer
- Licenses and songs downloader as packets
- Light and Dark mode
- Other nice things

## 🔜 Planned

- Song recommendation service based on user preferences
- Asynchronous mechanism of songs loading for faster playback
- Songs likes and playlists
- Playback history, likes history, playlists created by users
- License requests notifications
- Song recognision
- Payment mechanism for obtaining a license
- Better analytics

## 🔮 Technologies

- **Java** for the app logic
- **Firebase** as the backend
- **Retrofit** for type-safe HTTP requests
- **GSON** to convert Java Objects into JSON and back
- **Glide** for image loading and caching
- **Palette**  to create color-coordinated visual elements for songs based on their album art
- Other nice things

## 🚀 Getting started

To get started with the app, follow these steps:

1. Clone this repository to your local machine
2. Import the project into Android Studio
3. Build and run the app on an emulator or physical device

## 👀 Preview

| Music player | Song data | Notification control |
| - | - | - |
| <img src="art/music_player.jpeg" width="300px"/> | <img src="art/music_player_analytics.png" width="300px"/> | <img src="art/notification_control.png" width="300px"/> |

| Song upload (blank) | Song upload | Song edit |
| - | - | - |
| <img src="art/song_upload_blank.png" width="300px"/> | <img src="art/song_upload_filled.png" width="300px"/> | <img src="art/song_edit.png" width="300px"/> |

| Menu | Profile | Song search |
| - | - | - |
| <img src="art/bottom_menu.png" width="300px"/> | <img src="art/profile.png" width="300px"/> | <img src="art/search.png" width="300px"/> |

| Permission request | Request evaluation |
| - | - |
| <img src="art/permission_request.png" width="300px"/> | <img src="art/request_evaluation.png" width="300px"/> |

| Unevaluated requests | Evaluated requests | Declined requests |
| - | - | - |
| <img src="art/requests_notevaluated.png" width="300px"/> | <img src="art/requests_accepted.png" width="300px"/> | <img src="art/requests_declined.png" width="300px"/> |

| Received licenses | Given licenses | License viewer |
| - | - | - |
| <img src="art/licenses_received.png" width="300px"/> | <img src="art/licenses_given.png" width="300px"/> | <img src="art/license_viewer.png" width="300px"/> |
