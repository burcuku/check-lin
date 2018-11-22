from subprocess import call
import sys


def create_tests(classUnderTest, input_file, package_name, tests_file, stat_dir, stat_file, min_depth, max_depth):
    print("Creating tests...")

    for d in range(min_depth, max_depth):
        sbt_params = "run {0} {1} {2} {3}{6} stat/{4} {5}{6} {6}".format(classUnderTest, input_file, package_name, tests_file, stat_dir, stat_file, d)
        call("sbt -J-Xmx4G -J-Xms4G \"{0}\"".format(sbt_params), shell=True)
        print("Created Tests for d={0} for file: {1}\n".format(d, input_file))

    print("Creating tests completed...")


if __name__ == '__main__':
    if len(sys.argv) != 9:
        print("Please specify the class under test, directory of input files, name of test files, "
              "stat dir, stat file, and the min and max depth for the hitting families.")
        sys.exit()
    create_tests(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5], sys.argv[6], sys.argv[7], sys.argv[8])
