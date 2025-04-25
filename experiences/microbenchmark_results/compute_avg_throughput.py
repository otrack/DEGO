import os
import argparse
import statistics

def setUp_parser():
    parser = argparse.ArgumentParser(description='Compute and analyze statistical metrics such as mean, maximum, minimum, and confidence intervals.')
    parser.add_argument('-t', '--typeObj', dest='type_objs', action="append", type=str, help='Objects\' name(s)')
    parser.add_argument('-typeOp', dest='type_op', type=str, help='Operation\'s performance')
    parser.add_argument('-p', '--path', dest='directory_to_write', default='/', help='Name of the directory where the files will be saved')
    parser.add_argument('-d', '--detailed', dest='detailed', action='store_true', help='Compute min, max and confidence interval')
    parser.add_argument('-u', '--unit', dest='unit', type=int, default='1', help='Divide the values by \'unit\'')
    parser.add_argument('-a', '--aggregate', dest='aggregate', action='store_false', help='Compute the global throughput')
    parser.add_argument('-s', '--scientific', dest='scientific', action='store_true', help='Save the value in scientific format')

    args = parser.parse_args()

    return args

def compute_statistics(nThread, data):
    if not data:
        return None, None, None, None, None

    data = [int(x/args.unit) for x in data]

    if args.aggregate:
        data = [int(x/nThread) for x in data]
    mean = sum(data) / len(data)

    if args.detailed:
        max_value = max(data)
        min_value = min(data)
        standard_deviation = statistics.stdev(data)
        margin_of_error = 1.96 * standard_deviation / len(data) ** 0.5
        if args.scientific:
            return ("{:.1e}".format(int(mean)), "{:.1e}".format(int(max_value)), "{:.1e}".format(int(min_value)), "{:.1e}".format(int(mean + margin_of_error)), "{:.1e}".format(int(mean - margin_of_error)))
        else:
            return (int(mean), int(max_value), int(min_value), int(mean + margin_of_error), int(mean - margin_of_error))

    if args.scientific:
        return ("{:.1e}".format(int(mean)),)
    else:
        return(int(mean),)

def get_nThread_and_perf_values(list_files):

    list_dict = list()

    for file in list_files:
        dict_perf = {}
        for line in file:
            parts = line.strip().split()
            if len(parts) == 2:
                nb_thread_part = parts[0]
                perf_part = parts[1]
                try:
                    nb_thread_value = int(nb_thread_part)
                    perf_value = float(perf_part)
                    if nb_thread_value not in dict_perf:
                        dict_perf[nb_thread_value] = []
                    dict_perf[nb_thread_value].append(perf_value)
                except ValueError:
                    pass

        list_dict.append((file, dict_perf))

    return list_dict

def write_values(file, values):

    str_to_write = ""

    for value in values:
        str_to_write += str(value) + " "

    str_to_write += "\n"

    file.write(str_to_write)

def load_files(type_objects, type_op):

    name_files = list()

    for type_object in type_objects:
        name_files.append("microbenchmark_results/"+type_object + "_" + type_op + ".txt")

    list_files = list()

    for name_file in name_files:
        try:
            list_files.append(open(name_file, 'r+'))
        except FileNotFoundError:
            print(f"File not found : {name_file}")

    return list_files

def close_files(list_files):

    for file in list_files:
        file.close()

if __name__ == "__main__":

    args = setUp_parser()

    list_files = load_files(args.type_objs, args.type_op)

    list_thread_values_tuples = get_nThread_and_perf_values(list_files)

    for nthread_perf_tuples in list_thread_values_tuples:

        file = nthread_perf_tuples[0]
        nthread_perf_dict = nthread_perf_tuples[1]

        file_name = args.directory_to_write + os.path.basename(file.name)

        file_to_write = open(file_name, 'w')

        if args.detailed:
            file_to_write.write("AVG | MAX | MIN | UPPER BOUND | LOWER BOUND (x"+ str(args.unit) +")\n")

        for key,value in nthread_perf_dict.items():
            values_to_write = (key,) + compute_statistics(key, value)
            write_values(file_to_write, values_to_write)

        file_to_write.close()

    close_files(list_files)
