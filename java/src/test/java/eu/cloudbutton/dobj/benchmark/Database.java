package eu.cloudbutton.dobj.benchmark;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.types.Counter;
import eu.cloudbutton.dobj.set.ConcurrentHashSet;
import eu.cloudbutton.dobj.Timeline;
import eu.cloudbutton.dobj.key.Key;
import eu.cloudbutton.dobj.key.KeyGenerator;
import eu.cloudbutton.dobj.key.SimpleKeyGenerator;
import lombok.Getter;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;

import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomGeneratorFactory;

@Getter
public class Database {

    private final String typeMap;
    private final String typeSet;
    private final String typeQueue;
    private final String typeCounter;
    private final Factory factory;

    private final int nbThread;
    private final int nbUsers;

    private Map<Key, Integer> mapProfiles;
    private Map<Key, Set<Key>> mapFollowers;
    private Map<Key, Set<Key>> mapFollowing;
    private Map<Key, Timeline<String>> mapTimelines;
    private Map<Key, Integer> mapCommunityStatus;
    private Set<Key> community;

    private final Map<Integer, Key> mapIndiceToKey;
    private final Map<Key, Integer> mapKeyToIndice;
    private final long[] reciprocalDegree;
    private final long[] inDegree;
    private final long[] outDegree;
    private long reciprocal = 0; // Number of nodes with a reciprocal degree bigger than 0
    private long out = 0; // Number of nodes with an out degree bigger than 0
    private long in = 0; //Number of nodes with an in degree bigger than 0
    private long edges_r = 0; // Number of reciprocal edges
    private long edges_d = 0; // Number of directed edges
    private long diag = 0;
    private double diag_sum_r_dist;
    private double diag_sum_d_dist;

    private final KeyGenerator keyGenerator;
    private final ConcurrentSkipListMap<Long, Key> usersFollowProbability;
    private Map<Integer,ConcurrentSkipListMap<Long, Key>> localUsersFollowProbability;
    private long usersFollowProbabilityRange;
    private Map<Integer,Long> localUsersFollowProbabilityRange;
    private final Map<Integer, ConcurrentSkipListMap<Long,Key>> localUsersUsageProbability;
    private final Map<Integer, Long> localUsersUsageProbabilityRange;
    private final List<List<Key>> listLocalUser;
    private final Map<Integer, Map<Key, Queue<Key>>> listLocalUsersFollow;
    private final Map<Integer, List<Key>> mapUserToAdd;
    private Map<Key, Integer> mapUserUsage;
    public final boolean usageStat = false;
    public final boolean isDAP = true;
    private final Map<Key, Integer> mapUserToIndiceThread;
    private final Map<Key, Queue<Key>> mapListUserFollow;
    private final AtomicInteger count;
    private final ThreadLocal<Random> random;

    private final double alpha;

    private static final double SCALEUSAGE = 10.0;
    private static final double SCALEFOLLOW = 10.0;
    private static final double FOLLOWERSHAPE = 1;
    private static final double FOLLOWINGSHAPE = 1;

    private final Counter counter;
    private final ExecutorService executorService;

    public Database(String typeMap, String typeSet, String typeQueue, String typeCounter,
                    int nbThread, int nbUserInit, double alpha) throws ClassNotFoundException {

        this.typeMap = typeMap;
        this.typeSet = typeSet;
        this.typeQueue = typeQueue;
        this.typeCounter = typeCounter;
        this.factory = new Factory(typeMap,typeSet,typeQueue,typeCounter,"List");

        this.nbThread = nbThread;
        this.random = ThreadLocal.withInitial(() -> new Random(94));

        try {
            mapFollowers = factory.newMap();
            mapFollowing = factory.newMap();
            mapTimelines = factory.newMap();
            mapProfiles = factory.newMap();
            community = factory.newSet();
            counter = factory.newCounter();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        mapCommunityStatus = new ConcurrentHashMap<>();

        usersFollowProbability = new ConcurrentSkipListMap<>();
        localUsersUsageProbability = new ConcurrentHashMap<>();
        localUsersUsageProbabilityRange = new ConcurrentHashMap<>();
        localUsersFollowProbabilityRange = new ConcurrentHashMap<>();
        localUsersFollowProbability = new ConcurrentSkipListMap<>();
        nbUsers = nbUserInit;
        keyGenerator = new SimpleKeyGenerator();
        listLocalUser = new ArrayList<>();
        listLocalUsersFollow = new ConcurrentHashMap<>();
        count = new AtomicInteger();

        mapIndiceToKey = new ConcurrentHashMap<>();
        mapKeyToIndice = new ConcurrentHashMap<>();
        reciprocalDegree = new long[nbUsers];
        inDegree = new long[nbUsers];
        outDegree = new long[nbUsers];

        for (int i = 0; i < nbThread; i++) {
            localUsersUsageProbability.put(i , new ConcurrentSkipListMap<>());
            localUsersUsageProbabilityRange.put(i, 0L);
            listLocalUsersFollow.put(i, new HashMap<>());
            listLocalUser.add(new ArrayList<>());
        }

        mapUserToAdd = new ConcurrentHashMap<>();
        mapUserToIndiceThread = new HashMap<>();
        mapListUserFollow = new ConcurrentHashMap<>();
        mapUserUsage = new ConcurrentHashMap<>();

        for (int i = 0; i < nbThread; i++) {
            mapUserToAdd.put(i, new ArrayList<>());
            if (isDAP) {
                localUsersFollowProbabilityRange.put(i, 0L);
                localUsersFollowProbability.put(i, new ConcurrentSkipListMap<>());
            }
        }

        executorService = Executors.newFixedThreadPool(nbThread);

        this.alpha = alpha;
    }

    public void generateAndSaveGraph() throws InterruptedException, ExecutionException {
        generateUsers();
        addingPhase();
        followingPhase();
        saveGraph("graph_following_retwis_" + nbUsers + "_users.txt");
    }

    public Key generateUser(){
        return keyGenerator.nextKey();
    }

    public void generateUsers() throws InterruptedException {
        System.out.println("Generate users");
        String fileName = "nodes_info_" + nbUsers + "_users.txt";
        Set<Key> localSetUser = new TreeSet<>();
        int r_degree, o_degree, i_degree;
        List<Integer> powerLawArray = generateValues(nbUsers, nbUsers, 600, SCALEUSAGE);
        long somme = 0;

        try {
            File fichier = new File(fileName);

            FileReader fileReader = new FileReader(fichier);

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            bufferedReader.readLine();

            for (int i = 0; i < nbUsers;) {

                Key user = generateUser();
                if (localSetUser.add(user)) {
                    if (i % nbUsers * 0.05 == 0) {
                        System.out.println(i);
                    }

                    String[] degrees = bufferedReader.readLine().split(" ");

                    r_degree = (int) Double.parseDouble(degrees[0]);
                    i_degree = (int) Double.parseDouble(degrees[1]);
                    o_degree = (int) Double.parseDouble(degrees[2]);
                    mapIndiceToKey.put(i, user);
                    mapKeyToIndice.put(user,i);

                    reciprocalDegree[i] = r_degree;
                    inDegree[i] = i_degree;
                    outDegree[i] = o_degree;

                    somme += powerLawArray.get(i);
                    usersFollowProbability.put(somme, user);

                    if (r_degree>0)
                        reciprocal++;
                    if (i_degree>0)
                        in++;
                    if (o_degree>0)
                        out++;

                    i++;
                }
            }

            usersFollowProbabilityRange = somme;

            bufferedReader.close();
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        edges_r = reciprocal*2;
        edges_d = (in + out)/2;

        int diag_sum_r = 0, diag_sum_d = 0;

        for (int i = 0; i < nbUsers; i++) {
            if (reciprocalDegree[i] != 0)
                diag_sum_r += Math.pow(reciprocalDegree[i],2)/edges_r;

            if (inDegree[i] != 0 && outDegree[i] != 0){
                diag_sum_d += (inDegree[i]*outDegree[i]) / edges_d;
                diag += 1;
            }
        }

        diag_sum_r_dist = diag_sum_r / ((reciprocal * (reciprocal - 1)) / 2);
        diag_sum_d_dist = diag_sum_d/(out*in-diag);

    }

    public static List<Integer> generateValues(int numValues, double desiredMaxValue, double SHAPE, double SCALE) throws InterruptedException {
        List<Double> doubleValues = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        RandomGenerator rand = RandomGeneratorFactory.createRandomGenerator(new Random(94));
        ParetoDistribution distribution = new ParetoDistribution(rand,SCALE,SHAPE);

        double maxGeneratedValue = 0;
        for (int i = 0; i < numValues; i++) {
            double randomValue = distribution.sample();
            doubleValues.add(randomValue);
            if (randomValue > maxGeneratedValue) {
                maxGeneratedValue = randomValue;
            }
        }

        double scaleFactor = desiredMaxValue / maxGeneratedValue;

        for (int i = 0; i < numValues; i++) {
            double scaledValue = doubleValues.get(i) * scaleFactor;
            values.add((int) Math.round(scaledValue)+1);
        }

        return values;
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public void clearLoadingData() {
        usersFollowProbability.clear();
        localUsersUsageProbability.clear();
        localUsersUsageProbabilityRange.clear();
        listLocalUser.clear();
        listLocalUsersFollow.clear();
        mapUserToAdd.clear();
        mapUserToIndiceThread.clear();
        mapListUserFollow.clear();
    }

    @FunctionalInterface
    interface MyCallableWithArgument {
        Void call(Integer argument) throws Exception;
    }

    public void addingPhase() throws ExecutionException, InterruptedException {
        System.out.println("Start add phase");

        List<Future<Void>> futures = new ArrayList<>();

        Map<Integer, AtomicInteger> somme = new ConcurrentHashMap<>();

        for (int i = 0; i < nbThread; i++)
            somme.put(i, new AtomicInteger());

        List<Integer> powerLawArray = generateValues(nbUsers, nbUsers, 600, SCALEUSAGE);

        Collections.sort(powerLawArray);

        MyCallableWithArgument myCallable = (Integer i) -> {

            Key user;

            somme.get(i%nbThread).addAndGet(powerLawArray.get(i));

            user = mapIndiceToKey.get(i);
            if (usageStat)
                mapUserUsage.put(user, 0);
            addOriginalUser(user);
            localUsersUsageProbability.get(i%nbThread).put(somme.get(i%nbThread).longValue(), user);
            localUsersUsageProbabilityRange.compute(i%nbThread,  (k,v) -> Math.max(v,somme.get(i%nbThread).longValue()));
            return null;
        };

        for (int i = 0; i < nbUsers; i++) {
            int finalI = i;
            Callable<Void> callable = () -> myCallable.call(finalI);
            futures.add(executorService.submit(callable));

        }

        for (Future<Void> future :futures){
            future.get();
        }

        System.out.println("End add phase");
    }

    public void followingPhase() throws InterruptedException, ExecutionException {
        System.out.println("Start follow phase");

        long startTime;
        startTime = System.nanoTime();
        List<Future<Void>> futures = new ArrayList<>();
        LongAdder count = new LongAdder();
        MyCallableWithArgument myCallable = (Integer i) -> {

            if (i % nbUsers * 0.05 == 0) {
                System.out.println(i);
            }

            long a, counter = 0, directed_sum = 0;
            double pr;
            Key userA, userB;

            // Sampling of reciprocal edges
            for (int j = i; j < nbUsers; j++) {
                if (reciprocalDegree[j] != 0 && reciprocalDegree[i] != 0){
                    pr = (double) 2*reciprocalDegree[i]*reciprocalDegree[j]/edges_r + diag_sum_r_dist;

                    if (pr>1)
                        pr = 1;

                    a = random.get().nextDouble() < pr ? 1 : 0;

                    if (a==1){

                        userA = mapIndiceToKey.get(i);
                        userB = mapIndiceToKey.get(j);

                        followUser(userA,userB);
                        followUser(userB,userA);

                        if (inDegree[i] != 0 && outDegree[j] != 0){
                            counter++;
                            directed_sum += (long) ((double) inDegree[i]*outDegree[j]/edges_d +diag_sum_d_dist);
                        }

                        if (inDegree[j] != 0 && outDegree[i] != 0){
                            counter++;
                            directed_sum += (long) ((double) inDegree[j]*outDegree[i]/edges_d +diag_sum_d_dist);
                        }
                    }
                }
            }

            long sampled_reciprocal = directed_sum/(out*in-diag-counter);

            // Sampling of directed edges
            for (int j = i; j < nbUsers; j++) {

                if (inDegree[i] != 0 && outDegree[j] != 0){
                    pr = (double) inDegree[i]*outDegree[j]/edges_d + diag_sum_d_dist + sampled_reciprocal;

                    if (pr>1)
                        pr = 1;

                    a = random.get().nextDouble() < pr ? 1 : 0;

                    if (a==1){
                        userA = mapIndiceToKey.get(i);
                        userB = mapIndiceToKey.get(j);

                        followUser(userB, userA);
                    }
                }

                if (inDegree[j] != 0 && outDegree[i] != 0){
                    pr = (double) inDegree[j]*outDegree[i]/edges_d + diag_sum_d_dist + sampled_reciprocal;

                    if (pr>1)
                        pr = 1;

                    a = random.get().nextDouble() < pr ? 1 : 0;

                    if (a==1){
                        userA = mapIndiceToKey.get(i);
                        userB = mapIndiceToKey.get(j);

                        followUser(userA, userB);
                    }
                }
            }

            count.increment();
            if (count.longValue()%1000==0) System.out.print(".");

            return null;
        };

        for (int i = 0; i < nbUsers; i++) {
            int finalI = i;
            Callable<Void> callable = () -> myCallable.call(finalI);
            futures.add(executorService.submit(callable));
        }

        for (Future<Void> future :futures){
            future.get();
        }

        System.out.println("time in ns : " + (System.nanoTime() - startTime));
        System.out.println("End follow phase");
    }

    private void saveGraph(String fileName){
        System.out.println("Start saving graph");
        StringBuilder builder = new StringBuilder();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Key userA : mapIndiceToKey.values()){
                builder.append(mapKeyToIndice.get(userA).toString());

                for (Key userB: mapFollowing.get(userA)){
                    builder.append(" " + mapKeyToIndice.get(userB));
                }

                builder.append("\n");
            }

            writer.write(builder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("End saving graph");
    }

    public void loadGraph() throws InterruptedException, ClassNotFoundException, IOException, ExecutionException {
        System.out.println("Start loading graph ("+alpha+")");
        int numberOfUsersInFile;
        String fileName = "graph_following_retwis_" + nbUsers + "_users.txt";
        try (Stream<String> fileStream = Files.lines(Paths.get(fileName))) {
            //Lines count
            numberOfUsersInFile = (int) fileStream.count();
        }

        Set<Key> localSetUser = new HashSet<>();
        List<Integer> powerLawArray = generateValues(numberOfUsersInFile, numberOfUsersInFile, alpha, SCALEUSAGE);
        Map<Key, Queue<Key>> tmpListUsersFollow = new HashMap<>();

        int val, indiceThread, nbUserPerThread = (int) Math.ceil((double)numberOfUsersInFile/(double)nbThread);


        for (int i = 0; i < numberOfUsersInFile;) {
            Key user = generateUser();
            if (localSetUser.add(user)) {

                if(isDAP)
                    indiceThread = i/nbUserPerThread;
                else
                    indiceThread = Math.abs(user.hashCode()%nbThread);

                mapUserToAdd.get(indiceThread).add(user);
                mapUserToIndiceThread.put(user, indiceThread);
                mapListUserFollow.put(user, new LinkedList<>());

                if (usageStat)
                    mapUserUsage.put(user, 0);

                mapIndiceToKey.put(i, user);
                mapKeyToIndice.put(user, i);

                tmpListUsersFollow.put(user, new LinkedList<>());
                i++;
            }
        }

        Queue<String[]> lines = new ConcurrentLinkedQueue<>();

        try {
            File fichier = new File(fileName);

            FileReader fileReader = new FileReader(fichier);

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();

            while (line != null){
                lines.add(line.split(" "));
                line = bufferedReader.readLine();
            }

            bufferedReader.close();
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Future<Void>> futures = new ArrayList<>();

        Callable<Void> myCallable = () -> {

            String[] values = lines.poll();

            assert values != null;
            int userIndice = Integer.parseInt(values[0]);

           for (int j = 1; j < values.length; j++) {
                try{
                    if (isDAP) {
                        int userToFollowIndice, rangeStart, userRangeLevel = userIndice/ nbUserPerThread;
                        rangeStart = userRangeLevel * nbUserPerThread;
                        userToFollowIndice = (userIndice + j) % nbUserPerThread + rangeStart;
                        assert userToFollowIndice/nbUserPerThread == userIndice/nbUserPerThread;
                        mapListUserFollow.get(mapIndiceToKey.get(userIndice)).add(mapIndiceToKey.get(userToFollowIndice));
                    }
                    else
                        mapListUserFollow.get(mapIndiceToKey.get(userIndice)).add(mapIndiceToKey.get(Integer.parseInt(values[j])));
                } catch (NullPointerException e) {
                    System.out.println("key from " + userIndice + " is suposed to be null : " + mapIndiceToKey.get(userIndice));
                }
            }

            return null;
        };

        for (int i = 0; i < numberOfUsersInFile; i++) {
            futures.add(executorService.submit(myCallable));
        }

        for (Future<Void> future :futures){
            future.get();
        }

        // We then sort the users according to the number of links, so that the users with the most links are the most active.

        int nbLink;
        Map<Key, Integer> mapNbLinkPerUser = new HashMap<>();
        Map<Integer, AtomicInteger> sommeUsage = new HashMap<>();
        long sommeFollow = 0L;
        Map<Integer, Long> threadSommeFollow = new HashMap<>();
        if (isDAP)
            for (int i = 0; i < nbThread; i++)
                threadSommeFollow.put(i, 0L);

        for (int i = 0; i < numberOfUsersInFile; i++) {
            nbLink = 0;

            Key user = mapIndiceToKey.get(i);
            nbLink += mapListUserFollow.get(user).size();

            mapNbLinkPerUser.put(user, nbLink);
        }

        mapNbLinkPerUser = sortMapByValue(mapNbLinkPerUser);
        Collections.sort(powerLawArray);
        Collections.reverse(powerLawArray);

        for (int i = 0; i < nbThread; i++) {
            sommeUsage.put(i, new AtomicInteger());
        }

        int j = 0;

        for (Key user: mapNbLinkPerUser.keySet()){
            val = powerLawArray.get(j);
            indiceThread = mapUserToIndiceThread.get(user);
            sommeUsage.get(indiceThread).addAndGet(val);

            if (isDAP)
                threadSommeFollow.put(indiceThread, threadSommeFollow.get(indiceThread) + val);
            else
                sommeFollow += val;

            localUsersUsageProbability.get(indiceThread).put(sommeUsage.get(indiceThread).longValue(), user);
            localUsersUsageProbabilityRange.put(indiceThread, sommeUsage.get(indiceThread).longValue());

            if (isDAP)
                localUsersFollowProbability.get(indiceThread).put(threadSommeFollow.get(indiceThread), user);
            else
                usersFollowProbability.put(sommeFollow, user);
            listLocalUser.get(indiceThread).add(user);
            listLocalUsersFollow.get(indiceThread).put(user, tmpListUsersFollow.get(user));

            j++;
        }

        if (isDAP)
            for (int i = 0; i < nbThread; i++)
                localUsersFollowProbabilityRange.put(i, threadSommeFollow.get(i));
        else
            usersFollowProbabilityRange = sommeFollow;
        System.out.println("End loading graph");
    }

    public static Map<Key, Integer> sortMapByValue(Map<Key, Integer> inputMap) {
        // Convert the inputMap to a List of Map.Entry objects
        List<Map.Entry<Key, Integer>> entryList = new ArrayList<>(inputMap.entrySet());

        // Sort the entryList using a custom comparator based on values
        Collections.sort(entryList, Comparator.comparing(Map.Entry::getValue));
        Collections.reverse(entryList);
        // Create a new LinkedHashMap to store the sorted entries
        Map<Key, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Key, Integer> entry : entryList) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public void addOriginalUser(Key user) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        mapFollowers.put(user, new ConcurrentHashSet<>());
        mapFollowing.put(user, new HashSet<>());
        mapTimelines.put(user, new Timeline(factory.newQueue()));
        mapProfiles.put(user, 0);
        mapCommunityStatus.put(user, 0);
    }

    public void addUser(Key user, Set<Key> dummySet, Timeline<String> dummyTimeline) {
        mapFollowers.put(user, dummySet);
        mapFollowing.put(user, dummySet);
        mapTimelines.put(user, dummyTimeline);
        mapProfiles.put(user, 0);
        mapCommunityStatus.put(user, 0);
    }

    public void removeUser(Key user){
        mapFollowers.remove(user);
        mapFollowing.remove(user);
        mapTimelines.remove(user);
        mapProfiles.remove(user);
        mapCommunityStatus.remove(user);
    }

    // Adding user_A to the followers of user_B
    // and user_B to the following of user_A
    // user_A  is following user_B
    public void followUser(Key userA, Key userB) throws InterruptedException {
        mapFollowers.get(userB).add(userA);
        mapFollowing.get(userA).add(userB);
    }

    // Removing user_A to the followers of user_B
    // and user_B to the following of user_A
    public void unfollowUser(Key userA, Key userB){
        mapFollowers.get(userB).remove(userA);
        mapFollowing.get(userA).remove(userB);
    }

    public void tweet(Key user, String msg) {
        Set<Key> set = mapFollowers.get(user);
        int i = 0;
        for (Key follower : set) {
            Timeline<String> timeline = mapTimelines.get(follower);
            timeline.add(msg);
            i++;
            if (i>1) break;
        }
    }

    public void showTimeline(Key user)  {
        mapTimelines.get(user).read();
    }

    public void updateProfile(Key user){
        mapProfiles.compute(user,
                (_,p) ->
                {
                    return p++;
                });
    }

    public void joinCommunity(Key user){
        community.add(user);
    }

    public void leaveCommunity(Key user){
        community.remove(user);
    }

    public String statistics() {
        int in=0, out=0, max_in=0, max_out=0, wall=0, max_wall=0, nbOpTot = 0, nbOp = 0;

        if (usageStat) {
            mapUserUsage = sortMapByValue(mapUserUsage);
            int n, nbUserActif = 0, i = 0;
            for (Key user : mapUserUsage.keySet()) {
                if(mapUserUsage.get(user)>0)
                    nbUserActif++;
            }
            for (Key user : mapUserUsage.keySet()){
                n = mapUserUsage.get(user);
                if (i <= nbUserActif*0.2)
                    nbOp += n;

                nbOpTot += n;
                if (n>0)
                    i++;
            }
        }
        for (Key user : mapFollowers.keySet()){
            int degree = mapFollowers.get(user).size();
            in+=degree;
            if (degree>max_in) max_in = degree;
        }
        in=in/mapFollowers.size();

        for (Key user : mapFollowing.keySet()){
            int degree = mapFollowing.get(user).size();
            out+=degree;
            if (degree>max_out) max_out = degree;
        }
        out=out/mapFollowing.size();

        for(Timeline timeline: mapTimelines.values()){
            int w = timeline.size();
            wall += w;
            if (w>max_wall) max_wall = w;
        }
        wall=wall/mapTimelines.size();

        StringBuilder builder = new StringBuilder();
        builder.append("#users:"+mapFollowers.size()
                +", avg in degree:" + in
                + ", max in degree:" + max_in
                + ", avg out degree:" + out
                +", max out degree:" + max_out
                + ", avg timeline:" + wall
                +", max timeline:" + max_wall
        );

        if (usageStat)
            builder.append(",% op from 20% more actif user : " + (nbOp/(double)nbOpTot)*100);
        if (mapFollowers instanceof ConcurrentHashMap) {
            ConcurrentHashMap map = (ConcurrentHashMap) mapFollowers;
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Followers:=\n");
        for(Key user: mapFollowers.keySet()) {
            builder.append(user+"->"+mapFollowers.get(user)+"\n");
        }
        builder.append("Pr[user]:=");
        builder.append(localUsersUsageProbability);
        builder.append("\n");
        builder.append("Range:=");
        builder.append(localUsersUsageProbabilityRange);
        return builder.toString();
    }
}
