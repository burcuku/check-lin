import os
import statistics
import sys
from os import path

D_DEPTH_STR = 'd-hitting-depth: '
HISTORY_FILE_STR = 'History-file: '
NUM_OPS_STR = 'Num-operations: '
NUM_CONCURRENT_OPS = 'Num-concurrent-ops: '
NUM_CONCURRENT_OP_PAIRS = 'Num-concurrent-op-pairs: '
NUM_DISTINCT_SCHEDULES = 'Num-distinct-schedules: '


def calculate_stats(depth, num_histories, num_ops, num_conc_ops, num_conc_op_pairs, num_distinct_sch):
    stats = {}
    stats["depth"] = depth
    stats["files"] = num_histories
    stats["ops_average"] = sum(num_ops) / num_histories
    stats["conc_ops"] = {
        "max": max(num_conc_ops),
        "average": sum(num_conc_ops) / num_histories,
        "stddev": statistics.stdev(num_conc_ops)
    }
    stats["conc_op_pairs"] = {
        "max": max(num_conc_op_pairs),
        "average": sum(num_conc_op_pairs) / num_histories,
        "stddev": statistics.stdev(num_conc_op_pairs)
    }
    stats["dist_sch"] = {
        "max": max(num_distinct_sch),
        "average": sum(num_distinct_sch) / num_histories,
        "stddev": statistics.stdev(num_distinct_sch)
    }
    return stats


def process_file(stat_file, ds_stats):
    num_histories = 0
    depth = 0
    num_ops = []
    num_conc_ops = []
    num_conc_op_pairs = []
    num_distinct_sch = []

    with open(stat_file, "r") as stats:
        for line in stats:
            if line.startswith(D_DEPTH_STR):
                depth = int(line[len(D_DEPTH_STR):])
            elif line.startswith(HISTORY_FILE_STR):
                num_histories +=1
            elif line.startswith(NUM_OPS_STR):
                v = int(line[len(NUM_OPS_STR):])
                num_ops.append(v)
            elif line.startswith(NUM_CONCURRENT_OPS):
                v = int(line[len(NUM_CONCURRENT_OPS):])
                num_conc_ops.append(v)
            elif line.startswith(NUM_CONCURRENT_OP_PAIRS):
                v = int(line[len(NUM_CONCURRENT_OP_PAIRS):])
                num_conc_op_pairs.append(v)
            elif line.startswith(NUM_DISTINCT_SCHEDULES):
                v = int(line[len(NUM_DISTINCT_SCHEDULES):])
                num_distinct_sch.append(v)

    return calculate_stats(depth, num_histories, num_ops, num_conc_ops, num_conc_op_pairs, num_distinct_sch)


def write_history_stats(stat, name, history_stats_file):
    line = "{0}\t{1:.2f}\t{2}\t{3:.2f}\t{4:.2f}\t{5}\t{6:.2f}\t{7:.2f}\n".format(
        name,
        stat.get("ops_average", 0.0),
        stat.get("conc_ops", {}).get("max", 0),
        stat.get("conc_ops", {}).get("average", 0.0),
        stat.get("conc_ops", {}).get("stddev", 0.0),
        stat.get("conc_op_pairs", {}).get("max", 0),
        stat.get("conc_op_pairs", {}).get("average", 0.0),
        stat.get("conc_op_pairs", {}).get("stddev", 0.0),
    )
    with open(history_stats_file, 'a') as f:
        f.write(line)


def write_schedule_stats(stat, name, schedule_stats_file):
    line = "{0}\t{1}\t{2}\t{3:.0f}\t{4:.0f}\n".format(
        name,
        stat.get("depth", 0),
        stat.get("dist_sch", {}).get("max", 0),
        stat.get("dist_sch", {}).get("average", 0.0),
        stat.get("dist_sch", {}).get("stddev", 0.0)
    )
    with open(schedule_stats_file, 'a') as f:
        f.write(line)


def stats_blank_line(history_stats_file, schedule_stats_file):
    with open(schedule_stats_file, 'a') as f:
        f.write("\n")

    with open(history_stats_file, 'a') as f:
        f.write("\n")


def process_dir(name, stats_dir, history_stats, schedule_stats):
    c = 0
    if not path.isdir(stats_dir):
        print("Invalid stats directory: %s" % (stats_dir, ))
        return

    for filename in sorted(os.listdir(stats_dir)):
        stat_file = path.join(stats_dir, filename)
        stat = process_file(stat_file, ds_stats=(c == 0))
        if c == 0:
            write_history_stats(stat, name, history_stats)
        write_schedule_stats(stat, filename, schedule_stats)
        c += 1

    stats_blank_line(history_stats, schedule_stats)


def stats_headers(history_stats_file, schedule_stats_file):
    os.makedirs(path.dirname(history_stats_file), exist_ok=True)
    os.makedirs(path.dirname(schedule_stats_file), exist_ok=True)

    schedule_header_line = "Name\td\tmax\tavg\tstddev\n"
    with open(schedule_stats_file, 'w+') as f:
        f.write(schedule_header_line)

    history_header_line = "Name\tops\tmax(c)\tavg(c)\tstddev(c)\tmax(cp)\tavg(cp)\tstddev(cp)\n"
    with open(history_stats_file, 'w+') as f:
        f.write(history_header_line)


if __name__ == '__main__':
    if len(sys.argv) != 5:
        print("Please specify: (1) data structure name (2) stats directory (3) report for history stats (4) report for schedule stats")
        print("Sample usage: python processStats.py stats/statsABQ results/t1.txt result/t2.txt")
        sys.exit()
    process_dir(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4])
