public class Manager {
    public static void main(String[] args) {
        System.out.println("Application started\n");
        System.out.println("Establishing connection...");
        Client client = new Client();
        System.out.println("Connection established successfully\n");

        //Getting list of files from old storage
        System.out.println("Getting list of files to transfer...\n");
        String[] oldList = client.getOldList();
        if (oldList.length == 0) {
            System.out.println("Impossible to complete operation: no files in old storage");
            client.finish();
            return;
        }

        //Transferring files
        System.out.println("Transfer process stared...\n");
        client.transfer(oldList);

        //Getting list of files from new storage
        String[] newList = client.getNewList();

        //Checking if operation was completed successfully
        System.out.println("Checking for errors...");
        if (!client.transferredSuccessfully(oldList, newList)) {
            System.out.println("An error occurred while transferring, try again");
            return;
        } else {
            System.out.println("No errors found");
        }

        System.out.println("Process completed successfully, " + newList.length + " files transferred");
        client.finish();
    }
}