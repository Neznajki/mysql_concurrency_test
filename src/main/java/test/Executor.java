package test;

import concurrency.UpdateRecordTask;
import config.ConfigGetter;
import mariadb.ConnectionStorage;
import mariadb.DbHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Executor {
    public static void main(String[] args) {
        var executor = new Executor();

        executor.execute();
    }

    private Executor()
    {

    }

    private void execute()
    {
        try {
            long startTime = System.nanoTime();
            var configGetter = new ConfigGetter();
            System.out.println(configGetter.getString("dbhost"));

            List<Future<Integer>> futures = new ArrayList<>();
            List<Integer> handledRecords = new ArrayList<>();
            ExecutorService executorService = Executors.newFixedThreadPool(configGetter.getInteger("concurrecncy"));
            var connectionStorage = new ConnectionStorage(configGetter);
            DbHandler dbHandler = new DbHandler(connectionStorage, configGetter);
            dbHandler.prepareDb();

            for (int i = 0; i< configGetter.getInteger("executions"); i++) {
                futures.add(executorService.submit(new UpdateRecordTask(dbHandler)));
            }

            for (Future<Integer> future: futures) {
                if (handledRecords.indexOf(future.get()) == -1) {
                    handledRecords.add(future.get());
                }
            }

            executorService.shutdown();
            long endTime = System.nanoTime();
            System.out.println(String.format("handled %d records in %d seconds", handledRecords.size(), (endTime - startTime) / 1000000000));
        } catch (Exception e) {
            e.printStackTrace();
        }
//        for (List<LoanEntity> loanList : splitData.getThreadDataCollection()) {
//            futures.add(executorService.submit(new HistoryGatherTask(loanList)));
//        }
//
//        for (Future<List<LoanEntity>> future : futures) {
//            future.get();
//        }
//
//        executorService.shutdown();
//        System.out.println("done required data gathering");
    }
}
