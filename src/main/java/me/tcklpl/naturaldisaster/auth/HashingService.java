package me.tcklpl.naturaldisaster.auth;

public class HashingService implements Runnable {

    private final HashingManager manager;
    HashingManager.HashData hashData;
    boolean success;

    public HashingService(HashingManager manager, HashingManager.HashData hashData) {
        this.manager = manager;
        this.hashData = hashData;
    }

    @Override
    public void run() {
        char[] password = hashData.getPassword().toCharArray();
        if (hashData.getOption() == HashingManager.HashingOption.HASH) {
            // iterações, memória, npiveis de paralelismo
            hashData.setHash(manager.getArgon2().hash(4, 1024 * 512, 8, password));
        } else {
            success = manager.getArgon2().verify(hashData.getHash(), password);
        }
        manager.acceptTask(this);
    }
}
