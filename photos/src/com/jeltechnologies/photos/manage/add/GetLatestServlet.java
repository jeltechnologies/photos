package com.jeltechnologies.photos.manage.add;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.background.BackgroundServices;
import com.jeltechnologies.photos.background.thumbs.GetLatestPicturesThread;
import com.jeltechnologies.photos.servlet.BaseServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/get-latest")
public class GetLatestServlet extends BaseServlet {
    private static final long serialVersionUID = -3618259294179327128L;

    private static final Logger LOGGER = LoggerFactory.getLogger(GetLatestServlet.class);

    private static final boolean TESTING = false;
    private Random random = new Random();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	String status = request.getParameter("status");
	if (status == null || status.equals("")) {
	    BackgroundServices services = BackgroundServices.getInstance();
	    @SuppressWarnings("rawtypes")
	    Future getLatestPayload = services.submitTask(new GetLatestPicturesThread());
	    try {
		getLatestPayload.get();
	    } catch (Exception e) {
		LOGGER.error("Error getting latest pictures", e);
	    }
	}
	GetLatestPayload payload = getPayload();
	respondJson(response, payload);
    }

    public GetLatestPayload getPayload() {
	BackgroundServices services = BackgroundServices.getInstance();
	GetLatestPayload payload = new GetLatestPayload();
	int thumbsSize = services.getThumbsQueueSize();
	int videoSize = services.getVideoQueueSize();

	if (TESTING) {
	    int dice = random.nextInt(100);
	    if (dice < 95) {
		thumbsSize = random.nextInt(10000);
		videoSize = random.nextInt(10000);
	    }
	}

	payload.setThumbsQueue(thumbsSize);
	payload.setVideosQueue(videoSize);
	if (thumbsSize == 0 && videoSize == 0) {
	    payload.setStatus("Done");
	} else {
	    payload.setStatus("Working");
	}
	return payload;
    }

}
