import Config
from Utils import *

placeholders = {
    "*-": "{",
    "-*": "}",
}
template = """typealias {handler}{T} = ({lambdaT}) -> Unit
open class {clz}{T} *-
    private val subscribers: HashSet<{handler}{T}> = HashSet()
    operator fun invoke({invokeArgs}) *-
        for (handler in subscribers)
            handler({callArgs})
    -*
    fun add(handler: {handler}{T}): {clz}{T} *-
        subscribers.add(handler)
        return this
    -*
    fun remove(handler: {handler}{T}): {clz}{T} *-
        subscribers.remove(handler)
        return this
    -*
    fun clear(): {clz}{T} *-
        subscribers.clear()
        return this
    -*
    operator fun minusAssign(handler: {handler}{T}) *-
        subscribers.remove(handler)
    -*
    operator fun plusAssign(handler: {handler}{T}) *-
        subscribers.add(handler)
    -*
-*
"""


def genDelegate(argNum: int, clz: str, handler: str):
    if argNum == 0:
        T = ""
        invokeArgs = ""
        callArgs = ""
        numerSuffix = ""
    else:
        end = argNum - 1
        TStr = StringIO()
        invokeArgsStr = StringIO()
        callArgsStr = StringIO()
        numerSuffix = str(argNum)
        TStr.write('<')
        for i in range(argNum):
            ipp = str(i + 1)
            TStr.write(f"Arg{ipp}")
            invokeArgsStr.write(f"arg{ipp}:Arg{ipp}")
            callArgsStr.write(f"arg{ipp}")
            if i != end:
                TStr.write(',')
                invokeArgsStr.write(',')
                callArgsStr.write(',')

        TStr.write('>')

        T = getStr(TStr)
        invokeArgs = getStr(invokeArgsStr)
        callArgs = getStr(callArgsStr)
    gened = template.format(
        clz=clz + numerSuffix,
        handler=handler + numerSuffix,
        invokeArgs=invokeArgs,
        callArgs=callArgs,
        T=T,
        lambdaT=T.strip('<').strip('>')
    )
    res = replaceBy(gened, placeholders)
    return res


def create(number: int, path: str = "Delegates.kt"):
    clz = Config.v("DelegateClassName", "Delegate")
    handler = Config.v("DelegateHandlerName", "DelegateHandler")
    with StringIO() as delegate:
        for i in range(number):
            delegate.write(genDelegate(i, clz, handler))
            delegate.write('\n')
        content = delegate.getvalue()
        with open(path, "w") as f:
            f.write(content)


def main():
    Config.read()
    Config.turnOnAutoSave()
    number = Config.v("DelegateNumber", 20)
    path = Config.v("DelegateFilePath", "Delegates.kt")
    create(number, path)
    Config.saveWhenOn()


if __name__ == '__main__':
    main()
