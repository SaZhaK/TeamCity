import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {
    Client client = new Client();

    @Test
    void getOldListTest() {
        client.getOldList();

        assertEquals(200, client.getStatusCode());
    }

    @Test
    void getNewListTest() {
        client.getNewList();

        assertEquals(200, client.getStatusCode());
    }

    // Run only if old storage contains files
    @Test
    void getOldByNameTest() {
        String[] files = client.getOldList();
        client.getOldByName(files[0]);

        assertEquals(200, client.getStatusCode());
    }

    // Run only if new storage contains files
    @Test
    void getNewByNameTest() {
        String[] files = client.getNewList();
        client.getNewByName(files[0]);

        assertEquals(200, client.getStatusCode());
    }

    // Run only if old storage contains files
    @Test
    void deleteFromOldStorageTest() {
        String[] files = client.getOldList();
        client.deleteFromOldStorage(files[0]);

        assertEquals(200, client.getStatusCode());
    }

    // Run only if new storage contains files
    @Test
    void deleteFromNewStorageTest() {
        String[] files = client.getNewList();
        client.deleteFromNewStorage(files[0]);

        assertEquals(200, client.getStatusCode());
    }

    // Run only if old storage contains files
    @Test
    void loadByNameTest() {
        String[] files = client.getOldList();
        try {
            client.loadByName(files[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(200, client.getStatusCode());
    }

    // Run only if no functions were used before
    // Old storage has to contain 5000 files and new storage has to contain 0 files
    // Executes the whole operation of transferring and therefore replicates Manager class
    @Test
    void transferTest() {
        String[] files = client.getOldList();
        client.transfer(files);
        assertEquals(0, client.getOldList().length);
        assertEquals(5000, client.getNewList().length);
    }
}