import os
from zipfile import ZipFile

deployDir = "main/build/tmp/deploy"
unzipDir = "UnzipJar"

from os import listdir
from os.path import isfile, join


def main():
    deployJarName = os.listdir(deployDir)[0]
    deployJar = os.path.join(deployDir, deployJarName)
    with ZipFile(deployJar, 'r') as jar:
        jar.extractall(unzipDir)


if __name__ == '__main__':
    main()
