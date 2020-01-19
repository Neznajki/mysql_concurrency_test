package concurrency;

import mariadb.DbHandler;

import java.util.concurrent.Callable;

public class UpdateRecordTask implements Callable<Integer> {
    private DbHandler dbHandler;

    public UpdateRecordTask(DbHandler dbHandler)
    {
        this.dbHandler = dbHandler;
    }

    @Override
    public Integer call() {
        try {
            return this.dbHandler.getLockingItemId();
        } catch (RuntimeException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
