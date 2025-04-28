import os
from collections import Counter

# List of types and associated filenames
types = [
    "AtomicLong",
    "ConcurrentHashMap",
    "ConcurrentSkipListSet",
    "ConcurrentLinkedQueue"
]

def process_file(filename):
    with open(filename, 'r') as f:
        content = f.read()
    # Remove leading "+" if any, and split by spaces
    methods = [method.lstrip('+') for method in content.split()]
    counter = Counter(methods)
    return counter

def generate_pie(counter, typename):
    total_invocations = sum(counter.values())
    most_common = counter.most_common(3)

    # The names of the top 3 methods
    top_methods = set(method for method, _ in most_common)

    # Methods that are not in top 3
    other_methods = [method for method in counter if method not in top_methods]
    other_invocations = sum(counter[method] for method in other_methods)

    entries = []
    for method, count in most_common:
        proportion = (count / total_invocations) * 100
        entries.append(f"{proportion:.1f}/\\scriptsize {method}")

    if other_methods:
        proportion = (other_invocations / total_invocations) * 100
        entries.append(
            f"{proportion:.1f}/\\scriptsize \\textit{{\\shortstack[c]{{others\\\\({len(other_methods)})}}}}"
        )

    entries_str = ", ".join(entries)
    return f"\\pie[rotate=115, radius=1.3, before number=\\printonlylargeenough{{10}}, after number=\\ifprintnumber\\%\\fi]{{{entries_str}}} % {typename}"

def main():
    for typename in types:
        filename = f"{typename}.txt"
        if os.path.exists(filename):
            counter = process_file(filename)
            pie = generate_pie(counter, typename)
            print(pie)
        else:
            print(f"Warning: {filename} not found.")

if __name__ == "__main__":
    main()
