# TigerGuard
Administrative-like Discord bot

TigerGuard is an opt-in bot built upon Java Discord API (JDA) for interacting with the Discord service, and further utilizes LavaLink for audio related features. There are a few other libraries such as JavaTuples and EmojiJava which are used for making certain things easier where native Java logic needed a bit more work.

## Features
- Reaction Roles
- Embed Polls
- XP System
- Date system (related to XP system and other minor features)
- Audio support in voice channels
- Role handling and management (to an extent)
- Commands of course, a bit too many to name

## Notes
- For the database logic, it is built for MySQL/MariaDB databases primarily, other databases may need additional code changes to work. As for functionality with the xp system, the bot currently is in the process of directing all code to support a unified data structure as the current structure is moving away from purely server-oriented to global-oriented with server flavored logic sprinkled about, such changes will come over time as I find room for what logic is currently sought next.
- Debugging, currently there is a boolean for enabling/disabling the debug logic which mostly just related to additional outputs for certain areas of code to be produced. By default, the bot will print out embed messages (mostly time-limited) which will advice a user on what the bot did, is trying to do, or what it could not do. Some may be private messages, while others not so much.
- Administrative commands, these are denoted with the command structure of `/tg...`, any command not starting explicitly with those begginning characters are not administrative commands, thus in your server, it is best to restrict these commands to only users that need them.
