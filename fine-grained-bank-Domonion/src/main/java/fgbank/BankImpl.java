package fgbank;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Bank implementation.
 *
 * <p>:TODO: This implementation has to be made thread-safe.
 */
public class BankImpl implements Bank {
    /**
     * An array of accounts by index.
     */
    private final Account[] accounts;

    /**
     * Creates new bank instance.
     *
     * @param n the number of accounts (numbered from 0 to n-1).
     */
    public BankImpl(int n) {
        accounts = new Account[n];
        for (int i = 0; i < n; i++) {
            accounts[i] = new Account();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfAccounts() {
        return accounts.length;
    }

    /**
     * {@inheritDoc}
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public long getAmount(int index) {
        long res;
        accounts[index].lock.lock();
        res = accounts[index].amount;
        accounts[index].lock.unlock();
        return res;
    }

    /**
     * {@inheritDoc}
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public long getTotalAmount() {
        long sum = 0;
        for (Account account : accounts) {
            account.lock.lock();
        }
        for (Account account : accounts) {
            sum += account.amount;
        }
        for (Account account : accounts) {
            account.lock.unlock();
        }
        return sum;
    }

    /**
     * {@inheritDoc}
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public long deposit(int index, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);
        Account account = accounts[index];
        account.lock.lock();
        if (amount > MAX_AMOUNT || account.amount + amount > MAX_AMOUNT) {
            account.lock.unlock();
            throw new IllegalStateException("Overflow");
        }
        account.amount += amount;
        long res = account.amount;
        account.lock.unlock();
        return res;
    }

    /**
     * {@inheritDoc}
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public long withdraw(int index, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);
        Account account = accounts[index];
        account.lock.lock();
        if (account.amount - amount < 0) {
            account.lock.unlock();
            throw new IllegalStateException("Underflow");
        }
        account.amount -= amount;
        long res = account.amount;
        account.lock.unlock();
        return res;
    }

    /**
     * {@inheritDoc}
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public void transfer(int fromIndex, int toIndex, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);
        if (fromIndex == toIndex)
            throw new IllegalArgumentException("fromIndex == toIndex");
        Account from = accounts[fromIndex];
        Account to = accounts[toIndex];
        if (fromIndex < toIndex) {
            from.lock.lock();
            to.lock.lock();
        } else {
            to.lock.lock();
            from.lock.lock();
        }
        if (amount > from.amount) {
            if (fromIndex < toIndex) {
                from.lock.unlock();
                to.lock.unlock();
            } else {
                to.lock.unlock();
                from.lock.unlock();
            }
            throw new IllegalStateException("Underflow");
        } else if (amount > MAX_AMOUNT || to.amount + amount > MAX_AMOUNT) {
            if (fromIndex < toIndex) {
                from.lock.unlock();
                to.lock.unlock();
            } else {
                to.lock.unlock();
                from.lock.unlock();
            }
            throw new IllegalStateException("Overflow");
        }
        from.amount -= amount;
        to.amount += amount;
        if (fromIndex < toIndex) {
            from.lock.unlock();
            to.lock.unlock();
        } else {
            to.lock.unlock();
            from.lock.unlock();
        }
    }

    /**
     * Private account data structure.
     */
    private static class Account {
        /**
         * Amount of funds in this account.
         */
        long amount;
        private final Lock lock = new ReentrantLock();
    }
}
