#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

nbTest=1
benchmarkTime=20
warmingUpTime=20
#nbUsersInit=1000
nbHashCode=10000000
nbOps=10000000

# ADD, FOLLOW/UNFOLLOW, TWEET, READ, GROUP, PROFILE
ratio="5 5 15 60 5 10"

#alphas=("0.01" "0.1" "1")
#alphas=("0.1")

declare -A params

# Experience Figure 9

#params[juc]="-c juc.Counter -s ConcurrentHashSet -q Queue -m ConcurrentSkipListMap"
#params[dego]="-c CounterIncrementOnly -s ConcurrentHashSet -q QueueMASP -m ExtendedSegmentedSkipListMap"
#
#alphas=("1")
#
#> perf_retwis_fig_9.txt
#
#for impl in "${!params[@]}";
#do
#    for alpha in "${alphas[@]}";
#    do
#        for nbUsersInit in 100000 500000 1000000
#        do
#            for nbThread in 1 5 10 20 40 80
#            do
#                nbOps=$((1000000*nbThread))
#                for (( c=1; c<=nbTest; c++ ))
#                do
#                    perf=$(perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions -o perf.log ./test.sh ${params["${impl}"]} -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "${impl}_${nbThread}" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -A $alpha -z | grep -i "completion time :" | awk '{print $6}' | sed s/\(//g)
#                    echo ${impl}";"${alpha}";"${nbUsersInit}";"${nbThread}";"${perf} >> perf_retwis_fig_9.txt
#                done
#            done
#        done
#    done
#done
#
#params=()
#params[dap]="-c CounterIncrementOnly -s ShardedHashSet -q ShardedLinkedList -m ShardedSkipListMap"
#
#for impl in "${!params[@]}";
#do
#    for alpha in "${alphas[@]}";
#    do
#        for nbUsersInit in 100000 500000 1000000
#        do
#            for nbThread in 1 5 10 20 40 80
#            do
#                nbOps=$((1000000*nbThread))
#                for (( c=1; c<=nbTest; c++ ))
#                do
#                    perf=$(perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions -o perf.log ./test.sh ${params["${impl}"]} -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "${impl}_${nbThread}" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -A $alpha -z -D | grep -i "completion time :" | awk '{print $6}' | sed s/\(//g)
#                    echo ${impl}";"${alpha}";"${nbUsersInit}";"${nbThread}";"${perf} >> perf_retwis_fig_9.txt
#                done
#            done
#        done
#    done
#done

# Experience Figure 10

#params=()
#params[juc]="-c juc.Counter -s ConcurrentHashSet -q Queue -m ConcurrentSkipListMap"
#params[dego]="-c CounterIncrementOnly -s ConcurrentHashSet -q QueueMASP -m ExtendedSegmentedSkipListMap"
#
#alphas=("0.01" "0.1" "1")
#
#> perf_retwis_fig_10.txt
#
#for impl in "${!params[@]}";
#do
#    for alpha in "${alphas[@]}";
#    do
#        for nbUsersInit in 1000000
#        do
#            for nbThread in 1 5 10 20 40 80
#            do
#                nbOps=$((1000000*nbThread))
#                for (( c=1; c<=nbTest; c++ ))
#                do
#                    perf=$(perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions -o perf.log ./test.sh ${params["${impl}"]} -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "${impl}_${nbThread}" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -A $alpha -z -D | grep -i "completion time :" | awk '{print $6}' | sed s/\(//g)
#                    echo ${impl}";"${alpha}";"${nbUsersInit}";"${nbThread}";"${perf} >> perf_retwis_fig_10.txt
#                done
#            done
#        done
#    done
#done
#
#params=()
#params[dap]="-c CounterIncrementOnly -s ShardedHashSet -q ShardedLinkedList -m ShardedSkipListMap"
#
#for impl in "${!params[@]}";
#do
#    for alpha in "${alphas[@]}";
#    do
#        for nbUsersInit in 1000000
#        do
#            for nbThread in 1 5 10 20 40 80
#            do
#                nbOps=$((1000000*nbThread))
#                for (( c=1; c<=nbTest; c++ ))
#                do
#                    perf=$(perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions -o perf.log ./test.sh ${params["${impl}"]} -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "${impl}_${nbThread}" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -A $alpha -z -D | grep -i "completion time :" | awk '{print $6}' | sed s/\(//g)
#                    echo ${impl}";"${alpha}";"${nbUsersInit}";"${nbThread}";"${perf} >> perf_retwis_fig_10.txt
#                done
#            done
#        done
#    done
#done

# Experience Figure 6

initSize=16384
range=32768
nbTest=1
benchmarkTime=10
warmingUpTime=5
nbThreads=("1" "80")
#nbThreads=("1" "5" "10" "20" "40" "80")
objects=("juc.Counter" "CounterIncrementOnly" "LongAdder" "ConcurrentHashMap" "ExtendedSegmentedHashMap" "ConcurrentSkipListMap" "ExtendedSegmentedSkipListMap")
ratio="100 0 0"

for object in "${objects[@]}"; do
  python3 rm_file.py "Microbenchmark" "$object"

  for nbThread in "${nbThreads[@]}"; do
    for (( c=1; c<=nbTest; c++ )) do
        perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -m "$object" -t Microbenchmark -p -e -r "$ratio" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g "$nbThread"
        python3 analyse_perf.py perf.log "false" "$object" "" "$nbThread"
    done
  done
done

objects=("ConcurrentLinkedQueue" "QueueMASP")

for object in "${objects[@]}"; do
  python3 rm_file.py "Microbenchmark" "$object"

  for nbThread in "${nbThreads[@]}"; do
    for (( c=1; c<=nbTest; c++ )) do
        perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -m "$object" -t Microbenchmark -p -e -r "$ratio" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g "$nbThread" -a
        python3 analyse_perf.py perf.log "false" "$object" "" "$nbThread"
    done
  done
done

objects=("AtomicWriteOnceReference" "AtomicReference")
ratio="0 100 0"

for object in "${objects[@]}"; do
  python3 rm_file.py "Microbenchmark" "$object"

  for nbThread in "${nbThreads[@]}"; do
    for (( c=1; c<=nbTest; c++ )) do
        perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -m "$object" -t Microbenchmark -p -e -r "$ratio" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g "$nbThread"
        python3 analyse_perf.py perf.log "false" "$object" "" "$nbThread"
    done
  done
done