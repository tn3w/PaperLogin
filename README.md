# PaperLogin

Paper plugin that lets players log in to your website with their Minecraft account.
Codes are exchanged through Redis, so the website only needs a Redis client.

Requires Paper 1.21.3+ (Java 21) and Redis. Jedis is downloaded by Paper at startup.

## Build

```bash
./gradlew build          # build/libs/PaperLogin-<version>.jar
./gradlew runServer      # local test server
```

Copy the jar into `plugins/` and edit `plugins/PaperLogin/config.yml`.

## Flows

**Player-initiated:** `/login` shows a code (and a clickable link if `website-url` is
set). The website looks up `paperlogin:code:<CODE>`. Running `/login` again returns the
same code and extends it.

**Website-initiated:** the website creates hash `paperlogin:web:<code>` (any fields,
e.g. `identifier`) and shows the code. The player runs `/verify <code>`; the plugin adds
the player's info. A code claimed by one player cannot be claimed by another.

Redis work runs off the main thread; if Redis is down players get an error message.

## Redis keys

| Key | Type | Content | TTL |
| --- | --- | --- | --- |
| `paperlogin:code:<CODE>` | hash | `uuid`, `username`, `isOp` | `login-code-validity` |
| `paperlogin:player:<uuid>` | string | current login code | `login-code-validity` |
| `paperlogin:web:<code>` | hash | website fields + `uuid`, `username`, `isOp` | `web-code-validity` |

Login codes use `A-Z` and `2-9` without look-alike characters (`I`, `O`, `0`, `1`).

## Config

```yaml
redis:
  host: localhost
  port: 6379
  password: ''          # empty = no auth

auth:
  login-code-length: 9
  login-code-validity: 300   # seconds
  web-code-validity: 600     # seconds
  website-url: 'https://example.com/login/{code}'   # empty = no link
```

## Commands

| Command | Permission (default: everyone) |
| --- | --- |
| `/login` | `paperlogin.login` |
| `/verify <code>` | `paperlogin.verify` |

## License

[Apache-2.0](LICENSE)
