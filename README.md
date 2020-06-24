# emitron (Android)

__emitron__ is the code name for the raywenderlich.com app. This repo contains the code for the Android version of the app.

## Contributing

To contribute a __feature__ or __idea__ to emitron, create an issue explaining your idea.

If you find a __bug__, please create an issue.

If you find a __security vulnerability__, please contact emitron@razeware.com as soon as possible. See [SECURITY.md](SECURITY.md) for further details.

There is more info about contributing in [CONTRIBUITNG.md](CONTRIBUTING.md).

## Development

Currently, only people that hold an active raywenderlich.com subscription may use emitron. Non-subscribers will be shown a "no access" page on login. Subscribers have access to streaming videos, and a subset of subscribers (ones with a "Professional" subscription) is allowed to download videos for offline playback.

### Setup

1. Copy `gradle.properties.dist` to `gradle.properties`
2. Add `google-services.json` to `app` directory from Firebase console.

### Secrets Management

__emitron__ requires 2 secrets:

- `SSO_SECRET`. This is used to ensure secure communication with `guardpost`, the raywenderlich.com authentication service. Although this is secret, a sample secret is provided inside this repo. This shouldn't be used to create a beta or production build.
- `APP_TOKEN`. Required in order to enable downloads. This is not provided in the repo, and is not generally available.

> __NOTE:__ To get the release build secrets, check the emitron S3 bucket, or contact emitron@razeware.com. Developers should never need these, as CI will handle it.

If you are working on the download functionality and are having problems without an `APP_TOKEN`, contact emitron@razeware.com and somebody will assist you with your specific needs.

For further details, check out [CONTRIBUTING](CONTRIBUTING.md).


### Continuous Integration & Deployment

__emitron__ uses GitHub Actions to perform continuous integration and deployment. Every PR is built and tested before it can be merged.

- Merges to `development` will create a new build of the emitron β app on Firebase.
- Merges to `production` will create a new build of the emitron production app on Google Play Store.

