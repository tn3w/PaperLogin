# PaperLogin

A Minecraft Paper plugin that allows players to authenticate with a website using their Minecraft account.

## Features

- Two-way authentication between Minecraft and a website
- Redis-based storage for authentication data
- Login code generation for secure authentication
- Web verification commands for website-initiated authentication

## Requirements

- Paper Minecraft server (version 1.21.3)
- Java 17 or higher
- Redis server

## Setup

1. Clone this repository
2. Build the plugin with Gradle:
   ```bash
   ./gradlew clean jar --no-daemon
   ```
3. Copy the built JAR (`build/libs/PaperLogin<VERSION>.jar`) to your server's `plugins` folder
4. Start the server
5. Configure the plugin by editing `plugins/PaperLogin/config.yml`

### Install redis
1. Add Redis Labs repository using apt
   ```bash
   sudo add-apt-repository ppa:redislabs/redis -y
   ```
2. Update package index
   ```bash
   sudo apt update
   ```
3. Install Redis server
   ```bash
   sudo apt install redis-server -y
   ```
4. Enable and start Redis server
   ```bash
   sudo systemctl enable redis-server
   sudo systemctl start redis-server
   ```

## Configuration

```yaml
# Basic configuration for the PaperLogin plugin
redis:
  host: 'localhost'
  port: 6379
  password: ''
  # Set to true to use password
  auth-enabled: false
  
# Authentication Settings
auth:
  # Length of the generated login code
  login-code-length: 9
  # How long (in seconds) a login code remains valid
  login-code-validity: 300
  # How long (in seconds) a web verification code remains valid
  web-code-validity: 600
  # URL pattern for website login (set to empty string to disable)
  website-url: 'https://example.com/login/{code}'
```

## Commands

### For Players

- `/login` - Generates a login code that can be used on the website to authenticate
- `/verify <code>` - Verifies a code from the website to complete authentication

### Permissions

- `paperlogin.login` - Allows using the `/login` command (default: true)
- `paperlogin.verify` - Allows using the `/verify` command (default: true)

## Redis Data Structure

The plugin uses the following Redis key formats:

- `paperlogin:code:<player_uuid>` - Stores login verification codes
- `paperlogin:web:<code>` - Stores web verification codes

## Website Integration

To integrate with your website, you'll need to:

1. Connect to the same Redis instance
2. Read authentication data from Redis
3. Implement both:
   - Code verification for player-initiated login
   - Verification code generation for website-initiated login

## License

Copyright 2025 TN3w

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.