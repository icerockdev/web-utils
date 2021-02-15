# Web utils
- [Ktor](https://ktor.io/) based web-server
- HTTP (Requests, HTTP-exceptions, Query params parser)
- Validation
- Internationalization

## Installation
````kotlin
// Append repository
repositories {
    maven { url = url("https://dl.bintray.com/icerockdev/backend") }
}

// Append dependency
implementation("com.icerockdev:web-utils:0.8.0")
````

## Library usage
Lib include tools for:
 - Start web server (Ktor netty server)
 - Basic configuration for Ktor module and sample with it [Sample](./sample/src/main)
 - Exception list for common cases (BadRequestException, ForbiddenException, etc) 
 - Localization support tools
 - Validation support tools
 - Query params parser to Object (include ktor feature with required fields) 
 - JsonDataLogger - log request and response data via MDC, working by CallLogging

## TODO
 - Support multiple connectors from HOCON file
 
## Contributing
All development (both new features and bug fixes) is performed in the `develop` branch. This way `master` always contains the sources of the most recently released version. Please send PRs with bug fixes to the `develop` branch. Documentation fixes in the markdown files are an exception to this rule. They are updated directly in `master`.

The `develop` branch is pushed to `master` on release.

For more details on contributing please see the [contributing guide](CONTRIBUTING.md).

## License
        
    Copyright 2020 IceRock MAG Inc.
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
