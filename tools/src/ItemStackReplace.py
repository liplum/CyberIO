import sys
import re
import os
import glob
from pathlib import Path

args = sys.argv

ItemStackRegex = re.compile("ItemStack\((.*),(.*)\)")
LiquidStackRegex = re.compile("LiquidStack\((.*),(.*)\)")


def allFiles(folder: str):
    for filename in Path(folder).rglob('**/*'):
        yield os.path.abspath(filename)


def test():
    matched = ItemStackRegex.search("ItemStack(Items.copper,12)")
    print(matched)
    print(matched.group())
    print(matched.groups())

    unmatched = ItemStackRegex.search("ItemStack(Items.copper12)")
    print(unmatched)

    print(ItemStackRegex.search("                        ItemStack(CioItem.ic, 3),"))


def matchAndReplace(line: str, regex):
    matched = regex.search(line)
    if matched is not None:
        groups = matched.groups()
        raw = matched.group()
        new = f"{groups[0]}+{groups[1]}"
        return line.replace(raw, new)
    else:
        return line


def replace(path):
    lines = []
    with open(file=path, encoding="UTF-8") as f:
        for line in f.readlines():
            line = matchAndReplace(line, ItemStackRegex)
            line = matchAndReplace(line, LiquidStackRegex)
            lines.append(line)
    with open(file=path, mode='w', encoding="UTF-8") as f:
        f.writelines(lines)


def main():
    if len(args) < 1:
        print("No path given!")
        return
    path = args[1]
    if os.path.isfile(path):
        replace(path)
    elif os.path.isdir(path):
        for filePath in allFiles(path):
            if os.path.isfile(filePath):
                replace(filePath)
    else:
        print(f"Invalid path {path}")


if __name__ == '__main__':
    main()
