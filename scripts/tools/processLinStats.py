import sys
import os
from os import path

# #-of linearizable histories calculated by Violat
NUM_LINEARIZABLE = {
    "ABQ": 714,
    "CHM": 959,
    "CLD": 520,
    "CLQ": 616,
    "CSLM": 1321,
    "CSLS": 417,
    "LBD": 967,
    "LBQ": 975,
    "LTQ": 1229,
    "PBQ": 955
}


def log_occurrence(stats, trace_file, result):
    occurrence = stats.get(trace_file, [])
    occurrence.append(result)
    stats[trace_file] = occurrence


def print_occurrences(stats):
    for trace, o_s in stats.items():
        print('%s: %s' % (trace, str(o_s)))


def write_lin_stats(stats, name, linear_stats, depth):
    tot_c = 0
    lin_c = 0
    part_lin_c = 0
    for trace, o_s in stats.items():
        tot_c += 1
        lin_c += 1 if o_s == [1] else 0
        part_lin_c += 1 if o_s.count(2) > 0 else 0

    line = "{0}\t{1}\t{2}\t{3}\t{4}\t{5:.1f}\n".format(
        name,
        tot_c,
        NUM_LINEARIZABLE.get(name, 0),
        depth,
        lin_c + part_lin_c,
        float(lin_c + part_lin_c) * 100 / NUM_LINEARIZABLE.get(name, 1)
    )

    with open(linear_stats, 'a') as f:
        f.write(line)


def process_lin_stats(name, out_file, depth, linear_stats):
    os.makedirs(path.dirname(linear_stats), exist_ok=True)

    trace_file = None
    # Read all lines
    with open(out_file, 'r') as file:
        stats = {}
        # count occurrences of each file
        for line in file:
            if not line:
                continue

            if not trace_file and line.startswith('History from file'):
                trace_file = line.split('/')[-1]

            if trace_file:
                processed = False
                if line.startswith('Linearizable with schedule'):
                    log_occurrence(stats, trace_file, 1)
                    processed = True
                elif line.startswith('Part Linearizable with schedule'):
                    log_occurrence(stats, trace_file, 2)
                    processed = True
                elif line.startswith('Part is not found to be') or line.startswith('Not linearizable'):
                    log_occurrence(stats, trace_file, 0)
                    processed = True

                if processed:
                    trace_file = None

    write_lin_stats(stats, name, linear_stats, depth)
    # print_occurrences()


def lin_stats_headers(linear_stats_file):
    os.makedirs(path.dirname(linear_stats_file), exist_ok=True)

    linear_header_line = "Name\ths\tlin\td\ttot\tpercent\n"
    with open(linear_stats_file, 'w+') as f:
        f.write(linear_header_line)


if __name__ == '__main__':
    if len(sys.argv) != 5:
        print("Please specify: (1) data structure name (2) out file (3) depth (4) report for linearizability stats ")
        print("Sample usage: python processLinStats.py out/ABQ/D1.txt 1 results/t3.txt")
        sys.exit()
    process_lin_stats(sys.argv[1], sys.argv[2], int(sys.argv[3]), sys.argv[4])
