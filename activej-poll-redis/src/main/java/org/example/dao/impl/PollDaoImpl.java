package org.example.dao.impl;

import static io.activej.serializer.CompatibilityLevel.LEVEL_4_LE;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.example.dao.PollDao;
import org.example.pojo.Poll;

import io.activej.codegen.DefiningClassLoader;
import io.activej.eventloop.Eventloop;
import io.activej.promise.Promise;
import io.activej.redis.RedisClient;
import io.activej.redis.RedisRequest;
import io.activej.redis.RedisResponse;
import io.activej.serializer.BinarySerializer;
import io.activej.serializer.SerializerBuilder;

//[START EXAMPLE]
public final class PollDaoImpl implements PollDao {
    public static final String POLL_KEY_PREFIX = "poll.";
    public static final String ALL_POLL_SETS = "all.poll.sets";

    private final Random random = new Random();
    private final RedisClient redisClient;

    private static final DefiningClassLoader definingClassLoader = DefiningClassLoader.create();
    private static final BinarySerializer<Poll> serializer = SerializerBuilder.create(definingClassLoader)
            .withCompatibilityLevel(LEVEL_4_LE)
            .build(Poll.class); // Constructor not found:org.example.pojo.Poll() 需要无参构造器！

    public PollDaoImpl(Eventloop eventloop) {
        System.out.println("###INIT Eventloop: >>>= "+ eventloop);
        this.redisClient = RedisClient.create(eventloop);
    }

    @Override
    public Promise<Poll> find(final int id) {
        Promise<byte[]> result = redisClient.connect().then(connection -> {
            return connection.cmd(RedisRequest.of("GET", getKey(id)), RedisResponse.BYTES)
                    .whenException(Exception::printStackTrace)
                    .whenResult(bytes -> Promise.of(bytes));
        });

        //return result.map(bytes -> serializer.decode(bytes, 0)) // NPE
        return result.mapIfNonNull(bytes -> serializer.decode(bytes, 0))
                .whenResult(poll -> System.out.println("###DECODE >>>= "+ poll));
    }

    @Override
    public Promise<Map<Integer, Poll>> findAll() {
        Promise<List<Integer>> keys = redisClient.connect().then(connection -> {
            // 参数只能是 String, byte[] @see RedisRequest#writeArgument()
            return connection.cmd(RedisRequest.of("ZRANGE", ALL_POLL_SETS, "0", "-1"), RedisResponse.ARRAY)
                    .map(array -> Arrays.stream(array)
                            .map(bytes -> new String((byte[]) bytes))
                            .map(str -> Integer.parseInt(str))
                            .collect(Collectors.toList()))
                    .whenException(Exception::printStackTrace);
        });
        //return Promise.of(Collections.emptyMap());

        return keys.then(keyList -> {
            List<String> strList = keyList.stream()
                    .map(id -> getKey(id)) // add POLL_KEY_PREFIX
                    .collect(Collectors.toList());

            Promise<List<Poll>> polls = redisClient.connect().then(connection -> {
                strList.add(0, "MGET"); // CMD
                System.out.println("List: " + strList);
                return connection.cmd(RedisRequest.of(/* "MGET", */strList.toArray()), RedisResponse.ARRAY)
                        .map(array -> Arrays.stream(array)
                                .map(bytes -> serializer.decode((byte[]) bytes, 0))
                                .collect(Collectors.toList()))
                        .whenException(Exception::printStackTrace);
            });

            // 两个列表List 配对成一个Map
            return polls.mapIfNonNull(pollList -> {
                Map<Integer, Poll> map = IntStream.range(0, keyList.size())
                        .boxed()
                        .collect(Collectors.toMap(keyList::get, pollList::get));
                System.out.println("Map: " + map);
                return map;
            });
        });
    }

    @Override
    public Promise<Integer> add(Poll poll) {
        final int range = Integer.MAX_VALUE;
        final int id = random.nextInt(range);

        // add poll.id to redis sorted set
        Promise<Long> addResult = redisClient.connect().then(connection -> {
            Float score = Float.valueOf(System.currentTimeMillis()/1000);
            // java.lang.IllegalArgumentException: null
            // io.activej.redis.RedisRequest.writeArgument(RedisRequest.java:152)
            // 请求参数只能是String, byte[]两种类型
            return connection.cmd(RedisRequest.of("ZADD", ALL_POLL_SETS, String.valueOf(score), String.valueOf(id)), RedisResponse.LONG)
                    .whenResult(val -> System.out.println("###ZADD >>>= " + val))
                    .whenException(Exception::printStackTrace);
        });

        // store poll to redis with poll.id key
        Promise<String> setResult = redisClient.connect().then(connection -> {
            final byte[] array = new byte[1024];
            int len = serializer.encode(array, 0, poll);
            return connection.cmd(RedisRequest.of("SET", getKey(id), Arrays.copyOf(array, len)), RedisResponse.STRING)
                    .whenResult(val -> System.out.println("###SET >>>= " + val))
                    .whenException(Exception::printStackTrace);
        });
        //return setResult.mapIfNonNull(val -> id);

        return addResult.both(setResult)
                .whenException(e -> System.err.println(e.getMessage()))
                .map($ -> id)
                .whenResult(val -> System.out.println("###RETURN >>>= " + val));
    }

    @Override
    public Promise<Void> update(int id, Poll poll) {
        Promise<Void> result = redisClient.connect().then(connection -> {
            final byte[] array = new byte[1024];
            int len = serializer.encode(array, 0, poll);
            return connection.cmd(RedisRequest.of("SET", getKey(id), Arrays.copyOf(array, len)), RedisResponse.OK)
                    .whenResult(nil -> System.out.println("###UPDATE >>>= " + id))
                    .whenException(Exception::printStackTrace);
        });
        return result;
    }

    @Override
    public Promise<Void> remove(int id) {
        Promise<Void> result = redisClient.connect().then(connection -> {
            System.out.println("Transaction begins");
            return connection.multi()
                    //Caused by: io.activej.common.exception.MalformedDataException: Unknown RESP data type: 13
                    //at io.activej.redis.RESPv2.skipObject(RESPv2.java:187)
                    .then(() -> {
                        // 单个命令可以执行，事务含多个命令不能全都是 RedisResponse.SKIP
                        connection.cmd(RedisRequest.of("DEL", getKey(id)), RedisResponse.LONG)
                            .whenResult(nil -> System.out.println("###DEL >>>= " + id));
                        return Promise.complete(); // 必须完成，否则挂起不执行!
                    }).then(() -> {
                        // 单个命令可以执行，事务含多个命令不能全都是 RedisResponse.SKIP
                        connection.cmd(RedisRequest.of("ZREM", ALL_POLL_SETS, String.valueOf(id)), RedisResponse.LONG)
                            .whenResult(nil -> System.out.println("###ZREM >>>= " + id));
                        return Promise.complete();
                    }).then(() -> {
                        System.out.println("Committing transaction");
                        return connection.exec();
                    })
                    .then(transactionResult -> {
                        System.out.println("###TRANSX: " + Arrays.toString(transactionResult));
                        return Promise.complete(); // == null
                    })
                    .then(connection::quit);
            })
            .whenException(Exception::printStackTrace);

        return result;
    }

    public String getKey(int id) {
        return POLL_KEY_PREFIX + id;
    }

    public String getKey(String id) {
        return POLL_KEY_PREFIX + id;
    }
}
//[END EXAMPLE]