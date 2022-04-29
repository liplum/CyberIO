# For Server

### Configuration

Cyber IO provides a configuration file for hosts to edit settings, such as enabling [auto-update](#auto-update).

It locates in **./config/cyberio/config.json**.

### Auto-update

Cyber IO supports auto-update when you run it on the server version, whether you use it as *.zip* or *.jar*.

For security, auto-update is disabled as default in the config. 

Cyber IO will check the latest version from [update info file](update) on this GitHub repository.
To do so, you should keep the network be able to access GitHub and firewall open at a specific port.

### Reload or Reset Config by Command in-game
Using command "cio-reload-config" in-game, you can reload the config file from disk.
Using command "cio-reset-config" in-game, you can reset/create the config file on disk.