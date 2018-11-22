from subprocess import call
import os, sys
from os import path


def run_tests(test_path, out_file):
    print("Running tests for [%s]..." % (test_path,))

    os.makedirs(path.dirname(out_file), exist_ok=True)

    with open(out_file, "w") as out:
        for filename in os.listdir(test_path):
            if not filename.endswith(".class"):
                continue

            class_file, _ = path.splitext(path.join(test_path, filename).replace('/', '.').replace('\\', '.'))

            # print("Running: %s" % (class_file,))
            call("java {0}".format(class_file), shell=True, stdout=out)


if __name__ == '__main__':
    if len(sys.argv) != 3:
        print("Please specify the directory of test files and a directory name for the output files")
        sys.exit()

    run_tests(sys.argv[1], sys.argv[2])
