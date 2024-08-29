package fish.payara.fishmaps;

import fish.payara.fishmaps.messaging.Messenger;

import java.net.http.HttpClient;

public class HttpCaller implements Runnable {
    private volatile boolean shouldContinue = true;

    @Override
    public void run () {
        FishMapsMain.LOGGER.info("Starting HTTP service.");
        HttpClient client = HttpClient.newHttpClient();
        this.shouldContinue = true;

        while (this.shouldContinue) {
            try {
                Messenger.postFromCache(client);
            }
            catch (Exception e) {
                this.shouldContinue = false;
            }
        }

        FishMapsMain.LOGGER.info("Shutting down HTTP service.");
        client.close();
    }

    public void stop () {
        this.shouldContinue = false;
    }
}
