from io import StringIO


def replaceBy(text: str, dic: dict) -> str:
    for k, v in dic.items():
        text = text.replace(k, v)
    return text


def getStr(strIO: StringIO) -> str:
    v = strIO.getvalue()
    strIO.close()
    return v
