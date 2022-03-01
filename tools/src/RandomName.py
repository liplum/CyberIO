from io import StringIO


def chars(start, end):
    if isinstance(start, str):
        start = ord(start)
    if isinstance(end, str):
        end = ord(end)
    with StringIO() as s:
        for i in range(start, end):
            s.write('\'')
            s.write(chr(i))
            s.write('\'')
            s.write(',')
        return s.getvalue()
