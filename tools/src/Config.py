import json
import os
from typing import Dict, Any

ConfigPath = "config.json"
config: Dict[str, Any] = {}
autoSave = False


def turnOnAutoSave():
    global autoSave
    autoSave = True


def turnOffAutoSave():
    global autoSave
    autoSave = False


def read():
    def readJson():
        global config
        with open(ConfigPath, encoding="utf-8") as jsonF:
            content: str = jsonF.read()
            config = json.loads(content)

    createWhenNonexist()
    try:
        readJson()
    except:
        os.remove(ConfigPath)
        createWhenNonexist()
        readJson()


def f(key: str, *args, **kwargs) -> Any:
    if key not in config:
        return key
    original: str = config[key]
    try:
        return original.format(*args, **kwargs)
    except:
        return key


def v(key: str, default: Any = None) -> Any:
    if key not in config:
        if autoSave:
            config[key] = default
        return default
    value = config[key]
    if default is not None and not isinstance(value, type(default)):
        if autoSave:
            config[key] = default
        return default
    else:
        return value


def writeToConfig(content):
    with open(ConfigPath, "w", encoding="utf-8") as jsonF:
        jsonF.write(content)


def createWhenNonexist():
    if not os.path.exists(ConfigPath):
        writeToConfig(DefaultContent)


def saveWhenOn():
    if autoSave:
        writeToConfig(json.dumps(config, indent=4))


DefaultContent: str = """{

}"""
