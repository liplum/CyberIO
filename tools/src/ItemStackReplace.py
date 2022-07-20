import sys
import re

args = sys.argv

ItemStackRegex = re.compile("ItemStack\((.*),(.*)\)")
LiquidStackRegex = re.compile("LiquidStack\((.*),(.*)\)")


def test():
    matched = ItemStackRegex.match("ItemStack(Items.copper,12)")
    print(matched)
    print(matched.group())
    print(matched.groups())

    unmatched = ItemStackRegex.match("ItemStack(Items.copper12)")
    print(unmatched)


def matchAndReplace(line: str, regex):
    matched = regex.match(line)
    if matched is not None:
        groups = matched.groups()
        raw = matched.group()
        new = f"${groups[0]}+{groups[1]}"
        return line.replace(raw, new)
    else:
        return line


def main():
    lines = []
    with open(file=args[1]) as f:
        for line in f.readlines():
            line = matchAndReplace(line, ItemStackRegex)
            line = matchAndReplace(line, LiquidStackRegex)
            lines.append(line)
    with open(file=args[1], mode='w') as f:
        f.writelines(lines)


if __name__ == '__main__':
    main()
