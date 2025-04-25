import os
import argparse

def setUp_parser():
    parser = argparse.ArgumentParser(description='Generate a latex graph from the values in the files')
    parser.add_argument('-t', '--typeObj', dest='type_obj', type=str, help='Object\'s type')
    parser.add_argument('-f', '--files', dest='files', action="append", type=str, help='Files')
    parser.add_argument('-l', '--label', dest='label', action='store_false', help='Add the y label for the first plot')

    args = parser.parse_args()

    return args

def generate_latex_file(files, type):
    with open(type+'.tex', 'w') as f:
        f.write("\\begin{axis}[\n")
        if type == "Counter":
            f.write("    ylabel={Kop/s per process},\n")
            f.write("    xlabel={\\# threads},\n")
            f.write("    extra y ticks={1},\n")
            f.write("    extra y tick labels={$0$},\n")
        f.write("    xticklabels={1,5,10,20,40,80},\n")
        f.write("    xtick={1,2,3,4,5,6},\n")
        f.write("    ytick={0,10,1.0E2,1.0E3,1.0E4,1.0E5,1.0E6,1.0E7,1.0E8,1.0E9,1.0E10,1.0E11},\n")
        f.write("    ymin=1,\n")
        f.write("    ymax=100000000,\n")
        f.write("    xmin=0,\n")
        f.write("    xmax=6,\n")
        f.write("    y=0.35cm,\n")
        f.write("    ymode=log,\n")
        f.write("    log origin=0,\n")
        f.write("    ymajorgrids,\n")
        f.write("    axis x line*=bottom,\n")
        f.write("    axis y line*=left,\n")
        f.write("    ylabel style={font=\\Huge},\n")
        f.write("    xlabel style={font=\\Huge},\n")
        f.write("    title style={font=\\Huge},\n")
        f.write("    tick label style={font=\\Huge},\n")
        f.write("    legend style={draw=none, at={(0.5,-0.3)}, anchor=north,font=\\Huge},\n")
        f.write("    title="+type+"]\n")

        for filename in files:
            f.write("    \\addplot coordinates {\n")
            try:
                with open(filename, 'r') as file:
                    #with open(os.path.basename(filename), 'r') as file:
                    for line in file:
                        try:
                            x, y = line.strip().split()
                        except ValueError:
                            print(filename)
                            print(line.strip().split())

                        f.write(f"       ({x}, {y})\n")
            except FileNotFoundError:
                print(f"File not found : {filename}")
            f.write("   };\n\n")

        f.write("  \\legend{")
        for filename in files:
            obj_name = os.path.basename(filename)[:-4]
            obj_name = obj_name.split("_")[0]
            f.write(f"{obj_name}, ")
        f.write("};\n")
        f.write("\\end{axis}")

args = setUp_parser()

generate_latex_file(args.files, args.type_obj)
