import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Client {
    private static final int SUCCESS_CODE = 200;
    private static final String BASE_URL = "http://localhost:8080";

    private int statusCode;
    private CloseableHttpClient httpclient;
    private Logger log;

    public Client() {
        httpclient = HttpClients.createDefault();
        log = LogManager.getLogger(Client.class.getName());
    }

    /**
     * @return status code of last request
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     *  Closes connection
     *  Has to be called after completing all operations with Client
     */
    public void finish() {
        try {
            httpclient.close();
        } catch (IOException e) {
            log.log(Level.ERROR, e.getMessage(), e);
        }
    }

    /**
     * Is equal to request: GET <base-url>/oldStorage/files
     * @return String[] with list of files in old storage or an empty String[] if no files exist
     */
    public String[] getOldList() {
        return getList("/oldStorage/files");
    }

    /**
     * Is equal to request: GET <base-url>/newStorage/files
     * @return String[] with list of files in new storage or an empty String[] if no files exist
     */
    public String[] getNewList() {
        return getList("/newStorage/files");
    }

    /**
     * Is equal to request: GET <base-url>/oldStorage/files/{filename}
     * @return String name of specified file from old storage
     */
    public String getOldByName(String file) {
        return getByName(file, "/oldStorage/files/");
    }

    /**
     * Is equal to request: GET <base-url>/newStorage/files/{filename}
     * @return String name of specified file from new storage
     */
    public String getNewByName(String file) {
        return getByName(file, "/newStorage/files/");
    }

    /**
     * Is equal to request: DELETE <base-url>/oldStorage/files/{filename}
     */
    public void deleteFromOldStorage(String file) {
        delete(file, "/oldStorage/files/");
    }

    /**
     * Is equal to request: DELETE <base-url>/newStorage/files/{filename}
     */
    public void deleteFromNewStorage(String file) {
        delete(file, "/newStorage/files/");
    }

    /**
     * Is equal to request: POST <base-url>/newStorage/files multipart/form-data
     */
    public void loadByName(String file) throws IOException {
        HttpPost httpPost = new HttpPost(BASE_URL + "/newStorage/files");

        String content = getOldByName(file);

        File temp = File.createTempFile("temp", ".txt");
        FileWriter fileWriter = new FileWriter(temp);
        fileWriter.write(content);
        fileWriter.close();

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("file", temp, ContentType.MULTIPART_FORM_DATA, file);
        httpPost.setEntity(builder.build());

        do {
            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                statusCode = response.getStatusLine().getStatusCode();
            }
        } while (statusCode != SUCCESS_CODE);
    }

    /**
     * Transfers files from old storage to new storage
     * @param files String[] with names of files from old storage to be transferred
     */
    public void transfer(String[] files) {
        int counter = 0;
        for (String file : files) {
            try {
                loadByName(file);
                deleteFromOldStorage(file);
                System.out.println(++counter + " files out of " + files.length + " transferred...");
            } catch (IOException e) {
                log.log(Level.ERROR, e.getMessage(), e);
            }
        }
    }

    /**
     * Checks if transfer was completed successfully
     * @param oldList String[] with names of files from old storage
     * @param newList String[] with names of files from new storage
     */
    public boolean transferredSuccessfully(String[] oldList, String[] newList) {
        return oldList.length == newList.length;
    }

    private String[] getList(String command) {
        String[] list = null;
        do {
            try (CloseableHttpResponse response = httpclient.execute(new HttpGet(BASE_URL + command))) {
                statusCode = response.getStatusLine().getStatusCode();
                list = createReceivedFilesList(EntityUtils.toString(response.getEntity()));
            } catch (IOException e) {
                log.log(Level.ERROR, e.getMessage(), e);
            }
        } while (statusCode != SUCCESS_CODE);
        return list;
    }

    private String getByName(String file, String command) {
        String content = null;
        String request = BASE_URL + command + file;
        do {
            try (CloseableHttpResponse response = httpclient.execute(new HttpGet(request))) {
                statusCode = response.getStatusLine().getStatusCode();
                content = EntityUtils.toString(response.getEntity());
            } catch (IOException e) {
                log.log(Level.ERROR, e.getMessage(), e);
            }
        } while (statusCode != SUCCESS_CODE);
        return content;
    }

    private void delete(String file, String command) {
        String request = BASE_URL + command + file;
        do {
            try (CloseableHttpResponse response = httpclient.execute(new HttpDelete(request))) {
                statusCode = response.getStatusLine().getStatusCode();
            } catch (IOException e) {
                log.log(Level.ERROR, e.getMessage(), e);
            }
        } while (statusCode != SUCCESS_CODE);
    }

    private String[] createReceivedFilesList(String files) {
        if (files.length() <= 2) {
            return new String[0];
        } else {
            String[] receivedFiles = files.substring(1, files.length() - 1).split(",");
            for (int i = 0; i < receivedFiles.length; i++) {
                receivedFiles[i] = receivedFiles[i].substring(1, receivedFiles[i].length() - 1);
            }
            return receivedFiles;
        }
    }
}