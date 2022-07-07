package org.example.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.example.dao.PollDao;
import org.example.pojo.Poll;
import org.example.pojo.PollVote;
import org.example.repository.PollRepository;
import org.example.repository.PollVoteRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.activej.common.Utils;
import io.activej.promise.Promise;

public class PollService implements PollDao {
    //@Autowired
    private final PollRepository pollRepo;
    private final PollVoteRepository pollVoteRepo;
    private final Executor executor;

    public PollService(PollRepository pollRepo, PollVoteRepository pollVoteRepo, Executor executor) {
        this.pollRepo = pollRepo;
        this.pollVoteRepo = pollVoteRepo;
        this.executor = executor;
    }

    @Override
    public @NotNull String now() {
        return pollRepo.now();
    }

    @Override
    public Promise<@NotNull String> asyncNow() {
        return Promise.ofBlocking(executor, () -> {
            return pollRepo.now();
        });
    }

    @Override
    public Promise<@Nullable Poll> find(int id) {
        return Promise.ofBlocking(executor, () -> {
            Poll poll = pollRepo.findById(id);

            //List<PollVote> pollVoteList = pollVoteRepo.findAllById(Arrays.asList(new PollVoteId(id, null)));
            List<PollVote> pollVoteList = pollVoteRepo.findAllByPollId(id);
            if (pollVoteList != null && !pollVoteList.isEmpty()) {
                pollVoteList.forEach(item -> {
                    poll.getOptionsToVote().put(
                            item.getOption(),
                            item.getVotesCount());
                });
            }
            return poll;
        });
    }

    @Override
    public Promise<Map<Integer, Poll>> findAll() {
        return Promise.ofBlocking(executor, () -> {
            Map<Integer, Poll> result = new LinkedHashMap<>();

            List<Poll> pollList = pollRepo.findAll();
            pollList.forEach(item -> {
                Poll poll = new Poll(item.getTitle(), item.getMessage(), Utils.listOf());
                poll.setId(item.getId());
                result.put(item.getId(), poll);
            });

            for (Map.Entry<Integer, Poll> entry: result.entrySet()) {
                // Provided id of the wrong type for class org.example.pojo.PollVote.
                // Expected: class org.example.pojo.PollVoteId, got class java.lang.Integer
                //List<PollVote> pollVoteList = pollVoteRepo.findAllById(Arrays.asList(new PollVoteId(poll.getKey(), null)));
                List<PollVote> pollVoteList = pollVoteRepo.findAllByPollId(entry.getKey());

                if (pollVoteList != null && !pollVoteList.isEmpty()) {
                    pollVoteList.forEach(item -> {
                        entry.getValue().getOptionsToVote().put(
                                item.getOption(),
                                item.getVotesCount());
                    });
                }
            }

            return result;
        });
    }

    @Override
    public Promise<Integer> add(Poll poll) {
        return Promise.ofBlocking(executor, () -> {
            Poll savedPoll = pollRepo.save(poll);

            // org.hibernate.id.IdentifierGeneratorHelper - Natively generated identity: 11
            int pollId = savedPoll.getId(); // auto increment poll.id
            if (pollId > 0) {
                for (Map.Entry<String, Integer> entry : poll.getOptions()) {
                    PollVote pollVote = new PollVote();
                    pollVote.setPollId(pollId)
                        .setOption(entry.getKey())
                        .setVotesCount(entry.getValue());
                    pollVoteRepo.save(pollVote);
                }
            }

            return pollId;
        });
    }

    @Override
    public Promise<Boolean> update(int id, Poll poll) {
        return Promise.ofBlocking(executor, () -> {
            pollRepo.save(poll);

            for (Map.Entry<String, Integer> entry : poll.getOptions()) {
                PollVote pollVote = new PollVote();
                pollVote.setPollId(id)
                    .setOption(entry.getKey())
                    .setVotesCount(entry.getValue());
                pollVoteRepo.save(pollVote);
            }
            return true;
        });
    }


    @Override
    public Promise<Boolean> remove(int id) {
        return Promise.ofBlocking(executor, () -> {
            // Creating new transaction with name [org.springframework.data.jpa.repository.support.SimpleJpaRepository.deleteById]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
            pollRepo.deleteById(id); // Transaction 1
            // org.springframework.dao.InvalidDataAccessApiUsageException: No EntityManager with actual transaction available for current thread - cannot reliably process 'remove' call;
            // nested exception is javax.persistence.TransactionRequiredException: No EntityManager with actual transaction available for current thread - cannot reliably process 'remove' call
            // @see http://docs.spring.io/spring/docs/current/spring-framework-reference/html/orm.html
            // 报错原因：更新或删除没有加事务。解决方式：
            //1、在Service层加@Transactional
            //2、在Repository层加@Modifying
            // Creating new transaction with name [org.springframework.data.jpa.repository.support.SimpleJpaRepository.deleteAllByPollId]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
            pollVoteRepo.deleteAllByPollId(id); // Transaction 2
            return true;
        });
    }

}
