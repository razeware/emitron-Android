# emitron (Android)

__emitron__ is the code name for the Kodeco app. This repo contains the code for the Android version of the app.

## Contributing

To contribute a __feature__ or __idea__ to emitron, create an issue explaining your idea in detail.

If you find a __bug__, please create an issue.

If you find a __security vulnerability__, please contact emitron@kodeco.com as soon as possible. See [SECURITY.md](SECURITY.md) for further details.

There is more info about contributing in [CONTRIBUTING.md](CONTRIBUTING.md).

## Development & Setup

Anyone who wants to contribute to emitron can do so by cloning the repo and setting up the project.

However, only subscribers have access to streaming videos and Professional subscribers are allowed to download videos for offline playback.

### Setup (client_api_key)

1. Create a new file named `gradle.properties` in the project-level folder.
2. Copy `gradle.properties.dist` contents to `gradle.properties`.

#### Setup (google-services.json)

1. Open [Google Firebase console](https://firebase.google.com/).
2. Create a new project with an arbitrary name.
3. Add an Android app by following the instructions.
4. For the package name, put in `com.razeware.emitron`. You don't need the SHA-1 signing certificate.
5. Download the `google-services.json` file from the newly created app.
6. Add `google-services.json` to the `app` folder within the project.

That's it! You should be able to run the app and make your contributions! :]

### Optional Secrets

The **emitron** download feature requires a special secret key in `gradle.properties`:

- `app_token`. Required in order to enable downloads. This is not provided in the repo, and is not generally available. Additionally, if you don't own a "Professional" subscription, you won't be able to use downloads.

> __NOTE:__ To get the release build secrets, check the emitron S3 bucket, or contact emitron@kodeco.com. Developers should never need these, as CI will handle it.

If you are working on the download functionality and are having problems without an `app_token`, contact emitron@kodeco.com and somebody will assist you with your specific needs.

### Continuous Integration & Deployment

__emitron__ uses GitHub Actions to perform continuous integration and deployment. Every PR is built and tested before it can be merged.

- Merges to `development` will create a new build of the emitron β app on Firebase.
- Merges to `production` will create a new build of the emitron production app on Google Play Store.

