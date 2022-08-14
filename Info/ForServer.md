# For Server

### Configuration

Cyber IO provides a configuration file for hosts to edit settings, such as enabling [auto-update](#auto-update).

It locates in **./config/cyberio/config.json**.

### Auto-update

Cyber IO supports auto-update when you run it on the server version, whether you use it as *.zip* or *.jar*.

For security, auto-update is disabled as default in the config.

The command `cio-update` let you check and update to the latest version manually.

Cyber IO will check the latest version from [update info file](../update) on this GitHub repository.
To do so, you should keep the network be able to access GitHub and firewall open at a specific port.

### Reload or Reset Config by Command in-game

Using command "cio-reload-config" in-game, you can reload the config file from disk.

Using command "cio-reset-config" in-game, you can reset/create the config file on disk.

### Version Number

Cyber IO's version number is always in the format: <Major&gt;.<Minor&gt;

If some bugs were fixed or new visual effects changed/added on only **Client Side**, impossible to break the server,
it will keep the last version number, which is helpful in maintaining version consistency.

Similarly, if some functions were added or changed but still multiplayer compatible, it also keeps the last number.

On the other hand, if some bugs were fixed and affected both **Server Side** and **Client Side**,
its version number will update to date immediately.

Auto-update will check the true version number to judge whether it's necessary to update.

Thanks to auto-update, therefore, you don't have to worry about the version between client and server.


